"""Envio dos ticks ao backend por WebSocket, com heartbeat e reconexão automática."""

from __future__ import annotations

import asyncio
import logging
from datetime import UTC, datetime

from websockets.asyncio.client import ClientConnection, connect
from websockets.exceptions import WebSocketException

from mt5_bridge.config import BridgeConfig
from mt5_bridge.protocol import CandleBatch, Heartbeat, HistoryRequest, Source
from mt5_bridge.sources import TickSource

log = logging.getLogger("mt5_bridge")

MAX_BACKOFF_S = 30.0


def next_backoff(current: float) -> float:
    """Espera entre tentativas de reconexão: dobra a cada falha, até 30 s."""
    return min(MAX_BACKOFF_S, max(1.0, current * 2))


async def answer_history(
    websocket: ClientConnection, source: TickSource, mt5_lock: asyncio.Lock
) -> None:
    """Responde aos pedidos de histórico do backend enquanto os ticks seguem saindo."""
    async for raw in websocket:
        request = HistoryRequest.from_json(raw)
        if request is None:
            continue
        try:
            async with mt5_lock:
                candles = await asyncio.to_thread(
                    source.history, request.ticker, request.timeframe, request.count
                )
            batch = CandleBatch(request.request_id, request.ticker, request.timeframe, candles)
        except LookupError:
            batch = CandleBatch(
                request.request_id, request.ticker, request.timeframe, [], error="unknown-ticker"
            )
        await websocket.send(batch.to_json())
        log.info(
            "Histórico %s %s: %d candles", request.ticker, request.timeframe, len(batch.candles)
        )


async def run(config: BridgeConfig, source: TickSource, source_kind: Source) -> None:
    # A biblioteca do MT5 não é feita para duas threads ao mesmo tempo: ticks e histórico se
    # revezam com esta trava.
    mt5_lock = asyncio.Lock()
    backoff = 1.0
    while True:
        try:
            async with connect(
                config.ingest_url,
                additional_headers={"Authorization": f"Bearer {config.ingest_token}"},
                open_timeout=10,
            ) as websocket:
                log.info("Conectado a %s", config.ingest_url)
                backoff = 1.0
                for asset in source.assets():
                    await websocket.send(asset.to_json())
                log.info("Cadastro de %d ativos enviado", len(source.assets()))
                source.reset()
                listener = asyncio.create_task(answer_history(websocket, source, mt5_lock))
                try:
                    last_heartbeat = 0.0
                    loop = asyncio.get_running_loop()
                    while True:
                        # A leitura do MT5 bloqueia; numa thread, o WebSocket continua
                        # respondendo aos pings enquanto centenas de ativos são lidos.
                        if listener.done():
                            listener.result()
                        async with mt5_lock:
                            ticks = await asyncio.to_thread(source.poll)
                        for tick in ticks:
                            await websocket.send(tick.to_json())
                            log.debug("%s %s", tick.ticker, tick.last)
                        now = loop.time()
                        if now - last_heartbeat >= config.heartbeat_interval_s:
                            beat = Heartbeat(sent_at=datetime.now(UTC), source=source_kind)
                            await websocket.send(beat.to_json())
                            last_heartbeat = now
                        await asyncio.sleep(config.poll_interval_ms / 1000)
                finally:
                    listener.cancel()
        except (OSError, WebSocketException, TimeoutError) as error:
            log.warning("Conexão perdida (%s). Nova tentativa em %.0f s", error, backoff)
            await asyncio.sleep(backoff)
            backoff = next_backoff(backoff)

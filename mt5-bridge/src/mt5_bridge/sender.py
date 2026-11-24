"""Envio dos ticks ao backend por WebSocket, com heartbeat e reconexão automática."""

from __future__ import annotations

import asyncio
import logging
from datetime import UTC, datetime

from websockets.asyncio.client import connect
from websockets.exceptions import WebSocketException

from mt5_bridge.config import BridgeConfig
from mt5_bridge.protocol import Heartbeat, Source
from mt5_bridge.sources import TickSource

log = logging.getLogger("mt5_bridge")

MAX_BACKOFF_S = 30.0


def next_backoff(current: float) -> float:
    """Espera entre tentativas de reconexão: dobra a cada falha, até 30 s."""
    return min(MAX_BACKOFF_S, max(1.0, current * 2))


async def run(config: BridgeConfig, source: TickSource, source_kind: Source) -> None:
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
                last_heartbeat = 0.0
                loop = asyncio.get_running_loop()
                while True:
                    for tick in source.poll():
                        await websocket.send(tick.to_json())
                        log.debug("%s %s", tick.ticker, tick.last)
                    now = loop.time()
                    if now - last_heartbeat >= config.heartbeat_interval_s:
                        beat = Heartbeat(sent_at=datetime.now(UTC), source=source_kind)
                        await websocket.send(beat.to_json())
                        last_heartbeat = now
                    await asyncio.sleep(config.poll_interval_ms / 1000)
        except (OSError, WebSocketException, TimeoutError) as error:
            log.warning("Conexão perdida (%s). Nova tentativa em %.0f s", error, backoff)
            await asyncio.sleep(backoff)
            backoff = next_backoff(backoff)

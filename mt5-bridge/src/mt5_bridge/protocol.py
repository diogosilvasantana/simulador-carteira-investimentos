"""Mensagens enviadas pela ponte ao endpoint de ingestão do backend.

O mesmo formato é usado pelo simulador de pregão, para que o backend não saiba se o tick veio do
MT5, de um replay ou de um gerador sintético.
"""

from __future__ import annotations

import json
from dataclasses import asdict, dataclass
from datetime import UTC, datetime
from decimal import Decimal
from enum import StrEnum


class Source(StrEnum):
    LIVE = "LIVE"
    REPLAY = "REPLAY"
    SYNTHETIC = "SYNTHETIC"


@dataclass(frozen=True, slots=True)
class Tick:
    """Atualização de preço de um ativo, com preços em Decimal para não perder centavos."""

    ticker: str
    last: Decimal
    bid: Decimal | None
    ask: Decimal | None
    volume: int
    occurred_at: datetime
    source: Source = Source.LIVE
    previous_close: Decimal | None = None

    def __post_init__(self) -> None:
        if not self.ticker:
            raise ValueError("ticker vazio")
        if self.last <= 0:
            raise ValueError(f"preço inválido para {self.ticker}: {self.last}")
        if self.occurred_at.tzinfo is None:
            raise ValueError("occurred_at precisa de fuso horário")

    def to_json(self) -> str:
        payload = asdict(self)
        payload["type"] = "tick"
        payload["last"] = str(self.last)
        payload["bid"] = None if self.bid is None else str(self.bid)
        payload["ask"] = None if self.ask is None else str(self.ask)
        payload["previous_close"] = (
            None if self.previous_close is None else str(self.previous_close)
        )
        payload["occurred_at"] = to_instant(self.occurred_at)
        payload["source"] = self.source.value
        return json.dumps(payload, separators=(",", ":"))


@dataclass(frozen=True, slots=True)
class AssetInfo:
    """Cadastro de um ativo como o MT5 informa; o backend classifica pela descrição (ADR-010)."""

    ticker: str
    description: str
    path: str = ""

    def to_json(self) -> str:
        payload = asdict(self)
        payload["type"] = "asset"
        return json.dumps(payload, separators=(",", ":"), ensure_ascii=False)


@dataclass(frozen=True, slots=True)
class Heartbeat:
    """Sinal de vida enviado a cada 5 s; sem ele por 15 s, o backend marca o feed como STALE."""

    sent_at: datetime
    source: Source = Source.LIVE

    def to_json(self) -> str:
        return json.dumps(
            {
                "type": "heartbeat",
                "sent_at": to_instant(self.sent_at),
                "source": self.source.value,
            },
            separators=(",", ":"),
        )


class Timeframe(StrEnum):
    """Duração de cada candle pedido pelo backend."""

    M5 = "M5"
    M30 = "M30"
    H1 = "H1"
    D1 = "D1"
    W1 = "W1"

    @property
    def is_daily(self) -> bool:
        return self in (Timeframe.D1, Timeframe.W1)


# Maior histórico que o backend pode pedir de uma vez (5 anos de semanas, 1 ano de dias).
MAX_CANDLES = 1500


@dataclass(frozen=True, slots=True)
class Candle:
    """Barra de preços. Em candles diários e semanais, opened_at é a abertura do pregão no dia."""

    opened_at: datetime
    open: Decimal
    high: Decimal
    low: Decimal
    close: Decimal
    volume: int

    def to_payload(self) -> dict[str, object]:
        return {
            "opened_at": to_instant(self.opened_at),
            "open": str(self.open),
            "high": str(self.high),
            "low": str(self.low),
            "close": str(self.close),
            "volume": self.volume,
        }


@dataclass(frozen=True, slots=True)
class HistoryRequest:
    """Pedido de histórico que o backend manda pela mesma conexão. A ponte só lê do MT5."""

    request_id: str
    ticker: str
    timeframe: Timeframe
    count: int

    @staticmethod
    def from_json(raw: str | bytes) -> HistoryRequest | None:
        """Lê o pedido; qualquer outra mensagem, ou um pedido malformado, devolve None."""
        try:
            data = json.loads(raw)
            if not isinstance(data, dict) or data.get("type") != "history":
                return None
            count = int(data["count"])
            request = HistoryRequest(
                request_id=str(data["request_id"]),
                ticker=str(data["ticker"]).upper(),
                timeframe=Timeframe(data["timeframe"]),
                count=min(max(count, 1), MAX_CANDLES),
            )
        except (ValueError, KeyError, TypeError):
            return None
        return request if request.ticker and request.request_id else None


@dataclass(frozen=True, slots=True)
class CandleBatch:
    """Resposta a um pedido de histórico, com erro em vez de candles se o ativo não existir."""

    request_id: str
    ticker: str
    timeframe: Timeframe
    candles: list[Candle]
    error: str | None = None

    def to_json(self) -> str:
        return json.dumps(
            {
                "type": "candles",
                "request_id": self.request_id,
                "ticker": self.ticker,
                "timeframe": self.timeframe.value,
                "candles": [c.to_payload() for c in self.candles],
                "error": self.error,
            },
            separators=(",", ":"),
        )


def to_instant(moment: datetime) -> str:
    """ISO-8601 em UTC com sufixo Z, o formato que o Instant.parse do Java lê."""
    return moment.astimezone(UTC).isoformat().replace("+00:00", "Z")

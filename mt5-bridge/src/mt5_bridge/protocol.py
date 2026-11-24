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
        payload["occurred_at"] = to_instant(self.occurred_at)
        payload["source"] = self.source.value
        return json.dumps(payload, separators=(",", ":"))


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


def to_instant(moment: datetime) -> str:
    """ISO-8601 em UTC com sufixo Z, o formato que o Instant.parse do Java lê."""
    return moment.astimezone(UTC).isoformat().replace("+00:00", "Z")

"""Configuração da ponte, lida só de variáveis de ambiente (nenhum segredo no código)."""

from __future__ import annotations

import os
from collections.abc import Mapping
from dataclasses import dataclass


@dataclass(frozen=True, slots=True)
class BridgeConfig:
    """Configuração da ponte. ``symbols == ("ALL",)`` pede todos os ativos à vista da corretora."""

    ingest_url: str
    ingest_token: str
    symbols: tuple[str, ...]
    poll_interval_ms: int = 250
    heartbeat_interval_s: int = 5

    @classmethod
    def from_env(cls, env: Mapping[str, str] | None = None) -> BridgeConfig:
        source = os.environ if env is None else env
        try:
            url = source["BRIDGE_INGEST_URL"]
            token = source["BRIDGE_INGEST_TOKEN"]
        except KeyError as missing:
            raise ValueError(f"variável de ambiente obrigatória ausente: {missing}") from None
        symbols = tuple(
            s.strip().upper() for s in source.get("BRIDGE_SYMBOLS", "").split(",") if s.strip()
        )
        if not symbols:
            raise ValueError("BRIDGE_SYMBOLS precisa ter ao menos um ativo")
        return cls(
            ingest_url=url,
            ingest_token=token,
            symbols=symbols,
            poll_interval_ms=int(source.get("BRIDGE_POLL_INTERVAL_MS", "250")),
        )

"""Linha de comando da ponte.

Exemplos (PowerShell):
    $env:BRIDGE_INGEST_TOKEN = "local-dev-token"
    python -m mt5_bridge --source mt5 --symbols PETR4,VALE3,MXRF11
    python -m mt5_bridge --source synthetic --symbols PETR4,VALE3 --seed 7
"""

from __future__ import annotations

import argparse
import asyncio
import logging
import os
import sys

from mt5_bridge.config import BridgeConfig
from mt5_bridge.protocol import Source
from mt5_bridge.sender import run
from mt5_bridge.sources import ALL, Mt5Source, SyntheticSource, TickSource


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(prog="mt5-bridge", description="Ponte MT5 → simulador")
    parser.add_argument("--source", choices=["mt5", "synthetic"], default="mt5")
    parser.add_argument(
        "--symbols",
        help="Lista separada por vírgula, ou 'all' para todas as ações, FIIs, ETFs e units "
        "(sobrepõe BRIDGE_SYMBOLS)",
    )
    parser.add_argument(
        "--server-utc-offset",
        type=int,
        help="Fuso do servidor MT5 em horas (ex.: -3). Sem ele, a ponte detecta pelo tick mais "
        "recente, o que só funciona com o mercado aberto",
    )
    parser.add_argument("--url", help="Endpoint de ingestão (sobrepõe BRIDGE_INGEST_URL)")
    parser.add_argument(
        "--interval-ms",
        type=int,
        help="Intervalo entre leituras (sobrepõe BRIDGE_POLL_INTERVAL_MS). Padrão: 250 ms, ou "
        "1000 ms com --symbols all",
    )
    parser.add_argument("--seed", type=int, default=42, help="Semente do modo sintético")
    parser.add_argument(
        "--volatility",
        type=float,
        default=2.0,
        help="Volatilidade anual do modo sintético. O padrão é exagerado para os preços "
        "mudarem a cada poucos segundos; 0.3 é o realista",
    )
    parser.add_argument("--verbose", action="store_true", help="Mostra cada tick enviado")
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> None:
    args = parse_args(argv)
    logging.basicConfig(
        level=logging.DEBUG if args.verbose else logging.INFO,
        format="%(asctime)s %(levelname)s %(message)s",
    )
    env = dict(os.environ)
    env.setdefault("BRIDGE_INGEST_URL", "ws://localhost:8080/ingest")
    if args.url:
        env["BRIDGE_INGEST_URL"] = args.url
    if args.symbols:
        env["BRIDGE_SYMBOLS"] = args.symbols
    if args.interval_ms:
        env["BRIDGE_POLL_INTERVAL_MS"] = str(args.interval_ms)
    elif env.get("BRIDGE_SYMBOLS", "").strip().upper() == ALL:
        # Ler ~2.500 ativos a cada 250 ms ocupa a CPU à toa; 1 s basta para uma lista.
        env.setdefault("BRIDGE_POLL_INTERVAL_MS", "1000")
    try:
        config = BridgeConfig.from_env(env)
    except ValueError as error:
        sys.exit(f"Configuração inválida: {error}")

    source: TickSource
    if args.source == "mt5":
        source = Mt5Source(config.symbols, server_utc_offset_h=args.server_utc_offset)
        kind = Source.LIVE
    else:
        source = SyntheticSource(
            config.symbols,
            seed=args.seed,
            annual_volatility=args.volatility,
            interval_ms=config.poll_interval_ms,
        )
        kind = Source.SYNTHETIC

    names = source.symbols
    shown = ", ".join(names[:10]) + (f" e mais {len(names) - 10}" if len(names) > 10 else "")
    logging.info("Fonte %s com %d ativos: %s", args.source, len(names), shown)
    try:
        asyncio.run(run(config, source, kind))
    except KeyboardInterrupt:
        logging.info("Encerrado pelo usuário")
    finally:
        source.close()


if __name__ == "__main__":
    main()

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
from mt5_bridge.sources import Mt5Source, SyntheticSource, TickSource


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(prog="mt5-bridge", description="Ponte MT5 → simulador")
    parser.add_argument("--source", choices=["mt5", "synthetic"], default="mt5")
    parser.add_argument("--symbols", help="Lista separada por vírgula (sobrepõe BRIDGE_SYMBOLS)")
    parser.add_argument("--url", help="Endpoint de ingestão (sobrepõe BRIDGE_INGEST_URL)")
    parser.add_argument("--seed", type=int, default=42, help="Semente do modo sintético")
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
    try:
        config = BridgeConfig.from_env(env)
    except ValueError as error:
        sys.exit(f"Configuração inválida: {error}")

    source: TickSource
    if args.source == "mt5":
        source = Mt5Source(config.symbols)
        kind = Source.LIVE
    else:
        source = SyntheticSource(
            config.symbols, seed=args.seed, interval_ms=config.poll_interval_ms
        )
        kind = Source.SYNTHETIC

    logging.info("Fonte %s com %s", args.source, ", ".join(config.symbols))
    try:
        asyncio.run(run(config, source, kind))
    except KeyboardInterrupt:
        logging.info("Encerrado pelo usuário")
    finally:
        source.close()


if __name__ == "__main__":
    main()

"""
Spike da ponte MT5 -> simulador de carteira.

Objetivo: responder, em ~2 minutos de pregão, as perguntas que podem
inviabilizar a arquitetura (ADR-003):
  1. O pacote oficial MetaTrader5 conecta no seu terminal?
  2. A corretora libera os ativos da B3 que você quer?
  3. Com que frequência chegam ticks e com quanto atraso?

Este script SÓ LÊ dados. Não envia ordens nem altera nada na conta.

Pré-requisitos (no Windows, com o MT5 aberto e logado na corretora):
    pip install MetaTrader5
    python mt5_spike.py                      # ativos padrão, 120 s
    python mt5_spike.py PETR4 VALE3 --seconds 60
"""

from __future__ import annotations

import argparse
import csv
import statistics
import sys
import time
from datetime import UTC, datetime

try:
    import MetaTrader5 as mt5  # noqa: N813 (alias usado na documentação oficial)
except ImportError:
    sys.exit("Pacote não encontrado. Rode: pip install MetaTrader5")

DEFAULT_SYMBOLS = ["PETR4", "VALE3", "ITUB4", "BBAS3", "WEGE3", "MXRF11", "BOVA11"]
POLL_INTERVAL_S = 0.25  # o mesmo intervalo previsto para a ponte


def connect() -> None:
    if not mt5.initialize():
        sys.exit(
            f"Falha ao conectar no terminal MT5: {mt5.last_error()}\n"
            "Confira se o MT5 está aberto e logado na corretora."
        )
    info = mt5.terminal_info()
    account = mt5.account_info()
    print(f"Terminal: {info.name} build {info.build} | conectado: {info.connected}")
    if account:
        print(f"Corretora: {account.company} | servidor: {account.server}")
    print()


def check_symbols(symbols: list[str]) -> list[str]:
    """Verifica quais ativos existem na corretora e os assina na Observação do Mercado."""
    available = []
    print(f"{'Ativo':<10}{'Disponível':<12}{'Descrição'}")
    for symbol in symbols:
        info = mt5.symbol_info(symbol)
        if info is None:
            print(f"{symbol:<10}{'NÃO':<12}não existe nesta corretora")
            continue
        if not info.visible and not mt5.symbol_select(symbol, True):
            print(f"{symbol:<10}{'NÃO':<12}não foi possível assinar")
            continue
        print(f"{symbol:<10}{'sim':<12}{info.description}")
        available.append(symbol)
    print()
    return available


def watch(symbols: list[str], seconds: int, csv_path: str) -> None:
    """Faz polling como a ponte faria e mede frequência e atraso dos ticks."""
    last_time_msc: dict[str, int] = {}
    changes: dict[str, int] = {s: 0 for s in symbols}
    delays_ms: dict[str, list[float]] = {s: [] for s in symbols}

    print(f"Observando {len(symbols)} ativos por {seconds} s (Ctrl+C para parar)...")
    end = time.monotonic() + seconds
    with open(csv_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(
            ["recebido_utc", "ativo", "tick_utc", "bid", "ask", "last", "volume", "atraso_ms"]
        )
        try:
            while time.monotonic() < end:
                for symbol in symbols:
                    tick = mt5.symbol_info_tick(symbol)
                    if tick is None or tick.time_msc == last_time_msc.get(symbol):
                        continue
                    last_time_msc[symbol] = tick.time_msc
                    now = datetime.now(UTC)
                    tick_dt = datetime.fromtimestamp(tick.time_msc / 1000, tz=UTC)
                    # Atenção: o horário do tick vem do servidor da corretora. Se o atraso
                    # medido for de horas, é fuso do servidor, não lentidão real.
                    delay = (now - tick_dt).total_seconds() * 1000
                    changes[symbol] += 1
                    delays_ms[symbol].append(delay)
                    writer.writerow(
                        [
                            now.isoformat(),
                            symbol,
                            tick_dt.isoformat(),
                            tick.bid,
                            tick.ask,
                            tick.last,
                            tick.volume,
                            round(delay),
                        ]
                    )
                time.sleep(POLL_INTERVAL_S)
        except KeyboardInterrupt:
            print("Interrompido.")

    print()
    print(f"{'Ativo':<10}{'Ticks':>7}{'Ticks/min':>11}{'Atraso p50':>13}{'Atraso p95':>13}")
    for symbol in symbols:
        d = sorted(delays_ms[symbol])
        per_min = changes[symbol] / (seconds / 60)
        if d:
            p50 = statistics.median(d)
            p95 = d[min(len(d) - 1, int(len(d) * 0.95))]
            print(f"{symbol:<10}{changes[symbol]:>7}{per_min:>11.1f}{p50:>11.0f}ms{p95:>11.0f}ms")
        else:
            print(f"{symbol:<10}{0:>7}{0:>11.1f}{'-':>13}{'-':>13}")
    print(f"\nTicks gravados em {csv_path}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Spike da ponte MT5 (somente leitura)")
    parser.add_argument("symbols", nargs="*", default=DEFAULT_SYMBOLS)
    parser.add_argument("--seconds", type=int, default=120)
    parser.add_argument("--csv", default="spike_ticks.csv")
    args = parser.parse_args()

    connect()
    try:
        available = check_symbols([s.upper() for s in args.symbols])
        if not available:
            sys.exit(
                "Nenhum ativo disponível. Veja na Observação do Mercado "
                "como a corretora nomeia os ativos."
            )
        watch(available, args.seconds, args.csv)
    finally:
        mt5.shutdown()


if __name__ == "__main__":
    main()

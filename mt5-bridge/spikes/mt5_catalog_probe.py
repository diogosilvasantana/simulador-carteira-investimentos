"""Sonda do catálogo do MT5: o que a corretora informa sobre cada ativo (somente leitura).

Responde se dá para separar tipo, setor e segmento só com dados do MT5. Não envia ordens.

Uso (PowerShell, com o MT5 aberto e logado):
    .venv\\Scripts\\python spikes\\mt5_catalog_probe.py
    .venv\\Scripts\\python spikes\\mt5_catalog_probe.py --csv catalogo.csv

Mande a saída do terminal (não o CSV) para analisarmos.
"""

from __future__ import annotations

import argparse
import csv
import re
from collections import Counter
from typing import Any

import MetaTrader5 as mt5  # noqa: N813 (alias da documentação oficial)

# Ações, units, FIIs, ETFs e BDRs, sem fracionário, opções, direitos e futuros.
CANDIDATES = re.compile(r"^[A-Z0-9]{4}(3|4|5|6|7|8|11|31|32|33|34|35|39)$")
SAMPLES = [
    "PETR4",
    "VALE3",
    "ITUB4",
    "TAEE11",
    "SANB11",
    "MXRF11",
    "HGLG11",
    "BOVA11",
    "IVVB11",
    "AAPL34",
    "WEGE3",
    "RURA11",
]
INDICES = ["IBOV", "IFIX", "SMLL", "IDIV", "IBXX", "IBXL", "IMOB", "IFNC", "ICON", "IEEX", "UTIL"]
# Campos que podem trazer classificação, conforme a versão do terminal.
CLASS_FIELDS = [
    "path",
    "sector",
    "industry",
    "sector_name",
    "industry_name",
    "category",
    "exchange",
    "isin",
    "basis",
    "currency_base",
]


def as_dict(info: Any) -> dict[str, Any]:
    return dict(info._asdict()) if info is not None else {}


def main() -> None:
    parser = argparse.ArgumentParser(description="Sonda do catálogo do MT5 (somente leitura)")
    parser.add_argument("--csv", help="Salva todos os ativos candidatos neste arquivo")
    args = parser.parse_args()

    if not mt5.initialize():
        raise SystemExit(f"Falha ao conectar no MT5: {mt5.last_error()}")
    try:
        terminal = mt5.terminal_info()
        account = mt5.account_info()
        print(
            f"Terminal build {mt5.version()} | corretora {getattr(terminal, 'company', '?')}"
            f" | servidor {getattr(account, 'server', '?')}"
        )

        symbols = mt5.symbols_get() or ()
        print(f"\nTotal de símbolos na corretora: {len(symbols)}")
        top = Counter(s.path.split("\\")[0] if s.path else "(sem pasta)" for s in symbols)
        print("Pastas de primeiro nível:")
        for folder, count in top.most_common(20):
            print(f"  {count:6d}  {folder}")

        rows = [as_dict(s) for s in symbols if CANDIDATES.match(s.name)]
        print(f"\nCandidatos (ações, units, FIIs, ETFs, BDRs): {len(rows)}")
        available = [f for f in CLASS_FIELDS if rows and f in rows[0]]
        missing = [f for f in CLASS_FIELDS if f not in available]
        print(f"Campos de classificação presentes: {', '.join(available) or 'nenhum'}")
        print(f"Campos ausentes nesta versão: {', '.join(missing) or 'nenhum'}")

        for field in available:
            if field == "isin":
                filled = sum(1 for r in rows if r.get(field))
                print(f"\n{field}: preenchido em {filled} de {len(rows)}")
                continue
            values = Counter(str(r.get(field)) for r in rows)
            print(f"\n{field}: {len(values)} valores distintos (mais comuns)")
            for value, count in values.most_common(15):
                print(f"  {count:6d}  {value!r}")

        by_suffix: dict[str, list[str]] = {}
        for r in rows:
            suffix = re.sub(r"^[A-Z0-9]{4}", "", r["name"])
            by_suffix.setdefault(suffix, []).append(f"{r['name']}: {r.get('description', '')}")
        print("\nDescrições por sufixo (até 6 exemplos):")
        for suffix in sorted(by_suffix, key=lambda s: (len(s), s)):
            print(f"  [{suffix}] {len(by_suffix[suffix])} ativos")
            for line in by_suffix[suffix][:6]:
                print(f"      {line}")

        print("\nExemplos com todos os campos de classificação:")
        for name in SAMPLES:
            info = as_dict(mt5.symbol_info(name))
            if not info:
                print(f"  {name}: não existe nesta corretora")
                continue
            picked = {k: info.get(k) for k in ["description", *available]}
            print(f"  {name}: {picked}")

        print("\nÍndices:")
        for name in INDICES:
            info = mt5.symbol_info(name)
            print(
                f"  {name}: {'disponível' if info is not None else 'não encontrado'}"
                + (f" ({info.path})" if info is not None else "")
            )

        if args.csv:
            fields = ["name", "description", *[f for f in available if f != "description"]]
            with open(args.csv, "w", newline="", encoding="utf-8") as out:
                writer = csv.DictWriter(out, fieldnames=fields, extrasaction="ignore")
                writer.writeheader()
                writer.writerows(rows)
            print(f"\nCSV salvo em {args.csv}")
    finally:
        mt5.shutdown()


if __name__ == "__main__":
    main()

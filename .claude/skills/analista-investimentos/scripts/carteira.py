#!/usr/bin/env python3
"""Pesos, concentração e aporte sem vendas. Uso: carteira.py entrada.json

Entrada: {"cash": "1000.00", "maxWeight": "10", "assets": [
  {"ticker": "PETR4", "price": "38.47", "quantity": "10", "target": "50", "lot": "1"}]}

`target` e `maxWeight` em %; alvos somam 100 incluindo um eventual ativo de caixa/renda fixa.
`lot` é a menor quantidade negociável. `maxWeight` (opcional) só gera alerta.
"""

import json
import sys
from decimal import ROUND_FLOOR, ROUND_HALF_UP, Decimal, InvalidOperation

CENT = Decimal("0.01")
HUNDRED = Decimal(100)


def decimal(value: object, field: str) -> Decimal:
    # float já chega com erro binário; texto preserva o centavo.
    if not isinstance(value, str):
        raise ValueError(f"{field}: informe como texto decimal")
    try:
        number = Decimal(value)
    except InvalidOperation:
        raise ValueError(f"{field}: número inválido") from None
    if not number.is_finite() or number < 0:
        raise ValueError(f"{field}: deve ser finito e não negativo")
    return number


def plan(data: dict) -> dict:
    cash = decimal(data["cash"], "cash")
    limit = decimal(data.get("maxWeight", "100"), "maxWeight")
    items = []
    for raw in data["assets"]:
        name = raw["ticker"]
        item = {
            "ticker": name,
            "price": decimal(raw["price"], f"{name}.price"),
            "quantity": decimal(raw.get("quantity", "0"), f"{name}.quantity"),
            "target": decimal(raw["target"], f"{name}.target"),
            "lot": decimal(raw.get("lot", "1"), f"{name}.lot"),
        }
        if item["price"] == 0 or item["lot"] == 0:
            raise ValueError(f"{name}: price e lot devem ser maiores que zero")
        items.append(item)
    if len({i["ticker"] for i in items}) != len(items):
        raise ValueError("ticker repetido")
    if sum((i["target"] for i in items), Decimal(0)) != HUNDRED:
        raise ValueError("os alvos devem somar 100")

    total = cash + sum((i["price"] * i["quantity"] for i in items), Decimal(0))
    if total == 0:
        raise ValueError("carteira sem valor")
    gaps = {
        i["ticker"]: max(Decimal(0), total * i["target"] / HUNDRED - i["price"] * i["quantity"])
        for i in items
    }
    need = sum(gaps.values(), Decimal(0))
    # Quando algo está acima do alvo, o caixa não cobre todos os déficits: divide na proporção.
    scale = min(Decimal(1), cash / need) if need else Decimal(0)
    remaining = cash
    result, alerts = [], []
    for i in items:
        step = i["price"] * i["lot"]
        lots = (gaps[i["ticker"]] * scale / step).to_integral_value(ROUND_FLOOR)
        # O centavo arredondado é o que sai do caixa; sem esta trava a soma poderia passar dele.
        while lots and (lots * step).quantize(CENT, ROUND_HALF_UP) > remaining:
            lots -= 1
        cost = (lots * step).quantize(CENT, ROUND_HALF_UP)
        remaining -= cost
        final = i["price"] * (i["quantity"] + lots * i["lot"])
        weight = (final / total * HUNDRED).quantize(CENT, ROUND_HALF_UP)
        if weight > limit:
            alerts.append(f"{i['ticker']}: {weight}% acima do limite de {limit}%")
        result.append(
            {
                "ticker": i["ticker"],
                "buyQuantity": format(lots * i["lot"], "f"),
                "buyValue": str(cost),
                "finalWeight": str(weight),
                "deviation": str(weight - i["target"]),
            }
        )
    return {
        "total": str(total.quantize(CENT, ROUND_HALF_UP)),
        "assets": result,
        "remainingCash": str(remaining.quantize(CENT)),
        "alerts": alerts,
        "note": "Sem custos de operação; nenhuma venda é sugerida.",
    }


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__, file=sys.stderr)
        return 2
    try:
        with open(sys.argv[1], encoding="utf-8") as file:
            output = plan(json.load(file))
    except (OSError, ValueError, KeyError, TypeError) as error:
        print(f"Entrada inválida: {error}", file=sys.stderr)
        return 1
    print(json.dumps(output, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    sys.exit(main())

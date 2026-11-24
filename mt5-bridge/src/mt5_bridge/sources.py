"""Fontes de ticks: o terminal MetaTrader 5 ou um gerador sintético para o mercado fechado."""

from __future__ import annotations

import math
import random
from collections.abc import Iterable, Mapping
from datetime import UTC, datetime
from decimal import ROUND_HALF_EVEN, Decimal
from typing import Any, Protocol

from mt5_bridge.protocol import Source, Tick

CENT = Decimal("0.01")

# Preços iniciais do modo sintético, só para os valores parecerem plausíveis.
DEFAULT_PRICES: Mapping[str, Decimal] = {
    "PETR4": Decimal("38.50"),
    "VALE3": Decimal("61.20"),
    "ITUB4": Decimal("36.10"),
    "BBAS3": Decimal("27.80"),
    "WEGE3": Decimal("52.40"),
    "MXRF11": Decimal("9.75"),
    "BOVA11": Decimal("128.40"),
}


class TickSource(Protocol):
    """Devolve os ticks novos desde a última chamada."""

    def poll(self) -> list[Tick]: ...

    def close(self) -> None: ...


def to_price(value: float | Decimal) -> Decimal:
    return Decimal(str(value)).quantize(CENT, rounding=ROUND_HALF_EVEN)


class SyntheticSource:
    """Passeio aleatório (movimento browniano geométrico) por ativo, com semente fixa.

    A mesma semente gera sempre a mesma sequência de preços, o que permite repetir testes.
    """

    def __init__(
        self,
        symbols: Iterable[str],
        *,
        seed: int = 42,
        annual_volatility: float = 0.30,
        interval_ms: int = 250,
        trade_probability: float = 0.6,
        clock: Any = None,
    ) -> None:
        self._random = random.Random(seed)
        self._prices = {s: DEFAULT_PRICES.get(s, Decimal("50.00")) for s in symbols}
        # Pregão de ~7 h em 252 dias por ano.
        steps_per_year = 252 * 7 * 3600 * 1000 / interval_ms
        self._sigma = annual_volatility / math.sqrt(steps_per_year)
        self._trade_probability = trade_probability
        self._clock = clock or (lambda: datetime.now(UTC))

    def poll(self) -> list[Tick]:
        ticks: list[Tick] = []
        now = self._clock()
        for symbol, price in self._prices.items():
            if self._random.random() > self._trade_probability:
                continue
            shock = self._random.gauss(0.0, 1.0)
            factor = math.exp(-0.5 * self._sigma**2 + self._sigma * shock)
            new_price = max(CENT, to_price(float(price) * factor))
            self._prices[symbol] = new_price
            ticks.append(
                Tick(
                    ticker=symbol,
                    last=new_price,
                    bid=new_price - CENT if new_price > CENT else None,
                    ask=new_price + CENT,
                    volume=self._random.randint(1, 50) * 100,
                    occurred_at=now,
                    source=Source.SYNTHETIC,
                )
            )
        return ticks

    def close(self) -> None:
        pass


class Mt5Source:
    """Lê ticks do terminal MetaTrader 5 aberto e logado (só Windows). Não envia ordens."""

    def __init__(self, symbols: Iterable[str]) -> None:
        import MetaTrader5 as mt5  # noqa: N813 (alias da documentação oficial)

        self._mt5 = mt5
        if not mt5.initialize():
            raise RuntimeError(f"Falha ao conectar no MT5: {mt5.last_error()}")
        self._symbols: list[str] = []
        for symbol in symbols:
            if mt5.symbol_info(symbol) is None or not mt5.symbol_select(symbol, True):
                print(f"[mt5] ativo indisponível na corretora, ignorado: {symbol}")
                continue
            self._symbols.append(symbol)
        if not self._symbols:
            raise RuntimeError("Nenhum dos ativos pedidos está disponível no MT5")
        self._last_time_msc: dict[str, int] = {}

    @property
    def symbols(self) -> list[str]:
        return list(self._symbols)

    def poll(self) -> list[Tick]:
        ticks: list[Tick] = []
        for symbol in self._symbols:
            raw = self._mt5.symbol_info_tick(symbol)
            if raw is None or raw.time_msc == self._last_time_msc.get(symbol):
                continue
            self._last_time_msc[symbol] = raw.time_msc
            tick = to_tick(symbol, raw)
            if tick is not None:
                ticks.append(tick)
        return ticks

    def close(self) -> None:
        self._mt5.shutdown()


def to_tick(symbol: str, raw: Any) -> Tick | None:
    """Converte o tick do MT5. Sem último negócio (pré-abertura), usa a melhor oferta de compra."""
    bid = to_price(raw.bid) if raw.bid else None
    ask = to_price(raw.ask) if raw.ask else None
    last = to_price(raw.last) if raw.last else bid
    if last is None or last <= 0:
        return None
    if bid is not None and ask is not None and bid > ask:
        bid, ask = None, None
    return Tick(
        ticker=symbol,
        last=last,
        bid=bid,
        ask=ask,
        volume=int(raw.volume),
        # O horário vem do servidor da corretora; convertido para UTC pelo epoch em milissegundos.
        occurred_at=datetime.fromtimestamp(raw.time_msc / 1000, tz=UTC),
        source=Source.LIVE,
    )

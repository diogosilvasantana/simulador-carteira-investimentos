"""Fontes de ticks: o terminal MetaTrader 5 ou um gerador sintético para o mercado fechado."""

from __future__ import annotations

import logging
import math
import random
import re
import time
from collections.abc import Callable, Iterable, Mapping, Sequence
from datetime import UTC, date, datetime, timedelta
from decimal import ROUND_HALF_EVEN, Decimal
from typing import Any, Protocol

from mt5_bridge.protocol import AssetInfo, Candle, Source, Tick, Timeframe

log = logging.getLogger("mt5_bridge")

CENT = Decimal("0.01")
SECONDS_PER_DAY = 86_400
ALL = "ALL"

# Ler o candle diário pode baixar histórico do servidor e levar centenas de ms por ativo. Limitar
# por leitura evita travar a primeira passada do modo all, que tem centenas de ativos.
HISTORY_LOADS_PER_POLL = 50

# Mercado à vista da vitrine: ações (3 a 8), units, FIIs, Fiagros e ETFs (11) e BDRs (31 a 39).
# Ficam de fora opções, futuros, fracionário (F), direitos e recibos (1, 2, 9, 10, 12, 13).
STOREFRONT = re.compile(r"^[A-Z0-9]{4}(3|4|5|6|7|8|11|31|32|33|34|35|39)$")
CASH_MARKET_PATH = "BOVESPA\\A VISTA\\"

# Descrições no formato da B3 para o modo sintético, como o MT5 da XP as mostra.
SYNTHETIC_DESCRIPTIONS: Mapping[str, str] = {
    "PETR4": "PETROBRAS   PN      N2",
    "VALE3": "VALE        ON      NM",
    "ITUB4": "ITAUUNIBANCOPN      N1",
    "BBAS3": "BRASIL      ON      NM",
    "BBDC4": "BRADESCO    PN      N1",
    "ABEV3": "AMBEV S/A   ON",
    "WEGE3": "WEG         ON      NM",
    "B3SA3": "B3          ON      NM",
    "RENT3": "LOCALIZA    ON      NM",
    "SUZB3": "SUZANO S.A. ON      NM",
    "MXRF11": "FII MAXI RENCI",
    "HGLG11": "FII HGLG PAXCI",
    "KNRI11": "FII KINEA   CI",
    "BOVA11": "ISHARES BOVACI",
    "IVVB11": "ISHARE SP500CI",
}

# Pregão regular da B3 em UTC: 10h às 17h de Brasília, que não tem horário de verão desde 2019.
SESSION_OPEN_UTC_H = 13
SESSION_CLOSE_UTC_H = 20

STEPS: Mapping[Timeframe, timedelta] = {
    Timeframe.M5: timedelta(minutes=5),
    Timeframe.M30: timedelta(minutes=30),
    Timeframe.H1: timedelta(hours=1),
    Timeframe.D1: timedelta(days=1),
    Timeframe.W1: timedelta(weeks=1),
}

# Preços iniciais do modo sintético, só para os valores parecerem plausíveis.
DEFAULT_PRICES: Mapping[str, Decimal] = {
    "PETR4": Decimal("38.50"),
    "VALE3": Decimal("61.20"),
    "ITUB4": Decimal("36.10"),
    "BBAS3": Decimal("27.80"),
    "BBDC4": Decimal("15.90"),
    "ABEV3": Decimal("13.20"),
    "WEGE3": Decimal("52.40"),
    "B3SA3": Decimal("12.80"),
    "RENT3": Decimal("44.60"),
    "SUZB3": Decimal("55.10"),
    "MXRF11": Decimal("9.75"),
    "HGLG11": Decimal("158.30"),
    "KNRI11": Decimal("139.90"),
    "BOVA11": Decimal("128.40"),
    "IVVB11": Decimal("352.10"),
}


class TickSource(Protocol):
    """Devolve os ticks novos desde a última chamada."""

    @property
    def symbols(self) -> list[str]: ...

    def assets(self) -> list[AssetInfo]:
        """Cadastro dos ativos acompanhados, enviado a cada conexão."""
        ...

    def poll(self) -> list[Tick]: ...

    def history(self, ticker: str, timeframe: Timeframe, count: int) -> list[Candle]:
        """Últimos candles do ativo, do mais antigo ao mais recente. LookupError se não existir."""
        ...

    def reset(self) -> None:
        """Esquece o que já foi enviado, para a próxima leitura mandar tudo de novo."""
        ...

    def close(self) -> None: ...


def to_price(value: float | Decimal) -> Decimal:
    return Decimal(str(value)).quantize(CENT, rounding=ROUND_HALF_EVEN)


def is_storefront_symbol(name: str, path: str) -> bool:
    """Ativo do mercado à vista que entra na vitrine (o backend ainda classifica o tipo)."""
    return path.startswith(CASH_MARKET_PATH) and STOREFRONT.match(name) is not None


def synthetic_description(symbol: str) -> str:
    known = SYNTHETIC_DESCRIPTIONS.get(symbol)
    if known is not None:
        return known
    code = "CI" if symbol.endswith("11") else "DRN" if symbol.endswith("34") else "ON"
    return f"{symbol:<12}{code}"


class SyntheticSource:
    """Passeio aleatório (movimento browniano geométrico) por ativo, com semente fixa.

    A mesma semente gera sempre a mesma sequência de preços, o que permite repetir testes. O preço
    inicial de cada ativo faz o papel de fechamento anterior.
    """

    def __init__(
        self,
        symbols: Iterable[str],
        *,
        seed: int = 42,
        annual_volatility: float = 0.30,
        interval_ms: int = 250,
        trade_probability: float = 0.6,
        clock: Callable[[], datetime] | None = None,
    ) -> None:
        chosen = list(symbols)
        if chosen == [ALL]:
            chosen = list(DEFAULT_PRICES)
        self._random = random.Random(seed)
        self._seed = seed
        self._annual_volatility = annual_volatility
        self._previous_close = {s: DEFAULT_PRICES.get(s, Decimal("50.00")) for s in chosen}
        self._prices = dict(self._previous_close)
        # Pregão de ~7 h em 252 dias por ano.
        steps_per_year = 252 * 7 * 3600 * 1000 / interval_ms
        self._sigma = annual_volatility / math.sqrt(steps_per_year)
        self._trade_probability = trade_probability
        self._clock = clock or (lambda: datetime.now(UTC))

    @property
    def symbols(self) -> list[str]:
        return list(self._prices)

    def assets(self) -> list[AssetInfo]:
        return [AssetInfo(s, synthetic_description(s)) for s in self._prices]

    def reset(self) -> None:
        pass

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
                    previous_close=self._previous_close[symbol],
                )
            )
        return ticks

    def history(self, ticker: str, timeframe: Timeframe, count: int) -> list[Candle]:
        """Histórico inventado mas reprodutível: a mesma semente dá sempre os mesmos candles, e o
        último fecha no preço atual do ativo, para o gráfico emendar com os ticks."""
        if ticker not in self._prices:
            raise LookupError(ticker)
        times = session_times(timeframe, count, self._clock())
        # Semente por ativo e período: pedir PETR4 não muda o histórico de VALE3.
        rng = random.Random(f"{self._seed}:{ticker}:{timeframe.value}")
        years = STEPS[timeframe].total_seconds() / (252 * 7 * 3600)
        if timeframe.is_daily:
            years = STEPS[timeframe].days / 365
        sigma = self._annual_volatility * math.sqrt(years)
        closes = [float(self._prices[ticker])]
        for _ in range(len(times) - 1):
            closes.append(closes[-1] / math.exp(sigma * rng.gauss(0.0, 1.0)))
        closes.reverse()
        candles: list[Candle] = []
        previous = closes[0]
        for opened_at, close in zip(times, closes, strict=True):
            wick = abs(rng.gauss(0.0, 0.5)) * sigma
            high = max(previous, close) * (1 + wick)
            low = min(previous, close) * (1 - wick)
            candles.append(
                Candle(
                    opened_at=opened_at,
                    open=max(CENT, to_price(previous)),
                    high=max(CENT, to_price(high)),
                    low=max(CENT, to_price(low)),
                    close=max(CENT, to_price(close)),
                    volume=rng.randint(100, 5000) * 100,
                )
            )
            previous = close
        return candles

    def close(self) -> None:
        pass


class Mt5Source:
    """Lê ticks do terminal MetaTrader 5 aberto e logado (só Windows). Não envia ordens."""

    def __init__(self, symbols: Iterable[str], *, server_utc_offset_h: int | None = None) -> None:
        import MetaTrader5 as mt5  # noqa: N813 (alias da documentação oficial)

        self._mt5 = mt5
        if not mt5.initialize():
            raise RuntimeError(f"Falha ao conectar no MT5: {mt5.last_error()}")
        wanted = list(symbols)
        if wanted == [ALL]:
            wanted = sorted(
                s.name for s in mt5.symbols_get() or () if is_storefront_symbol(s.name, s.path)
            )
            log.info("Modo all: %d ativos à vista encontrados na corretora", len(wanted))
        self._symbols: list[str] = []
        self._assets: list[AssetInfo] = []
        for symbol in wanted:
            info = mt5.symbol_info(symbol)
            if info is None or not mt5.symbol_select(symbol, True):
                log.warning("Ativo indisponível na corretora, ignorado: %s", symbol)
                continue
            self._symbols.append(symbol)
            self._assets.append(AssetInfo(symbol, info.description or "", info.path or ""))
        if not self._symbols:
            raise RuntimeError("Nenhum dos ativos pedidos está disponível no MT5")
        self._offset_s = 3600 * (
            server_utc_offset_h if server_utc_offset_h is not None else self._detect_offset()
        )
        # Chave do último tick enviado: horário e se já tinha fechamento anterior. Quando o
        # fechamento chega depois, o mesmo negócio é reenviado com a variação.
        self._last_sent: dict[str, tuple[int, bool]] = {}
        self._previous_close: dict[str, tuple[int, Decimal | None]] = {}
        self._slow_poll_warned = False

    @property
    def symbols(self) -> list[str]:
        return list(self._symbols)

    def assets(self) -> list[AssetInfo]:
        return list(self._assets)

    def reset(self) -> None:
        # O backend guarda as cotações em memória: depois de reiniciar, precisa receber o último
        # tick de cada ativo, mesmo dos que não negociaram de novo.
        self._last_sent.clear()

    def _detect_offset(self) -> int:
        """O MT5 informa horários no relógio do servidor da corretora, não em UTC.

        Com o mercado aberto, o tick mais recente é de agora; a diferença para o relógio do PC,
        arredondada à hora, é o fuso do servidor.
        """
        latest = max(
            (t.time for s in self._symbols if (t := self._mt5.symbol_info_tick(s)) is not None),
            default=None,
        )
        if latest is None:
            return 0
        offset = round((int(latest) - time.time()) / 3600)
        if abs(offset) > 14:
            log.warning("Fuso do servidor MT5 não detectado; use --server-utc-offset")
            return 0
        log.info(
            "Fuso do servidor MT5: UTC%+d (detectado; use --server-utc-offset se errado)", offset
        )
        return offset

    def poll(self) -> list[Tick]:
        started = time.perf_counter()
        ticks: list[Tick] = []
        budget = HISTORY_LOADS_PER_POLL
        for symbol in self._symbols:
            raw = self._mt5.symbol_info_tick(symbol)
            if raw is None:
                continue
            close, loaded = self._previous_close_for(symbol, raw.time, load=budget > 0)
            budget -= loaded
            key = (raw.time_msc, close is not None)
            if key == self._last_sent.get(symbol):
                continue
            self._last_sent[symbol] = key
            tick = to_tick(symbol, raw, previous_close=close, offset_s=self._offset_s)
            if tick is not None:
                ticks.append(tick)
        elapsed_ms = (time.perf_counter() - started) * 1000
        if elapsed_ms > 250 and not self._slow_poll_warned:
            log.warning("Leitura de %d ativos levou %.0f ms", len(self._symbols), elapsed_ms)
            self._slow_poll_warned = True
        return ticks

    def _previous_close_for(
        self, symbol: str, server_time_s: int, *, load: bool
    ) -> tuple[Decimal | None, int]:
        """Devolve o fechamento anterior e quantos candles foram lidos agora (0 ou 1)."""
        day = server_time_s // SECONDS_PER_DAY
        cached = self._previous_close.get(symbol)
        if cached is not None and cached[0] == day:
            return cached[1], 0
        if not load:
            return None, 0
        rates = self._mt5.copy_rates_from_pos(symbol, self._mt5.TIMEFRAME_D1, 0, 3)
        value = previous_close(rates, server_time_s)
        self._previous_close[symbol] = (day, value)
        return value, 1

    def history(self, ticker: str, timeframe: Timeframe, count: int) -> list[Candle]:
        if self._mt5.symbol_info(ticker) is None or not self._mt5.symbol_select(ticker, True):
            raise LookupError(ticker)
        frame = getattr(self._mt5, f"TIMEFRAME_{timeframe.value}")
        rates = self._mt5.copy_rates_from_pos(ticker, frame, 0, count)
        return to_candles(rates, timeframe, offset_s=self._offset_s)

    def close(self) -> None:
        self._mt5.shutdown()


def previous_close(rates: Sequence[Any] | None, server_time_s: int) -> Decimal | None:
    """Fechamento do último candle diário anterior ao dia do tick (ambos no relógio do servidor)."""
    if rates is None:
        return None
    day = server_time_s // SECONDS_PER_DAY
    for bar in reversed(list(rates)):
        if int(bar["time"]) // SECONDS_PER_DAY < day:
            close = float(bar["close"])
            return to_price(close) if close > 0 else None
    return None


def to_tick(
    symbol: str, raw: Any, *, previous_close: Decimal | None = None, offset_s: int = 0
) -> Tick | None:
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
        occurred_at=datetime.fromtimestamp(raw.time_msc / 1000 - offset_s, tz=UTC),
        source=Source.LIVE,
        previous_close=previous_close,
    )


def session_times(timeframe: Timeframe, count: int, now: datetime) -> list[datetime]:
    """Aberturas dos últimos candles até agora, só em dias úteis e dentro do pregão."""
    step = STEPS[timeframe]
    times: list[datetime] = []
    if timeframe.is_daily:
        day = now.date()
        if timeframe is Timeframe.W1:
            day -= timedelta(days=day.weekday())
        while len(times) < count:
            if day.weekday() < 5:
                times.append(session_open(day))
            day -= step
    else:
        minutes = int(step.total_seconds() // 60)
        cursor = now.replace(second=0, microsecond=0)
        cursor -= timedelta(minutes=cursor.minute % minutes)
        while len(times) < count:
            in_session = SESSION_OPEN_UTC_H <= cursor.hour < SESSION_CLOSE_UTC_H
            if cursor.weekday() < 5 and in_session:
                times.append(cursor)
            cursor -= step
    times.reverse()
    return times


def session_open(day: date) -> datetime:
    """Candles diários marcam a abertura do pregão: assim a data é a mesma em UTC e em Brasília."""
    return datetime(day.year, day.month, day.day, SESSION_OPEN_UTC_H, tzinfo=UTC)


def to_candles(
    rates: Sequence[Any] | None, timeframe: Timeframe, *, offset_s: int = 0
) -> list[Candle]:
    """Converte as barras do MT5 (horários no relógio do servidor da corretora)."""
    candles: list[Candle] = []
    for bar in rates if rates is not None else ():
        server_time = int(bar["time"])
        if timeframe.is_daily:
            opened_at = session_open(
                date.fromordinal(date(1970, 1, 1).toordinal() + server_time // SECONDS_PER_DAY)
            )
        else:
            opened_at = datetime.fromtimestamp(server_time - offset_s, tz=UTC)
        prices = [to_price(float(bar[k])) for k in ("open", "high", "low", "close")]
        if min(prices) <= 0:
            continue
        real = int(bar["real_volume"])
        candles.append(
            Candle(
                opened_at=opened_at,
                open=prices[0],
                high=prices[1],
                low=prices[2],
                close=prices[3],
                volume=real if real > 0 else int(bar["tick_volume"]),
            )
        )
    return candles

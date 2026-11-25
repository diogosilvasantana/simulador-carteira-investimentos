from datetime import UTC, datetime
from decimal import Decimal
from types import SimpleNamespace

import pytest

from mt5_bridge.protocol import Source, Timeframe
from mt5_bridge.sender import next_backoff
from mt5_bridge.sources import (
    SyntheticSource,
    is_storefront_symbol,
    previous_close,
    session_times,
    synthetic_description,
    to_candles,
    to_tick,
)

FIXED_NOW = datetime(2026, 11, 24, 13, 15, tzinfo=UTC)


def make_source(seed: int) -> SyntheticSource:
    return SyntheticSource(["PETR4", "VALE3", "XPTO3"], seed=seed, clock=lambda: FIXED_NOW)


def test_synthetic_source_is_deterministic_for_same_seed() -> None:
    first = [t.to_json() for _ in range(50) for t in make_source(7).poll()]
    second = [t.to_json() for _ in range(50) for t in make_source(7).poll()]

    assert first == second


def test_synthetic_prices_stay_positive_with_cents_and_valid_spread() -> None:
    source = make_source(1)
    ticks = [t for _ in range(2000) for t in source.poll()]

    assert ticks
    for tick in ticks:
        assert tick.last > 0
        assert tick.last == tick.last.quantize(Decimal("0.01"))
        assert tick.source is Source.SYNTHETIC
        if tick.bid is not None and tick.ask is not None:
            assert tick.bid < tick.ask


def test_unknown_symbol_starts_at_default_price() -> None:
    source = SyntheticSource(["XPTO3"], seed=3, trade_probability=1.0, clock=lambda: FIXED_NOW)

    (tick,) = source.poll()

    assert abs(tick.last - Decimal("50.00")) < Decimal("1.00")


def test_mt5_tick_uses_bid_when_there_is_no_last_trade() -> None:
    raw = SimpleNamespace(bid=38.46, ask=38.48, last=0.0, volume=0, time_msc=1795997700000)

    tick = to_tick("PETR4", raw)

    assert tick is not None
    assert tick.last == Decimal("38.46")
    assert tick.occurred_at.tzinfo is UTC


def test_mt5_tick_without_any_price_is_ignored() -> None:
    raw = SimpleNamespace(bid=0.0, ask=0.0, last=0.0, volume=0, time_msc=1)

    assert to_tick("PETR4", raw) is None


def test_backoff_doubles_up_to_thirty_seconds() -> None:
    assert next_backoff(1.0) == 2.0
    assert next_backoff(16.0) == 30.0
    assert next_backoff(30.0) == 30.0


DAY = 86_400
TODAY_10H = 20_000 * DAY + 10 * 3600


def test_previous_close_skips_todays_candle() -> None:
    rates = [
        {"time": 19_998 * DAY, "close": 37.10},
        {"time": 19_999 * DAY, "close": 37.00},
        {"time": 20_000 * DAY, "close": 38.47},
    ]

    assert previous_close(rates, TODAY_10H) == Decimal("37.00")


def test_previous_close_uses_last_candle_before_first_trade_of_the_day() -> None:
    rates = [{"time": 19_998 * DAY, "close": 37.10}, {"time": 19_999 * DAY, "close": 37.00}]

    assert previous_close(rates, TODAY_10H) == Decimal("37.00")


def test_previous_close_is_unknown_without_history() -> None:
    assert previous_close(None, TODAY_10H) is None
    assert previous_close([{"time": 20_000 * DAY, "close": 38.0}], TODAY_10H) is None


def test_storefront_filter_keeps_cash_market_stocks_funds_etfs_and_bdrs() -> None:
    cash = "BOVESPA\\A VISTA\\"
    kept = ["PETR4", "VALE3", "SANB11", "MXRF11", "BOVA11", "BRKM5", "AAPL34", "A1AP34", "BACW39"]
    dropped = ["PETR4F", "PETRL380", "ITSA1", "MXRF12", "BDOM"]

    assert all(is_storefront_symbol(s, cash + s) for s in kept)
    assert not any(is_storefront_symbol(s, cash + s) for s in dropped)


def test_storefront_filter_drops_symbols_outside_cash_market() -> None:
    assert not is_storefront_symbol("DI1F31", "BMF\\DI1F31")
    assert not is_storefront_symbol("IBOV", "BOVESPA\\INDICES\\IBOV")


def test_synthetic_descriptions_follow_b3_fixed_width() -> None:
    assert synthetic_description("PETR4") == "PETROBRAS   PN      N2"
    assert synthetic_description("XPTO11") == "XPTO11      CI"
    assert synthetic_description("XPTO3") == "XPTO3       ON"


def test_mt5_tick_converts_server_time_to_utc() -> None:
    # Servidor em UTC-3: 10:00 no relógio do servidor são 13:00 UTC.
    raw = SimpleNamespace(bid=38.46, ask=38.48, last=38.47, volume=100, time_msc=TODAY_10H * 1000)

    tick = to_tick("PETR4", raw, previous_close=Decimal("37.00"), offset_s=-3 * 3600)

    assert tick is not None
    assert tick.occurred_at.hour == 13
    assert tick.previous_close == Decimal("37.00")


def test_synthetic_all_uses_built_in_symbols_with_previous_close() -> None:
    source = SyntheticSource(["ALL"], seed=1, trade_probability=1.0, clock=lambda: FIXED_NOW)

    ticks = source.poll()

    assert len(ticks) == len(source.symbols) > 10
    assert all(t.previous_close is not None for t in ticks)
    assert {a.ticker for a in source.assets()} == set(source.symbols)


def test_synthetic_history_is_reproducible_and_ends_at_the_current_price() -> None:
    first = make_source(7).history("PETR4", Timeframe.D1, 30)
    second = make_source(7).history("PETR4", Timeframe.D1, 30)

    assert first == second
    assert len(first) == 30
    assert first[-1].close == Decimal("38.50")


def test_synthetic_history_has_consistent_bars() -> None:
    for candle in make_source(3).history("VALE3", Timeframe.M5, 200):
        assert candle.low <= min(candle.open, candle.close)
        assert candle.high >= max(candle.open, candle.close)
        assert candle.low > 0


def test_synthetic_history_rejects_unknown_ticker() -> None:
    with pytest.raises(LookupError):
        make_source(1).history("NOPE3", Timeframe.D1, 10)


def test_session_times_skip_weekends_and_stay_inside_the_session() -> None:
    # 24/11/2026 é uma terça; cinco dias úteis para trás passam pelo fim de semana.
    days = session_times(Timeframe.D1, 5, FIXED_NOW)

    assert [d.date().isoformat() for d in days] == [
        "2026-11-18",
        "2026-11-19",
        "2026-11-20",
        "2026-11-23",
        "2026-11-24",
    ]
    for moment in session_times(Timeframe.M30, 40, FIXED_NOW):
        assert moment.weekday() < 5
        assert 13 <= moment.hour < 20


def test_mt5_daily_bars_keep_the_server_date() -> None:
    # Barra diária do dia 24 no relógio do servidor (meia-noite), com volume real.
    bar = {
        "time": 1_795_478_400,
        "open": 38.1,
        "high": 38.9,
        "low": 37.95,
        "close": 38.47,
        "tick_volume": 10,
        "real_volume": 1_250_000,
    }

    (candle,) = to_candles([bar], Timeframe.D1, offset_s=3 * 3600)

    assert candle.opened_at == datetime(2026, 11, 24, 13, tzinfo=UTC)
    assert candle.close == Decimal("38.47")
    assert candle.volume == 1_250_000

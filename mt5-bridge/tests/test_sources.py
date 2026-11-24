from datetime import UTC, datetime
from decimal import Decimal
from types import SimpleNamespace

from mt5_bridge.protocol import Source
from mt5_bridge.sender import next_backoff
from mt5_bridge.sources import SyntheticSource, to_tick

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

import json
from datetime import UTC, datetime, timedelta, timezone
from decimal import Decimal

import pytest

from mt5_bridge.protocol import Heartbeat, Source, Tick

SAO_PAULO = timezone(timedelta(hours=-3))


def make_tick(**overrides: object) -> Tick:
    values: dict[str, object] = {
        "ticker": "PETR4",
        "last": Decimal("38.47"),
        "bid": Decimal("38.46"),
        "ask": Decimal("38.48"),
        "volume": 1200,
        "occurred_at": datetime(2026, 11, 23, 10, 15, tzinfo=SAO_PAULO),
    }
    values.update(overrides)
    return Tick(**values)  # type: ignore[arg-type]


def test_tick_serializes_prices_as_strings_and_time_in_utc() -> None:
    payload = json.loads(make_tick().to_json())

    assert payload["type"] == "tick"
    assert payload["last"] == "38.47"
    assert payload["occurred_at"] == "2026-11-23T13:15:00Z"
    assert payload["source"] == "LIVE"


def test_tick_without_bid_and_ask_serializes_nulls() -> None:
    payload = json.loads(make_tick(bid=None, ask=None).to_json())

    assert payload["bid"] is None
    assert payload["ask"] is None
    assert payload["previous_close"] is None


def test_tick_serializes_previous_close_as_string() -> None:
    payload = json.loads(make_tick(previous_close=Decimal("37.00")).to_json())

    assert payload["previous_close"] == "37.00"


@pytest.mark.parametrize("last", [Decimal("0"), Decimal("-1")])
def test_tick_rejects_non_positive_price(last: Decimal) -> None:
    with pytest.raises(ValueError, match="preço inválido"):
        make_tick(last=last)


def test_tick_rejects_naive_datetime() -> None:
    with pytest.raises(ValueError, match="fuso"):
        make_tick(occurred_at=datetime(2026, 11, 23, 10, 15))


def test_heartbeat_carries_source() -> None:
    beat = Heartbeat(sent_at=datetime(2026, 11, 23, tzinfo=UTC), source=Source.SYNTHETIC)

    assert json.loads(beat.to_json()) == {
        "type": "heartbeat",
        "sent_at": "2026-11-23T00:00:00Z",
        "source": "SYNTHETIC",
    }

import sys
from types import SimpleNamespace
from typing import Any

import pytest

from mt5_bridge.sources import HISTORY_LOADS_PER_POLL, Mt5Source

DAY = 86_400
NOW_S = 20_000 * DAY + 13 * 3600


class FakeMt5:
    """Imita o pacote MetaTrader5 só no que a ponte usa."""

    TIMEFRAME_D1 = 16408

    def __init__(self, symbols: list[str]) -> None:
        self.names = symbols
        self.history_loads = 0

    def initialize(self) -> bool:
        return True

    def shutdown(self) -> None:
        pass

    def symbols_get(self) -> list[Any]:
        return [SimpleNamespace(name=n) for n in [*self.names, "PETR4F", "WINZ26"]]

    def symbol_info(self, symbol: str) -> Any:
        return SimpleNamespace(name=symbol)

    def symbol_select(self, symbol: str, enable: bool) -> bool:
        return True

    def symbol_info_tick(self, symbol: str) -> Any:
        return SimpleNamespace(
            bid=10.0, ask=10.02, last=10.01, volume=100, time=NOW_S, time_msc=NOW_S * 1000
        )

    def copy_rates_from_pos(self, symbol: str, timeframe: int, start: int, count: int) -> Any:
        self.history_loads += 1
        return [{"time": 19_999 * DAY, "close": 9.80}]


@pytest.fixture
def fake_mt5(monkeypatch: pytest.MonkeyPatch) -> FakeMt5:
    fake = FakeMt5([f"AB{chr(65 + i // 26)}{chr(65 + i % 26)}3" for i in range(30)])
    monkeypatch.setitem(sys.modules, "MetaTrader5", fake)
    return fake


def test_all_mode_keeps_only_cash_market_symbols(fake_mt5: FakeMt5) -> None:
    source = Mt5Source(["ALL"], server_utc_offset_h=-3)

    assert source.symbols == sorted(fake_mt5.names)


def test_first_poll_limits_history_loads_and_resends_once_close_is_known(
    fake_mt5: FakeMt5,
) -> None:
    source = Mt5Source(["ALL"], server_utc_offset_h=-3)

    first = source.poll()
    second = source.poll()
    third = source.poll()

    assert len(first) == 30
    assert sum(t.previous_close is not None for t in first) == HISTORY_LOADS_PER_POLL
    assert len(second) == 30 - HISTORY_LOADS_PER_POLL
    assert all(t.previous_close is not None for t in second)
    assert third == []
    assert fake_mt5.history_loads == 30

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
        cash = [self.symbol_info(n) for n in [*self.names, "PETR4F"]]
        return [*cash, SimpleNamespace(name="DI1F31", path="BMF\\DI1F31", description="DI")]

    def symbol_info(self, symbol: str) -> Any:
        return SimpleNamespace(
            name=symbol, path=f"BOVESPA\\A VISTA\\{symbol}", description=f"{symbol:<12}ON"
        )

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
    fake = FakeMt5([f"AB{chr(65 + i // 26)}{chr(65 + i % 26)}3" for i in range(70)])
    monkeypatch.setitem(sys.modules, "MetaTrader5", fake)
    return fake


def test_all_mode_keeps_only_storefront_symbols(fake_mt5: FakeMt5) -> None:
    source = Mt5Source(["ALL"], server_utc_offset_h=-3)

    assert source.symbols == sorted(fake_mt5.names)


def test_assets_carry_mt5_description_and_path(fake_mt5: FakeMt5) -> None:
    source = Mt5Source(["ABAA3"], server_utc_offset_h=-3)

    (asset,) = source.assets()

    assert asset.ticker == "ABAA3"
    assert asset.description == "ABAA3       ON"
    assert asset.path == "BOVESPA\\A VISTA\\ABAA3"


def test_reset_resends_unchanged_ticks_after_reconnect(fake_mt5: FakeMt5) -> None:
    source = Mt5Source(["ABAA3"], server_utc_offset_h=-3)
    source.poll()
    source.poll()

    source.reset()

    assert len(source.poll()) == 1


def test_first_poll_limits_history_loads_and_resends_once_close_is_known(
    fake_mt5: FakeMt5,
) -> None:
    source = Mt5Source(["ALL"], server_utc_offset_h=-3)

    first = source.poll()
    second = source.poll()
    third = source.poll()

    assert len(first) == 70
    assert sum(t.previous_close is not None for t in first) == HISTORY_LOADS_PER_POLL
    assert len(second) == 70 - HISTORY_LOADS_PER_POLL
    assert all(t.previous_close is not None for t in second)
    assert third == []
    assert fake_mt5.history_loads == 70

import pytest

from mt5_bridge.config import BridgeConfig


def test_reads_symbols_normalized() -> None:
    config = BridgeConfig.from_env(
        {
            "BRIDGE_INGEST_URL": "wss://backend.tailnet/ingest",
            "BRIDGE_INGEST_TOKEN": "secret",
            "BRIDGE_SYMBOLS": " petr4, VALE3 ,,mxrf11",
        }
    )

    assert config.symbols == ("PETR4", "VALE3", "MXRF11")
    assert config.poll_interval_ms == 250


def test_requires_ingest_url() -> None:
    with pytest.raises(ValueError, match="BRIDGE_INGEST_URL"):
        BridgeConfig.from_env({"BRIDGE_INGEST_TOKEN": "x", "BRIDGE_SYMBOLS": "PETR4"})


def test_requires_at_least_one_symbol() -> None:
    with pytest.raises(ValueError, match="BRIDGE_SYMBOLS"):
        BridgeConfig.from_env({"BRIDGE_INGEST_URL": "u", "BRIDGE_INGEST_TOKEN": "t"})

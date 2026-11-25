import unittest
from decimal import Decimal

from carteira import plan


def asset(ticker, price, target, quantity="0", lot="1"):
    return {"ticker": ticker, "price": price, "target": target, "quantity": quantity, "lot": lot}


class PlanTest(unittest.TestCase):
    def test_should_split_new_portfolio_by_target(self):
        out = plan({"cash": "1000", "assets": [asset("A", "10", "60"), asset("B", "20", "40")]})
        self.assertEqual(["60", "20"], [a["buyQuantity"] for a in out["assets"]])
        self.assertEqual("0.00", out["remainingCash"])

    def test_should_buy_only_the_underweight_asset(self):
        out = plan({"cash": "100", "assets": [asset("A", "10", "50", "90"), asset("B", "10", "50")]})
        self.assertEqual(["0", "10"], [a["buyQuantity"] for a in out["assets"]])

    def test_should_respect_lot_and_keep_change(self):
        out = plan({"cash": "1050", "assets": [asset("A", "1", "100", lot="100")]})
        self.assertEqual("1000", out["assets"][0]["buyQuantity"])
        self.assertEqual("50.00", out["remainingCash"])

    def test_should_never_spend_more_than_cash(self):
        assets = [asset(str(n), "0.01", "25", lot="0.5") for n in range(4)]
        out = plan({"cash": "0.02", "assets": assets})
        self.assertGreaterEqual(Decimal(out["remainingCash"]), 0)

    def test_should_alert_concentration(self):
        out = plan({"cash": "100", "maxWeight": "10", "assets": [asset("A", "1", "100")]})
        self.assertEqual(1, len(out["alerts"]))

    def test_should_reject_invalid_input(self):
        for bad in (asset("A", "1", "90"), asset("A", 1.1, "100"), asset("A", "NaN", "100")):
            with self.subTest(bad=bad), self.assertRaises(ValueError):
                plan({"cash": "1", "assets": [bad]})


if __name__ == "__main__":
    unittest.main()

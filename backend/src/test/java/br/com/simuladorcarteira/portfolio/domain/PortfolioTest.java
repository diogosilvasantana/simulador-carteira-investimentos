package br.com.simuladorcarteira.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PortfolioTest {

  private static final Instant NOW = Instant.parse("2026-11-25T15:00:00Z");
  private static final Ticker PETR4 = new Ticker("PETR4");
  private static final TradingCosts NO_COSTS =
      new TradingCosts(List.of(new TradingCosts.Rate(LocalDate.of(2020, 1, 1), BigDecimal.ZERO)));

  private static Portfolio withCash(String amount) {
    Portfolio portfolio = new Portfolio();
    portfolio.contribute(Money.brl(amount), NOW);
    return portfolio;
  }

  @Test
  void shouldAddContributionToCash() {
    Portfolio portfolio = withCash("1000.00");

    assertThat(portfolio.cash()).isEqualTo(Money.brl("1000.00"));
    assertThat(portfolio.contributed()).isEqualTo(Money.brl("1000.00"));
  }

  @Test
  void shouldRejectNonPositiveContribution() {
    Portfolio portfolio = new Portfolio();

    assertThatThrownBy(() -> portfolio.contribute(Money.zero(), NOW))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldIncludeCostsInTheAveragePrice() {
    Portfolio portfolio = withCash("10000.00");
    TradingCosts costs =
        new TradingCosts(
            List.of(new TradingCosts.Rate(LocalDate.of(2020, 1, 1), new BigDecimal("0.03"))));

    portfolio.buy(PETR4, "Petrobras PN", AssetClass.STOCKS, 100, Money.brl("38.00"), costs, NOW);

    // 3.800,00 + 0,03% = 3.801,14 → 38,0114 por ação, arredondado para 38,01
    assertThat(portfolio.position(PETR4).orElseThrow().averagePrice())
        .isEqualTo(Money.brl("38.01"));
    assertThat(portfolio.cash()).isEqualTo(Money.brl("6198.86"));
  }

  @Test
  void shouldAverageThePriceAcrossBuys() {
    Portfolio portfolio = withCash("10000.00");

    portfolio.buy(PETR4, "Petrobras PN", AssetClass.STOCKS, 100, Money.brl("30.00"), NO_COSTS, NOW);
    portfolio.buy(PETR4, "Petrobras PN", AssetClass.STOCKS, 100, Money.brl("40.00"), NO_COSTS, NOW);

    Position position = portfolio.position(PETR4).orElseThrow();
    assertThat(position.quantity()).isEqualTo(200);
    assertThat(position.averagePrice()).isEqualTo(Money.brl("35.00"));
  }

  @Test
  void shouldRejectBuyAboveAvailableCash() {
    Portfolio portfolio = withCash("100.00");

    assertThatThrownBy(
            () ->
                portfolio.buy(
                    PETR4,
                    "Petrobras PN",
                    AssetClass.STOCKS,
                    10,
                    Money.brl("38.00"),
                    NO_COSTS,
                    NOW))
        .isInstanceOf(InsufficientCashException.class);
    assertThat(portfolio.cash()).isEqualTo(Money.brl("100.00"));
  }

  @Test
  void shouldRejectSellAboveOpenPosition() {
    Portfolio portfolio = withCash("1000.00");
    portfolio.buy(PETR4, "Petrobras PN", AssetClass.STOCKS, 10, Money.brl("38.00"), NO_COSTS, NOW);

    assertThatThrownBy(() -> portfolio.sell(PETR4, 11, Money.brl("40.00"), NO_COSTS, NOW))
        .isInstanceOf(InsufficientPositionException.class);
  }

  @Test
  void shouldRealizeTheResultOnSellAndKeepTheAveragePrice() {
    Portfolio portfolio = withCash("1000.00");
    portfolio.buy(PETR4, "Petrobras PN", AssetClass.STOCKS, 10, Money.brl("30.00"), NO_COSTS, NOW);

    portfolio.sell(PETR4, 4, Money.brl("35.00"), NO_COSTS, NOW);

    Position position = portfolio.position(PETR4).orElseThrow();
    assertThat(position.quantity()).isEqualTo(6);
    assertThat(position.averagePrice()).isEqualTo(Money.brl("30.00"));
    assertThat(portfolio.realizedPnl()).isEqualTo(Money.brl("20.00"));
    assertThat(portfolio.cash()).isEqualTo(Money.brl("840.00"));
  }

  @Test
  void shouldRemoveThePositionWhenFullySold() {
    Portfolio portfolio = withCash("1000.00");
    portfolio.buy(PETR4, "Petrobras PN", AssetClass.STOCKS, 10, Money.brl("30.00"), NO_COSTS, NOW);

    portfolio.sell(PETR4, 10, Money.brl("25.00"), NO_COSTS, NOW);

    assertThat(portfolio.positions()).isEmpty();
    assertThat(portfolio.realizedPnl()).isEqualTo(Money.brl("-50.00"));
  }

  @Test
  void shouldMoveCashIntoFixedIncome() {
    Portfolio portfolio = withCash("1000.00");

    FixedIncomeHolding holding =
        portfolio.investFixedIncome(
            "cdb-110", "CDB 110% do CDI", Money.brl("600.00"), new BigDecimal("110"), false, NOW);

    assertThat(portfolio.cash()).isEqualTo(Money.brl("400.00"));
    assertThat(holding.start()).isEqualTo(LocalDate.of(2026, 11, 25));
  }

  @Test
  void shouldRejectFixedIncomeAboveAvailableCash() {
    Portfolio portfolio = withCash("100.00");

    assertThatThrownBy(
            () ->
                portfolio.investFixedIncome(
                    "cdb-110", "CDB", Money.brl("600.00"), new BigDecimal("110"), false, NOW))
        .isInstanceOf(InsufficientCashException.class);
  }

  @Test
  void shouldRestoreTheSameStateItWasSavedWith() {
    Portfolio original = withCash("10000.00");
    original.buy(PETR4, "PETROBRAS PN", AssetClass.STOCKS, 100, Money.brl("38.00"), NO_COSTS, NOW);
    original.sell(PETR4, 100, Money.brl("40.00"), NO_COSTS, NOW);
    original.investFixedIncome(
        "cdb-110", "CDB 110% do CDI", Money.brl("1000.00"), new BigDecimal("110"), false, NOW);

    Portfolio restored = Portfolio.restore(original.state());

    assertThat(restored.state()).isEqualTo(original.state());
    assertThat(restored.realizedPnl()).isEqualTo(Money.brl("200.00"));
  }

  @Test
  void shouldKeepNumberingOperationsAfterRestore() {
    Portfolio original = withCash("1000.00");
    original.investFixedIncome("cdb-110", "CDB", Money.brl("100.00"), BigDecimal.TEN, false, NOW);

    Portfolio restored = Portfolio.restore(original.state());
    FixedIncomeHolding next =
        restored.investFixedIncome(
            "cdb-110", "CDB", Money.brl("100.00"), BigDecimal.TEN, false, NOW);

    assertThat(next.id()).isEqualTo(2);
  }
}

package br.com.simuladorcarteira.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class TradingCostsTest {

  @Test
  void shouldUseTheRateInForceOnTheTradeDate() {
    TradingCosts costs =
        new TradingCosts(
            List.of(
                new TradingCosts.Rate(LocalDate.of(2026, 1, 1), new BigDecimal("0.05")),
                new TradingCosts.Rate(LocalDate.of(2020, 1, 1), new BigDecimal("0.03"))));

    assertThat(costs.feeOn(LocalDate.of(2025, 6, 1), Money.brl("10000.00")))
        .isEqualTo(Money.brl("3.00"));
    assertThat(costs.feeOn(LocalDate.of(2026, 6, 1), Money.brl("10000.00")))
        .isEqualTo(Money.brl("5.00"));
  }
}

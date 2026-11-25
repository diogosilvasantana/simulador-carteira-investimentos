package br.com.simuladorcarteira.fixedincome.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class IndexRateTest {

  private static final LocalDate DAY = LocalDate.of(2026, 11, 24);

  @Test
  void shouldAnnualizeTheDailyCdiOverTwoHundredFiftyTwoBusinessDays() {
    IndexRate cdi = new IndexRate(RateIndex.CDI, DAY, new BigDecimal("0.055131"));

    // (1 + 0,00055131)^252 − 1 = 14,90% ao ano
    assertThat(cdi.annualizedPercent()).isEqualByComparingTo("14.90");
  }

  @Test
  void shouldKeepTheSelicTargetBecauseItIsAlreadyAnnual() {
    IndexRate selic = new IndexRate(RateIndex.SELIC_TARGET, DAY, new BigDecimal("15.00"));

    assertThat(selic.annualizedPercent()).isEqualByComparingTo("15.00");
  }

  @Test
  void shouldNotAnnualizeMonthlyInflation() {
    IndexRate ipca = new IndexRate(RateIndex.IPCA, DAY, new BigDecimal("0.16"));

    assertThat(ipca.annualizedPercent()).isNull();
  }
}

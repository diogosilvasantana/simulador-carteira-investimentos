package br.com.simuladorcarteira.fixedincome.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class CdiAccrualTest {

  private static final BigDecimal HUNDRED_PERCENT = new BigDecimal("100");

  @Test
  void shouldCompoundDailyCdiRates() {
    List<BigDecimal> days = List.of(new BigDecimal("0.055131"), new BigDecimal("0.055131"));

    Money value = CdiAccrual.accrue(Money.brl("10000.00"), days, HUNDRED_PERCENT);

    // 10.000 × 1,00055131² = 10.011,03
    assertThat(value).isEqualTo(Money.brl("10011.03"));
  }

  @Test
  void shouldApplyThePercentageOfCdiToEachDay() {
    List<BigDecimal> days = Collections.nCopies(21, new BigDecimal("0.055131"));

    Money full = CdiAccrual.accrue(Money.brl("10000.00"), days, HUNDRED_PERCENT);
    Money partial = CdiAccrual.accrue(Money.brl("10000.00"), days, new BigDecimal("92"));

    assertThat(full).isEqualTo(Money.brl("10116.42"));
    assertThat(partial).isEqualTo(Money.brl("10107.06"));
  }

  @Test
  void shouldKeepPrincipalWhenNoBusinessDayPassed() {
    Money value = CdiAccrual.accrue(Money.brl("5000.00"), List.of(), HUNDRED_PERCENT);

    assertThat(value).isEqualTo(Money.brl("5000.00"));
  }

  @Test
  void shouldRejectNonPositivePercentageOfCdi() {
    assertThatThrownBy(() -> CdiAccrual.accrue(Money.brl("5000.00"), List.of(), BigDecimal.ZERO))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

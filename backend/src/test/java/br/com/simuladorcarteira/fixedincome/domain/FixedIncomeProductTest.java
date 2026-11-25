package br.com.simuladorcarteira.fixedincome.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FixedIncomeProductTest {

  @Test
  void shouldRejectNonPositivePercentageOfCdi() {
    assertThatThrownBy(
            () ->
                new FixedIncomeProduct(
                    "cdb-0", "CDB 0% do CDI", ProductKind.CDB, BigDecimal.ZERO, false, 0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldNotAllowRedemptionDuringTheGracePeriod() {
    FixedIncomeProduct lci =
        new FixedIncomeProduct(
            "lci-92", "LCI 92% do CDI", ProductKind.LCI, new BigDecimal("92"), true, 90);

    assertThat(lci.allowsRedemptionAfter(89)).isFalse();
    assertThat(lci.allowsRedemptionAfter(90)).isTrue();
  }
}

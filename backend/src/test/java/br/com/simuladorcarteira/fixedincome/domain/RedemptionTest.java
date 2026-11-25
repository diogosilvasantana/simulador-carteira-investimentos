package br.com.simuladorcarteira.fixedincome.domain;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.shared.Money;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RedemptionTest {

  private static final TaxRules RULES = TaxRulesFixtures.current();
  private static final LocalDate START = LocalDate.of(2026, 1, 5);

  @Test
  void shouldChargeIncomeTaxOnTheYieldByHoldingTime() {
    RedemptionResult result =
        Redemption.of(
            Money.brl("10000.00"), Money.brl("11000.00"), START, START.plusDays(400), false, RULES);

    assertThat(result.incomeTax()).isEqualTo(Money.brl("175.00"));
    assertThat(result.iof()).isEqualTo(Money.zero());
    assertThat(result.net()).isEqualTo(Money.brl("10825.00"));
  }

  @Test
  void shouldChargeIofBeforeIncomeTaxWhenRedeemedEarly() {
    RedemptionResult result =
        Redemption.of(
            Money.brl("10000.00"), Money.brl("10050.00"), START, START.plusDays(10), false, RULES);

    // IOF de 66% sobre R$ 50 = R$ 33; IR de 22,5% sobre os R$ 17 que sobram = R$ 3,825, arredondado
    // para R$ 3,82
    assertThat(result.iof()).isEqualTo(Money.brl("33.00"));
    assertThat(result.incomeTax()).isEqualTo(Money.brl("3.82"));
    assertThat(result.net()).isEqualTo(Money.brl("10013.18"));
  }

  @Test
  void shouldExemptIncomeTaxForTaxFreeProducts() {
    RedemptionResult result =
        Redemption.of(
            Money.brl("10000.00"), Money.brl("10800.00"), START, START.plusDays(365), true, RULES);

    assertThat(result.incomeTax()).isEqualTo(Money.zero());
    assertThat(result.net()).isEqualTo(Money.brl("10800.00"));
  }

  @Test
  void shouldNotChargeTaxWithoutYield() {
    RedemptionResult result =
        Redemption.of(Money.brl("10000.00"), Money.brl("10000.00"), START, START, false, RULES);

    assertThat(result.iof()).isEqualTo(Money.zero());
    assertThat(result.incomeTax()).isEqualTo(Money.zero());
  }
}

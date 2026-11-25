package br.com.simuladorcarteira.fixedincome.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TaxRulesTest {

  static final TaxRules RULES = TaxRulesFixtures.current();

  @ParameterizedTest(name = "{0} dias: {1}%")
  @CsvSource({"1, 22.5", "180, 22.5", "181, 20", "360, 20", "361, 17.5", "720, 17.5", "721, 15"})
  void shouldUseRegressiveIncomeTaxByHoldingDays(long days, String expected) {
    BigDecimal rate = RULES.incomeTaxOn(LocalDate.of(2026, 11, 24)).rateFor(days);

    assertThat(rate).isEqualByComparingTo(expected);
  }

  @ParameterizedTest(name = "{0} dias: IOF {1}%")
  @CsvSource({"1, 96", "2, 93", "15, 50", "29, 3", "30, 0", "400, 0"})
  void shouldUseRegressiveIofOnTheFirstTwentyNineDays(long days, String expected) {
    BigDecimal rate = RULES.iofOn(LocalDate.of(2026, 11, 24)).rateFor(days);

    assertThat(rate).isEqualByComparingTo(expected);
  }

  @Test
  void shouldPickTheTableInForceOnTheRedemptionDate() {
    IncomeTaxTable flat =
        new IncomeTaxTable(
            LocalDate.of(2030, 1, 1),
            List.of(new IncomeTaxTable.Bracket(null, new BigDecimal("17.5"))));
    TaxRules rules =
        new TaxRules(List.of(RULES.incomeTaxOn(LocalDate.of(2026, 1, 1)), flat), RULES.iof());

    assertThat(rules.incomeTaxOn(LocalDate.of(2029, 12, 31)).rateFor(30))
        .isEqualByComparingTo("22.5");
    assertThat(rules.incomeTaxOn(LocalDate.of(2030, 1, 1)).rateFor(30))
        .isEqualByComparingTo("17.5");
  }
}

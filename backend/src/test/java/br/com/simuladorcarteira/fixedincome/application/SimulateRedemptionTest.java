package br.com.simuladorcarteira.fixedincome.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.simuladorcarteira.fixedincome.domain.IncomeTaxTable;
import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import br.com.simuladorcarteira.fixedincome.domain.IofTable;
import br.com.simuladorcarteira.fixedincome.domain.MissingIndexRatesException;
import br.com.simuladorcarteira.fixedincome.domain.RateIndex;
import br.com.simuladorcarteira.fixedincome.domain.TaxRules;
import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class SimulateRedemptionTest {

  private static final BigDecimal CDI = new BigDecimal("0.055131");
  private static final BigDecimal FULL = new BigDecimal("100");
  // Segunda-feira, 2 de novembro de 2026.
  private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);

  private final FakeRates rates = new FakeRates();
  private final SimulateRedemption simulate = new SimulateRedemption(rates, flatRules());

  @Test
  void shouldAccrueOnlyOnPublishedBusinessDaysBeforeRedemption() {
    publishCdi(MONDAY, MONDAY.plusDays(1), MONDAY.plusDays(2));

    RedemptionSimulation result =
        simulate.simulate(Money.brl("10000.00"), FULL, MONDAY, MONDAY.plusDays(2), false);

    // Rende na segunda e na terça; a quarta é o dia do resgate.
    assertThat(result.result().gross()).isEqualTo(Money.brl("10011.03"));
    assertThat(result.projectedBusinessDays()).isZero();
  }

  @Test
  void shouldProjectFutureWeekdaysWithTheLastPublishedCdi() {
    publishCdi(MONDAY);

    RedemptionSimulation result =
        simulate.simulate(Money.brl("10000.00"), FULL, MONDAY, MONDAY.plusDays(7), false);

    // Segunda publicada; terça a sexta projetadas; sábado e domingo não rendem.
    assertThat(result.projectedBusinessDays()).isEqualTo(4);
    assertThat(result.ratesUntil()).isEqualTo(MONDAY);
  }

  @Test
  void shouldFailClearlyWhenTheCentralBankDataIsNotLoadedYet() {
    assertThatThrownBy(
            () ->
                simulate.simulate(Money.brl("10000.00"), FULL, MONDAY, MONDAY.plusDays(30), false))
        .isInstanceOf(MissingIndexRatesException.class);
  }

  @Test
  void shouldRejectRedemptionBeforeTheInvestment() {
    publishCdi(MONDAY);

    assertThatThrownBy(
            () ->
                simulate.simulate(Money.brl("10000.00"), FULL, MONDAY, MONDAY.minusDays(1), false))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private void publishCdi(LocalDate... days) {
    for (LocalDate day : days) {
      rates.saveAll(List.of(new IndexRate(RateIndex.CDI, day, CDI)));
    }
  }

  private static TaxRules flatRules() {
    return new TaxRules(
        List.of(
            new IncomeTaxTable(
                LocalDate.of(2005, 1, 1),
                List.of(new IncomeTaxTable.Bracket(null, new BigDecimal("15"))))),
        List.of(new IofTable(LocalDate.of(2008, 1, 1), List.of())));
  }
}

package br.com.simuladorcarteira.fixedincome.application;

import br.com.simuladorcarteira.fixedincome.domain.CdiAccrual;
import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import br.com.simuladorcarteira.fixedincome.domain.MissingIndexRatesException;
import br.com.simuladorcarteira.fixedincome.domain.RateIndex;
import br.com.simuladorcarteira.fixedincome.domain.Redemption;
import br.com.simuladorcarteira.fixedincome.domain.TaxRules;
import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

/** Caso de uso: quanto um pós-fixado atrelado ao CDI rende entre duas datas, já com impostos. */
@Service
public class SimulateRedemption {

  private final IndexRateRepository rates;
  private final TaxRules taxRules;

  public SimulateRedemption(IndexRateRepository rates, TaxRules taxRules) {
    this.rates = rates;
    this.taxRules = taxRules;
  }

  /**
   * O título rende nos dias úteis de {@code start} (inclusive) a {@code end} (exclusive), como no
   * mercado: quem aplica e resgata no mesmo dia não recebe rendimento.
   */
  public RedemptionSimulation simulate(
      Money principal,
      BigDecimal indexPercentage,
      LocalDate start,
      LocalDate end,
      boolean incomeTaxExempt) {
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("O resgate não pode ser antes da aplicação");
    }
    IndexRate last =
        rates
            .latest(RateIndex.CDI)
            .orElseThrow(() -> new MissingIndexRatesException(RateIndex.CDI, start, end));
    LocalDate lastAccrualDay = end.minusDays(1);
    List<BigDecimal> daily = new ArrayList<>();
    for (IndexRate rate : rates.between(RateIndex.CDI, start, min(lastAccrualDay, last.date()))) {
      daily.add(rate.percent());
    }
    // Sem calendário de feriados por enquanto: projeta só os dias de semana.
    int projected = businessDaysBetween(max(start, last.date().plusDays(1)), lastAccrualDay);
    daily.addAll(Collections.nCopies(projected, last.percent()));
    Money gross = CdiAccrual.accrue(principal, daily, indexPercentage);
    return new RedemptionSimulation(
        Redemption.of(principal, gross, start, end, incomeTaxExempt, taxRules),
        last.date(),
        projected);
  }

  private static int businessDaysBetween(LocalDate from, LocalDate to) {
    int count = 0;
    for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
      if (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY) {
        count++;
      }
    }
    return count;
  }

  private static LocalDate min(LocalDate a, LocalDate b) {
    return a.isBefore(b) ? a : b;
  }

  private static LocalDate max(LocalDate a, LocalDate b) {
    return a.isAfter(b) ? a : b;
  }
}

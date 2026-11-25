package br.com.simuladorcarteira.fixedincome.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * IOF regressivo sobre o rendimento de resgates feitos antes de 30 dias, válido a partir de uma
 * data. {@code dailyRatesPercent.get(0)} é a alíquota para 1 dia, e assim por diante.
 */
public record IofTable(LocalDate validFrom, List<BigDecimal> dailyRatesPercent) {

  private static final BigDecimal FULL = BigDecimal.valueOf(100);

  public IofTable {
    Objects.requireNonNull(validFrom, "validFrom");
    dailyRatesPercent = List.copyOf(dailyRatesPercent);
  }

  public BigDecimal rateFor(long holdingDays) {
    if (holdingDays < 1) {
      return FULL;
    }
    if (holdingDays > dailyRatesPercent.size()) {
      return BigDecimal.ZERO;
    }
    return dailyRatesPercent.get((int) holdingDays - 1);
  }
}

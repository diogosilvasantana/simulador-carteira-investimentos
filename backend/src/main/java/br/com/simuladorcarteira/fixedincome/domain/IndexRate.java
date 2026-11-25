package br.com.simuladorcarteira.fixedincome.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Valor de um indexador numa data, em percentual (a unidade depende do {@link RateIndex}). */
public record IndexRate(RateIndex index, LocalDate date, BigDecimal percent) {

  // Convenção do mercado brasileiro para taxas ao ano em dias úteis.
  private static final int BUSINESS_DAYS_PER_YEAR = 252;
  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  public IndexRate {
    Objects.requireNonNull(index, "index");
    Objects.requireNonNull(date, "date");
    Objects.requireNonNull(percent, "percent");
  }

  /**
   * Taxa equivalente ao ano, para comparar indexadores na tela. Nula para o IPCA, que é
   * publicado por mês e não se compõe assim.
   */
  public @Nullable BigDecimal annualizedPercent() {
    return switch (index) {
      case CDI -> {
        BigDecimal daily = BigDecimal.ONE.add(percent.divide(HUNDRED, MathContext.DECIMAL64));
        yield daily
            .pow(BUSINESS_DAYS_PER_YEAR, MathContext.DECIMAL64)
            .subtract(BigDecimal.ONE)
            .multiply(HUNDRED)
            .setScale(2, RoundingMode.HALF_EVEN);
      }
      case SELIC_TARGET -> percent;
      case IPCA -> null;
    };
  }
}

package br.com.simuladorcarteira.fixedincome.domain;

import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

/**
 * Rendimento de um título pós-fixado atrelado ao CDI: o fator de cada dia útil é
 * {@code 1 + CDI do dia × percentual contratado}, e os fatores se multiplicam.
 */
public final class CdiAccrual {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  // Fator com 16 casas por dia: precisão de sobra sem deixar o número crescer a cada multiplicação.
  private static final int FACTOR_SCALE = 16;
  private static final MathContext PRECISION = new MathContext(34, RoundingMode.HALF_EVEN);

  private CdiAccrual() {}

  /**
   * @param dailyCdiPercents CDI de cada dia útil do período, em % ao dia (ex.: 0,055131)
   * @param indexPercentage percentual do CDI contratado (ex.: 105 para "105% do CDI")
   */
  public static Money accrue(
      Money principal, List<BigDecimal> dailyCdiPercents, BigDecimal indexPercentage) {
    if (indexPercentage.signum() <= 0) {
      throw new IllegalArgumentException("Percentual do CDI deve ser positivo: " + indexPercentage);
    }
    BigDecimal share = indexPercentage.divide(HUNDRED, PRECISION);
    BigDecimal factor = BigDecimal.ONE;
    for (BigDecimal dailyPercent : dailyCdiPercents) {
      BigDecimal daily = dailyPercent.divide(HUNDRED, PRECISION).multiply(share, PRECISION);
      factor =
          factor
              .multiply(BigDecimal.ONE.add(daily), PRECISION)
              .setScale(FACTOR_SCALE, RoundingMode.HALF_EVEN);
    }
    return principal.times(factor);
  }
}

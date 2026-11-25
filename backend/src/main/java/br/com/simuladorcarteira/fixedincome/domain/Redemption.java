package br.com.simuladorcarteira.fixedincome.domain;

import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Calcula os impostos de um resgate de renda fixa. O IOF incide primeiro sobre o rendimento; o IR
 * incide sobre o que sobra dele. Produtos isentos (LCI, LCA) não pagam IR.
 */
public final class Redemption {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  private Redemption() {}

  public static RedemptionResult of(
      Money principal,
      Money gross,
      LocalDate start,
      LocalDate end,
      boolean incomeTaxExempt,
      TaxRules rules) {
    long days = ChronoUnit.DAYS.between(start, end);
    BigDecimal iofRate = rules.iofOn(end).rateFor(days);
    BigDecimal incomeTaxRate =
        incomeTaxExempt ? BigDecimal.ZERO : rules.incomeTaxOn(end).rateFor(days);
    Money yield = gross.minus(principal);
    if (!yield.isGreaterThan(Money.zero())) {
      return new RedemptionResult(
          principal, gross, yield, Money.zero(), Money.zero(), gross, days, iofRate, incomeTaxRate);
    }
    Money iof = yield.times(iofRate.divide(HUNDRED, MathContext.DECIMAL64));
    Money incomeTax = yield.minus(iof).times(incomeTaxRate.divide(HUNDRED, MathContext.DECIMAL64));
    Money net = gross.minus(iof).minus(incomeTax);
    return new RedemptionResult(
        principal, gross, yield, iof, incomeTax, net, days, iofRate, incomeTaxRate);
  }
}

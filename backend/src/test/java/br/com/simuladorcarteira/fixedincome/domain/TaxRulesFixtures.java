package br.com.simuladorcarteira.fixedincome.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

/** Tabelas vigentes em 2026: IR regressivo (Lei 11.033/2004) e IOF regressivo (Decreto 6.306/2007). */
final class TaxRulesFixtures {

  private TaxRulesFixtures() {}

  static TaxRules current() {
    IncomeTaxTable incomeTax =
        new IncomeTaxTable(
            LocalDate.of(2005, 1, 1),
            List.of(
                new IncomeTaxTable.Bracket(180, new BigDecimal("22.5")),
                new IncomeTaxTable.Bracket(360, new BigDecimal("20")),
                new IncomeTaxTable.Bracket(720, new BigDecimal("17.5")),
                new IncomeTaxTable.Bracket(null, new BigDecimal("15"))));
    IofTable iof =
        new IofTable(
            LocalDate.of(2008, 1, 1),
            Stream.of(
                    96, 93, 90, 86, 83, 80, 76, 73, 70, 66, 63, 60, 56, 53, 50, 46, 43, 40, 36, 33,
                    30, 26, 23, 20, 16, 13, 10, 6, 3)
                .map(BigDecimal::valueOf)
                .toList());
    return new TaxRules(List.of(incomeTax), List.of(iof));
  }
}

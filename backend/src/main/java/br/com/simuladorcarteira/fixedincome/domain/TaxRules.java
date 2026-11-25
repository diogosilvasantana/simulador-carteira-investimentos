package br.com.simuladorcarteira.fixedincome.domain;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Tabelas de IR e IOF versionadas por data de vigência (ADR-011). Uma mudança de lei vira uma nova
 * tabela, e resgates de antes da mudança continuam com a regra da época.
 */
public record TaxRules(List<IncomeTaxTable> incomeTax, List<IofTable> iof) {

  public TaxRules {
    incomeTax = List.copyOf(incomeTax);
    iof = List.copyOf(iof);
  }

  public IncomeTaxTable incomeTaxOn(LocalDate date) {
    return inForce(incomeTax, IncomeTaxTable::validFrom, date, "IR");
  }

  public IofTable iofOn(LocalDate date) {
    return inForce(iof, IofTable::validFrom, date, "IOF");
  }

  private static <T> T inForce(
      List<T> tables, Function<T, LocalDate> validFrom, LocalDate date, String tax) {
    return tables.stream()
        .filter(table -> !validFrom.apply(table).isAfter(date))
        .max(Comparator.comparing(validFrom))
        .orElseThrow(
            () -> new IllegalStateException("Nenhuma tabela de " + tax + " vigente em " + date));
  }
}

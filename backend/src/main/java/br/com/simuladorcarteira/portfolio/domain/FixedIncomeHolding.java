package br.com.simuladorcarteira.portfolio.domain;

import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Uma aplicação em renda fixa pós-fixada: quanto entrou, em qual produto e quando. */
public record FixedIncomeHolding(
    long id,
    String productId,
    String name,
    Money principal,
    BigDecimal indexPercentage,
    boolean incomeTaxExempt,
    LocalDate start) {

  public FixedIncomeHolding {
    Objects.requireNonNull(productId, "productId");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(principal, "principal");
    Objects.requireNonNull(indexPercentage, "indexPercentage");
    Objects.requireNonNull(start, "start");
  }
}

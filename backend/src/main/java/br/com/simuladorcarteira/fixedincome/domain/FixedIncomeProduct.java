package br.com.simuladorcarteira.fixedincome.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Produto modelo de renda fixa pós-fixada (ADR-011): rende um percentual do CDI, pode ser isento
 * de IR (LCI, LCA) e pode ter carência, os dias em que o resgate ainda não é permitido.
 */
public record FixedIncomeProduct(
    String id,
    String name,
    ProductKind kind,
    BigDecimal indexPercentage,
    boolean incomeTaxExempt,
    int gracePeriodDays) {

  public FixedIncomeProduct {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(kind, "kind");
    Objects.requireNonNull(indexPercentage, "indexPercentage");
    if (indexPercentage.signum() <= 0) {
      throw new IllegalArgumentException("Percentual do CDI deve ser positivo: " + id);
    }
    if (gracePeriodDays < 0) {
      throw new IllegalArgumentException("Carência negativa: " + id);
    }
  }

  public boolean allowsRedemptionAfter(long holdingDays) {
    return holdingDays >= gracePeriodDays;
  }
}

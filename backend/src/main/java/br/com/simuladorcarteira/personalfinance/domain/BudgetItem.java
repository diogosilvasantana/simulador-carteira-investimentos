package br.com.simuladorcarteira.personalfinance.domain;

import br.com.simuladorcarteira.shared.Money;
import java.util.Objects;

/**
 * Um valor mensal do orçamento. Renda entra antes do desconto de consignado, porque a parcela já
 * está nas dívidas e seria subtraída duas vezes.
 */
public record BudgetItem(long id, String name, BudgetKind kind, Money amount) {

  public BudgetItem {
    Objects.requireNonNull(kind, "kind");
    if (name.isBlank()) {
      throw new IllegalArgumentException("Informe o nome do item");
    }
    if (amount.isNegative()) {
      throw new IllegalArgumentException("O valor não pode ser negativo");
    }
  }
}

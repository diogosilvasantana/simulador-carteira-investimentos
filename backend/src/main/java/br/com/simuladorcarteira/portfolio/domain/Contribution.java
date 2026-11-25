package br.com.simuladorcarteira.portfolio.domain;

import br.com.simuladorcarteira.shared.Money;
import java.time.Instant;
import java.util.Objects;

/** Aporte: dinheiro novo que entra no caixa da carteira simulada. */
public record Contribution(Money amount, Instant at) {

  public Contribution {
    Objects.requireNonNull(amount, "amount");
    Objects.requireNonNull(at, "at");
    if (amount.amount().signum() <= 0) {
      throw new IllegalArgumentException("O aporte precisa ser positivo");
    }
  }
}

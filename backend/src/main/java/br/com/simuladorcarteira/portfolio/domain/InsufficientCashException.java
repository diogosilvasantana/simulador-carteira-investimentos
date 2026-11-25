package br.com.simuladorcarteira.portfolio.domain;

import br.com.simuladorcarteira.shared.Money;

/** A operação custa mais do que o caixa da carteira tem. */
public class InsufficientCashException extends RuntimeException {

  private final Money required;
  private final Money available;

  public InsufficientCashException(Money required, Money available) {
    super("Caixa insuficiente: precisa de %s e há %s".formatted(required, available));
    this.required = required;
    this.available = available;
  }

  public Money required() {
    return required;
  }

  public Money available() {
    return available;
  }
}

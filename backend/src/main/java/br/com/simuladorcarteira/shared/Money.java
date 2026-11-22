package br.com.simuladorcarteira.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Valor monetário imutável (ADR-005).
 *
 * <p>Toda quantia de dinheiro do sistema passa por aqui para que escala e arredondamento sejam
 * decididos num único lugar: duas casas decimais com arredondamento bancário (HALF_EVEN), que não
 * acumula viés quando muitos valores são somados.
 */
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

  public static final Currency BRL = Currency.getInstance("BRL");
  private static final int SCALE = 2;
  private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

  public Money {
    Objects.requireNonNull(amount, "amount");
    Objects.requireNonNull(currency, "currency");
    amount = amount.setScale(SCALE, ROUNDING);
  }

  public static Money brl(String amount) {
    return new Money(new BigDecimal(amount), BRL);
  }

  public static Money brl(BigDecimal amount) {
    return new Money(amount, BRL);
  }

  public static Money zero() {
    return new Money(BigDecimal.ZERO, BRL);
  }

  public Money plus(Money other) {
    requireSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public Money minus(Money other) {
    requireSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }

  public Money times(BigDecimal factor) {
    Objects.requireNonNull(factor, "factor");
    return new Money(amount.multiply(factor), currency);
  }

  public Money times(long factor) {
    return times(BigDecimal.valueOf(factor));
  }

  public boolean isNegative() {
    return amount.signum() < 0;
  }

  public boolean isGreaterThan(Money other) {
    return compareTo(other) > 0;
  }

  @Override
  public int compareTo(Money other) {
    requireSameCurrency(other);
    return amount.compareTo(other.amount);
  }

  private void requireSameCurrency(Money other) {
    Objects.requireNonNull(other, "other");
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Moedas diferentes: %s e %s".formatted(currency, other.currency));
    }
  }

  @Override
  public String toString() {
    return currency.getCurrencyCode() + " " + amount.toPlainString();
  }
}

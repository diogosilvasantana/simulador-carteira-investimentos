package br.com.simuladorcarteira.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoneyTest {

  @Test
  void shouldKeepTwoDecimalPlaces() {
    Money money = Money.brl("10");

    assertThat(money.amount()).isEqualByComparingTo("10.00");
    assertThat(money.amount().scale()).isEqualTo(2);
  }

  @ParameterizedTest(name = "{0} arredonda para {1}")
  @CsvSource({"10.125, 10.12", "10.135, 10.14", "10.126, 10.13", "-10.125, -10.12"})
  void shouldRoundHalfEven(String raw, String expected) {
    assertThat(Money.brl(raw).amount()).isEqualByComparingTo(expected);
  }

  @Test
  void shouldAddAndSubtract() {
    Money cash = Money.brl("1000.00");

    Money result = cash.minus(Money.brl("250.40")).plus(Money.brl("0.10"));

    assertThat(result).isEqualTo(Money.brl("749.70"));
  }

  @Test
  void shouldMultiplyByQuantity() {
    Money price = Money.brl("32.17");

    assertThat(price.times(100)).isEqualTo(Money.brl("3217.00"));
  }

  @Test
  void shouldMultiplyByDecimalFactorAndRound() {
    Money gross = Money.brl("100.00");

    assertThat(gross.times(new BigDecimal("0.00005"))).isEqualTo(Money.brl("0.00"));
    assertThat(gross.times(new BigDecimal("0.15"))).isEqualTo(Money.brl("15.00"));
  }

  @Test
  void shouldCompareValues() {
    assertThat(Money.brl("10.01").isGreaterThan(Money.brl("10.00"))).isTrue();
    assertThat(Money.brl("-0.01").isNegative()).isTrue();
    assertThat(Money.zero().isNegative()).isFalse();
  }

  @Test
  void shouldTreatSameValueWithDifferentScaleAsEqual() {
    assertThat(Money.brl("5")).isEqualTo(Money.brl("5.000"));
  }

  @Test
  void shouldRejectOperationsBetweenDifferentCurrencies() {
    Money reais = Money.brl("10.00");
    Money dollars = new Money(BigDecimal.TEN, Currency.getInstance("USD"));

    assertThatThrownBy(() -> reais.plus(dollars))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Moedas diferentes");
  }

  @Test
  void shouldRejectNullAmount() {
    assertThatThrownBy(() -> Money.brl((BigDecimal) null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldPrintCurrencyAndAmount() {
    assertThat(Money.brl("1234.5")).hasToString("BRL 1234.50");
  }
}

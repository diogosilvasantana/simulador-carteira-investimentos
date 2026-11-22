package br.com.simuladorcarteira.shared;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Código de negociação de um ativo na B3, como {@code PETR4}, {@code MXRF11} ou {@code BOVA11}.
 *
 * <p>Formato do mercado à vista: quatro letras seguidas de um ou dois dígitos, com {@code F}
 * opcional no fim para o mercado fracionário.
 */
public record Ticker(String value) {

  private static final Pattern FORMAT = Pattern.compile("[A-Z0-9]{4}\\d{1,2}F?");

  public Ticker {
    Objects.requireNonNull(value, "value");
    value = value.strip().toUpperCase(Locale.ROOT);
    if (!FORMAT.matcher(value).matches()) {
      throw new IllegalArgumentException("Ticker inválido: " + value);
    }
  }

  public static Ticker of(String value) {
    return new Ticker(value);
  }

  /** Ticker do lote padrão, sem o sufixo do fracionário. */
  public Ticker standardLot() {
    return isFractional() ? new Ticker(value.substring(0, value.length() - 1)) : this;
  }

  public boolean isFractional() {
    return value.endsWith("F");
  }

  @Override
  public String toString() {
    return value;
  }
}

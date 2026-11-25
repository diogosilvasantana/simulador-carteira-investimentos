package br.com.simuladorcarteira.fixedincome.domain;

import java.time.LocalDate;

/** Não há taxas do indexador para o período pedido (ex.: o Banco Central ainda não respondeu). */
public class MissingIndexRatesException extends RuntimeException {

  public MissingIndexRatesException(RateIndex index, LocalDate from, LocalDate to) {
    super("Sem taxas de %s entre %s e %s".formatted(index, from, to));
  }
}

package br.com.simuladorcarteira.marketdata.domain;

/** Tick que não pode ser aceito: preço não positivo, spread invertido, volume negativo. */
public class InvalidTickException extends RuntimeException {

  public InvalidTickException(String message) {
    super(message);
  }
}

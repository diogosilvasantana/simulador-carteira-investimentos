package br.com.simuladorcarteira.portfolio.domain;

import br.com.simuladorcarteira.shared.Ticker;

/** Venda de mais unidades do que a carteira tem. */
public class InsufficientPositionException extends RuntimeException {

  public InsufficientPositionException(Ticker ticker, long requested, long available) {
    super(
        "Não dá para vender %d de %s: a carteira tem %d"
            .formatted(requested, ticker.value(), available));
  }
}

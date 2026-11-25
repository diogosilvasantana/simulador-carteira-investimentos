package br.com.simuladorcarteira.portfolio.domain;

import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.time.Instant;
import java.util.Objects;

/** Uma ordem a mercado executada no simulador, com o preço da última cotação. */
public record Execution(
    long id,
    OrderSide side,
    Ticker ticker,
    long quantity,
    Money price,
    Money fees,
    Money total,
    Instant executedAt) {

  public Execution {
    Objects.requireNonNull(side, "side");
    Objects.requireNonNull(ticker, "ticker");
    Objects.requireNonNull(price, "price");
    Objects.requireNonNull(fees, "fees");
    Objects.requireNonNull(total, "total");
    Objects.requireNonNull(executedAt, "executedAt");
  }
}

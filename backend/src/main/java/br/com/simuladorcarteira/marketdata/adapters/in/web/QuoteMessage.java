package br.com.simuladorcarteira.marketdata.adapters.in.web;

import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

/** Cotação como sai pela API e pelo WebSocket. O domínio nunca é serializado diretamente. */
public record QuoteMessage(
    String ticker,
    BigDecimal last,
    @Nullable BigDecimal bid,
    @Nullable BigDecimal ask,
    long volume,
    Instant occurredAt,
    String source) {

  public static QuoteMessage from(Tick tick) {
    return new QuoteMessage(
        tick.ticker().value(),
        tick.last().amount(),
        amountOrNull(tick.bid()),
        amountOrNull(tick.ask()),
        tick.volume(),
        tick.occurredAt(),
        tick.source().name());
  }

  private static @Nullable BigDecimal amountOrNull(@Nullable Money money) {
    return money == null ? null : money.amount();
  }
}

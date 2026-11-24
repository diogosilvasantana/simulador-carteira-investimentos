package br.com.simuladorcarteira.marketdata.adapters.in.web;

import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.shared.Money;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * Cotação como sai pela API e pelo WebSocket. O domínio nunca é serializado diretamente.
 *
 * <p>Preços vão como texto decimal ("38.47"), não como número JSON: o JavaScript leria um número
 * como ponto flutuante e poderia errar centavos.
 */
public record QuoteMessage(
    String ticker,
    String last,
    @Nullable String bid,
    @Nullable String ask,
    long volume,
    Instant occurredAt,
    String source) {

  public static QuoteMessage from(Tick tick) {
    return new QuoteMessage(
        tick.ticker().value(),
        decimal(tick.last()),
        decimalOrNull(tick.bid()),
        decimalOrNull(tick.ask()),
        tick.volume(),
        tick.occurredAt(),
        tick.source().name());
  }

  private static String decimal(Money money) {
    return money.amount().toPlainString();
  }

  private static @Nullable String decimalOrNull(@Nullable Money money) {
    return money == null ? null : decimal(money);
  }
}

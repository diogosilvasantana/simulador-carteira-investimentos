package br.com.simuladorcarteira.marketdata.adapters.in.web;

import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * Cotação como sai pela API e pelo WebSocket. O domínio nunca é serializado diretamente.
 *
 * <p>Preços vão como texto decimal ("38.47"), não como número JSON: o JavaScript leria um número
 * como ponto flutuante e poderia errar centavos. A variação ({@code changePercent}) já vem em
 * pontos percentuais: "1.25" significa alta de 1,25%.
 */
public record QuoteMessage(
    String ticker,
    String last,
    @Nullable String bid,
    @Nullable String ask,
    @Nullable String previousClose,
    @Nullable String changePercent,
    long volume,
    Instant occurredAt,
    String source) {

  public static QuoteMessage from(Tick tick) {
    return new QuoteMessage(
        tick.ticker().value(),
        decimal(tick.last()),
        decimalOrNull(tick.bid()),
        decimalOrNull(tick.ask()),
        decimalOrNull(tick.previousClose()),
        percentOrNull(tick.changePercent()),
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

  private static @Nullable String percentOrNull(@Nullable BigDecimal percent) {
    return percent == null ? null : percent.toPlainString();
  }
}

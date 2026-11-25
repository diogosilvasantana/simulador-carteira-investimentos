package br.com.simuladorcarteira.marketdata.adapters.in.web;

import br.com.simuladorcarteira.marketdata.domain.Asset;
import br.com.simuladorcarteira.marketdata.domain.AssetCategory;
import br.com.simuladorcarteira.marketdata.domain.AssetQuote;
import br.com.simuladorcarteira.marketdata.domain.ListingSegment;
import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * Ativo e cotação como saem pela API e pelo WebSocket. O domínio nunca é serializado diretamente.
 *
 * <p>Preços vão como texto decimal ("38.47"), não como número JSON: o JavaScript leria um número
 * como ponto flutuante e poderia errar centavos. A variação ({@code changePercent}) já vem em
 * pontos percentuais: "1.25" significa alta de 1,25%. Os campos do ativo ficam nulos quando o
 * catálogo ainda não o conhece; os da cotação, quando o ativo ainda não negociou.
 */
public record QuoteMessage(
    String ticker,
    @Nullable String name,
    @Nullable String type,
    @Nullable String category,
    @Nullable String listingSegment,
    @Nullable String last,
    @Nullable String bid,
    @Nullable String ask,
    @Nullable String previousClose,
    @Nullable String changePercent,
    @Nullable Long volume,
    @Nullable Instant occurredAt,
    @Nullable String source) {

  public static QuoteMessage from(AssetQuote quote) {
    Asset asset = quote.asset();
    Tick tick = quote.tick();
    return new QuoteMessage(
        quote.ticker().value(),
        asset == null ? null : asset.name(),
        asset == null ? null : asset.type().name(),
        quote.category().map(AssetCategory::slug).orElse(null),
        asset == null ? null : segmentOrNull(asset.listingSegment()),
        tick == null ? null : decimal(tick.last()),
        tick == null ? null : decimalOrNull(tick.bid()),
        tick == null ? null : decimalOrNull(tick.ask()),
        tick == null ? null : decimalOrNull(tick.previousClose()),
        tick == null ? null : percentOrNull(tick.changePercent()),
        tick == null ? null : tick.volume(),
        tick == null ? null : tick.occurredAt(),
        tick == null ? null : tick.source().name());
  }

  private static @Nullable String segmentOrNull(@Nullable ListingSegment segment) {
    return segment == null ? null : segment.name();
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

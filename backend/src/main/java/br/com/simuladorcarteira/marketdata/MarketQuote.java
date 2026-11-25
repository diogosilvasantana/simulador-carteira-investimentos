package br.com.simuladorcarteira.marketdata;

import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.math.BigDecimal;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Fotografia pública de um ativo: nome, categoria da vitrine (slug, nula se a classificação não
 * conhece o ativo) e último preço, nulo enquanto não houver negócio.
 */
public record MarketQuote(
    Ticker ticker,
    String name,
    @Nullable String categorySlug,
    @Nullable Money last,
    @Nullable BigDecimal changePercent) {

  public MarketQuote {
    Objects.requireNonNull(ticker, "ticker");
    Objects.requireNonNull(name, "name");
  }
}

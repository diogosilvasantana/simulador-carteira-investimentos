package br.com.simuladorcarteira.marketdata.domain;

import br.com.simuladorcarteira.shared.Ticker;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Um ativo com a sua última cotação. Qualquer um dos lados pode faltar: um ativo pouco negociado
 * ainda sem tick hoje, ou um tick de um ativo que o catálogo ainda não conhece.
 */
public record AssetQuote(Ticker ticker, @Nullable Asset asset, @Nullable Tick tick) {

  public AssetQuote {
    Objects.requireNonNull(ticker, "ticker");
    if (asset == null && tick == null) {
      throw new IllegalArgumentException("Cotação sem ativo e sem tick para " + ticker);
    }
    if (asset != null && !asset.ticker().equals(ticker)) {
      throw new IllegalArgumentException("Ativo de outro ticker: " + asset.ticker());
    }
    if (tick != null && !tick.ticker().equals(ticker)) {
      throw new IllegalArgumentException("Tick de outro ticker: " + tick.ticker());
    }
  }

  public Optional<AssetCategory> category() {
    return asset == null ? Optional.empty() : asset.category();
  }
}

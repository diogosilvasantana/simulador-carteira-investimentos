package br.com.simuladorcarteira.marketdata.application;

import br.com.simuladorcarteira.marketdata.MarketQuote;
import br.com.simuladorcarteira.marketdata.MarketQuotes;
import br.com.simuladorcarteira.marketdata.domain.Asset;
import br.com.simuladorcarteira.marketdata.domain.AssetCategory;
import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.shared.Ticker;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Implementa a API pública de cotações a partir do catálogo e da última cotação recebida. */
@Service
public class LookupQuote implements MarketQuotes {

  private final AssetRepository assets;
  private final LatestQuoteRepository quotes;

  public LookupQuote(AssetRepository assets, LatestQuoteRepository quotes) {
    this.assets = assets;
    this.quotes = quotes;
  }

  @Override
  public Optional<MarketQuote> find(Ticker ticker) {
    Optional<Asset> asset = assets.findByTicker(ticker);
    Optional<Tick> tick = quotes.findByTicker(ticker);
    if (asset.isEmpty() && tick.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        new MarketQuote(
            ticker,
            asset.map(Asset::name).orElse(ticker.value()),
            asset.flatMap(Asset::category).map(AssetCategory::slug).orElse(null),
            tick.map(Tick::last).orElse(null),
            tick.map(Tick::changePercent).orElse(null)));
  }
}

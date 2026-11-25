package br.com.simuladorcarteira.marketdata.application;

import br.com.simuladorcarteira.marketdata.domain.Asset;
import br.com.simuladorcarteira.marketdata.domain.AssetCategory;
import br.com.simuladorcarteira.marketdata.domain.AssetQuote;
import br.com.simuladorcarteira.marketdata.domain.Tick;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Caso de uso: fotografia do mercado para a tela carregar antes do primeiro lote em tempo real. */
@Service
public class ListMarket {

  private static final Comparator<AssetQuote> BY_TICKER =
      Comparator.comparing(quote -> quote.ticker().value());

  private final AssetRepository assets;
  private final LatestQuoteRepository quotes;

  public ListMarket(AssetRepository assets, LatestQuoteRepository quotes) {
    this.assets = assets;
    this.quotes = quotes;
  }

  /** Todas as cotações recebidas, com o ativo quando o catálogo o conhece. */
  public List<AssetQuote> quotes() {
    return quotes.findAll().stream().map(this::withAsset).sorted(BY_TICKER).toList();
  }

  /** Todos os ativos da categoria, inclusive os que ainda não negociaram hoje. */
  public List<AssetQuote> byCategory(AssetCategory category) {
    return assets.findAll().stream()
        .filter(asset -> asset.category().filter(category::equals).isPresent())
        .map(this::withTick)
        .sorted(BY_TICKER)
        .toList();
  }

  /** Quantos ativos há em cada categoria, para as abas mostrarem o total. */
  public Map<AssetCategory, Long> countByCategory() {
    Map<AssetCategory, Long> counts = new EnumMap<>(AssetCategory.class);
    for (AssetCategory category : AssetCategory.values()) {
      counts.put(category, 0L);
    }
    for (Asset asset : assets.findAll()) {
      asset.category().ifPresent(category -> counts.merge(category, 1L, Long::sum));
    }
    return counts;
  }

  private AssetQuote withAsset(Tick tick) {
    return new AssetQuote(tick.ticker(), assets.findByTicker(tick.ticker()).orElse(null), tick);
  }

  private AssetQuote withTick(Asset asset) {
    return new AssetQuote(asset.ticker(), asset, quotes.findByTicker(asset.ticker()).orElse(null));
  }
}

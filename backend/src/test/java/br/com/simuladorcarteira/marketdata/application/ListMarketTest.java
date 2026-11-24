package br.com.simuladorcarteira.marketdata.application;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.marketdata.domain.AssetCategory;
import br.com.simuladorcarteira.marketdata.domain.AssetQuote;
import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.marketdata.domain.TickSource;
import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ListMarketTest {

  private static final Instant NOW = Instant.parse("2026-11-24T13:15:00Z");

  private final FakeAssets assets = new FakeAssets();
  private final FakeQuotes quotes = new FakeQuotes();
  private final RegisterAsset registerAsset = new RegisterAsset(assets);
  private final ListMarket listMarket = new ListMarket(assets, quotes);

  @Test
  void shouldListQuotesSortedByTicker() {
    quotes.save(tick("VALE3"));
    quotes.save(tick("BOVA11"));
    quotes.save(tick("PETR4"));

    List<AssetQuote> all = listMarket.quotes();

    assertThat(all).extracting(q -> q.ticker().value()).containsExactly("BOVA11", "PETR4", "VALE3");
  }

  @Test
  void shouldListOnlyAssetsOfTheCategoryIncludingThoseWithoutTicks() {
    registerAsset.register(Ticker.of("MXRF11"), "FII MAXI RENCI");
    registerAsset.register(Ticker.of("HGLG11"), "FII HGLG PAXCI");
    registerAsset.register(Ticker.of("PETR4"), "PETROBRAS   PN      N2");
    quotes.save(tick("MXRF11"));

    List<AssetQuote> funds = listMarket.byCategory(AssetCategory.REAL_ESTATE_FUNDS);

    assertThat(funds).extracting(q -> q.ticker().value()).containsExactly("HGLG11", "MXRF11");
    assertThat(funds.get(0).tick()).isNull();
    assertThat(funds.get(1).tick()).isNotNull();
  }

  @Test
  void shouldCountAssetsPerCategoryIgnoringUnknownTypes() {
    registerAsset.register(Ticker.of("MXRF11"), "FII MAXI RENCI");
    registerAsset.register(Ticker.of("PETR4"), "PETROBRAS   PN      N2");
    registerAsset.register(Ticker.of("VALE3"), "VALE        ON      NM");
    registerAsset.register(Ticker.of("DDIF31"), "CUPOM CAMBIAL");

    Map<AssetCategory, Long> counts = listMarket.countByCategory();

    assertThat(counts)
        .containsEntry(AssetCategory.STOCKS, 2L)
        .containsEntry(AssetCategory.REAL_ESTATE_FUNDS, 1L)
        .containsEntry(AssetCategory.ETFS, 0L);
  }

  private static Tick tick(String ticker) {
    return new Tick(Ticker.of(ticker), Money.brl("10.00"), null, null, 0, NOW, TickSource.LIVE);
  }
}

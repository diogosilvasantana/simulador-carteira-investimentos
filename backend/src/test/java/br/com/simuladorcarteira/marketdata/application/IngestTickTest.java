package br.com.simuladorcarteira.marketdata.application;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.marketdata.domain.Asset;
import br.com.simuladorcarteira.marketdata.domain.AssetQuote;
import br.com.simuladorcarteira.marketdata.domain.AssetType;
import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.marketdata.domain.TickSource;
import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class IngestTickTest {

  private static final Instant NOW = Instant.parse("2026-11-24T13:15:00Z");

  private final FakeQuotes quotes = new FakeQuotes();
  private final FakeAssets assets = new FakeAssets();
  private final List<AssetQuote> broadcasted = new ArrayList<>();
  private final IngestTick ingestTick = new IngestTick(quotes, assets, broadcasted::add);

  @Test
  void shouldStoreAndBroadcastFirstTick() {
    Tick tick = tick("38.47", NOW);

    boolean accepted = ingestTick.ingest(tick);

    assertThat(accepted).isTrue();
    assertThat(quotes.findByTicker(Ticker.of("PETR4"))).contains(tick);
    assertThat(broadcasted).extracting(AssetQuote::tick).containsExactly(tick);
  }

  @Test
  void shouldBroadcastTickWithItsCatalogAsset() {
    Asset petrobras = new Asset(Ticker.of("PETR4"), "PETROBRAS", AssetType.STOCK, null);
    assets.save(petrobras);

    ingestTick.ingest(tick("38.47", NOW));

    assertThat(broadcasted).extracting(AssetQuote::asset).containsExactly(petrobras);
  }

  @Test
  void shouldReplaceQuoteWithNewerTick() {
    ingestTick.ingest(tick("38.47", NOW));
    Tick newer = tick("38.49", NOW.plusSeconds(1));

    ingestTick.ingest(newer);

    assertThat(quotes.findByTicker(Ticker.of("PETR4"))).contains(newer);
  }

  @Test
  void shouldDiscardOutOfOrderTick() {
    Tick current = tick("38.49", NOW.plusSeconds(1));
    ingestTick.ingest(current);

    boolean accepted = ingestTick.ingest(tick("38.47", NOW));

    assertThat(accepted).isFalse();
    assertThat(quotes.findByTicker(Ticker.of("PETR4"))).contains(current);
    assertThat(broadcasted).extracting(AssetQuote::tick).containsExactly(current);
  }

  @Test
  void shouldAcceptTickWithSameTimeThatAddsPreviousClose() {
    ingestTick.ingest(tick("38.47", NOW));
    Tick withPreviousClose =
        new Tick(
            Ticker.of("PETR4"),
            Money.brl("38.47"),
            null,
            null,
            Money.brl("37.00"),
            100,
            NOW,
            TickSource.LIVE);

    boolean accepted = ingestTick.ingest(withPreviousClose);

    assertThat(accepted).isTrue();
    assertThat(quotes.findByTicker(Ticker.of("PETR4"))).contains(withPreviousClose);
  }

  private static Tick tick(String price, Instant at) {
    return new Tick(Ticker.of("PETR4"), Money.brl(price), null, null, 100, at, TickSource.LIVE);
  }
}

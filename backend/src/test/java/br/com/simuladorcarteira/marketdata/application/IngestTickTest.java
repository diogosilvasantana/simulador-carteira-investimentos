package br.com.simuladorcarteira.marketdata.application;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.marketdata.domain.TickSource;
import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class IngestTickTest {

  private static final Instant NOW = Instant.parse("2026-11-24T13:15:00Z");

  private final FakeQuotes quotes = new FakeQuotes();
  private final List<Tick> broadcasted = new ArrayList<>();
  private final IngestTick ingestTick = new IngestTick(quotes, broadcasted::add);

  @Test
  void shouldStoreAndBroadcastFirstTick() {
    Tick tick = tick("38.47", NOW);

    boolean accepted = ingestTick.ingest(tick);

    assertThat(accepted).isTrue();
    assertThat(quotes.findByTicker(Ticker.of("PETR4"))).contains(tick);
    assertThat(broadcasted).containsExactly(tick);
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
    assertThat(broadcasted).containsExactly(current);
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

  @Test
  void shouldListQuotesSortedByTicker() {
    ingestTick.ingest(tickFor("VALE3", NOW));
    ingestTick.ingest(tickFor("BOVA11", NOW));
    ingestTick.ingest(tickFor("PETR4", NOW));

    List<Tick> all = new ListLatestQuotes(quotes).all();

    assertThat(all).extracting(t -> t.ticker().value()).containsExactly("BOVA11", "PETR4", "VALE3");
  }

  private static Tick tick(String price, Instant at) {
    return new Tick(Ticker.of("PETR4"), Money.brl(price), null, null, 100, at, TickSource.LIVE);
  }

  private static Tick tickFor(String ticker, Instant at) {
    return new Tick(Ticker.of(ticker), Money.brl("10.00"), null, null, 0, at, TickSource.LIVE);
  }

  private static final class FakeQuotes implements LatestQuoteRepository {
    private final Map<Ticker, Tick> data = new HashMap<>();

    @Override
    public Optional<Tick> findByTicker(Ticker ticker) {
      return Optional.ofNullable(data.get(ticker));
    }

    @Override
    public List<Tick> findAll() {
      return List.copyOf(data.values());
    }

    @Override
    public void save(Tick tick) {
      data.put(tick.ticker(), tick);
    }
  }
}

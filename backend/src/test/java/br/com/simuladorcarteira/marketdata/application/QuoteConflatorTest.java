package br.com.simuladorcarteira.marketdata.application;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.marketdata.domain.AssetQuote;
import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.marketdata.domain.TickSource;
import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.time.Instant;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class QuoteConflatorTest {

  private static final Instant NOW = Instant.parse("2026-11-24T13:15:00Z");

  private final QuoteConflator conflator = new QuoteConflator();

  @Test
  void shouldKeepOnlyTheLatestQuoteOfEachTickerInABatch() {
    conflator.put(quote("PETR4", "38.47"));
    conflator.put(quote("VALE3", "61.20"));
    conflator.put(quote("PETR4", "38.50"));

    assertThat(conflator.drain())
        .extracting(q -> q.ticker().value() + " " + Objects.requireNonNull(q.tick()).last())
        .containsExactly("PETR4 BRL 38.50", "VALE3 BRL 61.20");
  }

  @Test
  void shouldStartEmptyAfterDraining() {
    conflator.put(quote("PETR4", "38.47"));
    conflator.drain();

    assertThat(conflator.drain()).isEmpty();
  }

  private static AssetQuote quote(String ticker, String price) {
    Tick tick = new Tick(Ticker.of(ticker), Money.brl(price), null, null, 0, NOW, TickSource.LIVE);
    return new AssetQuote(tick.ticker(), null, tick);
  }
}

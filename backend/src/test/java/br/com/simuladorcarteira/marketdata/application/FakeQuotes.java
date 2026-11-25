package br.com.simuladorcarteira.marketdata.application;

import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.shared.Ticker;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class FakeQuotes implements LatestQuoteRepository {

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

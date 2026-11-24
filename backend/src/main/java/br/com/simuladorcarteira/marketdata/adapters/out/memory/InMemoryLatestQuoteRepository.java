package br.com.simuladorcarteira.marketdata.adapters.out.memory;

import br.com.simuladorcarteira.marketdata.application.LatestQuoteRepository;
import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.shared.Ticker;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** Cotações atuais em memória. Vai para o Redis quando houver mais de uma instância (ADR-006). */
@Repository
class InMemoryLatestQuoteRepository implements LatestQuoteRepository {

  private final Map<Ticker, Tick> latest = new ConcurrentHashMap<>();

  @Override
  public Optional<Tick> findByTicker(Ticker ticker) {
    return Optional.ofNullable(latest.get(ticker));
  }

  @Override
  public List<Tick> findAll() {
    return List.copyOf(latest.values());
  }

  @Override
  public void save(Tick tick) {
    latest.put(tick.ticker(), tick);
  }
}

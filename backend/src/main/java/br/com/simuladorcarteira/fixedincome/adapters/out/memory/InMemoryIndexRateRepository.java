package br.com.simuladorcarteira.fixedincome.adapters.out.memory;

import br.com.simuladorcarteira.fixedincome.application.IndexRateRepository;
import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import br.com.simuladorcarteira.fixedincome.domain.RateIndex;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.springframework.stereotype.Repository;

/** Histórico em memória até a persistência chegar (issue #4): a carga inicial roda a cada subida. */
@Repository
class InMemoryIndexRateRepository implements IndexRateRepository {

  private final Map<RateIndex, NavigableMap<LocalDate, IndexRate>> byIndex =
      new ConcurrentHashMap<>();

  @Override
  public void saveAll(List<IndexRate> rates) {
    for (IndexRate rate : rates) {
      series(rate.index()).put(rate.date(), rate);
    }
  }

  @Override
  public Optional<IndexRate> latest(RateIndex index) {
    var last = series(index).lastEntry();
    return last == null ? Optional.empty() : Optional.of(last.getValue());
  }

  @Override
  public List<IndexRate> between(RateIndex index, LocalDate from, LocalDate to) {
    if (to.isBefore(from)) {
      return List.of();
    }
    return List.copyOf(series(index).subMap(from, true, to, true).values());
  }

  private NavigableMap<LocalDate, IndexRate> series(RateIndex index) {
    return byIndex.computeIfAbsent(index, key -> new ConcurrentSkipListMap<>());
  }
}

package br.com.simuladorcarteira.fixedincome.application;

import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import br.com.simuladorcarteira.fixedincome.domain.RateIndex;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Caso de uso: traz do Banco Central as taxas que ainda não temos, de cada indexador. */
@Service
public class SyncIndexRates {

  private static final Logger log = LoggerFactory.getLogger(SyncIndexRates.class);

  private final IndexRateRepository rates;
  private final IndexRateSource source;

  public SyncIndexRates(IndexRateRepository rates, IndexRateSource source) {
    this.rates = rates;
    this.source = source;
  }

  /**
   * Busca do dia seguinte à última taxa guardada até hoje; sem histórico, desde {@code
   * backfillStart}. Uma falha num indexador não impede os outros.
   *
   * @return quantas taxas novas foram guardadas
   */
  public int sync(LocalDate backfillStart, LocalDate today) {
    int saved = 0;
    for (RateIndex index : RateIndex.values()) {
      LocalDate from = rates.latest(index).map(r -> r.date().plusDays(1)).orElse(backfillStart);
      if (from.isAfter(today)) {
        continue;
      }
      try {
        List<IndexRate> fetched = source.fetch(index, from, today);
        rates.saveAll(fetched);
        saved += fetched.size();
      } catch (RuntimeException e) {
        log.warn("Não deu para atualizar {} desde {}: {}", index, from, e.getMessage());
      }
    }
    return saved;
  }
}

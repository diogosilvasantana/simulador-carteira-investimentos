package br.com.simuladorcarteira.marketdata.application;

import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.shared.Ticker;
import java.util.List;
import java.util.Optional;

/** Porta de saída: guarda a última cotação de cada ativo. */
public interface LatestQuoteRepository {

  Optional<Tick> findByTicker(Ticker ticker);

  List<Tick> findAll();

  void save(Tick tick);
}

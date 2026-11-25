package br.com.simuladorcarteira.marketdata.application;

import br.com.simuladorcarteira.marketdata.domain.Asset;
import br.com.simuladorcarteira.shared.Ticker;
import java.util.List;
import java.util.Optional;

/** Porta de saída: o catálogo de ativos. */
public interface AssetRepository {

  Optional<Asset> findByTicker(Ticker ticker);

  List<Asset> findAll();

  void save(Asset asset);
}

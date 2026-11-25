package br.com.simuladorcarteira.marketdata.adapters.out.memory;

import br.com.simuladorcarteira.marketdata.application.AssetRepository;
import br.com.simuladorcarteira.marketdata.domain.Asset;
import br.com.simuladorcarteira.shared.Ticker;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** Catálogo em memória: a ponte reenvia todos os ativos a cada conexão, então nada se perde. */
@Repository
class InMemoryAssetRepository implements AssetRepository {

  private final Map<Ticker, Asset> assets = new ConcurrentHashMap<>();

  @Override
  public Optional<Asset> findByTicker(Ticker ticker) {
    return Optional.ofNullable(assets.get(ticker));
  }

  @Override
  public List<Asset> findAll() {
    return List.copyOf(assets.values());
  }

  @Override
  public void save(Asset asset) {
    assets.put(asset.ticker(), asset);
  }
}

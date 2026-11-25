package br.com.simuladorcarteira.marketdata.application;

import br.com.simuladorcarteira.marketdata.domain.Asset;
import br.com.simuladorcarteira.shared.Ticker;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class FakeAssets implements AssetRepository {

  private final Map<Ticker, Asset> data = new HashMap<>();

  @Override
  public Optional<Asset> findByTicker(Ticker ticker) {
    return Optional.ofNullable(data.get(ticker));
  }

  @Override
  public List<Asset> findAll() {
    return List.copyOf(data.values());
  }

  @Override
  public void save(Asset asset) {
    data.put(asset.ticker(), asset);
  }
}

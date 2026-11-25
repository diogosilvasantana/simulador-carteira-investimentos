package br.com.simuladorcarteira.marketdata.application;

import br.com.simuladorcarteira.marketdata.domain.Asset;
import br.com.simuladorcarteira.marketdata.domain.AssetClassifier;
import br.com.simuladorcarteira.shared.Ticker;
import org.springframework.stereotype.Service;

/** Caso de uso: o feed informa um ativo e o catálogo guarda a classificação dele (ADR-010). */
@Service
public class RegisterAsset {

  private final AssetRepository assets;

  public RegisterAsset(AssetRepository assets) {
    this.assets = assets;
  }

  public Asset register(Ticker ticker, String description) {
    Asset asset = AssetClassifier.classify(ticker, description);
    assets.save(asset);
    return asset;
  }
}

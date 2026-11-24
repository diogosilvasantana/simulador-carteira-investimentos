package br.com.simuladorcarteira.marketdata.domain;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

/** Tipo do papel segundo a especificação da B3. */
public enum AssetType {
  STOCK(AssetCategory.STOCKS),
  UNIT(AssetCategory.STOCKS),
  REAL_ESTATE_FUND(AssetCategory.REAL_ESTATE_FUNDS),
  AGRIBUSINESS_FUND(AssetCategory.AGRIBUSINESS_FUNDS),
  ETF(AssetCategory.ETFS),
  BDR(AssetCategory.BDRS),
  BDR_ETF(AssetCategory.BDRS),
  /** Não reconhecido (futuros, ativos de teste, outros fundos): fica fora da vitrine. */
  OTHER(null);

  private final @Nullable AssetCategory category;

  AssetType(@Nullable AssetCategory category) {
    this.category = category;
  }

  public Optional<AssetCategory> category() {
    return Optional.ofNullable(category);
  }
}

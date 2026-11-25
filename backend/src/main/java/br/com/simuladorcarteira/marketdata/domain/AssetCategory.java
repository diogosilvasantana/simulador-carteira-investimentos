package br.com.simuladorcarteira.marketdata.domain;

import java.util.Arrays;
import java.util.Optional;

/** Agrupamento da vitrine: o que o investidor procura como "ações", "FIIs" e assim por diante. */
public enum AssetCategory {
  STOCKS("acoes"),
  REAL_ESTATE_FUNDS("fiis"),
  AGRIBUSINESS_FUNDS("fiagros"),
  ETFS("etfs"),
  BDRS("bdrs");

  private final String slug;

  AssetCategory(String slug) {
    this.slug = slug;
  }

  /** Identificador estável usado em URLs e tópicos. */
  public String slug() {
    return slug;
  }

  public static Optional<AssetCategory> fromSlug(String slug) {
    return Arrays.stream(values()).filter(category -> category.slug.equals(slug)).findFirst();
  }
}

package br.com.simuladorcarteira.portfolio.domain;

/**
 * Classe de ativo da carteira, na ordem fixa em que aparece na alocação e nos gráficos (design
 * system). Os slugs são os mesmos das categorias do mercado e da tela.
 */
public enum AssetClass {
  STOCKS("acoes"),
  FIXED_INCOME("renda-fixa"),
  AGRIBUSINESS_FUNDS("fiagros"),
  REAL_ESTATE_FUNDS("fiis"),
  BDRS("bdrs"),
  ETFS("etfs");

  private final String slug;

  AssetClass(String slug) {
    this.slug = slug;
  }

  public String slug() {
    return slug;
  }

  public static AssetClass fromSlug(String slug) {
    for (AssetClass value : values()) {
      if (value.slug.equals(slug)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Classe de ativo desconhecida: " + slug);
  }
}

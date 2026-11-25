package br.com.simuladorcarteira.fixedincome.adapters.out.config;

import br.com.simuladorcarteira.fixedincome.application.FixedIncomeProducts;
import br.com.simuladorcarteira.fixedincome.domain.FixedIncomeProduct;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Produtos modelo da configuração: mudar taxa ou criar produto não exige código (ADR-011). */
@ConfigurationProperties("simulador.fixedincome")
record ProductProperties(List<FixedIncomeProduct> products) implements FixedIncomeProducts {

  ProductProperties {
    products = List.copyOf(products);
  }

  @Override
  public List<FixedIncomeProduct> all() {
    return products;
  }
}

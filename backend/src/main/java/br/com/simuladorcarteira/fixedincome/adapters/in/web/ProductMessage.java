package br.com.simuladorcarteira.fixedincome.adapters.in.web;

import br.com.simuladorcarteira.fixedincome.domain.FixedIncomeProduct;

/** Produto modelo como sai pela API; percentuais em texto decimal. */
record ProductMessage(
    String id,
    String name,
    String kind,
    String indexPercentage,
    boolean incomeTaxExempt,
    int gracePeriodDays) {

  static ProductMessage from(FixedIncomeProduct product) {
    return new ProductMessage(
        product.id(),
        product.name(),
        product.kind().name(),
        product.indexPercentage().toPlainString(),
        product.incomeTaxExempt(),
        product.gracePeriodDays());
  }
}

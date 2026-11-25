package br.com.simuladorcarteira.fixedincome.application;

import br.com.simuladorcarteira.fixedincome.domain.FixedIncomeProduct;
import java.util.List;

/** Porta de saída: o catálogo de produtos modelo de renda fixa. */
public interface FixedIncomeProducts {

  List<FixedIncomeProduct> all();
}

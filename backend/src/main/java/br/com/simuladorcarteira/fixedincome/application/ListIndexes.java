package br.com.simuladorcarteira.fixedincome.application;

import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import br.com.simuladorcarteira.fixedincome.domain.RateIndex;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Caso de uso: o valor mais recente de cada indexador, para a tela de renda fixa. */
@Service
public class ListIndexes {

  private final IndexRateRepository rates;

  public ListIndexes(IndexRateRepository rates) {
    this.rates = rates;
  }

  public List<IndexRate> latest() {
    return Arrays.stream(RateIndex.values()).map(rates::latest).flatMap(Optional::stream).toList();
  }
}

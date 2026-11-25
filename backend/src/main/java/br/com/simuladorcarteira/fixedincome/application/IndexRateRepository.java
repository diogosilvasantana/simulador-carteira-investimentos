package br.com.simuladorcarteira.fixedincome.application;

import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import br.com.simuladorcarteira.fixedincome.domain.RateIndex;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Porta de saída: histórico dos indexadores. */
public interface IndexRateRepository {

  void saveAll(List<IndexRate> rates);

  Optional<IndexRate> latest(RateIndex index);

  /** Taxas do indexador entre as duas datas, inclusive, em ordem de data. */
  List<IndexRate> between(RateIndex index, LocalDate from, LocalDate to);
}

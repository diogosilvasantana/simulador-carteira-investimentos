package br.com.simuladorcarteira.fixedincome.application;

import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import br.com.simuladorcarteira.fixedincome.domain.RateIndex;
import java.time.LocalDate;
import java.util.List;

/** Porta de saída: de onde vêm as taxas oficiais (o Banco Central, ADR-011). */
public interface IndexRateSource {

  List<IndexRate> fetch(RateIndex index, LocalDate from, LocalDate to);
}

package br.com.simuladorcarteira.fixedincome.adapters.in.web;

import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/**
 * Valor de um indexador. Percentuais em texto decimal, como os preços; {@code annualPercent} traz a
 * taxa equivalente ao ano, nula para o IPCA.
 */
record IndexRateMessage(
    String index, LocalDate date, String percent, String unit, @Nullable String annualPercent) {

  static IndexRateMessage from(IndexRate rate) {
    String unit =
        switch (rate.index()) {
          case CDI -> "% ao dia";
          case SELIC_TARGET -> "% ao ano";
          case IPCA -> "% no mês";
        };
    return new IndexRateMessage(
        rate.index().name(), rate.date(), rate.percent().toPlainString(), unit);
  }
}

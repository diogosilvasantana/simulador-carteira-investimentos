package br.com.simuladorcarteira.fixedincome.adapters.in.web;

import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import java.time.LocalDate;

/** Valor de um indexador. Percentuais em texto decimal, como os preços (ver QuoteMessage). */
record IndexRateMessage(String index, LocalDate date, String percent, String unit) {

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

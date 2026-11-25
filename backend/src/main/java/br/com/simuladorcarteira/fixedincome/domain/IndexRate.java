package br.com.simuladorcarteira.fixedincome.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Valor de um indexador numa data, em percentual (a unidade depende do {@link RateIndex}). */
public record IndexRate(RateIndex index, LocalDate date, BigDecimal percent) {

  public IndexRate {
    Objects.requireNonNull(index, "index");
    Objects.requireNonNull(date, "date");
    Objects.requireNonNull(percent, "percent");
  }
}

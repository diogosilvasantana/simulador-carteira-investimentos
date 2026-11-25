package br.com.simuladorcarteira.portfolio.domain;

import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Custos de negociação versionados por data (sem números mágicos no código): a taxa vigente num dia
 * é a da tabela mais recente com início até ele.
 */
public record TradingCosts(List<Rate> rates) {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  /** Percentual sobre o volume financeiro (emolumentos e liquidação da B3). */
  public record Rate(LocalDate validFrom, BigDecimal feePercent) {
    public Rate {
      Objects.requireNonNull(validFrom, "validFrom");
      Objects.requireNonNull(feePercent, "feePercent");
      if (feePercent.signum() < 0) {
        throw new IllegalArgumentException("Taxa negativa");
      }
    }
  }

  public TradingCosts {
    rates = rates.stream().sorted(Comparator.comparing(Rate::validFrom)).toList();
    if (rates.isEmpty()) {
      throw new IllegalArgumentException("Sem tabela de custos");
    }
  }

  public Money feeOn(LocalDate day, Money gross) {
    Rate current = rates.getFirst();
    for (Rate rate : rates) {
      if (!rate.validFrom().isAfter(day)) {
        current = rate;
      }
    }
    return gross.times(current.feePercent().divide(HUNDRED, MathContext.DECIMAL64));
  }
}

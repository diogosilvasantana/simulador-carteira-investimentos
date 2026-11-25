package br.com.simuladorcarteira.fixedincome.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Tabela de imposto de renda sobre o rendimento, válida a partir de uma data. As faixas vêm em
 * ordem de prazo; a última, sem limite, vale para qualquer prazo maior.
 */
public record IncomeTaxTable(LocalDate validFrom, List<Bracket> brackets) {

  /** Faixa até {@code upToDays} dias corridos (inclusive); {@code null} = sem limite. */
  public record Bracket(@Nullable Integer upToDays, BigDecimal ratePercent) {

    public Bracket {
      Objects.requireNonNull(ratePercent, "ratePercent");
    }
  }

  public IncomeTaxTable {
    Objects.requireNonNull(validFrom, "validFrom");
    brackets = List.copyOf(brackets);
    if (brackets.isEmpty() || brackets.getLast().upToDays() != null) {
      throw new IllegalArgumentException("A última faixa de IR precisa valer sem limite de prazo");
    }
  }

  public BigDecimal rateFor(long holdingDays) {
    for (Bracket bracket : brackets) {
      Integer limit = bracket.upToDays();
      if (limit == null || holdingDays <= limit) {
        return bracket.ratePercent();
      }
    }
    return brackets.getLast().ratePercent();
  }
}

package br.com.simuladorcarteira.fixedincome;

import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/** API pública do módulo fixedincome para a carteira aplicar e avaliar renda fixa (ADR-004). */
public interface FixedIncomeMarket {

  Optional<FixedIncomeOffer> offer(String productId);

  FixedIncomeValue valueOf(
      Money principal,
      BigDecimal indexPercentage,
      LocalDate start,
      LocalDate today,
      boolean incomeTaxExempt);
}

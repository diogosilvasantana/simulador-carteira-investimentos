package br.com.simuladorcarteira.fixedincome.application;

import br.com.simuladorcarteira.fixedincome.FixedIncomeMarket;
import br.com.simuladorcarteira.fixedincome.FixedIncomeOffer;
import br.com.simuladorcarteira.fixedincome.FixedIncomeValue;
import br.com.simuladorcarteira.fixedincome.domain.MissingIndexRatesException;
import br.com.simuladorcarteira.fixedincome.domain.RedemptionResult;
import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Implementa a API pública com o catálogo de produtos e a simulação de resgate. */
@Service
public class FixedIncomeFacade implements FixedIncomeMarket {

  private final FixedIncomeProducts products;
  private final SimulateRedemption simulateRedemption;

  public FixedIncomeFacade(FixedIncomeProducts products, SimulateRedemption simulateRedemption) {
    this.products = products;
    this.simulateRedemption = simulateRedemption;
  }

  @Override
  public Optional<FixedIncomeOffer> offer(String productId) {
    return products.all().stream()
        .filter(product -> product.id().equals(productId))
        .findFirst()
        .map(
            product ->
                new FixedIncomeOffer(
                    product.id(),
                    product.name(),
                    product.indexPercentage(),
                    product.incomeTaxExempt(),
                    product.gracePeriodDays()));
  }

  @Override
  public FixedIncomeValue valueOf(
      Money principal,
      BigDecimal indexPercentage,
      LocalDate start,
      LocalDate today,
      boolean incomeTaxExempt) {
    try {
      RedemptionResult result =
          simulateRedemption
              .simulate(principal, indexPercentage, start, today, incomeTaxExempt)
              .result();
      return new FixedIncomeValue(
          result.gross(), result.iof(), result.incomeTax(), result.net(), true);
    } catch (MissingIndexRatesException e) {
      // Sem CDI ainda (backend acabou de subir): a aplicação vale o que entrou até os dados
      // chegarem.
      return new FixedIncomeValue(principal, Money.zero(), Money.zero(), principal, false);
    }
  }
}

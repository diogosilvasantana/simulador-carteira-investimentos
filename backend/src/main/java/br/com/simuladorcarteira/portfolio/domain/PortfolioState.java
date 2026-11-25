package br.com.simuladorcarteira.portfolio.domain;

import br.com.simuladorcarteira.shared.Money;
import java.util.List;
import java.util.Objects;

/**
 * Tudo o que define a carteira num instante, para gravar e reconstruir sem expor os campos do
 * agregado. Inclui as posições zeradas, que guardam o resultado realizado das vendas.
 *
 * @param lastId último número dado a uma execução ou aplicação, para continuar a sequência
 */
public record PortfolioState(
    Money cash,
    Money contributed,
    long lastId,
    List<Position> positions,
    List<FixedIncomeHolding> fixedIncome,
    List<Contribution> contributions,
    List<Execution> executions) {

  public PortfolioState {
    Objects.requireNonNull(cash, "cash");
    Objects.requireNonNull(contributed, "contributed");
    positions = List.copyOf(positions);
    fixedIncome = List.copyOf(fixedIncome);
    contributions = List.copyOf(contributions);
    executions = List.copyOf(executions);
  }

  public static PortfolioState empty() {
    return new PortfolioState(
        Money.zero(), Money.zero(), 0, List.of(), List.of(), List.of(), List.of());
  }
}

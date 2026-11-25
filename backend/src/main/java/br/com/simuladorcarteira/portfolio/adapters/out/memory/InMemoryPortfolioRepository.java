package br.com.simuladorcarteira.portfolio.adapters.out.memory;

import br.com.simuladorcarteira.portfolio.application.PortfolioRepository;
import br.com.simuladorcarteira.portfolio.domain.Portfolio;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/**
 * Uma carteira só, em memória, até chegarem o login e a persistência (#4): reiniciar o backend
 * recomeça o treino.
 */
@Repository
class InMemoryPortfolioRepository implements PortfolioRepository {

  private Portfolio portfolio = new Portfolio();

  @Override
  public synchronized <T> T update(Function<Portfolio, T> change) {
    return change.apply(portfolio);
  }

  @Override
  public synchronized void reset() {
    portfolio = new Portfolio();
  }
}

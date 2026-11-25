package br.com.simuladorcarteira.portfolio.application;

import br.com.simuladorcarteira.portfolio.domain.Portfolio;
import java.util.function.Function;

/**
 * Porta de saída: guarda a carteira. Toda leitura e alteração passa por {@link #update} para que
 * duas ordens simultâneas não gastem o mesmo caixa.
 */
public interface PortfolioRepository {

  <T> T update(Function<Portfolio, T> change);

  void reset();
}

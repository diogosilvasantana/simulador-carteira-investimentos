package br.com.simuladorcarteira.marketdata;

import br.com.simuladorcarteira.shared.Ticker;
import java.util.Optional;

/**
 * API pública do módulo marketdata (ADR-004): a última cotação de um ativo, para outros módulos
 * executarem ordens e avaliarem posições sem conhecer o domínio do mercado.
 */
public interface MarketQuotes {

  Optional<MarketQuote> find(Ticker ticker);
}

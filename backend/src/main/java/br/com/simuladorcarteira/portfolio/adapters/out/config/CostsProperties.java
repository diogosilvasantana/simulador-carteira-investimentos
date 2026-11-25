package br.com.simuladorcarteira.portfolio.adapters.out.config;

import br.com.simuladorcarteira.portfolio.domain.TradingCosts;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Tabela de custos de negociação vinda da configuração, versionada por data. */
@ConfigurationProperties("simulador.portfolio")
record CostsProperties(List<TradingCosts.Rate> tradingCosts) {

  CostsProperties {
    tradingCosts = List.copyOf(tradingCosts);
  }
}

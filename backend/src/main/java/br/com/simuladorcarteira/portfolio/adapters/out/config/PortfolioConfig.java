package br.com.simuladorcarteira.portfolio.adapters.out.config;

import br.com.simuladorcarteira.portfolio.domain.TradingCosts;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CostsProperties.class)
class PortfolioConfig {

  @Bean
  TradingCosts tradingCosts(CostsProperties properties) {
    return new TradingCosts(properties.tradingCosts());
  }

  @Bean
  @ConditionalOnMissingBean
  Clock clock() {
    return Clock.systemUTC();
  }
}

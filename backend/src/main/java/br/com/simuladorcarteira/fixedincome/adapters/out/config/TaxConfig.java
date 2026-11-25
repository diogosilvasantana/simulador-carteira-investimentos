package br.com.simuladorcarteira.fixedincome.adapters.out.config;

import br.com.simuladorcarteira.fixedincome.domain.TaxRules;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TaxProperties.class)
class TaxConfig {

  @Bean
  TaxRules taxRules(TaxProperties properties) {
    return new TaxRules(properties.incomeTax(), properties.iof());
  }
}

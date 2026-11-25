package br.com.simuladorcarteira.fixedincome.adapters.out.bcb;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(BcbProperties.class)
class BcbConfig {

  @Bean
  BcbSgsClient bcbSgsClient(BcbProperties properties) {
    // Prazos curtos: se o Banco Central estiver fora, a subida do sistema não fica esperando.
    SimpleClientHttpRequestFactory requests = new SimpleClientHttpRequestFactory();
    requests.setConnectTimeout(Duration.ofSeconds(5));
    requests.setReadTimeout(Duration.ofSeconds(20));
    return new BcbSgsClient(RestClient.builder().requestFactory(requests), properties);
  }
}

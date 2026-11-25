package br.com.simuladorcarteira.marketdata.adapters.out.stomp;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/** Endpoint STOMP {@code /ws} para o navegador, com broker simples em memória (ADR-006). */
@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
@EnableConfigurationProperties(RealtimeProperties.class)
class StompConfig implements WebSocketMessageBrokerConfigurer {

  private final RealtimeProperties properties;

  StompConfig(RealtimeProperties properties) {
    this.properties = properties;
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns(properties.allowedOrigins().toArray(String[]::new));
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }
}

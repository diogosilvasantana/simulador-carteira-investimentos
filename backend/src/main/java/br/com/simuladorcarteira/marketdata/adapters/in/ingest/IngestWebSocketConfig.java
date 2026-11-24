package br.com.simuladorcarteira.marketdata.adapters.in.ingest;

import br.com.simuladorcarteira.marketdata.application.IngestTick;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import tools.jackson.databind.ObjectMapper;

/**
 * WebSocket {@code /ingest}: a ponte MT5 (ou o simulador de pregão) abre a conexão e envia os
 * ticks. Em produção, a porta só é publicada na rede Tailscale (ADR-003).
 */
@Configuration
@EnableWebSocket
@EnableConfigurationProperties(IngestProperties.class)
class IngestWebSocketConfig implements WebSocketConfigurer {

  private final IngestTick ingestTick;
  private final ObjectMapper objectMapper;
  private final IngestProperties properties;

  IngestWebSocketConfig(
      IngestTick ingestTick, ObjectMapper objectMapper, IngestProperties properties) {
    this.ingestTick = ingestTick;
    this.objectMapper = objectMapper;
    this.properties = properties;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(new TickIngestHandler(ingestTick, objectMapper), "/ingest")
        .addInterceptors(new IngestTokenInterceptor(properties.token()));
  }
}

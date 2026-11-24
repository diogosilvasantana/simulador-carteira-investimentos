package br.com.simuladorcarteira.marketdata.adapters.in.ingest;

import br.com.simuladorcarteira.marketdata.application.IngestTick;
import br.com.simuladorcarteira.marketdata.application.RegisterAsset;
import br.com.simuladorcarteira.marketdata.domain.InvalidTickException;
import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.marketdata.domain.TickSource;
import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Recebe as mensagens da ponte (cadastro de ativos, ticks e heartbeats). Uma mensagem inválida é registrada e
 * descartada sem derrubar a conexão, para um tick ruim não interromper o feed.
 */
class TickIngestHandler extends TextWebSocketHandler {

  private static final Logger log = LoggerFactory.getLogger(TickIngestHandler.class);

  private final IngestTick ingestTick;
  private final RegisterAsset registerAsset;
  private final ObjectMapper objectMapper;

  TickIngestHandler(IngestTick ingestTick, RegisterAsset registerAsset, ObjectMapper objectMapper) {
    this.ingestTick = ingestTick;
    this.registerAsset = registerAsset;
    this.objectMapper = objectMapper;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    log.info("Feed conectado: {}", session.getRemoteAddress());
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    try {
      IngestMessage incoming = objectMapper.readValue(message.getPayload(), IngestMessage.class);
      String type = Objects.requireNonNullElse(incoming.type(), "");
      switch (type) {
        case "tick" -> ingestTick.ingest(toTick(incoming));
        case "asset" ->
            registerAsset.register(
                Ticker.of(required(incoming.ticker(), "ticker")),
                required(incoming.description(), "description"));
        case "heartbeat" -> log.debug("Heartbeat do feed {}", incoming.source());
        default -> log.warn("Tipo de mensagem desconhecido: '{}'", type);
      }
    } catch (JacksonException
        | InvalidTickException
        | IllegalArgumentException
        | DateTimeParseException e) {
      log.warn("Mensagem de ingestão descartada: {}", e.getMessage());
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    log.info("Feed desconectado: {} ({})", session.getRemoteAddress(), status);
  }

  private static Tick toTick(IngestMessage message) {
    return new Tick(
        Ticker.of(required(message.ticker(), "ticker")),
        Money.brl(required(message.last(), "last")),
        moneyOrNull(message.bid()),
        moneyOrNull(message.ask()),
        moneyOrNull(message.previousClose()),
        Objects.requireNonNullElse(message.volume(), 0L),
        Instant.parse(required(message.occurredAt(), "occurred_at")),
        TickSource.valueOf(Objects.requireNonNullElse(message.source(), "LIVE")));
  }

  private static String required(@Nullable String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("campo obrigatório ausente: " + field);
    }
    return value;
  }

  private static @Nullable Money moneyOrNull(@Nullable String value) {
    return value == null ? null : Money.brl(new BigDecimal(value));
  }
}

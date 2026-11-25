package br.com.simuladorcarteira.marketdata.adapters.out.stomp;

import br.com.simuladorcarteira.marketdata.adapters.in.web.QuoteMessage;
import br.com.simuladorcarteira.marketdata.application.QuoteBroadcaster;
import br.com.simuladorcarteira.marketdata.application.QuoteConflator;
import br.com.simuladorcarteira.marketdata.domain.AssetCategory;
import br.com.simuladorcarteira.marketdata.domain.AssetQuote;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Publica as cotações em lotes (ADR-010): a cada intervalo, um lote por categoria em
 * {@code /topic/quotes/category/{categoria}} e uma mensagem por ativo que mudou em
 * {@code /topic/quotes/{ticker}}. Quem não assina um tópico não recebe nada dele.
 */
@Component
class StompQuoteBroadcaster implements QuoteBroadcaster {

  static final String TICKER_TOPIC = "/topic/quotes/";
  static final String CATEGORY_TOPIC = "/topic/quotes/category/";

  private final SimpMessagingTemplate messaging;
  private final QuoteConflator conflator = new QuoteConflator();

  StompQuoteBroadcaster(SimpMessagingTemplate messaging) {
    this.messaging = messaging;
  }

  @Override
  public void broadcast(AssetQuote quote) {
    conflator.put(quote);
  }

  @Scheduled(fixedDelayString = "${simulador.realtime.flush-interval-ms:500}")
  void flush() {
    List<AssetQuote> batch = conflator.drain();
    if (batch.isEmpty()) {
      return;
    }
    Map<AssetCategory, List<QuoteMessage>> byCategory = new EnumMap<>(AssetCategory.class);
    for (AssetQuote quote : batch) {
      QuoteMessage message = QuoteMessage.from(quote);
      messaging.convertAndSend(TICKER_TOPIC + message.ticker(), message);
      quote
          .category()
          .ifPresent(
              category ->
                  byCategory.computeIfAbsent(category, key -> new ArrayList<>()).add(message));
    }
    byCategory.forEach(
        (category, messages) ->
            messaging.convertAndSend(CATEGORY_TOPIC + category.slug(), messages));
  }
}

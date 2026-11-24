package br.com.simuladorcarteira.marketdata.adapters.out.stomp;

import br.com.simuladorcarteira.marketdata.adapters.in.web.QuoteMessage;
import br.com.simuladorcarteira.marketdata.application.QuoteBroadcaster;
import br.com.simuladorcarteira.marketdata.domain.Tick;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica cada cotação em dois tópicos: {@code /topic/quotes} (todas, para a lista) e
 * {@code /topic/quotes/{ticker}} (um ativo, para telas de detalhe).
 */
@Component
class StompQuoteBroadcaster implements QuoteBroadcaster {

  static final String ALL_QUOTES = "/topic/quotes";

  private final SimpMessagingTemplate messaging;

  StompQuoteBroadcaster(SimpMessagingTemplate messaging) {
    this.messaging = messaging;
  }

  @Override
  public void broadcast(Tick tick) {
    QuoteMessage message = QuoteMessage.from(tick);
    messaging.convertAndSend(ALL_QUOTES, message);
    messaging.convertAndSend(ALL_QUOTES + "/" + message.ticker(), message);
  }
}

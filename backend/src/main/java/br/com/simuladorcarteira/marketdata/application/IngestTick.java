package br.com.simuladorcarteira.marketdata.application;

import br.com.simuladorcarteira.marketdata.domain.Tick;
import org.springframework.stereotype.Service;

/** Caso de uso: recebe um tick do feed, guarda como cotação atual e repassa a quem assiste. */
@Service
public class IngestTick {

  private final LatestQuoteRepository quotes;
  private final QuoteBroadcaster broadcaster;

  public IngestTick(LatestQuoteRepository quotes, QuoteBroadcaster broadcaster) {
    this.quotes = quotes;
    this.broadcaster = broadcaster;
  }

  /**
   * Retorna {@code false} quando o tick é mais antigo que a cotação guardada e foi descartado.
   */
  public boolean ingest(Tick tick) {
    boolean stale =
        quotes.findByTicker(tick.ticker()).map(current -> !tick.isNewerThan(current)).orElse(false);
    if (stale) {
      return false;
    }
    quotes.save(tick);
    broadcaster.broadcast(tick);
    return true;
  }
}

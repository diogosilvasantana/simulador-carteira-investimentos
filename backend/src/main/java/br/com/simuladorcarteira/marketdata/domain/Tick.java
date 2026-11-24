package br.com.simuladorcarteira.marketdata.domain;

import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.time.Instant;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Atualização de preço de um ativo vinda do feed.
 *
 * <p>Compra (bid) e venda (ask) são opcionais porque alguns feeds só informam o último negócio.
 */
public record Tick(
    Ticker ticker,
    Money last,
    @Nullable Money bid,
    @Nullable Money ask,
    long volume,
    Instant occurredAt,
    TickSource source) {

  public Tick {
    Objects.requireNonNull(ticker, "ticker");
    Objects.requireNonNull(last, "last");
    Objects.requireNonNull(occurredAt, "occurredAt");
    Objects.requireNonNull(source, "source");
    if (!last.isGreaterThan(Money.zero())) {
      throw new InvalidTickException("Preço inválido para %s: %s".formatted(ticker, last));
    }
    if (volume < 0) {
      throw new InvalidTickException("Volume negativo para %s: %d".formatted(ticker, volume));
    }
    if (bid != null && ask != null && bid.isGreaterThan(ask)) {
      throw new InvalidTickException(
          "Compra maior que venda para %s: %s > %s".formatted(ticker, bid, ask));
    }
  }

  /**
   * Ticks podem chegar fora de ordem quando a ponte reconecta; só o mais recente vale como
   * cotação atual.
   */
  public boolean isNewerThan(Tick other) {
    return occurredAt.isAfter(other.occurredAt);
  }
}

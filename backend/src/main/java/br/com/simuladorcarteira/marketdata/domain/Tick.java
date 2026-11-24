package br.com.simuladorcarteira.marketdata.domain;

import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Atualização de preço de um ativo vinda do feed.
 *
 * <p>Compra (bid), venda (ask) e fechamento anterior são opcionais porque nem todo feed os informa.
 */
public record Tick(
    Ticker ticker,
    Money last,
    @Nullable Money bid,
    @Nullable Money ask,
    @Nullable Money previousClose,
    long volume,
    Instant occurredAt,
    TickSource source) {

  private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
  private static final int PERCENT_SCALE = 2;

  public Tick {
    Objects.requireNonNull(ticker, "ticker");
    Objects.requireNonNull(last, "last");
    Objects.requireNonNull(occurredAt, "occurredAt");
    Objects.requireNonNull(source, "source");
    if (!last.isGreaterThan(Money.zero())) {
      throw new InvalidTickException("Preço inválido para %s: %s".formatted(ticker, last));
    }
    if (previousClose != null && !previousClose.isGreaterThan(Money.zero())) {
      throw new InvalidTickException(
          "Fechamento anterior inválido para %s: %s".formatted(ticker, previousClose));
    }
    if (volume < 0) {
      throw new InvalidTickException("Volume negativo para %s: %d".formatted(ticker, volume));
    }
    if (bid != null && ask != null && bid.isGreaterThan(ask)) {
      throw new InvalidTickException(
          "Compra maior que venda para %s: %s > %s".formatted(ticker, bid, ask));
    }
  }

  /** Tick sem fechamento anterior, para feeds que não o informam. */
  public Tick(
      Ticker ticker,
      Money last,
      @Nullable Money bid,
      @Nullable Money ask,
      long volume,
      Instant occurredAt,
      TickSource source) {
    this(ticker, last, bid, ask, null, volume, occurredAt, source);
  }

  /**
   * Ticks podem chegar fora de ordem quando a ponte reconecta; só o mais recente vale como
   * cotação atual.
   */
  public boolean isNewerThan(Tick other) {
    return occurredAt.isAfter(other.occurredAt);
  }

  /**
   * Variação em relação ao fechamento do pregão anterior, em pontos percentuais com duas casas.
   * Nula quando o feed não informou o fechamento.
   */
  public @Nullable BigDecimal changePercent() {
    if (previousClose == null) {
      return null;
    }
    BigDecimal base = previousClose.amount();
    return last.amount()
        .subtract(base)
        .multiply(ONE_HUNDRED)
        .divide(base, PERCENT_SCALE, RoundingMode.HALF_EVEN);
  }
}

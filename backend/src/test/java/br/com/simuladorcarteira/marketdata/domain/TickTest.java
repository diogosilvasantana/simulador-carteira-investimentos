package br.com.simuladorcarteira.marketdata.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TickTest {

  private static final Instant NOW = Instant.parse("2026-11-24T13:15:00Z");

  @Test
  void shouldAcceptTickWithoutBidAndAsk() {
    Tick tick =
        new Tick(Ticker.of("PETR4"), Money.brl("38.47"), null, null, 100, NOW, TickSource.LIVE);

    assertThat(tick.bid()).isNull();
    assertThat(tick.last()).isEqualTo(Money.brl("38.47"));
  }

  @Test
  void shouldRejectNonPositivePrice() {
    assertThatThrownBy(
            () -> new Tick(Ticker.of("PETR4"), Money.zero(), null, null, 0, NOW, TickSource.LIVE))
        .isInstanceOf(InvalidTickException.class)
        .hasMessageContaining("Preço inválido");
  }

  @Test
  void shouldRejectNegativeVolume() {
    assertThatThrownBy(
            () ->
                new Tick(Ticker.of("PETR4"), Money.brl("10"), null, null, -1, NOW, TickSource.LIVE))
        .isInstanceOf(InvalidTickException.class)
        .hasMessageContaining("Volume negativo");
  }

  @Test
  void shouldRejectBidAboveAsk() {
    assertThatThrownBy(
            () ->
                new Tick(
                    Ticker.of("PETR4"),
                    Money.brl("38.47"),
                    Money.brl("38.50"),
                    Money.brl("38.48"),
                    0,
                    NOW,
                    TickSource.LIVE))
        .isInstanceOf(InvalidTickException.class)
        .hasMessageContaining("Compra maior que venda");
  }

  @Test
  void shouldCompareByOccurrenceTime() {
    Tick older = tickAt(NOW);
    Tick newer = tickAt(NOW.plusMillis(250));

    assertThat(newer.isNewerThan(older)).isTrue();
    assertThat(older.isNewerThan(newer)).isFalse();
    assertThat(older.isNewerThan(older)).isFalse();
  }

  @Test
  void shouldComputeChangePercentOverPreviousClose() {
    Tick tick = tickWithPreviousClose("38.47", "37.00");

    assertThat(tick.changePercent()).isEqualByComparingTo(new BigDecimal("3.97"));
  }

  @Test
  void shouldReportNegativeChangeWhenPriceFell() {
    Tick tick = tickWithPreviousClose("9.70", "9.75");

    assertThat(tick.changePercent()).isEqualByComparingTo(new BigDecimal("-0.51"));
  }

  @Test
  void shouldHaveNoChangePercentWhenPreviousCloseIsUnknown() {
    Tick tick = tickAt(NOW);

    assertThat(tick.changePercent()).isNull();
  }

  @Test
  void shouldRejectNonPositivePreviousClose() {
    assertThatThrownBy(() -> tickWithPreviousClose("10.00", "0.00"))
        .isInstanceOf(InvalidTickException.class);
  }

  private static Tick tickWithPreviousClose(String last, String previousClose) {
    return new Tick(
        Ticker.of("PETR4"),
        Money.brl(last),
        null,
        null,
        Money.brl(previousClose),
        100,
        NOW,
        TickSource.LIVE);
  }

  private static Tick tickAt(Instant occurredAt) {
    return new Tick(
        Ticker.of("VALE3"), Money.brl("61.20"), null, null, 0, occurredAt, TickSource.SYNTHETIC);
  }
}

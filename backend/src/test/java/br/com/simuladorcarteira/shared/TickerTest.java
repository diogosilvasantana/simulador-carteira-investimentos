package br.com.simuladorcarteira.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TickerTest {

  @ParameterizedTest
  @ValueSource(strings = {"PETR4", "VALE3", "MXRF11", "BOVA11", "TAEE11", "PETR4F"})
  void shouldAcceptValidB3Tickers(String value) {
    assertThat(Ticker.of(value).value()).isEqualTo(value);
  }

  @Test
  void shouldNormalizeCaseAndWhitespace() {
    assertThat(Ticker.of(" petr4 ")).isEqualTo(Ticker.of("PETR4"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "PET", "PETR", "PETR123", "PE-R4", "PETR4X"})
  void shouldRejectInvalidTickers(String value) {
    assertThatThrownBy(() -> Ticker.of(value)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldIdentifyFractionalMarketAndItsStandardLot() {
    Ticker fractional = Ticker.of("PETR4F");

    assertThat(fractional.isFractional()).isTrue();
    assertThat(fractional.standardLot()).isEqualTo(Ticker.of("PETR4"));
    assertThat(Ticker.of("PETR4").standardLot()).isEqualTo(Ticker.of("PETR4"));
  }
}

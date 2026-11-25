package br.com.simuladorcarteira.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LoginThrottleTest {

  private static final Instant NOW = Instant.parse("2026-11-25T19:00:00Z");
  private static final Duration WINDOW = Duration.ofMinutes(15);

  private final LoginThrottle throttle = new LoginThrottle(3, 5, WINDOW);

  private void fail(String client, int times) {
    for (int i = 0; i < times; i++) {
      throttle.recordFailure(client, NOW);
    }
  }

  @Test
  void shouldAllowLoginBeforeTheLimit() {
    fail("1.2.3.4", 2);

    assertThat(throttle.isBlocked("1.2.3.4", NOW)).isFalse();
  }

  @Test
  void shouldBlockClientThatMissedThePasswordTooManyTimes() {
    fail("1.2.3.4", 3);

    assertThat(throttle.isBlocked("1.2.3.4", NOW)).isTrue();
  }

  @Test
  void shouldReleaseTheClientAfterTheWindow() {
    fail("1.2.3.4", 3);

    assertThat(throttle.isBlocked("1.2.3.4", NOW.plus(WINDOW).plusSeconds(1))).isFalse();
  }

  @Test
  void shouldBlockEveryoneWhenFailuresComeFromManyClients() {
    // Quem troca de IP a cada tentativa esbarra no limite geral.
    for (int i = 0; i < 5; i++) {
      throttle.recordFailure("10.0.0." + i, NOW);
    }

    assertThat(throttle.isBlocked("192.168.0.1", NOW)).isTrue();
  }

  @Test
  void shouldForgetClientFailuresAfterASuccessfulLogin() {
    fail("1.2.3.4", 2);

    throttle.recordSuccess("1.2.3.4");
    fail("1.2.3.4", 2);

    assertThat(throttle.isBlocked("1.2.3.4", NOW)).isFalse();
  }
}

package br.com.simuladorcarteira.identity.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class SecurityPropertiesTest {

  private static final String HASH = "$2b$04$dko8AeOm/OkZb2b0TIQ9Ke20OtfiOTioWlCZmKcJCPEfsSYfFlluq";

  private static SecurityProperties with(String username, String hash) {
    return new SecurityProperties(username, hash, 5, 20, Duration.ofMinutes(15));
  }

  @Test
  void shouldAcceptTheHashAsBcryptPrintsIt() {
    assertThat(with("diogo", HASH).passwordHash()).isEqualTo(HASH);
  }

  @Test
  void shouldAcceptTheHashInBase64SoComposeDoesNotEatTheDollarSigns() {
    String encoded = Base64.getEncoder().encodeToString(HASH.getBytes(StandardCharsets.US_ASCII));

    assertThat(with("diogo", encoded).passwordHash()).isEqualTo(HASH);
  }

  @Test
  void shouldRefuseToStartWithoutAPasswordHash() {
    assertThatThrownBy(() -> with("diogo", "")).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldRefuseAPlainPasswordInPlaceOfTheHash() {
    assertThatThrownBy(() -> with("diogo", "minha-senha"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldRefuseToStartWithoutAUsername() {
    assertThatThrownBy(() -> with(" ", HASH)).isInstanceOf(IllegalStateException.class);
  }
}

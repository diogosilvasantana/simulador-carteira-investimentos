package br.com.simuladorcarteira.identity.adapters.in.web;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Credenciais do único usuário e limites do login. A senha nunca fica aqui: só o hash BCrypt, que
 * vem de variável de ambiente.
 */
@Validated
@ConfigurationProperties("simulador.security")
record SecurityProperties(
    String username,
    String passwordHash,
    @DefaultValue("5") int maxFailuresPerClient,
    @DefaultValue("20") int maxFailuresOverall,
    @DefaultValue("15m") Duration lockout) {

  SecurityProperties {
    // Falha na subida em vez de rodar aberto ou com uma senha que ninguém configurou.
    if (username == null || username.isBlank()) {
      throw new IllegalStateException("Defina APP_USERNAME (simulador.security.username)");
    }
    passwordHash = bcrypt(passwordHash);
  }

  /**
   * Aceita o hash como sai do BCrypt ou em Base64. O Base64 existe porque o Docker Compose trata o
   * cifrão do hash como variável e estragaria o valor no caminho até aqui.
   */
  private static String bcrypt(@Nullable String configured) {
    String value = configured == null ? "" : configured.strip();
    if (!value.isEmpty() && !value.startsWith("$2")) {
      try {
        value = new String(Base64.getDecoder().decode(value), StandardCharsets.US_ASCII);
      } catch (IllegalArgumentException e) {
        value = "";
      }
    }
    if (!value.matches("\\$2[aby]?\\$\\d{2}\\$[./A-Za-z0-9]{53}")) {
      throw new IllegalStateException(
          "Defina APP_PASSWORD_HASH com o hash BCrypt da senha (scripts/gerar-hash-senha.py)");
    }
    return value;
  }
}

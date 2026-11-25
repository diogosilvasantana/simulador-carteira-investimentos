package br.com.simuladorcarteira.identity;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.PostgresTestConfiguration;
import br.com.simuladorcarteira.TestLogin;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

// Limite baixo e contexto próprio: os bloqueios daqui não atrapalham o login dos outros testes.
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "simulador.fixedincome.sync.enabled=false",
      "simulador.security.max-failures-per-client=3"
    })
@Import(PostgresTestConfiguration.class)
class LoginThrottleIntegrationTest {

  @LocalServerPort private int port;

  private int login(String password) {
    RestClient anonymous = RestClient.create("http://localhost:" + port);
    return anonymous
        .post()
        .uri("/api/auth/login")
        .headers(h -> h.addAll(TestLogin.csrf(anonymous)))
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body("username=" + TestLogin.USERNAME + "&password=" + password)
        .exchange((req, response) -> response.getStatusCode().value());
  }

  @Test
  void shouldBlockLoginAfterRepeatedWrongPasswords() {
    assertThat(login("wrong-1")).isEqualTo(401);
    login("wrong-2");
    login("wrong-3");

    // Nem a senha certa entra enquanto o bloqueio durar.
    assertThat(login(TestLogin.PASSWORD)).isEqualTo(429);
  }
}

package br.com.simuladorcarteira.identity;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.PostgresTestConfiguration;
import br.com.simuladorcarteira.TestLogin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "simulador.fixedincome.sync.enabled=false")
@Import(PostgresTestConfiguration.class)
class SecurityIntegrationTest {

  @LocalServerPort private int port;

  private RestClient anonymous;

  @BeforeEach
  void setUp() {
    anonymous = RestClient.create("http://localhost:" + port);
  }

  private int status(RestClient.RequestHeadersSpec<?> request) {
    return request.exchange((req, response) -> response.getStatusCode().value());
  }

  private int login(String password) {
    return status(
        anonymous
            .post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("username=" + TestLogin.USERNAME + "&password=" + password));
  }

  @Test
  void shouldRejectTheApiWithoutASession() {
    assertThat(status(anonymous.get().uri("/api/portfolio"))).isEqualTo(401);
  }

  @Test
  void shouldKeepTheHealthCheckOpenForTheServer() {
    assertThat(status(anonymous.get().uri("/actuator/health/liveness"))).isEqualTo(200);
  }

  @Test
  void shouldAnswerWhoIsLoggedIn() {
    String me = TestLogin.client(port).get().uri("/api/auth/me").retrieve().body(String.class);

    assertThat(me).contains("\"username\":\"tester\"");
  }

  @Test
  void shouldRejectAChangeWithoutTheCsrfToken() {
    HttpHeaders headers = TestLogin.headers(port);
    headers.remove("X-XSRF-TOKEN");

    int status =
        status(
            anonymous
                .post()
                .uri("/api/portfolio/contributions")
                .headers(h -> h.addAll(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"amount\":\"100.00\"}"));

    assertThat(status).isEqualTo(403);
  }

  @Test
  void shouldEndTheSessionOnLogout() {
    HttpHeaders headers = TestLogin.headers(port);

    anonymous
        .post()
        .uri("/api/auth/logout")
        .headers(h -> h.addAll(headers))
        .retrieve()
        .toBodilessEntity();

    assertThat(status(anonymous.get().uri("/api/auth/me").headers(h -> h.addAll(headers))))
        .isEqualTo(401);
  }
}

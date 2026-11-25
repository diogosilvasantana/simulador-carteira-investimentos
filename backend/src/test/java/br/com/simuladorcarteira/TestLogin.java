package br.com.simuladorcarteira;

import java.net.HttpCookie;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/** Faz o login dos testes de API e devolve um cliente com o cookie de sessão e o token CSRF. */
public final class TestLogin {

  public static final String USERNAME = "tester";
  public static final String PASSWORD = "test-password";

  private TestLogin() {}

  /** Cabeçalhos de um navegador logado: cookies de sessão e CSRF, mais o token no cabeçalho. */
  public static HttpHeaders headers(int port) {
    RestClient anonymous = RestClient.create("http://localhost:" + port);
    ResponseEntity<Void> login =
        anonymous
            .post()
            .uri("/api/auth/login")
            .headers(h -> h.addAll(csrf(anonymous)))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("username=" + USERNAME + "&password=" + PASSWORD)
            .retrieve()
            .toBodilessEntity();
    String session = cookie(login, "JSESSIONID");
    ResponseEntity<String> me =
        anonymous
            .get()
            .uri("/api/auth/me")
            .header(HttpHeaders.COOKIE, "JSESSIONID=" + session)
            .retrieve()
            .toEntity(String.class);
    String csrf = cookie(me, "XSRF-TOKEN");

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, "JSESSIONID=" + session + "; XSRF-TOKEN=" + csrf);
    headers.add("X-XSRF-TOKEN", csrf);
    return headers;
  }

  /** Como o navegador antes do login: a primeira resposta, mesmo um 401, já traz o token CSRF. */
  public static HttpHeaders csrf(RestClient anonymous) {
    ResponseEntity<Void> first =
        anonymous
            .get()
            .uri("/api/auth/me")
            .exchange(
                (request, response) ->
                    ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .<Void>build());
    String token = cookie(first, "XSRF-TOKEN");
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, "XSRF-TOKEN=" + token);
    headers.add("X-XSRF-TOKEN", token);
    return headers;
  }

  public static RestClient client(int port) {
    HttpHeaders headers = headers(port);
    return RestClient.builder()
        .baseUrl("http://localhost:" + port)
        .defaultHeaders(h -> h.addAll(headers))
        .build();
  }

  private static String cookie(ResponseEntity<?> response, String name) {
    List<String> setCookies =
        Objects.requireNonNullElse(response.getHeaders().get(HttpHeaders.SET_COOKIE), List.of());
    return setCookies.stream()
        .flatMap(header -> HttpCookie.parse(header).stream())
        .filter(c -> c.getName().equals(name))
        .map(HttpCookie::getValue)
        .reduce((first, last) -> last)
        .orElseThrow(() -> new IllegalStateException("Resposta sem o cookie " + name));
  }
}

package br.com.simuladorcarteira.portfolio;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.PostgresTestConfiguration;
import br.com.simuladorcarteira.marketdata.application.LatestQuoteRepository;
import br.com.simuladorcarteira.marketdata.domain.Tick;
import br.com.simuladorcarteira.marketdata.domain.TickSource;
import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "simulador.fixedincome.sync.enabled=false")
@Import(PostgresTestConfiguration.class)
class PortfolioApiIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private LatestQuoteRepository quotes;

  private RestClient client;

  @BeforeEach
  void setUp() {
    client = RestClient.create("http://localhost:" + port);
    client.delete().uri("/api/portfolio").retrieve().toBodilessEntity();
    quotes.save(
        new Tick(
            new Ticker("PETR4"),
            Money.brl("38.00"),
            null,
            null,
            Money.brl("37.50"),
            1000,
            Instant.now(),
            TickSource.LIVE));
  }

  @Test
  void shouldBuyAtTheLastPriceAfterAContribution() {
    post("/api/portfolio/contributions", "{\"amount\":\"10000.00\"}");

    String execution =
        post("/api/portfolio/orders", "{\"ticker\":\"PETR4\",\"side\":\"BUY\",\"quantity\":100}");
    String summary = client.get().uri("/api/portfolio").retrieve().body(String.class);

    assertThat(execution).contains("\"price\":\"38.00\"");
    assertThat(summary).contains("\"ticker\":\"PETR4\"").contains("\"quantity\":100");
  }

  @Test
  void shouldAnswerUnprocessableWhenCashIsShort() {
    HttpStatusCode status =
        client
            .post()
            .uri("/api/portfolio/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"ticker\":\"PETR4\",\"side\":\"BUY\",\"quantity\":100}")
            .exchange((request, response) -> response.getStatusCode());

    assertThat(status.value()).isEqualTo(422);
  }

  private @Nullable String post(String uri, String body) {
    return client
        .post()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(String.class);
  }
}

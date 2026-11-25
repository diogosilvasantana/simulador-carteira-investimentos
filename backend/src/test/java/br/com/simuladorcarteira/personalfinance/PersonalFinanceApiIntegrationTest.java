package br.com.simuladorcarteira.personalfinance;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.PostgresTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.client.RestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "simulador.fixedincome.sync.enabled=false",
      "simulador.personal-finance.enabled=true"
    })
@Import(PostgresTestConfiguration.class)
class PersonalFinanceApiIntegrationTest {

  @LocalServerPort private int port;

  @BeforeEach
  void clean(@Autowired JdbcClient jdbc) {
    jdbc.sql("delete from debt").update();
    jdbc.sql("delete from budget_item").update();
  }

  @Test
  void shouldPersistItemsAndDebtsAndShowThemInTheSnapshot() {
    post("/budget-items", "{\"name\":\"Salário\",\"kind\":\"INCOME\",\"amount\":\"5000.10\"}");
    post(
        "/debts",
        """
        {"creditor":"Banco A","balance":"900.00","monthlyRatePercent":"2.5",\
        "installment":"100.05","overdue":false,"collateral":false}\
        """);

    assertThat(get())
        .contains("\"available\":\"4900.05\"")
        .contains("\"stage\":\"PAY_DEBTS\"")
        .contains("\"monthlyRatePercent\":\"2.5000\"");
  }

  @Test
  void shouldRemoveADebt() {
    post(
        "/debts",
        "{\"creditor\":\"Banco A\",\"balance\":\"1\",\"overdue\":true,\"collateral\":false}");
    String id = get().replaceAll("(?s).*\"debts\":\\[\\{\"id\":(\\d+).*", "$1");

    client().delete().uri("/debts/" + id).retrieve().toBodilessEntity();

    assertThat(get()).contains("\"debts\":[]");
  }

  @Test
  void shouldAnswer404WhenRemovingWhatDoesNotExist() {
    HttpStatusCode status =
        client().delete().uri("/debts/999999").exchange((q, r) -> r.getStatusCode());

    assertThat(status.value()).isEqualTo(404);
  }

  @Test
  void shouldRejectANegativeAmount() {
    HttpStatusCode status =
        client()
            .post()
            .uri("/budget-items")
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"name\":\"X\",\"kind\":\"OTHER\",\"amount\":\"-1\"}")
            .exchange((q, r) -> r.getStatusCode());

    assertThat(status.value()).isEqualTo(400);
  }

  private RestClient client() {
    return RestClient.create("http://localhost:" + port + "/api/personal-finance");
  }

  private String get() {
    return String.valueOf(client().get().retrieve().body(String.class));
  }

  private void post(String path, String json) {
    client()
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(json)
        .retrieve()
        .toBodilessEntity();
  }
}

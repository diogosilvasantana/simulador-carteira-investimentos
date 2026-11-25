package br.com.simuladorcarteira.fixedincome;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.fixedincome.application.IndexRateRepository;
import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import br.com.simuladorcarteira.fixedincome.domain.RateIndex;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "simulador.fixedincome.sync.enabled=false")
class FixedIncomeApiIntegrationTest {

  private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);

  @LocalServerPort private int port;

  @Autowired private IndexRateRepository rates;

  @Test
  void shouldSimulateACdiLinkedInvestmentWithTaxesFromConfiguration() {
    rates.saveAll(
        List.of(
            new IndexRate(RateIndex.CDI, MONDAY, new BigDecimal("0.055131")),
            new IndexRate(RateIndex.CDI, MONDAY.plusDays(1), new BigDecimal("0.055131"))));

    String body =
        RestClient.create("http://localhost:" + port)
            .post()
            .uri("/api/fixed-income/simulations")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {"principal":"10000.00","indexPercentage":"100","start":"2026-11-02","end":"2026-11-04","incomeTaxExempt":false}
                """)
            .retrieve()
            .body(String.class);

    // R$ 11,03 de rendimento em 2 dias: IOF de 93% (2 dias) e IR de 22,5% sobre o resto.
    assertThat(body)
        .contains("\"gross\":\"10011.03\"")
        .contains("\"iofRatePercent\":\"93\"")
        .contains("\"incomeTaxRatePercent\":\"22.5\"");
  }

  @Test
  void shouldListTheLatestValueOfEachIndex() {
    rates.saveAll(List.of(new IndexRate(RateIndex.SELIC_TARGET, MONDAY, new BigDecimal("14.25"))));

    String body =
        RestClient.create("http://localhost:" + port)
            .get()
            .uri("/api/fixed-income/indexes")
            .retrieve()
            .body(String.class);

    assertThat(body).contains("\"index\":\"SELIC_TARGET\"").contains("\"unit\":\"% ao ano\"");
  }
}

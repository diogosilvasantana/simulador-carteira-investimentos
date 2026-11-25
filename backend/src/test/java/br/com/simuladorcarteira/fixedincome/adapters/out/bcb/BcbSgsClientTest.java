package br.com.simuladorcarteira.fixedincome.adapters.out.bcb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import br.com.simuladorcarteira.fixedincome.domain.RateIndex;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class BcbSgsClientTest {

  private static final String BASE = "https://bcb.test";

  private final RestClient.Builder builder = RestClient.builder();
  private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
  private final BcbSgsClient client = new BcbSgsClient(builder, new BcbProperties(BASE));

  @Test
  void shouldReadCdiSeriesWithBrazilianDates() {
    server
        .expect(
            requestTo(
                BASE
                    + "/dados/serie/bcdata.sgs.12/dados?formato=json&dataInicial=23/11/2026&dataFinal=24/11/2026"))
        .andRespond(
            withSuccess(
                """
                [{"data":"23/11/2026","valor":"0.055131"},{"data":"24/11/2026","valor":"0.055131"}]
                """,
                MediaType.APPLICATION_JSON));

    List<IndexRate> rates =
        client.fetch(RateIndex.CDI, LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 24));

    assertThat(rates)
        .extracting(IndexRate::date)
        .containsExactly(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 24));
    assertThat(rates.getFirst().percent()).isEqualByComparingTo("0.055131");
    server.verify();
  }

  @Test
  void shouldTreatNotFoundAsAPeriodWithoutValues() {
    server
        .expect(
            requestTo(
                BASE
                    + "/dados/serie/bcdata.sgs.432/dados?formato=json&dataInicial=21/11/2026&dataFinal=22/11/2026"))
        .andRespond(withResourceNotFound());

    List<IndexRate> rates =
        client.fetch(
            RateIndex.SELIC_TARGET, LocalDate.of(2026, 11, 21), LocalDate.of(2026, 11, 22));

    assertThat(rates).isEmpty();
  }

  @Test
  void shouldSplitLongPeriodsIntoWindowsTheApiAccepts() {
    String empty = "[]";
    server
        .expect(
            requestTo(
                BASE
                    + "/dados/serie/bcdata.sgs.433/dados?formato=json&dataInicial=01/01/2015&dataFinal=31/12/2019"))
        .andRespond(withSuccess(empty, MediaType.APPLICATION_JSON));
    server
        .expect(
            requestTo(
                BASE
                    + "/dados/serie/bcdata.sgs.433/dados?formato=json&dataInicial=01/01/2020&dataFinal=24/11/2024"))
        .andRespond(withSuccess(empty, MediaType.APPLICATION_JSON));

    client.fetch(RateIndex.IPCA, LocalDate.of(2015, 1, 1), LocalDate.of(2024, 11, 24));

    server.verify();
  }
}

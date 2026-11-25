package br.com.simuladorcarteira.fixedincome.adapters.out.bcb;

import br.com.simuladorcarteira.fixedincome.application.IndexRateSource;
import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import br.com.simuladorcarteira.fixedincome.domain.RateIndex;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Lê as séries do SGS do Banco Central. A API recusa janelas longas (o limite documentado é de 10
 * anos), então períodos grandes são buscados em partes.
 */
class BcbSgsClient implements IndexRateSource {

  static final Map<RateIndex, Integer> SERIES =
      Map.of(RateIndex.CDI, 12, RateIndex.SELIC_TARGET, 432, RateIndex.IPCA, 433);
  static final int WINDOW_YEARS = 5;
  private static final DateTimeFormatter BCB_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final ParameterizedTypeReference<List<SgsPoint>> POINTS =
      new ParameterizedTypeReference<>() {};

  private final RestClient http;
  private final String baseUrl;

  BcbSgsClient(RestClient.Builder builder, BcbProperties properties) {
    this.http = builder.build();
    this.baseUrl = properties.baseUrl();
  }

  @Override
  public List<IndexRate> fetch(RateIndex index, LocalDate from, LocalDate to) {
    List<IndexRate> rates = new ArrayList<>();
    for (LocalDate start = from; !start.isAfter(to); start = start.plusYears(WINDOW_YEARS)) {
      LocalDate windowEnd = start.plusYears(WINDOW_YEARS).minusDays(1);
      LocalDate end = windowEnd.isAfter(to) ? to : windowEnd;
      for (SgsPoint point : window(SERIES.get(index), start, end)) {
        rates.add(
            new IndexRate(
                index, LocalDate.parse(point.data(), BCB_DATE), new BigDecimal(point.valor())));
      }
    }
    return rates;
  }

  private List<SgsPoint> window(@Nullable Integer series, LocalDate from, LocalDate to) {
    try {
      List<SgsPoint> points =
          http.get()
              // URL montada à mão: com variáveis de template, o Spring codificaria as barras das
              // datas (%2F).
              .uri(
                  URI.create(
                      baseUrl
                          + "/dados/serie/bcdata.sgs."
                          + series
                          + "/dados?formato=json&dataInicial="
                          + BCB_DATE.format(from)
                          + "&dataFinal="
                          + BCB_DATE.format(to)))
              .retrieve()
              .body(POINTS);
      return points == null ? List.of() : points;
    } catch (HttpClientErrorException.NotFound e) {
      // O SGS responde 404 quando não há valor no período (ex.: só fim de semana).
      return List.of();
    }
  }

  /** Um ponto da série como o SGS devolve: {@code {"data":"24/11/2026","valor":"0.055131"}}. */
  record SgsPoint(String data, String valor) {}
}

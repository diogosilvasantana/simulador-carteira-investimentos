package br.com.simuladorcarteira.marketdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.simuladorcarteira.PostgresTestConfiguration;
import br.com.simuladorcarteira.TestLogin;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/** Caminho completo: a ponte envia um tick em /ingest e o navegador o recebe por STOMP. */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "simulador.ingest.token=test-token")
@Import(PostgresTestConfiguration.class)
class RealtimeQuotesIntegrationTest {

  private static final String TICK =
      """
      {"type":"tick","ticker":"PETR4","last":"38.47","bid":"38.46","ask":"38.48",\
      "previous_close":"37.00","volume":1200,"occurred_at":"2026-11-24T13:15:00+00:00","source":"LIVE"}\
      """;

  private static final String ASSET =
      """
      {"type":"asset","ticker":"PETR4","description":"PETROBRAS   PN      N2",\
      "path":"BOVESPA\\\\A VISTA\\\\PETR4"}\
      """;

  @LocalServerPort private int port;

  private @Nullable WebSocketSession ingestSession;
  private @Nullable StompSession stompSession;

  @AfterEach
  void closeSessions() throws Exception {
    if (ingestSession != null) {
      ingestSession.close();
    }
    if (stompSession != null) {
      stompSession.disconnect();
    }
  }

  @Test
  void shouldDeliverIngestedTickToStompSubscribers() throws Exception {
    BlockingQueue<Map<String, Object>> received = new LinkedBlockingQueue<>();
    StompSession stomp = connectStomp();
    stomp.subscribe("/topic/quotes/PETR4", new QueueFrameHandler(received));
    Thread.sleep(200);

    WebSocketSession ingest = connectIngest("test-token");
    ingest.sendMessage(new TextMessage(TICK));

    Map<String, Object> quote =
        Objects.requireNonNull(received.poll(5, TimeUnit.SECONDS), "nenhuma cotação recebida");
    assertThat(quote).containsEntry("ticker", "PETR4").containsEntry("source", "LIVE");
    // Texto, não número: o navegador não pode converter preço para ponto flutuante.
    assertThat(quote).containsEntry("last", "38.47").containsEntry("bid", "38.46");
    assertThat(quote)
        .containsEntry("previousClose", "37.00")
        .containsEntry("changePercent", "3.97");
  }

  @Test
  void shouldGroupQuotesByCategoryWithCatalogData() throws Exception {
    BlockingQueue<List<Map<String, Object>>> received = new LinkedBlockingQueue<>();
    StompSession stomp = connectStomp();
    stomp.subscribe("/topic/quotes/category/acoes", new BatchFrameHandler(received));
    Thread.sleep(200);

    WebSocketSession ingest = connectIngest("test-token");
    ingest.sendMessage(new TextMessage(ASSET));
    ingest.sendMessage(new TextMessage(TICK));

    List<Map<String, Object>> batch =
        Objects.requireNonNull(received.poll(5, TimeUnit.SECONDS), "nenhum lote recebido");
    assertThat(batch)
        .singleElement()
        .satisfies(
            quote ->
                assertThat(quote)
                    .containsEntry("ticker", "PETR4")
                    .containsEntry("name", "PETROBRAS")
                    .containsEntry("type", "STOCK")
                    .containsEntry("category", "acoes")
                    .containsEntry("listingSegment", "LEVEL_2"));
    String assets =
        TestLogin.client(port)
            .get()
            .uri("/api/assets?category=acoes")
            .retrieve()
            .body(String.class);
    assertThat(assets).contains("\"ticker\":\"PETR4\"").contains("\"last\":\"38.47\"");
  }

  @Test
  void shouldRejectIngestWithWrongToken() {
    assertThatThrownBy(() -> connectIngest("wrong-token")).isInstanceOf(ExecutionException.class);
  }

  private WebSocketSession connectIngest(String token) throws Exception {
    WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    WebSocketSession session =
        new StandardWebSocketClient()
            .execute(
                new TextWebSocketHandler(),
                headers,
                URI.create("ws://localhost:" + port + "/ingest"))
            .get(5, TimeUnit.SECONDS);
    ingestSession = session;
    return session;
  }

  private StompSession connectStomp() throws Exception {
    WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
    client.setMessageConverter(new JacksonJsonMessageConverter());
    StompSession session =
        client
            .connectAsync(
                "ws://localhost:" + port + "/ws",
                new WebSocketHttpHeaders(TestLogin.headers(port)),
                new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);
    stompSession = session;
    return session;
  }

  private record BatchFrameHandler(BlockingQueue<List<Map<String, Object>>> queue)
      implements StompFrameHandler {

    @Override
    public Type getPayloadType(StompHeaders headers) {
      return List.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleFrame(StompHeaders headers, @Nullable Object payload) {
      if (payload instanceof List<?> list) {
        queue.add((List<Map<String, Object>>) list);
      }
    }
  }

  private record QueueFrameHandler(BlockingQueue<Map<String, Object>> queue)
      implements StompFrameHandler {

    @Override
    public Type getPayloadType(StompHeaders headers) {
      return Map.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleFrame(StompHeaders headers, @Nullable Object payload) {
      if (payload instanceof Map<?, ?> map) {
        queue.add((Map<String, Object>) map);
      }
    }
  }
}

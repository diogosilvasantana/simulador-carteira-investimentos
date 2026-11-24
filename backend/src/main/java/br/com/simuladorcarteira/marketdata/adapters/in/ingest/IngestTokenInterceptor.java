package br.com.simuladorcarteira.marketdata.adapters.in.ingest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/** Só a ponte com o token certo abre a conexão de ingestão (ADR-003). */
class IngestTokenInterceptor implements HandshakeInterceptor {

  private static final Logger log = LoggerFactory.getLogger(IngestTokenInterceptor.class);
  private static final String BEARER = "Bearer ";

  private final byte[] expectedToken;

  IngestTokenInterceptor(String expectedToken) {
    this.expectedToken = expectedToken.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public boolean beforeHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes) {
    String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (expectedToken.length > 0
        && header != null
        && header.startsWith(BEARER)
        && MessageDigest.isEqual(
            expectedToken, header.substring(BEARER.length()).getBytes(StandardCharsets.UTF_8))) {
      return true;
    }
    log.warn("Conexão de ingestão recusada de {}", request.getRemoteAddress());
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return false;
  }

  @Override
  public void afterHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      @Nullable Exception exception) {}
}

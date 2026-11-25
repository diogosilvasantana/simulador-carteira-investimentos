package br.com.simuladorcarteira.identity.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Freio contra quem tenta adivinhar a senha: um cliente que erra demais numa janela fica
 * bloqueado até ela passar. Como o IP pode ser trocado a cada tentativa, existe também um limite
 * geral, que bloqueia todo mundo; para um sistema de um usuário só, esperar alguns minutos é um
 * preço pequeno.
 */
public final class LoginThrottle {

  private final int maxPerClient;
  private final int maxOverall;
  private final Duration window;
  private final Map<String, Deque<Instant>> failures = new HashMap<>();
  private final Deque<Instant> allFailures = new ArrayDeque<>();

  public LoginThrottle(int maxPerClient, int maxOverall, Duration window) {
    if (maxPerClient <= 0 || maxOverall <= 0) {
      throw new IllegalArgumentException("Os limites de tentativas precisam ser positivos");
    }
    this.maxPerClient = maxPerClient;
    this.maxOverall = maxOverall;
    this.window = Objects.requireNonNull(window, "window");
  }

  public synchronized boolean isBlocked(String client, Instant now) {
    prune(allFailures, now);
    Deque<Instant> mine = failures.get(client);
    if (mine != null) {
      prune(mine, now);
    }
    return allFailures.size() >= maxOverall || (mine != null && mine.size() >= maxPerClient);
  }

  public synchronized void recordFailure(String client, Instant now) {
    failures.computeIfAbsent(client, c -> new ArrayDeque<>()).addLast(now);
    allFailures.addLast(now);
    // Sem isso, IPs que erraram uma vez ficariam na memória para sempre.
    failures.values().forEach(list -> prune(list, now));
    failures.values().removeIf(Deque::isEmpty);
  }

  public synchronized void recordSuccess(String client) {
    failures.remove(client);
  }

  private void prune(Deque<Instant> list, Instant now) {
    Instant oldest = now.minus(window);
    while (!list.isEmpty() && !list.peekFirst().isAfter(oldest)) {
      list.pollFirst();
    }
  }
}

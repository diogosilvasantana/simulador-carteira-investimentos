package br.com.simuladorcarteira.identity.adapters.in.web;

import br.com.simuladorcarteira.identity.domain.LoginThrottle;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/** Recusa a tentativa de login, sem nem conferir a senha, enquanto o cliente estiver bloqueado. */
class LoginThrottleFilter extends OncePerRequestFilter {

  static final String LOGIN_PATH = "/api/auth/login";

  private final LoginThrottle throttle;
  private final Clock clock;

  LoginThrottleFilter(LoginThrottle throttle, Clock clock) {
    this.throttle = throttle;
    this.clock = clock;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !("POST".equals(request.getMethod()) && LOGIN_PATH.equals(request.getRequestURI()));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (throttle.isBlocked(request.getRemoteAddr(), clock.instant())) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setHeader("Retry-After", "900");
      return;
    }
    chain.doFilter(request, response);
  }
}

package br.com.simuladorcarteira.portfolio.adapters.in.web;

import br.com.simuladorcarteira.portfolio.application.PortfolioErrors;
import br.com.simuladorcarteira.portfolio.domain.InsufficientCashException;
import br.com.simuladorcarteira.portfolio.domain.InsufficientPositionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Erros da carteira como Problem Details (RFC 9457), com título em português para a tela. */
@RestControllerAdvice(assignableTypes = PortfolioController.class)
class PortfolioProblems {

  @ExceptionHandler(InsufficientCashException.class)
  ProblemDetail cash(InsufficientCashException e) {
    ProblemDetail problem = problem(HttpStatus.UNPROCESSABLE_CONTENT, "Caixa insuficiente", e);
    problem.setProperty("required", e.required().amount().toPlainString());
    problem.setProperty("available", e.available().amount().toPlainString());
    return problem;
  }

  @ExceptionHandler(InsufficientPositionException.class)
  ProblemDetail position(InsufficientPositionException e) {
    return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Quantidade acima da posição", e);
  }

  @ExceptionHandler(PortfolioErrors.NoQuoteException.class)
  ProblemDetail noQuote(PortfolioErrors.NoQuoteException e) {
    return problem(HttpStatus.CONFLICT, "Ativo sem cotação", e);
  }

  @ExceptionHandler(PortfolioErrors.UnknownProductException.class)
  ProblemDetail unknown(PortfolioErrors.UnknownProductException e) {
    return problem(HttpStatus.NOT_FOUND, "Ativo ou produto desconhecido", e);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ProblemDetail invalid(IllegalArgumentException e) {
    return problem(HttpStatus.BAD_REQUEST, "Pedido inválido", e);
  }

  private static ProblemDetail problem(HttpStatus status, String title, RuntimeException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
    problem.setTitle(title);
    return problem;
  }
}

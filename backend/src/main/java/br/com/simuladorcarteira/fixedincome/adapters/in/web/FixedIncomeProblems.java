package br.com.simuladorcarteira.fixedincome.adapters.in.web;

import br.com.simuladorcarteira.fixedincome.domain.MissingIndexRatesException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Erros da renda fixa como Problem Details (RFC 9457). */
@RestControllerAdvice(assignableTypes = FixedIncomeController.class)
class FixedIncomeProblems {

  @ExceptionHandler(MissingIndexRatesException.class)
  ProblemDetail missingRates(MissingIndexRatesException e) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    problem.setTitle("Indexador ainda não disponível");
    return problem;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ProblemDetail invalid(IllegalArgumentException e) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    problem.setTitle("Simulação inválida");
    return problem;
  }
}

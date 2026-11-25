package br.com.simuladorcarteira.portfolio.adapters.in.web;

import br.com.simuladorcarteira.portfolio.adapters.in.web.Messages.ExecutionMessage;
import br.com.simuladorcarteira.portfolio.adapters.in.web.Messages.FixedIncomeMessage;
import br.com.simuladorcarteira.portfolio.adapters.in.web.Messages.QuoteMessage;
import br.com.simuladorcarteira.portfolio.adapters.in.web.Messages.SummaryMessage;
import br.com.simuladorcarteira.portfolio.adapters.in.web.Requests.ContributionRequest;
import br.com.simuladorcarteira.portfolio.adapters.in.web.Requests.FixedIncomeRequest;
import br.com.simuladorcarteira.portfolio.adapters.in.web.Requests.OrderRequest;
import br.com.simuladorcarteira.portfolio.application.ManagePortfolio;
import br.com.simuladorcarteira.portfolio.application.PortfolioSummary;
import br.com.simuladorcarteira.portfolio.application.ViewPortfolio;
import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** A carteira simulada: resumo, aportes, ordens a mercado e renda fixa. */
@RestController
@RequestMapping("/api/portfolio")
class PortfolioController {

  private final ViewPortfolio view;
  private final ManagePortfolio manage;

  PortfolioController(ViewPortfolio view, ManagePortfolio manage) {
    this.view = view;
    this.manage = manage;
  }

  @GetMapping
  SummaryMessage summary() {
    PortfolioSummary summary = view.summary();
    return SummaryMessage.from(summary);
  }

  @PostMapping("/contributions")
  @ResponseStatus(HttpStatus.CREATED)
  SummaryMessage contribute(@Valid @RequestBody ContributionRequest request) {
    manage.contribute(Money.brl(Objects.requireNonNull(request.amount())));
    return SummaryMessage.from(view.summary());
  }

  @PostMapping("/orders/preview")
  QuoteMessage preview(@Valid @RequestBody OrderRequest request) {
    return QuoteMessage.from(
        manage.preview(
            Objects.requireNonNull(request.side()), ticker(request), request.quantity()));
  }

  @PostMapping("/orders")
  @ResponseStatus(HttpStatus.CREATED)
  ExecutionMessage order(@Valid @RequestBody OrderRequest request) {
    return ExecutionMessage.from(
        manage.execute(
            Objects.requireNonNull(request.side()), ticker(request), request.quantity()));
  }

  @PostMapping("/fixed-income")
  @ResponseStatus(HttpStatus.CREATED)
  FixedIncomeMessage investFixedIncome(@Valid @RequestBody FixedIncomeRequest request) {
    var holding =
        manage.investFixedIncome(
            Objects.requireNonNull(request.productId()),
            Money.brl(Objects.requireNonNull(request.amount())));
    return view.summary().fixedIncome().stream()
        .filter(f -> f.id() == holding.id())
        .findFirst()
        .map(FixedIncomeMessage::from)
        .orElseThrow();
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void reset() {
    manage.reset();
  }

  private static Ticker ticker(OrderRequest request) {
    return new Ticker(
        Objects.requireNonNull(request.ticker()).trim().toUpperCase(java.util.Locale.ROOT));
  }
}

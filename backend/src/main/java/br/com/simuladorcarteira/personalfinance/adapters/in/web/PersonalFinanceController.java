package br.com.simuladorcarteira.personalfinance.adapters.in.web;

import br.com.simuladorcarteira.personalfinance.application.EntryNotFoundException;
import br.com.simuladorcarteira.personalfinance.application.ManagePersonalFinance;
import br.com.simuladorcarteira.personalfinance.domain.BudgetItem;
import br.com.simuladorcarteira.personalfinance.domain.Debt;
import br.com.simuladorcarteira.personalfinance.domain.FinancialSnapshot;
import br.com.simuladorcarteira.shared.Money;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Finanças pessoais. Só existe com a propriedade ligada: sem login, não pode ir para a internet. */
@RestController
@RequestMapping("/api/personal-finance")
@ConditionalOnProperty("simulador.personal-finance.enabled")
class PersonalFinanceController {

  private final ManagePersonalFinance manage;

  PersonalFinanceController(ManagePersonalFinance manage) {
    this.manage = manage;
  }

  @GetMapping
  Messages.SnapshotMessage snapshot() {
    var items = manage.items();
    return Messages.SnapshotMessage.from(FinancialSnapshot.of(items, manage.debts()), items);
  }

  @PostMapping("/budget-items")
  @ResponseStatus(HttpStatus.CREATED)
  void add(@Valid @RequestBody Messages.ItemRequest request) {
    manage.add(
        new BudgetItem(
            0,
            Objects.requireNonNull(request.name()).trim(),
            Objects.requireNonNull(request.kind()),
            Money.brl(Objects.requireNonNull(request.amount()))));
  }

  @PostMapping("/debts")
  @ResponseStatus(HttpStatus.CREATED)
  void add(@Valid @RequestBody Messages.DebtRequest request) {
    BigDecimal installment = request.installment();
    manage.add(
        new Debt(
            0,
            Objects.requireNonNull(request.creditor()).trim(),
            Money.brl(Objects.requireNonNull(request.balance())),
            request.monthlyRatePercent(),
            installment == null ? null : Money.brl(installment),
            request.overdue(),
            request.collateral()));
  }

  @DeleteMapping("/budget-items/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void removeItem(@PathVariable("id") long id) {
    manage.removeItem(id);
  }

  @DeleteMapping("/debts/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void removeDebt(@PathVariable("id") long id) {
    manage.removeDebt(id);
  }

  @ExceptionHandler
  ProblemDetail notFound(EntryNotFoundException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problem.setTitle("Registro não encontrado");
    return problem;
  }
}

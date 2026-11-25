package br.com.simuladorcarteira.personalfinance.application;

import br.com.simuladorcarteira.personalfinance.domain.BudgetItem;
import br.com.simuladorcarteira.personalfinance.domain.Debt;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Casos de uso do orçamento e das dívidas. */
@Service
@ConditionalOnProperty("simulador.personal-finance.enabled")
public class ManagePersonalFinance {

  private final PersonalFinanceRepository repository;

  public ManagePersonalFinance(PersonalFinanceRepository repository) {
    this.repository = repository;
  }

  public List<BudgetItem> items() {
    return repository.items();
  }

  public List<Debt> debts() {
    return repository.debts();
  }

  public void add(BudgetItem item) {
    repository.add(item);
  }

  public void add(Debt debt) {
    repository.add(debt);
  }

  public void removeItem(long id) {
    if (!repository.removeItem(id)) {
      throw new EntryNotFoundException(id);
    }
  }

  public void removeDebt(long id) {
    if (!repository.removeDebt(id)) {
      throw new EntryNotFoundException(id);
    }
  }
}

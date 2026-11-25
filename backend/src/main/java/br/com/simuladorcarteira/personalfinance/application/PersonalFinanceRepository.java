package br.com.simuladorcarteira.personalfinance.application;

import br.com.simuladorcarteira.personalfinance.domain.BudgetItem;
import br.com.simuladorcarteira.personalfinance.domain.Debt;
import java.util.List;

/** Porta de saída. As remoções dizem se havia o que remover. */
public interface PersonalFinanceRepository {

  List<BudgetItem> items();

  List<Debt> debts();

  void add(BudgetItem item);

  void add(Debt debt);

  boolean removeItem(long id);

  boolean removeDebt(long id);
}

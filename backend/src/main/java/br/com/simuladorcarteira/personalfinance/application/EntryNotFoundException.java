package br.com.simuladorcarteira.personalfinance.application;

/** O item ou a dívida não existe mais. */
public class EntryNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public EntryNotFoundException(long id) {
    super("Registro não encontrado: " + id);
  }
}

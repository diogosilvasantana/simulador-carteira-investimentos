package br.com.simuladorcarteira.portfolio.application;

import br.com.simuladorcarteira.shared.Ticker;

/** Erros dos casos de uso que não são regra de dinheiro do domínio. */
public final class PortfolioErrors {

  private PortfolioErrors() {}

  /** O ativo ainda não tem cotação: sem preço, não há como executar a mercado. */
  public static class NoQuoteException extends RuntimeException {
    public NoQuoteException(Ticker ticker) {
      super(
          "Sem cotação para %s: a ordem a mercado precisa de um último preço"
              .formatted(ticker.value()));
    }
  }

  /** O ativo não está no catálogo do mercado nem é um produto de renda fixa. */
  public static class UnknownProductException extends RuntimeException {
    public UnknownProductException(String id) {
      super("Produto desconhecido: " + id);
    }
  }
}

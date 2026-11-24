package br.com.simuladorcarteira.marketdata.application;

import br.com.simuladorcarteira.marketdata.domain.Tick;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/** Caso de uso: fotografia das cotações atuais, para a tela carregar antes do primeiro tick. */
@Service
public class ListLatestQuotes {

  private final LatestQuoteRepository quotes;

  public ListLatestQuotes(LatestQuoteRepository quotes) {
    this.quotes = quotes;
  }

  public List<Tick> all() {
    return quotes.findAll().stream()
        .sorted(Comparator.comparing(tick -> tick.ticker().value()))
        .toList();
  }
}

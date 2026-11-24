package br.com.simuladorcarteira.marketdata.adapters.in.web;

import br.com.simuladorcarteira.marketdata.application.ListLatestQuotes;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quotes")
class QuoteController {

  private final ListLatestQuotes listLatestQuotes;

  QuoteController(ListLatestQuotes listLatestQuotes) {
    this.listLatestQuotes = listLatestQuotes;
  }

  @GetMapping
  List<QuoteMessage> latest() {
    return listLatestQuotes.all().stream().map(QuoteMessage::from).toList();
  }
}

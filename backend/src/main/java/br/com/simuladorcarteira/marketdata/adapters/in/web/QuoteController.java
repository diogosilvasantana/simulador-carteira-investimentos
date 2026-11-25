package br.com.simuladorcarteira.marketdata.adapters.in.web;

import br.com.simuladorcarteira.marketdata.application.ListMarket;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quotes")
class QuoteController {

  private final ListMarket listMarket;

  QuoteController(ListMarket listMarket) {
    this.listMarket = listMarket;
  }

  @GetMapping
  List<QuoteMessage> latest() {
    return listMarket.quotes().stream().map(QuoteMessage::from).toList();
  }
}

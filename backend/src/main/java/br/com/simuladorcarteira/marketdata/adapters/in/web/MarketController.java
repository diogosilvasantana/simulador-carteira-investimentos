package br.com.simuladorcarteira.marketdata.adapters.in.web;

import br.com.simuladorcarteira.marketdata.application.ListMarket;
import br.com.simuladorcarteira.marketdata.domain.AssetCategory;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Catálogo por categoria: a tela carrega só a aba aberta (ADR-010). */
@RestController
@RequestMapping("/api")
class MarketController {

  private final ListMarket listMarket;

  MarketController(ListMarket listMarket) {
    this.listMarket = listMarket;
  }

  @GetMapping("/categories")
  List<CategoryMessage> categories() {
    return listMarket.countByCategory().entrySet().stream()
        .map(entry -> new CategoryMessage(entry.getKey().slug(), entry.getValue()))
        .toList();
  }

  @GetMapping("/assets")
  List<QuoteMessage> assets(@RequestParam("category") String category) {
    AssetCategory chosen =
        AssetCategory.fromSlug(category)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Categoria desconhecida: " + category));
    return listMarket.byCategory(chosen).stream().map(QuoteMessage::from).toList();
  }
}

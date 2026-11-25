package br.com.simuladorcarteira.fixedincome.adapters.in.web;

import br.com.simuladorcarteira.fixedincome.application.FixedIncomeProducts;
import br.com.simuladorcarteira.fixedincome.application.ListIndexes;
import br.com.simuladorcarteira.fixedincome.application.SimulateRedemption;
import br.com.simuladorcarteira.shared.Money;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fixed-income")
class FixedIncomeController {

  private final ListIndexes listIndexes;
  private final SimulateRedemption simulateRedemption;
  private final FixedIncomeProducts products;

  FixedIncomeController(
      ListIndexes listIndexes,
      SimulateRedemption simulateRedemption,
      FixedIncomeProducts products) {
    this.listIndexes = listIndexes;
    this.simulateRedemption = simulateRedemption;
    this.products = products;
  }

  @GetMapping("/products")
  List<ProductMessage> products() {
    return products.all().stream().map(ProductMessage::from).toList();
  }

  @GetMapping("/indexes")
  List<IndexRateMessage> indexes() {
    return listIndexes.latest().stream().map(IndexRateMessage::from).toList();
  }

  @PostMapping("/simulations")
  SimulationResponse simulate(@Valid @RequestBody SimulationRequest request) {
    // Os campos já passaram pela validação (@NotNull); o requireNonNull só informa o NullAway.
    return SimulationResponse.from(
        simulateRedemption.simulate(
            Money.brl(Objects.requireNonNull(request.principal())),
            Objects.requireNonNull(request.indexPercentage()),
            Objects.requireNonNull(request.start()),
            Objects.requireNonNull(request.end()),
            request.incomeTaxExempt()));
  }
}

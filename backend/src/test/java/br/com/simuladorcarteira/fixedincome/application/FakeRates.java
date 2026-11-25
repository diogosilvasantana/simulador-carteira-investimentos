package br.com.simuladorcarteira.fixedincome.application;

import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import br.com.simuladorcarteira.fixedincome.domain.RateIndex;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

final class FakeRates implements IndexRateRepository {

  private final List<IndexRate> data = new ArrayList<>();

  @Override
  public void saveAll(List<IndexRate> rates) {
    data.addAll(rates);
  }

  @Override
  public Optional<IndexRate> latest(RateIndex index) {
    return data.stream().filter(r -> r.index() == index).max(Comparator.comparing(IndexRate::date));
  }

  @Override
  public List<IndexRate> between(RateIndex index, LocalDate from, LocalDate to) {
    return data.stream()
        .filter(r -> r.index() == index && !r.date().isBefore(from) && !r.date().isAfter(to))
        .sorted(Comparator.comparing(IndexRate::date))
        .toList();
  }
}

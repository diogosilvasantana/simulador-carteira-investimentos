package br.com.simuladorcarteira.marketdata.domain;

import java.util.Arrays;
import java.util.Optional;

/** Segmento de listagem de governança da B3. Ações no mercado tradicional não têm segmento. */
public enum ListingSegment {
  NOVO_MERCADO("NM"),
  LEVEL_2("N2"),
  LEVEL_1("N1"),
  BOVESPA_MAIS("MA"),
  BOVESPA_MAIS_LEVEL_2("M2");

  private final String code;

  ListingSegment(String code) {
    this.code = code;
  }

  public static Optional<ListingSegment> fromCode(String code) {
    return Arrays.stream(values()).filter(segment -> segment.code.equals(code)).findFirst();
  }
}

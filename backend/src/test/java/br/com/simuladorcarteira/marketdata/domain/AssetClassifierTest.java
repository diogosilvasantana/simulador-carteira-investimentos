package br.com.simuladorcarteira.marketdata.domain;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.shared.Ticker;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Descrições reais do MT5 da XP, copiadas da sonda do catálogo (ADR-010). */
class AssetClassifierTest {

  @ParameterizedTest(name = "{0} \"{1}\" é {2}")
  @CsvSource(
      delimiter = '|',
      value = {
        "PETR4  | PETROBRAS   PN      N2  | STOCK",
        "VALE3  | VALE        ON      NM  | STOCK",
        "AESO3  | AESOPERACOESON          | STOCK",
        "BPAC5  | BTGP BANCO  PNA     N2  | STOCK",
        "AXIA7  | AXIA ENERGIAPNC     NM  | STOCK",
        "TAEE11 | TAESA       UNT ED  N2  | UNIT",
        "SANB11 | SANTANDER BRUNT         | UNIT",
        "MXRF11 | FII MAXI RENCI          | REAL_ESTATE_FUND",
        "AFHF11 | FII AFHF    CI  ERS     | REAL_ESTATE_FUND",
        "RURA11 | FIAGRO RURA CI          | AGRIBUSINESS_FUND",
        "BOVA11 | ISHARES BOVACI          | ETF",
        "ACWI11 | XPETFACWI   CI          | ETF",
        "AAPL34 | APPLE       DRN         | BDR",
        "EFXB31 | EQUIFAXBRL  DR1         | BDR",
        "AURA33 | AURA 360    DR3 ED      | BDR",
        "BACW39 | MSCI ACWI   DRE         | BDR_ETF",
        "DDIF31 | CUPOM CAMBIAL           | OTHER",
        "TF518  | ATIVO TESTE   ON DR     | OTHER"
      })
  void shouldClassifyByB3Specification(String ticker, String description, AssetType expected) {
    Asset asset = AssetClassifier.classify(Ticker.of(ticker), description);

    assertThat(asset.type()).isEqualTo(expected);
  }

  @ParameterizedTest(name = "\"{0}\" tem nome \"{1}\"")
  @CsvSource(
      delimiter = '|',
      value = {
        "PETROBRAS   PN      N2 | PETROBRAS",
        "AESOPERACOESON         | AESOPERACOES",
        "FII MAXI RENCI         | FII MAXI REN"
      })
  void shouldUseTheTwelveCharacterShortName(String description, String expected) {
    Asset asset = AssetClassifier.classify(Ticker.of("PETR4"), description);

    assertThat(asset.name()).isEqualTo(expected);
  }

  @ParameterizedTest(name = "\"{0}\" está em {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "VALE        ON      NM | NOVO_MERCADO",
        "ITAUUNIBANCOPN      N1 | LEVEL_1",
        "TAESA       UNT ED  N2 | LEVEL_2"
      })
  void shouldReadListingSegment(String description, ListingSegment expected) {
    Asset asset = AssetClassifier.classify(Ticker.of("VALE3"), description);

    assertThat(asset.listingSegment()).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {"ABEV3 | AMBEV S/A   ON", "MXRF11 | FII MAXI RENCI"})
  void shouldHaveNoListingSegmentWhenSpecificationHasNone(String ticker, String description) {
    Asset asset = AssetClassifier.classify(Ticker.of(ticker), description);

    assertThat(asset.listingSegment()).isNull();
  }

  @ParameterizedTest(name = "{0} vai para {1}")
  @CsvSource({
    "STOCK, STOCKS",
    "UNIT, STOCKS",
    "REAL_ESTATE_FUND, REAL_ESTATE_FUNDS",
    "AGRIBUSINESS_FUND, AGRIBUSINESS_FUNDS",
    "ETF, ETFS",
    "BDR, BDRS",
    "BDR_ETF, BDRS"
  })
  void shouldGroupTypesIntoStorefrontCategories(AssetType type, AssetCategory expected) {
    assertThat(type.category()).contains(expected);
  }

  @org.junit.jupiter.api.Test
  void shouldLeaveUnknownTypesOutOfTheStorefront() {
    assertThat(AssetType.OTHER.category()).isEmpty();
  }
}

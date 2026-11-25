package br.com.simuladorcarteira.marketdata.domain;

import br.com.simuladorcarteira.shared.Ticker;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Classifica um ativo pela descrição que o MT5 repassa da B3 (ADR-010).
 *
 * <p>A descrição tem largura fixa: 12 caracteres de nome curto e, depois, a especificação do papel
 * ({@code ON}, {@code PN}, {@code UNT}, {@code CI}, {@code DRN}...), marcas de negociação ex e o
 * segmento de listagem. O nome pode encostar na especificação ({@code AESOPERACOESON}), por isso o
 * corte é pela posição e não por espaços.
 */
public final class AssetClassifier {

  private static final int NAME_WIDTH = 12;
  private static final Pattern SHARE = Pattern.compile("ON|PN[A-H]?");
  private static final Set<String> BDR_CODES = Set.of("DRN", "DR1", "DR2", "DR3");
  // Fundos listados que não são FII, Fiagro nem ETF; não entram na vitrine por ora.
  private static final List<String> OTHER_FUND_PREFIXES = List.of("FIP", "FIDC", "FIC", "FI ");

  private AssetClassifier() {}

  public static Asset classify(Ticker ticker, String description) {
    String name = description.length() > NAME_WIDTH ? description.substring(0, NAME_WIDTH) : "";
    String rest = description.length() > NAME_WIDTH ? description.substring(NAME_WIDTH) : "";
    List<String> specification = rest.isBlank() ? List.of() : List.of(rest.strip().split("\\s+"));
    String code = specification.isEmpty() ? "" : specification.getFirst();
    String shortName = name.isBlank() ? description.strip() : name.strip();
    return new Asset(
        ticker, shortName, typeOf(description, shortName, code), listingSegmentOf(specification));
  }

  private static AssetType typeOf(String description, String name, String code) {
    if (name.contains("TESTE")) {
      return AssetType.OTHER;
    }
    if (description.startsWith("FII ")) {
      return AssetType.REAL_ESTATE_FUND;
    }
    if (description.startsWith("FIAGRO")) {
      return AssetType.AGRIBUSINESS_FUND;
    }
    if (BDR_CODES.contains(code)) {
      return AssetType.BDR;
    }
    return switch (code) {
      case "DRE" -> AssetType.BDR_ETF;
      case "UNT" -> AssetType.UNIT;
      case "CI" ->
          OTHER_FUND_PREFIXES.stream().anyMatch(name::startsWith) ? AssetType.OTHER : AssetType.ETF;
      default -> SHARE.matcher(code).matches() ? AssetType.STOCK : AssetType.OTHER;
    };
  }

  private static @Nullable ListingSegment listingSegmentOf(List<String> specification) {
    return specification.stream()
        .map(ListingSegment::fromCode)
        .flatMap(Optional::stream)
        .findFirst()
        .orElse(null);
  }
}

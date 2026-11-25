package br.com.simuladorcarteira.marketdata.domain;

import br.com.simuladorcarteira.shared.Ticker;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/** Ativo do catálogo: o que é, como se chama e em que segmento de listagem está. */
public record Asset(
    Ticker ticker, String name, AssetType type, @Nullable ListingSegment listingSegment) {

  public Asset {
    Objects.requireNonNull(ticker, "ticker");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(type, "type");
  }

  public Optional<AssetCategory> category() {
    return type.category();
  }
}

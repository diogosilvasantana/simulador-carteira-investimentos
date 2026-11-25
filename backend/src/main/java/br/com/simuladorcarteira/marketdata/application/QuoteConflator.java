package br.com.simuladorcarteira.marketdata.application;

import br.com.simuladorcarteira.marketdata.domain.AssetQuote;
import br.com.simuladorcarteira.shared.Ticker;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Junta as atualizações entre um envio e outro, guardando só a mais recente de cada ativo.
 *
 * <p>Na abertura chegam centenas de ticks por segundo; mandar um a um sobrecarrega o navegador.
 * Com o agrupamento, cada ativo aparece no máximo uma vez por lote (ADR-010).
 */
public class QuoteConflator {

  private Map<Ticker, AssetQuote> pending = new LinkedHashMap<>();

  public synchronized void put(AssetQuote quote) {
    pending.put(quote.ticker(), quote);
  }

  /** Devolve o lote acumulado e começa um novo. */
  public synchronized List<AssetQuote> drain() {
    if (pending.isEmpty()) {
      return List.of();
    }
    List<AssetQuote> batch = List.copyOf(pending.values());
    pending = new LinkedHashMap<>();
    return batch;
  }
}

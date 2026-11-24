package br.com.simuladorcarteira.marketdata.application;

import br.com.simuladorcarteira.marketdata.domain.Tick;

/** Porta de saída: avisa quem está assistindo que uma cotação mudou. */
public interface QuoteBroadcaster {

  void broadcast(Tick tick);
}

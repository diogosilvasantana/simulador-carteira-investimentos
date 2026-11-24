package br.com.simuladorcarteira.marketdata.domain;

/** De onde veio o preço: MT5 ao vivo, replay de pregão gravado ou gerador sintético. */
public enum TickSource {
  LIVE,
  REPLAY,
  SYNTHETIC
}

package br.com.simuladorcarteira.fixedincome.domain;

/** Indexador da renda fixa. Cada um é publicado numa unidade diferente pelo Banco Central. */
public enum RateIndex {
  /** Taxa do dia útil, em % ao dia. */
  CDI,
  /** Meta definida pelo Copom, em % ao ano. */
  SELIC_TARGET,
  /** Inflação do mês, em % no mês. */
  IPCA
}

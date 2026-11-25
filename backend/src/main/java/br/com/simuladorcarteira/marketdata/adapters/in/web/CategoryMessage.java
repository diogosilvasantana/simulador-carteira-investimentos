package br.com.simuladorcarteira.marketdata.adapters.in.web;

/** Uma aba da vitrine: a categoria e quantos ativos ela tem. */
public record CategoryMessage(String slug, long count) {}

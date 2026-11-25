# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Três públicos, confirmados pelo dono do produto:

- O próprio autor, que usa o simulador para treinar antes de investir de verdade.
- Iniciantes aprendendo a investir, que precisam entender o que estão vendo (termos como preço médio, carência, IR regressivo, % do CDI).
- Investidores que já operam e querem testar carteiras e estratégias sem arriscar dinheiro.

Todos usam o produto logado, em sessões de acompanhamento e de treino.

## Product Purpose

Home broker simulado com cotações reais da B3. Permite montar e acompanhar uma carteira diversificada (ações, FIIs, Fiagro, ETFs, BDRs e renda fixa), com aportes, proventos reinvestidos e rebalanceamento; operar em tempo real com preços reais; e treinar com o mercado fechado, em replay de pregões passados ou em modo sintético com semente fixa. Sucesso é o usuário ganhar prática e confiança sem perder dinheiro.

## Positioning

Preços reais do MetaTrader 5 (B3) e indexadores oficiais do Banco Central, com regras reais de IR e IOF versionadas por data, num ambiente onde nenhuma ordem chega ao mercado. O pregão simulado (ao vivo, replay e sintético) é o mecanismo próprio do produto.

## Operating Context

- Cotações chegam do MT5 por uma ponte Python; o backend Spring Boot distribui por WebSocket (STOMP) em lotes de 500 ms.
- Indexadores (CDI, Selic, IPCA) vêm do SGS do Banco Central.
- O usuário roda tudo no próprio PC (Windows) com `dev.cmd`.

## Capabilities and Constraints

- Hoje no app: cotações ao vivo por categoria (`/cotacoes/:categoria`), renda fixa com indexadores, produtos modelo e simulação de resgate (`/renda-fixa`), página inicial.
- Ainda sem backend: carteira, posições, caixa, aportes, ordens, proventos, replay e login. Telas dessas áreas não devem fingir dados reais.
- Dinheiro sempre como texto decimal vindo da API; nunca recalcular com ponto flutuante.
- Desktop e celular têm o mesmo peso.
- Nada pode parecer uma corretora real: o modo simulado fica sempre visível.

## Brand Commitments

- Nome: Simulador de Carteira.
- Interface e textos em português do Brasil.
- Design System aprovado: "Simulador de Carteira" (Carvão e Índigo), publicado como artifact em https://claude.ai/artifact/FWFzawgZnJurbJ3fsNu5VT.
- Menu lateral recolhível (com nomes, recolhe para só ícones) e carrossel de tipos de ativo: escolhas explícitas do dono do produto.
- Referência estrutural apenas inspiracional: Íon (Itaú). Não copiar cores nem estrutura.

## Evidence on Hand

Nenhum depoimento, cliente ou número de uso. Dados de demonstração precisam ser marcados como simulados.

## Product Principles

1. Treino sem risco, sempre explícito: o usuário nunca confunde simulação com mercado real.
2. Dados reais, regras reais: preços, indexadores e impostos vêm de fontes oficiais.
3. Ensinar enquanto mostra: termos do mercado aparecem com contexto para quem está começando.
4. Leve mesmo com milhares de ativos ao vivo.

## Accessibility & Inclusion

Alta e baixa nunca só pela cor (seta e sinal); contraste mínimo de 4,5:1 no texto; respeitar redução de movimento.

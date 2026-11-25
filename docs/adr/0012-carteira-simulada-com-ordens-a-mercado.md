# ADR-012: Carteira simulada com ordens a mercado, em memória

- **Status:** Aceito
- **Data:** 2026-11-25

## Contexto

O produto precisa que o usuário monte e acompanhe uma carteira diversificada: aportar dinheiro, comprar e vender ativos da B3, aplicar em renda fixa e ver o resultado a preço de mercado. Ainda não há login (`identity`) nem banco de dados (#4), e o mercado simulado entrega só a última cotação de cada ativo, sem livro de ofertas.

## Opções consideradas

1. **Módulos `trading` e `portfolio` separados, com eventos (ADR-004)**: fronteira mais pura, mas exige o registro de publicação de eventos do Spring Modulith, que depende de persistência que ainda não existe, e não há livro de ofertas para justificar um motor de ordens.
2. **Tudo no `portfolio`, ordens a mercado executadas na hora**: menos peças; a ordem executa no último preço do MT5, que é o que o simulador consegue oferecer hoje.

## Decisão

Opção 2. O módulo `portfolio` guarda caixa, aportes, posições (preço médio com custos), aplicações em renda fixa e execuções. Ordens são a mercado e executam no último preço recebido. Custos de negociação vêm da configuração, versionados por data. Para não acessar o interior de outros módulos, `marketdata` e `fixedincome` passam a expor APIs públicas no pacote raiz (`MarketQuotes`, `FixedIncomeMarket`). Há uma carteira só, em memória: reiniciar o backend recomeça o treino.

## Consequências

- A tela de carteira, a boleta com revisão e o assistente de montar carteira funcionam já.
- Quando chegarem livro de ofertas e ordens limitadas, as ordens migram para o módulo `trading`, publicando `Execution` como evento para o `portfolio`.
- Quando chegarem login e persistência, o repositório em memória vira um adaptador JPA por usuário, sem mexer no domínio.

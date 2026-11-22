# ADR-005: Dinheiro como BigDecimal em value object Money

- **Status:** Aceito
- **Data:** 2026-11-22

## Contexto

Erros de arredondamento em preço médio, custos e caixa são inaceitáveis num simulador financeiro (cenário de qualidade QA1).

## Opções consideradas

1. `double`: impreciso por natureza.
2. `long` em centavos: preciso, mas preços de ativos e taxas podem ter mais casas.
3. `BigDecimal` encapsulado em `Money`, com escala e arredondamento definidos num só lugar.

## Decisão

Opção 3. `Money` guarda valor e moeda; operações retornam novos objetos; arredondamento HALF_EVEN com escala 2 para valores monetários. Preços unitários e quantidades têm tipos próprios.

## Consequências

- Nenhum `double` para dinheiro (verificável por ArchUnit)
- Regras de arredondamento testadas uma vez
- Persistência como `NUMERIC(19,4)` no Postgres

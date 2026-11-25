# O que a plataforma oferece

Estado em 25/11/2026. Confirme no código antes de usar.

| Recurso | API |
| --- | --- |
| Categorias (`acoes`, `fiis`, `fiagros`, `etfs`, `bdrs`) | `GET /api/categories` |
| Ativos e cotação por categoria | `GET /api/assets?category=` |
| Um ativo | `GET /api/assets/{ticker}` |
| Últimas cotações | `GET /api/quotes` |
| Produtos modelo e indexadores | `GET /api/fixed-income/products` e `/indexes` |
| Simular resgate pós-fixado | `POST /api/fixed-income/simulations` |
| Ver a carteira simulada | `GET /api/portfolio` |
| Aportar, prever e enviar ordem | `POST /api/portfolio/contributions`, `/orders/preview`, `/orders` |
| Aplicar em renda fixa | `POST /api/portfolio/fixed-income` |
| Apagar a carteira | `DELETE /api/portfolio`: só com pedido explícito |

Valores vêm como texto decimal. Antes de usar cotação, confira `occurredAt` e `source`; campos podem
ser nulos. O endereço local do backend está em `docs/guias/`.

## Limites

- Uma carteira só, em memória: reiniciar o backend apaga tudo (ADR-012). Não há provento, TWR,
  alocação-alvo salva nem API de resgate de renda fixa
- Só diga que uma ordem foi executada após a API confirmar, e só envie ordem pedida pelo usuário
- Não há fundamentos, setor ou histórico de preços: busque fora e cite
- Renda fixa só pós-fixada ao CDI; Tesouro Selic é aproximado por 100% do CDI
- Datas futuras repetem o último CDI e ignoram feriados: apresente como projeção
- A simulação não modela risco de crédito e não bloqueia carência; compare `holdingDays` com
  `gracePeriodDays`
- Produtos são modelos, não ofertas reais
- A série Selic meta (SGS 432) pode vir com data futura até a próxima reunião do Copom: não chame
  isso de previsão

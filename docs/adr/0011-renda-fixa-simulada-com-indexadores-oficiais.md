# ADR-011: Renda fixa simulada com indexadores oficiais

- **Status:** Aceito
- **Data:** 2026-11-24

## Contexto

O simulador precisa montar carteiras diversificadas, com aportes e reinvestimento de proventos, e
isso inclui renda fixa: Tesouro Direto, CDB, LCI e LCA. O MT5 só cobre a bolsa, e renda fixa não
precisa de tempo real: o valor muda uma vez por dia (marcação na curva) ou poucas vezes ao dia
(preços do Tesouro).

Fontes disponíveis:

- **Banco Central, SGS:** API JSON pública e gratuita com CDI diário, Selic meta (série 432) e
  IPCA mensal (série 433). Consultas por período limitadas a 10 anos; últimos valores limitados a 20.
- **Tesouro Transparente:** série oficial "Taxas dos Títulos Ofertados pelo Tesouro Direto", diária
  desde 2004, em CSV. O arquivo é grande (centenas de MB) e só traz histórico.
- **ANBIMA Data:** preços de referência de debêntures, CRI, CRA e títulos públicos, grátis, mas só
  dos últimos cinco dias úteis; o histórico é pago (ANBIMA Feed).
- **CDB, LCI e LCA:** não há base pública de ofertas; cada banco define a sua taxa.

A tributação vigente em 2026 é a tabela regressiva de IR (22,5% a 15%), com LCI e LCA isentas para
pessoa física. A MP 1.303/2025, que unificaria a alíquota em 17,5%, caducou. A regra pode mudar de
novo.

## Opções consideradas

1. **Só Tesouro Direto, pelos preços oficiais:** real, mas deixa de fora CDB e LCI, que são a
   renda fixa mais comum do investidor iniciante.
2. **Fonte paga (ANBIMA Feed, brapi Pro):** cobre mais ativos, mas cria custo recorrente e
   dependência de terceiros num projeto de estudo.
3. **Indexadores oficiais + produtos modelo:** CDI, Selic e IPCA do Banco Central alimentam
   produtos simulados ("CDB 105% do CDI", "LCI 92% do CDI") que rendem dia a dia; o Tesouro Direto
   usa as taxas oficiais do Tesouro Transparente.

## Decisão

Opção 3, num módulo novo, `fixedincome`, em etapas:

1. **Agora:** cliente do SGS (CDI, Selic meta, IPCA), sincronizado na subida e duas vezes por dia,
   com carga inicial em janelas de até 10 anos. Domínio de rendimento pelo CDI (fator diário
   acumulado), IOF regressivo nos primeiros 29 dias e IR regressivo pelo prazo, com isenção para
   LCI e LCA. API de simulação de resgate.
2. **Depois:** importador do Tesouro Transparente (leitura em fluxo, guardando só a última taxa de
   cada título), produtos modelo na vitrine e posições de renda fixa nas carteiras, com aporte e
   reinvestimento.

As alíquotas de IR e IOF ficam na configuração, versionadas por data de início de vigência. Uma
mudança de lei vira uma nova entrada, sem mexer em código, e simulações de períodos antigos
continuam usando a regra da época.

## Consequências

- CDB e LCI simulados rendem como os reais em produtos pós-fixados, mas não refletem risco de
  crédito nem marcação a mercado de prefixados
- O sistema depende da disponibilidade da API do Banco Central; sem ela, a simulação responde com
  erro claro em vez de inventar taxa
- Os indexadores ficam em memória até a persistência (issue #4) chegar; a carga inicial roda a cada
  subida
- Tesouro Direto entra na etapa 2: o CSV oficial pede leitura em fluxo e atualização diária

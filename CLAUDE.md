# Instruções do projeto para o Claude

Simulador de carteira de investimentos: home broker simulado com cotações reais da B3.
Monorepo com `backend/` (Java 25, Spring Boot 4), `frontend/` (Angular 22), `mt5-bridge/`
(Python) e `infra/`. A arquitetura está em `docs/architecture/README.md` e as decisões em
`docs/adr/`. Leia o ADR relevante antes de mudar algo estrutural.

## Idioma

- Código (classes, métodos, variáveis, testes) em inglês, seguindo o glossário abaixo
- Documentação, ADRs, mensagens de commit, descrições de PR e textos da interface em português

## Glossário (linguagem ubíqua)

Use sempre estes nomes no código. Se um conceito novo surgir, adicione-o aqui e em
`docs/architecture/README.md` no mesmo PR.

| Termo | No código |
| --- | --- |
| Ativo | `Asset` |
| Ticker | `Ticker` |
| Ordem | `Order` |
| Execução | `Execution` |
| Posição | `Position` |
| Preço médio | `averagePrice` |
| Resultado realizado / não realizado | `realizedPnl` / `unrealizedPnl` |
| Caixa | `cash` |
| Aporte | `Contribution` |
| Provento | `CorporateAction` |
| JCP | `InterestOnEquity` |
| Data com / data ex / pagamento | `recordDate` / `exDate` / `paymentDate` |
| Alocação-alvo | `TargetAllocation` |
| Pregão | `TradingSession` |
| Tick / Candle / Feed | `Tick` / `Candle` / `MarketFeed` |

## Arquitetura do backend

- Monólito modular (ADR-001). Módulos são pacotes de primeiro nível em
  `backend/src/main/java/br/com/simuladorcarteira/`: `identity`, `marketdata`, `trading`,
  `portfolio`, `corporateactions`, `analytics`, `shared`
- Dentro de cada módulo, hexagonal (ADR-002): `domain` (Java puro, sem Spring nem JPA),
  `application` (casos de uso e portas), `adapters/in` e `adapters/out`
- Módulos só se comunicam pela API pública do pacote raiz do módulo ou por eventos (ADR-004)
- Nunca importar classes de `internal`, `domain`, `application` ou `adapters` de outro módulo
- Os testes `ModularityTests` e `ArchitectureTests` quebram o build se essas regras forem violadas

## Regras de código

- Dinheiro sempre em `Money` (BigDecimal, escala 2); nunca `double` ou `float` (ADR-005)
- Injeção por construtor e campos `final`; nada de `@Autowired` em campo
- Entidade JPA separada do modelo de domínio; a entidade nunca sai pela API
- DTOs da API como `record`, validados com Bean Validation
- Erros de domínio são exceções específicas, traduzidas para Problem Details (RFC 9457)
- Sem números mágicos: custos e alíquotas vêm de configuração versionada por data
- Comentários explicam o porquê, não o quê

## Testes

- TDD no domínio: escreva o teste antes da implementação
- Nome do teste descreve o comportamento (`shouldRejectSellAboveOpenPosition`)
- Estrutura Arrange-Act-Assert, um comportamento por teste
- Testes de domínio não sobem contexto Spring
- Para testar com o mercado fechado, use o simulador de pregão (modo replay ou sintético com
  semente fixa), nunca dados aleatórios sem semente

## Comandos

| O quê | Comando |
| --- | --- |
| Testes e verificações do backend | `cd backend && mvn verify` |
| Formatar o backend | `cd backend && mvn spotless:apply` |
| Lint e testes do frontend | `cd frontend && npm run lint && npm test -- --watch=false` |
| Build do frontend | `cd frontend && npm run build` |
| Lint e tipos da ponte | `cd mt5-bridge && ruff check . && ruff format --check . && mypy .` |

## Fluxo de trabalho

- Uma issue por branch, branch curta a partir da `main` (`feat/…`, `fix/…`, `chore/…`, `docs/…`)
- Commits em Conventional Commits com escopo: `feat(trading): …`, `fix(frontend): …`
- Todo PR preenche a Definition of Done do template
- Decisão de arquitetura nova = ADR novo em `docs/adr/` no mesmo PR
- Nunca commitar segredos; configuração por variáveis de ambiente

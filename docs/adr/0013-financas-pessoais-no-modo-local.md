# ADR-013: Finanças pessoais no modo local, com banco em arquivo

- **Status:** Aceito
- **Data:** 2026-11-25

## Contexto

A plataforma passa a cuidar também do orçamento e das dívidas. São dados sensíveis, ainda não há
login (#5) e o staging é público. O `dev.cmd` roda no Windows sem Docker e os dados precisam
sobreviver a reinícios.

## Opções consideradas

1. **PostgreSQL em Docker no PC**: igual à produção, mas exige Docker para o uso diário.
2. **PostgreSQL embarcado**: sem Docker, mas deixa um processo filho para administrar no Windows.
3. **H2 em arquivo só no perfil `local`**: nada a instalar; exige SQL portável.

## Decisão

Opção 3, num módulo novo, `personalfinance`. Migrations portáveis ficam em
`db/migration/common`, que roda nos dois bancos; as exclusivas do PostgreSQL, em
`db/migration/postgresql`. A área só existe com `simulador.personal-finance.enabled=true`, ligada
apenas no perfil `local`, que escuta somente em `127.0.0.1`. O acesso usa `JdbcClient`: são duas
tabelas sem relacionamento, e o domínio não precisa de entidade JPA.

Os dados ficam em `~/.simulador-carteira`, fora do repositório.

## Consequências

- Roda sem Docker e sem expor dados na rede
- O arquivo **não é cifrado** e não tem backup automático: até existirem, use a cifra de disco do
  sistema e copie a pasta com a aplicação fechada
- Ir para a VPS exige login, cifra, backup e migração dos dados para o PostgreSQL
- SQL de módulos que rodam localmente precisa funcionar nos dois bancos

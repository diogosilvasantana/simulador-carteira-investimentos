# ADR-013: Carteira persistida com SQL explícito, PostgreSQL no servidor e H2 no PC

- **Status:** Aceito
- **Data:** 2026-11-25

## Contexto

A carteira do ADR-012 vivia em memória e sumia a cada reinício. Para subir em produção (Coolify, PostgreSQL), ela precisa ir para o banco. Ao mesmo tempo, o `dev.cmd` roda no PC sem Docker, e o perfil local não tinha banco nenhum.

A carteira é um agregado pequeno (dezenas a centenas de linhas), sempre lido e alterado inteiro, numa transação, com as regras no domínio.

## Opções consideradas

1. **Entidades JPA** espelhando o agregado: mapeamento de coleções, entidades com construtor vazio (atrito com o NullAway) e o Hibernate decidindo quando gravar.
2. **SQL explícito com `JdbcClient`**: o adaptador lê o estado, o domínio altera, o adaptador grava a diferença.
3. **Perfil local com PostgreSQL instalado no Windows**: igual à produção, mas exige instalar e manter um serviço no PC.
4. **Perfil local com H2 em arquivo, em modo PostgreSQL**: nada a instalar; as migrations precisam ser portáveis.

## Decisão

Opções 2 e 4.

- O domínio ganha `PortfolioState` e `Portfolio.restore`, para gravar e reconstruir o agregado sem expor seus campos.
- `JdbcPortfolioRepository` trava a linha da carteira (`select … for update`), reconstrói, aplica a mudança e grava: posições são regravadas; execuções, aportes e aplicações só ganham as linhas novas.
- Migrations em `db/migration/common` (SQL portátil) e `db/migration/{vendor}` (o que só um banco entende, como o índice hash do PostgreSQL).
- O perfil local usa H2 em `backend/data/`; os testes usam PostgreSQL real (Testcontainers) e um teste sobe o perfil local com H2 em memória para garantir que as migrations rodam nos dois.

## Consequências

- A carteira sobrevive a reinícios no PC e no servidor
- Toda migration nova precisa rodar no H2 e no PostgreSQL; o `LocalProfileTests` quebra a CI se não rodar
- Duas ordens simultâneas esperam uma pela outra no banco, sem gastar o mesmo caixa
- As tabelas já têm `portfolio_id`; hoje há só a carteira 1, e o login vai separar uma por usuário
- Substitui a parte "em memória" do ADR-012

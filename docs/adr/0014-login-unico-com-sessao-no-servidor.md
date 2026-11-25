# ADR-014: Login de um usuário só, com sessão no servidor

- **Status:** Aceito
- **Data:** 2026-11-25

## Contexto

O sistema vai para a internet em `financas.zinklab.com.br` e mostra a carteira do dono. Só ele pode entrar. O módulo `identity` previa usuários e JWT, mas hoje existe um usuário e nenhuma outra aplicação consumindo a API.

## Opções consideradas

1. **JWT no navegador**: o token fica acessível ao JavaScript (ou exige cookie de qualquer jeito), e revogar antes de expirar dá trabalho.
2. **Sessão no servidor com cookie HttpOnly**: o padrão do Spring Security, revogável no logout, sem credencial legível por script.
3. **Login fora do app** (autenticação no proxy do Coolify): protege, mas tira o logout e o controle de tentativas do código e dos testes.

## Decisão

Opção 2.

- Um usuário, vindo de `APP_USERNAME` e `APP_PASSWORD_HASH` (BCrypt, custo 12, aceito também em Base64 por causa dos cifrões no Docker Compose). Sem as duas, o backend não sobe.
- Cookie de sessão `HttpOnly`, `Secure`, `SameSite=Strict`, expirando após 2 h sem uso. O ID da sessão muda no login.
- CSRF em cookie lido pelo Angular e devolvido em cabeçalho, inclusive no próprio login.
- Tudo exige sessão, inclusive o WebSocket `/ws`. Ficam abertos só o login, a saúde (para o Coolify, e bloqueada no nginx) e a ingestão `/ingest`, que tem token próprio e não é publicada no domínio.
- Freio de tentativas: 5 erros por IP ou 20 no total em 15 min bloqueiam o login por 15 min (HTTP 429). O IP vem do `X-Forwarded-For` aceito só de proxies da rede interna.
- O nginx envia CSP, HSTS, `X-Frame-Options`, `nosniff`, `Referrer-Policy` e `Permissions-Policy`.

## Consequências

- Sem senha em código, repositório ou imagem; o hash é gerado no PC por `scripts/gerar-hash-senha.py`
- Os testes de API fazem login (`TestLogin`) como o navegador faria
- Reiniciar o backend encerra as sessões (estão na memória); basta entrar de novo
- Quando houver mais usuários, a carteira já separa por `portfolio_id` (ADR-013) e o `InMemoryUserDetailsManager` dá lugar a uma tabela

# ADR-008: Gitflow com ambiente de staging e releases versionadas

- **Status:** Aceito
- **Data:** 2026-11-22

## Contexto

O projeto começou com trunk-based (tudo direto na `main`, com deploy a cada merge). Agora queremos
um ambiente de **staging** para validar mudanças antes da produção, versões numeradas com
CHANGELOG, e um processo de PR que sirva de portfólio: rótulos, títulos padronizados e notas de
versão geradas automaticamente.

## Opções consideradas

1. **Trunk-based**: simples e rápido, mas sem um lugar para validar antes da produção e sem marco
   claro de versão.
2. **GitHub Flow + ambientes por tag**: `main` vai para staging e tags vão para produção. Funciona,
   mas a produção fica a uma tag de distância de qualquer merge, sem um PR de promoção revisável.
3. **Gitflow simplificado**: `develop` (staging) e `main` (produção), com branches de feature,
   release e hotfix, e automação para cortar versões e fazer back-merge.

## Decisão

Gitflow simplificado (opção 3):

| Branch | Origem | Destino | Ambiente | Merge |
| --- | --- | --- | --- | --- |
| `main` | — | — | produção | — |
| `develop` | `main` | — | staging | — |
| `feat/*`, `fix/*`, `refactor/*`, `docs/*`, `chore/*`, `ci/*`, `test/*`, `perf/*` | `develop` | `develop` | — | squash |
| `release/vX.Y.Z` | `develop` (pelo workflow Release) | `main` | — | merge commit |
| `hotfix/*` | `main` | `main` | — | merge commit |
| back-merge | `main` | `develop` (PR automático) | — | merge commit |

- Versões em SemVer; o workflow **Release** calcula a próxima versão a partir da última tag,
  atualiza `pom.xml`, `package.json` e `pyproject.toml`, gera o `CHANGELOG.md` com git-cliff e abre
  o PR para a `main`.
- O merge na `main` cria a tag e a GitHub Release (notas por rótulo) e abre o back-merge.
- A política de branches é verificada em todo PR pelo job "Política de branches".

## Consequências

- Staging sempre reflete a `develop`, e produção só recebe código que passou por staging
- PRs de feature viram um único commit convencional na `develop` (squash), o que mantém o
  CHANGELOG limpo
- Mais cerimônia que trunk-based: um PR de release a cada versão e um back-merge depois
- PRs abertos por workflow com o `GITHUB_TOKEN` não disparam a CI; para isso, configurar o segredo
  `RELEASE_TOKEN` (token pessoal com permissão de conteúdo e PRs no repositório)
- Substitui a orientação de trunk-based que constava no `CONTRIBUTING.md` e no `CLAUDE.md`

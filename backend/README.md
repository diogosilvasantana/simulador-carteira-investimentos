# Backend

Monólito modular em Java 25 + Spring Boot 4 + Spring Modulith. Visão geral em
[`docs/architecture`](../docs/architecture/README.md).

## Módulos

| Pacote | Responsabilidade |
| --- | --- |
| `identity` | Usuários e autenticação |
| `marketdata` | Ticks, cotações, candles, replay, catálogo de ativos |
| `trading` | Ordens e execução simulada |
| `portfolio` | Carteiras, posições, preço médio |
| `corporateactions` | Proventos e eventos societários |
| `analytics` | Rentabilidade e rebalanceamento |
| `shared` | `Money`, `Ticker` e outros tipos de valor |

Cada módulo segue a estrutura hexagonal: `domain`, `application`, `adapters/in`, `adapters/out`.

## Comandos

```bash
./mvnw verify          # testes, regras de arquitetura, formatação e cobertura
./mvnw spotless:apply  # formata o código
./mvnw spring-boot:run # sobe em http://localhost:8080 (health em /actuator/health)
```

`./mvnw verify` também gera os diagramas dos módulos em `target/spring-modulith-docs`.

## O que quebra o build

- `ModularityTests`: um módulo usando tipos internos de outro, ou ciclo entre módulos
- `ArchitectureTests`: domínio dependendo de Spring/JPA, injeção por campo, dinheiro em `double`,
  pacote sem `package-info.java` com `@NullMarked`
- Error Prone: erros comuns de Java detectados na compilação
- NullAway: possível `null` em código de pacote `@NullMarked` (todos os pacotes do projeto)
- Spotless: código fora do google-java-format
- JaCoCo: cobertura de linhas abaixo de 80% em `shared` e nos pacotes `domain`

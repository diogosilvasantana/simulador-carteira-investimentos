# ADR-009: Maven como ferramenta de build do backend

- **Status:** Aceito
- **Data:** 2026-11-22

## Contexto

O backend é um único projeto Spring Boot (monólito modular, ADR-001). A escolha entre Maven e
Gradle afeta a CI, o Dockerfile, o workflow de release e o que quem visita o repositório precisa
conhecer para contribuir.

## Opções consideradas

1. **Gradle (Kotlin DSL)**: build incremental e cache mais rápidos, script como código. Ganha em
   builds grandes com muitos subprojetos e lógica customizada.
2. **Maven**: declarativo, previsível, padrão na maior parte dos projetos Java corporativos.

## Decisão

Maven, sempre pelo **Maven Wrapper** (`./mvnw`, modo *only-script*), com a versão do Maven fixada em
`backend/.mvn/wrapper/maven-wrapper.properties`.

Junto com o wrapper, o compilador roda **Error Prone** e **NullAway**, e todo pacote é
`@NullMarked` (JSpecify), regra verificada pelo `ArchitectureTests`.

## Consequências

- Mesma versão do Maven na máquina de quem desenvolve, na CI e no Dockerfile, sem instalar nada
- Para um projeto deste tamanho, a diferença de velocidade para o Gradle é pequena
- Configurações mais elaboradas ficam em XML; se o build crescer muito (vários subprojetos), a
  troca para Gradle pode ser reavaliada num novo ADR
- `.mvn/jvm.config` guarda os `--add-exports` que o Error Prone precisa no JDK 25

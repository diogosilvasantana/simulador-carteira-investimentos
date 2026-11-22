package br.com.simuladorcarteira;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Regras de arquitetura e de código limpo verificadas a cada build (ADR-002, ADR-005). */
@AnalyzeClasses(
    packages = "br.com.simuladorcarteira",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTests {

  @ArchTest
  static final ArchRule domainShouldNotDependOnFrameworks =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "..adapters..")
          .because("o domínio é Java puro e testável sem infraestrutura (ADR-002)")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule applicationShouldNotDependOnAdapters =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapters..")
          .because("casos de uso dependem de portas, não de implementações (ADR-002)")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule noFieldInjection =
      fields()
          .should()
          .notBeAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
          .because("injeção de dependência é feita pelo construtor")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule moneyIsNeverFloatingPoint =
      fields()
          .that()
          .haveNameMatching("(?i).*(price|amount|cash|cost|fee|value).*")
          .should()
          .notHaveRawType(double.class)
          .andShould()
          .notHaveRawType(float.class)
          .andShould()
          .notHaveRawType(Double.class)
          .andShould()
          .notHaveRawType(Float.class)
          .because("dinheiro usa Money/BigDecimal (ADR-005)")
          .allowEmptyShould(true);
}

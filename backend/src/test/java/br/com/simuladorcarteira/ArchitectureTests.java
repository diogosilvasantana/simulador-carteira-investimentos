package br.com.simuladorcarteira;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.jspecify.annotations.NullMarked;

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

  @ArchTest
  static final ArchRule everyPackageIsNullMarked =
      classes()
          .should(resideInNullMarkedPackage())
          .because("o NullAway só verifica pacotes com @NullMarked; crie o package-info.java");

  private static ArchCondition<JavaClass> resideInNullMarkedPackage() {
    return new ArchCondition<>("reside in a package annotated with @NullMarked") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        boolean nullMarked = javaClass.getPackage().isAnnotatedWith(NullMarked.class);
        String message =
            "%s está no pacote %s, que não tem @NullMarked"
                .formatted(javaClass.getName(), javaClass.getPackageName());
        events.add(new SimpleConditionEvent(javaClass, nullMarked, message));
      }
    };
  }
}

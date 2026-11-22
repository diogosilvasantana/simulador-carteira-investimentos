package br.com.simuladorcarteira;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Garante as fronteiras do monólito modular (ADR-001): nenhum módulo acessa tipos internos de
 * outro e não há dependência cíclica entre módulos.
 */
class ModularityTests {

  private final ApplicationModules modules =
      ApplicationModules.of(SimuladorCarteiraApplication.class);

  @Test
  void shouldRespectModuleBoundaries() {
    modules.verify();
  }

  @Test
  void shouldGenerateModuleDocumentation() {
    new Documenter(modules).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml();
  }
}

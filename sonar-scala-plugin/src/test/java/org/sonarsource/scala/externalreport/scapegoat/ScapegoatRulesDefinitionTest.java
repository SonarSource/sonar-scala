/*
 * SonarSource Scala
 * Copyright (C) 2018-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonarsource.scala.externalreport.scapegoat;

import org.junit.jupiter.api.Test;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class ScapegoatRulesDefinitionTest {

  @Test
  void external_repository() {
    RulesDefinition.Context context = new RulesDefinition.Context();
    ScapegoatRulesDefinition rulesDefinition = new ScapegoatRulesDefinition();
    rulesDefinition.define(context);

    assertThat(context.repositories()).hasSize(1);
    RulesDefinition.Repository repository = context.repository("external_scapegoat");
    assertThat(repository.name()).isEqualTo("Scapegoat");
    assertThat(repository.language()).isEqualTo("scala");
    assertThat(repository.isExternal()).isTrue();
    assertThat(repository.rules()).hasSize(123);

    RulesDefinition.Rule rule = repository.rule("com.sksamuel.scapegoat.inspections.AvoidToMinusOne");
    assertThat(rule).isNotNull();
    assertThat(rule.name()).isEqualTo("Checks for loops that use `x to n-1` instead of `x until n`");
    assertThat(rule.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(rule.severity()).isEqualTo("MINOR");
    assertThat(rule.htmlDescription()).isEqualTo("" +
      "See description of Scapegoat rule <code>com.sksamuel.scapegoat.inspections.AvoidToMinusOne</code> at the <a href=\"https://github.com/sksamuel/scapegoat/blob/master/README.md#inspections\">Scapegoat website</a>.");
    assertThat(rule.tags()).isEmpty();
    assertThat(rule.debtRemediationFunction().baseEffort()).isEqualTo("5min");
  }

}

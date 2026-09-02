/*
 * SonarSource Scala
 * Copyright (C) 2018-2026 SonarSource Sàrl
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
package org.sonarsource.scala.externalreport.scalafix;

import org.junit.jupiter.api.Test;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class ScalafixRulesDefinitionTest {

  @Test
  void scalafix_external_repository() {
    RulesDefinition.Context context = new RulesDefinition.Context();
    ScalafixRulesDefinition rulesDefinition = new ScalafixRulesDefinition();
    rulesDefinition.define(context);

    assertThat(context.repositories()).hasSize(1);
    RulesDefinition.Repository repository = context.repository("external_scalafix");
    assertThat(repository.name()).isEqualTo("Scalafix");
    assertThat(repository.language()).isEqualTo("scala");
    assertThat(repository.isExternal()).isTrue();
    assertThat(repository.rules()).hasSize(22);

    RulesDefinition.Rule rule = repository.rule("DisableSyntax.throw");
    assertThat(rule).isNotNull();
    assertThat(rule.name()).isEqualTo("Throwing exceptions should be avoided");
    assertThat(rule.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(rule.severity()).isEqualTo("MAJOR");
    assertThat(rule.htmlDescription()).isEqualTo("" +
      "<p>Exceptions should be avoided, consider encoding the error in the return type instead.</p> " +
      "<p>See more at the <a href=\"https://scalacenter.github.io/scalafix/docs/rules/DisableSyntax.html\">Scalafix website</a>.</p>");
    assertThat(rule.tags()).isEmpty();
    assertThat(rule.debtRemediationFunction().baseEffort()).isEqualTo("5min");
  }

}

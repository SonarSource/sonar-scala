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
package org.sonarsource.scala.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;

class ScalaRulesDefinitionTest {

  @Test
  void rules() {
    RulesDefinition.Repository repository = getRepositoryForVersion(Version.create(9, 3));

    assertThat(repository.name()).isEqualTo("Sonar");
    assertThat(repository.language()).isEqualTo("scala");

    RulesDefinition.Rule rule = repository.rule("ParsingError");
    assertThat(rule).isNotNull();
    assertThat(rule.name()).isEqualTo("Scala parser failure");
    assertThat(rule.type()).isEqualTo(RuleType.CODE_SMELL);
  }

  @Test
  void owasp_security_standard_includes_2021() {
    RulesDefinition.Repository repository = getRepositoryForVersion(Version.create(9, 3));

    RulesDefinition.Rule rule = repository.rule("S1313");
    assertThat(rule).isNotNull();
    assertThat(rule.securityStandards()).containsExactlyInAnyOrder("owaspTop10:a3", "owaspTop10-2021:a1");
  }

  @Test
  void owasp_security_standard() {
    RulesDefinition.Repository repository = getRepositoryForVersion(Version.create(8, 9));

    RulesDefinition.Rule rule = repository.rule("S1313");
    assertThat(rule).isNotNull();
    assertThat(rule.securityStandards()).containsExactly("owaspTop10:a3");
  }

  private RulesDefinition.Repository getRepositoryForVersion(Version version) {
    RulesDefinition rulesDefinition = new ScalaRulesDefinition(
      SonarRuntimeImpl.forSonarQube(version, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);

    return context.repository("scala");
  }

}

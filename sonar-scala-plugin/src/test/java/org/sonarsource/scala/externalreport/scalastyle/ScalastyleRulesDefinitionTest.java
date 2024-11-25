/*
 * SonarSource Scala
 * Copyright (C) 2018-2024 SonarSource SA
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
package org.sonarsource.scala.externalreport.scalastyle;

import org.junit.jupiter.api.Test;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class ScalastyleRulesDefinitionTest {

  @Test
  void scalastyle_external_repository() {
    RulesDefinition.Context context = new RulesDefinition.Context();
    ScalastyleRulesDefinition rulesDefinition = new ScalastyleRulesDefinition();
    rulesDefinition.define(context);

    assertThat(context.repositories()).hasSize(1);
    RulesDefinition.Repository scalastyleRepository = context.repository("external_scalastyle");
    assertThat(scalastyleRepository.name()).isEqualTo("Scalastyle");
    assertThat(scalastyleRepository.language()).isEqualTo("scala");
    assertThat(scalastyleRepository.isExternal()).isTrue();
    assertThat(scalastyleRepository.rules()).hasSize(72);

    RulesDefinition.Rule rule = scalastyleRepository.rule("org.scalastyle.file.FileLengthChecker");
    assertThat(rule).isNotNull();
    assertThat(rule.name()).isEqualTo("File size limit");
    assertThat(rule.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(rule.severity()).isEqualTo("MINOR");
    assertThat(rule.htmlDescription()).isEqualTo("" +
      "<p>Files which are too long can be hard to read and understand.</p> <p>See more at the <a href=\"http://www.scalastyle.org/rules-1.0.0.html#org_scalastyle_file_FileLengthChecker\">Scalastyle website</a>.</p>");
    assertThat(rule.tags()).isEmpty();
    assertThat(rule.debtRemediationFunction().baseEffort()).isEqualTo("5min");
  }

}

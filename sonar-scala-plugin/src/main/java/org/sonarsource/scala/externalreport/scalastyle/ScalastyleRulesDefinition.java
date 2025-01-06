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
package org.sonarsource.scala.externalreport.scalastyle;

import org.sonarsource.analyzer.commons.ExternalRuleLoader;
import org.sonarsource.scala.plugin.ScalaPlugin;

public class ScalastyleRulesDefinition extends ScalastyleFamilyRulesDefinition {

  private static final String RULES_JSON = "org/sonar/l10n/scala/rules/scalastyle/rules.json";

  static final ExternalRuleLoader RULE_LOADER = new ExternalRuleLoader(
    ScalastyleSensor.LINTER_KEY, ScalastyleSensor.LINTER_NAME, RULES_JSON, ScalaPlugin.SCALA_LANGUAGE_KEY, null);

  public ScalastyleRulesDefinition() {
    super(RULE_LOADER);
  }

}

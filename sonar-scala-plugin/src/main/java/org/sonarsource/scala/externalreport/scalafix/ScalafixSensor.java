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

import java.io.IOException;
import java.io.InputStream;
import org.sonar.api.notifications.AnalysisWarnings;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;
import org.sonarsource.scala.externalreport.IssueConsumer;
import org.sonarsource.scala.externalreport.scalastyle.ScalastyleFamilySensor;

public class ScalafixSensor extends ScalastyleFamilySensor {

  static final String LINTER_KEY = "scalafix";
  static final String LINTER_NAME = "Scalafix";

  public static final String REPORT_PROPERTY_KEY = "sonar.scala.scalafix.reportPaths";

  public ScalafixSensor(AnalysisWarnings analysisWarnings) {
    super(analysisWarnings, LINTER_KEY, LINTER_NAME, REPORT_PROPERTY_KEY);
  }

  @Override
  public ExternalRuleLoader ruleLoader() {
    return ScalafixRulesDefinition.RULE_LOADER;
  }

  @Override
  protected void readReport(InputStream in, IssueConsumer consumer) throws IOException {
    ScalafixReportReader.read(in, consumer);
  }

}

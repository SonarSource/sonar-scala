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

import com.sonarsource.scanner.engine.sensor.test.fixtures.TestSensorDescriptor;
import com.sonarsource.scanner.engine.sensor.test.fixtures.TestSensorDescriptorImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.rules.RuleType;
import org.sonarsource.scala.externalreport.scalastyle.ScalastyleSensorTest;
import org.sonarsource.slang.testing.ThreadLocalLogTester;

import static org.assertj.core.api.Assertions.assertThat;

class ScalafixSensorTest {

  private final List<String> analysisWarnings = new ArrayList<>();

  @BeforeEach
  void setup() {
    analysisWarnings.clear();
  }

  @RegisterExtension
  public ThreadLocalLogTester logTester = new ThreadLocalLogTester();

  @Test
  void test_descriptor() {
    TestSensorDescriptor sensorDescriptor = new TestSensorDescriptorImpl();
    ScalafixSensor sensor = new ScalafixSensor(analysisWarnings::add);
    sensor.describe(sensorDescriptor);
    assertThat(sensorDescriptor.name()).isEqualTo("Import of Scalafix issues");
    assertThat(sensorDescriptor.languages()).containsExactly("scala");
    ScalastyleSensorTest.assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  void scalafix_issues_with_sonarqube() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("scalafix-output.txt");
    assertThat(externalIssues).hasSize(2);

    ExternalIssue first = externalIssues.get(0);
    assertThat(first.primaryLocation().inputComponent().key()).isEqualTo("project:HelloWorld.scala");
    assertThat(first.ruleKey()).hasToString("external_scalafix:DisableSyntax.isInstanceOf");
    assertThat(first.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(first.severity()).isEqualTo(Severity.MAJOR);
    assertThat(first.primaryLocation().message()).isEqualTo("isInstanceOf checks are disabled, use pattern matching instead");
    assertThat(first.primaryLocation().textRange().start().line()).isEqualTo(5);

    ExternalIssue second = externalIssues.get(1);
    assertThat(second.primaryLocation().inputComponent().key()).isEqualTo("project:HelloWorld.scala");
    assertThat(second.ruleKey()).hasToString("external_scalafix:DisableSyntax.noValPatterns");
    assertThat(second.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(second.severity()).isEqualTo(Severity.MAJOR);
    assertThat(second.primaryLocation().message()).isEqualTo("this may result in a MatchError at runtime");
    assertThat(second.primaryLocation().textRange().start().line()).isEqualTo(9);

    ScalastyleSensorTest.assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  void no_issues_without_report_paths_property() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting(null);
    assertThat(externalIssues).isEmpty();
    ScalastyleSensorTest.assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  void no_issues_with_invalid_report_path() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("invalid-path.txt");
    assertThat(externalIssues).isEmpty();
    List<String> warnings = logTester.logs(Level.WARN);
    assertThat(warnings)
      .hasSize(1)
      .hasSameSizeAs(analysisWarnings);
    assertThat(warnings.get(0))
      .startsWith("Unable to import Scalafix report file(s):")
      .contains("invalid-path.txt")
      .endsWith("The report file(s) can not be found. Check that the property 'sonar.scala.scalafix.reportPaths' is correctly configured.");
    assertThat(analysisWarnings.get(0))
      .startsWith("Unable to import 1 Scalafix report file(s).")
      .endsWith("Please check that property 'sonar.scala.scalafix.reportPaths' is correctly configured and the analysis logs for more details.");
  }

  @Test
  void silently_ignores_a_report_without_diagnostics() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("not-a-scalafix-report.txt");
    assertThat(externalIssues).isEmpty();
    ScalastyleSensorTest.assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  void scalafix_issues_with_sbt_log_prefixes() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("sbt-scalafix-output.txt");

    assertThat(externalIssues).hasSize(1);
    ExternalIssue issue = externalIssues.get(0);
    assertThat(issue.primaryLocation().inputComponent().key()).isEqualTo("project:HelloWorld.scala");
    assertThat(issue.ruleKey()).hasToString("external_scalafix:DisableSyntax.isInstanceOf");
    assertThat(issue.primaryLocation().message()).isEqualTo("isInstanceOf checks are disabled, use pattern matching instead");
    assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(5);
    ScalastyleSensorTest.assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  void issues_with_a_mix_of_valid_and_problematic_lines() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("scalafix-with-errors.txt");
    assertThat(externalIssues).hasSize(2);

    ExternalIssue first = externalIssues.get(0);
    assertThat(first.ruleKey()).hasToString("external_scalafix:DisableSyntax.isInstanceOf");
    assertThat(first.primaryLocation().message()).isEqualTo("isInstanceOf checks are disabled, use pattern matching instead");
    assertThat(first.primaryLocation().textRange().start().line()).isEqualTo(5);

    ExternalIssue second = externalIssues.get(1);
    assertThat(second.ruleKey()).hasToString("external_scalafix:Custom.BannedImport");
    assertThat(second.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(second.severity()).isEqualTo(Severity.MAJOR);
    assertThat(second.primaryLocation().message()).isEqualTo("usage of banned import");
    assertThat(second.primaryLocation().textRange().start().line()).isEqualTo(9);

    assertThat(logTester.logs(Level.ERROR)).isEmpty();
    assertThat(logTester.logs(Level.WARN)).containsExactlyInAnyOrder(
      "Fail to resolve 1 file path(s) in Scalafix report. No issues imported related to file(s): /absolute/path/to/InvalidPath.scala");
    assertThat(logTester.logs(Level.DEBUG)).containsExactlyInAnyOrder(
      "Missing information or unsupported file type for source:'DisableSyntax.var', file:'/absolute/path/to/HelloWorld.scala', message:''");
  }

  @Test
  void issues_when_report_has_a_lot_of_unresolved_files() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("scalafix-with-a-lot-of-errors.txt");
    assertThat(externalIssues).isEmpty();
    assertThat(logTester.logs(Level.ERROR)).isEmpty();
    assertThat(logTester.logs(Level.WARN)).containsExactlyInAnyOrder("" +
      "Fail to resolve 30 file path(s) in Scalafix report. No issues imported related to file(s): " +
      "/absolute/path/to/InvalidPath00.scala;/absolute/path/to/InvalidPath01.scala;/absolute/path/to/InvalidPath02.scala;" +
      "/absolute/path/to/InvalidPath03.scala;/absolute/path/to/InvalidPath04.scala;/absolute/path/to/InvalidPath05.scala;" +
      "/absolute/path/to/InvalidPath06.scala;/absolute/path/to/InvalidPath07.scala;/absolute/path/to/InvalidPath08.scala;" +
      "/absolute/path/to/InvalidPath09.scala;/absolute/path/to/InvalidPath10.scala;/absolute/path/to/InvalidPath11.scala;" +
      "/absolute/path/to/InvalidPath12.scala;/absolute/path/to/InvalidPath13.scala;/absolute/path/to/InvalidPath14.scala;" +
      "/absolute/path/to/InvalidPath15.scala;/absolute/path/to/InvalidPath16.scala;/absolute/path/to/InvalidPath17.scala;" +
      "/absolute/path/to/InvalidPath18.scala;/absolute/path/to/InvalidPath19.scala;...");
    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }

  private List<ExternalIssue> executeSensorImporting(@Nullable String fileName) throws IOException {
    return ScalastyleSensorTest.executeSensorImporting(new ScalafixSensor(analysisWarnings::add), fileName);
  }

}

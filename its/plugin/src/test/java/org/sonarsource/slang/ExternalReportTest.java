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
package org.sonarsource.slang;

import com.sonar.orchestrator.build.SonarScanner;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonarqube.ws.Issues.Issue;
import org.sonarqube.ws.client.issues.SearchRequest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
public class ExternalReportTest extends TestBase {

  private static final String BASE_DIRECTORY = "projects/externalreport/";

  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder();

  @Test
  public void scalastyle() throws IOException {
    final String projectKey = "scalastyle";

    SonarScanner sonarScanner = getSonarScanner(projectKey, BASE_DIRECTORY, "scalastyle");
    Path projectDir = new File(BASE_DIRECTORY, "scalastyle").toPath();
    Path scalastyleReportPath = createTemporaryReportFromTemplate(projectDir.resolve("scalastyle-output.xml"),
      "{ABSOLUTE_HELLO_WORLD_PATH}", projectDir.resolve("HelloWorld.scala").toRealPath().toString());
    sonarScanner.setProperty("sonar.scala.scalastyle.reportPaths", scalastyleReportPath.toString());
    ORCHESTRATOR.executeBuild(sonarScanner);
    List<Issue> issues = getExternalIssues(projectKey);
    assertThat(issues).hasSize(2);
    assertThat(issues.stream().map(Issue::getRule).sorted().collect(Collectors.toList())).containsExactly(
      "external_scalastyle:org.scalastyle.file.HeaderMatchesChecker",
      "external_scalastyle:org.scalastyle.file.RegexChecker"
    );
    assertThat(issues.stream().map(Issue::getLine).sorted().collect(Collectors.toList())).containsExactly(
      1,
      6
    );
    Issue first = issues.get(0);
    assertThat(first.getDebt()).isEqualTo("5min");
  }

  @Test
  public void scapegoat() throws IOException {
    final String projectKey = "scapegoat";

    SonarScanner sonarScanner = getSonarScanner(projectKey, BASE_DIRECTORY, "scapegoat");
    Path projectDir = new File(BASE_DIRECTORY, "scapegoat").toPath();
    Path scapegoatReportPath = createTemporaryReportFromTemplate(projectDir.resolve("scapegoat-scalastyle.xml"),
      "{ABSOLUTE_HELLO_WORLD_PATH}", projectDir.resolve("HelloWorld.scala").toRealPath().toString());
    sonarScanner.setProperty("sonar.scala.scapegoat.reportPaths", scapegoatReportPath.toString());
    ORCHESTRATOR.executeBuild(sonarScanner);
    List<Issue> issues = getExternalIssues(projectKey);
    assertThat(issues).hasSize(3);
    assertThat(issues.stream().map(Issue::getRule).sorted().collect(Collectors.toList())).containsExactly(
      "external_scapegoat:com.sksamuel.scapegoat.inspections.EmptyCaseClass",
      "external_scapegoat:com.sksamuel.scapegoat.inspections.FinalModifierOnCaseClass",
      "external_scapegoat:com.sksamuel.scapegoat.inspections.unsafe.IsInstanceOf"
    );
    assertThat(issues.stream().map(Issue::getLine).sorted().collect(Collectors.toList())).containsExactly(
      5,
      9,
      9
    );
    Issue first = issues.get(0);
    assertThat(first.getDebt()).isEqualTo("5min");
  }

  private List<Issue> getExternalIssues(String componentKey) {
    return newWsClient().issues().search(new SearchRequest().setComponentKeys(Collections.singletonList(componentKey)))
      .getIssuesList().stream()
      .filter(issue -> issue.getRule().startsWith("external_"))
      .collect(Collectors.toList());
  }

  private Path createTemporaryReportFromTemplate(Path sourceReportPath, String placeHolder, String newValue) throws IOException {
    String reportContent = new String(Files.readAllBytes(sourceReportPath), UTF_8);
    reportContent = reportContent.replace(placeHolder, newValue);
    Path destReportPath = tmpDir.newFile(sourceReportPath.getFileName().toString()).toPath().toRealPath();
    Files.write(destReportPath, reportContent.getBytes(UTF_8));
    return destReportPath;
  }

  private static String formatIssues(List<Issue> issues) {
    return issues.stream()
      .map(issue -> filePath(issue) + "|" +
        issue.getRule() + "|" +
        issue.getSeverity().name() + "|" +
        issue.getDebt() + "|" +
        "line:" + issue.getLine() + "|" +
        issue.getMessage())
      .sorted()
      .collect(Collectors.joining("\n"));
  }

  private static String filePath(Issue issue) {
    return issue.getComponent().substring(issue.getComponent().indexOf(':') + 1);
  }

}

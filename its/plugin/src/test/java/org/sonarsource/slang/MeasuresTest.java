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

import java.util.List;
import org.junit.Test;
import org.sonarqube.ws.Issues;

import static org.assertj.core.api.Assertions.assertThat;

public class MeasuresTest extends TestBase {

  private static final String BASE_DIRECTORY = "projects/measures/";

  @Test
  public void scala_measures() {
    final String projectKey = "scalaMeasures";
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY, "scala"));

    final String componentKey = projectKey + ":file.scala";
    assertThat(getMeasureAsInt(componentKey, "ncloc")).isEqualTo(8);
    assertThat(getMeasureAsInt(componentKey, "comment_lines")).isEqualTo(2);
    assertThat(getMeasure(componentKey, "ncloc_data").getValue()).isEqualTo("1=1;3=1;7=1;10=1;11=1;12=1;13=1;15=1");
    assertThat(getMeasureAsInt(componentKey, "functions")).isEqualTo(1);

    List<Issues.Issue> issuesForRule = getIssuesForRule(projectKey, "scala:S1135");
    assertThat(issuesForRule).extracting(Issues.Issue::getLine).containsExactly(9);
    assertThat(issuesForRule).extracting(Issues.Issue::getComponent).containsExactly(componentKey);
  }
}

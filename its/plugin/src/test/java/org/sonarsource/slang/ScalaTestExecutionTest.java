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
package org.sonarsource.slang;

import com.sonar.orchestrator.build.SonarScanner;
import java.util.Map;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalaTestExecutionTest extends TestBase {

  private static final String BASE_DIRECTORY = "projects/scalatest/";

  @Test
  public void imports_scalatest_generated_test_execution_metrics() {
    String projectKey = "scalaTestExecution";
    SonarScanner scanner = getSonarScanner(projectKey, BASE_DIRECTORY, "scala")
      .setProperty("sonar.sources", "src/main/scala")
      .setProperty("sonar.tests", "src/test/scala")
      .setProperty("sonar.scala.scalatest.reportPaths", "reports/scalatest");

    ORCHESTRATOR.executeBuild(scanner);

    Map<String, org.sonarqube.ws.Measures.Measure> measures = getMeasures(projectKey,
      "tests", "test_failures", "test_errors", "skipped_tests", "test_execution_time");
    assertThat(measures.get("tests").getValue()).isEqualTo("3");
    assertThat(measures.get("test_failures").getValue()).isEqualTo("2");
    assertThat(measures.get("test_errors").getValue()).isEqualTo("1");
    assertThat(measures.get("skipped_tests").getValue()).isEqualTo("1");
    assertThat(measures.get("test_execution_time").getValue()).isEqualTo("43");
  }
}

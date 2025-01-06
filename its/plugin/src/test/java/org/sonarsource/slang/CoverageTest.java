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
package org.sonarsource.slang;

import com.sonar.orchestrator.build.SonarScanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class CoverageTest extends TestBase {

  private static final Path BASE_DIRECTORY = Paths.get("projects","measures");
  private static final String ABSOLUTE_PATH_PLACEHOLDER = "{ABSOLUTE_PATH_PLACEHOLDER}";

  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder();
  private Path workDir;

  public void setUpScala() throws Exception {
    workDir = tmpDir.newFolder("scala").toPath().toRealPath();
    Path src = BASE_DIRECTORY.resolve("scala/file2.scala");
    Path srcCopy = workDir.resolve(src.getFileName());
    Files.copy(src, srcCopy);
    Path report = BASE_DIRECTORY.resolve("scala/scoverage.xml");
    String reportContent = new String(Files.readAllBytes(report), UTF_8);
    reportContent = reportContent.replace(ABSOLUTE_PATH_PLACEHOLDER, srcCopy.toString().replace("\\", "\\\\"));
    Path reportCopy = workDir.resolve("coverage/.scoverage.xml");
    Files.createDirectories(reportCopy.getParent());
    Files.write(reportCopy, reportContent.getBytes(UTF_8));
  }

  @Test
  public void scala_coverage() throws Exception {
    final String projectKey = "scalaCoverage";
    setUpScala();
    SonarScanner scalaScanner = getSonarScanner(projectKey, workDir.getParent().toString(), "scala");
    scalaScanner.setProperty("sonar.scala.coverage.reportPaths", "coverage/.scoverage.xml");

    ORCHESTRATOR.executeBuild(scalaScanner);

    String componentKey = projectKey + ":file2.scala";
    assertThat(getMeasureAsInt(componentKey, "lines_to_cover")).isEqualTo(3);
    assertThat(getMeasureAsInt(componentKey, "uncovered_lines")).isEqualTo(1);
    assertThat(getMeasureAsInt(componentKey, "conditions_to_cover")).isNull();
    assertThat(getMeasureAsInt(componentKey, "uncovered_conditions")).isNull();
  }

}

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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DuplicationsTest extends TestBase {
  private static final String BASE_DIRECTORY = "projects/duplications/";

  @Test
  public void scala_duplications() {
    final String projectKey = "scalaDuplications";
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY, "scala"));

    assertThat(getMeasureAsInt(projectKey, "duplicated_lines")).isEqualTo(79);
    assertThat(getMeasureAsInt(projectKey, "duplicated_blocks")).isEqualTo(5);
    assertThat(getMeasureAsInt(projectKey, "duplicated_files")).isEqualTo(2);
    assertThat(getMeasure(projectKey, "duplicated_lines_density").getValue()).isEqualTo("64.2");
  }

}

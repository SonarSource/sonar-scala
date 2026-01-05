/*
 * SonarSource Scala
 * Copyright (C) 2018-2026 SonarSource SA
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
package org.sonarsource.scala.checks;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.sonarsource.scala.converter.ScalaConverter;
import org.sonarsource.slang.api.ASTConverter;
import org.sonarsource.slang.checks.api.SlangCheck;

public class ScalaVerifier {

  private static final Path BASE_DIR = Paths.get("src", "test", "resources", "checks");
  private static final ASTConverter CONVERTER = new ScalaConverter();

  public static void verify(String fileName, SlangCheck check) {
    org.sonarsource.slang.testing.Verifier.verify(CONVERTER, BASE_DIR.resolve(fileName), check);
  }

  public static void verifyNoIssue(String fileName, SlangCheck check) {
    org.sonarsource.slang.testing.Verifier.verifyNoIssue(CONVERTER, BASE_DIR.resolve(fileName), check);
  }
}

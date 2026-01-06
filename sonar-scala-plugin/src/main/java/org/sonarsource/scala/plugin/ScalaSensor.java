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
package org.sonarsource.scala.plugin;

import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonarsource.scala.converter.ScalaCodeVerifier;
import org.sonarsource.scala.converter.ScalaConverter;
import org.sonarsource.slang.api.ASTConverter;
import org.sonarsource.slang.checks.CommentedCodeCheck;
import org.sonarsource.slang.checks.api.SlangCheck;
import org.sonarsource.slang.plugin.SlangSensor;

public class ScalaSensor extends SlangSensor {

  private final Checks<SlangCheck> checks;

  public ScalaSensor(SonarRuntime sonarRuntime, CheckFactory checkFactory, FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter, ScalaLanguage language) {
    super(sonarRuntime, noSonarFilter, fileLinesContextFactory, language);
    checks = checkFactory.create(ScalaPlugin.SCALA_REPOSITORY_KEY);
    checks.addAnnotatedChecks((Iterable<?>) ScalaCheckList.checks());
    checks.addAnnotatedChecks(new CommentedCodeCheck(new ScalaCodeVerifier()));
  }

  @Override
  protected ASTConverter astConverter(SensorContext sensorContext) {
    return new ScalaConverter();
  }

  @Override
  protected Checks<SlangCheck> checks() {
    return checks;
  }

  @Override
  protected String repositoryKey() {
    return ScalaPlugin.SCALA_REPOSITORY_KEY;
  }

}

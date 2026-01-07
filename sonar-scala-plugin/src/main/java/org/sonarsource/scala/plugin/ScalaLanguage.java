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

import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public class ScalaLanguage extends AbstractLanguage {

  private Configuration configuration;

  public ScalaLanguage(Configuration configuration) {
    super(ScalaPlugin.SCALA_LANGUAGE_KEY, ScalaPlugin.SCALA_LANGUAGE_NAME);
    this.configuration = configuration;
  }

  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = configuration.getStringArray(ScalaPlugin.SCALA_FILE_SUFFIXES_KEY);
    if (suffixes.length == 0) {
      suffixes = ScalaPlugin.SCALA_FILE_SUFFIXES_DEFAULT_VALUE.split(",");
    }
    return suffixes;
  }

}

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
package org.sonarsource.scala.plugin;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

public class ScalaProfileDefinition implements BuiltInQualityProfilesDefinition {

  static final String PATH_TO_JSON = "org/sonar/l10n/scala/rules/scala/Sonar_way_profile.json";

  @Override
  public void define(BuiltInQualityProfilesDefinition.Context context) {
    NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(ScalaPlugin.PROFILE_NAME, ScalaPlugin.SCALA_LANGUAGE_KEY);
    BuiltInQualityProfileJsonLoader.load(profile, ScalaPlugin.SCALA_REPOSITORY_KEY, PATH_TO_JSON);
    profile.done();
  }

}

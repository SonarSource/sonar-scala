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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InstalledPluginTest extends TestBase {

  /**
   * SonarQube Community Build versions before 26.11 bundle their own Scala analyzer. Make sure that the
   * integration tests run against the plugin built by this project, and not the bundled one.
   */
  @Test
  public void installs_the_plugin_built_by_this_project() {
    String builtPlugin = Tests.builtPlugin("scala").getFile().getName();

    String installedPlugins = ORCHESTRATOR.getServer().newHttpCall("api/plugins/installed").setAdminCredentials().execute().getBodyAsString();

    assertThat(installedPlugins).contains("\"filename\":\"" + builtPlugin + "\"");
  }
}

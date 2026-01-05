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
package org.sonarsource.slang;

import com.sonar.orchestrator.container.Edition;
import com.sonar.orchestrator.junit4.OrchestratorRule;
import com.sonar.orchestrator.junit4.OrchestratorRuleBuilder;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.Location;
import com.sonar.orchestrator.locator.MavenLocation;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  CoverageTest.class,
  DuplicationsTest.class,
  ExternalReportTest.class,
  MeasuresTest.class,
  NoSonarTest.class,
})
public class Tests {

  static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE";

  private static final Set<String> LANGUAGES = new HashSet<>(Collections.singletonList("scala"));

  @ClassRule
  public static final OrchestratorRule ORCHESTRATOR;

  static {
    OrchestratorRuleBuilder orchestratorBuilder = OrchestratorRule.builderEnv();
    addLanguagePlugins(orchestratorBuilder);
    ORCHESTRATOR = orchestratorBuilder
      .useDefaultAdminCredentialsForBuilds(true)
      .setEdition(Edition.ENTERPRISE_LW)
      .activateLicense()
      .setSonarVersion(System.getProperty(SQ_VERSION_PROPERTY, DEFAULT_SQ_VERSION))
      .restoreProfileAtStartup(FileLocation.of("src/test/resources/nosonar-scala.xml"))
      .restoreProfileAtStartup(FileLocation.of("src/test/resources/norule.xml"))
      .build();
  }

  static void addLanguagePlugins(OrchestratorRuleBuilder builder) {
    String slangVersion = System.getProperty("slangVersion");

    LANGUAGES.forEach(language -> {
      Location pluginLocation;
      String plugin = "sonar-" + language +"-plugin";
      if (StringUtils.isEmpty(slangVersion)) {
        // use the plugin that was built on local machine
        pluginLocation = FileLocation.byWildcardMavenFilename(new File("../../" + plugin + "/build/libs"), plugin + "-*-all.jar");
      } else {
        // QA environment downloads the plugin built by the CI job
        pluginLocation = MavenLocation.of("org.sonarsource.slang", plugin, slangVersion);
      }

      builder.addPlugin(pluginLocation);
    });
  }

}

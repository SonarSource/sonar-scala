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

import com.google.common.io.Files;
import com.sonar.orchestrator.container.Edition;
import com.sonar.orchestrator.junit4.OrchestratorRule;
import com.sonar.orchestrator.junit4.OrchestratorRuleBuilder;
import com.sonar.orchestrator.locator.Locators;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.assertj.core.groups.Tuple;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonarsource.sonarlint.core.analysis.AnalysisEngine;
import org.sonarsource.sonarlint.core.analysis.api.ActiveRule;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisConfiguration;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisEngineConfiguration;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFile;
import org.sonarsource.sonarlint.core.analysis.api.ClientModuleFileSystem;
import org.sonarsource.sonarlint.core.analysis.api.ClientModuleInfo;
import org.sonarsource.sonarlint.core.analysis.api.Issue;
import org.sonarsource.sonarlint.core.analysis.command.AnalyzeCommand;
import org.sonarsource.sonarlint.core.analysis.command.RegisterModuleCommand;
import org.sonarsource.sonarlint.core.commons.api.SonarLanguage;
import org.sonarsource.sonarlint.core.commons.log.LogOutput;
import org.sonarsource.sonarlint.core.commons.log.SonarLintLogger;
import org.sonarsource.sonarlint.core.commons.progress.ProgressMonitor;
import org.sonarsource.sonarlint.core.plugin.commons.PluginsLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class SonarLintTest {
  @ClassRule
  public static TemporaryFolder temp = new TemporaryFolder();

  private static final String MODULE_KEY = "myModule";
  private static final LogOutput NOOP_LOG_OUTPUT = new LogOutput() {
    @Override
    public void log(@Nullable String formattedMessage, @Nonnull Level level, @Nullable String stacktrace) {
      // ignore
    }
  };
  private final ProgressMonitor progressMonitor = new ProgressMonitor(null);

  private static AnalysisEngine sonarlintEngine;

  @Rule
  public TemporaryFolder baseDir = new TemporaryFolder();

  @BeforeClass
  public static void prepare() throws Exception {
    SonarLintLogger.setTarget(NOOP_LOG_OUTPUT);
    var pluginJarLocations = getPluginJarLocations();
    var pluginConfiguration = new PluginsLoader.Configuration(pluginJarLocations, Set.of(SonarLanguage.SCALA), false, Optional.empty());
    // Closing pluginLoader here in prepare would make analysisEngine see 0 plugins during analysis
    var pluginLoader = new PluginsLoader().load(pluginConfiguration, Set.of());
    var analysisEngineConfiguration = AnalysisEngineConfiguration.builder()
      .setWorkDir(temp.newFolder().toPath())
      .build();
    var loadedPlugins = pluginLoader.getLoadedPlugins();

    sonarlintEngine = new AnalysisEngine(analysisEngineConfiguration, loadedPlugins, NOOP_LOG_OUTPUT);
  }

  private static @Nonnull Set<Path> getPluginJarLocations() {
    // Orchestrator is used only to retrieve plugin artifacts from filesystem or maven
    OrchestratorRuleBuilder orchestratorBuilder = OrchestratorRule.builderEnv();
    Tests.addLanguagePlugins(orchestratorBuilder);
    OrchestratorRule orchestrator = orchestratorBuilder
      .useDefaultAdminCredentialsForBuilds(true)
      .setEdition(Edition.ENTERPRISE_LW)
      .activateLicense()
      .setSonarVersion(System.getProperty(Tests.SQ_VERSION_PROPERTY, Tests.DEFAULT_SQ_VERSION))
      .build();

    Locators locators = orchestrator.getOrchestrator().getLocators();

    return orchestrator.getDistribution().getPluginLocations().stream()
      .filter(location -> !location.toString().contains("sonar-reset-data-plugin"))
      .map(plugin -> locators.locate(plugin).toPath())
      .collect(Collectors.toSet());
  }

  @AfterClass
  public static void stop() {
    SonarLintLogger.setTarget(null);
    sonarlintEngine.stop();
  }

  @Test
  public void test_scala() throws Exception {
    ClientInputFile inputFile = prepareInputFile("foo.scala",
      """
      object Code {
        def foo_bar() = {                   // scala:S100 (Method name)
          if (true) {                       // scala:S1145 (Useless if(true))
                val password = "blabla"     // scala:S181 (Unused variable)
          }
        }
      }""",
      false, "scala");

    String path = inputFile.uri().getPath();
    assertIssues(analyzeWithSonarLint(inputFile),
        tuple("scala:S100", 2, path),
        tuple("scala:S1145", 3, path),
        tuple("scala:S1481", 4, path)
      );
  }

  @Test
  public void test_scala_nosonar() throws Exception {
    ClientInputFile scalaInputFile = prepareInputFile("foo.scala",
      """
      package main
      object Code {
        def foo_bar() = { // NOSONAR                 // skipped scala:S100 (Method name)
          if (true) { // NOSONAR                     // skipped scala:S1145 (Useless if(true))
              val password = "blabla" // NOSONAR     // skipped scala:S181 (Unused variable)
          }
        }
      }""",
      false, "scala");
    assertThat(analyzeWithSonarLint(scalaInputFile)).isEmpty();
  }

  private List<Issue> analyzeWithSonarLint(ClientInputFile inputFile) throws ExecutionException, InterruptedException {
    ClientModuleFileSystem clientFileSystem = new TestClientModuleFileSystem(inputFile);
    sonarlintEngine.post(new RegisterModuleCommand(new ClientModuleInfo(MODULE_KEY, clientFileSystem)), progressMonitor).get();
    var languageKey = SonarLanguage.SCALA.name();
    var analysisConfiguration = AnalysisConfiguration.builder()
            .setBaseDir(baseDir.getRoot().toPath())
            .addInputFile(inputFile)
            .addActiveRules(
                    new ActiveRule("scala:S100", languageKey),
                    new ActiveRule("scala:S1145", languageKey),
                    new ActiveRule("scala:S1481", languageKey))
            .build();
    List<Issue> issues = new ArrayList<>();
    var analyzeCommand = new AnalyzeCommand(MODULE_KEY, analysisConfiguration, issues::add, NOOP_LOG_OUTPUT);
    sonarlintEngine.post(analyzeCommand, progressMonitor).get();
    return issues;
  }

  private void assertIssues(List<Issue> issues, Tuple... expectedIssues) {
    assertThat(issues)
      .extracting(Issue::getRuleKey, Issue::getStartLine, issue -> issue.getInputFile().uri().getPath())
      .containsExactlyInAnyOrder(expectedIssues);
  }

  private ClientInputFile prepareInputFile(String relativePath, String content, final boolean isTest, String language) throws IOException {
    File file = baseDir.newFile(relativePath);
    FileUtils.write(file, content, StandardCharsets.UTF_8);
    return new TestClientInputFile(baseDir.getRoot().toPath(), file.toPath(), isTest, language);
  }

  private record TestClientInputFile(Path base, Path path, boolean isTest, String fileLanguage) implements ClientInputFile {
    @Override
    public String getPath() {
      return path.toString();
    }

    @Override
    public String relativePath() {
      return base.relativize(path).toString();
    }

    @Override
    public URI uri() {
      return path.toUri();
    }

    @Override
    public boolean isTest() {
      return isTest;
    }

    @Override
    public Charset getCharset() {
      return StandardCharsets.UTF_8;
    }

    @Override
    public <G> G getClientObject() {
      return null;
    }

    @Override
    public InputStream inputStream() throws IOException {
      return new FileInputStream(path.toFile());
    }

    @Override
    public String contents() throws IOException {
      return Files.asCharSource(path.toFile(), StandardCharsets.UTF_8).read();
    }
  }

  private record TestClientModuleFileSystem(ClientInputFile inputFile) implements ClientModuleFileSystem {
    @Override
    public Stream<ClientInputFile> files(String s, InputFile.Type type) {
      return Stream.of(inputFile);
    }

    @Override
    public Stream<ClientInputFile> files() {
      return Stream.of(inputFile);
    }
  }
}

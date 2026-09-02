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

import com.sonarsource.scanner.engine.sensor.test.fixtures.SensorContextTester;
import com.sonarsource.scanner.engine.sensor.test.fixtures.TestInputFile;
import com.sonarsource.scanner.engine.sensor.test.fixtures.TestInputFileBuilder;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.scanner.plugin.api.impl.fs.DefaultFileSystem;
import org.sonarsource.slang.testing.ThreadLocalLogTester;

import static org.assertj.core.api.Assertions.assertThat;

class ScalaTestSensorTest {

  private static final Path REPORTS_DIRECTORY = Paths.get("src", "test", "resources", "scalatest").toAbsolutePath();
  private static final Path TESTSUITES_DIRECTORY = Paths.get("src", "test", "resources", "scalatest-testsuites").toAbsolutePath();
  private static final String MODULE_KEY = "scalatest-tests";
  private static final String SAMPLE_SPEC = "src/test/scala/com/example/SampleSpec.scala";
  private static final List<String> ANALYSIS_WARNINGS = new ArrayList<>();

  @RegisterExtension
  public ThreadLocalLogTester logTester = new ThreadLocalLogTester();

  @BeforeEach
  void setUp() {
    ANALYSIS_WARNINGS.clear();
  }

  @Test
  void imports_core_metrics_from_scalatest_xml() {
    SensorContextTester context = contextFor(REPORTS_DIRECTORY.resolve("TEST-com.example.SampleSpec.xml").toString());

    newSensor().execute(context);

    assertThat(measure(context, SAMPLE_SPEC, CoreMetrics.TESTS).value()).isEqualTo(3);
    assertThat(measure(context, SAMPLE_SPEC, CoreMetrics.TEST_FAILURES).value()).isEqualTo(1);
    assertThat(measure(context, SAMPLE_SPEC, CoreMetrics.TEST_ERRORS).value()).isEqualTo(1);
    assertThat(measure(context, SAMPLE_SPEC, CoreMetrics.SKIPPED_TESTS).value()).isEqualTo(1);
    assertThat(measure(context, SAMPLE_SPEC, CoreMetrics.TEST_EXECUTION_TIME).value()).isEqualTo(1000L);
  }

  @Test
  void imports_xml_files_in_a_configured_directory() {
    SensorContextTester context = contextFor(REPORTS_DIRECTORY.toString());

    newSensor().execute(context);

    assertThat(measure(context, SAMPLE_SPEC, CoreMetrics.TESTS).value()).isEqualTo(3);
  }

  @Test
  void imports_and_aggregates_multiple_suites_under_a_testsuites_root() {
    SensorContextTester context = contextFor(TESTSUITES_DIRECTORY.resolve("TEST-multiple-suites.xml").toString());

    newSensor().execute(context);

    assertThat(measure(context, "src/test/scala/com/example/FirstSpec.scala", CoreMetrics.TESTS).value()).isEqualTo(2);
    assertThat(measure(context, "src/test/scala/com/example/FirstSpec.scala", CoreMetrics.TEST_FAILURES).value()).isEqualTo(1);
    assertThat(measure(context, "src/test/scala/com/example/SecondSpec.scala", CoreMetrics.TESTS).value()).isEqualTo(2);
    assertThat(measure(context, "src/test/scala/com/example/SecondSpec.scala", CoreMetrics.TEST_ERRORS).value()).isEqualTo(1);
    assertThat(measure(context, "src/test/scala/com/example/SecondSpec.scala", CoreMetrics.SKIPPED_TESTS).value()).isEqualTo(1);
    assertThat(measure(context, "src/test/scala/com/example/SecondSpec.scala", CoreMetrics.TEST_EXECUTION_TIME).value()).isEqualTo(300L);
  }

  @Test
  void does_not_double_count_errors_when_testcase_and_suite_error_counts_differ() {
    Path partialAbortReport = Paths.get("src", "test", "resources", "scalatest-suite-errors", "TEST-com.example.PartialAbortSpec.xml").toAbsolutePath();
    SensorContextTester context = contextFor(partialAbortReport.toString());

    newSensor().execute(context);

    assertThat(measure(context, "src/test/scala/com/example/PartialAbortSpec.scala", CoreMetrics.TESTS).value()).isEqualTo(1);
    assertThat(measure(context, "src/test/scala/com/example/PartialAbortSpec.scala", CoreMetrics.TEST_ERRORS).value()).isEqualTo(2);
  }

  @Test
  void does_not_save_metrics_when_no_report_could_be_imported() {
    SensorContextTester context = contextFor(REPORTS_DIRECTORY.resolve("missing.xml").toString());

    newSensor().execute(context);

    assertThat(measure(context, SAMPLE_SPEC, CoreMetrics.TESTS)).isNull();
    assertThat(measure(context, SAMPLE_SPEC, CoreMetrics.TEST_EXECUTION_TIME)).isNull();
    assertThat(ANALYSIS_WARNINGS).hasSize(1);
    assertThat(logTester.logs()).anyMatch(message -> message.contains("missing.xml"));
  }

  @Test
  void warns_and_continues_when_a_report_is_missing() {
    SensorContextTester context = contextFor(REPORTS_DIRECTORY.resolve("missing.xml") + "," + REPORTS_DIRECTORY.resolve("TEST-com.example.SampleSpec.xml"));

    newSensor().execute(context);

    assertThat(measure(context, SAMPLE_SPEC, CoreMetrics.TESTS).value()).isEqualTo(3);
    assertThat(ANALYSIS_WARNINGS).hasSize(1);
    assertThat(logTester.logs()).anyMatch(message -> message.contains("missing.xml"));
  }

  @Test
  void warns_and_continues_when_a_report_is_malformed() {
    Path malformedReport = Paths.get("src", "test", "resources", "scalatest-invalid", "invalid.xml").toAbsolutePath();
    SensorContextTester context = contextFor(malformedReport + "," + REPORTS_DIRECTORY.resolve("TEST-com.example.SampleSpec.xml"));

    newSensor().execute(context);

    assertThat(measure(context, SAMPLE_SPEC, CoreMetrics.TESTS).value()).isEqualTo(3);
    assertThat(ANALYSIS_WARNINGS).hasSize(1);
    assertThat(logTester.logs()).anyMatch(message -> message.contains("invalid.xml"));
  }

  @Test
  void warns_and_continues_when_a_report_has_an_invalid_duration() {
    Path overflowingDurationReport = Paths.get("src", "test", "resources", "scalatest-invalid", "overflow-duration.xml").toAbsolutePath();
    SensorContextTester context = contextFor(overflowingDurationReport + "," + REPORTS_DIRECTORY.resolve("TEST-com.example.SampleSpec.xml"));

    newSensor().execute(context);

    assertThat(measure(context, SAMPLE_SPEC, CoreMetrics.TESTS).value()).isEqualTo(3);
    assertThat(ANALYSIS_WARNINGS).hasSize(1);
    assertThat(logTester.logs()).anyMatch(message -> message.contains("overflow-duration.xml"));
  }

  @Test
  void describes_the_sensor() {
    com.sonarsource.scanner.engine.sensor.test.fixtures.TestSensorDescriptor descriptor =
      new com.sonarsource.scanner.engine.sensor.test.fixtures.TestSensorDescriptorImpl();

    newSensor().describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("ScalaTest sensor for Scala test execution");
  }

  private static ScalaTestSensor newSensor() {
    return new ScalaTestSensor(ANALYSIS_WARNINGS::add);
  }

  private static SensorContextTester contextFor(String reportPaths) {
    SensorContextTester context = SensorContextTester.create(REPORTS_DIRECTORY);
    context.settings().setProperty(ScalaTestSensor.REPORT_PROPERTY_KEY, reportPaths);
    DefaultFileSystem fileSystem = new DefaultFileSystem(new File(MODULE_KEY));
    addTestFile(fileSystem, SAMPLE_SPEC);
    addTestFile(fileSystem, "src/test/scala/com/example/FirstSpec.scala");
    addTestFile(fileSystem, "src/test/scala/com/example/SecondSpec.scala");
    addTestFile(fileSystem, "src/test/scala/com/example/PartialAbortSpec.scala");
    context.setFileSystem(fileSystem);
    return context;
  }

  private static void addTestFile(DefaultFileSystem fileSystem, String relativePath) {
    TestInputFile inputFile = TestInputFileBuilder.create(MODULE_KEY, relativePath)
      .setType(InputFile.Type.TEST)
      .initMetadata("object Test")
      .setContents("object Test")
      .build();
    fileSystem.add(inputFile);
  }

  private static <T extends java.io.Serializable> Measure<T> measure(SensorContextTester context, String relativePath, org.sonar.api.measures.Metric<T> metric) {
    return context.measure(MODULE_KEY + ":" + relativePath, metric);
  }
}

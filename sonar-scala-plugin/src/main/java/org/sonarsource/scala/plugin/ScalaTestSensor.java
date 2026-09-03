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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.measure.NewMeasure;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.notifications.AnalysisWarnings;
import org.sonarsource.analyzer.commons.xml.SafeStaxParserFactory;

/** Imports the JUnit-style XML emitted by ScalaTest's XML reporter. */
public class ScalaTestSensor implements Sensor {

  public static final String REPORT_PROPERTY_KEY = "sonar.scala.scalatest.reportPaths";

  private static final Logger LOG = LoggerFactory.getLogger(ScalaTestSensor.class);
  private static final QName TESTSUITE = new QName("testsuite");
  private static final QName TESTSUITES = new QName("testsuites");
  private static final QName TESTCASE = new QName("testcase");
  private static final QName FAILURE = new QName("failure");
  private static final QName ERROR = new QName("error");
  private static final QName SKIPPED = new QName("skipped");
  private static final QName TIME = new QName("time");
  private static final QName ERRORS = new QName("errors");
  private static final QName NAME = new QName("name");

  private final AnalysisWarnings analysisWarnings;

  public ScalaTestSensor(AnalysisWarnings analysisWarnings) {
    this.analysisWarnings = analysisWarnings;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(ScalaPlugin.SCALA_LANGUAGE_KEY)
      .onlyWhenConfiguration(conf -> conf.hasKey(REPORT_PROPERTY_KEY))
      .name("ScalaTest sensor for Scala test execution");
  }

  @Override
  public void execute(SensorContext context) {
    Map<InputFile, TestMetrics> metricsByFile = new HashMap<>();
    for (File report : reportFiles(context)) {
      importReport(report, context, metricsByFile);
    }
    metricsByFile.forEach((inputFile, metrics) -> saveMetrics(context, inputFile, metrics));
  }

  private Set<File> reportFiles(SensorContext context) {
    Set<File> reports = new LinkedHashSet<>();
    for (String configuredPath : context.config().getStringArray(REPORT_PROPERTY_KEY)) {
      File configured = context.fileSystem().resolvePath(configuredPath);
      if (!configured.exists()) {
        warn("ScalaTest report path '" + configured + "' does not exist.");
      } else if (configured.isDirectory()) {
        File[] xmlFiles = configured.listFiles(file -> file.isFile() && file.getName().endsWith(".xml"));
        if (xmlFiles != null) {
          Arrays.sort(xmlFiles, Comparator.comparing(File::getAbsolutePath));
          reports.addAll(Arrays.asList(xmlFiles));
        }
      } else {
        reports.add(configured);
      }
    }
    return reports;
  }

  private void importReport(File report, SensorContext context, Map<InputFile, TestMetrics> metricsByFile) {
    try {
      for (TestMetrics suiteMetrics : parseReport(report)) {
        addSuiteMetrics(context, suiteMetrics, metricsByFile);
      }
    } catch (IOException | XMLStreamException | NumberFormatException | ArithmeticException e) {
      warn("Unable to import ScalaTest report '" + report + "': " + e.getMessage());
    }
  }

  private void addSuiteMetrics(SensorContext context, TestMetrics suiteMetrics, Map<InputFile, TestMetrics> metricsByFile) {
    InputFile inputFile = inputFileForSuite(context, suiteMetrics.suiteName);
    if (inputFile == null) {
      warn("Unable to resolve ScalaTest suite '" + suiteMetrics.suiteName + "' to a test file.");
      return;
    }
    metricsByFile.computeIfAbsent(inputFile, ignored -> new TestMetrics()).add(suiteMetrics);
  }

  private static InputFile inputFileForSuite(SensorContext context, String suiteName) {
    String suitePath = suiteName.replace('.', '/') + ".scala";
    InputFile resolved = null;
    for (InputFile inputFile : context.fileSystem().inputFiles(context.fileSystem().predicates().hasType(InputFile.Type.TEST))) {
      String relativePath = inputFile.relativePath().replace('\\', '/');
      if (relativePath.equals(suitePath) || relativePath.endsWith('/' + suitePath)) {
        if (resolved != null) {
          return null;
        }
        resolved = inputFile;
      }
    }
    return resolved;
  }

  private static void saveMetrics(SensorContext context, InputFile inputFile, TestMetrics metrics) {
    saveMeasure(context, inputFile, CoreMetrics.TESTS, metrics.tests);
    saveMeasure(context, inputFile, CoreMetrics.TEST_FAILURES, metrics.failures);
    saveMeasure(context, inputFile, CoreMetrics.TEST_ERRORS, metrics.errors);
    saveMeasure(context, inputFile, CoreMetrics.SKIPPED_TESTS, metrics.skipped);
    saveMeasure(context, inputFile, CoreMetrics.TEST_EXECUTION_TIME, metrics.durationInMilliseconds);
  }

  private static <T extends java.io.Serializable> void saveMeasure(SensorContext context, InputFile inputFile, Metric<T> metric, T value) {
    NewMeasure<T> measure = context.newMeasure();
    measure.on(inputFile).forMetric(metric).withValue(value).save();
  }

  private static List<TestMetrics> parseReport(File report) throws IOException, XMLStreamException {
    List<TestMetrics> completedSuites = new ArrayList<>();
    try (InputStream input = new FileInputStream(report)) {
      XMLEventReader reader = SafeStaxParserFactory.createXMLInputFactory().createXMLEventReader(input);
      boolean rootSeen = false;
      TestCase currentTestCase = null;
      Deque<TestMetrics> suiteMetrics = new ArrayDeque<>();
      while (reader.hasNext()) {
        XMLEvent event = reader.nextEvent();
        if (event.isStartElement()) {
          StartElement element = event.asStartElement();
          QName name = element.getName();
          if (!rootSeen) {
            if (!TESTSUITE.equals(name) && !TESTSUITES.equals(name)) {
              throw new XMLStreamException("Expected a testsuite or testsuites root element.");
            }
            rootSeen = true;
            if (TESTSUITE.equals(name)) {
              suiteMetrics.push(new TestMetrics(suiteName(element), errorsCount(element)));
            }
          } else if (TESTSUITE.equals(name)) {
            suiteMetrics.push(new TestMetrics(suiteName(element), errorsCount(element)));
          } else if (TESTCASE.equals(name)) {
            if (suiteMetrics.isEmpty()) {
              throw new XMLStreamException("A testcase must belong to a testsuite.");
            }
            currentTestCase = new TestCase(durationInMilliseconds(element));
          } else if (currentTestCase != null && FAILURE.equals(name)) {
            currentTestCase.status = Status.FAILURE;
          } else if (currentTestCase != null && ERROR.equals(name)) {
            currentTestCase.status = Status.ERROR;
          } else if (currentTestCase != null && SKIPPED.equals(name)) {
            currentTestCase.status = Status.SKIPPED;
          }
        } else if (event.isEndElement()) {
          QName name = event.asEndElement().getName();
          if (TESTCASE.equals(name) && currentTestCase != null) {
            suiteMetrics.peek().add(currentTestCase);
            currentTestCase = null;
          } else if (TESTSUITE.equals(name)) {
            TestMetrics completedSuite = suiteMetrics.pop();
            completedSuite.applyReportedErrors();
            if (suiteMetrics.isEmpty()) {
              completedSuites.add(completedSuite);
            } else {
              suiteMetrics.peek().add(completedSuite);
            }
          }
        }
      }
      if (!rootSeen) {
        throw new XMLStreamException("The report is empty.");
      }
    }
    return completedSuites;
  }

  private static String suiteName(StartElement suite) throws XMLStreamException {
    Attribute name = suite.getAttributeByName(NAME);
    if (name == null || name.getValue().isEmpty()) {
      throw new XMLStreamException("A testsuite must have a name attribute.");
    }
    return name.getValue();
  }

  private static int errorsCount(StartElement suite) {
    Attribute errors = suite.getAttributeByName(ERRORS);
    return errors == null ? 0 : Integer.parseInt(errors.getValue());
  }

  private static long durationInMilliseconds(StartElement testCase) {
    Attribute time = testCase.getAttributeByName(TIME);
    if (time == null || time.getValue().isEmpty()) {
      return 0L;
    }
    return new BigDecimal(time.getValue()).movePointRight(3).setScale(0, RoundingMode.HALF_UP).longValueExact();
  }

  private void warn(String message) {
    LOG.warn(message);
    analysisWarnings.addUnique(message);
  }

  private enum Status { SUCCESS, FAILURE, ERROR, SKIPPED }

  private static final class TestCase {
    private final long durationInMilliseconds;
    private Status status = Status.SUCCESS;

    private TestCase(long durationInMilliseconds) {
      this.durationInMilliseconds = durationInMilliseconds;
    }
  }

  private static final class TestMetrics {
    private final String suiteName;
    private final int reportedErrors;
    private int tests;
    private int failures;
    private int errors;
    private int skipped;
    private long durationInMilliseconds;

    private TestMetrics() {
      this(null, 0);
    }

    private TestMetrics(@Nullable String suiteName, int reportedErrors) {
      this.suiteName = suiteName;
      this.reportedErrors = reportedErrors;
    }

    private void add(TestCase testCase) {
      durationInMilliseconds += testCase.durationInMilliseconds;
      if (testCase.status == Status.SKIPPED) {
        skipped++;
        return;
      }
      tests++;
      switch (testCase.status) {
        case FAILURE:
          failures++;
          break;
        case ERROR:
          errors++;
          break;
        default:
          break;
      }
    }

    private void add(TestMetrics other) {
      tests += other.tests;
      failures += other.failures;
      errors += other.errors;
      skipped += other.skipped;
      durationInMilliseconds += other.durationInMilliseconds;
    }

    private void applyReportedErrors() {
      errors = Math.max(errors, reportedErrors);
      tests = Math.max(tests, failures + errors);
    }
  }
}

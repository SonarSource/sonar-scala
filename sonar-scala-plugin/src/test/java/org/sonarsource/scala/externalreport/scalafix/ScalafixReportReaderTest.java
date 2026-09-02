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
package org.sonarsource.scala.externalreport.scalafix;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class ScalafixReportReaderTest {

  @Test
  void reads_a_single_diagnostic_with_snippet_and_carets() throws IOException {
    List<String[]> issues = read(
      """
      Main.scala:6:3: error: [DisableSyntax.throw]: exceptions should be avoided
      throw new IllegalArgumentException
      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      """);

    assertThat(issues).hasSize(1);
    assertThat(issues.get(0)).containsExactly("Main.scala", "6", "DisableSyntax.throw", "exceptions should be avoided");
  }

  @Test
  void reads_consecutive_diagnostics_without_blank_lines_between_them() throws IOException {
    List<String[]> issues = read(
      """
      Main.scala:6:3: error: [DisableSyntax.throw]: exceptions should be avoided
      throw new IllegalArgumentException
      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      Main.scala:9:5: warning: [DisableSyntax.var]: mutable state should be avoided
      var x = 1
      ^^^^^^^^^
      """);

    assertThat(issues).hasSize(2);
    assertThat(issues.get(0)).containsExactly("Main.scala", "6", "DisableSyntax.throw", "exceptions should be avoided");
    assertThat(issues.get(1)).containsExactly("Main.scala", "9", "DisableSyntax.var", "mutable state should be avoided");
  }

  @Test
  void recognizes_error_warning_and_info_severities() throws IOException {
    List<String[]> issues = read(
      """
      Main.scala:1:1: error: [Rule.a]: message a
      Main.scala:2:1: warning: [Rule.b]: message b
      Main.scala:3:1: info: [Rule.c]: message c
      """);

    assertThat(issues).extracting(i -> i[2]).containsExactly("Rule.a", "Rule.b", "Rule.c");
  }

  @Test
  void ignores_lines_without_a_bracketed_rule_id() throws IOException {
    List<String[]> issues = read(
      """
      Running scalafix v0.11.0...
      error: Unknown rule 'FooBar'
      Main.scala:1:1: error: [Rule.a]: message a
      """);

    assertThat(issues).hasSize(1);
    assertThat(issues.get(0)).containsExactly("Main.scala", "1", "Rule.a", "message a");
  }

  @Test
  void reads_a_diagnostic_with_an_empty_message() throws IOException {
    List<String[]> issues = read("Main.scala:1:1: error: [Rule.a]:\n");

    assertThat(issues).hasSize(1);
    assertThat(issues.get(0)).containsExactly("Main.scala", "1", "Rule.a", "");
  }

  @Test
  void handles_windows_style_absolute_paths_with_a_drive_letter() throws IOException {
    List<String[]> issues = read("C:\\project\\Main.scala:5:9: error: [DisableSyntax.var]: mutable state should be avoided\n");

    assertThat(issues).hasSize(1);
    assertThat(issues.get(0)).containsExactly("C:\\project\\Main.scala", "5", "DisableSyntax.var", "mutable state should be avoided");
  }

  @Test
  void reads_diagnostics_with_sbt_log_level_prefixes() throws IOException {
    List<String[]> issues = read(
      """
      [error] /project/Main.scala:1:1: error: [Rule.error]: error message
      [warn] /project/Main.scala:2:1: warning: [Rule.warn]: warning message
      [warning] /project/Main.scala:3:1: warning: [Rule.warning]: warning message
      [info] /project/Main.scala:4:1: info: [Rule.info]: info message
      """);

    assertThat(issues)
      .hasSize(4)
      .allSatisfy(issue -> assertThat(issue[0]).isEqualTo("/project/Main.scala"))
      .extracting(issue -> issue[2])
      .containsExactly("Rule.error", "Rule.warn", "Rule.warning", "Rule.info");
  }

  @Test
  void silently_reads_nothing_from_a_file_with_no_diagnostics() throws IOException {
    List<String[]> issues = read("This is not a scalafix report.\nJust some random text.\n");
    assertThat(issues).isEmpty();
  }

  @Test
  void silently_reads_nothing_from_an_empty_or_whitespace_only_report() throws IOException {
    assertThat(read("")).isEmpty();
    assertThat(read(" \t\n\n")).isEmpty();
  }

  private static List<String[]> read(String content) throws IOException {
    List<String[]> issues = new ArrayList<>();
    ScalafixReportReader.read(new ByteArrayInputStream(content.getBytes(UTF_8)),
      (file, line, source, message) -> issues.add(new String[] {file, line, source, message}));
    return issues;
  }

}

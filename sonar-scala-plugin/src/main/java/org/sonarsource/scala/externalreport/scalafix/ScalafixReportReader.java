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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonarsource.scala.externalreport.IssueConsumer;

class ScalafixReportReader {

  private static final Pattern DIAGNOSTIC_LINE = Pattern.compile("^(?:\\[(?:error|warn|warning|info)]\\s+)?(.+):(\\d+):\\d+: (?:error|warning|info): \\[([^\\]]+)]:?\\s*(.*)$");
  private ScalafixReportReader() {
  }

  static void read(InputStream in, IssueConsumer consumer) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    String line;
    while ((line = reader.readLine()) != null) {
      Matcher matcher = DIAGNOSTIC_LINE.matcher(line);
      if (matcher.matches()) {
        String file = matcher.group(1);
        String lineNumber = matcher.group(2);
        String ruleId = matcher.group(3);
        String message = matcher.group(4);
        consumer.onIssue(file, lineNumber, ruleId, message);
      }
    }
  }

}

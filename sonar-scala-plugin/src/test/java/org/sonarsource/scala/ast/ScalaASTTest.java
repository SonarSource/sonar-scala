/*
 * SonarSource SLang
 * Copyright (C) 2018-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.scala.ast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sonarsource.scala.converter.ScalaConverter;
import org.sonarsource.slang.api.ParseException;
import org.sonarsource.slang.api.Tree;
import org.sonarsource.slang.visitors.TreePrinter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class ScalaASTTest {
  private static ScalaConverter converter = new ScalaConverter();

  @Test
  @Disabled("Used to quickly test one file")
  void test_one_file() throws IOException {
    // Replace it with the name of the file you want to test from src/test/resources/ast
    String filename = "EnumTypes.scala";
    Path scalaPath = Paths.get("src", "test", "resources", "ast", filename);
    Path astPath = Paths.get(scalaPath.toString().replaceFirst("\\.scala$", ".txt"));
    String actualAst = TreePrinter.table(parse(scalaPath));
    String expectingAst = astPath.toFile().exists() ? new String(Files.readAllBytes(astPath), UTF_8) : "";
    assertThat(actualAst)
            .describedAs("In the file: " + astPath + " (run ScalaASTTest.main manually)")
            .isEqualToIgnoringWhitespace(expectingAst);
  }

  @Test
  void all_scala_files() throws IOException {
    for (Path scalaPath : getScalaSources()) {
      Path astPath = Paths.get(scalaPath.toString().replaceFirst("\\.scala$", ".txt"));
      String actualAst = TreePrinter.table(parse(scalaPath));
      String expectingAst = astPath.toFile().exists() ? new String(Files.readAllBytes(astPath), UTF_8) : "";
      assertThat(actualAst)
        .describedAs("In the file: " + astPath + " (run ScalaASTTest.main manually)")
        .isEqualToIgnoringWhitespace(expectingAst);
    }
  }

  public static void main(String[] args) throws IOException {
    fix_all_cls_files_test_automatically();
  }

  private static void fix_all_cls_files_test_automatically() throws IOException {
    for (Path scalaPath : getScalaSources()) {
      Path astPath = Paths.get(scalaPath.toString().replaceFirst("\\.scala$", ".txt"));
      String actualAst = TreePrinter.table(parse(scalaPath));
      Files.write(astPath, actualAst.getBytes(UTF_8));
    }
  }

  private static List<Path> getScalaSources() throws IOException {
    try (Stream<Path> pathStream = Files.walk(Paths.get("src", "test", "resources", "ast"))) {
      return pathStream
        .filter(path -> !path.toFile().isDirectory() && path.getFileName().toString().endsWith(".scala"))
        .sorted()
        .collect(Collectors.toList());
    }
  }

  private static Tree parse(Path path) throws IOException {
    String code = new String(Files.readAllBytes(path), UTF_8);
    try {
      return converter.parse(code);
    } catch (ParseException e) {
      throw new ParseException(e.getMessage() + " in file " + path, e.getPosition(), e);
    } catch (RuntimeException e) {
      throw new RuntimeException(e.getClass().getSimpleName() + ": " + e.getMessage() + " in file " + path, e);
    }
  }
}

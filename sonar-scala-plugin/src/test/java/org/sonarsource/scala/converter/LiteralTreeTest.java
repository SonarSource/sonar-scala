/*
 * SonarSource Scala
 * Copyright (C) 2018-2025 SonarSource SA
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
package org.sonarsource.scala.converter;

import org.junit.jupiter.api.Test;
import org.sonarsource.slang.api.LiteralTree;
import org.sonarsource.slang.api.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarsource.slang.testing.TreeAssert.assertTree;

class LiteralTreeTest extends AbstractScalaConverterTest {

  @Test
  void string_literal() {
    assertTree(scalaStatement("\"Hello\"")).isEquivalentTo(slangStatement("\"Hello\";"));
  }

  @Test
  void string_with_interpolation() {
    Tree tree = scalaStatement("raw\"abc\"");
    assertTree(tree).isNotInstanceOf(LiteralTree.class);
    assertThat(tree.descendants()
      .filter(LiteralTree.class::isInstance)
      .map(LiteralTree.class::cast)
      .map(LiteralTree::value)).containsExactly("\"abc\"");

    //Test that string interpolation with variable are not mapped to LiteralTree
    Tree interpolationTree = scalaStatement("s\"abc $x \"");
    assertThat(interpolationTree.descendants().filter(LiteralTree.class::isInstance)).isEmpty();
  }

  @Test
  void int_literal() {
    assertTree(scalaStatement("42")).isEquivalentTo(slangStatement("42;"));
  }

  @Test
  void non_int_numeric_literals() {
    assertTree(scalaStatement("42.1")).isLiteral("42.1");
    assertTree(scalaStatement("42.1f")).isLiteral("42.1f");
    assertTree(scalaStatement("42.1d")).isLiteral("42.1d");
  }

  @Test
  void boolean_literals() {
    assertTree(scalaStatement("true")).isLiteral("true");
    assertTree(scalaStatement("false")).isLiteral("false");
  }
}

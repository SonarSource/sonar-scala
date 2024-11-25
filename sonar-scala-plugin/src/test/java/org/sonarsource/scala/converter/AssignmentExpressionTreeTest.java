/*
 * SonarSource Scala
 * Copyright (C) 2018-2024 SonarSource SA
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

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.sonarsource.slang.api.AssignmentExpressionTree;
import org.sonarsource.slang.api.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarsource.slang.testing.TreeAssert.assertTree;

class AssignmentExpressionTreeTest extends AbstractScalaConverterTest {

  @Test
  void assignment() {
    assertTree(scalaStatement("x = 42")).isEquivalentTo(slangStatement("x = 42;"));
    assertTree(scalaStatement("x += 42")).isNotEquivalentTo(slangStatement("x = 42;"));
  }

  @Test
  void named_argument() {
    assertThat(assignmentDescendants(scalaStatement("foo(param = 42)"))).isEmpty();
    assertThat(assignmentDescendants(scalaStatement("new MyClass(param = 42)"))).isEmpty();
    assertThat(assignmentDescendants(parse("class A { def this() = this(param = 42) }"))).isEmpty();
  }

  private Stream<Tree> assignmentDescendants(Tree tree) {
    return tree.descendants().filter(AssignmentExpressionTree.class::isInstance);
  }

}

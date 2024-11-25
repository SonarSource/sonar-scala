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

import org.junit.jupiter.api.Test;
import org.sonarsource.slang.api.IfTree;
import org.sonarsource.slang.api.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarsource.slang.testing.TreeAssert.assertTree;

class IfTreeTest extends AbstractScalaConverterTest {

  @Test
  void if_without_else() {
    Tree tree = scalaStatement("if (x) { 42 }");
    assertTree(tree).isEquivalentTo(slangStatement("if (x) { 42; };"));
    IfTree ifTree = (IfTree) tree;
    assertThat(ifTree.ifKeyword().text()).isEqualTo("if");
    assertThat(ifTree.elseBranch()).isNull();
    assertThat(ifTree.elseKeyword()).isNull();
  }

  @Test
  void if_with_else() {
    Tree tree = scalaStatement("if (x) { 42 } else { 43 }");
    assertTree(tree).isEquivalentTo(slangStatement("if (x) { 42; } else { 43 };"));
    IfTree ifTree = (IfTree) tree;
    assertThat(ifTree.ifKeyword().text()).isEqualTo("if");
    assertThat(ifTree.elseBranch()).isNotNull();
    assertThat(ifTree.elseKeyword().text()).isEqualTo("else");
  }
}

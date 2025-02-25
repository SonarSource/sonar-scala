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
import org.sonarsource.slang.api.NativeTree;
import org.sonarsource.slang.api.VariableDeclarationTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarsource.slang.testing.TreeAssert.assertTree;

class VariableDeclarationTreeTest extends AbstractScalaConverterTest {

  @Test
  void var_declaration() {
    assertTree(scalaStatement("var x = 42")).isEquivalentTo(slangStatement("var x = 42;"));
  }

  @Test
  void val_declaration() {
    assertTree(scalaStatement("val x = 42")).isEquivalentTo(slangStatement("val x = 42;"));
  }

  @Test
  void pattern() {
    assertTree(scalaStatement("val x :: y = my_list")).isInstanceOf(NativeTree.class);
  }

  @Test
  void modifiers() {
    assertThat(parse("object Obj { final val x = 42 }").descendants().filter(VariableDeclarationTree.class::isInstance)).isEmpty();
  }

  @Test
  void fields() {
    assertThat(scalaStatement("new { val x = 42 }").descendants().filter(VariableDeclarationTree.class::isInstance)).isEmpty();
    assertThat(scalaStatement("new { var x = 42 }").descendants().filter(VariableDeclarationTree.class::isInstance)).isEmpty();
  }
}

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
import org.sonarsource.slang.api.ReturnTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarsource.slang.testing.TreeAssert.assertTree;

class ReturnTreeTest extends AbstractScalaConverterTest {

  @Test
  void simple_return() {
    ReturnTree tree = (ReturnTree) scalaStatement("return \"foo\"");
    assertThat(tree.keyword().text()).isEqualTo("return");
    assertTree(tree.body()).isStringLiteral("foo");

    tree = (ReturnTree) scalaStatement("return foo()");
    assertThat(tree.keyword().text()).isEqualTo("return");
    assertThat(tree.body()).isInstanceOf(NativeTree.class);
  }

  @Test
  void empty_return_body() {
    ReturnTree tree = (ReturnTree) scalaStatement("return;");
    assertThat(tree.keyword().text()).isEqualTo("return");
    assertThat(tree.body()).isNull();
  }
}

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
import org.sonarsource.slang.api.NativeTree;
import org.sonarsource.slang.api.Tree;
import org.sonarsource.slang.api.UnaryExpressionTree;

import static org.sonarsource.slang.testing.TreeAssert.assertTree;

class UnaryExpressionTreeTest extends AbstractScalaConverterTest {
  @Test
  void operators() {
    Tree unaryExpressionTree = scalaStatement("!(a == 2)");
    assertTree(unaryExpressionTree).isUnaryExpression(UnaryExpressionTree.Operator.NEGATE);

    unaryExpressionTree = scalaStatement("+a");
    assertTree(unaryExpressionTree).isUnaryExpression(UnaryExpressionTree.Operator.PLUS);

    unaryExpressionTree = scalaStatement("-a");
    assertTree(unaryExpressionTree).isUnaryExpression(UnaryExpressionTree.Operator.MINUS);

    Tree tree = scalaStatement("~a");
    assertTree(tree).isInstanceOf(NativeTree.class);
  }
}

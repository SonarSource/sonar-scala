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
package org.sonarsource.scala.converter;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonarsource.slang.api.ClassDeclarationTree;
import org.sonarsource.slang.api.FunctionDeclarationTree;
import org.sonarsource.slang.api.IdentifierTree;
import org.sonarsource.slang.api.ModifierTree;
import org.sonarsource.slang.api.NativeTree;
import org.sonarsource.slang.api.ParameterTree;
import org.sonarsource.slang.api.TopLevelTree;
import org.sonarsource.slang.api.Tree;
import org.sonarsource.slang.impl.NativeTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarsource.slang.testing.TreeAssert.assertTree;

class FunctionDeclarationTreeTest extends AbstractScalaConverterTest {

  @Test
  void function() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement(
      "def foo(p1: String, p2: Int): Unit = { println(param) }");
    assertTree(func.name()).isIdentifier("foo");
    assertThat(func.formalParameters()).hasSize(2);
    assertThat(func.returnType()).isNotNull();
    assertThat(func.body().statementOrExpressions()).hasSize(1);
  }

  @Test
  void function_without_return_type() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement(
      "def foo(p1: String, p2: Int) = { println(param) }");
    assertThat(func.returnType()).isNull();
  }

  @Test
  void function_with_simple_body() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement(
      "def foo(p1: String, p2: Int) = param");
    assertThat(func.body().statementOrExpressions()).hasSize(1);
    assertTree(func.body().statementOrExpressions().get(0)).isIdentifier("param");
  }

  @Test
  void function_without_parameter_list() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement(
      "def foo = { println(param) }");
    assertThat(func.formalParameters()).isEmpty();
  }

  @Test
  void function_with_multiple_parameter_lists() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement("def foo(p1: String)(p2: Int) = { println(param) }");
    assertThat(func.formalParameters()).hasSize(2);
    assertTree(func.formalParameters().get(0)).hasTokens("p1", ":", "String");
    assertTree(func.formalParameters().get(1)).hasTokens("p2", ":", "Int");
  }

  @Test
  void function_parameters_test() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement(
        "def foo(p1: String) = param");
    assertTree(func).hasParameterNames("p1");
    assertTree(func.formalParameters().get(0)).isInstanceOf(ParameterTree.class);
  }

  @Test
  void function_multiples_parameters_test() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement(
        "def foo(p1: String, p2: String, p3: String) = {p1}");
    assertTree(func).hasParameterNames("p1", "p2", "p3");
  }

  @Test
  void function_default_parameter() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement(
        "def foo(p1: String = \"def\") = {p1}");
    assertThat(func.formalParameters()).hasSize(1);
    assertTree(func).hasParameterNames("p1");
    assertTree(func.formalParameters().get(0)).isInstanceOf(ParameterTree.class);
    ParameterTree parameter1 = (ParameterTree) func.formalParameters().get(0);
    assertTree(parameter1.defaultValue()).isLiteral("\"def\"");
  }

  @Test
  void function_multiple_parameters_with_default() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement(
        "def foo(p1: String = \"def\", p2: String, p3: String = \"def2\") = {p1}");
    assertThat(func.formalParameters()).hasSize(3);
    assertTree(func).hasParameterNames("p1", "p2", "p3");
    ParameterTree p1 = (ParameterTree) func.formalParameters().get(0);
    ParameterTree p2 = (ParameterTree) func.formalParameters().get(1);
    ParameterTree p3 = (ParameterTree) func.formalParameters().get(2);
    assertTree(p1.defaultValue()).isLiteral("\"def\"");
    assertTree(p2.defaultValue()).isNull();
    assertTree(p3.defaultValue()).isLiteral("\"def2\"");
  }

  @Test
  void function_implicit_parameter() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement(
        "def foo(implicit p1: String) = {p1}");
    assertThat(func.formalParameters()).hasSize(1);
    assertTree(func.formalParameters().get(0)).isInstanceOf(ParameterTree.class);
    assertThat(((ParameterTree)func.formalParameters().get(0)).modifiers()).hasSize(1);
    assertThat(((ParameterTree)func.formalParameters().get(0)).modifiers().get(0)).isInstanceOf(NativeTree.class);
  }

  @Test
  void function_annotated_parameter() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement(
      "def foo(@transient p1: String) = {p1}");
    assertThat(func.formalParameters()).hasSize(1);
    assertTree(func.formalParameters().get(0)).isInstanceOf(ParameterTree.class);
    assertThat(((ParameterTree)func.formalParameters().get(0)).modifiers()).hasSize(1);
    assertThat(((ParameterTree)func.formalParameters().get(0)).modifiers().get(0)).isInstanceOf(NativeTree.class);
  }

  @Test
  void function_with_annotated_and_implicit_parameter() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement(
        "def foo(implicit @transient p1: String) = {p1}");
    assertThat(func.formalParameters()).hasSize(1);
    Tree parameterTree = func.formalParameters().get(0);
    assertTree(parameterTree).isInstanceOf(ParameterTree.class);
    List<Tree> modifiers = ((ParameterTree) parameterTree).modifiers();
    assertThat(modifiers).hasSize(2);
    assertTree(modifiers.get(0)).hasTokens("@", "transient");
    assertTree(modifiers.get(1)).hasTokens("implicit");
  }

  @Test
  void function_with_annotated_and_implicit_parameters() {
    FunctionDeclarationTree func = (FunctionDeclarationTree) scalaStatement(
        "def foo(p1 : Int)(implicit @transient p2 : Char, @transient p3: String) = {p1}");
    assertThat(func.formalParameters()).hasSize(3);
    ParameterTree p1 = (ParameterTree)func.formalParameters().get(0);
    ParameterTree p2 = (ParameterTree)func.formalParameters().get(1);
    ParameterTree p3 = (ParameterTree)func.formalParameters().get(2);
    assertTree(p1.identifier()).isIdentifier("p1");
    assertThat(p1.modifiers()).isEmpty();
    assertTree(p2.identifier()).isIdentifier("p2");
    assertThat(p2.modifiers()).hasSize(2);
    // Note: Scalameta add "implicit" modifier after annotations and not before, but currently it does not worth to be fixed
    assertTree(p2.modifiers().get(0)).hasTokens("@", "transient");
    assertTree(p2.modifiers().get(1)).hasTokens("implicit");
    assertTree(p3.identifier()).isIdentifier("p3");
    assertThat(p3.modifiers()).hasSize(2);
    assertTree(p3.modifiers().get(0)).hasTokens("@", "transient");
    assertTree(p3.modifiers().get(1)).hasTokens("implicit");
  }

  @Test
  void secondary_constructor() {
    TopLevelTree topLevel = (TopLevelTree) parse("class Main(p1: Int, p2: String) { def this(p3: Int) { this(42, \"\") } }");
    ClassDeclarationTree classDecl = (ClassDeclarationTree) topLevel.children().get(0);
    List<Tree> members = (classDecl.children().get(0)).children();
    assertThat(members).hasSize(3);
    assertTree(members.get(0)).isInstanceOf(IdentifierTree.class);
    assertTree(members.get(1)).isInstanceOf(FunctionDeclarationTree.class);
    assertTree(members.get(2)).isInstanceOf(NativeTreeImpl.class);

    FunctionDeclarationTree primaryConstructor = (FunctionDeclarationTree)members.get(1);
    assertThat(primaryConstructor.formalParameters()).hasSize(2);
    assertTree(((ParameterTree)primaryConstructor.formalParameters().get(0)).identifier()).isIdentifier("p1");
    assertTree(((ParameterTree)primaryConstructor.formalParameters().get(1)).identifier()).isIdentifier("p2");
    assertThat(primaryConstructor.isConstructor()).isTrue();

    List<Tree> nativeChildren = members.get(2).children();
    assertThat(nativeChildren).hasSize(1);

    FunctionDeclarationTree secondaryConstructor = (FunctionDeclarationTree)nativeChildren.get(0);
    assertThat(secondaryConstructor.formalParameters()).hasSize(1);
    assertTree(((ParameterTree)secondaryConstructor.formalParameters().get(0)).identifier()).isIdentifier("p3");
    assertThat(secondaryConstructor.isConstructor()).isTrue();
  }

  @Test
  void secondary_constructor_expression() {
    TopLevelTree topLevel = (TopLevelTree) parse("class Main(p1: Int) { def this() = this(0) }");
    ClassDeclarationTree classDecl = (ClassDeclarationTree) topLevel.children().get(0);
    List<Tree> members = (classDecl.children().get(0)).children();
    assertThat(members).hasSize(3);
    assertTree(members.get(0)).isInstanceOf(IdentifierTree.class);
    assertTree(members.get(1)).isInstanceOf(FunctionDeclarationTree.class);
    assertTree(members.get(2)).isInstanceOf(NativeTreeImpl.class);

    FunctionDeclarationTree primaryConstructor = (FunctionDeclarationTree)members.get(1);
    assertThat(primaryConstructor.formalParameters()).hasSize(1);
    assertTree(((ParameterTree)primaryConstructor.formalParameters().get(0)).identifier()).isIdentifier("p1");
    assertThat(primaryConstructor.isConstructor()).isTrue();

    List<Tree> nativeChildren = members.get(2).children();
    assertThat(nativeChildren).hasSize(1);

    FunctionDeclarationTree secondaryConstructor = (FunctionDeclarationTree)nativeChildren.get(0);
    assertThat(secondaryConstructor.formalParameters()).isEmpty();
    assertThat(secondaryConstructor.isConstructor()).isTrue();
    List<Tree> expressions = secondaryConstructor.body().statementOrExpressions();
    assertThat(expressions).hasSize(1);
    assertTree(expressions.get(0)).isInstanceOf(NativeTreeImpl.class);
  }

  @Test
  void modifiers() {
    FunctionDeclarationTree privateFunc = scalaMethod(
      "private def foo(p1: String) = {p1}");
    assertThat(privateFunc.modifiers()).hasSize(1);
    ModifierTree modifier = (ModifierTree) privateFunc.modifiers().get(0);
    assertThat(modifier.kind()).isEqualTo(ModifierTree.Kind.PRIVATE);

    FunctionDeclarationTree overriddenFunc = scalaMethod(
      "override def foo(p1: String) = {p1}");
    assertThat(overriddenFunc.modifiers()).hasSize(1);
    modifier = (ModifierTree) overriddenFunc.modifiers().get(0);
    assertThat(modifier.kind()).isEqualTo(ModifierTree.Kind.OVERRIDE);

    FunctionDeclarationTree publicFunc = scalaMethod(
      "def foo(p1: String) = {p1}");
    assertThat(publicFunc.modifiers()).isEmpty();
  }

}

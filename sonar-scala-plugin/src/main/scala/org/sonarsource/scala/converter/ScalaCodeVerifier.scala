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
package org.sonarsource.scala.converter

import org.sonarsource.slang

import scala.meta._
import scala.util.{Success, Try}

class ScalaCodeVerifier extends slang.api.CodeVerifier {

  override def containsCode(commentContent: String): Boolean = {
    val content = if(commentContent.trim.startsWith(".")) "x" + commentContent else commentContent
    val wrappedContent = "object Obj { def f1() = { " + content + " } }"
    val source: Source = Try(wrappedContent.parse[Source]) match {
      case Success(scala.meta.parsers.Parsed.Success(t)) => t
      case _ => return false
    }

    val body = source
      .stats.head
      .asInstanceOf[Defn.Object]
      .templ.stats.head
      .asInstanceOf[Defn.Def]
      .body

    body match {
      case Term.Block(statements) =>
        !statements.forall(isSimpleExpression) && areAllOnSeparateLines(statements)
      case _ => true
    }
  }

  private def areAllOnSeparateLines(statements: List[Tree]) = {
    !statements
      .groupBy(_.pos.startLine)
      .values
      .exists(_.size > 1)
  }

  private def isSimpleExpression(tree: Tree): Boolean = {
    tree match {
      case Term.New(Init(_, _, List())) => true
      case Term.Apply(name, arg::_) if name.pos.startLine == arg.pos.startLine && name.pos.endColumn < arg.pos.startColumn - 1 => true
      case Term.Assign(Term.Select(_,_), _) => true
      case Term.Return(expr) if isSimpleExpression(expr) => true
      case Term.Try(expr, List(), None) if isSimpleExpression(expr) => true
      case _ => (tree.is[Lit]
        || tree.is[Term.Name]
        || tree.is[Term.Select]
        || tree.is[Term.ApplyInfix]
        || tree.is[Term.ApplyType]
        || tree.is[Term.Ascribe]
        || tree.is[Term.Function]
        || tree.is[Term.Tuple])
    }
  }

}

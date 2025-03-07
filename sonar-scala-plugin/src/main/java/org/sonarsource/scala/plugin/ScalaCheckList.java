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
package org.sonarsource.scala.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sonarsource.scala.checks.AllBranchesIdenticalScalaCheck;
import org.sonarsource.scala.checks.DuplicateBranchScalaCheck;
import org.sonarsource.scala.checks.UnusedPrivateMethodScalaCheck;
import org.sonarsource.slang.checks.AllBranchesIdenticalCheck;
import org.sonarsource.slang.checks.CheckList;
import org.sonarsource.slang.checks.DuplicateBranchCheck;
import org.sonarsource.slang.checks.MatchWithoutElseCheck;
import org.sonarsource.slang.checks.OctalValuesCheck;
import org.sonarsource.slang.checks.RedundantParenthesesCheck;
import org.sonarsource.slang.checks.UnusedPrivateMethodCheck;
import org.sonarsource.slang.checks.WrongAssignmentOperatorCheck;

public final class ScalaCheckList {

  private ScalaCheckList() {
    // utility class
  }

  static final Class[] SCALA_CHECK_BLACK_LIST = {
    MatchWithoutElseCheck.class,
    OctalValuesCheck.class,
    RedundantParenthesesCheck.class,
    WrongAssignmentOperatorCheck.class,
    // Language specific implementation is provided.
    UnusedPrivateMethodCheck.class,
    AllBranchesIdenticalCheck.class,
    DuplicateBranchCheck.class
  };

  static final List<Class<?>> SCALA_LANGUAGE_SPECIFIC_CHECKS = Arrays.asList(
    UnusedPrivateMethodScalaCheck.class,
    AllBranchesIdenticalScalaCheck.class,
    DuplicateBranchScalaCheck.class);

  public static List<Class<?>> checks() {
    List<Class<?>> list = new ArrayList<>(CheckList.excludeChecks(SCALA_CHECK_BLACK_LIST));
    list.addAll(SCALA_LANGUAGE_SPECIFIC_CHECKS);
    return list;
  }
}

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
package org.sonarsource.scala.checks;

import org.junit.jupiter.api.Test;

class UnusedPrivateMethodScalaCheckTest {

  @Test
  void test_with_companion_object() {
    ScalaVerifier.verify("UnusedPrivateMethodScala.scala", new UnusedPrivateMethodScalaCheck());
  }

  @Test
  void test() {
    ScalaVerifier.verify("UnusedPrivateMethodScala.NoCompanion.scala", new UnusedPrivateMethodScalaCheck());
  }

  @Test
  void test_inner_class() {
    ScalaVerifier.verify("UnusedPrivateMethodScala.InnerClass.scala", new UnusedPrivateMethodScalaCheck());
  }

  @Test
  void test_class_in_package() {
    ScalaVerifier.verify("UnusedPrivateMethodScala.Package.scala", new UnusedPrivateMethodScalaCheck());
  }
}

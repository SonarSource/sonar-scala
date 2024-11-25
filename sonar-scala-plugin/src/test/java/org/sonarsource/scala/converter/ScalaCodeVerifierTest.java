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

import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.Test;
import org.sonarsource.slang.api.CodeVerifier;

class ScalaCodeVerifierTest {

  private final CodeVerifier verifier = new ScalaCodeVerifier();

  @Test
  void testContainsCode() {

    CodeVerifierAssert.assertThat(verifier)
      .hasCode("val hello = 1")
      .hasCode("if (cond) true else false")
      .hasCode("return foo(bar, baz)")
      .hasCode("new line(x, y)")
      .hasCode("try x catch { y }")
      .hasCode("exec(param1, param2)")
      .hasCode("exec(\n param1, param2)")
      .hasCode("exec(x.y(z))")
      .hasCode("case None => true")
      .hasNoCode("")
      .hasNoCode("  ")
      .hasNoCode("1.0")
      .hasNoCode("hello")
      .hasNoCode("hello world")
      .hasNoCode("hello world and")
      .hasNoCode("hello world and John")
      .hasNoCode("TODO: remove when cats.Foldable support export-hook")
      .hasNoCode("abc:off")
      .hasNoCode("TODO: put something meaningful here?")
      .hasNoCode("1: The previous character was CR")
      .hasNoCode("Replace charset= parameter(s)")
      .hasNoCode("done (almost)")
      .hasNoCode("* Set Content-Type header to application/json;charset=utf-8")
      .hasNoCode("RSPEC-325")
      .hasNoCode("return something very useful")
      .hasNoCode("new line")
      .hasNoCode("try something different")
      .hasNoCode("done (almost done)")
      .hasNoCode("array[int]")
      .hasNoCode("(x, y) => z")
      .hasNoCode("* (42)")
      .hasNoCode("(name, persons)")
      .hasNoCode("* Copyright (c) 2012-2014");

    // Catch UnreachableError exception
    CodeVerifierAssert.assertThat(verifier)
      .hasCode("case None => true")
      .hasCode("case None => true")
      .hasCode(".set(123)")
      .hasCode("                     .set(123)")
      .hasNoCode(".Hello")
      .hasNoCode("TODO can we avoid the map() ?")
      .hasNoCode("hello.set 123")
      .hasNoCode("          .Hello")
      .hasNoCode("hello.World");
  }

  private static class CodeVerifierAssert extends AbstractAssert<CodeVerifierAssert, CodeVerifier> {

    private CodeVerifierAssert(CodeVerifier actual) {
      super(actual, CodeVerifierAssert.class);
    }

    static CodeVerifierAssert assertThat(CodeVerifier actual) {
      return new CodeVerifierAssert(actual);
    }

    CodeVerifierAssert hasCode(String code) {
      if (!actual.containsCode(code))
        failWithMessage(String.format("No code with name: '%s' found", code));
      return this;
    }

    CodeVerifierAssert hasNoCode(String code) {
      if (actual.containsCode(code))
        failWithMessage(String.format("Code with name: '%s' found, but should be absent", code));
      return this;
    }
  }
}

/*
 * SonarSource Scala
 * Copyright (C) 2018-2026 SonarSource Sàrl
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonarsource.slang.api.NativeTree;
import org.sonarsource.slang.api.StringLiteralTree;
import org.sonarsource.slang.api.TopLevelTree;
import org.sonarsource.slang.api.Tree;
import org.sonarsource.slang.checks.StringLiteralDuplicatedCheck;
import org.sonarsource.slang.checks.api.CheckContext;
import org.sonarsource.slang.checks.api.InitContext;
import org.sonarsource.slang.checks.api.SecondaryLocation;

@Rule(key = "S1192")
public class StringLiteralDuplicatedScalaCheck extends StringLiteralDuplicatedCheck {


  private static final int MINIMAL_LITERAL_LENGTH = 5;
  private static final Pattern NO_SEPARATOR_REGEXP = Pattern.compile("\\w++");
  private static final String ANNOTATION_QUALIFIED_NAME = "ScalaNativeKind(class scala.meta.Mod$Annot$ModAnnotImpl)";

  @Override
  public void initialize(InitContext init) {
    init.register(TopLevelTree.class, (ctx, tree) -> {
      Map<String, List<StringLiteralTree>> occurrences = new HashMap<>();

      Set<StringLiteralTree> annotationsStringLiterals = getStringLiteralsWithinAnnotation(tree);

      tree.descendants()
        .filter(StringLiteralTree.class::isInstance)
        .map(StringLiteralTree.class::cast)
        .filter(literal -> !annotationsStringLiterals.contains(literal))
        .filter(literal -> literal.content().length() > MINIMAL_LITERAL_LENGTH && !NO_SEPARATOR_REGEXP.matcher(literal.content()).matches())
        .forEach(literal -> occurrences.computeIfAbsent(literal.content(), key -> new LinkedList<>()).add(literal));
      check(ctx, occurrences, threshold);
    });
  }

  private static Set<StringLiteralTree> getStringLiteralsWithinAnnotation(TopLevelTree tree) {
    return tree.descendants()
      .filter(t -> t instanceof NativeTree nativeTree && nativeTree.nativeKind().toString().equals(ANNOTATION_QUALIFIED_NAME))
      .flatMap(Tree::descendants)
      .filter(StringLiteralTree.class::isInstance)
      .map(StringLiteralTree.class::cast)
      .collect(Collectors.toSet());
  }

  private static void check(CheckContext ctx, Map<String, List<StringLiteralTree>> occurrencesMap, int threshold) {
    for (Map.Entry<String, List<StringLiteralTree>> entry : occurrencesMap.entrySet()) {
      List<StringLiteralTree> occurrences = entry.getValue();
      int size = occurrences.size();
      if (size >= threshold) {
        StringLiteralTree first = occurrences.get(0);
        String message = String.format("Define a constant instead of duplicating this literal \"%s\" %s times.", first.content(), size);
        List<SecondaryLocation> secondaryLocations = occurrences.stream()
          .skip(1)
          .map(stringLiteral -> new SecondaryLocation(stringLiteral.metaData().textRange(), "Duplication"))
          .toList();
        double gap = size - 1.0;
        ctx.reportIssue(first, message, secondaryLocations, gap);
      }
    }
  }

}

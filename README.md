# sonar-scala
[![Build Status](https://api.cirrus-ci.com/github/SonarSource/sonar-scala.svg?branch=master)](https://cirrus-ci.com/github/SonarSource/sonar-scala)
[![Quality Gate Status](https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=SonarSource_sonar-scala&metric=alert_status&token=sqb_6e2451e1b8f2da87264cb9638302eaed1d9eb87a)](https://next.sonarqube.com/sonarqube/dashboard?id=SonarSource_sonar-scala)
[![Coverage](https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=SonarSource_sonar-scala&metric=coverage&token=sqb_6e2451e1b8f2da87264cb9638302eaed1d9eb87a)](https://next.sonarqube.com/sonarqube/dashboard?id=SonarSource_sonar-scala)

This is a developer documentation. If you want to analyze source code in SonarQube read the following documentation:

* Scala language: [analysis of Scala documentation](https://docs.sonarqube.org/latest/analysis/languages/scala/)

This analyzer is built on top of SLang (SonarSource Language).
SLang is a framework to quickly develop code analyzers for SonarQube to help developers write [Clean Code](https://www.sonarsource.com/solutions/clean-code/?utm_medium=referral&utm_source=github&utm_campaign=clean-code&utm_content=slang).

SLang defines language-agnostic AST. Using this AST we can develop simple syntax-based rules. Then we use a parser for real language to create this AST. Currently, Ruby and Scala analyzers use this approach.

## Scala

We use [Scalameta](https://scalameta.org/) to parse the Scala language.

### Scala coverage

For Scala files, we will import both [Scoverage](http://scoverage.org/) and JaCoCo coverage reports. Note that this will result in strange behavior since:

* Only line coverage will be used from the Scoverage report.
* JaCoCo can be imprecise when computing conditions coverage on Scala code, generating FP (typically on pattern matching).

This situation only applies to two Scala files, this current situation is acceptable.

## Have questions or feedback?

To provide feedback (request a feature, report a bug, etc.) use the [SonarQube Community Forum](https://community.sonarsource.com/). Please do not forget to specify the language, plugin version, and SonarQube version.

## Building

### Build

Build and run Unit Tests:

    ./gradlew build

## Integration Tests

By default, Integration Tests (ITs) are skipped during builds.
If you want to run them, you need first to retrieve the related projects which are used as input:

    git submodule update --init its/sources

Then build and run the Integration Tests using the `its` property:

    ./gradlew build -Pits --info --no-daemon

You can also build and run only Ruling Tests using the `ruling` property:

    ./gradlew build -Pruling --info --no-daemon

## Validate the published artifact locally

    ./gradlew build -x test publishToMavenLocal
    find "$HOME/.m2/repository/org/sonarsource/slang/sonar-scala-plugin" -ls

## License headers

License headers are automatically updated by the spotless plugin but only for Java files.
However, there are files such as `package-info.java` and `module-info.java` that spotless ignores.
Scala source files are also ignored by spotless.
For those files use a manual script like below to update the license. E.g., for Scala files (on Mac):

    `find . -type f -name "*.scala" -exec sed -i '' 's/2018-2023/2018-2024/' "{}" \;`

## License
Copyright 2018-2024 SonarSource.

SonarQube analyzers released after November 29, 2024, including patch fixes for prior versions,
are published under the [Sonar Source-Available License Version 1 (SSALv1)](LICENSE).

See individual files for details that specify the license applicable to each file.
Files subject to the SSALv1 will be noted in their headers.

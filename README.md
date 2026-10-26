<!-- Sonar Marketing hosts these approved brand assets on its Kentico Kontent CDN (assets-eu-01.kc-usercontent.com). Shared URLs are intentional; consult Marketing before replacing them. -->
<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://assets-eu-01.kc-usercontent.com/ef593040-b591-0198-9506-ed88b30bc023/a23fc7ba-23f0-489a-829d-ed88c0748521/Sonar_Logo_Dark%20Backgrounds.svg">
    <img src="https://assets-eu-01.kc-usercontent.com/ef593040-b591-0198-9506-ed88b30bc023/82c13eba-d95c-4bb8-8007-7ce77c14e043/Sonar_Logo_Light%20Backgrounds.svg" alt="Sonar logo" width="400">
  </picture>
</p>

[![CI](https://github.com/SonarQubeCommunity/sonar-scala/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/SonarQubeCommunity/sonar-scala/actions/workflows/ci.yml)

<!-- sonar-marketing:start -->
<!-- Marketing maintains this section. For wording changes, consult the relevant Product Marketing Manager (PMM). The repository maintainers review accuracy and merge changes. -->

# SonarQube analyzer for Scala

This repository contains the SonarQube analyzer for Scala. It uses Scalameta and the SLang analysis framework; the documentation below explains its parser integration, coverage import, and development workflow.

To learn more about Sonar’s Scala analysis, visit the [Sonar website](https://www.sonarsource.com/knowledge/languages/scala/).

<!-- sonar-marketing:end -->

This is a developer documentation. If you want to analyze source code in SonarQube read the following documentation:

* Scala language: [analysis of Scala documentation](https://docs.sonarqube.org/latest/analysis/languages/scala/)

This analyzer is built on top of SLang (SonarSource Language).
SLang is a framework to quickly develop code analyzers for SonarQube to help developers analyze code quality.

SLang defines language-agnostic AST. Using this AST we can develop simple syntax-based rules. Then we use a parser for real language to create this AST. Currently, Ruby and Scala analyzers use this approach.

## Scala

We use [Scalameta](https://scalameta.org/) to parse the Scala language.

### Scala coverage

For Scala files, we will import both [Scoverage](http://scoverage.org/) and JaCoCo coverage reports. Note that this will result in strange behavior since:

* Only line coverage will be used from the Scoverage report.
* JaCoCo can be imprecise when computing conditions coverage on Scala code, generating FP (typically on pattern matching).

This situation only applies to two Scala files, this current situation is acceptable.

### Rule descriptions

Rule metadata is maintained by hand. To change a rule, edit its files directly:

- `sonar-scala-plugin/src/main/resources/org/sonar/l10n/scala/rules/scala/S*.json` (title, severity, tags) and `S*.html` (description) for the Scala rules
- `rules.json` in the `scalafix`, `scalastyle`, and `scapegoat` folders next to it for the rules of the imported external reports

## Community Maintenance

This analyzer was developed by SonarSource and is now maintained by the community in the [SonarQubeCommunity](https://github.com/SonarQubeCommunity) organization. SonarQube Community Build bundles it in versions before 26.11. From 26.11 on, it is no longer bundled and must be installed manually. It builds on [SLang](https://github.com/SonarQubeCommunity/slang).

## Installing

Download `sonar-scala-plugin-<version>.jar` from [GitHub releases](https://github.com/SonarQubeCommunity/sonar-scala/releases), copy it into your SonarQube server's `extensions/plugins` directory, and restart the server. The plugin is not available from the SonarQube Marketplace.

## Feedback and Contributions

Report bugs and request features in [GitHub issues](https://github.com/SonarQubeCommunity/sonar-scala/issues). Contributions are welcome: see [CONTRIBUTING.md](CONTRIBUTING.md).

## Building

### Prerequisites

You will need Java 21 installed.

### Build

Initialize the `build-logic/common` submodule, which every build needs for the license file generator plugin:

    git submodule update --init build-logic/common

Build and run Unit Tests:

    ./gradlew build

## Integration Tests

By default, Integration Tests (ITs) are skipped during builds.
If you want to run them, you need first to retrieve the related projects which are used as input:

    git submodule update --init its/sources

Then build and run the Integration Tests using the `its` property:

    ./gradlew build -Pits --info --no-daemon

The tests install the plugin built by this project into SonarQube Community Build, which Orchestrator downloads from Maven Central, so no license or repository credentials are needed. They use the latest release by default; to test another version, add for example `-Dsonar.runtimeVersion=26.10.0.133078`.

You can also build and run only Ruling Tests using the `ruling` property:

    ./gradlew build -Pruling --info --no-daemon

## Validate the published artifact locally

    ./gradlew build -x test publishToMavenLocal
    find "$HOME/.m2/repository/org/sonarsource/slang/sonar-scala-plugin" -ls

## Releasing

Releases are published from GitHub:

1. Create and publish a GitHub release with a tag such as `v1.26.0`.
2. The [Release](.github/workflows/release.yml) workflow builds and tests the plugin with the version from the tag, and attaches `sonar-scala-plugin-<version>.jar` and its SHA-256 checksum to the release.
3. Set `version` in `gradle.properties` to the next development version, such as `1.26.1-SNAPSHOT`.

## License headers

License headers are automatically updated by the spotless plugin but only for Java files.
However, there are files such as `package-info.java` and `module-info.java` that spotless ignores.
Scala source files are also ignored by spotless.
For those files use a manual script like below to update the license. E.g., for Scala files (on Mac):

    `find . -type f -name "*.scala" -exec sed -i '' 's/2018-2025/2018-2026/' "{}" \;`

For Java `*-info.java` files (on Mac):

    `find . -type f -name "*-info.java" -exec sed -i '' 's/2018-2025/2018-2026/' "{}" \;`
## License
Copyright 2018-2026 SonarSource.

SonarQube analyzers released after November 29, 2024, including patch fixes for prior versions,
are published under the [Sonar Source-Available License Version 1 (SSALv1)](LICENSE.txt).

See individual files for details that specify the license applicable to each file.
Files subject to the SSALv1 will be noted in their headers.

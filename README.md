# sonar-scala

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

    ./gradlew build -Pruling-scala --info --no-daemon

## License headers

License headers are automatically updated by the spotless plugin but only for Java files.
Furthermore, there are files such as `package-info.java` and `module-info.java` that spotless ignores. Also, Scala and Go source files are not handled. For those files use a manual script like below to update the license. E.g., for Go files (on Mac):

    `find . -type f -name "*.go" -exec sed -i '' 's/2018-2023/2018-2024/' "{}" \;`

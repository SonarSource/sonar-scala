# Contributing

Thank you for helping to improve the Scala analyzer.

## Reporting Issues

Report bugs and request features in [GitHub issues](https://github.com/SonarQubeCommunity/sonar-scala/issues). For a false positive or false negative, include the rule key, a minimal code sample, and the analyzer version.

## Submitting Changes

1. Fork the repository and create a branch from `master`.
2. Follow the build instructions in the [README](README.md#building), including the submodule initialization.
3. Add or update tests that cover your change.
4. Run `./gradlew spotlessApply` to format the code, then `./gradlew build` to make sure that everything passes. Add `-Pits` to also run the integration tests, as CI does.
5. Open a pull request against `master` and describe what the change does and why.

The [CI](.github/workflows/ci.yml) workflow builds the project and runs the unit and integration tests on every pull request.

## Rule Descriptions

Rule metadata is maintained by hand. See [Rule descriptions](README.md#rule-descriptions) in the README.

## License

By contributing, you agree that your contributions are licensed under the [Sonar Source-Available License Version 1 (SSALv1)](LICENSE.txt), like the rest of the project.

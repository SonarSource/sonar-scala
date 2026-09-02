package example

import org.scalatest.funsuite.AnyFunSuite

class ExhaustiveSpec extends AnyFunSuite {
  test("a passing test") {
    Thread.sleep(10)
    assert(1 + 1 == 2)
  }

  test("a failing test") {
    Thread.sleep(10)
    assert(1 + 1 == 3)
  }

  test("an errored test") {
    Thread.sleep(10)
    throw new RuntimeException("expected integration-test error")
  }

  ignore("an ignored test") {
    Thread.sleep(10)
  }
}

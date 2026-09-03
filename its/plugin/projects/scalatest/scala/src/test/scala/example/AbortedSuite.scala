package example

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

class AbortedSuite extends AnyFunSuite with BeforeAndAfterAll {
  override protected def beforeAll(): Unit = {
    Thread.sleep(10)
    throw new RuntimeException("expected integration-test suite error")
  }

  test("is never run") {
    fail("beforeAll aborts this suite")
  }
}

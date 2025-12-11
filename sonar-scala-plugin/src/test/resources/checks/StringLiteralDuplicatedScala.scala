class MyClass {
  def foo(str: String) = {
    str match {
      case "Alice" =>
        val message = "Hello dear" + "Alice" // Noncompliant {{Define a constant instead of duplicating this literal "Hello dear" 3 times.}}
        message
      case "Bob" =>
        val message = "Hello dear" + "Bob"
        message
      case _ =>
        val message = "Hello dear" + "Guest"
        message
    }
  }

  def bar() : Unit = {
    val greeting1 = "Welcome to our site!" // Compliant, literal only appears twice
    val greeting2 = "Welcome to our site!"
  }

  def baz() : Unit = {
    val farewell1 = "Goodbye" // Compliant, literal is a single word
    val farewell2 = "Goodbye"
    val farewell3 = "Goodbye"
  }


  def foo2 (): Unit = {
    val a = "string_literal5" // Compliant
    val b = "string_literal5"
    val c = "string_literal5"
  }

  def qux(): Unit = {
    val x = "as d" // Compliant, literal length smaller than threshold
    val y = "as d"
    val z = "as d"
  }


  object CompliantAnnotation {
    @SuppressWarnings("scala:S1192") // Compliant
    def a(): String =  "Hello2!" + "Hello2!" + "Hello2!" // Noncompliant {{Define a constant instead of duplicating this literal "Hello2!" 3 times.}}
    @SuppressWarnings("scala:S1192")
    def b(): String =  "Welcome2!" + "Welcome2!" + "Welcome2!" // Noncompliant {{Define a constant instead of duplicating this literal "Welcome2!" 3 times.}}
    @SuppressWarnings("scala:S1192")
    def c(): String = "ByeBye!" +  "ByeBye!" + "ByeBye!" // Noncompliant {{Define a constant instead of duplicating this literal "ByeBye!" 3 times.}}
  }
  object CompliantAnnotation2 {
    @SuppressWarnings("scala:S1192") // Compliant
    def a(): String = "scala:S1192" + "scala:S1192" + "scala:S1192" // Noncompliant {{Define a constant instead of duplicating this literal "scala:S1192" 3 times.}}
  }

  object CompliantAnnotation3 {
    @Deprecated(since = "6582.23.1354") // Compliant
    def a(): String = "6582.23.1354" + "6582.23.1354" + "6582.23.1354" // Noncompliant {{Define a constant instead of duplicating this literal "6582.23.1354" 3 times.}}
  }

  @SuppressWarnings("scala:S1193") // Compliant
  object Reproducer {
    @SuppressWarnings("scala:S1193")
    def a(): String =  "Hello!" + "Hello!" + "Hello!" // Noncompliant {{Define a constant instead of duplicating this literal "Hello!" 3 times.}}
    @SuppressWarnings("scala:S1193")
    def b(): String =  "Welcome!" + "Welcome!" + "Welcome!" // Noncompliant {{Define a constant instead of duplicating this literal "Welcome!" 3 times.}}
  }

  @Deprecated(since = "2051.30.21654") // Compliant
  object Reproducer2 {
    @Deprecated(since = "2051.30.21654")
    def a(): String =  ""
    @Deprecated(since = "2051.30.21654")
    def b(): String =  ""
  }

  class MyNestedLiteral(values: Array[String]) extends scala.annotation.StaticAnnotation

  @MyNestedLiteral(Array("some-nested-string")) // Compliant
  object Reproducer3 {
    @MyNestedLiteral(Array("some-nested-string"))
    def a(): String =  ""
    @MyNestedLiteral(Array("some-nested-string", "some-nested-string", "some-nested-string"))
    def b(): String =  ""
  }

}

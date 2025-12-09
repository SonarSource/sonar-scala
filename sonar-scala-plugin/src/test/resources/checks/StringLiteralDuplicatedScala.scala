class MyClass {
  def foo(str: String) = {
    str match {
      case "Alice" =>
        val message = "Hello dear" + "Alice" // Noncompliant
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


  object Compliant {
    @SuppressWarnings("scala:S1192") // Compliant
    def a(): String =  "Hello2!" + "Hello2!" + "Hello2!" // Noncompliant
    @SuppressWarnings("scala:S1192")
    def b(): String =  "Welcome2!" + "Welcome2!" + "Welcome2!" // Noncompliant
    @SuppressWarnings("scala:S1192")
    def c(): String = "ByeBye!" +  "ByeBye!" + "ByeBye!" // Noncompliant
  }
  object Compliant2 {
    @SuppressWarnings("scala:S1192") // Compliant
    def a(): String = "scala:S1192" + "scala:S1192" + "scala:S1192" // Noncompliant
  }

  @SuppressWarnings("scala:S1193") // Compliant
  object Reproducer {
    @SuppressWarnings("scala:S1193")
    def a(): String =  "Hello!" + "Hello!" + "Hello!" // Noncompliant
    @SuppressWarnings("scala:S1193")
    def b(): String =  "Welcome!" + "Welcome!" + "Welcome!" // Noncompliant
  }
}

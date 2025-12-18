class MyClass {

  // Clean tests
  // Write the test in scala style, not with semincolon etc...


  def foo(x: Int) = {
    var y = if (x > 0) { // Noncompliant {{Add the missing "else" clause.}}
      x
    }
    y
  }

  def foo2(x: Int) = {
    if (x > 0) {// FN, there is not an else and the value of the if is returned by the method
      x // The return type is inferred as AnyValue
    }
  }

  def foo3(x: Int) = {
    if (if (x > 0) false else true) {
      println("Smaller")
    } else if (false) { // Noncompliant {{Add the missing "else" clause.}}
      println("Bigger")
    }
  }


  def foo4(x: Int): Int = {
    return if (x > 0) {
      x
    } else 10 // compliant
  }

  def foo5(x: Int): Int = {
    val y = foo4(x)
    if (y > 0) {
      println("There")
    } else {
      println("Hello")
    }
    return y
  }

  def modulo10(x: Int) = {
    var y = -1
    if (x < 10) {
      y = 0
    } else if ( x < 20) {
      y = 1
    } else if ( x < 30){
      y = 2
    }else if ( x < 40){
      y = 3
    }else if ( x < 50){ // Noncompliant {{Add the missing "else" clause.}}
      y = 4
    }
    y
  }


  def modulo10bis(x: Int) = {
    var y = 0
    if (x < 10) {
      y = 0
    } else if ( x < 20) {
      y = 1
    } else if ( x < 30){
      y = 2
    }else if ( x < 40){
      y = 3
    }else if ( x < 50){
      y = 4
    }else {
      y = 5
    }
    y
  }

  def methodA(n: Int): Int = {
    // This is scala specific
    val a = if (n < 3) { // Noncompliant {{Add the missing "else" clause.}}
      1
    }
    a match {
      case x: Int => n + x
      case _ => n
    }
  }

  def methodB(n: Int): Int = {
    val b = if (n < 3) {
      1
    } else if (n > 10) { // Noncompliant {{Add the missing "else" clause.}}
      n
    }
    b match {
      case x: Int => n + x
      case _ => n
    }
  }


  def bar(x: Int): Int = {
    var y = 0;
    if (x > 0) { // Compliant
      y = x
      y = x
    }
    return y;
  }


  def myMethod(x: Int): Int = {
    var y = 0;
    if (x == 30) {
      y = 30;
    } else if (x == 20) { // Noncompliant {{Add the missing "else" clause.}}
      y = 40;
    }
    return y;
  }

  def qux(x: Int): Int = {
    var y = 0;
    if (x == 30)
      return 5
    else if (x == 20) { // Compliant
      return 3
    } else if (x == 1) {
      throw new Exception("error")
    }

    if (x == 35) {
      y = 20
    } else if (x == 25) {
      return 3
    } else if (x == 5) { // Noncompliant {{Add the missing "else" clause.}}
      throw new Exception("error")
    }
    return y;
  }

  def nestedIf(x: Int, y: Int): Int = {
    var z = 0;
    if (x > 0) {
      if (y > 0) {
        z = x + y
      } else if (y == 0) {
        z = x
      } else if (y < 0) { // Noncompliant {{Add the missing "else" clause.}}
        z = x - y
      }
    }
    return z;
  }
}

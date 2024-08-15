class MyClass {
  def myMethod(x : Any) = {
    val cond: Option[Int] = Some(3)
    val value = 1

    cond match {
      case Some(value) => value
      case _ => value
    }

    cond match { // Noncompliant
      case Some(123) => value
      case _ => value
    }

    x match {
      case value: Int => value
      case value: Long => value
      case _ => value
    }

    value match { // Noncompliant
      case 1 => value
      case 2 => value
      case _ => value
    }

    value match { // Compliant
      case _ => value
    }

    if (value == 1) { // Noncompliant
      value
    } else if (value == 2) {
      value
    } else {
      value
    }

    cond match { // Noncompliant
        case 4 =>
          val a: String = "" + x + 1
          a
        case _ =>
          val a: String = "" + x + 1
          a
    }

    cond match {
        case (Some(x: Int), y) => // Compliant, shadowing the 'x' variable
          val a: String = "" + x + 1
          a
        case _ =>
          val a: String = "" + x + 1
          a
    }

    cond match {
        case (1 | 2) :: x :: tail => // Compliant, shadowing the 'x' variable
          val a: String = "" + x + 1
          a
        case _ =>
          val a: String = "" + x + 1
          a
    }

    cond match {
        case x: Long if x == 0 => // Compliant, shadowing the 'x' variable
          val a: String = "" + x + 1
          a
        case _ =>
          val a: String = "" + x + 1
          a
    }
  }
}

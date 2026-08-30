package minilang

object TestRunner {
  private case class Case(name: String, source: String, expectedType: Option[String], expectedValue: Option[String])

  private var executed = 0
  private var passed = 0

  private def expectSuccess(c: Case): Unit = {
    executed += 1
    Runner.run(c.source) match {
      case Right(ok) =>
        val typeOk = c.expectedType.forall(_ == ok.tpe.show)
        val valueOk = c.expectedValue.forall(_ == ok.value.show)
        if (typeOk && valueOk) passed += 1
        else throw new AssertionError(s"${c.name}: expected type=${c.expectedType}, value=${c.expectedValue}; got ${ok.tpe.show}, ${ok.value.show}")
      case Left(err) => throw new AssertionError(s"${c.name}: expected success, got $err")
    }
  }

  private def expectFailure(name: String, source: String): Unit = {
    executed += 1
    Runner.run(source) match {
      case Left(_)  => passed += 1
      case Right(x) => throw new AssertionError(s"$name: expected failure, got ${x.tpe.show} / ${x.value.show}")
    }
  }

  def main(args: Array[String]): Unit = {
    // 60 arithmetic programs
    for (i <- 1 to 20) {
      expectSuccess(Case(s"add-$i", s"$i + ${i + 1}", Some("Int"), Some((2 * i + 1).toString)))
      expectSuccess(Case(s"mul-$i", s"$i * 3", Some("Int"), Some((i * 3).toString)))
      expectSuccess(Case(s"nested-arith-$i", s"($i + 2) * 4 - 3", Some("Int"), Some(((i + 2) * 4 - 3).toString)))
    }

    // 30 comparison / Boolean programs -> 90 total
    for (i <- 1 to 10) {
      expectSuccess(Case(s"less-$i", s"$i < ${i + 1}", Some("Bool"), Some("true")))
      expectSuccess(Case(s"eq-$i", s"$i == $i", Some("Bool"), Some("true")))
      expectSuccess(Case(s"logic-$i", s"($i < ${i + 1}) && !false", Some("Bool"), Some("true")))
    }

    // 25 let/scoping programs -> 115 total
    for (i <- 1 to 25) {
      expectSuccess(Case(s"let-$i", s"let x = $i in let y = x + 2 in y * 2", Some("Int"), Some(((i + 2) * 2).toString)))
    }

    // 20 function programs -> 135 total
    for (i <- 1 to 20) {
      expectSuccess(Case(
        s"lambda-$i",
        s"let inc = fun(x: Int): Int => x + 1 in inc($i)",
        Some("Int"), Some((i + 1).toString)
      ))
    }

    // 10 pair programs -> 145 total
    for (i <- 1 to 10) {
      expectSuccess(Case(s"pair-$i", s"fst(($i, ${i + 1})) + snd(($i, ${i + 1}))", Some("Int"), Some((2 * i + 1).toString)))
    }

    // 10 recursive programs -> 155 total
    for (i <- 0 to 9) {
      val expected = (1 to i).product
      expectSuccess(Case(
        s"factorial-$i",
        s"let rec fact(n: Int): Int = if n == 0 then 1 else n * fact(n - 1) in fact($i)",
        Some("Int"), Some(expected.toString)
      ))
    }

    // 10 explicitly polymorphic programs -> 165 total
    for (i <- 1 to 10) {
      expectSuccess(Case(
        s"poly-id-$i",
        s"let id = fun[T](x: T): T => x in id[Int]($i)",
        Some("Int"), Some(i.toString)
      ))
    }

    // 20 intentionally ill-typed programs -> 185 total
    val invalid = Seq(
      "1 + true",
      "if 1 then 2 else 3",
      "if true then 1 else false",
      "let f = fun(x: Int): Int => x + 1 in f(true)",
      "fst(1)",
      "snd(false)",
      "!1",
      "-true",
      "1 && true",
      "false + true",
      "let x = 1 in y",
      "let f = fun(x: Int): Bool => x + 1 in f",
      "let id = fun[T](x: T): T => x in id[Int](false)",
      "let f = fun(x: Int): Int => x in f[Int](1)",
      "let rec f(x: Int): Bool = x + 1 in f(1)",
      "\"a\" - \"b\"",
      "true < false",
      "(1, true) + 2",
      "let f = fun(x: Int): Int => x in f + 1",
      "let id = fun[T](x: T): T => x in id[Unknown](1)"
    )
    invalid.zipWithIndex.foreach { case (src, idx) => expectFailure(s"invalid-${idx + 1}", src) }

    println(s"MiniLang test suite: $passed / $executed programs passed")
    if (passed != executed) sys.exit(1)
    else println("100% agreement between expected typing/evaluation outcomes and implementation behavior.")
  }
}

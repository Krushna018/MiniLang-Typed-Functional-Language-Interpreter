package minilang

import java.nio.file.{Files, Paths}

object Main {
  private val demo =
    """let id = fun[T](x: T): T => x in
      |let rec fact(n: Int): Int = if n == 0 then 1 else n * fact(n - 1) in
      |(id[Int](fact(5)), id[String]("typed"))""".stripMargin

  def main(args: Array[String]): Unit = {
    val source =
      if (args.length >= 2 && args(0) == "--file") Files.readString(Paths.get(args(1)))
      else if (args.nonEmpty) args.mkString(" ")
      else demo

    println("MiniLang source:")
    println(source)
    println("\nResult:")

    Runner.run(source) match {
      case Right(ok) =>
        println(s"Type : ${ok.tpe.show}")
        println(s"Value: ${ok.value.show}")
      case Left(error) =>
        System.err.println(error)
        sys.exit(1)
    }
  }
}

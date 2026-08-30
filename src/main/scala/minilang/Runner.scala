package minilang

import minilang.ast.*
import minilang.eval.*
import minilang.parser.Parser
import minilang.typing.TypeChecker

object Runner {
  case class Success(tpe: Type, value: Value)

  def run(source: String): Either[String, Success] = {
    try {
      val expr = Parser.parse(source)
      TypeChecker.infer(expr) match {
        case Left(err) => Left("Type error: " + err.message)
        case Right(tpe) =>
          try Right(Success(tpe, Evaluator.eval(expr)))
          catch { case e: EvalException => Left("Evaluation error: " + e.getMessage) }
      }
    } catch {
      case e: RuntimeException => Left(e.getMessage)
    }
  }
}

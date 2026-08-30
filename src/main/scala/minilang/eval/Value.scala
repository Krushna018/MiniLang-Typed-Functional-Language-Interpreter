package minilang.eval

import minilang.ast.Expr

sealed trait Value { def show: String }
case class IntVal(value: Int) extends Value { def show = value.toString }
case class BoolVal(value: Boolean) extends Value { def show = value.toString }
case class StringVal(value: String) extends Value { def show = "\"" + value + "\"" }
case object UnitVal extends Value { val show = "unit" }
case class PairVal(left: Value, right: Value) extends Value { def show = s"(${left.show}, ${right.show})" }
case class Closure(param: String, body: Expr, env: Map[String, Value]) extends Value {
  def show = "<function>"
}
case class RecClosure(name: String, param: String, body: Expr, env: Map[String, Value]) extends Value {
  def show = s"<rec-function:$name>"
}

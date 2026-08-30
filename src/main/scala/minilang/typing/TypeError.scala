package minilang.typing

sealed trait MiniTypeError {
  def message: String
}

case class UndefinedVariable(name: String) extends MiniTypeError {
  def message: String = s"undefined variable: $name"
}
case class UnknownTypeVariable(name: String) extends MiniTypeError {
  def message: String = s"unknown type variable: $name"
}
case class TypeMismatch(expected: String, actual: String, context: String) extends MiniTypeError {
  def message: String = s"type mismatch in $context: expected $expected, found $actual"
}
case class ExpectedFunction(actual: String) extends MiniTypeError {
  def message: String = s"expected a function, found $actual"
}
case class ExpectedPolymorphicFunction(actual: String) extends MiniTypeError {
  def message: String = s"expected a polymorphic value for type application, found $actual"
}
case class ExpectedBooleanCondition(actual: String) extends MiniTypeError {
  def message: String = s"if-condition must be Bool, found $actual"
}
case class BranchTypeMismatch(left: String, right: String) extends MiniTypeError {
  def message: String = s"if branches have different types: $left and $right"
}
case class InvalidOperandTypes(op: String, left: String, right: Option[String]) extends MiniTypeError {
  def message: String = right match {
    case Some(r) => s"invalid operands for '$op': $left and $r"
    case None    => s"invalid operand for '$op': $left"
  }
}
case class ExpectedPair(actual: String, projection: String) extends MiniTypeError {
  def message: String = s"$projection expects a pair, found $actual"
}
case class RecursiveReturnMismatch(function: String, expected: String, actual: String) extends MiniTypeError {
  def message: String = s"recursive function '$function' declares $expected but body has type $actual"
}
case class InvalidTypeAnnotation(details: String) extends MiniTypeError {
  def message: String = details
}

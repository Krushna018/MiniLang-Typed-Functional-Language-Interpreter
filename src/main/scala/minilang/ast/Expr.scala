package minilang.ast

sealed trait Expr
case class IntLit(value: Int) extends Expr
case class BoolLit(value: Boolean) extends Expr
case class StringLit(value: String) extends Expr
case object UnitLit extends Expr
case class Var(name: String) extends Expr
case class Let(name: String, value: Expr, body: Expr) extends Expr
case class LetRec(
    name: String,
    param: String,
    paramType: Type,
    returnType: Type,
    functionBody: Expr,
    inExpr: Expr
) extends Expr
case class IfThenElse(cond: Expr, ifTrue: Expr, ifFalse: Expr) extends Expr
case class Lambda(
    typeParam: Option[String],
    param: String,
    paramType: Type,
    returnType: Type,
    body: Expr
) extends Expr
case class Apply(function: Expr, argument: Expr) extends Expr
case class TypeApply(function: Expr, typeArg: Type) extends Expr
case class PairExpr(left: Expr, right: Expr) extends Expr
case class Fst(pair: Expr) extends Expr
case class Snd(pair: Expr) extends Expr
case class Unary(op: String, expr: Expr) extends Expr
case class Binary(op: String, left: Expr, right: Expr) extends Expr

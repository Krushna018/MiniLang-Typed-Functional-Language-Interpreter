package minilang.ast

sealed trait Type {
  def show: String
}

case object TInt extends Type { val show = "Int" }
case object TBool extends Type { val show = "Bool" }
case object TString extends Type { val show = "String" }
case object TUnit extends Type { val show = "Unit" }
case class TPair(left: Type, right: Type) extends Type {
  def show: String = s"(${left.show}, ${right.show})"
}
case class TFun(from: Type, to: Type) extends Type {
  def show: String = {
    val lhs = from match {
      case _: TFun => s"(${from.show})"
      case _       => from.show
    }
    s"$lhs -> ${to.show}"
  }
}
case class TVar(name: String) extends Type { def show: String = name }
case class TForAll(param: String, body: Type) extends Type {
  def show: String = s"forall $param. ${body.show}"
}

object TypeOps {
  def substitute(tpe: Type, variable: String, replacement: Type): Type = tpe match {
    case TInt | TBool | TString | TUnit => tpe
    case TVar(n) if n == variable       => replacement
    case v: TVar                        => v
    case TPair(a, b)                    => TPair(substitute(a, variable, replacement), substitute(b, variable, replacement))
    case TFun(a, b)                     => TFun(substitute(a, variable, replacement), substitute(b, variable, replacement))
    case TForAll(p, body) if p == variable => TForAll(p, body)
    case TForAll(p, body)               => TForAll(p, substitute(body, variable, replacement))
  }

  def freeTypeVars(tpe: Type): Set[String] = tpe match {
    case TInt | TBool | TString | TUnit => Set.empty
    case TVar(n)                        => Set(n)
    case TPair(a, b)                    => freeTypeVars(a) ++ freeTypeVars(b)
    case TFun(a, b)                     => freeTypeVars(a) ++ freeTypeVars(b)
    case TForAll(p, body)               => freeTypeVars(body) - p
  }
}

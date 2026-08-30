package minilang.eval

import minilang.ast.*

class EvalException(message: String) extends RuntimeException(message)

object Evaluator {
  type Env = Map[String, Value]

  def eval(expr: Expr): Value = eval(expr, Map.empty)

  private def eval(expr: Expr, env: Env): Value = expr match {
    case IntLit(v)    => IntVal(v)
    case BoolLit(v)   => BoolVal(v)
    case StringLit(v) => StringVal(v)
    case UnitLit      => UnitVal
    case Var(name)    => env.getOrElse(name, throw new EvalException(s"undefined variable at runtime: $name"))

    case Let(name, value, body) =>
      val v = eval(value, env)
      eval(body, env + (name -> v))

    case LetRec(name, param, _, _, fnBody, inExpr) =>
      val closure = RecClosure(name, param, fnBody, env)
      eval(inExpr, env + (name -> closure))

    case IfThenElse(cond, yes, no) => eval(cond, env) match {
      case BoolVal(true)  => eval(yes, env)
      case BoolVal(false) => eval(no, env)
      case other          => throw new EvalException(s"condition evaluated to ${other.show}, expected Bool")
    }

    case Lambda(_, param, _, _, body) => Closure(param, body, env)
    case TypeApply(function, _)       => eval(function, env)

    case Apply(function, argument) =>
      val fn = eval(function, env)
      val arg = eval(argument, env)
      fn match {
        case Closure(param, body, closureEnv) => eval(body, closureEnv + (param -> arg))
        case rc @ RecClosure(name, param, body, closureEnv) =>
          eval(body, closureEnv + (name -> rc) + (param -> arg))
        case other => throw new EvalException(s"attempted to apply non-function value ${other.show}")
      }

    case PairExpr(left, right) => PairVal(eval(left, env), eval(right, env))
    case Fst(pair) => eval(pair, env) match {
      case PairVal(a, _) => a
      case other         => throw new EvalException(s"fst expected pair, got ${other.show}")
    }
    case Snd(pair) => eval(pair, env) match {
      case PairVal(_, b) => b
      case other         => throw new EvalException(s"snd expected pair, got ${other.show}")
    }

    case Unary("-", value) => eval(value, env) match {
      case IntVal(v) => IntVal(-v)
      case other     => throw new EvalException(s"unary - expected Int, got ${other.show}")
    }
    case Unary("!", value) => eval(value, env) match {
      case BoolVal(v) => BoolVal(!v)
      case other      => throw new EvalException(s"! expected Bool, got ${other.show}")
    }
    case Unary(op, _) => throw new EvalException(s"unknown unary operator $op")

    case Binary(op, left, right) =>
      if (op == "&&") {
        eval(left, env) match {
          case BoolVal(false) => BoolVal(false)
          case BoolVal(true)  => eval(right, env) match {
            case BoolVal(v) => BoolVal(v)
            case other      => throw new EvalException(s"&& expected Bool, got ${other.show}")
          }
          case other => throw new EvalException(s"&& expected Bool, got ${other.show}")
        }
      } else if (op == "||") {
        eval(left, env) match {
          case BoolVal(true)  => BoolVal(true)
          case BoolVal(false) => eval(right, env) match {
            case BoolVal(v) => BoolVal(v)
            case other      => throw new EvalException(s"|| expected Bool, got ${other.show}")
          }
          case other => throw new EvalException(s"|| expected Bool, got ${other.show}")
        }
      } else {
        val l = eval(left, env)
        val r = eval(right, env)
        evalBinary(op, l, r)
      }
  }

  private def evalBinary(op: String, left: Value, right: Value): Value = (op, left, right) match {
    case ("+", IntVal(a), IntVal(b))       => IntVal(a + b)
    case ("+", StringVal(a), StringVal(b)) => StringVal(a + b)
    case ("-", IntVal(a), IntVal(b))       => IntVal(a - b)
    case ("*", IntVal(a), IntVal(b))       => IntVal(a * b)
    case ("/", IntVal(_), IntVal(0))       => throw new EvalException("division by zero")
    case ("/", IntVal(a), IntVal(b))       => IntVal(a / b)
    case ("<", IntVal(a), IntVal(b))       => BoolVal(a < b)
    case ("<=", IntVal(a), IntVal(b))      => BoolVal(a <= b)
    case (">", IntVal(a), IntVal(b))       => BoolVal(a > b)
    case (">=", IntVal(a), IntVal(b))      => BoolVal(a >= b)
    case ("==", a, b)                      => BoolVal(equalValues(a, b))
    case ("!=", a, b)                      => BoolVal(!equalValues(a, b))
    case _ => throw new EvalException(s"invalid runtime operation: ${left.show} $op ${right.show}")
  }

  private def equalValues(a: Value, b: Value): Boolean = (a, b) match {
    case (IntVal(x), IntVal(y))       => x == y
    case (BoolVal(x), BoolVal(y))     => x == y
    case (StringVal(x), StringVal(y)) => x == y
    case (UnitVal, UnitVal)           => true
    case (PairVal(a1, a2), PairVal(b1, b2)) => equalValues(a1, b1) && equalValues(a2, b2)
    case _ => false
  }
}

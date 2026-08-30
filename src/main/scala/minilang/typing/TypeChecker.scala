package minilang.typing

import minilang.ast.*
import minilang.ast.TypeOps.*

object TypeChecker {
  type Env = Map[String, Type]
  type TypeVars = Set[String]

  def infer(expr: Expr): Either[MiniTypeError, Type] = infer(expr, Map.empty, Set.empty)

  private def validateType(tpe: Type, vars: TypeVars): Either[MiniTypeError, Unit] = tpe match {
    case TInt | TBool | TString | TUnit => Right(())
    case TVar(n) => if (vars(n)) Right(()) else Left(UnknownTypeVariable(n))
    case TPair(a, b) => for { _ <- validateType(a, vars); _ <- validateType(b, vars) } yield ()
    case TFun(a, b)  => for { _ <- validateType(a, vars); _ <- validateType(b, vars) } yield ()
    case TForAll(p, body) => validateType(body, vars + p)
  }

  private def infer(expr: Expr, env: Env, typeVars: TypeVars): Either[MiniTypeError, Type] = expr match {
    case IntLit(_)    => Right(TInt)
    case BoolLit(_)   => Right(TBool)
    case StringLit(_) => Right(TString)
    case UnitLit      => Right(TUnit)
    case Var(name)    => env.get(name).toRight(UndefinedVariable(name))

    case Let(name, value, body) =>
      for {
        valueType <- infer(value, env, typeVars)
        bodyType  <- infer(body, env + (name -> valueType), typeVars)
      } yield bodyType

    case LetRec(name, param, paramType, returnType, fnBody, inExpr) =>
      for {
        _ <- validateType(paramType, typeVars)
        _ <- validateType(returnType, typeVars)
        declared = TFun(paramType, returnType)
        bodyType <- infer(fnBody, env + (name -> declared) + (param -> paramType), typeVars)
        _ <- if (bodyType == returnType) Right(())
             else Left(RecursiveReturnMismatch(name, returnType.show, bodyType.show))
        result <- infer(inExpr, env + (name -> declared), typeVars)
      } yield result

    case IfThenElse(cond, yes, no) =>
      for {
        condType <- infer(cond, env, typeVars)
        _ <- if (condType == TBool) Right(()) else Left(ExpectedBooleanCondition(condType.show))
        yesType <- infer(yes, env, typeVars)
        noType  <- infer(no, env, typeVars)
        _ <- if (yesType == noType) Right(()) else Left(BranchTypeMismatch(yesType.show, noType.show))
      } yield yesType

    case Lambda(typeParam, param, paramType, returnType, body) =>
      val scopedVars = typeVars ++ typeParam.toSet
      for {
        _ <- validateType(paramType, scopedVars)
        _ <- validateType(returnType, scopedVars)
        bodyType <- infer(body, env + (param -> paramType), scopedVars)
        _ <- if (bodyType == returnType) Right(())
             else Left(TypeMismatch(returnType.show, bodyType.show, s"body of function '$param'"))
      } yield typeParam match {
        case Some(tp) => TForAll(tp, TFun(paramType, returnType))
        case None     => TFun(paramType, returnType)
      }

    case TypeApply(function, typeArg) =>
      for {
        _ <- validateType(typeArg, typeVars)
        fnType <- infer(function, env, typeVars)
        result <- fnType match {
          case TForAll(param, body) => Right(substitute(body, param, typeArg))
          case other               => Left(ExpectedPolymorphicFunction(other.show))
        }
      } yield result

    case Apply(function, argument) =>
      for {
        fnType  <- infer(function, env, typeVars)
        argType <- infer(argument, env, typeVars)
        result <- fnType match {
          case TFun(from, to) if from == argType => Right(to)
          case TFun(from, _) => Left(TypeMismatch(from.show, argType.show, "function argument"))
          case other => Left(ExpectedFunction(other.show))
        }
      } yield result

    case PairExpr(left, right) =>
      for { a <- infer(left, env, typeVars); b <- infer(right, env, typeVars) } yield TPair(a, b)

    case Fst(pair) => infer(pair, env, typeVars).flatMap {
      case TPair(a, _) => Right(a)
      case other       => Left(ExpectedPair(other.show, "fst"))
    }

    case Snd(pair) => infer(pair, env, typeVars).flatMap {
      case TPair(_, b) => Right(b)
      case other       => Left(ExpectedPair(other.show, "snd"))
    }

    case Unary(op, value) => infer(value, env, typeVars).flatMap {
      case TInt if op == "-"   => Right(TInt)
      case TBool if op == "!"  => Right(TBool)
      case other                => Left(InvalidOperandTypes(op, other.show, None))
    }

    case Binary(op, left, right) =>
      for {
        lt <- infer(left, env, typeVars)
        rt <- infer(right, env, typeVars)
        result <- binaryType(op, lt, rt)
      } yield result
  }

  private def binaryType(op: String, left: Type, right: Type): Either[MiniTypeError, Type] = op match {
    case "+" if left == TInt && right == TInt => Right(TInt)
    case "+" if left == TString && right == TString => Right(TString)
    case "-" | "*" | "/" if left == TInt && right == TInt => Right(TInt)
    case "<" | "<=" | ">" | ">=" if left == TInt && right == TInt => Right(TBool)
    case "&&" | "||" if left == TBool && right == TBool => Right(TBool)
    case "==" | "!=" if left == right && !left.isInstanceOf[TFun] && !left.isInstanceOf[TForAll] => Right(TBool)
    case _ => Left(InvalidOperandTypes(op, left.show, Some(right.show)))
  }
}

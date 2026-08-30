package minilang.parser

import minilang.ast.*
import minilang.lexer.*

class ParseException(message: String) extends RuntimeException(message)

final class Parser(tokens: Vector[Token]) {
  private var pos = 0

  private def current: Token = tokens(pos)
  private def atEnd: Boolean = current.isInstanceOf[Eof]

  private def error(msg: String): Nothing =
    throw new ParseException(s"Parse error near '${current.lexeme}' at offset ${current.offset}: $msg")

  private def acceptSymbol(s: String): Boolean = current match {
    case SymbolToken(`s`, _) => pos += 1; true
    case _                   => false
  }

  private def expectSymbol(s: String): Unit = if (!acceptSymbol(s)) error(s"expected '$s'")

  private def acceptKeyword(k: String): Boolean = current match {
    case KeywordToken(`k`, _) => pos += 1; true
    case _                    => false
  }

  private def expectKeyword(k: String): Unit = if (!acceptKeyword(k)) error(s"expected keyword '$k'")

  private def expectIdent(): String = current match {
    case Ident(name, _) => pos += 1; name
    case _              => error("expected identifier")
  }

  def parseProgram(): Expr = {
    val expr = parseExpr()
    if (!atEnd) error("unexpected trailing input")
    expr
  }

  private def parseExpr(): Expr = {
    if (acceptKeyword("let")) parseLet()
    else if (acceptKeyword("if")) parseIf()
    else if (acceptKeyword("fun")) parseLambda()
    else parseOr()
  }

  private def parseLet(): Expr = {
    if (acceptKeyword("rec")) {
      val name = expectIdent()
      expectSymbol("(")
      val param = expectIdent()
      expectSymbol(":")
      val paramType = parseType()
      expectSymbol(")")
      expectSymbol(":")
      val returnType = parseType()
      expectSymbol("=")
      val fnBody = parseExpr()
      expectKeyword("in")
      val inExpr = parseExpr()
      LetRec(name, param, paramType, returnType, fnBody, inExpr)
    } else {
      val name = expectIdent()
      expectSymbol("=")
      val value = parseExpr()
      expectKeyword("in")
      val body = parseExpr()
      Let(name, value, body)
    }
  }

  private def parseIf(): Expr = {
    val cond = parseExpr()
    expectKeyword("then")
    val yes = parseExpr()
    expectKeyword("else")
    val no = parseExpr()
    IfThenElse(cond, yes, no)
  }

  private def parseLambda(): Expr = {
    val typeParam =
      if (acceptSymbol("[")) {
        val p = expectIdent()
        expectSymbol("]")
        Some(p)
      } else None

    expectSymbol("(")
    val param = expectIdent()
    expectSymbol(":")
    val paramType = parseType()
    expectSymbol(")")
    expectSymbol(":")
    val returnType = parseType()
    expectSymbol("=>")
    val body = parseExpr()
    Lambda(typeParam, param, paramType, returnType, body)
  }

  private def parseOr(): Expr = chain(() => parseAnd(), Set("||"))
  private def parseAnd(): Expr = chain(() => parseEquality(), Set("&&"))
  private def parseEquality(): Expr = chain(() => parseComparison(), Set("==", "!="))
  private def parseComparison(): Expr = chain(() => parseAdditive(), Set("<", "<=", ">", ">="))
  private def parseAdditive(): Expr = chain(() => parseMultiplicative(), Set("+", "-"))
  private def parseMultiplicative(): Expr = chain(() => parseUnary(), Set("*", "/"))

  private def chain(next: () => Expr, ops: Set[String]): Expr = {
    var left = next()
    var continue = true
    while (continue) {
      current match {
        case SymbolToken(op, _) if ops(op) =>
          pos += 1
          val right = next()
          left = Binary(op, left, right)
        case _ => continue = false
      }
    }
    left
  }

  private def parseUnary(): Expr = current match {
    case SymbolToken("!", _) => pos += 1; Unary("!", parseUnary())
    case SymbolToken("-", _) => pos += 1; Unary("-", parseUnary())
    case KeywordToken("fst", _) =>
      pos += 1; expectSymbol("("); val e = parseExpr(); expectSymbol(")"); Fst(e)
    case KeywordToken("snd", _) =>
      pos += 1; expectSymbol("("); val e = parseExpr(); expectSymbol(")"); Snd(e)
    case _ => parsePostfix()
  }

  private def parsePostfix(): Expr = {
    var expr = parsePrimary()
    var continue = true
    while (continue) {
      if (acceptSymbol("[")) {
        val tpe = parseType()
        expectSymbol("]")
        expr = TypeApply(expr, tpe)
      } else if (acceptSymbol("(")) {
        val arg = parseExpr()
        expectSymbol(")")
        expr = Apply(expr, arg)
      } else continue = false
    }
    expr
  }

  private def parsePrimary(): Expr = current match {
    case IntToken(v, _, _)    => pos += 1; IntLit(v)
    case StringToken(v, _, _) => pos += 1; StringLit(v)
    case KeywordToken("true", _)  => pos += 1; BoolLit(true)
    case KeywordToken("false", _) => pos += 1; BoolLit(false)
    case KeywordToken("unit", _)  => pos += 1; UnitLit
    case Ident(name, _)       => pos += 1; Var(name)
    case SymbolToken("(", _) =>
      pos += 1
      val first = parseExpr()
      if (acceptSymbol(",")) {
        val second = parseExpr()
        expectSymbol(")")
        PairExpr(first, second)
      } else {
        expectSymbol(")")
        first
      }
    case _ => error("expected expression")
  }

  private def parseType(): Type = parseArrowType()

  private def parseArrowType(): Type = {
    val left = parseAtomicType()
    if (acceptSymbol("->")) TFun(left, parseArrowType()) else left
  }

  private def parseAtomicType(): Type = current match {
    case KeywordToken("Int", _)    => pos += 1; TInt
    case KeywordToken("Bool", _)   => pos += 1; TBool
    case KeywordToken("String", _) => pos += 1; TString
    case KeywordToken("Unit", _)   => pos += 1; TUnit
    case Ident(name, _)             => pos += 1; TVar(name)
    case KeywordToken("forall", _) =>
      pos += 1
      val p = expectIdent()
      expectSymbol(".")
      TForAll(p, parseType())
    case SymbolToken("(", _) =>
      pos += 1
      val first = parseType()
      if (acceptSymbol(",")) {
        val second = parseType()
        expectSymbol(")")
        TPair(first, second)
      } else {
        expectSymbol(")")
        first
      }
    case _ => error("expected type")
  }
}

object Parser {
  def parse(input: String): Expr = new Parser(Lexer.tokenize(input)).parseProgram()
}

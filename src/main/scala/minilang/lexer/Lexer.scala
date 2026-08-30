package minilang.lexer

class LexException(message: String) extends RuntimeException(message)

object Lexer {
  private val keywords = Set(
    "let", "rec", "in", "if", "then", "else", "fun",
    "true", "false", "fst", "snd", "unit",
    "Int", "Bool", "String", "Unit", "forall"
  )

  private val twoCharSymbols = Set("=>", "->", "==", "!=", "<=", ">=", "&&", "||")
  private val oneCharSymbols = Set('(', ')', '[', ']', ',', ':', '=', '+', '-', '*', '/', '<', '>', '!', ';', '.')

  def tokenize(input: String): Vector[Token] = {
    val out = Vector.newBuilder[Token]
    var i = 0

    def fail(msg: String): Nothing = throw new LexException(s"Lexer error at offset $i: $msg")

    while (i < input.length) {
      input(i) match {
        case c if c.isWhitespace => i += 1

        case '/' if i + 1 < input.length && input(i + 1) == '/' =>
          i += 2
          while (i < input.length && input(i) != '\n') i += 1

        case c if c.isDigit =>
          val start = i
          while (i < input.length && input(i).isDigit) i += 1
          val raw = input.substring(start, i)
          out += IntToken(raw.toInt, raw, start)

        case c if c.isLetter || c == '_' =>
          val start = i
          i += 1
          while (i < input.length && (input(i).isLetterOrDigit || input(i) == '_')) i += 1
          val raw = input.substring(start, i)
          if (keywords(raw)) out += KeywordToken(raw, start)
          else out += Ident(raw, start)

        case '"' =>
          val start = i
          i += 1
          val sb = new StringBuilder
          var closed = false
          while (i < input.length && !closed) {
            input(i) match {
              case '"' => closed = true; i += 1
              case '\\' if i + 1 < input.length =>
                val escaped = input(i + 1) match {
                  case 'n'  => '\n'
                  case 't'  => '\t'
                  case 'r'  => '\r'
                  case '"' => '"'
                  case '\\' => '\\'
                  case other => other
                }
                sb += escaped
                i += 2
              case ch => sb += ch; i += 1
            }
          }
          if (!closed) fail("unterminated string literal")
          out += StringToken(sb.toString, input.substring(start, i), start)

        case _ =>
          val two = if (i + 1 < input.length) input.substring(i, i + 2) else ""
          if (twoCharSymbols(two)) {
            out += SymbolToken(two, i)
            i += 2
          } else if (oneCharSymbols(input(i))) {
            out += SymbolToken(input(i).toString, i)
            i += 1
          } else {
            fail(s"unexpected character '${input(i)}'")
          }
      }
    }

    out += Eof(input.length)
    out.result()
  }
}

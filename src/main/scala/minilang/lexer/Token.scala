package minilang.lexer

sealed trait Token { def lexeme: String; def offset: Int }
case class Ident(lexeme: String, offset: Int) extends Token
case class IntToken(value: Int, lexeme: String, offset: Int) extends Token
case class StringToken(value: String, lexeme: String, offset: Int) extends Token
case class SymbolToken(lexeme: String, offset: Int) extends Token
case class KeywordToken(lexeme: String, offset: Int) extends Token
case class Eof(offset: Int) extends Token { val lexeme = "<eof>" }

package com.github.grian32.gl3ijplugin.lexer

import com.github.grian32.gl3ijplugin.psi.Gl3Types
import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

// as close as possible of a port of the gl3 lexer, few differences:
// - whitespace/comments returned as WHITE_SPACE / COMMENT instead of skip as ij requires
// - tokens are offset ranges into the buffer, so there's some internal token data missinng
// - unknown == tokentype.badchar, eof == null
class Gl3Lexer : LexerBase() {
    private var input: CharSequence = ""
    private var endOffset = 0

    private var pos = 0
    private var readPos = 0
    private var ch = NUL

    private var tokenType: IElementType? = null
    private var tokenStart = 0
    private var tokenEnd = 0

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        input = buffer
        this.endOffset = endOffset
        readPos = startOffset
        readChar()
        advance()
    }

    override fun getState() = 0
    override fun getTokenType() = tokenType
    override fun getTokenStart() = tokenStart
    override fun getTokenEnd() = tokenEnd
    override fun getBufferSequence() = input
    override fun getBufferEnd() = endOffset

    override fun advance() {
        tokenStart = minOf(pos, endOffset)
        tokenType = nextToken()
        tokenEnd = minOf(pos, endOffset)
    }

    private fun readChar() {
        ch = if (readPos >= endOffset) NUL else input[readPos]

        pos = readPos
        readPos += 1
    }

    private fun peekChar(): Char = if (readPos >= endOffset) NUL else input[readPos]

    private fun nextToken(): IElementType? {
        if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
            while (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
                readChar()
            }
            return TokenType.WHITE_SPACE
        }

        if (ch == '/' && peekChar() == '/') {
            while (ch != '\n' && ch != NUL) {
                readChar()
            }
            return Gl3Types.COMMENT
        }

        val sct = singleCharToken[ch]
        if (sct != null) {
            readChar()
            return sct
        }

        var tok: IElementType = TokenType.BAD_CHARACTER

        when (ch) {
            '+' -> {
                tok = Gl3Types.PLUS
                tok = extendToken(tok, '=', Gl3Types.PLUS_ASSIGN)
            }
            '*' -> {
                tok = Gl3Types.ASTERISK
                tok = extendToken(tok, '=', Gl3Types.ASTERISK_ASSIGN)
            }
            '/' -> {
                tok = Gl3Types.SLASH
                tok = extendToken(tok, '=', Gl3Types.SLASH_ASSIGN)
            }
            '%' -> {
                tok = Gl3Types.PERCENT
                tok = extendToken(tok, '=', Gl3Types.PERCENT_ASSIGN)
            }
            '^' -> {
                tok = Gl3Types.CARET
                tok = extendToken(tok, '=', Gl3Types.CARET_ASSIGN)
            }
            '-' -> {
                tok = Gl3Types.MINUS
                if (peekChar() == '>') {
                    tok = extendToken(tok, '>', Gl3Types.ARROW)
                } else {
                    tok = extendToken(tok, '=', Gl3Types.MINUS_ASSIGN)
                }
            }
            '&' -> {
                tok = Gl3Types.AMPERSAND
                if (peekChar() == '&') {
                    tok = extendToken(tok, '&', Gl3Types.LAND)
                } else {
                    tok = extendToken(tok, '=', Gl3Types.AMPERSAND_ASSIGN)
                }
            }
            '|' -> {
                tok = Gl3Types.PIPE
                if (peekChar() == '|') {
                    tok = extendToken(tok, '|', Gl3Types.LOR)
                } else {
                    tok = extendToken(tok, '=', Gl3Types.PIPE_ASSIGN)
                }
            }
            '=' -> {
                tok = Gl3Types.ASSIGN
                tok = extendToken(tok, '=', Gl3Types.EQ)
            }
            '!' -> {
                tok = Gl3Types.NOT
                tok = extendToken(tok, '=', Gl3Types.NOTEQ)
            }
            '<' -> {
                tok = Gl3Types.LT
                if (peekChar() == '<') {
                    tok = extendToken(tok, '<', Gl3Types.SHL)
                    tok = extendToken(tok, '=', Gl3Types.SHL_ASSIGN)
                } else {
                    tok = extendToken(tok, '=', Gl3Types.LTEQ)
                }
            }
            '>' -> {
                tok = Gl3Types.GT
                if (peekChar() == '>') {
                    tok = extendToken(tok, '>', Gl3Types.SHR)
                    tok = extendToken(tok, '=', Gl3Types.SHR_ASSIGN)
                } else {
                    tok = extendToken(tok, '=', Gl3Types.GTEQ)
                }
            }
            '.' -> {
                if (peekChar() == '.' && endOffset > readPos + 1 && input[readPos + 1] == '.') {
                    readChar()
                    readChar()
                    tok = Gl3Types.ELLIPSIS
                } else {
                    tok = Gl3Types.DOT
                }
            }
            NUL -> {
                // A NUL past the end of the buffer is EOF, a literal NUL character is not
                if (pos >= endOffset) {
                    return null
                }
            }
            else -> {
                if (ch == '0' && peekChar() == 'x') {
                    readHexaInt()
                    readSuffix()
                    return Gl3Types.INT
                }

                if (isDigit(ch)) {
                    val tt = readNumber()
                    readSuffix()
                    return tt
                }

                if (isAlpha(ch)) {
                    val start = pos
                    readChar()
                    readIdentifier()
                    return identLookup(input.subSequence(start, minOf(pos, endOffset)).toString())
                }

                if (ch == '"') {
                    readChar()
                    tok = Gl3Types.STRING
                    readString()
                }

                if (ch == '\'') {
                    readChar()
                    tok = Gl3Types.CHAR
                    while (ch != '\'' && ch != '\n' && ch != NUL) {
                        if (ch == '\\') {
                            readEscape()
                        }
                        readChar()
                    }
                    if (ch != '\'') {
                        // unterminated character literal: leave the newline for the whitespace token
                        return tok
                    }
                }
            }
        }

        readChar()
        return tok
    }

    private fun readString() {
        while (ch != '"') {
            if (ch == NUL) {
                break
            }
            if (ch == '\\') {
                readEscape()
            }
            readChar()
        }
    }

    // readEscape consumes the character after a backslash.
    private fun readEscape() {
        readChar()
    }

    private fun readSuffix() {
        while (isAlphaNumeric(ch)) {
            readChar()
        }
    }

    private fun readHexaInt() {
        readChar()
        readChar()

        while (isHexaNumeral(ch)) {
            readChar()
        }
    }

    private fun readNumber(): IElementType {
        var tt = Gl3Types.INT

        while (isDigit(ch) || ch == '.') {
            if (ch == '.') {
                tt = Gl3Types.FLOAT
            }
            readChar()
        }

        return tt
    }

    private fun readIdentifier() {
        while (isAlphaNumeric(ch)) {
            readChar()
        }
    }

    private fun extendToken(tok: IElementType, next: Char, tt: IElementType): IElementType {
        if (peekChar() != next) {
            return tok
        }
        readChar()
        return tt
    }

    private companion object {
        const val NUL = '\u0000'

        val singleCharToken = mapOf(
            '~' to Gl3Types.TILDE,
            ';' to Gl3Types.SEMICOLON,
            '(' to Gl3Types.LPAREN,
            ')' to Gl3Types.RPAREN,
            '{' to Gl3Types.LBRACE,
            '}' to Gl3Types.RBRACE,
            '[' to Gl3Types.LBRACKET,
            ']' to Gl3Types.RBRACKET,
            ',' to Gl3Types.COMMA,
            ':' to Gl3Types.COLON,
        )

        fun identLookup(lit: String): IElementType = when (lit) {
            "int", "int32", "int16", "int8",
            "uint", "uint8", "uint16", "uint32",
            "none", "bool", "float", "float32", "char" -> Gl3Types.TYPE
            "def" -> Gl3Types.DEF
            "fnc" -> Gl3Types.FNC
            "return" -> Gl3Types.RETURN
            "true" -> Gl3Types.TRUE
            "false" -> Gl3Types.FALSE
            "as" -> Gl3Types.AS
            "sizeof" -> Gl3Types.SIZEOF
            "vararg" -> Gl3Types.VARARG
            "import" -> Gl3Types.IMPORT
            "if" -> Gl3Types.IF
            "else" -> Gl3Types.ELSE
            "while" -> Gl3Types.WHILE
            "struct" -> Gl3Types.STRUCT
            "break" -> Gl3Types.BREAK
            "continue" -> Gl3Types.CONTINUE
            "global" -> Gl3Types.GLOBAL
            "const" -> Gl3Types.CONST
            "extern" -> Gl3Types.EXTERN
            "private" -> Gl3Types.PRIVATE
            "nullptr" -> Gl3Types.NULLPTR
            else -> Gl3Types.IDENT
        }

        fun isDigit(c: Char) = c in '0'..'9'

        fun isAlpha(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'

        fun isAlphaNumeric(c: Char) = c in '0'..'9' || c in 'a'..'z' || c in 'A'..'Z' || c == '_'

        fun isHexaNumeral(c: Char) = c in 'A'..'F' || c in 'a'..'f' || c in '0'..'9'
    }
}

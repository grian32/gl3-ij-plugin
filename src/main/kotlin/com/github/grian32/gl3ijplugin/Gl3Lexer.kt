package com.github.grian32.gl3ijplugin

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
            return Gl3Tokens.COMMENT
        }

        val sct = singleCharToken[ch]
        if (sct != null) {
            readChar()
            return sct
        }

        var tok: IElementType = TokenType.BAD_CHARACTER

        when (ch) {
            '+' -> {
                tok = Gl3Tokens.PLUS
                tok = extendToken(tok, '=', Gl3Tokens.PLUS_ASSIGN)
            }
            '*' -> {
                tok = Gl3Tokens.ASTERISK
                tok = extendToken(tok, '=', Gl3Tokens.ASTERISK_ASSIGN)
            }
            '/' -> {
                tok = Gl3Tokens.SLASH
                tok = extendToken(tok, '=', Gl3Tokens.SLASH_ASSIGN)
            }
            '%' -> {
                tok = Gl3Tokens.PERCENT
                tok = extendToken(tok, '=', Gl3Tokens.PERCENT_ASSIGN)
            }
            '^' -> {
                tok = Gl3Tokens.CARET
                tok = extendToken(tok, '=', Gl3Tokens.CARET_ASSIGN)
            }
            '-' -> {
                tok = Gl3Tokens.MINUS
                if (peekChar() == '>') {
                    tok = extendToken(tok, '>', Gl3Tokens.ARROW)
                } else {
                    tok = extendToken(tok, '=', Gl3Tokens.MINUS_ASSIGN)
                }
            }
            '&' -> {
                tok = Gl3Tokens.AMPERSAND
                if (peekChar() == '&') {
                    tok = extendToken(tok, '&', Gl3Tokens.LAND)
                } else {
                    tok = extendToken(tok, '=', Gl3Tokens.AMPERSAND_ASSIGN)
                }
            }
            '|' -> {
                tok = Gl3Tokens.PIPE
                if (peekChar() == '|') {
                    tok = extendToken(tok, '|', Gl3Tokens.LOR)
                } else {
                    tok = extendToken(tok, '=', Gl3Tokens.PIPE_ASSIGN)
                }
            }
            '=' -> {
                tok = Gl3Tokens.ASSIGN
                tok = extendToken(tok, '=', Gl3Tokens.EQ)
            }
            '!' -> {
                tok = Gl3Tokens.NOT
                tok = extendToken(tok, '=', Gl3Tokens.NOTEQ)
            }
            '<' -> {
                tok = Gl3Tokens.LT
                if (peekChar() == '<') {
                    tok = extendToken(tok, '<', Gl3Tokens.SHL)
                    tok = extendToken(tok, '=', Gl3Tokens.SHL_ASSIGN)
                } else {
                    tok = extendToken(tok, '=', Gl3Tokens.LTEQ)
                }
            }
            '>' -> {
                tok = Gl3Tokens.GT
                if (peekChar() == '>') {
                    tok = extendToken(tok, '>', Gl3Tokens.SHR)
                    tok = extendToken(tok, '=', Gl3Tokens.SHR_ASSIGN)
                } else {
                    tok = extendToken(tok, '=', Gl3Tokens.GTEQ)
                }
            }
            '.' -> {
                if (peekChar() == '.' && endOffset > readPos + 1 && input[readPos + 1] == '.') {
                    readChar()
                    readChar()
                    tok = Gl3Tokens.ELLIPSIS
                } else {
                    tok = Gl3Tokens.DOT
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
                    return Gl3Tokens.INT
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
                    tok = Gl3Tokens.STRING
                    readString()
                }

                if (ch == '\'') {
                    readChar()
                    tok = Gl3Tokens.CHAR
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
        var tt = Gl3Tokens.INT

        while (isDigit(ch) || ch == '.') {
            if (ch == '.') {
                tt = Gl3Tokens.FLOAT
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
            '~' to Gl3Tokens.TILDE,
            ';' to Gl3Tokens.SEMICOLON,
            '(' to Gl3Tokens.LPAREN,
            ')' to Gl3Tokens.RPAREN,
            '{' to Gl3Tokens.LBRACE,
            '}' to Gl3Tokens.RBRACE,
            '[' to Gl3Tokens.LBRACKET,
            ']' to Gl3Tokens.RBRACKET,
            ',' to Gl3Tokens.COMMA,
            ':' to Gl3Tokens.COLON,
        )

        fun identLookup(lit: String): IElementType = when (lit) {
            "int", "int32", "int16", "int8",
            "uint", "uint8", "uint16", "uint32",
            "none", "bool", "float", "float32", "char" -> Gl3Tokens.TYPE
            "def" -> Gl3Tokens.DEF
            "fnc" -> Gl3Tokens.FNC
            "return" -> Gl3Tokens.RETURN
            "true" -> Gl3Tokens.TRUE
            "false" -> Gl3Tokens.FALSE
            "as" -> Gl3Tokens.AS
            "sizeof" -> Gl3Tokens.SIZEOF
            "vararg" -> Gl3Tokens.VARARG
            "import" -> Gl3Tokens.IMPORT
            "if" -> Gl3Tokens.IF
            "else" -> Gl3Tokens.ELSE
            "while" -> Gl3Tokens.WHILE
            "struct" -> Gl3Tokens.STRUCT
            "break" -> Gl3Tokens.BREAK
            "continue" -> Gl3Tokens.CONTINUE
            "global" -> Gl3Tokens.GLOBAL
            "const" -> Gl3Tokens.CONST
            "extern" -> Gl3Tokens.EXTERN
            "private" -> Gl3Tokens.PRIVATE
            "nullptr" -> Gl3Tokens.NULLPTR
            else -> Gl3Tokens.IDENT
        }

        fun isDigit(c: Char) = c in '0'..'9'

        fun isAlpha(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'

        fun isAlphaNumeric(c: Char) = c in '0'..'9' || c in 'a'..'z' || c in 'A'..'Z' || c == '_'

        fun isHexaNumeral(c: Char) = c in 'A'..'F' || c in 'a'..'f' || c in '0'..'9'
    }
}

package com.github.grian32.gl3ijplugin

import com.intellij.psi.tree.TokenSet

// as close to 1:1 to gl3 as possible
object Gl3Tokens {
    @JvmField val INT = Gl3TokenType("INT")
    @JvmField val FLOAT = Gl3TokenType("FLOAT")
    @JvmField val STRING = Gl3TokenType("STRING")
    @JvmField val CHAR = Gl3TokenType("CHAR")
    @JvmField val IDENT = Gl3TokenType("IDENT")

    @JvmField val COMMENT = Gl3TokenType("COMMENT")

    @JvmField val PLUS = Gl3TokenType("PLUS")
    @JvmField val MINUS = Gl3TokenType("MINUS")
    @JvmField val ASTERISK = Gl3TokenType("ASTERISK")
    @JvmField val SLASH = Gl3TokenType("SLASH")
    @JvmField val PERCENT = Gl3TokenType("PERCENT")
    @JvmField val AMPERSAND = Gl3TokenType("AMPERSAND")
    @JvmField val PIPE = Gl3TokenType("PIPE")
    @JvmField val CARET = Gl3TokenType("CARET")
    @JvmField val TILDE = Gl3TokenType("TILDE")
    @JvmField val SHL = Gl3TokenType("SHL")
    @JvmField val SHR = Gl3TokenType("SHR")

    @JvmField val ASSIGN = Gl3TokenType("ASSIGN")
    @JvmField val PLUS_ASSIGN = Gl3TokenType("PLUS_ASSIGN")
    @JvmField val MINUS_ASSIGN = Gl3TokenType("MINUS_ASSIGN")
    @JvmField val ASTERISK_ASSIGN = Gl3TokenType("ASTERISK_ASSIGN")
    @JvmField val SLASH_ASSIGN = Gl3TokenType("SLASH_ASSIGN")
    @JvmField val PERCENT_ASSIGN = Gl3TokenType("PERCENT_ASSIGN")
    @JvmField val AMPERSAND_ASSIGN = Gl3TokenType("AMPERSAND_ASSIGN")
    @JvmField val PIPE_ASSIGN = Gl3TokenType("PIPE_ASSIGN")
    @JvmField val CARET_ASSIGN = Gl3TokenType("CARET_ASSIGN")
    @JvmField val SHL_ASSIGN = Gl3TokenType("SHL_ASSIGN")
    @JvmField val SHR_ASSIGN = Gl3TokenType("SHR_ASSIGN")

    @JvmField val NOT = Gl3TokenType("NOT")
    @JvmField val LAND = Gl3TokenType("LAND")
    @JvmField val LOR = Gl3TokenType("LOR")
    @JvmField val EQ = Gl3TokenType("EQ")
    @JvmField val NOTEQ = Gl3TokenType("NOTEQ")
    @JvmField val LT = Gl3TokenType("LT")
    @JvmField val LTEQ = Gl3TokenType("LTEQ")
    @JvmField val GT = Gl3TokenType("GT")
    @JvmField val GTEQ = Gl3TokenType("GTEQ")

    @JvmField val SEMICOLON = Gl3TokenType("SEMICOLON")
    @JvmField val COMMA = Gl3TokenType("COMMA")
    @JvmField val DOT = Gl3TokenType("DOT")
    @JvmField val COLON = Gl3TokenType("COLON")
    @JvmField val ARROW = Gl3TokenType("ARROW")
    @JvmField val ELLIPSIS = Gl3TokenType("ELLIPSIS")
    @JvmField val LPAREN = Gl3TokenType("LPAREN")
    @JvmField val RPAREN = Gl3TokenType("RPAREN")
    @JvmField val LBRACE = Gl3TokenType("LBRACE")
    @JvmField val RBRACE = Gl3TokenType("RBRACE")
    @JvmField val LBRACKET = Gl3TokenType("LBRACKET")
    @JvmField val RBRACKET = Gl3TokenType("RBRACKET")

    @JvmField val TYPE = Gl3TokenType("TYPE")

    @JvmField val DEF = Gl3TokenType("DEF")
    @JvmField val FNC = Gl3TokenType("FNC")
    @JvmField val RETURN = Gl3TokenType("RETURN")
    @JvmField val TRUE = Gl3TokenType("TRUE")
    @JvmField val FALSE = Gl3TokenType("FALSE")
    @JvmField val AS = Gl3TokenType("AS")
    @JvmField val SIZEOF = Gl3TokenType("SIZEOF")
    @JvmField val VARARG = Gl3TokenType("VARARG")
    @JvmField val IMPORT = Gl3TokenType("IMPORT")
    @JvmField val IF = Gl3TokenType("IF")
    @JvmField val ELSE = Gl3TokenType("ELSE")
    @JvmField val WHILE = Gl3TokenType("WHILE")
    @JvmField val STRUCT = Gl3TokenType("STRUCT")
    @JvmField val BREAK = Gl3TokenType("BREAK")
    @JvmField val CONTINUE = Gl3TokenType("CONTINUE")
    @JvmField val GLOBAL = Gl3TokenType("GLOBAL")
    @JvmField val CONST = Gl3TokenType("CONST")
    @JvmField val EXTERN = Gl3TokenType("EXTERN")
    @JvmField val PRIVATE = Gl3TokenType("PRIVATE")
    @JvmField val NULLPTR = Gl3TokenType("NULLPTR")

    @JvmField val KEYWORDS = TokenSet.create(
        DEF, FNC, RETURN, TRUE, FALSE, AS, SIZEOF, VARARG, IMPORT, IF, ELSE, WHILE,
        STRUCT, BREAK, CONTINUE, GLOBAL, CONST, EXTERN, PRIVATE, NULLPTR,
    )
}

package com.github.grian32.gl3ijplugin.parser

import com.github.grian32.gl3ijplugin.psi.Gl3Types
import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.TokenType

object Gl3ParserUtil : GeneratedParserUtilBase() {
    // mirrors gl3's Token.NewlineBefore: true if the whitespace/comments right before the current token have no newline
    @JvmStatic
    fun noNewlineBefore(builder: PsiBuilder, @Suppress("UNUSED_PARAMETER") level: Int): Boolean {
        // PsiBuilder skips whitespace lazily, force it so rawLookup(-1) is the token right before the current one
        builder.tokenType
        var i = -1
        while (true) {
            val type = builder.rawLookup(i) ?: return true
            if (type != TokenType.WHITE_SPACE && type != Gl3Types.COMMENT) return true
            if (type == TokenType.WHITE_SPACE) {
                val start = builder.rawTokenTypeStart(i)
                val end = builder.rawTokenTypeStart(i + 1)
                if (builder.originalText.subSequence(start, end).contains('\n')) return false
            }
            i--
        }
    }
}

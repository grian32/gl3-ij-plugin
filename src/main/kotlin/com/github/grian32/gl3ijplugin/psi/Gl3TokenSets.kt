package com.github.grian32.gl3ijplugin.psi

import com.intellij.psi.tree.TokenSet

object Gl3TokenSets {
    @JvmField val KEYWORDS = TokenSet.create(
        Gl3Types.DEF, Gl3Types.FNC, Gl3Types.RETURN, Gl3Types.TRUE, Gl3Types.FALSE, Gl3Types.AS,
        Gl3Types.SIZEOF, Gl3Types.VARARG, Gl3Types.IMPORT, Gl3Types.IF, Gl3Types.ELSE, Gl3Types.WHILE,
        Gl3Types.STRUCT, Gl3Types.BREAK, Gl3Types.CONTINUE, Gl3Types.GLOBAL, Gl3Types.CONST,
        Gl3Types.EXTERN, Gl3Types.PRIVATE, Gl3Types.NULLPTR,
    )

    @JvmField val COMMENTS = TokenSet.create(Gl3Types.COMMENT)
}

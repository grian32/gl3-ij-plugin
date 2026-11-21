package com.github.grian32.gl3ijplugin.editor

import com.github.grian32.gl3ijplugin.psi.Gl3Types
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class Gl3BraceMatcher : PairedBraceMatcher {
    override fun getPairs() = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?) = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int) = openingBraceOffset

    private companion object {
        val PAIRS = arrayOf(
            BracePair(Gl3Types.LBRACE, Gl3Types.RBRACE, true),
            BracePair(Gl3Types.LPAREN, Gl3Types.RPAREN, false),
            BracePair(Gl3Types.LBRACKET, Gl3Types.RBRACKET, false),
        )
    }
}

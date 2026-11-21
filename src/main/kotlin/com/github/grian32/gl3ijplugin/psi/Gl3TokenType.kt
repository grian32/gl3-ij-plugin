package com.github.grian32.gl3ijplugin.psi

import com.github.grian32.gl3ijplugin.lang.Gl3Language
import com.intellij.psi.tree.IElementType

class Gl3TokenType(debugName: String) : IElementType(debugName, Gl3Language) {
    override fun toString() = "Gl3TokenType." + super.toString()
}

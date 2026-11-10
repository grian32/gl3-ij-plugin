package com.github.grian32.gl3ijplugin

import com.intellij.psi.tree.IElementType

class Gl3TokenType(debugName: String) : IElementType(debugName, Gl3Language) {
    override fun toString() = "Gl3TokenType." + super.toString()
}

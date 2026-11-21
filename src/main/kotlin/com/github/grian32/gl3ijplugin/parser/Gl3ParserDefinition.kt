package com.github.grian32.gl3ijplugin.parser

import com.github.grian32.gl3ijplugin.lang.Gl3Language
import com.github.grian32.gl3ijplugin.lexer.Gl3Lexer
import com.github.grian32.gl3ijplugin.psi.Gl3File
import com.github.grian32.gl3ijplugin.psi.Gl3TokenSets
import com.github.grian32.gl3ijplugin.psi.Gl3Types
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class Gl3ParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?) = Gl3Lexer()
    override fun createParser(project: Project?) = Gl3Parser()
    override fun getFileNodeType() = FILE
    override fun getCommentTokens() = Gl3TokenSets.COMMENTS
    override fun getStringLiteralElements() = STRINGS
    override fun createElement(node: ASTNode): PsiElement = Gl3Types.Factory.createElement(node)
    override fun createFile(viewProvider: FileViewProvider) = Gl3File(viewProvider)

    companion object {
        val FILE = IFileElementType(Gl3Language)
        private val STRINGS = TokenSet.create(Gl3Types.STRING)
    }
}

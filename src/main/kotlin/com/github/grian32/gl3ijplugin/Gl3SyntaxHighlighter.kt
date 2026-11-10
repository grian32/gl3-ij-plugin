package com.github.grian32.gl3ijplugin

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class Gl3SyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer() = Gl3Lexer()

    override fun getTokenHighlights(t: IElementType): Array<TextAttributesKey> = when (t) {
        in Gl3Tokens.KEYWORDS-> pack(KEYWORD)
        Gl3Tokens.TYPE -> pack(PRIMITIVE_TYPES)
        Gl3Tokens.INT -> pack(NUMBER)
        Gl3Tokens.STRING -> pack(STRING)
        Gl3Tokens.COMMENT -> pack(COMMENT)
        TokenType.BAD_CHARACTER -> pack(BAD_CHAR)
        else -> TextAttributesKey.EMPTY_ARRAY
    }

    companion object {
        val KEYWORD = createTextAttributesKey("GL3_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val PRIMITIVE_TYPES = createTextAttributesKey("GL3_PRIMITIVE_TYPES", DefaultLanguageHighlighterColors.KEYWORD)
        val NUMBER = createTextAttributesKey("GL3_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val STRING = createTextAttributesKey("GL3_STRING", DefaultLanguageHighlighterColors.STRING)
        val COMMENT = createTextAttributesKey("GL3_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val BAD_CHAR = createTextAttributesKey("GL3_BAD_CHAR", HighlighterColors.BAD_CHARACTER)
    }
}

class Gl3SyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, file: VirtualFile?) = Gl3SyntaxHighlighter()
}
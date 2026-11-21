package com.github.grian32.gl3ijplugin.editor

import com.github.grian32.gl3ijplugin.psi.Gl3Block
import com.github.grian32.gl3ijplugin.psi.Gl3StructDecl
import com.github.grian32.gl3ijplugin.psi.Gl3Types
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

// folds multi-line { } of blocks (fnc/if/else/while bodies) and struct bodies
class Gl3FoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        fun addBraces(node: ASTNode) {
            val lbrace = node.findChildByType(Gl3Types.LBRACE) ?: return
            // an unclosed brace has nothing sensible to fold
            val rbrace = node.findChildByType(Gl3Types.RBRACE) ?: return
            val range = TextRange(lbrace.startOffset, rbrace.textRange.endOffset)
            if (document.getLineNumber(range.startOffset) == document.getLineNumber(range.endOffset)) return
            descriptors += FoldingDescriptor(node, range)
        }

        PsiTreeUtil.findChildrenOfType(root, Gl3Block::class.java).forEach { addBraces(it.node) }
        PsiTreeUtil.findChildrenOfType(root, Gl3StructDecl::class.java).forEach { addBraces(it.node) }

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode) = "{...}"

    override fun isCollapsedByDefault(node: ASTNode) = false
}

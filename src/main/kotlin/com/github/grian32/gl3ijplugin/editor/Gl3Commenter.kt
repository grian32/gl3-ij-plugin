package com.github.grian32.gl3ijplugin.editor

import com.intellij.lang.Commenter

// gl3 only has line comments
class Gl3Commenter : Commenter {
    override fun getLineCommentPrefix() = "// "
    override fun getBlockCommentPrefix(): String? = null
    override fun getBlockCommentSuffix(): String? = null
    override fun getCommentedBlockCommentPrefix(): String? = null
    override fun getCommentedBlockCommentSuffix(): String? = null
}

package com.github.grian32.gl3ijplugin

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.annotations.NonNls

object Gl3Language : Language("GL3")

object Gl3FileType : LanguageFileType(Gl3Language) {
    override fun getName() = "GL3"
    override fun getDescription() = "GL3 source file"
    override fun getDefaultExtension() = "gl3"
    override fun getIcon() = Gl3Icons.FILE
}

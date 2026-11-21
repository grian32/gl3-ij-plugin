package com.github.grian32.gl3ijplugin.psi

import com.github.grian32.gl3ijplugin.lang.Gl3FileType
import com.github.grian32.gl3ijplugin.lang.Gl3Language
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider

class Gl3File(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, Gl3Language) {
    override fun getFileType() = Gl3FileType
    override fun toString() = "GL3 File"
}

package com.github.grian32.gl3ijplugin.editor

import com.github.grian32.gl3ijplugin.psi.Gl3Types
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler

class Gl3QuoteHandler : SimpleTokenSetQuoteHandler(Gl3Types.STRING, Gl3Types.CHAR)

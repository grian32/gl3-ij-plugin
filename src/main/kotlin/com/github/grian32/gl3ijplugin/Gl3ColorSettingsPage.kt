package com.github.grian32.gl3ijplugin

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage

class Gl3ColorSettingsPage : ColorSettingsPage {
    override fun getDisplayName() = "GL3"
    override fun getIcon() = Gl3Icons.FILE
    override fun getHighlighter() = Gl3SyntaxHighlighter()

    override fun getDemoText() = """
        // a comment
        import "io"

        struct Point {
            int32 x
            int32 y
        }

        global const int answer = 42

        extern fnc free(none* ptr) -> none

        fnc add(int32 a, int32 b) -> int32 {
            return a + b
        }

        fnc main() -> int32 {
            def Point p = Point:{ 1i32, 0x2i32 }
            def float f = 1.5
            def char c = 'a'
            def uint size = sizeof int32
            def int32* ptr = nullptr
            while p.x < 10i32 && true {
                p.x += 1i32
            }
            if ptr == nullptr {
                println("sum: %d\n", add(p.x, p.y) as int)
            } else {
                return 1i32
            }
            def int bad = 1 $ 2
            return 0i32
        }
    """.trimIndent()

    override fun getAttributeDescriptors() = DESCRIPTORS
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Keyword", Gl3SyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Primitive type", Gl3SyntaxHighlighter.PRIMITIVE_TYPES),
            AttributesDescriptor("Number", Gl3SyntaxHighlighter.NUMBER),
            AttributesDescriptor("String", Gl3SyntaxHighlighter.STRING),
            AttributesDescriptor("Comment", Gl3SyntaxHighlighter.COMMENT),
            AttributesDescriptor("Bad character", Gl3SyntaxHighlighter.BAD_CHAR),
        )
    }
}

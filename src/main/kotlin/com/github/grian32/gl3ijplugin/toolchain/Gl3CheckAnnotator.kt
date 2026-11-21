package com.github.grian32.gl3ijplugin.toolchain

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths

// runs `gl3 check --path <file>` with the unsaved editor text on stdin and shows its diagnostics.
// gl3 check stops after sema so this is cheap, and the daemon only calls it once typing pauses
class Gl3CheckAnnotator : ExternalAnnotator<Gl3CheckAnnotator.Input, List<Gl3CheckAnnotator.Diagnostic>>(), DumbAware {
    data class Input(val binary: Path, val path: Path, val text: String)

    // lines/cols are 1 based like gl3's positions
    data class Diagnostic(
        val error: Boolean,
        val message: String,
        val startLine: Int,
        val startCol: Int,
        val endLine: Int,
        val endCol: Int,
    )

    // always run, even when our own parser found errors, since gl3's errors are the source of truth
    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean) = collectInformation(file)

    override fun collectInformation(file: PsiFile): Input? {
        val vFile = file.virtualFile ?: return null
        if (!vFile.isInLocalFileSystem) return null
        val binary = Gl3Toolchain.getInstance().binaryOrNull() ?: return null
        return Input(binary, vFile.toNioPath(), file.text)
    }

    override fun doAnnotate(input: Input): List<Diagnostic> {
        val cmd = GeneralCommandLine(input.binary.toString(), "check", "--path", input.path.toString())
            .withWorkDirectory(input.path.parent?.toFile())
            .withCharset(StandardCharsets.UTF_8)
        val handler = CapturingProcessHandler(cmd)
        // gl3 reads all of stdin before writing anything, so writing first can't deadlock
        handler.processInput.use { it.write(input.text.toByteArray(StandardCharsets.UTF_8)) }
        val output = handler.runProcess(TIMEOUT_MS)

        if (output.isTimeout) {
            thisLogger().warn("gl3 check timed out on ${input.path}")
            return emptyList()
        }
        // exit 1 just means there were errors, anything else is gl3 itself failing
        if (output.exitCode != 0 && output.exitCode != 1) {
            thisLogger().warn("gl3 check exited with ${output.exitCode}: ${output.stderr}")
            return emptyList()
        }

        return try {
            parse(output.stdout, input.path)
        } catch (e: Exception) {
            thisLogger().warn("could not parse gl3 check output: ${output.stdout}", e)
            emptyList()
        }
    }

    override fun apply(file: PsiFile, diagnostics: List<Diagnostic>, holder: AnnotationHolder) {
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return
        for (d in diagnostics) {
            val severity = if (d.error) HighlightSeverity.ERROR else HighlightSeverity.WARNING
            val builder = holder.newAnnotation(severity, d.message)
            val range = toRange(document, d)
            if (range.isEmpty) {
                // errors at the end of a line or the file, eg. a missing } at eof
                builder.range(TextRange(range.startOffset, range.startOffset)).afterEndOfLine().create()
            } else {
                builder.range(range).create()
            }
        }
    }

    private fun parse(stdout: String, path: Path): List<Diagnostic> {
        val diagnostics = JsonParser.parseString(stdout).asJsonObject["diagnostics"].asJsonArray
        return diagnostics.map { it.asJsonObject }
            // diagnostics of imported files come with their own path, the import line gets its own error
            .filter { samePath(it["file"].asString, path) }
            .map { d ->
                val range = d["range"]?.takeIf { it.isJsonObject }?.asJsonObject
                Diagnostic(
                    error = d["severity"].asString == "error",
                    message = d["message"].asString,
                    startLine = range.pos("start", "line"),
                    startCol = range.pos("start", "col"),
                    endLine = range.pos("end", "line"),
                    endCol = range.pos("end", "col"),
                )
            }
    }

    private fun JsonObject?.pos(which: String, field: String): Int =
        this?.get(which)?.asJsonObject?.get(field)?.asInt ?: 1

    private fun samePath(a: String, b: Path) = try {
        Paths.get(a).normalize() == b.normalize()
    } catch (_: Exception) {
        false
    }

    private fun toRange(document: Document, d: Diagnostic): TextRange {
        val start = offset(document, d.startLine, d.startCol)
        val end = offset(document, d.endLine, d.endCol)
        if (end > start) return TextRange(start, end)
        // a zero width range in the middle of a line still gets one char so it's visible
        val lineEnd = document.getLineEndOffset(document.getLineNumber(start))
        return if (start < lineEnd) TextRange(start, start + 1) else TextRange(start, start)
    }

    // gl3 reports positions past the last line for errors at eof, so clamp to the document
    private fun offset(document: Document, line: Int, col: Int): Int {
        if (document.lineCount == 0) return 0
        val lineIdx = (line - 1).coerceIn(0, document.lineCount - 1)
        val lineStart = document.getLineStartOffset(lineIdx)
        val lineEnd = document.getLineEndOffset(lineIdx)
        return (lineStart + col - 1).coerceIn(lineStart, lineEnd)
    }

    private companion object {
        const val TIMEOUT_MS = 10_000
    }
}

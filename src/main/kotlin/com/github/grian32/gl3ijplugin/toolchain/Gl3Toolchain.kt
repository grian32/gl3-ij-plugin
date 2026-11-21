package com.github.grian32.gl3ijplugin.toolchain

import com.google.gson.JsonParser
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.io.Decompressor
import com.intellij.util.io.HttpRequests
import com.intellij.util.system.CpuArch
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.GZIPInputStream
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

// downloads the latest gl3 release for this os/arch into the ide system dir, once per ide session.
// versions live in <system>/gl3/<tag>/, if github can't be reached the newest cached one is used
@Service(Service.Level.APP)
class Gl3Toolchain {
    @Volatile
    private var binary: Path? = null
    private val resolving = AtomicBoolean(false)

    // the binary if it's ready, otherwise starts resolving it in the background and returns null
    fun binaryOrNull(): Path? {
        binary?.let { return it }
        if (resolving.compareAndSet(false, true)) {
            ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Setting up gl3", false) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        binary = resolve(indicator)
                    } catch (e: Exception) {
                        thisLogger().warn("could not set up gl3", e)
                        binary = newestCached()
                        if (binary == null) notifyError("Could not download gl3: ${e.message}")
                    }
                    if (binary != null) restartHighlighting()
                }
            })
        }
        return null
    }

    private fun resolve(indicator: ProgressIndicator): Path? {
        val asset = assetName() ?: run {
            notifyError("No gl3 release is available for ${SystemInfo.OS_NAME} ${CpuArch.CURRENT}")
            return null
        }

        indicator.text = "Checking latest gl3 release"
        val release = JsonParser.parseString(
            HttpRequests.request(LATEST_RELEASE_URL).accept("application/vnd.github+json").readString(indicator)
        ).asJsonObject
        val tag = release["tag_name"].asString
        val versionDir = rootDir().resolve(tag)
        val exe = versionDir.resolve(exeName())
        if (exe.exists()) return exe

        val url = release["assets"].asJsonArray
            .map { it.asJsonObject }
            .firstOrNull { it["name"].asString == asset }
            ?.get("browser_download_url")?.asString
            ?: error("release $tag has no $asset asset")

        indicator.text = "Downloading gl3 $tag"
        val archive = Files.createTempFile("gl3-", asset)
        try {
            HttpRequests.request(url).saveToFile(archive, indicator)
            // extract next to the final dir and move it in, so a half extracted version never gets used
            val tmpDir = Files.createTempDirectory(rootDir().also { Files.createDirectories(it) }, "$tag-")
            if (asset.endsWith(".zip")) {
                Decompressor.Zip(archive).extract(tmpDir)
            } else {
                GZIPInputStream(Files.newInputStream(archive)).use { Decompressor.Tar(it).extract(tmpDir) }
            }
            tmpDir.resolve(exeName()).toFile().setExecutable(true)
            Files.move(tmpDir, versionDir)
        } finally {
            Files.deleteIfExists(archive)
        }

        deleteOtherVersions(tag)
        return exe
    }

    private fun newestCached(): Path? {
        val root = rootDir()
        if (!root.isDirectory()) return null
        return root.listDirectoryEntries()
            .filter { it.resolve(exeName()).exists() }
            .maxByOrNull { it.getLastModifiedTime() }
            ?.resolve(exeName())
    }

    private fun deleteOtherVersions(keep: String) {
        rootDir().listDirectoryEntries()
            .filter { it.name != keep }
            .forEach { it.toFile().deleteRecursively() }
    }

    private fun restartHighlighting() {
        for (project in ProjectManager.getInstance().openProjects) {
            if (!project.isDisposed) DaemonCodeAnalyzer.getInstance(project).restart("gl3 toolchain ready")
        }
    }

    private fun notifyError(msg: String) {
        NotificationGroupManager.getInstance().getNotificationGroup("GL3")
            .createNotification(msg, NotificationType.WARNING)
            .notify(null)
    }

    companion object {
        private const val LATEST_RELEASE_URL = "https://api.github.com/repos/grian32/gl3/releases/latest"

        fun getInstance(): Gl3Toolchain = service()

        private fun rootDir(): Path = PathManager.getSystemDir().resolve("gl3")

        private fun exeName() = if (SystemInfo.isWindows) "gl3.exe" else "gl3"

        // matches the asset names from the gl3 release workflow
        private fun assetName(): String? = when {
            SystemInfo.isLinux && CpuArch.isIntel64() -> "gl3-linux-amd64.tar.gz"
            SystemInfo.isLinux && CpuArch.isArm64() -> "gl3-linux-arm64.tar.gz"
            SystemInfo.isMac && CpuArch.isArm64() -> "gl3-darwin-arm64.tar.gz"
            SystemInfo.isWindows && CpuArch.isIntel64() -> "gl3-windows-amd64.zip"
            else -> null
        }
    }
}

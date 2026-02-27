package tech.zhifu.app.myhub.component.media.internal

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import java.awt.Desktop
import java.io.File
import java.net.URI

actual fun openInSystemPlayer(
    file: PlatformFile,
    mimeType: String?
): Boolean {
    val rawPath = file.path
    if (rawPath.isBlank()) return false
    if (!Desktop.isDesktopSupported()) return false
    return try {
        val desktop = Desktop.getDesktop()
        val fileObj = File(rawPath.removePrefix("file://"))
        if (fileObj.exists()) {
            desktop.open(fileObj)
        } else {
            val uri = URI.create(rawPath)
            desktop.browse(uri)
        }
        true
    } catch (_: Throwable) {
        false
    }
}

actual fun isVlcAvailable(): Boolean {
    val os = System.getProperty("os.name").lowercase()
    val candidates = when {
        os.contains("mac") -> listOf(
            "/Applications/VLC.app",
            "/Applications/VLC.app/Contents/MacOS/VLC"
        )

        os.contains("win") -> listOf(
            "${System.getenv("ProgramFiles")}\\VideoLAN\\VLC\\vlc.exe",
            "${System.getenv("ProgramFiles(x86)")}\\VideoLAN\\VLC\\vlc.exe"
        )

        else -> listOf("/usr/bin/vlc", "/snap/bin/vlc", "/usr/local/bin/vlc")
    }
    if (candidates.any { it.isNotBlank() && File(it).exists() }) return true
    val path = System.getenv("PATH") ?: return false
    return path.split(File.pathSeparator).any { dir ->
        val file = File(dir, if (os.contains("win")) "vlc.exe" else "vlc")
        file.exists()
    }
}

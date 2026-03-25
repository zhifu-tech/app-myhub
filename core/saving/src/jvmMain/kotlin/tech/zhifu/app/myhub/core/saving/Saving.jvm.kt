package tech.zhifu.app.myhub.core.saving

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Composable
actual fun rememberImageSavingSupported(): Boolean = true

@Composable
actual fun rememberImageSaver(): ImageSaver {
    return remember {
        ImageSaver { imagePath ->
            withContext(Dispatchers.IO) {
                try {
                    val source =
                        resolveFilePath(imagePath) ?: return@withContext ImageSavingResult.Failure("invalid image path")
                    if (!source.exists()) return@withContext ImageSavingResult.Failure("image file not found")

                    val targetDir = resolveSavingDir()
                    Files.createDirectories(targetDir)
                    val target = targetDir.resolve("myhub_preview_${System.currentTimeMillis()}.png")
                    Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING)
                    ImageSavingResult.Success
                } catch (_: Exception) {
                    ImageSavingResult.Failure("save failed")
                }
            }
        }
    }
}

private fun resolveFilePath(imagePath: String): File? {
    return if (imagePath.startsWith("file://")) {
        File(imagePath.removePrefix("file://"))
    } else {
        File(imagePath)
    }
}

private fun resolveSavingDir(): Path {
    val userHome = System.getProperty("user.home")
    if (!userHome.isNullOrBlank()) {
        val defaultSavingDir = Path.of(userHome, "Downloads")
        if (Files.exists(defaultSavingDir) || Files.exists(defaultSavingDir.parent)) {
            return defaultSavingDir
        }
    }
    return Path.of(System.getProperty("java.io.tmpdir"), "myhub-saved-images")
}

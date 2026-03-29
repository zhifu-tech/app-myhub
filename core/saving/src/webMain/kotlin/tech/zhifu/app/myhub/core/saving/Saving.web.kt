package tech.zhifu.app.myhub.core.saving

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import kotlin.random.Random
import org.w3c.dom.HTMLAnchorElement

@Composable
actual fun rememberImageSaver(): ImageSaver {
    return remember {
        ImageSaver { imagePath ->
            if (imagePath.isBlank()) {
                return@ImageSaver ImageSavingResult.Failure("invalid image path")
            }
            try {
                downloadImage(imagePath)
                ImageSavingResult.Success
            } catch (_: Exception) {
                ImageSavingResult.Failure("save failed")
            }
        }
    }
}

@Composable
actual fun rememberImageSavingSupported(): Boolean = true

private fun downloadImage(imagePath: String) {
    require(!imagePath.startsWith("file://")) { "file URL is not supported on web" }
    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = imagePath
    anchor.download = "myhub_preview_${Random.nextInt(Int.MAX_VALUE)}.png"
    document.body?.appendChild(anchor)
    anchor.click()
    anchor.remove()
}

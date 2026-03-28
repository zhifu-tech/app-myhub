package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.zhifu.app.myhub.ui.model.ContentCard
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

@Composable
internal actual fun rememberPreviewSnapshot(
    card: ContentCard,
    width: Dp,
    snapshotController: PreviewSnapshotController?,
    exportMode: ExportMode,
): suspend () -> String? = remember(card, width, snapshotController, exportMode) {
    suspend {
        when (exportMode) {
            ExportMode.Visible -> snapshotController?.captureVisibleSnapshot?.invoke()
            ExportMode.FullContent -> snapshotController?.captureVisibleSnapshot?.invoke()
        }
    }
}

@Composable
internal actual fun Modifier.previewSnapshotSource(
    controller: PreviewSnapshotController?,
): Modifier {
    if (controller == null) return this

    val graphicsLayer = rememberGraphicsLayer()
    val capture = rememberUpdatedState(
        suspend {
            val imageBitmap = graphicsLayer.toImageBitmap()
            saveImageBitmapToTempFile(imageBitmap)
        }
    )

    DisposableEffect(controller) {
        controller.captureVisibleSnapshot = { capture.value.invoke() }
        onDispose {
            if (controller.captureVisibleSnapshot === capture.value) {
                controller.captureVisibleSnapshot = null
            }
        }
    }

    return this.drawWithContent {
        graphicsLayer.record { this@drawWithContent.drawContent() }
        drawContent()
    }
}

private suspend fun saveImageBitmapToTempFile(bitmap: ImageBitmap): String? {
    return withContext(Dispatchers.IO) {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return@withContext null

        val buffered = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val pixelMap = bitmap.toPixelMap()
        for (y in 0 until height) {
            for (x in 0 until width) {
                buffered.setRGB(x, y, pixelMap[x, y].toArgb())
            }
        }

        val file = File(
            System.getProperty("java.io.tmpdir"),
            "preview_share_${System.currentTimeMillis()}.png",
        )
        if (ImageIO.write(buffered, "png", file)) {
            "file://${file.absolutePath}"
        } else {
            null
        }
    }
}

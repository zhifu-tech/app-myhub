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
import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import tech.zhifu.app.myhub.datastore.model.domain.ContentCard

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
            imageBitmapToPngDataUrl(imageBitmap)
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

@OptIn(ExperimentalWasmJsInterop::class)
private fun imageBitmapToPngDataUrl(bitmap: ImageBitmap): String? {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0) return null

    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = width
    canvas.height = height
    val context = canvas.getContext("2d") as? CanvasRenderingContext2D ?: return null
    val pixelMap = bitmap.toPixelMap()
    val rgbaCache = HashMap<Int, String>(256)
    var currentFillColor: Int? = null

    for (y in 0 until height) {
        var runStartX = 0
        var runColor = pixelMap[0, y].toArgb()

        fun drawRun(endExclusiveX: Int) {
            if (endExclusiveX <= runStartX) return
            if (currentFillColor != runColor) {
                val rgba = rgbaCache.getOrPut(runColor) {
                    val r = (runColor shr 16) and 0xFF
                    val g = (runColor shr 8) and 0xFF
                    val b = runColor and 0xFF
                    val a = ((runColor ushr 24) and 0xFF) / 255.0
                    "rgba($r,$g,$b,$a)"
                }
                context.fillStyle = rgba.toJsString()
                currentFillColor = runColor
            }
            context.fillRect(
                runStartX.toDouble(),
                y.toDouble(),
                (endExclusiveX - runStartX).toDouble(),
                1.0
            )
        }

        for (x in 1 until width) {
            val color = pixelMap[x, y].toArgb()
            if (color != runColor) {
                drawRun(x)
                runStartX = x
                runColor = color
            }
        }
        drawRun(width)
    }

    return canvas.toDataURL("image/png")
}

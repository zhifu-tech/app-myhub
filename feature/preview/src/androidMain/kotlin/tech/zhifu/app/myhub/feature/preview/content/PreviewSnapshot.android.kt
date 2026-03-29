package tech.zhifu.app.myhub.feature.preview.content

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.findViewTreeCompositionContext
import androidx.compose.ui.unit.Dp
import androidx.core.view.drawToBitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.design.theme.AppTheme
import tech.zhifu.app.myhub.ui.model.ContentCard
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

@Composable
internal actual fun Modifier.previewSnapshotSource(
    controller: PreviewSnapshotController?,
): Modifier = this

@Composable
internal actual fun rememberPreviewSnapshot(
    card: ContentCard,
    width: Dp,
    snapshotController: PreviewSnapshotController?,
    exportMode: ExportMode,
): suspend () -> String? {
    val context = LocalContext.current
    val density = LocalDensity.current
    val parentView = LocalView.current
    val parentComposition = parentView.findViewTreeCompositionContext()
    val parentLifecycleOwner = parentView.findViewTreeLifecycleOwner()
    val parentSavedStateOwner = parentView.findViewTreeSavedStateRegistryOwner()

    return remember(context, density, card, width, parentComposition, snapshotController, exportMode) {
        suspend {
            val visible = if (exportMode == ExportMode.Visible) {
                snapshotController?.captureVisibleSnapshot?.invoke()
            } else {
                null
            }
            visible ?: captureFullContentImage(
                context = context,
                density = density,
                parentView = parentView,
                parentComposition = parentComposition,
                parentLifecycleOwner = parentLifecycleOwner,
                parentSavedStateOwner = parentSavedStateOwner,
                payload = card,
                width = width,
            )
        }
    }
}

private suspend fun captureFullContentImage(
    context: android.content.Context,
    density: androidx.compose.ui.unit.Density,
    parentView: View,
    parentComposition: CompositionContext?,
    parentLifecycleOwner: LifecycleOwner?,
    parentSavedStateOwner: SavedStateRegistryOwner?,
    payload: ContentCard,
    width: Dp,
): String? {
    val bitmap = withContext(Dispatchers.Main.immediate) {
        val widthPx = with(density) { width.roundToPx() }.coerceAtLeast(1)
        val composeView = ComposeView(context)
        val rootView = parentView.rootView as? ViewGroup ?: return@withContext null
        val container = FrameLayout(context)

        if (parentComposition != null) composeView.setParentCompositionContext(parentComposition)
        if (parentLifecycleOwner != null) composeView.setViewTreeLifecycleOwner(parentLifecycleOwner)
        if (parentSavedStateOwner != null) composeView.setViewTreeSavedStateRegistryOwner(parentSavedStateOwner)

        composeView.setContent {
            AppTheme {
                PreviewContent(
                    card = payload,
                    modifier = Modifier.width(width),
                )
            }
        }

        composeView.layoutParams = ViewGroup.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(composeView)
        rootView.addView(container, ViewGroup.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT))

        val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        container.measure(widthSpec, heightSpec)
        container.layout(0, 0, container.measuredWidth, container.measuredHeight)

        try {
            if (composeView.measuredWidth <= 0 || composeView.measuredHeight <= 0) null else composeView.drawToBitmap()
        } finally {
            rootView.removeView(container)
            composeView.disposeComposition()
        }
    }

    if (bitmap == null) return null
    return withContext(Dispatchers.IO) {
        try {
            val imageFile = File(
                context.cacheDir,
                "preview_share_${System.currentTimeMillis()}_${Random.nextInt(1000)}.png",
            )
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            "file://${imageFile.absolutePath}"
        } catch (e: Exception) {
            logger.error(e) { "Failed to generate preview share image" }
            null
        }
    }
}

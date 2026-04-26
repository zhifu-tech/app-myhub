package tech.zhifu.app.myhub.component.media.internal

import android.content.Intent
import android.net.Uri
import io.github.vinceglb.filekit.path
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tech.zhifu.app.myhub.component.media.MediaItem

actual fun openInSystemPlayer(
    item: MediaItem,
    mimeType: String?
): Boolean {
    val path = item.previewUrl.ifBlank { item.file?.path.orEmpty() }
    if (path.isBlank()) return false
    val contextProvider = object : KoinComponent {
        val context by inject<android.content.Context>()
    }
    val uri = Uri.parse(path)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return try {
        contextProvider.context.startActivity(intent)
        true
    } catch (_: Throwable) {
        false
    }
}

actual fun isVlcAvailable(): Boolean = true

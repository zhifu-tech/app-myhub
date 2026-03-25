package tech.zhifu.app.myhub.feature.preview.content

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

@Composable
internal actual fun rememberPreviewDownloader(
): PreviewDownloader {
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionRequester = rememberRuntimePermissionRequester(
        permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val currentPermissionRequester = rememberUpdatedState(permissionRequester)

    return remember(context) {
        PreviewDownloader { imagePath ->
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                val granted = currentPermissionRequester.value.request(context)
                if (!granted) {
                    return@PreviewDownloader PreviewDownloadResult.PermissionDenied
                }
            }

            val savedUri = withContext(Dispatchers.IO) { saveToGallery(context, imagePath) }
            if (savedUri == null) {
                return@PreviewDownloader PreviewDownloadResult.Failure("save failed")
            }

            withContext(Dispatchers.Main.immediate) {
                val openGalleryIntent =
                    Intent(Intent.ACTION_VIEW).apply {
                        data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                context.startActivity(openGalleryIntent)
            }

            PreviewDownloadResult.Success
        }
    }
}

@Composable
internal actual fun rememberPreviewDownloadSupported(): Boolean = true

@Composable
private fun rememberRuntimePermissionRequester(
    permission: String
): PermissionRequester {
    val continuationHolder = remember { mutableListOf<kotlin.coroutines.Continuation<Boolean>>() }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val pending = continuationHolder.toList()
            continuationHolder.clear()
            pending.forEach { it.resume(granted) }
        }

    return remember(permission, launcher) {
        PermissionRequester { context ->
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                suspendCancellableCoroutine { continuation ->
                    continuationHolder.add(continuation)
                    launcher.launch(permission)
                }
            }
        }
    }
}

private fun saveToGallery(context: Context, imagePath: String): android.net.Uri? {
    val filePath = imagePath.removePrefix("file://")
    val sourceFile = File(filePath)
    if (!sourceFile.exists()) return null

    val resolver = context.contentResolver
    val fileName = "myhub_preview_${System.currentTimeMillis()}.png"
    val contentValues =
        android.content.ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null
    resolver.openOutputStream(uri)?.use { output ->
        sourceFile.inputStream().use { input ->
            input.copyTo(output)
        }
    } ?: return null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val update = android.content.ContentValues().apply {
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }
        resolver.update(uri, update, null, null)
    }
    return uri
}

private fun interface PermissionRequester {
    suspend fun request(context: Context): Boolean
}

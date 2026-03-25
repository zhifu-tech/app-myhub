package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import kotlin.coroutines.resume

@Composable
internal actual fun rememberPreviewDownloader(): PreviewDownloader {
    return remember {
        PreviewDownloader { imagePath ->
            val authorized = requestPhotoPermission()
            if (!authorized) return@PreviewDownloader PreviewDownloadResult.PermissionDenied

            val filePath = imagePath.removePrefix("file://")
            val image = UIImage(contentsOfFile = filePath)
                ?: return@PreviewDownloader PreviewDownloadResult.Failure("invalid image path")

            val saved = saveImageToPhotos(image)
            if (saved) PreviewDownloadResult.Success else PreviewDownloadResult.Failure("save failed")
        }
    }
}

@Composable
internal actual fun rememberPreviewDownloadSupported(): Boolean = true

private suspend fun requestPhotoPermission(): Boolean {
    val status = PHPhotoLibrary.authorizationStatus()
    return when (status) {
        PHAuthorizationStatusAuthorized,
        PHAuthorizationStatusLimited -> true

        PHAuthorizationStatusDenied -> false
        PHAuthorizationStatusNotDetermined -> {
            suspendCancellableCoroutine { continuation ->
                PHPhotoLibrary.requestAuthorization { newStatus ->
                    continuation.resume(
                        newStatus == PHAuthorizationStatusAuthorized ||
                            newStatus == PHAuthorizationStatusLimited
                    )
                }
            }
        }

        else -> false
    }
}

private suspend fun saveImageToPhotos(image: UIImage): Boolean {
    return suspendCancellableCoroutine { continuation ->
        PHPhotoLibrary.sharedPhotoLibrary().performChanges(
            changeBlock = {
                PHAssetChangeRequest.creationRequestForAssetFromImage(image)
            },
            completionHandler = { success, _ ->
                continuation.resume(success)
            },
        )
    }
}

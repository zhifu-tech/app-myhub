package tech.zhifu.app.myhub.core.saving

import androidx.compose.runtime.Composable

sealed interface ImageSavingResult {

    data object Success : ImageSavingResult

    data object PermissionDenied : ImageSavingResult

    data object Unsupported : ImageSavingResult

    data class Failure(val message: String? = null) : ImageSavingResult
}

fun interface ImageSaver {
    suspend fun save(imagePath: String): ImageSavingResult
}

@Composable
expect fun rememberImageSavingSupported(): Boolean

@Composable
expect fun rememberImageSaver(): ImageSaver

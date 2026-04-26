package tech.zhifu.app.myhub.component.media

import io.github.vinceglb.filekit.PlatformFile

data class MediaItem(
    val id: String,
    val name: String,
    val previewUrl: String,
    val isVideo: Boolean = false,
    val file: PlatformFile? = null,
    val thumbnailUrl: String? = null,
)

package tech.zhifu.app.myhub.component.media

import io.github.vinceglb.filekit.PlatformFile

data class MediaItem(
    val id: String,
    val file: PlatformFile,
    val name: String,
    val isVideo: Boolean = false
)

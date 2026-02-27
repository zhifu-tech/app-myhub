package tech.zhifu.app.myhub.component.media

import io.github.vinceglb.filekit.PlatformFile

interface MediaPicker {
    suspend fun pickImagesAndVideos(maxItems: Int? = null): List<PlatformFile>
}

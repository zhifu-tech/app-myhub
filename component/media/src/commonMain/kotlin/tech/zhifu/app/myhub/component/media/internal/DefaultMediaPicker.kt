package tech.zhifu.app.myhub.component.media.internal

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import tech.zhifu.app.myhub.component.media.MediaPicker

class DefaultMediaPicker : MediaPicker {

    override suspend fun pickImagesAndVideos(
        maxItems: Int?
    ): List<PlatformFile> =
        runCatching {
            FileKit.openFilePicker(
                type = FileKitType.ImageAndVideo,
                mode = FileKitMode.Multiple(maxItems)
            )
        }.getOrNull().orEmpty()

    override suspend fun pickImages(
        maxItems: Int?
    ): List<PlatformFile> =
        runCatching {
            FileKit.openFilePicker(
                type = FileKitType.Image,
                mode = FileKitMode.Multiple(maxItems),
            )
        }.getOrNull().orEmpty()

    override suspend fun capturePhoto(): PlatformFile? =
        capturePhotoWithPlatformPicker()
}

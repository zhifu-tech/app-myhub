package tech.zhifu.app.myhub.component.media.internal

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.dialogs.openCameraPicker

internal actual suspend fun capturePhotoWithPlatformPicker(): PlatformFile? =
    runCatching {
        FileKit.openCameraPicker(
            type = FileKitCameraType.Photo,
        )
    }.getOrNull()

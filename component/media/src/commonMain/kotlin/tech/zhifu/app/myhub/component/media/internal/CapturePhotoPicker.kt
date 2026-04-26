package tech.zhifu.app.myhub.component.media.internal

import io.github.vinceglb.filekit.PlatformFile

internal expect suspend fun capturePhotoWithPlatformPicker(): PlatformFile?

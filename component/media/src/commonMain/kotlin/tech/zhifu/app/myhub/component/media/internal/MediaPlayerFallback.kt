package tech.zhifu.app.myhub.component.media.internal

import io.github.vinceglb.filekit.PlatformFile

expect fun openInSystemPlayer(
    file: PlatformFile,
    mimeType: String? = null
): Boolean

expect fun isVlcAvailable(): Boolean


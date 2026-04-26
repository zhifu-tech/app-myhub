package tech.zhifu.app.myhub.component.media.internal

import tech.zhifu.app.myhub.component.media.MediaItem

expect fun openInSystemPlayer(
    item: MediaItem,
    mimeType: String? = null
): Boolean

expect fun isVlcAvailable(): Boolean

package tech.zhifu.app.myhub.component.media.internal

import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.MediaPreviewer

class DefaultMediaPreviewer : MediaPreviewer {
    override fun openInSystemPlayer(
        item: MediaItem
    ): Boolean = openInSystemPlayer(
        item = item,
        mimeType = item.systemMimeType()
    )

    override fun isSystemPlayerPreferred(): Boolean =
        !isVlcAvailable()
}

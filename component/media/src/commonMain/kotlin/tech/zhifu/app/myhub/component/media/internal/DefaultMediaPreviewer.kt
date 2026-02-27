package tech.zhifu.app.myhub.component.media.internal

import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.MediaPreviewer
import tech.zhifu.app.myhub.component.media.util.systemMimeType

class DefaultMediaPreviewer : MediaPreviewer {
    override fun openInSystemPlayer(item: MediaItem): Boolean {
        return openInSystemPlayer(item.file, item.systemMimeType())
    }

    override fun isSystemPlayerPreferred(): Boolean {
        return !isVlcAvailable()
    }
}

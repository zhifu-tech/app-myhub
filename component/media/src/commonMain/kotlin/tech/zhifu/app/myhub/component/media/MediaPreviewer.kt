package tech.zhifu.app.myhub.component.media

interface MediaPreviewer {
    fun openInSystemPlayer(item: MediaItem): Boolean
    fun isSystemPlayerPreferred(): Boolean
}

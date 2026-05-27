package tech.zhifu.app.myhub.feature.ai.layer.storage.media

actual fun createMediaFileStore(): MediaFileStore =
    BrowserMediaFileStore()

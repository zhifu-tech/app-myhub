package tech.zhifu.app.myhub.datastore.file.storage

import kotlinx.serialization.Serializable

@Serializable
internal sealed interface WebStorageHandle {
    @Serializable
    data class BrowserStoredAsset(
        val key: String,
    ) : WebStorageHandle
}

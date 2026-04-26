package tech.zhifu.app.myhub.datastore.file.storage

import kotlinx.serialization.Serializable

@Serializable
internal sealed interface NativeStorageHandle {
    @Serializable
    data class ManagedAsset(
        val key: String,
    ) : NativeStorageHandle
}

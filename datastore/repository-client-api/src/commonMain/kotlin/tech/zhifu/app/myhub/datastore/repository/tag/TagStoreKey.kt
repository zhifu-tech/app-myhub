package tech.zhifu.app.myhub.datastore.repository.tag

import org.mobilenativefoundation.store.core5.InsertionStrategy
import org.mobilenativefoundation.store.core5.StoreKey

sealed class TagStoreKey : StoreKey<String> {

    data class ById(
        override val id: String,
    ) : TagStoreKey(), StoreKey.Single<String>

    data class ByUser(
        val userId: String,
        override val insertionStrategy: InsertionStrategy = InsertionStrategy.REPLACE
    ) : TagStoreKey(), StoreKey.Collection<String>
}

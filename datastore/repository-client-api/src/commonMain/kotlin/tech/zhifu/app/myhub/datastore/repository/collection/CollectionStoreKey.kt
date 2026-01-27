package tech.zhifu.app.myhub.datastore.repository.collection

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.InsertionStrategy
import org.mobilenativefoundation.store.core5.StoreKey

@OptIn(ExperimentalStoreApi::class)
sealed class CollectionStoreKey : StoreKey<String> {

    data class ById(
        override val id: String,
    ) : CollectionStoreKey(), StoreKey.Single<String>

    data class ByUser(
        val userId: String,
        override val insertionStrategy: InsertionStrategy = InsertionStrategy.REPLACE
    ) : CollectionStoreKey(), StoreKey.Collection<String>
}

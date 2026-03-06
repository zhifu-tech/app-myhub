package tech.zhifu.app.myhub.datastore.repository.collection

import org.mobilenativefoundation.store.core5.InsertionStrategy
import org.mobilenativefoundation.store.core5.StoreKey

sealed interface CollectionStoreKey<out Id : Any> : StoreKey<Id> {

    data class ById(
        override val id: String,
    ) : CollectionStoreKey<String>, StoreKey.Single<String>

    data class ByUser(
        val userId: String,
        override val page: Int = 1,
        override val size: Int = 10,
        override val sort: StoreKey.Sort? = null,
        override val filters: List<StoreKey.Filter<*>>? = null,
        override val insertionStrategy: InsertionStrategy = InsertionStrategy.REPLACE
    ) : CollectionStoreKey<Nothing>, StoreKey.Collection.Page
}

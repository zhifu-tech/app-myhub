package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.InsertionStrategy
import org.mobilenativefoundation.store.core5.StoreKey

@OptIn(ExperimentalStoreApi::class)
sealed interface CardStoreKey<out Id : Any> : StoreKey<Id> {

    data class ById(
        override val id: String,
    ) : CardStoreKey<String>, StoreKey.Single<String>

    data class ByIds(
        val ids: List<String>,
        override val insertionStrategy: InsertionStrategy = InsertionStrategy.REPLACE
    ) : CardStoreKey<String>, StoreKey.Collection<String>

    data class ByUser(
        val userId: String,
        override val page: Int = 1,
        override val size: Int = 20,
        override val sort: StoreKey.Sort? = null,
        override val filters: List<StoreKey.Filter<*>>? = null,
        override val insertionStrategy: InsertionStrategy = InsertionStrategy.REPLACE
    ) : CardStoreKey<Nothing>, StoreKey.Collection.Page
}

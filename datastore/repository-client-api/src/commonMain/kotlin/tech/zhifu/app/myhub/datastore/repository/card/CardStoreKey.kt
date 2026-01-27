package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.InsertionStrategy
import org.mobilenativefoundation.store.core5.StoreKey

@OptIn(ExperimentalStoreApi::class)
sealed class CardStoreKey : StoreKey<String> {

    data class ById(
        override val id: String,
    ) : CardStoreKey(), StoreKey.Single<String>

    data class ByUser(
        val userId: String,
        override val insertionStrategy: InsertionStrategy = InsertionStrategy.REPLACE
    ) : CardStoreKey(), StoreKey.Collection<String>
}

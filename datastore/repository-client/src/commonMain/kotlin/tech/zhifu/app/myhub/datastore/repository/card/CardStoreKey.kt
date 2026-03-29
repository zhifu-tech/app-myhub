package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.core5.InsertionStrategy
import org.mobilenativefoundation.store.core5.StoreKey

sealed interface CardStoreKey<out Id : Any> : StoreKey<Id> {

    data class ById(
        val userId: String,
        override val id: String,
    ) : CardStoreKey<String>, StoreKey.Single<String>

    data class ByUserCursor(
        val userId: String,
        val cursorUpdatedAt: Long? = null,
        val cursorTitle: String? = null,
        val orderByUpdated: Boolean = true,
        val orderByTitle: Boolean = false,
        val query: String? = null,
        override val cursor: String? = null,
        override val size: Int = 20,
        @Deprecated("Do not use")
        override val sort: StoreKey.Sort? = null,
        @Deprecated("Do not use")
        override val filters: List<StoreKey.Filter<*>>? = null,
        @Deprecated("Do not use")
        override val insertionStrategy: InsertionStrategy = InsertionStrategy.REPLACE
    ) : CardStoreKey<String>, StoreKey.Collection.Cursor<String>
}

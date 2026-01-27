package tech.zhifu.app.myhub.datastore.repository.collection

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.InsertionStrategy
import org.mobilenativefoundation.store.core5.StoreData
import tech.zhifu.app.myhub.datastore.model.domain.Collection

@OptIn(ExperimentalStoreApi::class)
sealed class CollectionStoreData : StoreData<String> {

    data class Single(
        val collection: Collection,
        override val id: String = collection.id
    ) : CollectionStoreData(), StoreData.Single<String>

    data class Items(
        override val items: List<Single>,
        val userId: String
    ) : CollectionStoreData(), StoreData.Collection<String, Single> {
        val collections: List<Collection> get() = items.map { it.collection }

        override fun copyWith(items: List<Single>): Items {
            return copy(items = items)
        }

        override fun insertItems(
            strategy: InsertionStrategy,
            items: List<Single>
        ): Items = copy(
            items = when (strategy) {
                InsertionStrategy.APPEND -> this.items + items
                InsertionStrategy.PREPEND -> items + this.items
                InsertionStrategy.REPLACE -> items
            }
        )

        companion object {
            fun fromCollections(collections: List<Collection>, userId: String): Items {
                return Items(
                    items = collections.map { Single(it) },
                    userId = userId
                )
            }
        }
    }
}

val CollectionStoreData.collections: List<Collection>
    get() = (this as? CollectionStoreData.Items)?.collections ?: emptyList()

val CollectionStoreData.collection: Collection?
    get() = (this as? CollectionStoreData.Single)?.collection

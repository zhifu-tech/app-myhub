package tech.zhifu.app.myhub.datastore.repository.tag

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.InsertionStrategy
import org.mobilenativefoundation.store.core5.StoreData
import tech.zhifu.app.myhub.datastore.model.domain.Tag

@OptIn(ExperimentalStoreApi::class)
sealed class TagStoreData : StoreData<String> {

    data class Single(
        val tag: Tag,
        override val id: String = tag.id
    ) : TagStoreData(), StoreData.Single<String>

    data class Collection(
        override val items: List<Single>,
        val userId: String
    ) : TagStoreData(), StoreData.Collection<String, Single> {
        val tags: List<Tag> get() = items.map { it.tag }

        override fun copyWith(items: List<Single>): Collection {
            return copy(items = items)
        }

        override fun insertItems(
            strategy: InsertionStrategy,
            items: List<Single>
        ): Collection = copy(
            items = when (strategy) {
                InsertionStrategy.APPEND -> this.items + items
                InsertionStrategy.PREPEND -> items + this.items
                InsertionStrategy.REPLACE -> items
            }
        )

        companion object {
            fun fromTags(tags: List<Tag>, userId: String): Collection {
                return Collection(
                    items = tags.map { Single(it) },
                    userId = userId
                )
            }
        }
    }
}

val TagStoreData.tags: List<Tag>
    get() = (this as? TagStoreData.Collection)?.tags ?: emptyList()

val TagStoreData.tag: Tag?
    get() = (this as? TagStoreData.Single)?.tag


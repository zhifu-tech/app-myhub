package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.InsertionStrategy
import org.mobilenativefoundation.store.core5.StoreData
import tech.zhifu.app.myhub.datastore.model.domain.Card

@OptIn(ExperimentalStoreApi::class)
sealed class CardStoreData : StoreData<String> {

    data class Single(
        val card: Card,
        override val id: String = card.id
    ) : CardStoreData(), StoreData.Single<String>

    data class Collection(
        override val items: List<Single>,
        val userId: String
    ) : CardStoreData(), StoreData.Collection<String, Single> {
        val cards: List<Card> get() = items.map { it.card }

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
            fun fromCards(cards: List<Card>, userId: String): Collection {
                return Collection(
                    items = cards.map { Single(it) },
                    userId = userId
                )
            }
        }
    }

    data class CollectionIds(
        override val items: List<Single>,
        val ids: List<String>
    ) : CardStoreData(), StoreData.Collection<String, Single> {
        val cards: List<Card> get() = items.map { it.card }

        override fun copyWith(items: List<Single>): CollectionIds {
            return copy(items = items)
        }

        override fun insertItems(
            strategy: InsertionStrategy,
            items: List<Single>
        ): CollectionIds = copy(
            items = when (strategy) {
                InsertionStrategy.APPEND -> this.items + items
                InsertionStrategy.PREPEND -> items + this.items
                InsertionStrategy.REPLACE -> items
            }
        )

        companion object {
            fun fromCards(cards: List<Card>, userId: String): Collection {
                return Collection(
                    items = cards.map { Single(it) },
                    userId = userId
                )
            }
        }
    }
}


val CardStoreData.cards: List<Card>
    get() = (this as? CardStoreData.Collection)?.cards ?: emptyList()

val CardStoreData.card: Card?
    get() = (this as? CardStoreData.Single)?.card

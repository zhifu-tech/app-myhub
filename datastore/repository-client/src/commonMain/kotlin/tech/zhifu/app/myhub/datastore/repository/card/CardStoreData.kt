package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.core5.InsertionStrategy
import org.mobilenativefoundation.store.core5.StoreData
import tech.zhifu.app.myhub.datastore.model.domain.Card

sealed class CardStoreData : StoreData<String> {

    data class Single(
        val card: Card,
        val userId: String,
        override val id: String = card.id
    ) : CardStoreData(), StoreData.Single<String>

    data class Collection(
        val userId: String,
        override val items: List<Single>,
    ) : CardStoreData(), StoreData.Collection<String, Single> {

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
                    items = cards.map {
                        Single(card = it, userId = userId)
                    },
                    userId = userId
                )
            }
        }
    }
}


val CardStoreData.cards: List<Card>
    get() = (this as? CardStoreData.Collection)?.items?.map { it.card } ?: emptyList()

val CardStoreData.card: Card?
    get() = (this as? CardStoreData.Single)?.card

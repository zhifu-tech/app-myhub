package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.CollectionListFilter
import tech.zhifu.app.myhub.datastore.datasource.CollectionSort
import tech.zhifu.app.myhub.datastore.datasource.LocalCollectionDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import kotlin.time.Instant

class LocalCollectionDataSourceImpl(
    private val database: MyHubDatabase,
) : LocalCollectionDataSource {

    override suspend fun insertCollection(collection: Collection) {
        database.transaction {
            database.collectionQueries.insertCollection(
                id = collection.id,
                name = collection.name,
                user_id = collection.userId,
                created_at = collection.createdAt.toString(),
                updated_at = collection.updatedAt.toString()
            )
            database.user_collectionQueries.insertUserCollection(
                user_id = collection.userId,
                collection_id = collection.id,
                role = "owner",
                created_at = collection.createdAt.toString(),
                updated_at = collection.updatedAt.toString()
            )
        }
    }

    override suspend fun updateCollection(collection: Collection) {
        database.collectionQueries.updateCollection(
            name = collection.name,
            updated_at = collection.updatedAt.toString(),
            id = collection.id
        )
    }

    override suspend fun getCollection(collectionId: String): Collection? {
        return database.collectionQueries
            .selectCollectionById(collectionId)
            .awaitAsOneOrNull()
            ?.toDomain()
    }

    override fun observeCollection(collectionId: String): Flow<Collection> {
        return database.collectionQueries
            .selectCollectionById(collectionId)
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { it.toDomain() }
    }

    override suspend fun getCollections(
        userId: String,
        page: Int,
        pageSize: Int
    ): List<Collection> {
        val offset = (page - 1) * pageSize

        val pagedCollections = database.collectionQueries
            .selectCollectionsByUserIdPaged(
                user_id = userId,
                limit = pageSize.toLong(),
                offset = offset.toLong()
            )
            .awaitAsList()

        if (pagedCollections.isEmpty()) {
            return emptyList()
        }

        val collectionIds = pagedCollections.map { it.id }
        val cardCounts = getCollectionCardCounts(collectionIds)
        val previewCardsMap = collectionIds.associateWith { getCollectionPreviewCards(it) }

        return pagedCollections.map { dbCollection ->
            val collection = dbCollection.toDomain()
            collection.copy(
                cardCount = cardCounts[collection.id] ?: 0,
                cards = previewCardsMap[collection.id] ?: emptyList()
            )
        }
    }

    override fun observeCollections(userId: String): Flow<List<Collection>> {
        return database.collectionQueries
            .selectCollectionsByUserId(userId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { dbCollections -> dbCollections.map { it.toDomain() } }
            .flatMapLatest { collections ->
                flow {
                    if (collections.isEmpty()) {
                        emit(emptyList())
                        return@flow
                    }
                    val collectionIds = collections.map { it.id }
                    val cardCounts = getCollectionCardCounts(collectionIds)
                    val previewCardsMap = collectionIds.associateWith { id -> getCollectionPreviewCards(id) }
                    emit(
                        collections.map { c ->
                            c.copy(
                                cardCount = cardCounts[c.id] ?: 0,
                                cards = previewCardsMap[c.id] ?: emptyList()
                            )
                        }
                    )
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCollectionsPage(
        userId: String,
        page: Int,
        size: Int,
        sort: CollectionSort?,
        filters: List<CollectionListFilter>?
    ): Flow<List<Collection>> {
        val offset = (page - 1) * size
        if (!filters.isNullOrEmpty()) {
            return observeCollections(userId).map { allItems ->
                val sorted = applyCollectionSort(allItems, sort)
                val filtered = applyCollectionFilters(sorted, filters)
                filtered.drop(offset).take(size)
            }
        }

        val query = when (sort ?: CollectionSort.NEWEST) {
            CollectionSort.NEWEST -> database.collectionQueries.selectCollectionsByUserIdPaged(
                user_id = userId,
                limit = size.toLong(),
                offset = offset.toLong()
            )

            CollectionSort.OLDEST -> database.collectionQueries.selectCollectionsByUserIdPagedOldest(
                user_id = userId,
                limit = size.toLong(),
                offset = offset.toLong()
            )
        }
        return query
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { dbCollections -> dbCollections.map { it.toDomain() } }
            .flatMapLatest { collections ->
                flow {
                    if (collections.isEmpty()) {
                        emit(emptyList())
                        return@flow
                    }
                    val collectionIds = collections.map { it.id }
                    val cardCounts = getCollectionCardCounts(collectionIds)
                    val previewCardsMap = collectionIds.associateWith { id -> getCollectionPreviewCards(id) }
                    var pageItems = collections.map { c ->
                        c.copy(
                            cardCount = cardCounts[c.id] ?: 0,
                            cards = previewCardsMap[c.id] ?: emptyList()
                        )
                    }
                    pageItems = applyCollectionFilters(pageItems, filters)
                    emit(
                        pageItems
                    )
                }
            }
    }

    override suspend fun deleteCollection(collectionId: String) {
        database.collectionQueries.deleteCollection(collectionId)
    }

    override suspend fun deleteCollections(userId: String) {
        database.collectionQueries.deleteCollectionsByUserId(userId)
    }

    override suspend fun getCollectionPreviewCards(collectionId: String): List<Card> {
        val cardIds: List<String> = database.collection_cardQueries
            .selectCollectionPreviewCardIds(collection_id = collectionId)
            .awaitAsList()

        if (cardIds.isEmpty()) {
            return emptyList()
        }

        val tagRows = database.card_tagQueries
            .selectTagsByCardIds(cardIds)
            .awaitAsList()
        val tagsByCardId = tagRows.groupBy { it.card_id }
            .mapValues { entry -> entry.value.map { it.toDomain() } }

        return cardIds.mapNotNull { cardId ->
            val fullCard = database.card_with_metadataQueries
                .selectCardWithMetadataByCardId(cardId)
                .awaitAsList()
                .firstOrNull() ?: return@mapNotNull null
            fullCard.toDomain(tagsByCardId[cardId].orEmpty())
        }
    }

    override suspend fun getCollectionCardCounts(collectionIds: List<String>): Map<String, Int> {
        if (collectionIds.isEmpty()) {
            return emptyMap()
        }
        return collectionIds.associateWith { id ->
            database.collection_cardQueries
                .selectCollectionCardCount(collection_id = id)
                .awaitAsOne()
                .toInt()
        }
    }

    override suspend fun insertCollectionCard(
        collectionId: String,
        cardId: String,
        createdAt: Instant
    ) {
        database.collection_cardQueries.insertCollectionCard(
            collection_id = collectionId,
            card_id = cardId,
            sort_order = null,
            created_at = createdAt.toString()
        )
    }

    private fun applyCollectionFilters(
        items: List<Collection>,
        filters: List<CollectionListFilter>?
    ): List<Collection> {
        if (filters.isNullOrEmpty()) return items
        var result = items
        filters.forEach { filter ->
            result = filter(result)
        }
        return result
    }

    private fun applyCollectionSort(
        items: List<Collection>,
        sort: CollectionSort?
    ): List<Collection> = when (sort ?: CollectionSort.NEWEST) {
        CollectionSort.NEWEST -> items.sortedByDescending { it.updatedAt }
        CollectionSort.OLDEST -> items.sortedBy { it.updatedAt }
    }
}

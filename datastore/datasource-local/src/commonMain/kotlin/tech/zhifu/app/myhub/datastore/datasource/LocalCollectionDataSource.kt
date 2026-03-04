package tech.zhifu.app.myhub.datastore.datasource

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import kotlin.time.Instant

enum class CollectionSort {
    NEWEST,
    OLDEST,
}

typealias CollectionListFilter = (List<Collection>) -> List<Collection>

interface LocalCollectionDataSource {
    suspend fun insertCollection(collection: Collection)

    suspend fun updateCollection(collection: Collection)

    suspend fun getCollection(collectionId: String): Collection?

    fun observeCollection(collectionId: String): Flow<Collection>

    suspend fun getCollections(userId: String, page: Int = 1, pageSize: Int = 10): List<Collection>

    fun observeCollections(userId: String): Flow<List<Collection>>

    fun observeCollectionsPage(
        userId: String,
        page: Int,
        size: Int,
        sort: CollectionSort? = null,
        filters: List<CollectionListFilter>? = null
    ): Flow<List<Collection>>

    suspend fun deleteCollection(collectionId: String)

    suspend fun deleteCollections(userId: String)

    suspend fun getCollectionPreviewCards(collectionId: String): List<Card>

    suspend fun getCollectionCardCounts(collectionIds: List<String>): Map<String, Int>

    suspend fun insertCollectionCard(
        collectionId: String,
        cardId: String,
        createdAt: Instant
    )
}

package tech.zhifu.app.myhub.datastore.repository.collection

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.core5.StoreKey
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.impl.extensions.get
import tech.zhifu.app.myhub.datastore.datasource.collection.LocalCollectionDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.domain.CollectionCard
import tech.zhifu.app.myhub.datastore.repository.impl.recordDeleteOperation
import tech.zhifu.app.myhub.datastore.repository.impl.recordInsertOperation
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.sync.SyncEntityType

class CollectionRepositoryImpl(
    private val store: CollectionStore,
    private val syncRepository: SyncRepository,
    private val localCollectionDataSource: LocalCollectionDataSource,
    private val logger: Logger = logger("CollectionRepo")
) : CollectionRepository {

    override suspend fun insertCollection(
        collection: Collection,
        needSync: Boolean
    ) {
        store.write(
            StoreWriteRequest.of(
                key = CollectionStoreKey.ById(collection.id),
                value = CollectionStoreData.Single(collection)
            )
        )

        if (needSync) {
            syncRepository.recordInsertOperation(
                userId = collection.userId,
                entityType = SyncEntityType.Collection,
                entityId = collection.id,
                payload = collection,
            )
        }
    }

    override suspend fun getCollection(
        collectionId: String
    ): CollectionStoreData = store.get<CollectionStoreKey<String>, CollectionStoreData, StoreWriteResponse>(
        key = CollectionStoreKey.ById(collectionId)
    )

    override suspend fun getCollections(
        userId: String,
        page: Int,
        size: Int,
        sort: StoreKey.Sort?,
    ): CollectionStoreData = store.get<CollectionStoreKey<String>, CollectionStoreData, StoreWriteResponse>(
        key = CollectionStoreKey.ByUser(
            userId = userId,
            page = page,
            size = size,
            sort = sort,
        )
    )

    override fun streamCollection(
        collectionId: String,
        refresh: Boolean
    ): Flow<StoreReadResponse<CollectionStoreData>> = store.stream<StoreWriteResponse>(
        request = StoreReadRequest.cached(
            key = CollectionStoreKey.ById(collectionId),
            refresh = refresh
        )
    )

    override fun streamCollections(
        userId: String,
        page: Int,
        size: Int,
        sort: StoreKey.Sort?,
        refresh: Boolean
    ): Flow<StoreReadResponse<CollectionStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.cached(
                key = CollectionStoreKey.ByUser(
                    userId = userId,
                    page = page,
                    size = size,
                    sort = sort,
                ),
                refresh = refresh
            )
        )

    override suspend fun clearCollection(
        collectionId: String
    ) {
        val collection = getCollection(collectionId).collection ?: return
        store.clear(key = CollectionStoreKey.ById(collectionId))

        syncRepository.recordDeleteOperation(
            userId = collection.userId,
            entityType = SyncEntityType.Collection,
            entityId = collectionId,
            payload = collection,
        )
    }

    override suspend fun insertCollectionCard(
        collectionCard: CollectionCard
    ) {
        localCollectionDataSource.insertCollectionCard(
            collectionId = collectionCard.collectionId,
            cardId = collectionCard.cardId,
            createdAt = collectionCard.createdAt
        )
    }
}

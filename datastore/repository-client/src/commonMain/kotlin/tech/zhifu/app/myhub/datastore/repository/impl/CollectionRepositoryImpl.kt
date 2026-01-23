package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.cache.Cache
import tech.zhifu.app.myhub.cache.CacheConfig
import tech.zhifu.app.myhub.cache.cache
import tech.zhifu.app.myhub.datastore.datasource.LocalCollectionDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalSyncDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.repository.CollectionRepository
import tech.zhifu.app.myhub.datastore.repository.SyncChangeApplier
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import tech.zhifu.app.myhub.sync.SyncPullChange

class CollectionRepositoryImpl(
    private val localCollectionDataSource: LocalCollectionDataSource,
    private val localSyncDataSource: LocalSyncDataSource,
    private val collectionCache: Cache<String, Collection> = cache(CacheConfig(maximumSize = 99))
) : CollectionRepository {

    override val syncCollectionChangeApplier: SyncChangeApplier = object : SyncChangeApplier {
        override suspend fun applyChanges(
            entity: SyncEntityType,
            operations: SyncOperations,
            change: SyncPullChange
        ) {
            when (operations) {
                SyncOperations.Insert -> localSyncDataSource.applyChange(
                    deserializer = Collection.serializer(),
                    payload = change.payload
                ) {
                    insertCollection(this, false)
                }

                SyncOperations.Delete -> {
                    localCollectionDataSource.deleteCollection(change.entityId)
                }

                else -> {}
            }
        }
    }

    override suspend fun insertCollection(collection: Collection, needSync: Boolean) {
        localCollectionDataSource.insertCollection(collection)
        if (needSync) {
            localSyncDataSource.recordInsertOperation(
                userId = collection.userId,
                entityType = SyncEntityType.Collection,
                entityId = collection.id,
                payload = collection
            )
        }
    }

    override suspend fun getCollection(collectionId: String): Collection? {
        val cached = collectionCache.get(collectionId)
        if (cached != null) {
            return cached
        }
        val collection = localCollectionDataSource.getCollection(collectionId)
        if (collection != null) {
            collectionCache.put(collectionId, collection)
        }
        return collection
    }

    override fun observeCollection(collectionId: String): Flow<Collection> {
        return localCollectionDataSource.observeCollection(collectionId)
    }
}

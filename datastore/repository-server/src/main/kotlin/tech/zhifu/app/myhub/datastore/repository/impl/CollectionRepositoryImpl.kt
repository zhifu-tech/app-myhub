package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalCollectionDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.repository.CollectionRepository

/**
 * 卡集仓库实现（服务端）
 * 使用 LocalCollectionDataSource 实现，避免代码重复
 */
class CollectionRepositoryImpl(
    private val localDataSource: LocalCollectionDataSource
) : CollectionRepository {

    override suspend fun getCollections(userId: String): List<Collection> {
        return localDataSource.getCollections(userId)
    }

    override suspend fun getCollectionById(collectionId: String): Collection? {
        return localDataSource.getCollection(collectionId)
    }

    override suspend fun createCollection(collection: Collection): Collection {
        try {
            localDataSource.insertCollection(collection)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to create collection: ${e.message}", e)
        }
        return collection
    }

    override suspend fun updateCollection(collection: Collection): Collection {
        // 检查卡集是否存在
        val existing = localDataSource.getCollection(collection.id)
            ?: throw IllegalArgumentException("Collection with id '${collection.id}' not found")

        try {
            localDataSource.updateCollection(collection)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to update collection: ${e.message}", e)
        }
        return collection
    }

    override suspend fun deleteCollection(collectionId: String) {
        localDataSource.deleteCollection(collectionId)
    }
}

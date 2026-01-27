package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.domain.Collection

interface RemoteCollectionDataSource {
    suspend fun getCollections(userId: String): List<Collection>
    suspend fun getCollectionById(id: String, userId: String): Collection?
    suspend fun createCollection(collection: Collection, userId: String): Collection
    suspend fun updateCollection(id: String, collection: Collection, userId: String): Collection
    suspend fun deleteCollection(id: String, userId: String)
}

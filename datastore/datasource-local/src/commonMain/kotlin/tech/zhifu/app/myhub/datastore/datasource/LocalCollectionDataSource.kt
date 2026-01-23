package tech.zhifu.app.myhub.datastore.datasource

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.Collection

interface LocalCollectionDataSource {
    suspend fun insertCollection(collection: Collection)

    suspend fun getCollection(collectionId: String): Collection?

    fun observeCollection(collectionId: String): Flow<Collection>

    suspend fun getCollections(userId: String): List<Collection>

    fun observeCollections(userId: String): Flow<List<Collection>>

    suspend fun deleteCollection(collectionId: String)

    suspend fun deleteCollections(userId: String)
}

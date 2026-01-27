package tech.zhifu.app.myhub.datastore.repository.collection

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.model.domain.Collection

interface CollectionRepository {

    suspend fun insertCollection(collection: Collection, needSync: Boolean = true)

    suspend fun getCollection(collectionId: String): CollectionStoreData?

    suspend fun getCollections(userId: String): CollectionStoreData?

    fun streamCollection(collectionId: String, refresh: Boolean = false): Flow<StoreReadResponse<CollectionStoreData>>

    fun streamCollections(userId: String, refresh: Boolean = false): Flow<StoreReadResponse<CollectionStoreData>>

    suspend fun clearCollection(collectionId: String)
}

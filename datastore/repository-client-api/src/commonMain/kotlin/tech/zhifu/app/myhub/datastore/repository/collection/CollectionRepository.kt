package tech.zhifu.app.myhub.datastore.repository.collection

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.StoreKey
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.domain.CollectionCard

@OptIn(ExperimentalStoreApi::class)
interface CollectionRepository {

    suspend fun insertCollection(
        collection: Collection,
        needSync: Boolean = true
    )

    suspend fun getCollection(
        collectionId: String
    ): CollectionStoreData

    suspend fun getCollections(
        userId: String,
        page: Int,
        size: Int,
        sort: StoreKey.Sort? = null,
    ): CollectionStoreData

    fun streamCollection(
        collectionId: String,
        refresh: Boolean = false
    ): Flow<StoreReadResponse<CollectionStoreData>>

    fun streamCollections(
        userId: String,
        page: Int = 1,
        size: Int = 10,
        sort: StoreKey.Sort? = null,
        refresh: Boolean = false
    ): Flow<StoreReadResponse<CollectionStoreData>>

    suspend fun clearCollection(
        collectionId: String
    )

    suspend fun insertCollectionCard(
        collectionCard: CollectionCard
    )
}

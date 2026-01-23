package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.Collection

interface CollectionRepository {

    val syncCollectionChangeApplier: SyncChangeApplier

    suspend fun insertCollection(collection: Collection, needSync: Boolean = true)

    suspend fun getCollection(collectionId: String): Collection?

    fun observeCollection(collectionId: String): Flow<Collection>
}

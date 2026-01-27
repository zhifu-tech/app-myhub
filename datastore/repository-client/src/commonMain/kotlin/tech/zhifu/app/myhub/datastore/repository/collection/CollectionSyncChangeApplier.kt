package tech.zhifu.app.myhub.datastore.repository.collection

import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.repository.impl.applyChange
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import tech.zhifu.app.myhub.sync.SyncPullChange

class CollectionSyncChangeApplier(
    private val collectionRepo: CollectionRepository,
    private val syncRepo: SyncRepository,
) : SyncChangeApplier {

    override suspend fun applyChanges(
        entity: SyncEntityType,
        operations: SyncOperations,
        change: SyncPullChange
    ) {
        when (operations) {
            SyncOperations.Insert -> syncRepo.applyChange(
                deserializer = Collection.serializer(),
                payload = change.payload
            ) {
                collectionRepo.insertCollection(this, needSync = false)
            }

            SyncOperations.Delete -> {
                collectionRepo.clearCollection(change.entityId)
            }
        }
    }
}

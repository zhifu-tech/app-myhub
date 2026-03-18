package tech.zhifu.app.myhub.datastore.repository.tag

import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.repository.sync.applyChange
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import tech.zhifu.app.myhub.sync.SyncPullChange

class TagSyncChangeApplier(
    private val tagRepo: TagRepository,
    private val syncRepo: SyncRepository,
) : SyncChangeApplier {

    override suspend fun applyChanges(
        entity: SyncEntityType,
        operations: SyncOperations,
        change: SyncPullChange
    ) {
        when (operations) {
            SyncOperations.Insert -> syncRepo.applyChange(
                deserializer = Tag.serializer(),
                payload = change.payload
            ) {
                tagRepo.insertTag(this, needSync = false)
            }

            SyncOperations.Delete -> {
                tagRepo.clearTag(change.entityId)
            }
        }
    }
}

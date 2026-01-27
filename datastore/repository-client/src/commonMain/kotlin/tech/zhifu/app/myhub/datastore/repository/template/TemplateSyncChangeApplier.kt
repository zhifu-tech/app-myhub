package tech.zhifu.app.myhub.datastore.repository.template

import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import tech.zhifu.app.myhub.datastore.repository.impl.applyChange
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import tech.zhifu.app.myhub.sync.SyncPullChange

class TemplateSyncChangeApplier(
    private val templateRepo: CardTemplateRepository,
    private val syncRepo: SyncRepository,
) : SyncChangeApplier {

    override suspend fun applyChanges(
        entity: SyncEntityType,
        operations: SyncOperations,
        change: SyncPullChange
    ) {
        when (operations) {
            SyncOperations.Insert -> syncRepo.applyChange(
                deserializer = CardTemplate.serializer(),
                payload = change.payload
            ) {
                templateRepo.insertTemplate(this, userId = "system", needSync = false)
            }

            SyncOperations.Delete -> {
                // Template 删除通常不需要处理
            }
        }
    }
}

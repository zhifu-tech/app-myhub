package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalCardTemplateDataSource
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import tech.zhifu.app.myhub.datastore.repository.CardTemplateRepository
import tech.zhifu.app.myhub.datastore.repository.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.SyncRepository
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import tech.zhifu.app.myhub.sync.SyncPullChange

class CardTemplateRepositoryImpl(
    private val localCardTemplateDataSource: LocalCardTemplateDataSource,
    private val syncRepository: SyncRepository,
) : CardTemplateRepository {

    override val syncChangeApplier: SyncChangeApplier = object : SyncChangeApplier {
        override suspend fun applyChanges(
            entity: SyncEntityType,
            operations: SyncOperations,
            change: SyncPullChange
        ) {
            when (operations) {
                SyncOperations.Insert -> syncRepository.applyChange(
                    deserializer = CardTemplate.serializer(),
                    payload = change.payload
                ) {
                    insertTemplate(this, userId = "system", needSync = false)
                }

                SyncOperations.Delete -> Unit
            }
        }
    }

    override suspend fun insertTemplate(template: CardTemplate, userId: String, needSync: Boolean) {
        localCardTemplateDataSource.insertTemplate(template)
        if (needSync) {
            syncRepository.recordInsertOperation(
                userId = userId,
                entityType = SyncEntityType.Template,
                entityId = template.id,
                payload = template
            )
        }
    }

    override suspend fun getTemplates(): List<CardTemplate> {
        return localCardTemplateDataSource.getTemplates()
    }
}

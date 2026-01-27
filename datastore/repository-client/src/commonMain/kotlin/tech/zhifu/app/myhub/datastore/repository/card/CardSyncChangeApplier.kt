package tech.zhifu.app.myhub.datastore.repository.card

import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.repository.impl.applyChange
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import tech.zhifu.app.myhub.sync.SyncPullChange

internal class CardSyncChangeApplier(
    private val cardRepo: CardRepository,
    private val syncRepo: SyncRepository,
) : SyncChangeApplier {

    override suspend fun applyChanges(
        entity: SyncEntityType,
        operations: SyncOperations,
        change: SyncPullChange
    ) {
        when (operations) {
            SyncOperations.Insert -> syncRepo.applyChange(
                deserializer = Card.serializer(),
                payload = change.payload
            ) {
                cardRepo.insertCard(this, needSync = false)
            }

            SyncOperations.Delete -> {
                cardRepo.clearCard(cardId = change.entityId)
            }
        }
    }
}

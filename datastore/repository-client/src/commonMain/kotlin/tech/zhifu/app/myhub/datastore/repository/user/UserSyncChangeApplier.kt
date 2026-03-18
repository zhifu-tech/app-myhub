package tech.zhifu.app.myhub.datastore.repository.user

import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.sync.applyChange
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import tech.zhifu.app.myhub.sync.SyncPullChange

class UserSyncChangeApplier(
    private val userRepo: UserRepository,
    private val syncRepo: SyncRepository,
) : SyncChangeApplier {

    override suspend fun applyChanges(
        entity: SyncEntityType,
        operations: SyncOperations,
        change: SyncPullChange
    ) {
        when (operations) {
            SyncOperations.Insert -> syncRepo.applyChange(
                deserializer = User.serializer(),
                payload = change.payload
            ) {
                userRepo.insertUser(this, needSync = false)
            }

            SyncOperations.Delete -> {
                // User 通常不删除
            }
        }
    }
}

class UserPreferencesSyncChangeApplier(
    private val userRepo: UserRepository,
    private val syncRepo: SyncRepository,
) : SyncChangeApplier {

    override suspend fun applyChanges(
        entity: SyncEntityType,
        operations: SyncOperations,
        change: SyncPullChange
    ) {
        when (operations) {
            SyncOperations.Insert -> syncRepo.applyChange(
                deserializer = UserPreferences.serializer(),
                payload = change.payload
            ) {
                userRepo.insertUserPreferences(this, needSync = false)
            }

            SyncOperations.Delete -> {
                // UserPreferences 通常不删除
            }
        }
    }
}

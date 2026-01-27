package tech.zhifu.app.myhub.datastore.repository.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.datasource.LocalSyncDataSource
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserStoreData
import tech.zhifu.app.myhub.datastore.repository.user.preferences
import tech.zhifu.app.myhub.sync.SyncCoordinator
import tech.zhifu.app.myhub.sync.SyncMode
import tech.zhifu.app.myhub.sync.SyncRequest
import tech.zhifu.app.myhub.sync.SyncScheduleConfig
import tech.zhifu.app.myhub.sync.SyncScheduler
import tech.zhifu.app.myhub.sync.SyncTrigger
import kotlin.time.Duration.Companion.milliseconds

class SyncRepositoryImpl(
    private val localSyncDataSource: LocalSyncDataSource,
    private val userRepository: Lazy<UserRepository>,
    override val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    },
    private val syncScheduler: SyncScheduler,
    private val syncScope: CoroutineScope,
    private val syncCoordinator: Lazy<SyncCoordinator>,
    private val syncStatus: SyncStatusWrapper,
) : SyncRepository {
    private var autoSyncJob: Job? = null

    override fun observeSyncStatus(userId: String): Flow<SyncStatus> {
        return syncStatus.statusFlow(userId)
    }

    override suspend fun requestSync(userId: String, trigger: SyncTrigger) {
        syncCoordinator.value.requestSync(SyncRequest(userId, trigger))
    }

    override fun startAutoSync(userId: String) {
        autoSyncJob?.cancel()
        autoSyncJob = syncScope.launch {
            userRepository.value.streamUserPreferences(userId)
                .filterIsInstance<StoreReadResponse.Data<UserStoreData>>()
                .mapNotNull { response -> response.value.preferences }
                .map { pref: UserPreferences ->
                    SyncScheduleConfig(
                        interval = pref.syncInterval.milliseconds,
                        mode = if (pref.autoSync) SyncMode.ENABLED else SyncMode.DISABLED
                    )
                }
                .distinctUntilChanged()
                .collect { config ->
                    syncScheduler.start(config) { trigger ->
                        SyncRequest(userId, trigger)
                    }
                }
        }
    }

    override fun stopAutoSync(userId: String) {
        autoSyncJob?.cancel()
        autoSyncJob = null
        syncScheduler.stop()
    }

    override suspend fun insertOutboxAndOpLog(
        outboxId: String,
        oplogId: String,
        userId: String,
        entityType: String,
        entityId: String,
        operation: String,
        payload: String,
        sequence: Long,
        createdAt: String,
        status: String,
        retryCount: Long,
        nextRetryAt: String?,
        lastError: String?
    ) {
        localSyncDataSource.insertOutboxAndOpLog(
            outboxId = outboxId,
            oplogId = oplogId,
            userId = userId,
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            payload = payload,
            sequence = sequence,
            createdAt = createdAt,
            status = status,
            retryCount = retryCount,
            nextRetryAt = nextRetryAt,
            lastError = lastError,
        )
    }
}

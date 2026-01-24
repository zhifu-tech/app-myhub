package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.datasource.LocalSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.SyncOutboxStatus
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import tech.zhifu.app.myhub.datastore.repository.CardTemplateRepository
import tech.zhifu.app.myhub.datastore.repository.CollectionRepository
import tech.zhifu.app.myhub.datastore.repository.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.SyncRepository
import tech.zhifu.app.myhub.datastore.repository.SyncStatus
import tech.zhifu.app.myhub.datastore.repository.TagRepository
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.sync.SyncCoordinator
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncMode
import tech.zhifu.app.myhub.sync.SyncOutboxUploadItem
import tech.zhifu.app.myhub.sync.SyncRequest
import tech.zhifu.app.myhub.sync.SyncScheduleConfig
import tech.zhifu.app.myhub.sync.SyncTrigger
import tech.zhifu.app.myhub.sync.toSyncEntityType
import tech.zhifu.app.myhub.sync.toSyncOperation
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

class SyncRepositoryImpl(
    private val localSyncDataSource: LocalSyncDataSource,
    private val remoteSyncDataSource: RemoteSyncDataSource,
    private val userRepository: Lazy<UserRepository>,
    private val tagRepository: Lazy<TagRepository>,
    private val cardTemplateRepository: Lazy<CardTemplateRepository>,
    private val cardRepository: Lazy<CardRepository>,
    private val collectionRepository: Lazy<CollectionRepository>,
    override val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    },
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : SyncRepository, SyncCoordinator {
    private val scheduler = SyncForegroundScheduler(scope, this)
    private val statusMap = mutableMapOf<String, MutableStateFlow<SyncStatus>>()
    private val pullLimit = 200
    private val pushLimit = 200
    private val outboxRetentionDays = 7
    private val oplogRetentionDays = 30
    private val retryBaseDelayMs = 10_000L
    private val retryMaxDelayMs = 600_000L
    private var autoSyncJob: Job? = null
    private val syncAppliers: Map<SyncEntityType, SyncChangeApplier> by lazy {
        mapOf(
            SyncEntityType.User to userRepository.value.syncUserChangeApplier,
            SyncEntityType.UserPreferences to userRepository.value.syncUserPreferencesChangeApplier,
            SyncEntityType.Card to cardRepository.value.syncChangeApplier,
            SyncEntityType.Tag to tagRepository.value.syncChangeApplier,
            SyncEntityType.Collection to collectionRepository.value.syncCollectionChangeApplier,
            SyncEntityType.Template to cardTemplateRepository.value.syncChangeApplier
        )
    }

    override fun observeSyncStatus(userId: String): Flow<SyncStatus> {
        return statusFlow(userId)
    }

    override suspend fun requestSync(userId: String, trigger: SyncTrigger) {
        requestSync(SyncRequest(userId, trigger))
    }

    override fun startAutoSync(userId: String) {
        autoSyncJob?.cancel()
        autoSyncJob = scope.launch {
            userRepository.value.observeUserPreferences(userId)
                .map { pref ->
                    SyncScheduleConfig(
                        interval = pref.syncInterval.milliseconds,
                        mode = if (pref.autoSync) SyncMode.ENABLED else SyncMode.DISABLED
                    )
                }
                .distinctUntilChanged()
                .collect { config ->
                    scheduler.start(config) { trigger ->
                        SyncRequest(userId, trigger)
                    }
                }
        }
    }

    override fun stopAutoSync(userId: String) {
        autoSyncJob?.cancel()
        autoSyncJob = null
        scheduler.stop()
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

    override suspend fun requestSync(request: SyncRequest) {
        val status = statusFlow(request.userId)
        status.value = SyncStatus.RUNNING
        try {
            pushOutbox(request.userId)
            pullChanges(request.userId)
            status.value = SyncStatus.IDLE
        } catch (_: Exception) {
            status.value = SyncStatus.FAILED
        }
    }

    private suspend fun pushOutbox(userId: String) {
        val now = Clock.System.now()
        val pending = localSyncDataSource.getReadyOutboxByUserId(
            userId = userId,
            now = now.toString()
        )
        if (pending.isEmpty()) return
        pending.chunked(pushLimit).forEach { batch ->
            val items = batch.map { outbox ->
                SyncOutboxUploadItem(
                    id = outbox.id,
                    entityType = outbox.entity_type,
                    entityId = outbox.entity_id,
                    operation = outbox.operation,
                    payload = outbox.payload,
                    sequence = outbox.sequence,
                    createdAt = outbox.created_at
                )
            }
            val response = remoteSyncDataSource.pushOutbox(userId, items)
            response.acceptedIds.forEach { id ->
                localSyncDataSource.updateOutboxStatus(
                    id = id,
                    status = SyncOutboxStatus.SUCCESS,
                    nextRetryAt = null,
                    lastError = null
                )
            }
            response.failed.forEach { (id, error) ->
                val outbox = batch.firstOrNull { it.id == id }
                val nextRetryAt = outbox?.let { computeNextRetryAt(it.retry_count, now) }
                localSyncDataSource.updateOutboxStatus(
                    id = id,
                    status = SyncOutboxStatus.FAILED,
                    nextRetryAt = nextRetryAt,
                    lastError = error
                )
                localSyncDataSource.incrementOutboxRetry(
                    id = id,
                    nextRetryAt = nextRetryAt,
                    lastError = error
                )
            }
        }
        cleanupSuccessfulOutbox(userId)
    }

    private fun computeNextRetryAt(retryCount: Long, now: kotlin.time.Instant): String {
        val attempt = (retryCount + 1).coerceAtMost(6)
        val delayMs = (retryBaseDelayMs * (1L shl (attempt.toInt() - 1)))
            .coerceAtMost(retryMaxDelayMs)
        return now.plus(delayMs.milliseconds).toString()
    }

    private suspend fun pullChanges(userId: String) {
        SyncEntityType.entries.forEach { entityType ->
            val state = localSyncDataSource.getSyncState(userId, entityType.value)
            val response = remoteSyncDataSource.pullChanges(
                userId = userId,
                entityType = entityType.value,
                sinceToken = state?.last_sync_token,
                limit = pullLimit
            )
            response.changes.forEach { change ->
                val entityType = change.toSyncEntityType() ?: return
                val operations = change.toSyncOperation() ?: return
                val applier = syncAppliers[entityType] ?: return@forEach
                // 1. Apply changes first.
                applier.applyChanges(entityType, operations, change)

                // 2. Insert operation log
                val oplogId = "oplog-${change.entityType}-${change.entityId}-${change.updatedAt}"
                localSyncDataSource.insertOpLog(
                    id = oplogId,
                    userId = userId,
                    entityType = change.entityType,
                    entityId = change.entityId,
                    operation = change.operation,
                    payload = change.payload,
                    createdAt = change.updatedAt
                )
            }
            if (response.nextToken != null) {
                val stateId = "sync-$userId-$entityType"
                localSyncDataSource.insertSyncState(
                    id = stateId,
                    userId = userId,
                    entityType = entityType.value,
                    lastSyncAt = Clock.System.now().toString(),
                    lastSyncToken = response.nextToken
                )
            }
        }
        cleanupOpLogs(userId)
    }

    private suspend fun cleanupSuccessfulOutbox(userId: String) {
        val cutoff = Clock.System.now().minus(outboxRetentionDays.days)
        localSyncDataSource.deleteOutboxByStatusBefore(
            userId = userId,
            status = SyncOutboxStatus.SUCCESS,
            beforeAt = cutoff.toString()
        )
    }

    private suspend fun cleanupOpLogs(userId: String) {
        val cutoff = Clock.System.now().minus(oplogRetentionDays.days)
        localSyncDataSource.deleteOpLogsBefore(
            userId = userId,
            beforeAt = cutoff.toString()
        )
    }

    private fun statusFlow(userId: String): MutableStateFlow<SyncStatus> {
        return statusMap.getOrPut(userId) { MutableStateFlow(SyncStatus.IDLE) }
    }
}

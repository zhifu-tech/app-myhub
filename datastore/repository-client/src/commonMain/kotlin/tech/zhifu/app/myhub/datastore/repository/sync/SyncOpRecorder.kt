package tech.zhifu.app.myhub.datastore.repository.sync

import kotlinx.serialization.DeserializationStrategy
import tech.zhifu.app.myhub.datastore.datasource.sync.SyncOutboxStatus
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import kotlin.time.Clock
import kotlin.time.Instant

internal suspend fun <T> SyncRepository.applyChange(
    deserializer: DeserializationStrategy<T>,
    payload: String,
    block: suspend T.() -> Unit
) = runCatching<T> {
    json.decodeFromString(deserializer, payload)
}.getOrNull()?.apply {
    block(this)
}

internal suspend inline fun <reified T> SyncRepository.recordInsertOperation(
    userId: String,
    entityType: SyncEntityType,
    entityId: String,
    payload: T,
    now: Instant = Clock.System.now(),
) = recordOperation(
    userId = userId,
    entityType = entityType,
    entityId = entityId,
    operation = SyncOperations.Insert,
    payload = json.encodeToString(payload),
    now = now
)

internal suspend inline fun <reified T> SyncRepository.recordDeleteOperation(
    userId: String,
    entityType: SyncEntityType,
    entityId: String,
    payload: T,
    now: Instant = Clock.System.now(),
) = recordOperation(
    userId = userId,
    entityType = entityType,
    entityId = entityId,
    operation = SyncOperations.Delete,
    payload = json.encodeToString(payload),
    now = now
)

private suspend fun SyncRepository.recordOperation(
    userId: String,
    entityType: SyncEntityType,
    entityId: String,
    operation: SyncOperations,
    payload: String,
    now: Instant = Clock.System.now(),
) {
    val timestamp = now.toEpochMilliseconds()
    val outboxId = "outbox-$userId-$entityId-$timestamp"
    val oplogId = "oplog-$userId-$entityId-$timestamp"
    insertOutboxAndOpLog(
        outboxId = outboxId,
        oplogId = oplogId,
        userId = userId,
        entityType = entityType.value,
        entityId = entityId,
        operation = operation.value,
        payload = payload,
        sequence = timestamp,
        createdAt = now.toString(),
        status = SyncOutboxStatus.PENDING,
        retryCount = 0,
        nextRetryAt = null,
        lastError = null
    )
}

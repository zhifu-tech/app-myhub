package tech.zhifu.app.myhub.datastore.repository.tag

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.impl.extensions.get
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.repository.sync.recordDeleteOperation
import tech.zhifu.app.myhub.datastore.repository.sync.recordInsertOperation
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.sync.SyncEntityType
import kotlin.random.Random
import kotlin.time.Clock

class TagRepositoryImpl(
    private val store: TagStore,
    private val syncRepository: SyncRepository,
    private val logger: Logger = logger("TagRepo")
) : TagRepository {

    override suspend fun insertTag(tag: Tag, needSync: Boolean) {
        store.write(
            StoreWriteRequest.of(
                key = TagStoreKey.ById(tag.id),
                value = TagStoreData.Single(tag)
            )
        )

        if (needSync) {
            syncRepository.recordInsertOperation(
                userId = tag.userId,
                entityType = SyncEntityType.Tag,
                entityId = tag.id,
                payload = tag,
            )
        }
    }

    override suspend fun getTag(tagId: String): TagStoreData? =
        runCatching {
            store.get<TagStoreKey, TagStoreData, StoreWriteResponse>(
                key = TagStoreKey.ById(tagId)
            )
        }.onFailure {
            logger.error(it) { "get tag for {tag:$tagId} from store failed" }
        }.getOrNull()

    override suspend fun getTags(userId: String): TagStoreData =
        store.get<TagStoreKey, TagStoreData, StoreWriteResponse>(
            key = TagStoreKey.ByUser(userId)
        )

    override fun streamTag(tagId: String, refresh: Boolean): Flow<StoreReadResponse<TagStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.cached(
                key = TagStoreKey.ById(tagId),
                refresh = refresh
            )
        )

    override fun streamTags(userId: String, refresh: Boolean): Flow<StoreReadResponse<TagStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.cached(
                key = TagStoreKey.ByUser(userId),
                refresh = refresh
            )
        )

    override suspend fun fetchTags(userId: String): Flow<StoreReadResponse<TagStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.fresh(
                key = TagStoreKey.ByUser(userId)
            )
        )

    override suspend fun ensureTags(userId: String, tags: List<Tag>, needSync: Boolean): List<Tag> {
        logger.debug { "ensure tags: $tags" }
        if (tags.isEmpty()) return emptyList()

        val existingTags = getTags(userId).tags
        val byId = existingTags.associateBy { it.id }
        val byName = existingTags.associateBy { it.name }
        logger.debug { "existing tags: $existingTags" }
        logger.debug { "byId: $byId" }
        logger.debug { "byName: $byName" }
        return tags.map { tag ->
            val existing = tag.id.takeIf { it.isNotBlank() }?.let(byId::get) ?: byName[tag.name]
            if (existing != null) {
                existing
            } else {
                val now = Clock.System.now()
                val newTag = tag.copy(
                    id = generateTagId(now),
                    userId = userId,
                    createdAt = now,
                    updatedAt = now
                )
                logger.debug { "new tag: $newTag" }
                insertTag(newTag, needSync = needSync)
                logger.debug { "inserted new tag: $newTag" }
                newTag
            }
        }
    }

    override suspend fun clearTag(tagId: String) {
        val tag = getTag(tagId)?.tag ?: return
        store.clear(key = TagStoreKey.ById(tagId))

        syncRepository.recordDeleteOperation(
            userId = tag.userId,
            entityType = SyncEntityType.Tag,
            entityId = tagId,
            payload = tag,
        )
    }

    private fun generateTagId(now: kotlin.time.Instant): String {
        val rand = Random.nextInt(0, 1_000_000)
        return "tag-${now.toEpochMilliseconds()}-$rand"
    }
}

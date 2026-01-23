package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import tech.zhifu.app.myhub.cache.Cache
import tech.zhifu.app.myhub.cache.CacheConfig
import tech.zhifu.app.myhub.cache.cache
import tech.zhifu.app.myhub.datastore.datasource.LocalSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.repository.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.TagRepository
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import tech.zhifu.app.myhub.sync.SyncPullChange
import kotlin.random.Random
import kotlin.time.Clock

/**
 * 标签仓库实现（客户端）
 * 实现本地和远程数据源的协调，支持响应式接口
 */
class TagRepositoryImpl(
    private val localTagDataSource: LocalTagDataSource,
    private val localSyncDataSource: LocalSyncDataSource,
    private val tagCache: Cache<String, Tag> = cache(CacheConfig(maximumSize = 99))
) : TagRepository {

    override val syncChangeApplier: SyncChangeApplier = object : SyncChangeApplier {

        override suspend fun applyChanges(
            entity: SyncEntityType,
            operations: SyncOperations,
            change: SyncPullChange
        ) {
            when (operations) {
                SyncOperations.Insert -> localSyncDataSource.applyChange(
                    deserializer = Tag.serializer(),
                    payload = change.payload
                ) {
                    insertTag(this, false)
                }

                SyncOperations.Delete -> localSyncDataSource.applyChange(
                    deserializer = Tag.serializer(),
                    payload = change.payload
                ) {
                    deleteTag(this.id, false)
                }

                else -> {}
            }
        }
    }

    override suspend fun insertTag(tag: Tag, needSync: Boolean) {
        localTagDataSource.insertTag(tag)
        if (needSync) {
            localSyncDataSource.recordInsertOperation(
                userId = tag.userId,
                entityType = SyncEntityType.Tag,
                entityId = tag.id,
                payload = tag,
            )
        }
    }

    override suspend fun getTag(tagId: String): Tag? {
        val cached = tagCache.get(tagId)
        if (cached != null) {
            return cached
        }
        val localTag = localTagDataSource.getTag(tagId)
        if (localTag != null) {
            tagCache.put(tagId, localTag)
            return localTag
        }
        return null
    }

    override fun observeTag(tagId: String): Flow<Tag> {
        return localTagDataSource.observeTag(tagId).onEach {
            tagCache.put(tagId, it)
        }
    }

    override suspend fun getTags(userId: String): List<Tag> {
        return localTagDataSource.getTags(userId).onEach {
            tagCache.put(it.id, it)
        }
    }

    override suspend fun ensureTags(userId: String, tags: List<Tag>, needSync: Boolean): List<Tag> {
        if (tags.isEmpty()) return emptyList()
        val existingTags = getTags(userId)
        val byId = existingTags.associateBy { it.id }
        val byName = existingTags.associateBy { it.name }
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
                insertTag(newTag, needSync = needSync)
                newTag
            }
        }
    }

    override fun observeTags(userId: String): Flow<List<Tag>> {
        return localTagDataSource.observeTags(userId).onEach { tags ->
            tags.forEach {
                tagCache.put(it.id, it)
            }
        }
    }

    override suspend fun deleteTag(id: String, needSync: Boolean) {
        val tag = localTagDataSource.getTag(id)
        localTagDataSource.deleteTag(id)
        if (needSync) {
            localSyncDataSource.recordDeleteOperation(
                userId = id,
                entityType = SyncEntityType.Tag,
                entityId = id,
                payload = tag
            )
        }
    }

    private fun generateTagId(now: kotlin.time.Instant): String {
        val rand = Random.nextInt(0, 1_000_000)
        return "tag-${now.toEpochMilliseconds()}-$rand"
    }
}

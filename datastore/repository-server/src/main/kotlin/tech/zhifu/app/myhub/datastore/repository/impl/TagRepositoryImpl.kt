package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.repository.TagRepository
import kotlin.random.Random
import kotlin.time.Clock

/**
 * 标签仓库实现（服务端）
 * 使用 LocalTagDataSource 实现，避免代码重复
 */
class TagRepositoryImpl(
    private val localDataSource: LocalTagDataSource
) : TagRepository {

    override suspend fun getTag(tagId: String): Tag? {
        return localDataSource.getTag(tagId)
    }

    override suspend fun getTags(userId: String): List<Tag> {
        return localDataSource.getTags(userId)
    }
    
    override suspend fun getTagByName(name: String, userId: String): Tag? {
        return localDataSource.getTagByName(name, userId)
    }

    override suspend fun upsertTag(tag: Tag): Tag {
        // 检查是否存在（通过 ID 或名称）
        val existing = tag.id.takeIf { it.isNotBlank() }
            ?.let { localDataSource.getTag(it) }
            ?: localDataSource.getTagByName(tag.name, tag.userId)
        
        if (existing != null) {
            // 如果存在，更新（先删除再插入，简单的更新策略）
            localDataSource.deleteTag(existing.id)
        }
        
        // 插入标签
        localDataSource.insertTag(tag)
        
        // 返回标签（从数据库重新获取以确保数据一致性）
        return localDataSource.getTag(tag.id) ?: tag
    }

    override suspend fun deleteTag(tagId: String) {
        localDataSource.deleteTag(tagId)
    }

    override suspend fun ensureTags(userId: String, tags: List<Tag>): List<Tag> {
        if (tags.isEmpty()) return emptyList()

        // 获取用户所有现有标签
        val existingTags = localDataSource.getTags(userId)
        val byId = existingTags.associateBy { it.id }
        val byName = existingTags.associateBy { it.name }

        // 确保每个标签都存在
        return tags.map { tag ->
            // 先通过 ID 查找，如果 ID 为空或不存在，则通过名称查找
            val existing = tag.id.takeIf { it.isNotBlank() }?.let(byId::get)
                ?: byName[tag.name]
            
            if (existing != null) {
                // 标签已存在，返回现有标签
                existing
            } else {
                // 标签不存在，创建新标签
                val now = Clock.System.now()
                val newTag = tag.copy(
                    id = generateTagId(now),
                    userId = userId,
                    createdAt = now,
                    updatedAt = now
                )
                localDataSource.insertTag(newTag)
                newTag
            }
        }
    }

    override suspend fun getTagsByIds(tagIds: List<String>): List<Tag> {
        if (tagIds.isEmpty()) return emptyList()

        return tagIds.mapNotNull { tagId ->
            localDataSource.getTag(tagId)
        }
    }
    
    /**
     * 生成标签 ID
     */
    private fun generateTagId(now: kotlin.time.Instant): String {
        val rand = Random.nextInt(0, 1_000_000)
        return "tag-${now.toEpochMilliseconds()}-$rand"
    }
}

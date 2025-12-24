package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.datastore.model.TagStats
import tech.zhifu.app.myhub.datastore.repository.TagRepository
import kotlin.time.Clock

/**
 * 标签仓库实现
 */
class TagRepositoryImpl(
    private val localDataSource: LocalTagDataSource
) : TagRepository {

    override fun getAllTags(): Flow<List<Tag>> {
        return localDataSource.observeTags()
    }

    override suspend fun getTagById(id: String): Tag? {
        return localDataSource.getTagById(id)
    }

    override suspend fun getTagByName(name: String): Tag? {
        return localDataSource.getTagByName(name)
    }

    override suspend fun createTag(tag: Tag): Result<Tag> {
        return try {
            // 检查是否已存在同名标签
            val existing = localDataSource.getTagByName(tag.name)
            if (existing != null) {
                return Result.failure(IllegalStateException("Tag with name '${tag.name}' already exists"))
            }

            localDataSource.insertTag(tag)
            Result.success(tag)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTag(tag: Tag): Result<Tag> {
        return try {
            localDataSource.updateTag(tag)
            Result.success(tag)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTag(id: String): Result<Unit> {
        return try {
            localDataSource.deleteTag(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTagStats(): Flow<List<TagStats>> {
        // TODO: 实现标签统计（需要从卡片数据中计算）
        return localDataSource.observeTags().map { tags ->
            tags.map { tag ->
                TagStats(
                    tagId = tag.id,
                    tagName = tag.name,
                    totalCards = tag.cardCount,
                    favoriteCards = 0, // TODO: 需要从卡片数据计算
                    recentCards = 0 // TODO: 需要从卡片数据计算
                )
            }
        }
    }

    override fun getPopularTags(limit: Int): Flow<List<Tag>> {
        return localDataSource.observeTags().map { tags ->
            tags.sortedByDescending { it.cardCount }.take(limit)
        }
    }
}


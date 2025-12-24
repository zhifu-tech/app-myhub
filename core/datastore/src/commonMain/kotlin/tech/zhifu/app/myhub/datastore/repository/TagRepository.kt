package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.datastore.model.TagStats

/**
 * 标签仓库接口
 */
interface TagRepository {
    /**
     * 获取所有标签
     */
    fun getAllTags(): Flow<List<Tag>>

    /**
     * 根据ID获取标签
     */
    suspend fun getTagById(id: String): Tag?

    /**
     * 根据名称获取标签
     */
    suspend fun getTagByName(name: String): Tag?

    /**
     * 创建新标签
     */
    suspend fun createTag(tag: Tag): Result<Tag>

    /**
     * 更新标签
     */
    suspend fun updateTag(tag: Tag): Result<Tag>

    /**
     * 删除标签
     */
    suspend fun deleteTag(id: String): Result<Unit>

    /**
     * 获取标签统计信息
     */
    fun getTagStats(): Flow<List<TagStats>>

    /**
     * 获取热门标签（按使用次数排序）
     */
    fun getPopularTags(limit: Int = 10): Flow<List<Tag>>
}


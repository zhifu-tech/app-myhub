package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.model.domain.Tag

/**
 * 标签仓库接口（服务端）
 * 不包含响应式流（Flow），只提供同步方法
 */
interface TagRepository {
    /**
     * 获取指定标签
     */
    suspend fun getTag(tagId: String): Tag?

    /**
     * 获取用户所有标签
     */
    suspend fun getTags(userId: String): List<Tag>
    
    /**
     * 根据名称获取标签
     */
    suspend fun getTagByName(name: String, userId: String): Tag?

    /**
     * 创建或更新标签
     */
    suspend fun upsertTag(tag: Tag): Tag

    /**
     * 删除标签
     */
    suspend fun deleteTag(tagId: String)

    /**
     * 确保标签存在（如果不存在则创建）
     * 这是关键业务逻辑，用于自动创建标签
     * 
     * @param userId 用户 ID
     * @param tags 需要确保存在的标签列表
     * @return 已存在的标签列表（包括新创建的）
     */
    suspend fun ensureTags(userId: String, tags: List<Tag>): List<Tag>

    /**
     * 根据标签 ID 列表批量获取标签
     */
    suspend fun getTagsByIds(tagIds: List<String>): List<Tag>
}

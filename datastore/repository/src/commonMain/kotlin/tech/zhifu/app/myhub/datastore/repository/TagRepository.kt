package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.Tag

/**
 * 标签仓库接口（基础接口，同步风格）
 */
interface TagRepository {
    suspend fun getAllTags(): List<Tag>
    suspend fun getTagById(id: String): Tag?
    suspend fun getTagByName(name: String): Tag?
    suspend fun createTag(tag: Tag): Tag
    suspend fun updateTag(tag: Tag): Tag
    suspend fun deleteTag(id: String): Boolean
}

/**
 * 响应式标签仓库接口（扩展接口，客户端使用）
 */
interface ReactiveTagRepository : TagRepository {
    fun observeAllTags(): Flow<List<Tag>>
}


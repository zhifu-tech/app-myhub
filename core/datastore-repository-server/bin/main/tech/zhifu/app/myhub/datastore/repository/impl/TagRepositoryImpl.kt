package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.datastore.repository.TagRepository

/**
 * 标签仓库实现（服务端）
 * 使用 LocalTagDataSource 实现，避免代码重复
 */
class TagRepositoryImpl(
    private val localDataSource: LocalTagDataSource
) : TagRepository {

    override suspend fun getAllTags(): List<Tag> {
        return localDataSource.getAllTags()
    }

    override suspend fun getTagById(id: String): Tag? {
        return localDataSource.getTagById(id)
    }

    override suspend fun getTagByName(name: String): Tag? {
        return localDataSource.getTagByName(name)
    }

    override suspend fun createTag(tag: Tag): Tag {
        // 检查是否已存在同名标签
        val existingTag = localDataSource.getTagByName(tag.name)
        if (existingTag != null) {
            throw IllegalArgumentException("Tag with name '${tag.name}' already exists")
        }

        try {
            localDataSource.insertTag(tag)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to create tag: ${e.message}", e)
        }
        return tag
    }

    override suspend fun updateTag(tag: Tag): Tag {
        localDataSource.updateTag(tag)
        return tag
    }

    override suspend fun deleteTag(id: String): Boolean {
        return try {
            localDataSource.deleteTag(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}


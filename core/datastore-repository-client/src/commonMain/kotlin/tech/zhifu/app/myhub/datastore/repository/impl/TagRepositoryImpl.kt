package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteTagDataSource
import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.datastore.repository.ReactiveTagRepository

/**
 * 标签仓库实现（客户端）
 * 实现本地和远程数据源的协调，支持响应式接口
 */
class TagRepositoryImpl(
    private val localDataSource: LocalTagDataSource,
    private val remoteDataSource: RemoteTagDataSource
) : ReactiveTagRepository {

    override suspend fun getAllTags(): List<Tag> {
        return localDataSource.getAllTags()
    }

    override fun observeAllTags(): Flow<List<Tag>> {
        return localDataSource.observeTags()
    }

    override suspend fun getTagById(id: String): Tag? {
        // 先从本地获取
        val localTag = localDataSource.getTagById(id)
        if (localTag != null) {
            return localTag
        }

        // 如果本地没有，从远程获取
        return try {
            val remoteTag = remoteDataSource.getTagById(id)
            remoteTag?.let { localDataSource.insertTag(it) }
            remoteTag
        } catch (_: Exception) {
            null
        }
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

        // 先保存到本地
        try {
        localDataSource.insertTag(tag)
        } catch (e: Exception) {
            // 如果插入失败（例如约束违反），抛出异常
            throw IllegalArgumentException("Failed to create tag: ${e.message}", e)
        }

        // 然后同步到远程
        return try {
            val remoteTag = remoteDataSource.createTag(tag)
            localDataSource.updateTag(remoteTag)
            remoteTag
        } catch (_: Exception) {
            // 如果远程同步失败，返回本地标签
            tag
        }
    }

    override suspend fun updateTag(tag: Tag): Tag {
        // 先更新本地
        localDataSource.updateTag(tag)

        // 然后同步到远程
        return try {
            val remoteTag = remoteDataSource.updateTag(tag)
            localDataSource.updateTag(remoteTag)
            remoteTag
        } catch (_: Exception) {
            // 如果远程同步失败，返回本地标签
            tag
        }
    }

    override suspend fun deleteTag(id: String): Boolean {
        return try {
            // 先删除本地
            localDataSource.deleteTag(id)

            // 然后删除远程
            try {
                remoteDataSource.deleteTag(id)
            } catch (_: Exception) {
                // 如果远程删除失败，仍然返回成功（本地已删除）
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}


package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.UserContextProvider
import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.datastore.repository.ReactiveTagRepository

/**
 * 标签仓库实现（客户端）
 * 实现本地和远程数据源的协调，支持响应式接口
 */
class TagRepositoryImpl(
    private val localDataSource: LocalTagDataSource,
    private val remoteDataSource: RemoteTagDataSource,
    private val userContextProvider: UserContextProvider,
    private val userDataSource: LocalUserDataSource
) : ReactiveTagRepository {

    private suspend fun requireUserId(): String {
        return userContextProvider.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
    }

    override suspend fun getAllTags(): List<Tag> {
        val userId = requireUserId()
        return localDataSource.getAllTags(userId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAllTags(): Flow<List<Tag>> {
        return userDataSource.observeUser().flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                localDataSource.observeTags(user.id)
            }
        }
    }

    override suspend fun getTagById(id: String): Tag? {
        val userId = requireUserId()
        // 先从本地获取
        val localTag = localDataSource.getTagById(id, userId)
        if (localTag != null) {
            return localTag
        }

        // 如果本地没有，从远程获取
        return try {
            val remoteTag = remoteDataSource.getTagById(id)
            remoteTag?.let { localDataSource.insertTag(it, userId) }
            remoteTag
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getTagByName(name: String): Tag? {
        val userId = requireUserId()
        return localDataSource.getTagByName(name, userId)
    }

    override suspend fun createTag(tag: Tag): Tag {
        val userId = requireUserId()
        // 检查是否已存在同名标签
        val existingTag = localDataSource.getTagByName(tag.name, userId)
        if (existingTag != null) {
            throw IllegalArgumentException("Tag with name '${tag.name}' already exists")
        }

        // 先保存到本地
        try {
            localDataSource.insertTag(tag, userId)
        } catch (e: Exception) {
            // 如果插入失败（例如约束违反），抛出异常
            throw IllegalArgumentException("Failed to create tag: ${e.message}", e)
        }

        // 然后同步到远程
        return try {
            val remoteTag = remoteDataSource.createTag(tag)
            localDataSource.updateTag(remoteTag, userId)
            remoteTag
        } catch (_: Exception) {
            // 如果远程同步失败，返回本地标签
            tag
        }
    }

    override suspend fun updateTag(tag: Tag): Tag {
        val userId = requireUserId()
        // 先更新本地
        localDataSource.updateTag(tag, userId)

        // 然后同步到远程
        return try {
            val remoteTag = remoteDataSource.updateTag(tag)
            localDataSource.updateTag(remoteTag, userId)
            remoteTag
        } catch (_: Exception) {
            // 如果远程同步失败，返回本地标签
            tag
        }
    }

    override suspend fun deleteTag(id: String): Boolean {
        return try {
            val userId = requireUserId()
            // 先删除本地
            localDataSource.deleteTag(id, userId)

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


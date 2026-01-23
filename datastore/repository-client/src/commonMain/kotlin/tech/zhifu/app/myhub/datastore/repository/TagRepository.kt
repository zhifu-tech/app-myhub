package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.Tag

interface TagRepository {

    val syncChangeApplier: SyncChangeApplier

    suspend fun insertTag(tag: Tag, needSync: Boolean = true)

    suspend fun getTag(tagId: String): Tag?
    fun observeTag(tagId: String): Flow<Tag>

    suspend fun getTags(userId: String): List<Tag>
    fun observeTags(userId: String): Flow<List<Tag>>

    suspend fun ensureTags(userId: String, tags: List<Tag>, needSync: Boolean = true): List<Tag>

    suspend fun deleteTag(id: String, needSync: Boolean)
}

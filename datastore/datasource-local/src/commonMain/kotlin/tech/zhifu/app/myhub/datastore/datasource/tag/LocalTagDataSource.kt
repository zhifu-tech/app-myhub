package tech.zhifu.app.myhub.datastore.datasource.tag

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.Tag

interface LocalTagDataSource {
    suspend fun getTag(tagId: String): Tag?
    fun observeTag(tagId: String): Flow<Tag>

    suspend fun getTags(userId: String): List<Tag>
    fun observeTags(userId: String): Flow<List<Tag>>

    suspend fun getTagByName(name: String, userId: String): Tag?

    suspend fun insertTag(tag: Tag)
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(id: String)
}

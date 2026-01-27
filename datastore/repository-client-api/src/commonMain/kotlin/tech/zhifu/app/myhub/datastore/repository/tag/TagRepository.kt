package tech.zhifu.app.myhub.datastore.repository.tag

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.model.domain.Tag

interface TagRepository {

    suspend fun insertTag(tag: Tag, needSync: Boolean = true)

    suspend fun getTag(tagId: String): TagStoreData?

    suspend fun getTags(userId: String): TagStoreData?

    fun streamTag(tagId: String, refresh: Boolean = false): Flow<StoreReadResponse<TagStoreData>>

    fun streamTags(userId: String, refresh: Boolean = false): Flow<StoreReadResponse<TagStoreData>>

    suspend fun fetchTags(userId: String): Flow<StoreReadResponse<TagStoreData>>

    suspend fun ensureTags(userId: String, tags: List<Tag>, needSync: Boolean = true): List<Tag>

    suspend fun clearTag(tagId: String)
}

package tech.zhifu.app.myhub.datastore.datasource.tag

import tech.zhifu.app.myhub.datastore.model.domain.Tag

interface RemoteTagDataSource {
    suspend fun getTags(userId: String): List<Tag>
    suspend fun getTagById(id: String, userId: String): Tag?
    suspend fun createTag(tag: Tag, userId: String): Tag
    suspend fun updateTag(id: String, tag: Tag, userId: String): Tag
    suspend fun deleteTag(id: String, userId: String)
}

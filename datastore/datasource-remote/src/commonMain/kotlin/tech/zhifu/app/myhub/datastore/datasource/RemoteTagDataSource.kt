package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.domain.Tag

interface RemoteTagDataSource {
    suspend fun getAllTags(): List<Tag>
    suspend fun getTagById(id: String): Tag?
    suspend fun createTag(tag: Tag): Tag
    suspend fun updateTag(tag: Tag): Tag
    suspend fun deleteTag(id: String)
}

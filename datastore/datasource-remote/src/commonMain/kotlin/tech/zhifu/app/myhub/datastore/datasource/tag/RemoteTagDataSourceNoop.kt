package tech.zhifu.app.myhub.datastore.datasource.tag

import tech.zhifu.app.myhub.datastore.model.domain.Tag

class RemoteTagDataSourceNoop : RemoteTagDataSource {
    override suspend fun getTags(userId: String): List<Tag> = emptyList()

    override suspend fun getTagById(id: String, userId: String): Tag? = null

    override suspend fun createTag(tag: Tag, userId: String): Tag = tag

    override suspend fun updateTag(id: String, tag: Tag, userId: String): Tag = tag

    override suspend fun deleteTag(id: String, userId: String) = Unit
}

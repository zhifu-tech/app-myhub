package tech.zhifu.app.myhub.datastore.datasource.impl

import tech.zhifu.app.myhub.datastore.datasource.RemoteTagDataSource
import tech.zhifu.app.myhub.datastore.model.Tag

/**
 * 远程标签数据源占位实现（仅用于测试）
 */
class RemoteTagDataSourceStub : RemoteTagDataSource {
    override suspend fun getAllTags(): List<Tag> {
        return emptyList()
    }

    override suspend fun getTagById(id: String): Tag? {
        return null
    }

    override suspend fun createTag(tag: Tag): Tag {
        return tag
    }

    override suspend fun updateTag(tag: Tag): Tag {
        return tag
    }

    override suspend fun deleteTag(id: String) {
        // Stub implementation
    }
}


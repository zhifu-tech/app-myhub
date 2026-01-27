//package tech.zhifu.app.myhub.datastore.repository.impl
//
//import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
//import tech.zhifu.app.myhub.datastore.datasource.UserContextProvider
//import tech.zhifu.app.myhub.datastore.model.Tag
//import tech.zhifu.app.myhub.datastore.repository.tag.TagRepository
//
///**
// * 标签仓库实现（服务端）
// * 使用 LocalTagDataSource 实现，避免代码重复
// */
//class TagRepositoryImpl(
//    private val localDataSource: LocalTagDataSource,
//    private val userContextProvider: UserContextProvider
//) : TagRepository {
//
//    private suspend fun requireUserId(): String {
//        return userContextProvider.getCurrentUserId()
//            ?: throw IllegalStateException("User not authenticated")
//    }
//
//    override suspend fun getAllTags(): List<Tag> {
//        val userId = requireUserId()
//        return localDataSource.getAllTags(userId)
//    }
//
//    override suspend fun getTagById(id: String): Tag? {
//        val userId = requireUserId()
//        return localDataSource.getTagById(id, userId)
//    }
//
//    override suspend fun getTagByName(name: String): Tag? {
//        val userId = requireUserId()
//        return localDataSource.getTagByName(name, userId)
//    }
//
//    override suspend fun createTag(tag: Tag): Tag {
//        val userId = requireUserId()
//        // 检查是否已存在同名标签
//        val existingTag = localDataSource.getTagByName(tag.name, userId)
//        if (existingTag != null) {
//            throw IllegalArgumentException("Tag with name '${tag.name}' already exists")
//        }
//
//        try {
//            localDataSource.insertTag(tag, userId)
//        } catch (e: Exception) {
//            throw IllegalArgumentException("Failed to create tag: ${e.message}", e)
//        }
//        return tag
//    }
//
//    override suspend fun updateTag(tag: Tag): Tag {
//        val userId = requireUserId()
//        localDataSource.updateTag(tag, userId)
//        return tag
//    }
//
//    override suspend fun deleteTag(id: String): Boolean {
//        return try {
//            val userId = requireUserId()
//            localDataSource.deleteTag(id, userId)
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }
//}
//

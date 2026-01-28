package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.domain.Collection

interface RemoteCollectionDataSource {
    /**
     * 获取用户的集合列表（支持分页）
     * @param userId 用户 ID
     * @param page 页码（从 1 开始），如果为 null 则返回所有集合
     * @param pageSize 每页数量，仅在 page 不为 null 时有效
     * @return 集合列表（如果指定了分页，返回的集合包含 cardCount 和 previewCards）
     */
    suspend fun getCollections(
        userId: String,
        page: Int? = null,
        pageSize: Int? = null
    ): List<Collection>
    suspend fun getCollectionById(id: String, userId: String): Collection?
    suspend fun createCollection(collection: Collection, userId: String): Collection
    suspend fun updateCollection(id: String, collection: Collection, userId: String): Collection
    suspend fun deleteCollection(id: String, userId: String)
}

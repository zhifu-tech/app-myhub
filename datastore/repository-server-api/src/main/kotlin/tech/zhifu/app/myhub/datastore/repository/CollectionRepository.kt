package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.model.domain.Collection

/**
 * 卡集仓库接口（服务端）
 */
interface CollectionRepository {
    /**
     * 获取指定用户的卡集列表
     */
    suspend fun getCollections(userId: String): List<Collection>

    /**
     * 根据 ID 获取卡集
     */
    suspend fun getCollectionById(collectionId: String): Collection?

    /**
     * 创建卡集
     */
    suspend fun createCollection(collection: Collection): Collection

    /**
     * 更新卡集
     */
    suspend fun updateCollection(collection: Collection): Collection

    /**
     * 删除卡集
     */
    suspend fun deleteCollection(collectionId: String)
}

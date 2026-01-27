package tech.zhifu.app.myhub.service

import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.dto.CollectionResponse
import tech.zhifu.app.myhub.datastore.model.dto.CreateCollectionRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateCollectionRequest
import tech.zhifu.app.myhub.datastore.model.dto.toResponse
import tech.zhifu.app.myhub.datastore.repository.CollectionRepository
import tech.zhifu.app.myhub.exception.ForbiddenException
import tech.zhifu.app.myhub.exception.NotFoundException
import kotlin.time.Clock

/**
 * 卡集服务
 * 处理卡集相关的业务逻辑
 */
class CollectionService(
    private val collectionRepository: CollectionRepository
) {
    /**
     * 获取用户的所有卡集
     */
    suspend fun getCollections(userId: String): List<CollectionResponse> {
        val collections = collectionRepository.getCollections(userId)
        return collections.map { it.toResponse() }
    }

    /**
     * 获取指定卡集
     */
    suspend fun getCollection(id: String, userId: String): CollectionResponse {
        val collection = collectionRepository.getCollectionById(id)
            ?: throw NotFoundException("Collection", id)

        // 验证用户权限
        if (collection.userId != userId) {
            throw ForbiddenException("Not authorized to access this collection")
        }

        return collection.toResponse()
    }

    /**
     * 创建卡集
     */
    suspend fun createCollection(request: CreateCollectionRequest, userId: String): CollectionResponse {
        // 验证请求
        request.validate()

        // 创建卡集
        val collection = Collection(
            id = generateCollectionId(),
            name = request.name.trim(),
            topic = request.topic?.trim(),
            description = request.description?.trim(),
            userId = userId,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )

        val created = collectionRepository.createCollection(collection)
        return created.toResponse()
    }

    /**
     * 更新卡集
     */
    suspend fun updateCollection(
        id: String,
        request: UpdateCollectionRequest,
        userId: String
    ): CollectionResponse {
        // 验证请求
        request.validate()

        // 检查卡集是否存在
        val existing = collectionRepository.getCollectionById(id)
            ?: throw NotFoundException("Collection", id)

        // 验证用户权限
        if (existing.userId != userId) {
            throw ForbiddenException("Not authorized to update this collection")
        }

        // 更新卡集
        val updated = existing.copy(
            name = request.name.trim(),
            topic = request.topic?.trim(),
            description = request.description?.trim(),
            updatedAt = Clock.System.now()
        )

        val saved = collectionRepository.updateCollection(updated)
        return saved.toResponse()
    }

    /**
     * 删除卡集
     */
    suspend fun deleteCollection(id: String, userId: String) {
        // 检查卡集是否存在
        val existing = collectionRepository.getCollectionById(id)
            ?: throw NotFoundException("Collection", id)

        // 验证用户权限
        if (existing.userId != userId) {
            throw ForbiddenException("Not authorized to delete this collection")
        }

        collectionRepository.deleteCollection(id)
    }

    /**
     * 生成卡集 ID
     */
    private fun generateCollectionId(): String {
        return "collection-${System.currentTimeMillis()}-${(0..9999).random()}"
    }
}

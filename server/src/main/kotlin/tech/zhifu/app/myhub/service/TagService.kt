package tech.zhifu.app.myhub.service

import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.model.dto.CreateTagRequest
import tech.zhifu.app.myhub.datastore.model.dto.TagResponse
import tech.zhifu.app.myhub.datastore.model.dto.UpdateTagRequest
import tech.zhifu.app.myhub.datastore.model.dto.toResponse
import tech.zhifu.app.myhub.datastore.repository.TagRepository
import tech.zhifu.app.myhub.exception.ForbiddenException
import tech.zhifu.app.myhub.exception.NotFoundException
import kotlin.time.Clock

/**
 * 标签服务
 * 处理标签相关的业务逻辑
 */
class TagService(
    private val tagRepository: TagRepository
) {
    /**
     * 获取用户的所有标签
     */
    suspend fun getTags(userId: String): List<TagResponse> {
        val tags = tagRepository.getTags(userId)
        return tags.map { it.toResponse() }
    }

    /**
     * 获取指定标签
     */
    suspend fun getTag(id: String, userId: String): TagResponse {
        val tag = tagRepository.getTag(id)
            ?: throw NotFoundException("Tag", id)

        // 验证用户权限
        if (tag.userId != userId) {
            throw ForbiddenException("Not authorized to access this tag")
        }

        return tag.toResponse()
    }

    /**
     * 创建标签
     */
    suspend fun createTag(request: CreateTagRequest, userId: String): TagResponse {
        // 验证请求
        request.validate()

        // 检查是否已存在同名标签
        val existing = tagRepository.getTagByName(request.name, userId)
        if (existing != null) {
            throw IllegalArgumentException("Tag with name '${request.name}' already exists")
        }

        // 创建标签
        val tag = Tag(
            id = generateTagId(),
            name = request.name.trim(),
            color = request.color,
            description = request.description,
            userId = userId,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            cardCount = 0
        )

        val created = tagRepository.upsertTag(tag)
        return created.toResponse()
    }

    /**
     * 更新标签
     */
    suspend fun updateTag(
        id: String,
        request: UpdateTagRequest,
        userId: String
    ): TagResponse {
        // 验证请求
        request.validate()

        // 检查标签是否存在
        val existing = tagRepository.getTag(id)
            ?: throw NotFoundException("Tag", id)

        // 验证用户权限
        if (existing.userId != userId) {
            throw ForbiddenException("Not authorized to update this tag")
        }

        // 如果名称改变，检查新名称是否已被使用
        if (existing.name != request.name) {
            val existingWithNewName = tagRepository.getTagByName(request.name, userId)
            if (existingWithNewName != null && existingWithNewName.id != id) {
                throw IllegalArgumentException("Tag with name '${request.name}' already exists")
            }
        }

        // 更新标签
        val updated = existing.copy(
            name = request.name.trim(),
            color = request.color,
            description = request.description,
            updatedAt = Clock.System.now()
        )

        val saved = tagRepository.upsertTag(updated)
        return saved.toResponse()
    }

    /**
     * 删除标签
     */
    suspend fun deleteTag(id: String, userId: String) {
        // 检查标签是否存在
        val existing = tagRepository.getTag(id)
            ?: throw NotFoundException("Tag", id)

        // 验证用户权限
        if (existing.userId != userId) {
            throw ForbiddenException("Not authorized to delete this tag")
        }

        tagRepository.deleteTag(id)
    }

    /**
     * 生成标签 ID
     */
    private fun generateTagId(): String {
        return "tag-${System.currentTimeMillis()}-${(0..9999).random()}"
    }
}

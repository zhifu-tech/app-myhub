package tech.zhifu.app.myhub.service

import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.datastore.repository.TagRepository
import kotlin.time.Clock

/**
 * 标签服务
 */
class TagService(
    private val tagRepository: TagRepository
) {
    suspend fun getAllTags(): List<Tag> {
        return tagRepository.getAllTags()
    }

    suspend fun getTagById(id: String): Tag? {
        return tagRepository.getTagById(id)
    }

    suspend fun createTag(
        name: String,
        color: String? = null,
        description: String? = null
    ): Tag {
        if (name.isBlank()) {
            throw IllegalArgumentException("Tag name cannot be empty")
        }

        // 检查名称是否已存在
        tagRepository.getTagByName(name)?.let {
            throw IllegalArgumentException("Tag with name '$name' already exists")
        }

        val tag = Tag(
            id = generateId(),
            name = name.trim(),
            color = color,
            description = description,
            cardCount = 0,
            createdAt = Clock.System.now()
        )

        return tagRepository.createTag(tag)
    }

    suspend fun updateTag(tag: Tag): Tag {
        if (tag.name.isBlank()) {
            throw tech.zhifu.app.myhub.exception.ValidationException("Tag name cannot be empty")
        }

        return tagRepository.updateTag(tag)
    }

    suspend fun deleteTag(id: String): Boolean {
        return tagRepository.deleteTag(id)
    }

    private fun generateId(): String {
        return "tag-${System.currentTimeMillis()}-${(0..9999).random()}"
    }
}


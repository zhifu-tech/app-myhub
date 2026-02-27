package tech.zhifu.app.myhub.service

import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.model.dto.CardResponse
import tech.zhifu.app.myhub.datastore.model.dto.CreateCardRequest
import tech.zhifu.app.myhub.datastore.model.dto.PaginatedResponse
import tech.zhifu.app.myhub.datastore.model.dto.PaginationInfo
import tech.zhifu.app.myhub.datastore.model.dto.PartialUpdateCardRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateCardRequest
import tech.zhifu.app.myhub.datastore.model.dto.toResponse
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import tech.zhifu.app.myhub.datastore.repository.TagRepository
import tech.zhifu.app.myhub.exception.ForbiddenException
import tech.zhifu.app.myhub.exception.NotFoundException
import kotlin.time.Clock

/**
 * 卡片服务
 * 处理卡片相关的业务逻辑
 */
class CardService(
    private val cardRepository: CardRepository,
    private val tagRepository: TagRepository
) {
    /**
     * 获取卡片列表（支持分页和筛选）
     */
    suspend fun getCards(
        userId: String,
        page: Int? = null,
        limit: Int? = null,
        type: String? = null,
        isFavorite: Boolean? = null
    ): PaginatedResponse<CardResponse> {
        // 验证分页参数（如果提供了）
        if (page != null) {
            require(page > 0) { "Page must be greater than 0" }
        }
        if (limit != null) {
            require(limit in 1..100) { "Limit must be between 1 and 100" }
        }

        val cards = cardRepository.getCards(
            userId = userId,
            page = page,
            limit = limit,
            type = type,
            isFavorite = isFavorite
        )

        // 如果未指定分页，返回所有数据（total = cards.size）
        val actualPage = page ?: 1
        val actualLimit = limit ?: cards.size

        val total = if (page != null && limit != null) {
            cardRepository.countCards(
                userId = userId,
                type = type,
                isFavorite = isFavorite
            )
        } else {
            cards.size.toLong()
        }

        return PaginatedResponse(
            data = cards.map { it.toResponse() },
            pagination = PaginationInfo(
                page = actualPage,
                limit = actualLimit,
                total = total,
                totalPages = if (actualLimit > 0) ((total + actualLimit - 1) / actualLimit).toInt() else 1
            )
        )
    }

    /**
     * 获取指定卡片
     */
    suspend fun getCard(id: String, userId: String): CardResponse {
        val card = cardRepository.getCard(id)
            ?: throw NotFoundException("Card", id)

        // 验证用户权限
        if (card.userId != userId) {
            throw ForbiddenException("Not authorized to access this card")
        }

        return card.toResponse()
    }

    /**
     * 创建卡片
     */
    suspend fun createCard(request: CreateCardRequest, userId: String): CardResponse {
        // 验证请求
        request.validate()

        // 处理标签：从 tagIds 获取 Tag 对象
        val tags = resolveTags(request.tagIds, userId)

        // 创建卡片
        val card = Card(
            id = generateCardId(),
            type = CardType.fromWire(request.type),
            source = CardSource.Own,
            carriers = "[\"text\"]",
            userId = userId,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            tags = tags
        )

        val created = cardRepository.upsertCard(card)
        return created.toResponse()
    }

    /**
     * 完整更新卡片
     */
    suspend fun updateCard(
        id: String,
        request: UpdateCardRequest,
        userId: String
    ): CardResponse {
        // 验证请求
        request.validate()

        // 检查卡片是否存在
        val existing = cardRepository.getCard(id)
            ?: throw NotFoundException("Card", id)

        // 验证用户权限
        if (existing.userId != userId) {
            throw ForbiddenException("Not authorized to update this card")
        }

        // 处理标签：从 tagIds 获取 Tag 对象
        val tags = resolveTags(request.tagIds, userId)

        // 更新卡片
        val updated = existing.copy(
            type = CardType.fromWire(request.type),
            updatedAt = Clock.System.now(),
            tags = tags
        )

        val saved = cardRepository.upsertCard(updated)
        return saved.toResponse()
    }

    /**
     * 部分更新卡片
     */
    suspend fun partialUpdateCard(
        id: String,
        request: PartialUpdateCardRequest,
        userId: String
    ): CardResponse {
        // 验证请求
        request.validate()

        // 检查卡片是否存在
        val existing = cardRepository.getCard(id)
            ?: throw NotFoundException("Card", id)

        // 验证用户权限
        if (existing.userId != userId) {
            throw ForbiddenException("Not authorized to update this card")
        }

        // 处理标签：如果提供了 tagIds，则更新标签；否则保持原有标签
        val tags = request.tagIds?.let { tagIds ->
            resolveTags(tagIds, userId)
        } ?: existing.tags

        // 部分更新
        val updated = existing.copy(
            type = request.type?.let(CardType::fromWire) ?: existing.type,
            updatedAt = Clock.System.now(),
            tags = tags
        )

        val saved = cardRepository.upsertCard(updated)
        return saved.toResponse()
    }

    /**
     * 删除卡片
     */
    suspend fun deleteCard(id: String, userId: String) {
        // 检查卡片是否存在
        val existing = cardRepository.getCard(id)
            ?: throw NotFoundException("Card", id)

        // 验证用户权限
        if (existing.userId != userId) {
            throw ForbiddenException("Not authorized to delete this card")
        }

        cardRepository.deleteCard(id)
    }

    /**
     * 生成卡片 ID
     */
    private fun generateCardId(): String {
        return "card-${System.currentTimeMillis()}-${(0..9999).random()}"
    }

    /**
     * 从 tagIds 解析 Tag 对象列表
     * 如果 tagId 不存在，抛出 NotFoundException
     */
    private suspend fun resolveTags(tagIds: List<String>, userId: String): List<Tag> {
        if (tagIds.isEmpty()) return emptyList()

        val tags = tagIds.map { tagId ->
            val tag = tagRepository.getTag(tagId)
                ?: throw NotFoundException("Tag", tagId)
            // 验证标签属于当前用户
            if (tag.userId != userId) {
                throw ForbiddenException("Not authorized to use tag: $tagId")
            }
            tag
        }

        return tags
    }
}

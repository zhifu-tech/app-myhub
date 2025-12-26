package tech.zhifu.app.myhub.service

import tech.zhifu.app.myhub.datastore.model.CardDto
import tech.zhifu.app.myhub.datastore.model.CreateCardRequest
import tech.zhifu.app.myhub.datastore.model.UpdateCardRequest
import tech.zhifu.app.myhub.datastore.model.toDto
import tech.zhifu.app.myhub.datastore.model.toDomain
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.SearchFilter
import tech.zhifu.app.myhub.datastore.model.SortBy
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import kotlin.time.Clock

/**
 * 卡片服务
 * 处理卡片相关的业务逻辑
 */
class CardService(
    private val cardRepository: CardRepository
) {
    suspend fun getAllCards(): List<CardDto> {
        return cardRepository.getAllCards().map { it.toDto() }
    }

    suspend fun getCardById(id: String): CardDto? {
        return cardRepository.getCardById(id)?.toDto()
    }

    suspend fun searchCards(
        query: String? = null,
        types: String? = null,
        tags: String? = null,
        favorite: Boolean? = null,
        template: Boolean? = null,
        sortBy: String? = null
    ): List<CardDto> {
        val cardTypes = types?.split(",")?.mapNotNull { typeStr ->
            try {
                CardType.valueOf(typeStr.trim().uppercase())
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()

        val tagList = tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

        val sortByEnum = try {
            sortBy?.let { SortBy.valueOf(it.uppercase()) }
                ?: SortBy.UPDATED_AT_DESC
        } catch (e: Exception) {
            SortBy.UPDATED_AT_DESC
        }

        val filter = SearchFilter(
            query = query,
            cardTypes = cardTypes,
            tags = tagList,
            isFavorite = favorite,
            isTemplate = template,
            sortBy = sortByEnum
        )

        return cardRepository.searchCards(filter).map { it.toDto() }
    }

    suspend fun createCard(request: CreateCardRequest): CardDto {
        // 验证卡片类型
        try {
            CardType.valueOf(request.type.uppercase())
        } catch (e: Exception) {
            throw tech.zhifu.app.myhub.exception.ValidationException("Invalid card type: ${request.type}")
        }

        // 验证内容不为空
        if (request.content.isBlank()) {
            throw tech.zhifu.app.myhub.exception.ValidationException("Card content cannot be empty")
        }

        // 转换为 Card 模型
        val card = request.toDomain()
        val createdCard = cardRepository.createCard(card)
        return createdCard.toDto()
    }

    suspend fun updateCard(id: String, request: UpdateCardRequest): CardDto {
        val existing = cardRepository.getCardById(id)
            ?: throw tech.zhifu.app.myhub.exception.NotFoundException("Card", id)

        // 如果提供了内容，验证不为空
        request.content?.let {
            if (it.isBlank()) {
                throw tech.zhifu.app.myhub.exception.ValidationException("Card content cannot be empty")
            }
        }

        // 合并更新
        val updatedCard = existing.copy(
            title = request.title ?: existing.title,
            content = request.content ?: existing.content,
            author = request.author ?: existing.author,
            source = request.source ?: existing.source,
            language = request.language ?: existing.language,
            tags = request.tags ?: existing.tags,
            isFavorite = request.isFavorite ?: existing.isFavorite,
            isTemplate = request.isTemplate ?: existing.isTemplate,
            updatedAt = Clock.System.now(),
            metadata = request.metadata?.toDomain() ?: existing.metadata
        )
        
        val savedCard = cardRepository.updateCard(updatedCard)
        return savedCard.toDto()
    }

    suspend fun deleteCard(id: String): Boolean {
        return cardRepository.deleteCard(id)
    }

    suspend fun toggleFavorite(id: String): CardDto {
        val existing = cardRepository.getCardById(id)
            ?: throw tech.zhifu.app.myhub.exception.NotFoundException("Card", id)
        val toggledCard = cardRepository.toggleFavorite(id)
        return toggledCard.toDto()
    }
}


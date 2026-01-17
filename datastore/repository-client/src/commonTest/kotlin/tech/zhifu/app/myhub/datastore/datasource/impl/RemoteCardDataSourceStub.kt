package tech.zhifu.app.myhub.datastore.datasource.impl

import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.model.CardDto
import tech.zhifu.app.myhub.datastore.model.CreateCardRequest
import tech.zhifu.app.myhub.datastore.model.SearchFilter
import tech.zhifu.app.myhub.datastore.model.UpdateCardRequest
import kotlin.time.Clock

/**
 * 远程数据源占位实现（仅用于测试）
 */
class RemoteCardDataSourceStub : RemoteCardDataSource {
    private var nextId = 1
    private val cards = mutableListOf<CardDto>()

    override suspend fun getAllCards(): List<CardDto> {
        return cards.toList()
    }

    override suspend fun getCardById(id: String): CardDto? {
        return cards.find { it.id == id }
    }

    override suspend fun searchCards(filter: SearchFilter): List<CardDto> {
        return cards.toList()
    }

    override suspend fun createCard(request: CreateCardRequest): CardDto {
        val now = Clock.System.now().toString()
        val id = (nextId++).toString()
        val newCard = CardDto(
            id = id,
            type = request.type,
            title = request.title,
            content = request.content,
            author = request.author,
            source = request.source,
            language = request.language,
            isFavorite = request.isFavorite,
            isTemplate = request.isTemplate,
            createdAt = now,
            updatedAt = now,
            lastReviewedAt = null,
            tags = request.tags,
            metadata = request.metadata
        )
        cards.add(newCard)
        return newCard
    }

    override suspend fun updateCard(id: String, request: UpdateCardRequest): CardDto {
        val index = cards.indexOfFirst { it.id == id }
        val now = Clock.System.now().toString()
        val updatedCard = CardDto(
            id = id,
            type = cards.getOrNull(index)?.type ?: "QUOTE",
            title = request.title,
            content = request.content ?: "",
            author = request.author,
            source = request.source,
            language = request.language,
            isFavorite = request.isFavorite ?: false,
            isTemplate = request.isTemplate ?: false,
            createdAt = cards.getOrNull(index)?.createdAt ?: now,
            updatedAt = now,
            lastReviewedAt = null,
            tags = request.tags ?: emptyList(),
            metadata = request.metadata
        )
        if (index != -1) {
            cards[index] = updatedCard
        } else {
            cards.add(updatedCard)
        }
        return updatedCard
    }

    override suspend fun deleteCard(id: String) {
        cards.removeAll { it.id == id }
    }

    override suspend fun toggleFavorite(id: String): CardDto {
        val index = cards.indexOfFirst { it.id == id }
        val now = Clock.System.now().toString()
        if (index != -1) {
            val card = cards[index]
            val updated = card.copy(isFavorite = !card.isFavorite, updatedAt = now)
            cards[index] = updated
            return updated
        }
        return CardDto(
            id = id,
            type = "QUOTE",
            title = null,
            content = "",
            author = null,
            source = null,
            language = null,
            isFavorite = true,
            isTemplate = false,
            createdAt = now,
            updatedAt = now,
            lastReviewedAt = null,
            tags = emptyList(),
            metadata = null
        )
    }
}

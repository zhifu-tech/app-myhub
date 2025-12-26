package tech.zhifu.app.myhub.datastore.datasource.impl

import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.model.CardDto
import tech.zhifu.app.myhub.datastore.model.CreateCardRequest
import tech.zhifu.app.myhub.datastore.model.UpdateCardRequest
import tech.zhifu.app.myhub.datastore.model.SearchFilter
import kotlin.time.Clock

/**
 * 远程数据源占位实现（仅用于测试）
 */
class RemoteCardDataSourceStub : RemoteCardDataSource {
    private var nextId = 1

    override suspend fun getAllCards(): List<CardDto> {
        return emptyList()
    }

    override suspend fun getCardById(id: String): CardDto? {
        return null
    }

    override suspend fun searchCards(filter: SearchFilter): List<CardDto> {
        return emptyList()
    }

    override suspend fun createCard(request: CreateCardRequest): CardDto {
        val now = Clock.System.now().toString()
        val id = (nextId++).toString()
        return CardDto(
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
    }

    override suspend fun updateCard(id: String, request: UpdateCardRequest): CardDto {
        val now = Clock.System.now().toString()
        return CardDto(
            id = id,
            type = "QUOTE",
            title = request.title,
            content = request.content ?: "",
            author = request.author,
            source = request.source,
            language = request.language,
            isFavorite = request.isFavorite ?: false,
            isTemplate = request.isTemplate ?: false,
            createdAt = now,
            updatedAt = now,
            lastReviewedAt = null,
            tags = request.tags ?: emptyList(),
            metadata = request.metadata
        )
    }

    override suspend fun deleteCard(id: String) {
    }

    override suspend fun toggleFavorite(id: String): CardDto {
        val now = Clock.System.now().toString()
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


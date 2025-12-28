package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.UserContextProvider
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.SearchFilter
import tech.zhifu.app.myhub.datastore.model.SortBy
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import kotlin.time.Clock

/**
 * 卡片仓库实现（服务端）
 * 复用 LocalCardDataSource 的实现，避免代码重复
 */
class CardRepositoryImpl(
    private val localDataSource: LocalCardDataSource,
    private val userContextProvider: UserContextProvider
) : CardRepository {

    private suspend fun requireUserId(): String {
        return userContextProvider.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
    }

    override suspend fun getAllCards(): List<Card> {
        val userId = requireUserId()
        return localDataSource.getAllCards(userId)
    }

    override suspend fun getCardById(id: String): Card? {
        val userId = requireUserId()
        return localDataSource.getCardById(id, userId)
    }

    override suspend fun searchCards(filter: SearchFilter): List<Card> {
        // 获取所有卡片，然后进行过滤和排序
        val userId = requireUserId()
        val allCards = localDataSource.getAllCards(userId)

        return allCards.filter { card ->
            // 查询匹配
            val matchesQuery = filter.query?.let { query ->
                query.isBlank() ||
                    card.title?.contains(query, ignoreCase = true) == true ||
                    card.content.contains(query, ignoreCase = true) ||
                    card.author?.contains(query, ignoreCase = true) == true ||
                    card.tags.any { it.contains(query, ignoreCase = true) }
            } ?: true

            // 类型过滤
            val matchesType = filter.cardTypes.isEmpty() || filter.cardTypes.contains(card.type)

            // 标签过滤
            val matchesTags = filter.tags.isEmpty() || filter.tags.all { card.tags.contains(it) }

            // 收藏过滤
            val matchesFavorite = filter.isFavorite?.let { card.isFavorite == it } ?: true

            // 模板过滤
            val matchesTemplate = filter.isTemplate?.let { card.isTemplate == it } ?: true

            matchesQuery && matchesType && matchesTags && matchesFavorite && matchesTemplate
        }.sortedWith(compareBy { card ->
            when (filter.sortBy) {
                SortBy.CREATED_AT_ASC -> card.createdAt.epochSeconds
                SortBy.CREATED_AT_DESC -> -card.createdAt.epochSeconds
                SortBy.UPDATED_AT_ASC -> card.updatedAt.epochSeconds
                SortBy.UPDATED_AT_DESC -> -card.updatedAt.epochSeconds
                SortBy.TITLE_ASC -> card.title?.lowercase() ?: ""
                SortBy.TITLE_DESC -> card.title?.lowercase()?.reversed() ?: ""
                SortBy.LAST_REVIEWED_AT_ASC -> card.lastReviewedAt?.epochSeconds ?: Long.MAX_VALUE
                SortBy.LAST_REVIEWED_AT_DESC -> -(card.lastReviewedAt?.epochSeconds ?: Long.MIN_VALUE)
            }
        })
    }

    override suspend fun createCard(card: Card): Card {
        val userId = requireUserId()
        val cardToInsert = if (card.id.isBlank()) {
            card.copy(id = generateId(), createdAt = Clock.System.now(), updatedAt = Clock.System.now())
        } else {
            card.copy(updatedAt = Clock.System.now())
        }
        localDataSource.insertCard(cardToInsert, userId)
        return cardToInsert
    }

    override suspend fun updateCard(card: Card): Card {
        val userId = requireUserId()
        val existing = localDataSource.getCardById(card.id, userId)
            ?: throw IllegalArgumentException("Card not found: ${card.id}")

        val updatedCard = card.copy(updatedAt = Clock.System.now())
        localDataSource.updateCard(updatedCard, userId)
        return updatedCard
    }

    override suspend fun deleteCard(id: String): Boolean {
        return try {
            val userId = requireUserId()
            localDataSource.deleteCard(id, userId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun toggleFavorite(cardId: String): Card {
        val userId = requireUserId()
        val existing = localDataSource.getCardById(cardId, userId)
            ?: throw IllegalArgumentException("Card not found: $cardId")

        val updated = existing.copy(
            isFavorite = !existing.isFavorite,
            updatedAt = Clock.System.now()
        )

        localDataSource.updateCard(updated, userId)
        return updated
    }

    private fun generateId(): String {
        return "card-${System.currentTimeMillis()}-${(0..9999).random()}"
    }
}

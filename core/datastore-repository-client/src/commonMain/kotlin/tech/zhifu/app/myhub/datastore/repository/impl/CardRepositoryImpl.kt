package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.CreateCardRequest
import tech.zhifu.app.myhub.datastore.model.SearchFilter
import tech.zhifu.app.myhub.datastore.model.SortBy
import tech.zhifu.app.myhub.datastore.model.UpdateCardRequest
import tech.zhifu.app.myhub.datastore.model.toDomain
import tech.zhifu.app.myhub.datastore.model.toDto
import tech.zhifu.app.myhub.datastore.repository.ReactiveCardRepository
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import kotlin.time.Clock

/**
 * 卡片仓库实现（客户端）
 * 实现本地和远程数据源的协调，支持响应式接口
 */
class CardRepositoryImpl(
    private val localDataSource: LocalCardDataSource,
    private val remoteDataSource: RemoteCardDataSource
) : ReactiveCardRepository {

    private val logger = logger("CardRepositoryImpl")

    override suspend fun getAllCards(): List<Card> {
        // 优先从远程获取最新数据
        return try {
            // 从远程获取所有卡片
            val remoteCards = remoteDataSource.getAllCards().map { it.toDomain() }

            logger.info { "Fetched ${remoteCards.size} cards from remote data source" }

            // 保存到本地（更新或插入）
            remoteCards.forEach { card ->
                val existingCard = localDataSource.getCardById(card.id)
                if (existingCard != null) {
                    localDataSource.updateCard(card)
                } else {
                    localDataSource.insertCard(card)
                }
            }
            remoteCards
        } catch (e: Exception) {
            logger.error(e) {
                "Failed to fetch cards from remote data source: ${e.message}"
            }
            // 如果远程获取失败，返回本地数据（降级处理）
            localDataSource.getAllCards()
        }
    }

    override fun observeAllCards(): Flow<List<Card>> {
        return localDataSource.observeCards()
    }

    override suspend fun getCardById(id: String): Card? {
        // 先从本地获取
        val localCard = localDataSource.getCardById(id)
        if (localCard != null) {
            return localCard
        }

        // 如果本地没有，从远程获取
        return try {
            val remoteCard = remoteDataSource.getCardById(id)?.toDomain()
            remoteCard?.let { localDataSource.insertCard(it) }
            remoteCard
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun searchCards(filter: SearchFilter): List<Card> {
        return localDataSource.getAllCards().filter { card ->
            // 搜索查询匹配
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

    override fun observeSearchCards(filter: SearchFilter): Flow<List<Card>> {
        return localDataSource.observeCards().map { cards ->
            cards.filter { card ->
                // 搜索查询匹配
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
                    SortBy.LAST_REVIEWED_AT_ASC -> card.lastReviewedAt?.epochSeconds
                        ?: Long.MAX_VALUE

                    SortBy.LAST_REVIEWED_AT_DESC -> -(card.lastReviewedAt?.epochSeconds
                        ?: Long.MIN_VALUE)
                }
            })
        }
    }

    override suspend fun createCard(card: Card): Card {
        // 先保存到本地
        localDataSource.insertCard(card)

        // 然后同步到远程
        val request = card.toDto().let { dto ->
            CreateCardRequest(
                type = dto.type,
                title = dto.title,
                content = dto.content,
                author = dto.author,
                source = dto.source,
                language = dto.language,
                tags = dto.tags,
                isFavorite = dto.isFavorite,
                isTemplate = dto.isTemplate,
                metadata = dto.metadata
            )
        }
        val remoteCard = remoteDataSource.createCard(request).toDomain()

        // 更新本地数据
        localDataSource.updateCard(remoteCard)

        return remoteCard
    }

    override suspend fun updateCard(card: Card): Card {
        // 先更新本地
        val updatedCard = card.copy(updatedAt = Clock.System.now())
        localDataSource.updateCard(updatedCard)

        // 然后同步到远程
        val request = UpdateCardRequest(
            title = updatedCard.title,
            content = updatedCard.content,
            author = updatedCard.author,
            source = updatedCard.source,
            language = updatedCard.language,
            tags = updatedCard.tags,
            isFavorite = updatedCard.isFavorite,
            isTemplate = updatedCard.isTemplate,
            metadata = updatedCard.metadata?.toDto()
        )
        val remoteCard = remoteDataSource.updateCard(card.id, request).toDomain()

        // 更新本地数据
        localDataSource.updateCard(remoteCard)

        return remoteCard
    }

    override suspend fun deleteCard(id: String): Boolean {
        return try {
            // 先删除本地
            localDataSource.deleteCard(id)

            // 然后删除远程
            remoteDataSource.deleteCard(id)

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun toggleFavorite(cardId: String): Card {
        val card = localDataSource.getCardById(cardId)
            ?: throw IllegalStateException("Card not found: $cardId")

        // 先更新本地
        val updatedCard = card.copy(isFavorite = !card.isFavorite, updatedAt = Clock.System.now())
        localDataSource.updateCard(updatedCard)

        // 然后同步到远程
        val remoteCard = remoteDataSource.toggleFavorite(cardId).toDomain()
        localDataSource.updateCard(remoteCard)

        return remoteCard
    }

    // ReactiveCardRepository 接口实现
    override fun observeFavoriteCards(): Flow<List<Card>> {
        return localDataSource.observeCards().map { cards ->
            cards.filter { it.isFavorite }
        }
    }

    override fun observeCardsByType(type: CardType): Flow<List<Card>> {
        return localDataSource.observeCards().map { cards ->
            cards.filter { it.type == type }
        }
    }

    override fun observeCardsByTag(tag: String): Flow<List<Card>> {
        return localDataSource.observeCards().map { cards ->
            cards.filter { it.tags.contains(tag) }
        }
    }
}

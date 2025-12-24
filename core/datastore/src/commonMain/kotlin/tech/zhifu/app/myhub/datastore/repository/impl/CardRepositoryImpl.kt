package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.dto.CreateCardRequest
import tech.zhifu.app.myhub.datastore.dto.UpdateCardRequest
import tech.zhifu.app.myhub.datastore.dto.toDomain
import tech.zhifu.app.myhub.datastore.dto.toDto
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.SearchFilter
import tech.zhifu.app.myhub.datastore.model.SortBy
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import kotlin.time.Clock

/**
 * 卡片仓库实现
 * 实现本地和远程数据源的协调
 */
class CardRepositoryImpl(
    private val localDataSource: LocalCardDataSource,
    private val remoteDataSource: RemoteCardDataSource
) : CardRepository {

    override fun getAllCards(): Flow<List<Card>> {
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

    override fun searchCards(filter: SearchFilter): Flow<List<Card>> {
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

    override suspend fun createCard(card: Card): Result<Card> {
        return try {
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

            Result.success(remoteCard)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCard(card: Card): Result<Card> {
        return try {
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

            Result.success(remoteCard)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCard(id: String): Result<Unit> {
        return try {
            // 先删除本地
            localDataSource.deleteCard(id)

            // 然后删除远程
            remoteDataSource.deleteCard(id)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(cardId: String): Result<Card> {
        return try {
            val card = localDataSource.getCardById(cardId)
                ?: return Result.failure(IllegalStateException("Card not found"))

            // 先更新本地
            val updatedCard = card.copy(isFavorite = !card.isFavorite)
            localDataSource.updateCard(updatedCard)

            // 然后同步到远程
            val remoteCard = remoteDataSource.toggleFavorite(cardId).toDomain()
            localDataSource.updateCard(remoteCard)

            Result.success(remoteCard)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFavoriteCards(): Flow<List<Card>> {
        return localDataSource.observeCards().map { cards ->
            cards.filter { it.isFavorite }
        }
    }

    override fun getCardsByType(type: CardType): Flow<List<Card>> {
        return localDataSource.observeCards().map { cards ->
            cards.filter { it.type == type }
        }
    }

    override fun getCardsByTag(tag: String): Flow<List<Card>> {
        return localDataSource.observeCards().map { cards ->
            cards.filter { it.tags.contains(tag) }
        }
    }

    override suspend fun sync(): Result<Unit> {
        return try {
            // 从远程获取所有卡片
            val remoteCards = remoteDataSource.getAllCards().map { it.toDomain() }

            // 更新本地数据
            remoteCards.forEach { card ->
                localDataSource.insertCard(card)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refresh(): Result<Unit> {
        return sync()
    }
}

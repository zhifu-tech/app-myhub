package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.ReviewProgress
import tech.zhifu.app.myhub.datastore.model.domain.articleMetadata
import tech.zhifu.app.myhub.datastore.model.domain.codeMetadata
import tech.zhifu.app.myhub.datastore.model.domain.ideaMetadata
import tech.zhifu.app.myhub.datastore.model.domain.quoteMetadata
import tech.zhifu.app.myhub.datastore.model.domain.todoMetadata
import tech.zhifu.app.myhub.datastore.model.domain.videoMetadata
import tech.zhifu.app.myhub.datastore.model.domain.wordMetadata

class LocalCardDataSourceImpl(
    private val database: MyHubDatabase
) : LocalCardDataSource {

    override suspend fun insertCard(card: Card) {
        database.transaction {
            database.cardQueries.insertCard(
                id = card.id,
                type = card.type,
                title = card.title,
                content = card.content,
                user_id = card.userId,
                created_at = card.createdAt.toString(),
                updated_at = card.updatedAt.toString()
            )

            database.card_tagQueries.deleteCardTagsByCardId(card.id)
            card.tags.forEach { tag ->
                database.card_tagQueries.insertCardTag(
                    card_id = card.id,
                    tag_id = tag.id,
                    created_at = card.createdAt.toString()
                )
            }

            database.user_cardQueries.insertUserCard(
                user_id = card.userId,
                card_id = card.id,
                is_favorite = 0,
                last_reviewed_at = null,
                created_at = card.createdAt.toString(),
                updated_at = card.updatedAt.toString()
            )

            card.articleMetadata?.let { metadata ->
                database.card_metadata_articleQueries.insertCardMetadataArticle(
                    card_id = card.id,
                    url = metadata.url,
                    summary = metadata.summary,
                    cover_image_url = metadata.coverImageUrl,
                    author = metadata.author
                )
            }

            card.codeMetadata?.let { metadata ->
                database.card_metadata_codeQueries.insertCardMetadataCode(
                    card_id = card.id,
                    language = metadata.language,
                    snippet = metadata.snippet,
                    description = metadata.description
                )
            }

            card.ideaMetadata?.let { metadata ->
                database.card_metadata_ideaQueries.insertCardMetadataIdea(
                    card_id = card.id,
                    priority = metadata.priority,
                    status = metadata.status
                )
            }

            card.quoteMetadata?.let { metadata ->
                database.card_metadata_quoteQueries.insertCardMetadataQuote(
                    card_id = card.id,
                    author = metadata.author,
                    category = metadata.category,
                    source = metadata.source
                )
            }

            card.todoMetadata?.let { metadata ->
                database.card_metadata_todoQueries.insertCardMetadataTodo(
                    card_id = card.id,
                    status = metadata.status,
                    priority = metadata.priority,
                    due_at = metadata.dueAt?.toString(),
                    completed_at = metadata.completedAt?.toString()
                )
            }

            card.wordMetadata?.let { metadata ->
                database.card_metadata_wordQueries.insertCardMetadataWord(
                    card_id = card.id,
                    pronunciation = metadata.pronunciation,
                    definition = metadata.definition,
                    example = metadata.example
                )
            }

            card.videoMetadata?.let { metadata ->
                database.card_metadata_videoQueries.insertVideoMetadata(
                    card_id = card.id,
                    video_url = metadata.videoUrl,
                    thumbnail_url = metadata.thumbnailUrl,
                    duration_seconds = metadata.durationSeconds?.toLong(),
                    platform = metadata.platform
                )
            }
        }
    }

    override suspend fun getCard(cardId: String): Card? {
        val card = database.card_with_metadataQueries
            .selectCardWithMetadataByCardId(cardId)
            .awaitAsList()
            .firstOrNull()
            ?: return null
        val tags = database.card_tagQueries
            .selectTagsByCardId(cardId)
            .awaitAsList()
            .map { it.toDomain() }
        return card.toDomain(tags)
    }

    override fun observeCard(cardId: String): Flow<Card> {
        val cardFlow = database.card_with_metadataQueries
            .selectCardWithMetadataByCardId(cardId)
            .asFlow()
            .mapToOne(Dispatchers.Default)
        val tagsFlow = database.card_tagQueries
            .selectTagsByCardId(cardId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }
        return combine(cardFlow, tagsFlow) { card, tags ->
            card.toDomain(tags)
        }
    }


    override suspend fun getCards(
        userId: String,
        page: Int,
        limit: Int,
        type: String?,
        isFavorite: Boolean?
    ): List<Card> {
        val offset = (page - 1) * limit
        val isFavoriteInt = isFavorite?.let { if (it) 1L else 0L }

        val cards = database.card_with_metadataQueries
            .selectCardWithMetadataByUserIdWithFilters(
                userId = userId,
                type = type,
                isFavorite = isFavoriteInt,
                limit = limit.toLong(),
                offset = offset.toLong()
            )
            .awaitAsList()

        if (cards.isEmpty()) {
            return emptyList()
        }

        val cardIds = cards.map { it.card_id }
        val tagRows = database.card_tagQueries
            .selectTagsByCardIds(cardIds)
            .awaitAsList()

        val tagsByCardId = tagRows.groupBy { it.card_id }
            .mapValues { entry -> entry.value.map { it.toDomain() } }
        return cards.map { card ->
            card.toDomain(tagsByCardId[card.card_id].orEmpty())
        }
    }

    override suspend fun countCards(
        userId: String,
        type: String?,
        isFavorite: Boolean?
    ): Long {
        val isFavoriteInt = isFavorite?.let { if (it) 1L else 0L }

        return database.card_with_metadataQueries
            .countCardWithMetadataByUserIdWithFilters(
                userId = userId,
                type = type,
                isFavorite = isFavoriteInt
            )
            .awaitAsOne()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCards(userId: String): Flow<List<Card>> {
        val cardsFlow = database.card_with_metadataQueries
            .selectCardWithMetadataByUserId(userId)
            .asFlow()
            .mapToList(Dispatchers.Default)
        val tagsFlow = cardsFlow
            .map { cards -> cards.map { it.card_id } }
            .distinctUntilChanged()
            .flatMapLatest { cardIds ->
                if (cardIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    database.card_tagQueries
                        .selectTagsByCardIds(cardIds)
                        .asFlow()
                        .mapToList(Dispatchers.Default)
                }
            }
            .map { rows ->
                rows.groupBy { it.card_id }
                    .mapValues { entry -> entry.value.map { it.toDomain() } }
            }
        return combine(cardsFlow, tagsFlow) { cards, tagsByCardId ->
            cards.map { card ->
                card.toDomain(tagsByCardId[card.card_id].orEmpty())
            }
        }
    }

    override suspend fun deleteCard(cardId: String) {
        database.cardQueries.deleteCard(cardId)
    }

    override suspend fun deleteCards(userId: String) {
        database.cardQueries.deleteCardsByUserId(userId)
    }

    override suspend fun getUnreviewedCards(userId: String): List<Card> {
        // selectUnreviewedCards 返回的是 Card 行，需要转换为完整的 Card 对象
        val cardRows = database.user_cardQueries
            .selectUnreviewedCards(userId)
            .awaitAsList()

        if (cardRows.isEmpty()) {
            return emptyList()
        }

        val cardIds = cardRows.map { it.id }

        // 从 card_with_metadata 获取完整卡片信息
        val cards = cardIds.mapNotNull { cardId ->
            val cardRow = database.card_with_metadataQueries
                .selectCardWithMetadataByCardId(cardId)
                .awaitAsList()
                .firstOrNull()
                ?: return@mapNotNull null

            val tags = database.card_tagQueries
                .selectTagsByCardId(cardId)
                .awaitAsList()
                .map { it.toDomain() }

            cardRow.toDomain(tags)
        }

        return cards
    }

    override suspend fun getReviewProgress(userId: String): ReviewProgress {
        val progress = database.user_cardQueries
            .selectReviewProgress(user_id = userId)
            .awaitAsOne()
        return ReviewProgress(
            completed = progress.completed.toInt(),
            total = progress.total_count.toInt()
        )
    }
}

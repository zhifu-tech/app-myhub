package tech.zhifu.app.myhub.datastore.datasource.card

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.tag.toDomain
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.attribution
import tech.zhifu.app.myhub.datastore.model.domain.carrierImage
import tech.zhifu.app.myhub.datastore.model.domain.carrierVideo
import tech.zhifu.app.myhub.datastore.model.domain.code
import tech.zhifu.app.myhub.datastore.model.domain.content
import tech.zhifu.app.myhub.datastore.model.domain.execution
import tech.zhifu.app.myhub.datastore.model.domain.lexicon
import tech.zhifu.app.myhub.datastore.model.domain.link
import tech.zhifu.app.myhub.datastore.model.domain.site

class LocalCardDataSourceImpl(
    private val database: MyHubDatabase
) : LocalCardDataSource {

    override suspend fun insertCard(card: Card) {
        database.transaction {
            database.cardQueries.insertCard(
                id = card.id,
                type = card.type.wire,
                source = card.source.wire,
                carriers = card.carriers,
                user_id = card.userId,
                created_at = card.createdAt.toString(),
                updated_at = card.updatedAt.toString()
            )

            card.metadata.content?.let {
                database.card_metadata_contentQueries.insertCardMetadataContent(
                    card_id = card.id,
                    title = it.title,
                    summary = it.summary,
                    content = it.content
                )
            }
            card.metadata.attribution?.let {
                database.card_metadata_attributionQueries.insertCardMetadataAttribution(
                    card_id = card.id,
                    author = it.author,
                    origin = it.origin,
                    language = it.language,
                    style_key = it.styleKey,
                    style_color = it.styleColor
                )
            }
            card.metadata.carrierImage?.let {
                database.card_metadata_carrier_imageQueries.insertCardMetadataCarrierImage(
                    card_id = card.id,
                    url = it.url,
                    thumbnail_url = it.thumbnailUrl
                )
            }
            card.metadata.carrierVideo?.let {
                database.card_metadata_carrier_videoQueries.insertCardMetadataCarrierVideo(
                    card_id = card.id,
                    url = it.videoUrl,
                    duration = it.durationSeconds,
                    platform = it.platform,
                    cover_image_url = it.coverImageUrl
                )
            }
            card.metadata.execution?.let {
                database.card_metadata_executionQueries.insertCardMetadataExecution(
                    card_id = card.id,
                    status = it.status,
                    priority = it.priority,
                    due_at = it.dueAt?.toString(),
                    completed_at = it.completedAt?.toString(),
                    steps = it.steps
                )
            }
            card.metadata.lexicon?.let {
                database.card_metadata_lexiconQueries.insertCardMetadataLexicon(
                    card_id = card.id,
                    pronunciation = it.pronunciation,
                    definition = it.definition,
                    example = it.example
                )
            }
            card.metadata.link?.let {
                database.card_metadata_linkQueries.insertCardMetadataLink(
                    card_id = card.id,
                    url = it.url
                )
            }
            card.metadata.code?.let {
                database.card_metadata_codeQueries.insertCardMetadataCode(
                    card_id = card.id,
                    language = it.language,
                    snippet = it.snippet
                )
            }
            card.metadata.site?.let {
                database.card_metadata_siteQueries.insertCardMetadataSite(
                    card_id = card.id,
                    id = it.id,
                    name = it.name,
                    fav_icon = it.favIcon
                )
            }

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
            Pair(card, tags)
        }.flatMapLatest { (card, tags) ->
            flow {
                emit(card.toDomain(tags))
            }
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
            Pair(cards, tagsByCardId)
        }.flatMapLatest { (cards, tagsByCardId) ->
            flow {
                val list = cards.map { card ->
                    card.toDomain(tagsByCardId[card.card_id].orEmpty())
                }
                emit(list)
            }
        }
    }

    override fun observeCardsPage(
        userId: String,
        page: Int,
        size: Int,
        sort: CardSort?,
    ): Flow<List<Card>> {
        val offset = (page - 1) * size
        val query = when (sort ?: CardSort.NEWEST) {
            CardSort.NEWEST -> database.card_with_metadataQueries
                .selectCardWithMetadataByUserIdWithFilters(
                    userId = userId,
                    type = null,
                    isFavorite = null,
                    limit = size.toLong(),
                    offset = offset.toLong()
                )

            CardSort.OLDEST -> database.card_with_metadataQueries
                .selectCardWithMetadataByUserIdWithFiltersOldest(
                    userId = userId,
                    type = null,
                    isFavorite = null,
                    limit = size.toLong(),
                    offset = offset.toLong()
                )
        }

        val cardsFlow = query
            .asFlow()
            .mapToList(Dispatchers.Default)

        val tagsFlow = cardsFlow
            .map { cards -> cards.map { it.card_id } }
            .distinctUntilChanged()
            .flatMapLatest { cardIds ->
                if (cardIds.isEmpty()) {
                    flowOf(emptyMap())
                } else {
                    database.card_tagQueries
                        .selectTagsByCardIds(cardIds)
                        .asFlow()
                        .mapToList(Dispatchers.Default)
                        .map { rows ->
                            rows.groupBy { it.card_id }
                                .mapValues { entry -> entry.value.map { it.toDomain() } }
                        }
                }
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
}

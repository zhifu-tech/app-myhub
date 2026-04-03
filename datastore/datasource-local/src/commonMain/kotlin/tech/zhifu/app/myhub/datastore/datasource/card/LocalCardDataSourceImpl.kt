package tech.zhifu.app.myhub.datastore.datasource.card

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.content
import tech.zhifu.app.myhub.datastore.model.domain.location
import tech.zhifu.app.myhub.datastore.model.domain.source
import tech.zhifu.app.myhub.datastore.model.domain.tags
import tech.zhifu.app.myhub.datastore.model.domain.ui
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger

class LocalCardDataSourceImpl(
    private val database: MyHubDatabase
) : LocalCardDataSource {
    private val json = Json {
        explicitNulls = false
        encodeDefaults = false
    }

    override suspend fun insertCard(
        userId: String,
        card: Card,
    ) {
        runCatching {
            database.transaction {
                val createdEpoch = card.createdAt.toEpochMilliseconds()
                val updatedEpoch = card.updatedAt.toEpochMilliseconds()
                database.cardQueries.insertCard(
                    id = card.id,
                    type = card.type.value,
                    title = card.title,
                    summary = card.summary,
                    content = card.content?.let { json.encodeToString(it) },
                    ui = card.ui?.let { json.encodeToString(it) },
                    location = card.location?.let { json.encodeToString(it) },
                    tags = json.encodeToString(card.tags),
                    status = card.status.wire,
                    source = card.source?.let { json.encodeToString(it) },
                    created_at = createdEpoch,
                    updated_at = updatedEpoch,
                    version = 1,
                    deleted_at = card.deletedAt?.toEpochMilliseconds(),
                )
                database.user_cardQueries.insertUserCard(
                    user_id = userId,
                    card_id = card.id,
                )
            }
        }.onFailure { error ->
            logger.error(error) {
                "insertCard.failed cardId=${card.id}, userId=$userId, error=${error.message}. " +
                    "请重点检查: 1) card 表结构是否已迁移到最新字段; 2) user 是否存在导致 FK 失败。"
            }
            throw error
        }
    }

    override fun flowCard(
        cardId: String
    ): Flow<Card?> = database.cardQueries.selectCardById(cardId)
        .asFlow()
        .mapToOneOrNull(context = Dispatchers.Default)
        .map { it?.toDomain() }

    override fun flowCards(
        userId: String,
        cursorCardId: String?,
        cursorTitle: String?,
        cursorUpdatedAt: Long?,
        orderByUpdated: Boolean,
        orderByTitle: Boolean,
        query: String?,
        limit: Int
    ): Flow<List<Card>> {
        val normalizedQuery = query
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.lowercase()

        val query = when {
            orderByUpdated -> {
                val hasCursor = cursorCardId != null && cursorUpdatedAt != null
                if (normalizedQuery != null) {
                    if (hasCursor) {
                        database.cardQueries.selectCardsByUserIdWithUpdatedDescSearchNext(
                            userId = userId,
                            query = normalizedQuery,
                            cursorUpdatedAt = cursorUpdatedAt,
                            cursorId = cursorCardId,
                            limit = limit.toLong()
                        )
                    } else {
                        database.cardQueries.selectCardsByUserIdWithUpdatedDescSearchFirst(
                            userId = userId,
                            query = normalizedQuery,
                            limit = limit.toLong()
                        )
                    }
                } else {
                    if (hasCursor) {
                        database.cardQueries.selectCardsByUserIdWithUpdatedDescNext(
                            userId = userId,
                            cursorUpdatedAt = cursorUpdatedAt,
                            cursorId = cursorCardId,
                            limit = limit.toLong()
                        )
                    } else {
                        database.cardQueries.selectCardsByUserIdWithUpdatedDescFirst(
                            userId = userId,
                            limit = limit.toLong()
                        )
                    }
                }
            }

            orderByTitle -> {
                logger.debug { "orderByTitle hasCursor=$cursorCardId, cursorTitle=$cursorTitle" }
                val hasCursor = cursorCardId != null && cursorTitle != null
                if (normalizedQuery != null) {
                    if (hasCursor) {
                        database.cardQueries.selectCardsByUserIdWithTitleDescSearchNext(
                            userId = userId,
                            query = normalizedQuery,
                            cursorTitle = cursorTitle,
                            cursorId = cursorCardId,
                            limit = limit.toLong()
                        )
                    } else {
                        database.cardQueries.selectCardsByUserIdWithTitleDescSearchFirst(
                            userId = userId,
                            query = normalizedQuery,
                            limit = limit.toLong()
                        )
                    }
                } else {
                    if (hasCursor) {
                        database.cardQueries.selectCardsByUserIdWithTitleDescNext(
                            userId = userId,
                            cursorTitle = cursorTitle,
                            cursorId = cursorCardId,
                            limit = limit.toLong()
                        )
                    } else {
                        database.cardQueries.selectCardsByUserIdWithTitleDescFirst(
                            userId = userId,
                            limit = limit.toLong()
                        )
                    }
                }
            }

            else -> error("No ordering specified")
        }

        return query.asFlow()
            .mapToList(context = Dispatchers.Default)
            .map(transform = List<DbCard>::toDomainList)
    }

    override suspend fun deleteCard(
        cardId: String
    ) {
        database.cardQueries.deleteCard(cardId)
    }
}

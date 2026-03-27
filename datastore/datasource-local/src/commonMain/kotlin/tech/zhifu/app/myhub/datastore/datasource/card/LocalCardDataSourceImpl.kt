package tech.zhifu.app.myhub.datastore.datasource.card

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.content
import tech.zhifu.app.myhub.datastore.model.domain.location
import tech.zhifu.app.myhub.datastore.model.domain.source
import tech.zhifu.app.myhub.datastore.model.domain.tags
import tech.zhifu.app.myhub.datastore.model.domain.ui

class LocalCardDataSourceImpl(
    private val database: MyHubDatabase
) : LocalCardDataSource {

    override suspend fun insertCard(
        userId: String,
        card: Card,
    ) = database.transaction {
        val createdEpoch = card.createdAt.toEpochMilliseconds()
        val updatedEpoch = card.updatedAt.toEpochMilliseconds()
        database.cardQueries.insertCard(
            id = card.id,
            type = card.type.value,
            title = card.title,
            summary = card.summary,
            content = card.content.toString(),
            ui = card.ui.toString(),
            location = card.location?.toString().orEmpty(),
            tags = card.tags.toString(),
            status = card.status.wire,
            source = card.source.toString(),
            created_at = createdEpoch,
            updated_at = updatedEpoch,
            version = 1,
            deleted = 0,
        )
        database.user_cardQueries.insertUserCard(
            user_id = userId,
            card_id = card.id,
        )
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
        limit: Int
    ): Flow<List<Card>> {
        val query = when {
            orderByUpdated -> {
                val hasCursor = cursorCardId != null && cursorUpdatedAt != null
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

            orderByTitle -> {
                val hasCursor = cursorCardId != null && cursorTitle != null
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

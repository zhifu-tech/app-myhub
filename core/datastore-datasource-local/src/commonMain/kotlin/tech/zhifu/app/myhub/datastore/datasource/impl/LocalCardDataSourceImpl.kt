package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardMetadata
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.ChecklistItem
import kotlin.time.Instant

/**
 * 本地卡片数据源实现（使用SQLDelight）
 */
class LocalCardDataSourceImpl(
    private val database: MyHubDatabase
) : LocalCardDataSource {

    override suspend fun getAllCards(): List<Card> {
        return database.cardQueries.selectAll().awaitAsList().map { it.toCard() }
    }

    override suspend fun getCardById(id: String): Card? {
        return database.cardQueries.selectById(id).awaitAsOneOrNull()?.toCard()
    }

    override suspend fun insertCard(card: Card) {
        database.transaction {
            // 插入卡片
            database.cardQueries.insertCard(
                id = card.id,
                type = card.type.name,
                title = card.title,
                content = card.content,
                author = card.author,
                source = card.source,
                language = card.language,
                is_favorite = if (card.isFavorite) 1L else 0L,
                is_template = if (card.isTemplate) 1L else 0L,
                created_at = card.createdAt.toString(),
                updated_at = card.updatedAt.toString(),
                last_reviewed_at = card.lastReviewedAt?.toString()
            )

            // 插入标签
            card.tags.forEach { tagName ->
                database.cardQueries.insertCardTag(card.id, tagName)
            }

            // 插入元数据
            val metadata = card.metadata
            if (metadata != null) {
                database.cardQueries.insertCardMetadata(
                    card_id = card.id,
                    quote_author = metadata.quoteAuthor,
                    quote_category = metadata.quoteCategory,
                    code_language = metadata.codeLanguage,
                    code_snippet = metadata.codeSnippet,
                    article_url = metadata.articleUrl,
                    article_summary = metadata.articleSummary,
                    article_image_url = metadata.articleImageUrl,
                    word_pronunciation = metadata.wordPronunciation,
                    word_definition = metadata.wordDefinition,
                    word_example = metadata.wordExample,
                    idea_priority = metadata.ideaPriority,
                    idea_status = metadata.ideaStatus
                )

                // 插入待办清单项
                metadata.checklistItems.forEach { item ->
                    database.cardQueries.insertChecklistItem(
                        id = item.id,
                        card_id = card.id,
                        text = item.text,
                        is_completed = if (item.isCompleted) 1L else 0L,
                        item_order = item.order.toLong()
                    )
                }
            }
        }
    }

    override suspend fun updateCard(card: Card) {
        database.transaction {
            // 更新卡片
            database.cardQueries.updateCard(
                type = card.type.name,
                title = card.title,
                content = card.content,
                author = card.author,
                source = card.source,
                language = card.language,
                is_favorite = if (card.isFavorite) 1L else 0L,
                is_template = if (card.isTemplate) 1L else 0L,
                updated_at = card.updatedAt.toString(),
                last_reviewed_at = card.lastReviewedAt?.toString(),
                id = card.id
            )

            // 更新标签（先删除再插入）
            database.cardQueries.deleteCardTags(card.id)
            card.tags.forEach { tagName ->
                database.cardQueries.insertCardTag(card.id, tagName)
            }

            // 更新元数据
            val metadata = card.metadata
            if (metadata != null) {
                database.cardQueries.insertCardMetadata(
                    card_id = card.id,
                    quote_author = metadata.quoteAuthor,
                    quote_category = metadata.quoteCategory,
                    code_language = metadata.codeLanguage,
                    code_snippet = metadata.codeSnippet,
                    article_url = metadata.articleUrl,
                    article_summary = metadata.articleSummary,
                    article_image_url = metadata.articleImageUrl,
                    word_pronunciation = metadata.wordPronunciation,
                    word_definition = metadata.wordDefinition,
                    word_example = metadata.wordExample,
                    idea_priority = metadata.ideaPriority,
                    idea_status = metadata.ideaStatus
                )

                // 更新待办清单项（先删除再插入）
                database.cardQueries.deleteChecklistItems(card.id)
                metadata.checklistItems.forEach { item ->
                    database.cardQueries.insertChecklistItem(
                        id = item.id,
                        card_id = card.id,
                        text = item.text,
                        is_completed = if (item.isCompleted) 1L else 0L,
                        item_order = item.order.toLong()
                    )
                }
            } else {
                // 如果没有元数据，删除现有的
                database.cardQueries.deleteCardMetadata(card.id)
                database.cardQueries.deleteChecklistItems(card.id)
            }
        }
    }

    override suspend fun deleteCard(id: String) {
        // 由于外键约束，删除卡片会自动删除关联的标签、元数据和待办清单项
        database.cardQueries.deleteCard(id)
    }

    override suspend fun deleteAllCards() {
        database.cardQueries.deleteAll()
    }

    override fun observeCards(): Flow<List<Card>> {
        return database.cardQueries.selectAll()
            .asFlow()
            .mapLatest { query ->
                query.awaitAsList().map { row ->
                    row.toCard()
                }
            }
    }

    /**
     * 将数据库行转换为Card实体
     */
    private suspend fun tech.zhifu.app.myhub.datastore.database.Card.toCard(): Card {
        val cardId = id
        val tags = database.cardQueries.selectCardTags(cardId).awaitAsList()
        val metadataRow = database.cardQueries.selectCardMetadata(cardId).awaitAsOneOrNull()
        val checklistItems = database.cardQueries.selectChecklistItems(cardId)
            .awaitAsList()
            .map { item ->
                ChecklistItem(
                    id = item.id,
                    text = item.text,
                    isCompleted = item.is_completed == 1L,
                    order = item.item_order.toInt()
                )
            }

        val metadata = metadataRow?.let { row ->
            CardMetadata(
                quoteAuthor = row.quote_author,
                quoteCategory = row.quote_category,
                codeLanguage = row.code_language,
                codeSnippet = row.code_snippet,
                articleUrl = row.article_url,
                articleSummary = row.article_summary,
                articleImageUrl = row.article_image_url,
                wordPronunciation = row.word_pronunciation,
                wordDefinition = row.word_definition,
                wordExample = row.word_example,
                checklistItems = checklistItems,
                ideaPriority = row.idea_priority,
                ideaStatus = row.idea_status
            )
        } ?: if (checklistItems.isNotEmpty()) {
            CardMetadata(checklistItems = checklistItems)
        } else {
            null
        }

        return Card(
            id = cardId,
            type = CardType.valueOf(type),
            title = title,
            content = content,
            author = author,
            source = source,
            language = language,
            tags = tags,
            isFavorite = is_favorite == 1L,
            isTemplate = is_template == 1L,
            createdAt = Instant.parse(created_at),
            updatedAt = Instant.parse(updated_at),
            lastReviewedAt = last_reviewed_at?.let { Instant.parse(it) },
            metadata = metadata
        )
    }
}

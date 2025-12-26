package tech.zhifu.app.myhub.datastore.database.manage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import tech.zhifu.app.myhub.datastore.database.manage.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.model.Card

/**
 * 卡片数据加载器
 * 从 JSON 文件加载卡片数据并写入数据库
 */
class CardDataLoader(
    private val database: MyHubDatabase
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        serializersModule = SerializersModule {
            contextual(InstantSerializer)
        }
    }

    /**
     * 从资源文件加载数据
     * @param resourcePath 资源文件路径，相对于 composeResources 目录（例如："database/init/card.json"）
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadFromResource(resourcePath: String) = withContext(Dispatchers.Default) {
        val jsonString = Res.readBytes("files/$resourcePath").decodeToString()
        val cards = json.decodeFromString<List<Card>>(jsonString)
        insertCards(cards)
    }

    /**
     * 插入卡片数据
     */
    private suspend fun insertCards(cards: List<Card>) = withContext(Dispatchers.Default) {
        database.transaction {
            cards.forEach { card ->
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

                // 插入标签关联
                card.tags.forEach { tagName ->
                    database.cardQueries.insertCardTag(
                        card_id = card.id,
                        tag_name = tagName
                    )
                }

                // 插入元数据（如果有）
                card.metadata?.let { metadata ->
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
                }

                // 插入待办清单项（如果有）
                card.metadata?.checklistItems?.forEachIndexed { index, item ->
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

    /**
     * 清空卡片数据
     */
    suspend fun clearData() = withContext(Dispatchers.Default) {
        database.cardQueries.deleteAll()
    }
}



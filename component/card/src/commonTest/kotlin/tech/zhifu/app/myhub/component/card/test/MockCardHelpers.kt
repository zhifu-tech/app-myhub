package tech.zhifu.app.myhub.component.card.test

import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardMetadata
import tech.zhifu.app.myhub.datastore.model.CardType
import kotlin.time.Clock

/**
 * 测试辅助函数 - 创建 Mock Card 对象
 */
object MockCardHelpers {

    /**
     * 创建测试用的 Quote Card
     */
    fun createTestQuoteCard(
        id: String = "test-quote-1",
        content: String = "The only way to do great work is to love what you do.",
        author: String? = "Steve Jobs",
        category: String = "MOTIVATION",
        isFavorite: Boolean = false,
        title: String? = null
    ): Card {
        val now = Clock.System.now()
        return Card(
            id = id,
            type = CardType.QUOTE,
            title = title,
            content = content,
            author = author,
            isFavorite = isFavorite,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                quoteAuthor = author,
                quoteCategory = category
            )
        )
    }

    /**
     * 创建测试用的 Code Card
     */
    fun createTestCodeCard(
        id: String = "test-code-1",
        title: String? = null,
        content: String = "fun main() {\n    println(\"Hello, World!\")\n}",
        language: String = "kotlin",
        isFavorite: Boolean = false
    ): Card {
        val now = Clock.System.now()
        return Card(
            id = id,
            type = CardType.CODE,
            title = title,
            content = content,
            language = language,
            isFavorite = isFavorite,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                codeLanguage = language,
                codeSnippet = content
            )
        )
    }

    /**
     * 创建测试用的 Idea Card
     */
    fun createTestIdeaCard(
        id: String = "test-idea-1",
        title: String? = null,
        content: String = "This is a great idea!",
        isFavorite: Boolean = false
    ): Card {
        val now = Clock.System.now()
        return Card(
            id = id,
            type = CardType.IDEA,
            title = title,
            content = content,
            isFavorite = isFavorite,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                ideaStatus = "new"
            )
        )
    }

    /**
     * 创建测试用的 Article Card
     */
    fun createTestArticleCard(
        id: String = "test-article-1",
        title: String? = null,
        content: String = "This is an article summary...",
        url: String? = "https://example.com/article",
        isFavorite: Boolean = false
    ): Card {
        val now = Clock.System.now()
        return Card(
            id = id,
            type = CardType.ARTICLE,
            title = title,
            content = content,
            source = url,
            isFavorite = isFavorite,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                articleUrl = url,
                articleSummary = content
            )
        )
    }

    /**
     * 创建测试用的 Dictionary Card
     */
    fun createTestDictionaryCard(
        id: String = "test-dictionary-1",
        title: String? = null,
        word: String = "hello",
        definition: String = "A greeting",
        pronunciation: String? = "/həˈloʊ/",
        isFavorite: Boolean = false
    ): Card {
        val now = Clock.System.now()
        return Card(
            id = id,
            type = CardType.DICTIONARY,
            title = title,
            content = word,
            isFavorite = isFavorite,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                wordDefinition = definition,
                wordPronunciation = pronunciation
            )
        )
    }

    /**
     * 创建测试用的 Checklist Card
     */
    fun createTestChecklistCard(
        id: String = "test-checklist-1",
        title: String? = null,
        content: String = "Shopping List",
        isFavorite: Boolean = false
    ): Card {
        val now = Clock.System.now()
        return Card(
            id = id,
            type = CardType.CHECKLIST,
            title = title,
            content = content,
            isFavorite = isFavorite,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                checklistItems = emptyList()
            )
        )
    }

    /**
     * 创建测试用的 Card，支持自定义所有字段
     */
    fun createTestCard(
        id: String = "test-card-1",
        type: CardType = CardType.QUOTE,
        title: String? = null,
        content: String = "Test content",
        author: String? = null,
        source: String? = null,
        language: String? = null,
        tags: List<String> = emptyList(),
        isFavorite: Boolean = false,
        metadata: CardMetadata? = null
    ): Card {
        val now = Clock.System.now()
        return Card(
            id = id,
            type = type,
            title = title,
            content = content,
            author = author,
            source = source,
            language = language,
            tags = tags,
            isFavorite = isFavorite,
            createdAt = now,
            updatedAt = now,
            metadata = metadata
        )
    }
}


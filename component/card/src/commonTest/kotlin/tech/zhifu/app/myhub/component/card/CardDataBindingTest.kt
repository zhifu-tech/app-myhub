package tech.zhifu.app.myhub.component.card

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import tech.zhifu.app.myhub.component.card.test.MockCardHelpers
import tech.zhifu.app.myhub.datastore.model.CardType

/**
 * Card 数据绑定测试
 * 测试卡片数据与组件之间的绑定关系
 */
class CardDataBindingTest {

    @Test
    fun `test QuoteCard data binding`() {
        // Given
        val card = MockCardHelpers.createTestQuoteCard(
            content = "Test quote content",
            author = "Test Author",
            category = "TEST",
            isFavorite = true
        )

        // Then - 验证数据正确绑定
        assertEquals(CardType.QUOTE, card.type)
        assertEquals("Test quote content", card.content)
        assertEquals("Test Author", card.author)
        assertEquals("Test Author", card.metadata?.quoteAuthor)
        assertEquals("TEST", card.metadata?.quoteCategory)
        assertEquals(true, card.isFavorite)
    }

    @Test
    fun `test CodeCard data binding`() {
        // Given
        val card = MockCardHelpers.createTestCodeCard(
            title = "My Code",
            content = "fun main() {}",
            language = "kotlin",
            isFavorite = false
        )

        // Then - 验证数据正确绑定
        assertEquals(CardType.CODE, card.type)
        assertEquals("My Code", card.title)
        assertEquals("fun main() {}", card.content)
        assertEquals("kotlin", card.language)
        assertEquals("kotlin", card.metadata?.codeLanguage)
        assertEquals(false, card.isFavorite)
    }

    @Test
    fun `test IdeaCard data binding`() {
        // Given
        val card = MockCardHelpers.createTestIdeaCard(
            title = "My Idea",
            content = "This is an idea",
            isFavorite = true
        )

        // Then - 验证数据正确绑定
        assertEquals(CardType.IDEA, card.type)
        assertEquals("My Idea", card.title)
        assertEquals("This is an idea", card.content)
        assertEquals(true, card.isFavorite)
        assertNotNull(card.metadata?.ideaStatus)
    }

    @Test
    fun `test ArticleCard data binding`() {
        // Given
        val card = MockCardHelpers.createTestArticleCard(
            title = "My Article",
            content = "Article summary",
            url = "https://example.com/article",
            isFavorite = false
        )

        // Then - 验证数据正确绑定
        assertEquals(CardType.ARTICLE, card.type)
        assertEquals("My Article", card.title)
        assertEquals("Article summary", card.content)
        assertEquals("https://example.com/article", card.source)
        assertEquals("https://example.com/article", card.metadata?.articleUrl)
        assertEquals("Article summary", card.metadata?.articleSummary)
        assertEquals(false, card.isFavorite)
    }

    @Test
    fun `test DictionaryCard data binding`() {
        // Given
        val card = MockCardHelpers.createTestDictionaryCard(
            title = "Hello",
            word = "hello",
            definition = "A greeting",
            pronunciation = "/həˈloʊ/",
            isFavorite = true
        )

        // Then - 验证数据正确绑定
        assertEquals(CardType.DICTIONARY, card.type)
        assertEquals("Hello", card.title)
        assertEquals("hello", card.content)
        assertEquals("A greeting", card.metadata?.wordDefinition)
        assertEquals("/həˈloʊ/", card.metadata?.wordPronunciation)
        assertEquals(true, card.isFavorite)
    }

    @Test
    fun `test ChecklistCard data binding`() {
        // Given
        val card = MockCardHelpers.createTestChecklistCard(
            title = "Shopping List",
            content = "Shopping List",
            isFavorite = false
        )

        // Then - 验证数据正确绑定
        assertEquals(CardType.CHECKLIST, card.type)
        assertEquals("Shopping List", card.title)
        assertEquals("Shopping List", card.content)
        assertEquals(false, card.isFavorite)
        assertNotNull(card.metadata?.checklistItems)
    }

    @Test
    fun `test Card with null optional fields`() {
        // Given - 测试可选字段为 null 的情况
        val card = MockCardHelpers.createTestQuoteCard(
            title = null,
            author = null
        )

        // Then - 验证 null 值正确处理
        assertEquals(null, card.title)
        assertEquals(null, card.author)
        assertEquals(null, card.metadata?.quoteAuthor)
    }

    @Test
    fun `test Card timestamps are set`() {
        // Given
        val card = MockCardHelpers.createTestQuoteCard()

        // Then - 验证时间戳已设置
        assertNotNull(card.createdAt)
        assertNotNull(card.updatedAt)
    }
}


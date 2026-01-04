package tech.zhifu.app.myhub.datastore.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Card 数据模型单元测试
 */
class CardTest {

    @Test
    fun `test Card creation with required fields`() {
        // Given
        val now = Clock.System.now()

        // When
        val card = Card(
            id = "card-1",
            type = CardType.QUOTE,
            content = "Test content",
            createdAt = now,
            updatedAt = now
        )

        // Then
        assertEquals("card-1", card.id)
        assertEquals(CardType.QUOTE, card.type)
        assertEquals("Test content", card.content)
        assertNull(card.title)
        assertNull(card.author)
        assertTrue(card.tags.isEmpty())
        assertFalse(card.isFavorite)
        assertFalse(card.isTemplate)
        assertNull(card.lastReviewedAt)
        assertNull(card.metadata)
    }

    @Test
    fun `test Card creation with all fields`() {
        // Given
        val now = Clock.System.now()
        val metadata = CardMetadata(
            quoteAuthor = "Test Author",
            quoteCategory = "Literature"
        )

        // When
        val card = Card(
            id = "card-1",
            type = CardType.QUOTE,
            title = "Test Title",
            content = "Test content",
            author = "Author",
            source = "Source",
            language = "en",
            tags = listOf("tag1", "tag2"),
            isFavorite = true,
            isTemplate = true,
            createdAt = now,
            updatedAt = now,
            lastReviewedAt = now,
            metadata = metadata,
            userId = "user-1"
        )

        // Then
        assertEquals("card-1", card.id)
        assertEquals(CardType.QUOTE, card.type)
        assertEquals("Test Title", card.title)
        assertEquals("Test content", card.content)
        assertEquals("Author", card.author)
        assertEquals("Source", card.source)
        assertEquals("en", card.language)
        assertEquals(2, card.tags.size)
        assertTrue(card.isFavorite)
        assertTrue(card.isTemplate)
        assertNotNull(card.lastReviewedAt)
        assertNotNull(card.metadata)
        assertEquals("user-1", card.userId)
    }

    @Test
    fun `test CardType enum values`() {
        // Then
        assertEquals(6, CardType.entries.size)
        assertEquals(CardType.QUOTE, CardType.entries[0])
        assertEquals(CardType.CODE, CardType.entries[1])
        assertEquals(CardType.IDEA, CardType.entries[2])
        assertEquals(CardType.ARTICLE, CardType.entries[3])
        assertEquals(CardType.DICTIONARY, CardType.entries[4])
        assertEquals(CardType.CHECKLIST, CardType.entries[5])
    }

    @Test
    fun `test CardMetadata with checklist items`() {
        // Given
        val checklistItems = listOf(
            ChecklistItem(id = "item-1", text = "Item 1", isCompleted = false, order = 0),
            ChecklistItem(id = "item-2", text = "Item 2", isCompleted = true, order = 1)
        )

        // When
        val metadata = CardMetadata(
            checklistItems = checklistItems
        )

        // Then
        assertEquals(2, metadata.checklistItems.size)
        assertEquals("item-1", metadata.checklistItems[0].id)
        assertEquals("Item 1", metadata.checklistItems[0].text)
        assertFalse(metadata.checklistItems[0].isCompleted)
        assertEquals("item-2", metadata.checklistItems[1].id)
        assertTrue(metadata.checklistItems[1].isCompleted)
    }

    @Test
    fun `test ChecklistItem creation`() {
        // When
        val item = ChecklistItem(
            id = "item-1",
            text = "Test item",
            isCompleted = true,
            order = 5
        )

        // Then
        assertEquals("item-1", item.id)
        assertEquals("Test item", item.text)
        assertTrue(item.isCompleted)
        assertEquals(5, item.order)
    }

    @Test
    fun `test ChecklistItem default values`() {
        // When
        val item = ChecklistItem(
            id = "item-1",
            text = "Test item"
        )

        // Then
        assertFalse(item.isCompleted)
        assertEquals(0, item.order)
    }
}


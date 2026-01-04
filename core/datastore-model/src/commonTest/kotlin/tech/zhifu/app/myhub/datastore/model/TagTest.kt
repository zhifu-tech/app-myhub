package tech.zhifu.app.myhub.datastore.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock

/**
 * Tag 数据模型单元测试
 */
class TagTest {

    @Test
    fun `test Tag creation with required fields`() {
        // Given
        val now = Clock.System.now()

        // When
        val tag = Tag(
            id = "tag-1",
            name = "Test Tag",
            createdAt = now
        )

        // Then
        assertEquals("tag-1", tag.id)
        assertEquals("Test Tag", tag.name)
        assertNull(tag.color)
        assertNull(tag.description)
        assertEquals(0, tag.cardCount)
        assertNull(tag.userId)
    }

    @Test
    fun `test Tag creation with all fields`() {
        // Given
        val now = Clock.System.now()

        // When
        val tag = Tag(
            id = "tag-1",
            name = "Test Tag",
            color = "#FF5733",
            description = "Test description",
            cardCount = 10,
            createdAt = now,
            userId = "user-1"
        )

        // Then
        assertEquals("tag-1", tag.id)
        assertEquals("Test Tag", tag.name)
        assertEquals("#FF5733", tag.color)
        assertEquals("Test description", tag.description)
        assertEquals(10, tag.cardCount)
        assertEquals("user-1", tag.userId)
    }

    @Test
    fun `test TagStats creation`() {
        // When
        val stats = TagStats(
            tagId = "tag-1",
            tagName = "Test Tag",
            totalCards = 10,
            favoriteCards = 5,
            recentCards = 3
        )

        // Then
        assertEquals("tag-1", stats.tagId)
        assertEquals("Test Tag", stats.tagName)
        assertEquals(10, stats.totalCards)
        assertEquals(5, stats.favoriteCards)
        assertEquals(3, stats.recentCards)
    }
}


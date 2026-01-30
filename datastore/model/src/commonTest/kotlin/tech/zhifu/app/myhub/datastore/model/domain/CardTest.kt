package tech.zhifu.app.myhub.datastore.model.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.time.Instant

/**
 * Card 领域模型单元测试
 */
class CardTest {

    private fun instant(epochMillis: Long = 0L) = Instant.fromEpochMilliseconds(epochMillis)

    @Test
    fun `Card equality`() {
        val a = Card(
            id = "c1",
            type = "word",
            title = "Title",
            content = "Content",
            userId = "u1",
            createdAt = instant(1000),
            updatedAt = instant(2000)
        )
        val b = a.copy()
        val c = a.copy(id = "c2")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    @Test
    fun `Card copy`() {
        val card = Card(
            id = "c1",
            type = "idea",
            content = "content",
            userId = "u1",
            createdAt = instant(),
            updatedAt = instant(1)
        )
        val copied = card.copy(title = "New Title", content = "New Content")
        assertEquals("c1", copied.id)
        assertEquals("New Title", copied.title)
        assertEquals("New Content", copied.content)
        assertEquals(instant(), copied.createdAt)
    }

    @Test
    fun `Card default optional fields`() {
        val card = Card(
            id = "c1",
            type = "word",
            content = "x",
            userId = "u1",
            createdAt = instant(),
            updatedAt = instant()
        )
        assertEquals(null, card.title)
        assertEquals(emptyList(), card.tags)
    }

    @Test
    fun `Card isFavorite extension`() {
        val card = Card(
            id = "c1",
            type = "word",
            content = "x",
            userId = "u1",
            createdAt = instant(),
            updatedAt = instant()
        )
        assertFalse(card.isFavorite)
    }
}

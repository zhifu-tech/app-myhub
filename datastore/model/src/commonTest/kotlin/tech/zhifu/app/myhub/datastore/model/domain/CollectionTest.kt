package tech.zhifu.app.myhub.datastore.model.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Instant

/**
 * Collection 领域模型单元测试
 */
class CollectionTest {

    private fun instant(epochMillis: Long = 0L) = Instant.fromEpochMilliseconds(epochMillis)

    @Test
    fun `Collection equality`() {
        val a = Collection(
            id = "col1",
            name = "My Collection",
            userId = "u1",
            createdAt = instant(1000),
            updatedAt = instant(2000)
        )
        val b = a.copy()
        val c = a.copy(name = "Other")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    @Test
    fun `Collection default optional fields`() {
        val col = Collection(
            id = "col1",
            name = "Name",
            userId = "u1",
            createdAt = instant(),
            updatedAt = instant()
        )
        assertEquals(null, col.topic)
        assertEquals(null, col.description)
        assertEquals(0, col.cardCount)
        assertEquals(emptyList(), col.cards)
    }

    @Test
    fun `Collection copy`() {
        val col = Collection(
            id = "col1",
            name = "Name",
            topic = "Topic",
            userId = "u1",
            createdAt = instant(),
            updatedAt = instant(),
            cardCount = 3
        )
        val copied = col.copy(description = "Desc", cardCount = 10)
        assertEquals("Desc", copied.description)
        assertEquals(10, copied.cardCount)
        assertEquals("Topic", copied.topic)
    }
}

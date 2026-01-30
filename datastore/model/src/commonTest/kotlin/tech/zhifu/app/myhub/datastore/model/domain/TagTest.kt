package tech.zhifu.app.myhub.datastore.model.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Instant

/**
 * Tag 领域模型单元测试
 */
class TagTest {

    private fun instant(epochMillis: Long = 0L) = Instant.fromEpochMilliseconds(epochMillis)

    @Test
    fun `Tag equality`() {
        val a = Tag(
            id = "t1",
            name = "Tag1",
            color = "#fff",
            userId = "u1",
            createdAt = instant(1000),
            updatedAt = instant(2000)
        )
        val b = a.copy()
        val c = a.copy(name = "Tag2")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    @Test
    fun `Tag copy and default optional fields`() {
        val tag = Tag(
            id = "t1",
            name = "Name",
            userId = "u1",
            createdAt = instant(),
            updatedAt = instant()
        )
        assertEquals(null, tag.color)
        assertEquals(null, tag.description)
        assertEquals(0, tag.cardCount)

        val copied = tag.copy(color = "#red", cardCount = 5)
        assertEquals("#red", copied.color)
        assertEquals(5, copied.cardCount)
    }
}

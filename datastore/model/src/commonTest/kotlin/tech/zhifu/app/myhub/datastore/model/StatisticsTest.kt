package tech.zhifu.app.myhub.datastore.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Statistics 数据模型单元测试
 */
class StatisticsTest {

    @Test
    fun `test Statistics creation with default values`() {
        // When
        val stats = Statistics()

        // Then
        assertEquals(0, stats.totalCards)
        assertEquals(0, stats.favoriteCards)
        assertEquals(0, stats.recentEdits)
        assertTrue(stats.cardsByType.isEmpty())
        assertTrue(stats.cardsByTag.isEmpty())
        assertNull(stats.lastSyncTime)
    }

    @Test
    fun `test Statistics creation with all fields`() {
        // When
        val stats = Statistics(
            totalCards = 100,
            favoriteCards = 20,
            recentEdits = 10,
            cardsByType = mapOf(
                CardType.QUOTE to 30,
                CardType.CODE to 20,
                CardType.IDEA to 50
            ),
            cardsByTag = mapOf(
                "tag1" to 40,
                "tag2" to 60
            ),
            lastSyncTime = 1234567890L
        )

        // Then
        assertEquals(100, stats.totalCards)
        assertEquals(20, stats.favoriteCards)
        assertEquals(10, stats.recentEdits)
        assertEquals(3, stats.cardsByType.size)
        assertEquals(30, stats.cardsByType[CardType.QUOTE])
        assertEquals(20, stats.cardsByType[CardType.CODE])
        assertEquals(50, stats.cardsByType[CardType.IDEA])
        assertEquals(2, stats.cardsByTag.size)
        assertEquals(40, stats.cardsByTag["tag1"])
        assertEquals(60, stats.cardsByTag["tag2"])
        assertEquals(1234567890L, stats.lastSyncTime)
    }

    @Test
    fun `test CardStatistics creation`() {
        // When
        val cardStats = CardStatistics(
            cardId = "card-1",
            viewCount = 100,
            editCount = 10,
            shareCount = 5,
            lastViewedAt = 1234567890L
        )

        // Then
        assertEquals("card-1", cardStats.cardId)
        assertEquals(100, cardStats.viewCount)
        assertEquals(10, cardStats.editCount)
        assertEquals(5, cardStats.shareCount)
        assertEquals(1234567890L, cardStats.lastViewedAt)
    }

    @Test
    fun `test CardStatistics with default values`() {
        // When
        val cardStats = CardStatistics(cardId = "card-1")

        // Then
        assertEquals("card-1", cardStats.cardId)
        assertEquals(0, cardStats.viewCount)
        assertEquals(0, cardStats.editCount)
        assertEquals(0, cardStats.shareCount)
        assertNull(cardStats.lastViewedAt)
    }
}


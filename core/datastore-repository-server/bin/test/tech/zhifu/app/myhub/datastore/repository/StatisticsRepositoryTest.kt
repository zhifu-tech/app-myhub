package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalCardDataSourceImpl
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.repository.impl.StatisticsRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/**
 * StatisticsRepository 测试（服务端）
 */
class StatisticsRepositoryTest {

    @Test
    fun `test get statistics`() = runDatabaseTest { database ->
        // Given
        val cardDataSource = LocalCardDataSourceImpl(database)
        val repository = StatisticsRepositoryImpl(cardDataSource)

        // 创建测试卡片
        val card1 = createTestCard("1", CardType.QUOTE, isFavorite = true, tags = listOf("tag1"))
        val card2 = createTestCard("2", CardType.CODE, isFavorite = true, tags = listOf("tag1", "tag2"))
        val card3 = createTestCard("3", CardType.IDEA, isFavorite = false, tags = listOf("tag2"))

        cardDataSource.insertCard(card1)
        cardDataSource.insertCard(card2)
        cardDataSource.insertCard(card3)

        // When
        val result = repository.getStatistics()

        // Then
        assertNotNull(result)
        assertEquals(3, result.totalCards)
        assertEquals(2, result.favoriteCards)
        assertEquals(3, result.cardsByType.size)
        assertEquals(1, result.cardsByType[CardType.QUOTE])
        assertEquals(1, result.cardsByType[CardType.CODE])
        assertEquals(1, result.cardsByType[CardType.IDEA])
        assertEquals(2, result.cardsByTag["tag1"])
        assertEquals(2, result.cardsByTag["tag2"])
    }

    @Test
    fun `test get statistics with empty database`() = runDatabaseTest { database ->
        // Given
        val cardDataSource = LocalCardDataSourceImpl(database)
        val repository = StatisticsRepositoryImpl(cardDataSource)

        // When
        val result = repository.getStatistics()

        // Then
        assertNotNull(result)
        assertEquals(0, result.totalCards)
        assertEquals(0, result.favoriteCards)
        assertEquals(0, result.recentEdits)
        assertTrue(result.cardsByType.isEmpty())
        assertTrue(result.cardsByTag.isEmpty())
    }

    @Test
    fun `test refresh statistics`() = runDatabaseTest { database ->
        // Given
        val cardDataSource = LocalCardDataSourceImpl(database)
        val repository = StatisticsRepositoryImpl(cardDataSource)
        val card = createTestCard("1", CardType.QUOTE)
        cardDataSource.insertCard(card)

        // When
        val refreshedStats = repository.refreshStatistics()

        // Then
        assertNotNull(refreshedStats)
        assertEquals(1, refreshedStats.totalCards)
    }

    @Test
    fun `test get statistics with recent edits`() = runDatabaseTest { database ->
        // Given
        val cardDataSource = LocalCardDataSourceImpl(database)
        val repository = StatisticsRepositoryImpl(cardDataSource)

        // 创建最近编辑的卡片（7天内）
        val recentCard = createTestCard("1", CardType.QUOTE, updatedAt = Clock.System.now().minus(1.days))
        // 创建旧的卡片（超过7天）
        val oldCard = createTestCard("2", CardType.CODE, updatedAt = Clock.System.now().minus(10.days))

        cardDataSource.insertCard(recentCard)
        cardDataSource.insertCard(oldCard)

        // When
        val result = repository.getStatistics()

        // Then
        assertNotNull(result)
        assertEquals(2, result.totalCards)
        assertEquals(1, result.recentEdits) // 只有最近7天内的卡片
    }

    @Test
    fun `test get statistics with card type breakdown`() = runDatabaseTest { database ->
        // Given
        val cardDataSource = LocalCardDataSourceImpl(database)
        val repository = StatisticsRepositoryImpl(cardDataSource)

        val quoteCard1 = createTestCard("1", CardType.QUOTE)
        val quoteCard2 = createTestCard("2", CardType.QUOTE)
        val codeCard = createTestCard("3", CardType.CODE)
        val ideaCard = createTestCard("4", CardType.IDEA)

        cardDataSource.insertCard(quoteCard1)
        cardDataSource.insertCard(quoteCard2)
        cardDataSource.insertCard(codeCard)
        cardDataSource.insertCard(ideaCard)

        // When
        val result = repository.getStatistics()

        // Then
        assertNotNull(result)
        assertEquals(2, result.cardsByType[CardType.QUOTE])
        assertEquals(1, result.cardsByType[CardType.CODE])
        assertEquals(1, result.cardsByType[CardType.IDEA])
    }

    @Test
    fun `test get statistics with tag breakdown`() = runDatabaseTest { database ->
        // Given
        val cardDataSource = LocalCardDataSourceImpl(database)
        val repository = StatisticsRepositoryImpl(cardDataSource)

        val card1 = createTestCard("1", CardType.QUOTE, tags = listOf("tag1", "tag2"))
        val card2 = createTestCard("2", CardType.CODE, tags = listOf("tag1", "tag3"))
        val card3 = createTestCard("3", CardType.IDEA, tags = listOf("tag2"))

        cardDataSource.insertCard(card1)
        cardDataSource.insertCard(card2)
        cardDataSource.insertCard(card3)

        // When
        val result = repository.getStatistics()

        // Then
        assertNotNull(result)
        assertEquals(2, result.cardsByTag["tag1"])
        assertEquals(2, result.cardsByTag["tag2"])
        assertEquals(1, result.cardsByTag["tag3"])
    }

    /**
     * 创建测试用的卡片
     */
    private fun createTestCard(
        id: String,
        type: CardType,
        isFavorite: Boolean = false,
        tags: List<String> = emptyList(),
        updatedAt: kotlin.time.Instant = Clock.System.now()
    ): Card {
        return Card(
            id = id,
            type = type,
            title = "Test Card $id",
            content = "Test content",
            author = null,
            source = null,
            language = null,
            isFavorite = isFavorite,
            isTemplate = false,
            tags = tags,
            metadata = null,
            createdAt = Clock.System.now(),
            updatedAt = updatedAt,
            lastReviewedAt = null
        )
    }
}


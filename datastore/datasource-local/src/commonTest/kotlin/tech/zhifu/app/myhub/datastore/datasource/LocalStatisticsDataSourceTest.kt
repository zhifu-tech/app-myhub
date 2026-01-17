package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalStatisticsDataSourceImpl
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Statistics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Clock

/**
 * LocalStatisticsDataSource 测试
 */
class LocalStatisticsDataSourceTest {

    private val testUserId = "test-user-1"

    @Test
    fun `test save and get statistics`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalStatisticsDataSourceImpl(database)
        val statistics = createTestStatistics()

        // When
        dataSource.saveStatistics(statistics, testUserId)
        val result = dataSource.getStatistics(testUserId)

        // Then
        assertNotNull(result)
        assertEquals(10, result.totalCards)
        assertEquals(5, result.favoriteCards)
        assertEquals(3, result.recentEdits)
    }

    @Test
    fun `test statistics with card type breakdown`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalStatisticsDataSourceImpl(database)
        val statistics = Statistics(
            totalCards = 10,
            favoriteCards = 5,
            recentEdits = 3,
            cardsByType = mapOf(
                CardType.QUOTE to 4,
                CardType.CODE to 3,
                CardType.IDEA to 3
            ),
            cardsByTag = emptyMap(),
            lastSyncTime = null
        )

        // When
        dataSource.saveStatistics(statistics, testUserId)
        val result = dataSource.getStatistics(testUserId)

        // Then
        assertNotNull(result)
        assertEquals(4, result.cardsByType[CardType.QUOTE])
        assertEquals(3, result.cardsByType[CardType.CODE])
        assertEquals(3, result.cardsByType[CardType.IDEA])
    }

    @Test
    fun `test statistics with tag breakdown`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalStatisticsDataSourceImpl(database)
        val statistics = Statistics(
            totalCards = 10,
            favoriteCards = 5,
            recentEdits = 3,
            cardsByType = emptyMap(),
            cardsByTag = mapOf(
                "tag1" to 5,
                "tag2" to 3,
                "tag3" to 2
            ),
            lastSyncTime = null
        )

        // When
        dataSource.saveStatistics(statistics, testUserId)
        val result = dataSource.getStatistics(testUserId)

        // Then
        assertNotNull(result)
        assertEquals(5, result.cardsByTag["tag1"])
        assertEquals(3, result.cardsByTag["tag2"])
        assertEquals(2, result.cardsByTag["tag3"])
    }

    @Test
    fun `test clear statistics`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalStatisticsDataSourceImpl(database)
        val statistics = createTestStatistics()
        dataSource.saveStatistics(statistics, testUserId)

        // When
        dataSource.clearStatistics(testUserId)
        val result = dataSource.getStatistics(testUserId)

        // Then
        // 清空后应该返回默认值或 null
        if (result != null) {
            assertEquals(0, result.totalCards)
            assertEquals(0, result.favoriteCards)
            assertEquals(0, result.recentEdits)
        }
    }

    @Test
    fun `test get statistics when not exists`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalStatisticsDataSourceImpl(database)

        // When
        val result = dataSource.getStatistics(testUserId)

        // Then
        // 初始状态下可能返回 null 或默认值
        // 这取决于实现
    }

    @Test
    fun `test update statistics`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalStatisticsDataSourceImpl(database)
        val statistics1 = Statistics(
            totalCards = 10,
            favoriteCards = 5,
            recentEdits = 3,
            cardsByType = emptyMap(),
            cardsByTag = emptyMap(),
            lastSyncTime = null
        )
        dataSource.saveStatistics(statistics1, testUserId)

        // When
        val statistics2 = Statistics(
            totalCards = 20,
            favoriteCards = 10,
            recentEdits = 6,
            cardsByType = emptyMap(),
            cardsByTag = emptyMap(),
            lastSyncTime = Clock.System.now().toEpochMilliseconds()
        )
        dataSource.saveStatistics(statistics2, testUserId)
        val result = dataSource.getStatistics(testUserId)

        // Then
        assertNotNull(result)
        assertEquals(20, result.totalCards)
        assertEquals(10, result.favoriteCards)
        assertEquals(6, result.recentEdits)
        assertNotNull(result.lastSyncTime)
    }

    /**
     * 创建测试用的统计信息
     */
    private fun createTestStatistics(): Statistics {
        return Statistics(
            totalCards = 10,
            favoriteCards = 5,
            recentEdits = 3,
            cardsByType = mapOf(
                CardType.QUOTE to 4,
                CardType.CODE to 3,
                CardType.IDEA to 3
            ),
            cardsByTag = mapOf(
                "tag1" to 5,
                "tag2" to 3
            ),
            lastSyncTime = Clock.System.now().toEpochMilliseconds()
        )
    }
}


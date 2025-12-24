package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.first
import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalStatisticsDataSourceImpl
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.datastore.repository.impl.StatisticsRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * StatisticsRepository 测试
 */
class StatisticsRepositoryTest {

    @Test
    fun `test get statistics`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalStatisticsDataSourceImpl(database)
        val repository = StatisticsRepositoryImpl(localDataSource)
        val statistics = createTestStatistics()
        localDataSource.saveStatistics(statistics)

        // When
        val flow = repository.getStatistics()
        val result = flow.first()

        // Then
        assertNotNull(result)
        assertEquals(10, result.totalCards)
        assertEquals(5, result.favoriteCards)
        assertEquals(3, result.recentEdits)
    }

    @Test
    fun `test get statistics when not exists returns default`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalStatisticsDataSourceImpl(database)
        val repository = StatisticsRepositoryImpl(localDataSource)

        // When
        val flow = repository.getStatistics()
        val result = flow.first()

        // Then
        assertNotNull(result)
        assertEquals(0, result.totalCards)
        assertEquals(0, result.favoriteCards)
        assertEquals(0, result.recentEdits)
    }

    @Test
    fun `test refresh statistics`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalStatisticsDataSourceImpl(database)
        val repository = StatisticsRepositoryImpl(localDataSource)
        val statistics = createTestStatistics()
        localDataSource.saveStatistics(statistics)

        // When
        val result = repository.refreshStatistics()

        // Then
        assertTrue(result.isSuccess)
        val refreshedStats = result.getOrNull()
        assertNotNull(refreshedStats)
        assertEquals(10, refreshedStats.totalCards)
    }

    @Test
    fun `test get statistics with card type breakdown`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalStatisticsDataSourceImpl(database)
        val repository = StatisticsRepositoryImpl(localDataSource)
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
        localDataSource.saveStatistics(statistics)

        // When
        val flow = repository.getStatistics()
        val result = flow.first()

        // Then
        assertNotNull(result)
        assertEquals(4, result.cardsByType[CardType.QUOTE])
        assertEquals(3, result.cardsByType[CardType.CODE])
        assertEquals(3, result.cardsByType[CardType.IDEA])
    }

    @Test
    fun `test get statistics with tag breakdown`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalStatisticsDataSourceImpl(database)
        val repository = StatisticsRepositoryImpl(localDataSource)
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
        localDataSource.saveStatistics(statistics)

        // When
        val flow = repository.getStatistics()
        val result = flow.first()

        // Then
        assertNotNull(result)
        assertEquals(5, result.cardsByTag["tag1"])
        assertEquals(3, result.cardsByTag["tag2"])
        assertEquals(2, result.cardsByTag["tag3"])
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


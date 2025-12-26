package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.datasource.RemoteStatisticsDataSource
import tech.zhifu.app.myhub.datastore.datasource.TestUtils.createMockHttpClient
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.datastore.network.ApiConfig
import tech.zhifu.app.myhub.datastore.network.ApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Clock

/**
 * RemoteStatisticsDataSource 测试
 */
class RemoteStatisticsDataSourceTest {

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
                "tag2" to 3,
                "tag3" to 2
            ),
            lastSyncTime = Clock.System.now().toEpochMilliseconds()
        )
    }

    @Test
    fun `test getStatistics success`() = runTest {
        // Given
        val expectedStatistics = createTestStatistics()
        val httpClient = createMockHttpClient { request ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.STATISTICS_PATH}", request.url.toString())
            respond(
                content = Json.encodeToString(expectedStatistics),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteStatisticsDataSource = RemoteStatisticsDataSourceImpl(httpClient)

        // When
        val result = dataSource.getStatistics()

        // Then
        assertNotNull(result)
        assertEquals(10, result.totalCards)
        assertEquals(5, result.favoriteCards)
        assertEquals(3, result.recentEdits)
        assertEquals(4, result.cardsByType[CardType.QUOTE])
        assertEquals(3, result.cardsByType[CardType.CODE])
        assertEquals(5, result.cardsByTag["tag1"])
    }

    @Test
    fun `test getStatistics api error`() = runTest {
        // Given
        val httpClient = createMockHttpClient { request ->
            respond(
                content = "",
                status = HttpStatusCode.InternalServerError
            )
        }
        val dataSource: RemoteStatisticsDataSource = RemoteStatisticsDataSourceImpl(httpClient)

        // When & Then
        assertFailsWith<ApiException> {
            dataSource.getStatistics()
        }
    }
}


package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.datasource.RemoteTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.TestUtils.createMockHttpClient
import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.datastore.network.ApiConfig
import tech.zhifu.app.myhub.datastore.network.ApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock

/**
 * RemoteTagDataSource 测试
 */
class RemoteTagDataSourceTest {

    private fun createTestTag(id: String = "1", name: String = "tag1"): Tag {
        return Tag(
            id = id,
            name = name,
            color = "#FF5733",
            description = "Test description",
            cardCount = 0,
            createdAt = Clock.System.now()
        )
    }

    @Test
    fun `test getAllTags success`() = runTest {
        // Given
        val expectedTags = listOf(
            createTestTag("1", "tag1"),
            createTestTag("2", "tag2")
        )
        val httpClient = createMockHttpClient { request ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}", request.url.toString())
            respond(
                content = Json.encodeToString(expectedTags),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteTagDataSource = RemoteTagDataSourceImpl(httpClient)

        // When
        val result = dataSource.getAllTags()

        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("tag1", result[0].name)
    }

    @Test
    fun `test getAllTags api error`() = runTest {
        // Given
        val httpClient = createMockHttpClient { request ->
            respond(
                content = "",
                status = HttpStatusCode.InternalServerError
            )
        }
        val dataSource: RemoteTagDataSource = RemoteTagDataSourceImpl(httpClient)

        // When & Then
        assertFailsWith<ApiException> {
            dataSource.getAllTags()
        }
    }

    @Test
    fun `test getTagById success`() = runTest {
        // Given
        val expectedTag = createTestTag("1", "tag1")
        val httpClient = createMockHttpClient { request ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}/1", request.url.toString())
            respond(
                content = Json.encodeToString(expectedTag),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteTagDataSource = RemoteTagDataSourceImpl(httpClient)

        // When
        val result = dataSource.getTagById("1")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("tag1", result.name)
    }

    @Test
    fun `test getTagById not found`() = runTest {
        // Given
        val httpClient = createMockHttpClient { request ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }
        val dataSource: RemoteTagDataSource = RemoteTagDataSourceImpl(httpClient)

        // When
        val result = dataSource.getTagById("999")

        // Then
        assertNull(result)
    }

    @Test
    fun `test createTag success`() = runTest {
        // Given
        val tag = createTestTag("1", "new-tag")
        val httpClient = createMockHttpClient { requestData ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}", requestData.url.toString())
            respond(
                content = Json.encodeToString(tag),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteTagDataSource = RemoteTagDataSourceImpl(httpClient)

        // When
        val result = dataSource.createTag(tag)

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("new-tag", result.name)
    }

    @Test
    fun `test updateTag success`() = runTest {
        // Given
        val tag = createTestTag("1", "updated-tag")
        val httpClient = createMockHttpClient { requestData ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}/1", requestData.url.toString())
            respond(
                content = Json.encodeToString(tag),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteTagDataSource = RemoteTagDataSourceImpl(httpClient)

        // When
        val result = dataSource.updateTag(tag)

        // Then
        assertNotNull(result)
        assertEquals("updated-tag", result.name)
    }

    @Test
    fun `test updateTag not found`() = runTest {
        // Given
        val tag = createTestTag("999", "tag")
        val httpClient = createMockHttpClient { requestData ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }
        val dataSource: RemoteTagDataSource = RemoteTagDataSourceImpl(httpClient)

        // When & Then
        assertFailsWith<ApiException> {
            dataSource.updateTag(tag)
        }
    }

    @Test
    fun `test deleteTag success`() = runTest {
        // Given
        val httpClient = createMockHttpClient { requestData ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}/1", requestData.url.toString())
            respond(
                content = "",
                status = HttpStatusCode.NoContent
            )
        }
        val dataSource: RemoteTagDataSource = RemoteTagDataSourceImpl(httpClient)

        // When & Then (should not throw)
        dataSource.deleteTag("1")
    }

    @Test
    fun `test deleteTag not found`() = runTest {
        // Given
        val httpClient = createMockHttpClient { requestData ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }
        val dataSource: RemoteTagDataSource = RemoteTagDataSourceImpl(httpClient)

        // When & Then (should not throw, NotFound is treated as success)
        dataSource.deleteTag("999")
    }
}


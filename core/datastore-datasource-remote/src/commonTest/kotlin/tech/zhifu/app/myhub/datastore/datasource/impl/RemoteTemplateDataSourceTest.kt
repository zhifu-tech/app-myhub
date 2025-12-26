package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.datasource.RemoteTemplateDataSource
import tech.zhifu.app.myhub.datastore.datasource.TestUtils.createMockHttpClient
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Template
import tech.zhifu.app.myhub.datastore.network.ApiConfig
import tech.zhifu.app.myhub.datastore.network.ApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock

/**
 * RemoteTemplateDataSource 测试
 */
class RemoteTemplateDataSourceTest {

    private fun createTestTemplate(id: String = "1", name: String = "template1"): Template {
        val now = Clock.System.now()
        return Template(
            id = id,
            name = name,
            description = "Test description",
            cardType = CardType.QUOTE,
            previewImageUrl = null,
            defaultContent = "Default content",
            defaultMetadata = null,
            defaultTags = emptyList(),
            usageCount = 0,
            isSystemTemplate = false,
            createdAt = now,
            updatedAt = now
        )
    }

    @Test
    fun `test getAllTemplates success`() = runTest {
        // Given
        val expectedTemplates = listOf(
            createTestTemplate("1", "template1"),
            createTestTemplate("2", "template2")
        )
        val httpClient = createMockHttpClient { request ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}", request.url.toString())
            respond(
                content = Json.encodeToString(expectedTemplates),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteTemplateDataSource = RemoteTemplateDataSourceImpl(httpClient)

        // When
        val result = dataSource.getAllTemplates()

        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("template1", result[0].name)
    }

    @Test
    fun `test getAllTemplates api error`() = runTest {
        // Given
        val httpClient = createMockHttpClient { request ->
            respond(
                content = "",
                status = HttpStatusCode.InternalServerError
            )
        }
        val dataSource: RemoteTemplateDataSource = RemoteTemplateDataSourceImpl(httpClient)

        // When & Then
        assertFailsWith<ApiException> {
            dataSource.getAllTemplates()
        }
    }

    @Test
    fun `test getTemplateById success`() = runTest {
        // Given
        val expectedTemplate = createTestTemplate("1", "template1")
        val httpClient = createMockHttpClient { request ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}/1", request.url.toString())
            respond(
                content = Json.encodeToString(expectedTemplate),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteTemplateDataSource = RemoteTemplateDataSourceImpl(httpClient)

        // When
        val result = dataSource.getTemplateById("1")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("template1", result.name)
    }

    @Test
    fun `test getTemplateById not found`() = runTest {
        // Given
        val httpClient = createMockHttpClient { request ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }
        val dataSource: RemoteTemplateDataSource = RemoteTemplateDataSourceImpl(httpClient)

        // When
        val result = dataSource.getTemplateById("999")

        // Then
        assertNull(result)
    }

    @Test
    fun `test createTemplate success`() = runTest {
        // Given
        val template = createTestTemplate("1", "new-template")
        val httpClient = createMockHttpClient { requestData ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}", requestData.url.toString())
            respond(
                content = Json.encodeToString(template),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteTemplateDataSource = RemoteTemplateDataSourceImpl(httpClient)

        // When
        val result = dataSource.createTemplate(template)

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("new-template", result.name)
    }

    @Test
    fun `test updateTemplate success`() = runTest {
        // Given
        val template = createTestTemplate("1", "updated-template")
        val httpClient = createMockHttpClient { requestData ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}/1", requestData.url.toString())
            respond(
                content = Json.encodeToString(template),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteTemplateDataSource = RemoteTemplateDataSourceImpl(httpClient)

        // When
        val result = dataSource.updateTemplate(template)

        // Then
        assertNotNull(result)
        assertEquals("updated-template", result.name)
    }

    @Test
    fun `test updateTemplate not found`() = runTest {
        // Given
        val template = createTestTemplate("999", "template")
        val httpClient = createMockHttpClient { requestData ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }
        val dataSource: RemoteTemplateDataSource = RemoteTemplateDataSourceImpl(httpClient)

        // When & Then
        assertFailsWith<ApiException> {
            dataSource.updateTemplate(template)
        }
    }

    @Test
    fun `test deleteTemplate success`() = runTest {
        // Given
        val httpClient = createMockHttpClient { requestData ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}/1", requestData.url.toString())
            respond(
                content = "",
                status = HttpStatusCode.NoContent
            )
        }
        val dataSource: RemoteTemplateDataSource = RemoteTemplateDataSourceImpl(httpClient)

        // When & Then (should not throw)
        dataSource.deleteTemplate("1")
    }

    @Test
    fun `test deleteTemplate not found`() = runTest {
        // Given
        val httpClient = createMockHttpClient { requestData ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }
        val dataSource: RemoteTemplateDataSource = RemoteTemplateDataSourceImpl(httpClient)

        // When & Then (should not throw, NotFound is treated as success)
        dataSource.deleteTemplate("999")
    }
}


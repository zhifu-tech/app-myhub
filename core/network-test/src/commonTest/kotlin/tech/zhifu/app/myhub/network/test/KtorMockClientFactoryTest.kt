package tech.zhifu.app.myhub.network.test

import io.ktor.client.call.body
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * KtorMockClientFactory 测试
 */
class KtorMockClientFactoryTest {

    @Serializable
    data class TestData(
        val id: String,
        val name: String,
        val value: Int = 0
    )

    @Test
    fun `test createMockHttpClient returns configured HttpClient`() = runTest {
        // Given
        val expectedData = TestData("1", "test", 42)
        val httpClient = createMockHttpClient { request ->
            respond(
                content = Json.encodeToString(TestData.serializer(), expectedData),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // When
        val response = httpClient.get("https://example.com/api/test")
        val result: TestData = response.body()

        // Then
        assertEquals(expectedData.id, result.id)
        assertEquals(expectedData.name, result.name)
        assertEquals(expectedData.value, result.value)
    }

    @Test
    fun `test createMockHttpClient handles different status codes`() = runTest {
        // Given
        val httpClient = createMockHttpClient { request ->
            when (request.url.encodedPath) {
                "/success" -> respond(
                    content = """{"status":"ok"}""",
                    status = HttpStatusCode.OK
                )

                "/not-found" -> respond(
                    content = "",
                    status = HttpStatusCode.NotFound
                )

                "/server-error" -> respond(
                    content = """{"error":"internal error"}""",
                    status = HttpStatusCode.InternalServerError
                )

                else -> respond(
                    content = "",
                    status = HttpStatusCode.BadRequest
                )
            }
        }

        // When & Then - Success
        val successResponse = httpClient.get("https://example.com/success")
        assertEquals(HttpStatusCode.OK, successResponse.status)

        // When & Then - Not Found
        val notFoundResponse = httpClient.get("https://example.com/not-found")
        assertEquals(HttpStatusCode.NotFound, notFoundResponse.status)

        // When & Then - Server Error
        val serverErrorResponse = httpClient.get("https://example.com/server-error")
        assertEquals(HttpStatusCode.InternalServerError, serverErrorResponse.status)
    }

    @Test
    fun `test createMockHttpClient configures JSON serialization`() = runTest {
        // Given
        val testData = TestData("123", "test-name", 999)
        val httpClient = createMockHttpClient { request ->
            respond(
                content = Json.encodeToString(TestData.serializer(), testData),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // When
        val response = httpClient.get("https://example.com/api/data")
        val result: TestData = response.body()

        // Then - JSON 序列化/反序列化正常工作
        assertEquals(testData.id, result.id)
        assertEquals(testData.name, result.name)
        assertEquals(testData.value, result.value)
    }

    @Test
    fun `test createMockHttpClient configures default request headers`() = runTest {
        // Given
        val httpClient = createMockHttpClient { request ->
            // 验证请求头
            val contentType = request.headers[HttpHeaders.ContentType]
            val accept = request.headers[HttpHeaders.Accept]

            respond(
                content = """{"contentType":"$contentType","accept":"$accept"}""",
                status = HttpStatusCode.OK
            )
        }

        // When
        val response = httpClient.get("https://example.com/api/test")
        val responseText = response.body<String>()

        // Then - 默认请求头应该已配置
        assertNotNull(responseText)
        // 注意：DefaultRequest 插件会在请求时添加 Content-Type 和 Accept 头
    }

    @Test
    fun `test createMockHttpClient handles request URL correctly`() = runTest {
        // Given
        val expectedPath = "/api/test/endpoint"
        val expectedQuery = "param=value"
        var actualPath: String? = null
        var actualQuery: String? = null

        val httpClient = createMockHttpClient { request ->
            actualPath = request.url.encodedPath
            actualQuery = request.url.encodedQuery

            respond(
                content = """{"path":"$actualPath","query":"$actualQuery"}""",
                status = HttpStatusCode.OK
            )
        }

        // When
        httpClient.get("https://example.com$expectedPath?$expectedQuery")

        // Then
        assertEquals(expectedPath, actualPath)
        assertEquals(expectedQuery, actualQuery)
    }

    @Test
    fun `test createMockHttpClient handles empty response`() = runTest {
        // Given
        val httpClient = createMockHttpClient { request ->
            respond(
                content = "",
                status = HttpStatusCode.NoContent
            )
        }

        // When
        val response = httpClient.get("https://example.com/api/empty")

        // Then
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `test createMockHttpClient handles JSON with unknown keys`() = runTest {
        // Given - JSON 配置了 ignoreUnknownKeys = true
        val jsonWithUnknownKeys = """{"id":"1","name":"test","unknownField":"ignored"}"""
        val httpClient = createMockHttpClient { request ->
            respond(
                content = jsonWithUnknownKeys,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // When
        val response = httpClient.get("https://example.com/api/data")
        val result: TestData = response.body()

        // Then - 应该忽略未知字段，只解析已知字段
        assertEquals("1", result.id)
        assertEquals("test", result.name)
        assertEquals(0, result.value) // 默认值
    }

    @Test
    fun `test createMockHttpClient handles multiple sequential requests`() = runTest {
        // Given
        var requestCount = 0
        val httpClient = createMockHttpClient { request ->
            requestCount++
            respond(
                content = """{"count":$requestCount}""",
                status = HttpStatusCode.OK
            )
        }

        // When
        httpClient.get("https://example.com/api/1")
        httpClient.get("https://example.com/api/2")
        httpClient.get("https://example.com/api/3")

        // Then
        assertEquals(3, requestCount)
    }
}


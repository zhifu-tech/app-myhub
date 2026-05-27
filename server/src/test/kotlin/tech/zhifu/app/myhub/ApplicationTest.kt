package tech.zhifu.app.myhub

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Server 集成测试：验证 HTTP → 路由 的回归底线。
 * 覆盖健康检查、根路径、认证、未授权访问与错误响应。
 */
class ApplicationTest {

    @Test
    fun `GET health returns 200 and ok status`() = testApplication {
        application { module() }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("ok") || body.contains("\"status\""))
    }

    @Test
    fun `GET root returns 200 and API version text`() = testApplication {
        application { module() }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("MyHub") && body.contains("API"))
    }

    @Test
    fun `POST api auth login with empty userId returns 400`() = testApplication {
        application { module() }
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"userId":""}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("error") || body.contains("VALIDATION") || body.contains("User ID"))
    }

    @Test
    fun `POST api auth refresh without token returns 401 or 400`() = testApplication {
        application { module() }
        val response = client.post("/api/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody("""{"refreshToken":""}""")
        }
        assertTrue(
            response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.BadRequest,
            "Expected 401 or 400, got ${response.status}"
        )
    }

    @Test
    fun `GET unknown path returns 404`() = testApplication {
        application { module() }
        val response = client.get("/api/nonexistent-resource-404")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET api sync changes without required params returns 400`() = testApplication {
        application { module() }
        val response = client.get("/api/sync/changes")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}

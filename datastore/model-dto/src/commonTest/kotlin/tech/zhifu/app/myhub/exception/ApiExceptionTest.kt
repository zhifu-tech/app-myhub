package tech.zhifu.app.myhub.exception

import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * ApiException 及子类单元测试
 */
class ApiExceptionTest {

    @Test
    fun `ApiException with statusCode and message`() {
        val e = ApiException(HttpStatusCode.NotFound, "Not found")
        assertEquals(HttpStatusCode.NotFound, e.statusCode)
        assertEquals("Not found", e.message)
        assertNull(e.cause)
    }

    @Test
    fun `ApiException message-only constructor uses InternalServerError`() {
        val e = ApiException("Something failed")
        assertEquals(HttpStatusCode.InternalServerError, e.statusCode)
        assertEquals("Something failed", e.message)
    }

    @Test
    fun `NotFoundException extends ApiException`() {
        val e = NotFoundException("Card", "c1")
        assertEquals(HttpStatusCode.NotFound, e.statusCode)
        assertEquals("Card with id 'c1' not found", e.message)
    }
}

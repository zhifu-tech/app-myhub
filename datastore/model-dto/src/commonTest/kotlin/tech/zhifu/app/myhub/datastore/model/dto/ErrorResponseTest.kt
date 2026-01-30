package tech.zhifu.app.myhub.datastore.model.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * ErrorResponse DTO 单元测试
 */
class ErrorResponseTest {

    @Test
    fun `ErrorResponse equality`() {
        val detail = ErrorDetail(code = "ERR", message = "Msg")
        val a = ErrorResponse(error = detail)
        val b = ErrorResponse(error = detail.copy())
        val c = ErrorResponse(error = ErrorDetail(code = "X", message = "Y"))
        assertEquals(a, b)
        assertNotEquals(a, c)
    }
}

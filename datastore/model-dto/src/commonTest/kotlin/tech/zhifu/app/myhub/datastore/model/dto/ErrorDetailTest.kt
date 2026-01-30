package tech.zhifu.app.myhub.datastore.model.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 * ErrorDetail DTO 单元测试
 */
class ErrorDetailTest {

    @Test
    fun `ErrorDetail equality`() {
        val a = ErrorDetail(code = "ERR", message = "Message")
        val b = a.copy()
        val c = a.copy(code = "OTHER")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    @Test
    fun `ErrorDetail default optional fields`() {
        val detail = ErrorDetail(code = "C", message = "M")
        assertNull(detail.details)
        assertNull(detail.path)
    }

    @Test
    fun `ErrorDetail copy with details`() {
        val detail = ErrorDetail(
            code = "VALIDATION",
            message = "Invalid",
            details = mapOf("field" to "error")
        )
        assertEquals(mapOf("field" to "error"), detail.details)
    }
}

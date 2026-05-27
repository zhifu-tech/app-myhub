package tech.zhifu.app.myhub.datastore.model.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 * LoginRequest DTO 单元测试
 */
class LoginRequestTest {

    @Test
    fun `LoginRequest equality`() {
        val a = LoginRequest(userId = "u1")
        val b = a.copy()
        val c = LoginRequest(userId = "u2")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    @Test
    fun `LoginRequest default optional fields`() {
        val req = LoginRequest(userId = "u1")
        assertNull(req.username)
        assertNull(req.displayName)
        assertNull(req.avatarUrl)
        assertNull(req.avatarText)
    }

    @Test
    fun `LoginRequest copy with optional fields`() {
        val req = LoginRequest(
            userId = "u1",
            username = "user",
            displayName = "Display",
            avatarUrl = "https://example.com/avatar"
        )
        assertEquals("user", req.username)
        assertEquals("Display", req.displayName)
        assertEquals("https://example.com/avatar", req.avatarUrl)
    }
}

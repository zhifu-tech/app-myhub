package tech.zhifu.app.myhub.datastore.model.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * LoginResponse DTO 单元测试
 */
class LoginResponseTest {

    @Test
    fun `LoginResponse equality`() {
        val a = LoginResponse(
            accessToken = "at",
            refreshToken = "rt",
            expiresIn = 3600,
            tokenType = "Bearer",
            userId = "u1",
            username = "user"
        )
        val b = a.copy()
        val c = a.copy(userId = "u2")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    @Test
    fun `LoginResponse copy`() {
        val res = LoginResponse(
            accessToken = "at",
            refreshToken = "rt",
            expiresIn = 3600,
            tokenType = "Bearer",
            userId = "u1",
            username = "user"
        )
        val copied = res.copy(expiresIn = 7200, username = "other")
        assertEquals(7200, copied.expiresIn)
        assertEquals("other", copied.username)
        assertEquals("at", copied.accessToken)
    }
}

package tech.zhifu.app.myhub.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * NetworkException 和 ApiException 测试
 */
class NetworkExceptionTest {

    @Test
    fun `test ApiException creation with message`() {
        // Given
        val message = "API error occurred"

        // When
        val exception = ApiException(message)

        // Then
        assertEquals(message, exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `test ApiException creation with message and cause`() {
        // Given
        val message = "API error occurred"
        val cause = RuntimeException("Root cause")

        // When
        val exception = ApiException(message, cause)

        // Then
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `test NetworkException creation with message`() {
        // Given
        val message = "Network error occurred"

        // When
        val exception = NetworkException(message)

        // Then
        assertEquals(message, exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `test NetworkException creation with message and cause`() {
        // Given
        val message = "Network error occurred"
        val cause = RuntimeException("Connection timeout")

        // When
        val exception = NetworkException(message, cause)

        // Then
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `test ApiException is Exception`() {
        // Given
        val exception = ApiException("Test")

        // Then
        assertNotNull(exception as? Exception)
    }

    @Test
    fun `test NetworkException is Exception`() {
        // Given
        val exception = NetworkException("Test")

        // Then
        assertNotNull(exception as? Exception)
    }

    @Test
    fun `test ApiException can be thrown and caught`() {
        // Given
        val exception = ApiException("Test error")

        // When & Then
        try {
            throw exception
        } catch (e: ApiException) {
            assertEquals("Test error", e.message)
        }
    }

    @Test
    fun `test NetworkException can be thrown and caught`() {
        // Given
        val exception = NetworkException("Test error")

        // When & Then
        try {
            throw exception
        } catch (e: NetworkException) {
            assertEquals("Test error", e.message)
        }
    }
}


package tech.zhifu.app.myhub.network

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * ApiConfig 测试
 */
class ApiConfigTest {

    @AfterTest
    fun tearDown() {
        // 清理测试后的状态
        // 注意：由于 ApiConfig 使用 object，无法真正重置为 null
        // 这里设置一个测试值，实际测试中应该独立运行
    }

    @Test
    fun `test BASE_URL returns valid URL`() {
        // Given - 获取当前 BASE_URL（可能是默认值、系统属性或之前设置的值）
        val baseUrl = ApiConfig.BASE_URL

        // Then - 验证 URL 格式（应该是一个有效的 URL）
        assertTrue(baseUrl.isNotEmpty(), "BASE_URL should not be empty, got: '$baseUrl'")
        // URL 应该以 http:// 或 https:// 开头，或者至少包含一个冒号（表示有端口或协议）
        // 注意：系统属性可能包含任何值，所以只验证非空
        val isValidUrl = baseUrl.startsWith("http://") ||
            baseUrl.startsWith("https://") ||
            baseUrl.contains(":")
        assertTrue(
            isValidUrl,
            "BASE_URL should be a valid URL format (starts with http:// or https://, or contains ':'), got: '$baseUrl'"
        )
    }

    @Test
    fun `test setBaseUrl sets custom URL`() {
        // Given
        val customUrl = "https://api.example.com"

        // When
        ApiConfig.setBaseUrl(customUrl)
        val baseUrl = ApiConfig.BASE_URL

        // Then
        assertEquals(customUrl, baseUrl)
    }

    @Test
    fun `test setBaseUrl overrides previous value`() {
        // Given
        val firstUrl = "https://api.first.com"
        val secondUrl = "https://api.second.com"

        // When
        ApiConfig.setBaseUrl(firstUrl)
        val firstBaseUrl = ApiConfig.BASE_URL
        ApiConfig.setBaseUrl(secondUrl)
        val secondBaseUrl = ApiConfig.BASE_URL

        // Then
        assertEquals(firstUrl, firstBaseUrl)
        assertEquals(secondUrl, secondBaseUrl)
    }

    @Test
    fun `test API path constants`() {
        // Then
        assertEquals("/api/cards", ApiConfig.CARDS_PATH)
        assertEquals("/api/tags", ApiConfig.TAGS_PATH)
        assertEquals("/api/templates", ApiConfig.TEMPLATES_PATH)
        assertEquals("/api/users", ApiConfig.USERS_PATH)
        assertEquals("/api/statistics", ApiConfig.STATISTICS_PATH)
    }

    @Test
    fun `test timeout constants`() {
        // Then
        assertEquals(30_000L, ApiConfig.CONNECT_TIMEOUT)
        assertEquals(30_000L, ApiConfig.SOCKET_TIMEOUT)
    }

    @Test
    fun `test setBaseUrl with empty string`() {
        // Given
        val customUrl = "https://api.example.com"
        ApiConfig.setBaseUrl(customUrl)
        assertEquals(customUrl, ApiConfig.BASE_URL)

        // When
        ApiConfig.setBaseUrl("")

        // Then - 空字符串会被设置为值（不会重置为 null）
        val baseUrl = ApiConfig.BASE_URL
        assertEquals("", baseUrl, "Empty string should be returned as-is")
    }

    @Test
    fun `test setBaseUrl with trailing slash`() {
        // Given
        val urlWithSlash = "https://api.example.com/"
        val urlWithoutSlash = "https://api.example.com"

        // When
        ApiConfig.setBaseUrl(urlWithSlash)
        val baseUrlWithSlash = ApiConfig.BASE_URL
        ApiConfig.setBaseUrl(urlWithoutSlash)
        val baseUrlWithoutSlash = ApiConfig.BASE_URL

        // Then - 应该保持原始值
        assertEquals(urlWithSlash, baseUrlWithSlash)
        assertEquals(urlWithoutSlash, baseUrlWithoutSlash)
    }
}


package tech.zhifu.app.myhub.logger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * LoggerConfig 单元测试
 */
class LoggerConfigTest {

    @Test
    fun `test LoggerConfig with default values`() {
        // When
        val config = LoggerConfig(appName = "TestApp")

        // Then
        assertEquals("TestApp", config.appName)
        assertFalse(config.useAndroidLogger) // 默认值为 false
    }

    @Test
    fun `test LoggerConfig with custom values`() {
        // When
        val config = LoggerConfig(
            appName = "MyApp",
            useAndroidLogger = false
        )

        // Then
        assertEquals("MyApp", config.appName)
        assertFalse(config.useAndroidLogger)
    }

    @Test
    fun `test LoggerConfig equality`() {
        // Given
        val config1 = LoggerConfig(appName = "TestApp", useAndroidLogger = true)
        val config2 = LoggerConfig(appName = "TestApp", useAndroidLogger = true)
        val config3 = LoggerConfig(appName = "TestApp", useAndroidLogger = false)

        // Then
        assertEquals(config1, config2)
        assertNotEquals(config1, config3)
    }

    @Test
    fun `test LoggerConfig copy`() {
        // Given
        val original = LoggerConfig(appName = "OriginalApp", useAndroidLogger = true)

        // When
        val copied = original.copy(appName = "CopiedApp", useAndroidLogger = false)

        // Then
        assertEquals("OriginalApp", original.appName)
        assertTrue(original.useAndroidLogger)
        assertEquals("CopiedApp", copied.appName)
        assertFalse(copied.useAndroidLogger)
    }
}

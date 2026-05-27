package tech.zhifu.app.myhub

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Platform 单元测试
 * 测试各平台的 getPlatform() 实现
 */
class PlatformTest {

    @Test
    fun `test getPlatform returns non-null platform`() {
        // When
        val platform = getPlatform()

        // Then
        assertNotNull(platform)
    }

    @Test
    fun `test platform has name property`() {
        // When
        val platform = getPlatform()

        // Then
        assertNotNull(platform.name)
        assertTrue(platform.name.isNotBlank())
    }

    @Test
    fun `test platform name is not empty`() {
        // When
        val platform = getPlatform()

        // Then
        assertTrue(platform.name.isNotEmpty(), "Platform name should not be empty")
    }
}


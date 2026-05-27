package tech.zhifu.app.myhub.cache

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * CacheConfig 单元测试
 */
class CacheConfigTest {

    @Test
    fun `test CacheConfig with default values`() {
        val config = CacheConfig()

        assertEquals(1_000L, config.maximumSize)
        assertEquals(null, config.expireAfterWrite)
        assertEquals(null, config.expireAfterAccess)
    }

    @Test
    fun `test CacheConfig with custom values`() {
        val config = CacheConfig(
            maximumSize = 500,
            expireAfterWrite = 5.minutes,
            expireAfterAccess = 1.minutes
        )

        assertEquals(500L, config.maximumSize)
        assertEquals(5.minutes, config.expireAfterWrite)
        assertEquals(1.minutes, config.expireAfterAccess)
    }

    @Test
    fun `test CacheConfig equality`() {
        val config1 = CacheConfig(maximumSize = 100, expireAfterWrite = 10.seconds)
        val config2 = CacheConfig(maximumSize = 100, expireAfterWrite = 10.seconds)
        val config3 = CacheConfig(maximumSize = 200)

        assertEquals(config1, config2)
        assertNotEquals(config1, config3)
    }

    @Test
    fun `test CacheConfig copy`() {
        val original = CacheConfig(
            maximumSize = 100,
            expireAfterWrite = 5.seconds,
            expireAfterAccess = 1.seconds
        )
        val copied = original.copy(maximumSize = 200, expireAfterWrite = null)

        assertEquals(100L, original.maximumSize)
        assertEquals(5.seconds, original.expireAfterWrite)
        assertEquals(200L, copied.maximumSize)
        assertEquals(null, copied.expireAfterWrite)
        assertEquals(1.seconds, copied.expireAfterAccess)
    }

    @Test
    fun `test CacheConfig requires positive maximumSize`() {
        assertFailsWith<IllegalArgumentException> {
            CacheConfig(maximumSize = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            CacheConfig(maximumSize = -1)
        }
    }

    @Test
    fun `test CacheConfig accepts maximumSize one`() {
        val config = CacheConfig(maximumSize = 1)
        assertEquals(1L, config.maximumSize)
    }
}

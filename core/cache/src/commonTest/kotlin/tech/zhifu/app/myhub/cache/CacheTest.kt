package tech.zhifu.app.myhub.cache

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Cache 行为单元测试（通过 cache() 创建的默认实现）
 */
class CacheTest {

    @Test
    fun `get returns null when key not present`() {
        val cache = cache<String, String>()
        assertNull(cache.get("missing"))
    }

    @Test
    fun `put and get returns stored value`() {
        val cache = cache<String, String>()
        cache.put("key", "value")
        assertEquals("value", cache.get("key"))
    }

    @Test
    fun `getOrPut loads and caches value`() = runTest {
        val cache = cache<String, String>()
        var loadCount = 0
        val result = cache.getOrPut("key") {
            loadCount++
            "loaded"
        }
        assertEquals("loaded", result)
        assertEquals(1, loadCount)
        assertEquals("loaded", cache.get("key"))
    }

    @Test
    fun `getOrPut returns cached value on second call`() = runTest {
        val cache = cache<String, Int>()
        var loadCount = 0
        cache.getOrPut("key") { loadCount++; 42 }
        val second = cache.getOrPut("key") { loadCount++; 99 }
        assertEquals(42, second)
        assertEquals(1, loadCount)
    }

    @Test
    fun `invalidate removes entry`() {
        val cache = cache<String, String>()
        cache.put("key", "value")
        assertEquals("value", cache.get("key"))
        cache.invalidate("key")
        assertNull(cache.get("key"))
    }

    @Test
    fun `invalidateAll clears cache`() {
        val cache = cache<String, String>()
        cache.put("a", "1")
        cache.put("b", "2")
        cache.invalidateAll()
        assertNull(cache.get("a"))
        assertNull(cache.get("b"))
    }

    @Test
    fun `cache with custom config respects maximumSize`() = runTest {
        val cache = cache<String, Int>(CacheConfig(maximumSize = 2))
        cache.put("a", 1)
        cache.put("b", 2)
        cache.put("c", 3)
        // Cache4k evicts by LRU; with size 2, "a" should be evicted
        assertNull(cache.get("a"))
        assertEquals(2, cache.get("b"))
        assertEquals(3, cache.get("c"))
    }

    @Test
    fun `different key types work`() {
        val stringCache = cache<String, Int>()
        stringCache.put("one", 1)
        assertEquals(1, stringCache.get("one"))

        val intCache = cache<Int, String>()
        intCache.put(1, "one")
        assertEquals("one", intCache.get(1))
    }
}

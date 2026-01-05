package tech.zhifu.app.myhub.analytics

import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BaseAnalyticsProviderTest {

    @Test
    fun `events are buffered before initialization`() = runTest {
        val provider = MockProvider()
        
        // 在初始化前记录事件
        val event1 = AnalyticsEvent(name = "event1")
        val event2 = AnalyticsEvent(name = "event2")
        
        provider.logEvent(event1)
        provider.logEvent(event2)
        
        // 等待异步缓冲操作完成
        // 由于 providerScope 使用 Dispatchers.Default（真实调度器），
        // 在 iOS 平台上需要足够的延迟来确保协程执行
        // 使用循环等待，最多等待 1 秒
        var waited = 0L
        val maxWait = 1000L
        while (waited < maxWait) {
            delay(50)
            waited += 50
            // 如果事件已经被上报（不应该发生），或者初始化已完成，退出循环
            if (provider.loggedEvents.size > 0 || provider.isInitialized) {
                break
            }
        }
        
        // 此时事件应该被缓冲，还未上报
        assertEquals(0, provider.loggedEvents.size, "Events should be buffered, not logged yet")
        assertFalse(provider.isInitialized, "Provider should not be initialized yet")
        
        // 初始化
        provider.initialize(ProviderConfig(type = ProviderType.CONSOLE))
        
        // flushBufferedEvents 是同步的，但为了确保，再等待一下
        advanceUntilIdle()
        // 额外延迟确保所有操作完成（iOS 平台需要）
        delay(100)
        
        // 初始化后，缓冲的事件应该被上报
        assertTrue(provider.isInitialized, "Provider should be initialized")
        assertTrue(
            provider.loggedEvents.size >= 2,
            "Expected at least 2 events after initialization, got ${provider.loggedEvents.size}"
        )
        // 验证事件名称（可能顺序不同，所以检查包含）
        val eventNames = provider.loggedEvents.map { it.name }.toSet()
        assertTrue(eventNames.contains("event1"), "Should contain event1")
        assertTrue(eventNames.contains("event2"), "Should contain event2")
    }

    @Test
    fun `events are logged immediately after initialization`() = runTest {
        val provider = MockProvider()
        
        // 先初始化
        provider.initialize(ProviderConfig(type = ProviderType.CONSOLE))
        
        // 等待初始化完成
        advanceUntilIdle()
        
        // 初始化后记录事件
        val event = AnalyticsEvent(name = "event1")
        provider.logEvent(event)
        
        // 事件应该立即上报（因为已初始化，不会走缓冲逻辑）
        assertEquals(1, provider.loggedEvents.size)
        assertEquals("event1", provider.loggedEvents[0].name)
    }

    @Test
    fun `isReady state flow updates correctly`() = runTest {
        val provider = MockProvider()
        
        // 初始状态应该是 false
        assertFalse(provider.isReady.value)
        
        // 初始化
        provider.initialize(ProviderConfig(type = ProviderType.CONSOLE))
        
        // 初始化后应该是 true
        assertTrue(provider.isReady.value)
    }
}

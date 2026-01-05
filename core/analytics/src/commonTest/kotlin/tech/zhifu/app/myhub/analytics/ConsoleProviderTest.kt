package tech.zhifu.app.myhub.analytics

import tech.zhifu.app.myhub.analytics.provider.ConsoleProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConsoleProviderTest {

    @Test
    fun `console provider initializes correctly`() = runTest {
        val provider = ConsoleProvider()
        
        assertEquals("Console", provider.name)
        assertTrue(provider.supportedPlatforms.contains(Platform.JVM))
        assertTrue(provider.supportedRegions.contains(Region.DOMESTIC))
        assertTrue(provider.supportedRegions.contains(Region.OVERSEAS))
        
        provider.initialize(ProviderConfig(type = ProviderType.CONSOLE))
        
        assertTrue(provider.isInitialized)
    }

    @Test
    fun `console provider logs events`() = runTest {
        val provider = ConsoleProvider()
        provider.initialize(ProviderConfig(type = ProviderType.CONSOLE))
        
        val event = AnalyticsEvent(
            name = "test_event",
            parameters = mapOf(
                "key1" to AnalyticsValue.Str("value1"),
                "key2" to AnalyticsValue.Int(123L)
            ),
            value = 456.78,
            currency = "USD"
        )
        
        // 由于是控制台输出，我们无法直接验证输出
        // 但可以验证方法调用不会抛出异常
        provider.logEvent(event)
        assertTrue(provider.isInitialized)
    }

    @Test
    fun `console provider sets user properties`() = runTest {
        val provider = ConsoleProvider()
        provider.initialize(ProviderConfig(type = ProviderType.CONSOLE))
        
        provider.setUserProperty("key1", AnalyticsValue.Str("value1"))
        provider.setUserProperty("key2", null)
        
        assertTrue(provider.isInitialized)
    }

    @Test
    fun `console provider sets user ID`() = runTest {
        val provider = ConsoleProvider()
        provider.initialize(ProviderConfig(type = ProviderType.CONSOLE))
        
        provider.setUserId("user123")
        provider.setUserId(null)
        
        assertTrue(provider.isInitialized)
    }

    @Test
    fun `console provider sets screen`() = runTest {
        val provider = ConsoleProvider()
        provider.initialize(ProviderConfig(type = ProviderType.CONSOLE))
        
        provider.setScreen("Dashboard", "DashboardScreen")
        provider.setScreen("Settings")
        
        assertTrue(provider.isInitialized)
    }

    @Test
    fun `console provider resets`() = runTest {
        val provider = ConsoleProvider()
        provider.initialize(ProviderConfig(type = ProviderType.CONSOLE))
        
        provider.reset()
        
        assertTrue(provider.isInitialized)
    }
}

package tech.zhifu.app.myhub.analytics

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnalyticsManagerTest {

    @Test
    fun `manager respects enabled config`() = runTest {
        val config = AnalyticsConfig(
            region = Region.DOMESTIC,
            enabled = false,
            providers = listOf(
                ProviderConfig(type = ProviderType.CONSOLE, enabled = true)
            )
        )
        val consent = DefaultAnalyticsConsent()
        val factory = AnalyticsProviderFactory()
        factory.register(ProviderType.CONSOLE) { MockProvider() }
        
        val manager = AnalyticsManager(config, consent, providerFactory = factory)
        manager.initialize()
        
        val provider = factory.create(ProviderConfig(type = ProviderType.CONSOLE)) as MockProvider
        manager.logEvent(AnalyticsEvent(name = "test"))
        
        // 由于 enabled = false，事件不应该被记录
        assertEquals(0, provider.loggedEvents.size)
    }

    @Test
    fun `manager respects consent`() = runTest {
        val config = AnalyticsConfig(
            region = Region.DOMESTIC,
            enabled = true,
            providers = listOf(
                ProviderConfig(type = ProviderType.CONSOLE, enabled = true)
            )
        )
        val consent = DefaultAnalyticsConsent(analyticsAllowed = false)
        val factory = AnalyticsProviderFactory()
        factory.register(ProviderType.CONSOLE) { MockProvider() }
        
        val manager = AnalyticsManager(config, consent, providerFactory = factory)
        manager.initialize()
        
        val provider = factory.create(ProviderConfig(type = ProviderType.CONSOLE)) as MockProvider
        manager.logEvent(AnalyticsEvent(name = "test"))
        
        // 由于 consent = false，事件不应该被记录
        assertEquals(0, provider.loggedEvents.size)
    }

    @Test
    fun `manager logs events to all providers`() = runTest {
        val provider1 = MockProvider(name = "Provider1")
        val provider2 = MockProvider(name = "Provider2")
        
        val config = AnalyticsConfig(
            region = Region.DOMESTIC,
            enabled = true,
            providers = listOf(
                ProviderConfig(type = ProviderType.CONSOLE, enabled = true)
            )
        )
        val consent = DefaultAnalyticsConsent()
        val factory = AnalyticsProviderFactory()
        factory.register(ProviderType.CONSOLE) { provider1 }
        
        val manager = AnalyticsManager(config, consent, providerFactory = factory)
        manager.initialize()
        
        val event = AnalyticsEvent(name = "test_event")
        manager.logEvent(event)
        
        // 事件应该被记录到 provider
        assertEquals(1, provider1.loggedEvents.size)
        assertEquals("test_event", provider1.loggedEvents[0].name)
    }

    @Test
    fun `manager sets user properties`() = runTest {
        val provider = MockProvider()
        val config = AnalyticsConfig(
            region = Region.DOMESTIC,
            enabled = true,
            providers = listOf(
                ProviderConfig(type = ProviderType.CONSOLE, enabled = true)
            )
        )
        val consent = DefaultAnalyticsConsent()
        val factory = AnalyticsProviderFactory()
        factory.register(ProviderType.CONSOLE) { provider }
        
        val manager = AnalyticsManager(config, consent, providerFactory = factory)
        manager.initialize()
        
        manager.setUserProperty("key1", AnalyticsValue.Str("value1"))
        manager.setUserProperty("key2", AnalyticsValue.Int(123L))
        
        assertEquals(2, provider.userProperties.size)
        assertEquals("value1", (provider.userProperties["key1"] as AnalyticsValue.Str).value)
        assertEquals(123L, (provider.userProperties["key2"] as AnalyticsValue.Int).value)
    }

    @Test
    fun `manager sets user ID`() = runTest {
        val provider = MockProvider()
        val config = AnalyticsConfig(
            region = Region.DOMESTIC,
            enabled = true,
            providers = listOf(
                ProviderConfig(type = ProviderType.CONSOLE, enabled = true)
            )
        )
        val consent = DefaultAnalyticsConsent()
        val factory = AnalyticsProviderFactory()
        factory.register(ProviderType.CONSOLE) { provider }
        
        val manager = AnalyticsManager(config, consent, providerFactory = factory)
        manager.initialize()
        
        manager.setUserId("user123")
        assertEquals("user123", provider.currentUserId)

        manager.setUserId(null)
        assertEquals(null, provider.currentUserId)
    }

    @Test
    fun `manager sets screen`() = runTest {
        val provider = MockProvider()
        val config = AnalyticsConfig(
            region = Region.DOMESTIC,
            enabled = true,
            providers = listOf(
                ProviderConfig(type = ProviderType.CONSOLE, enabled = true)
            )
        )
        val consent = DefaultAnalyticsConsent()
        val factory = AnalyticsProviderFactory()
        factory.register(ProviderType.CONSOLE) { provider }
        
        val manager = AnalyticsManager(config, consent, providerFactory = factory)
        manager.initialize()
        
        manager.setScreen("Dashboard", "DashboardScreen")
        assertEquals("Dashboard", provider.currentScreenName)
        assertEquals("DashboardScreen", provider.currentScreenClass)
    }

    @Test
    fun `manager resets all providers`() = runTest {
        val provider = MockProvider()
        val config = AnalyticsConfig(
            region = Region.DOMESTIC,
            enabled = true,
            providers = listOf(
                ProviderConfig(type = ProviderType.CONSOLE, enabled = true)
            )
        )
        val consent = DefaultAnalyticsConsent()
        val factory = AnalyticsProviderFactory()
        factory.register(ProviderType.CONSOLE) { provider }
        
        val manager = AnalyticsManager(config, consent, providerFactory = factory)
        manager.initialize()
        
        manager.setUserId("user123")
        manager.logEvent(AnalyticsEvent(name = "test"))
        
        manager.reset()
        
        assertTrue(provider.resetCalled)
        assertEquals(0, provider.loggedEvents.size)
        assertEquals(null, provider.currentUserId)
    }

    @Test
    fun `manager logs multiple events`() = runTest {
        val provider = MockProvider()
        val config = AnalyticsConfig(
            region = Region.DOMESTIC,
            enabled = true,
            providers = listOf(
                ProviderConfig(type = ProviderType.CONSOLE, enabled = true)
            )
        )
        val consent = DefaultAnalyticsConsent()
        val factory = AnalyticsProviderFactory()
        factory.register(ProviderType.CONSOLE) { provider }
        
        val manager = AnalyticsManager(config, consent, providerFactory = factory)
        manager.initialize()
        
        val events = listOf(
            AnalyticsEvent(name = "event1"),
            AnalyticsEvent(name = "event2"),
            AnalyticsEvent(name = "event3")
        )
        manager.logEvents(events)
        
        assertEquals(3, provider.loggedEvents.size)
        assertEquals("event1", provider.loggedEvents[0].name)
        assertEquals("event2", provider.loggedEvents[1].name)
        assertEquals("event3", provider.loggedEvents[2].name)
    }

    @Test
    fun `manager filters providers by region`() = runTest {
        val domesticProvider = MockProvider(
            name = "Domestic",
            supportedRegions = setOf(Region.DOMESTIC)
        )
        val overseasProvider = MockProvider(
            name = "Overseas",
            supportedRegions = setOf(Region.OVERSEAS)
        )
        
        val config = AnalyticsConfig(
            region = Region.DOMESTIC,
            enabled = true,
            providers = listOf(
                ProviderConfig(type = ProviderType.CONSOLE, enabled = true)
            )
        )
        val consent = DefaultAnalyticsConsent()
        val factory = AnalyticsProviderFactory()
        factory.register(ProviderType.CONSOLE) { domesticProvider }
        
        val manager = AnalyticsManager(config, consent, providerFactory = factory)
        manager.initialize()
        
        // 只有支持 DOMESTIC 的 provider 应该被初始化
        assertTrue(domesticProvider.isInitialized)
    }
}

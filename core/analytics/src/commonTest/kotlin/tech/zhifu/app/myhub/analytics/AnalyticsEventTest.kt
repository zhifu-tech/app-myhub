package tech.zhifu.app.myhub.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AnalyticsEventTest {

    @Test
    fun `screenView creates correct event`() {
        val event = AnalyticsEvent.screenView("Dashboard", "DashboardScreen")
        
        assertEquals(AnalyticsEvents.SCREEN_VIEW, event.name)
        assertEquals("Dashboard", (event.parameters["screen_name"] as AnalyticsValue.Str).value)
        assertEquals("DashboardScreen", (event.parameters["screen_class"] as AnalyticsValue.Str).value)
    }

    @Test
    fun `screenView without screenClass`() {
        val event = AnalyticsEvent.screenView("Dashboard")
        
        assertEquals(AnalyticsEvents.SCREEN_VIEW, event.name)
        assertEquals("Dashboard", (event.parameters["screen_name"] as AnalyticsValue.Str).value)
        assertEquals(null, event.parameters["screen_class"])
    }

    @Test
    fun `userLogin creates correct event`() {
        val event = AnalyticsEvent.userLogin("email")
        
        assertEquals(AnalyticsEvents.USER_LOGIN, event.name)
        assertEquals("email", (event.parameters["method"] as AnalyticsValue.Str).value)
    }

    @Test
    fun `purchase creates correct event`() {
        val items = listOf(
            PurchaseItem("item1", "Product 1", "category1", 2, 99.99),
            PurchaseItem("item2", "Product 2", "category2", 1, 49.99)
        )
        val event = AnalyticsEvent.purchase(249.97, "USD", items)
        
        assertEquals(AnalyticsEvents.PURCHASE, event.name)
        assertEquals(249.97, event.value)
        assertEquals("USD", event.currency)
        assertEquals(2L, (event.parameters["items_count"] as AnalyticsValue.Int).value)
    }

    @Test
    fun `custom event creation`() {
        val event = AnalyticsEvent(
            name = "custom_event",
            parameters = mapOf(
                "key1" to AnalyticsValue.Str("value1"),
                "key2" to AnalyticsValue.Int(123L),
                "key3" to AnalyticsValue.Bool(true)
            ),
            value = 456.78,
            currency = "CNY"
        )
        
        assertEquals("custom_event", event.name)
        assertEquals(3, event.parameters.size)
        assertEquals(456.78, event.value)
        assertEquals("CNY", event.currency)
    }
}

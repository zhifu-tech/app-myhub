package tech.zhifu.app.myhub.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnalyticsValueTest {

    @Test
    fun `from String returns Str`() {
        val value = AnalyticsValue.from("test")
        assertNotNull(value)
        assertEquals("test", (value as AnalyticsValue.Str).value)
    }

    @Test
    fun `from Double returns Num`() {
        val value = AnalyticsValue.from(123.45)
        assertNotNull(value)
        assertEquals(123.45, (value as AnalyticsValue.Num).value)
    }

    @Test
    fun `from Float returns Num`() {
        val value = AnalyticsValue.from(123.45f)
        assertNotNull(value)
        assertEquals(123.45, (value as AnalyticsValue.Num).value, 0.01)
    }

    @Test
    fun `from Long returns Int`() {
        val value = AnalyticsValue.from(123L)
        assertNotNull(value)
        assertEquals(123L, (value as AnalyticsValue.Int).value)
    }

    @Test
    fun `from Int returns Int`() {
        val value = AnalyticsValue.from(123)
        assertNotNull(value)
        // 在 JS 平台上，整数可能返回 Int 或 Num，都接受
        when (value) {
            is AnalyticsValue.Int -> assertEquals(123L, value.value)
            is AnalyticsValue.Num -> assertEquals(123.0, value.value)
            else -> throw AssertionError("Expected Int or Num, got ${value::class.simpleName}")
        }
    }

    @Test
    fun `from Short returns Int`() {
        val value = AnalyticsValue.from(123.toShort())
        assertNotNull(value)
        // 在 JS 平台上，整数可能返回 Int 或 Num，都接受
        when (value) {
            is AnalyticsValue.Int -> assertEquals(123L, value.value)
            is AnalyticsValue.Num -> assertEquals(123.0, value.value)
            else -> throw AssertionError("Expected Int or Num, got ${value::class.simpleName}")
        }
    }

    @Test
    fun `from Byte returns Int`() {
        val value = AnalyticsValue.from(123.toByte())
        assertNotNull(value)
        // 在 JS 平台上，整数可能返回 Int 或 Num，都接受
        when (value) {
            is AnalyticsValue.Int -> assertEquals(123L, value.value)
            is AnalyticsValue.Num -> assertEquals(123.0, value.value)
            else -> throw AssertionError("Expected Int or Num, got ${value::class.simpleName}")
        }
    }

    @Test
    fun `from Boolean returns Bool`() {
        val value = AnalyticsValue.from(true)
        assertNotNull(value)
        assertEquals(true, (value as AnalyticsValue.Bool).value)
    }

    @Test
    fun `from null returns null`() {
        val value = AnalyticsValue.from(null)
        assertNull(value)
    }

    @Test
    fun `from unknown type converts to Str`() {
        val value = AnalyticsValue.from(listOf(1, 2, 3))
        assertNotNull(value)
        assertEquals("[1, 2, 3]", (value as AnalyticsValue.Str).value)
    }
}

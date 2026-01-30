package tech.zhifu.app.myhub.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * SyncMode, SyncTrigger, SyncScheduleConfig, SyncRequest 单元测试
 */
class SyncModelsTest {

    @Test
    fun `SyncMode enum`() {
        assertEquals(2, SyncMode.entries.size)
        assertEquals(SyncMode.ENABLED, SyncMode.ENABLED)
        assertEquals(SyncMode.DISABLED, SyncMode.DISABLED)
    }

    @Test
    fun `SyncTrigger enum`() {
        assertEquals(5, SyncTrigger.entries.size)
        assertEquals(SyncTrigger.APP_START, SyncTrigger.APP_START)
        assertEquals(SyncTrigger.MANUAL, SyncTrigger.MANUAL)
    }

    @Test
    fun `SyncScheduleConfig equality`() {
        val a = SyncScheduleConfig(interval = 5.minutes, mode = SyncMode.ENABLED)
        val b = SyncScheduleConfig(interval = 5.minutes, mode = SyncMode.ENABLED)
        val c = SyncScheduleConfig(interval = 10.seconds, mode = SyncMode.DISABLED)
        assertEquals(a, b)
        assertNotEquals(a, c)
        assertEquals(5.minutes, a.interval)
        assertEquals(SyncMode.ENABLED, a.mode)
    }

    @Test
    fun `SyncRequest equality`() {
        val a = SyncRequest(userId = "u1", trigger = SyncTrigger.APP_START)
        val b = a.copy()
        val c = SyncRequest(userId = "u2", trigger = SyncTrigger.MANUAL)
        assertEquals(a, b)
        assertNotEquals(a, c)
        assertEquals("u1", a.userId)
        assertEquals(SyncTrigger.APP_START, a.trigger)
    }

    @Test
    fun `SyncRequestFactory typealias`() {
        val factory: SyncRequestFactory = { trigger ->
            SyncRequest(userId = "u1", trigger = trigger)
        }
        val req = factory(SyncTrigger.FOREGROUND)
        assertEquals("u1", req.userId)
        assertEquals(SyncTrigger.FOREGROUND, req.trigger)
    }
}

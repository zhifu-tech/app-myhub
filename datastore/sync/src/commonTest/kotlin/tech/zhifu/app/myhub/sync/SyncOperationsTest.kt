package tech.zhifu.app.myhub.sync

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * SyncOperations 单元测试
 */
class SyncOperationsTest {

    @Test
    fun `SyncOperations values`() {
        assertEquals("INSERT", SyncOperations.Insert.value)
        assertEquals("DELETE", SyncOperations.Delete.value)
    }

    @Test
    fun `SyncOperations enum entries`() {
        assertEquals(2, SyncOperations.entries.size)
    }
}

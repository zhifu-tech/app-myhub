package tech.zhifu.app.myhub.sync

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * SyncEntityType 单元测试
 */
class SyncEntityTypeTest {

    @Test
    fun `SyncEntityType values`() {
        assertEquals("user", SyncEntityType.User.value)
        assertEquals("user_preferences", SyncEntityType.UserPreferences.value)
        assertEquals("collection", SyncEntityType.Collection.value)
        assertEquals("card", SyncEntityType.Card.value)
        assertEquals("tag", SyncEntityType.Tag.value)
        assertEquals("template", SyncEntityType.Template.value)
    }

    @Test
    fun `SyncEntityType enum entries`() {
        val values = SyncEntityType.entries
        assertEquals(6, values.size)
    }
}

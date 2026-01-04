package tech.zhifu.app.myhub.settings.data.store

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * 设置值序列化器测试
 */
class SettingSerializerTest {

    @Test
    fun `test BooleanSettingSerializer serialize`() {
        val serializer = BooleanSettingSerializer()
        assertEquals("true", serializer.serialize(true))
        assertEquals("false", serializer.serialize(false))
    }

    @Test
    fun `test BooleanSettingSerializer deserialize`() {
        val serializer = BooleanSettingSerializer()
        assertEquals(true, serializer.deserialize("true"))
        assertEquals(false, serializer.deserialize("false"))
    }

    @Test
    fun `test StringSettingSerializer serialize`() {
        val serializer = StringSettingSerializer()
        assertEquals("test", serializer.serialize("test"))
        assertEquals("", serializer.serialize(""))
        assertEquals("123", serializer.serialize("123"))
    }

    @Test
    fun `test StringSettingSerializer deserialize`() {
        val serializer = StringSettingSerializer()
        assertEquals("test", serializer.deserialize("test"))
        assertEquals("", serializer.deserialize(""))
        assertEquals("123", serializer.deserialize("123"))
    }

    @Test
    fun `test IntSettingSerializer serialize`() {
        val serializer = IntSettingSerializer()
        assertEquals("0", serializer.serialize(0))
        assertEquals("123", serializer.serialize(123))
        assertEquals("-456", serializer.serialize(-456))
    }

    @Test
    fun `test IntSettingSerializer deserialize`() {
        val serializer = IntSettingSerializer()
        assertEquals(0, serializer.deserialize("0"))
        assertEquals(123, serializer.deserialize("123"))
        assertEquals(-456, serializer.deserialize("-456"))
    }

    @Test
    fun `test IntSettingSerializer deserialize invalid value`() {
        val serializer = IntSettingSerializer()
        assertFailsWith<NumberFormatException> {
            serializer.deserialize("invalid")
        }
    }

    @Test
    fun `test LongSettingSerializer serialize`() {
        val serializer = LongSettingSerializer()
        assertEquals("0", serializer.serialize(0L))
        assertEquals("123456789", serializer.serialize(123456789L))
        assertEquals("-987654321", serializer.serialize(-987654321L))
    }

    @Test
    fun `test LongSettingSerializer deserialize`() {
        val serializer = LongSettingSerializer()
        assertEquals(0L, serializer.deserialize("0"))
        assertEquals(123456789L, serializer.deserialize("123456789"))
        assertEquals(-987654321L, serializer.deserialize("-987654321"))
    }

    @Test
    fun `test LongSettingSerializer deserialize invalid value`() {
        val serializer = LongSettingSerializer()
        assertFailsWith<NumberFormatException> {
            serializer.deserialize("invalid")
        }
    }

    @Test
    fun `test serializer round trip`() {
        // Boolean
        val boolSerializer = BooleanSettingSerializer()
        assertEquals(true, boolSerializer.deserialize(boolSerializer.serialize(true)))
        assertEquals(false, boolSerializer.deserialize(boolSerializer.serialize(false)))

        // String
        val stringSerializer = StringSettingSerializer()
        assertEquals("test", stringSerializer.deserialize(stringSerializer.serialize("test")))

        // Int
        val intSerializer = IntSettingSerializer()
        assertEquals(123, intSerializer.deserialize(intSerializer.serialize(123)))

        // Long
        val longSerializer = LongSettingSerializer()
        assertEquals(123456789L, longSerializer.deserialize(longSerializer.serialize(123456789L)))
    }
}



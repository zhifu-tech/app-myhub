package tech.zhifu.app.myhub.settings.data.store

/**
 * 设置值序列化器接口
 * 负责将设置值序列化为字符串存储，以及从字符串反序列化
 */
interface SettingSerializer<T> {
    /**
     * 序列化为字符串
     */
    fun serialize(value: T): String
    
    /**
     * 从字符串反序列化
     */
    fun deserialize(value: String): T
}

/**
 * Boolean 类型序列化器
 */
class BooleanSettingSerializer : SettingSerializer<Boolean> {
    override fun serialize(value: Boolean): String = value.toString()
    override fun deserialize(value: String): Boolean = value.toBoolean()
}

/**
 * String 类型序列化器
 */
class StringSettingSerializer : SettingSerializer<String> {
    override fun serialize(value: String): String = value
    override fun deserialize(value: String): String = value
}

/**
 * Int 类型序列化器
 */
class IntSettingSerializer : SettingSerializer<Int> {
    override fun serialize(value: Int): String = value.toString()
    override fun deserialize(value: String): Int = value.toInt()
}

/**
 * Long 类型序列化器
 */
class LongSettingSerializer : SettingSerializer<Long> {
    override fun serialize(value: Long): String = value.toString()
    override fun deserialize(value: String): Long = value.toLong()
}



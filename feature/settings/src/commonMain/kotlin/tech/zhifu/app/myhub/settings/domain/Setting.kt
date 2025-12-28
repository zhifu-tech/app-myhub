package tech.zhifu.app.myhub.settings.domain

import kotlinx.coroutines.flow.Flow

/**
 * 设置项接口（泛型，类型安全）
 * 
 * @param T 设置值的类型
 */
interface Setting<T> {
    /**
     * 设置项的唯一标识键
     */
    val key: String
    
    /**
     * 设置项的作用域
     */
    val scope: SettingScope
    
    /**
     * 默认值
     */
    val defaultValue: T
    
    /**
     * 获取当前值（响应式）
     * 当设置值发生变化时，Flow 会自动发出新值
     */
    fun observe(): Flow<T>
    
    /**
     * 获取当前值（同步）
     */
    suspend fun get(): T
    
    /**
     * 设置值
     */
    suspend fun set(value: T)
    
    /**
     * 重置为默认值
     */
    suspend fun reset()
}


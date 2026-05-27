package tech.zhifu.app.myhub.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 平台枚举
 */
enum class Platform {
    ANDROID, IOS, JVM, JS, WASM
}

/**
 * 地区枚举
 */
enum class Region {
    DOMESTIC,  // 国内
    OVERSEAS   // 海外
}

/**
 * 统计服务提供商接口
 * 各个统计 SDK 的适配器实现此接口
 */
interface AnalyticsProvider {
    /** 提供商名称 */
    val name: String

    /** 是否已初始化 */
    val isInitialized: Boolean

    /**
     * Provider 就绪状态（StateFlow）
     * 用于监听初始化完成状态，避免竞态条件
     */
    val isReady: StateFlow<Boolean>

    /** 支持的平台 */
    val supportedPlatforms: Set<Platform>

    /** 支持的地区 */
    val supportedRegions: Set<Region>

    /**
     * 初始化统计服务
     */
    suspend fun initialize(config: ProviderConfig)

    /**
     * 记录事件
     *
     * 注意：如果 Provider 未就绪，事件会被缓冲
     * 初始化完成后会自动上报缓冲的事件
     */
    fun logEvent(event: AnalyticsEvent)

    /**
     * 设置用户属性
     */
    fun setUserProperty(key: String, value: AnalyticsValue?)

    /**
     * 设置用户 ID
     */
    fun setUserId(userId: String?)

    /**
     * 设置当前屏幕
     */
    fun setScreen(screenName: String, screenClass: String? = null)

    /**
     * 重置用户数据
     */
    fun reset()
}

/**
 * Provider 基础实现（抽象类）
 * 提供事件缓冲机制，避免初始化期间的竞态条件
 */
abstract class BaseAnalyticsProvider : AnalyticsProvider {
    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val eventBuffer = mutableListOf<AnalyticsEvent>()
    private val bufferLock = Mutex()

    /**
     * Provider 内部协程作用域
     * 用于非阻塞的事件缓冲操作
     */
    private val providerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    override val isInitialized: Boolean
        get() = _isReady.value

    /**
     * 子类在初始化完成后调用此方法
     */
    protected suspend fun markAsReady() {
        _isReady.value = true
        // 上报缓冲的事件
        flushBufferedEvents()
    }

    override fun logEvent(event: AnalyticsEvent) {
        if (_isReady.value) {
            // 已就绪，直接上报
            doLogEvent(event)
        } else {
            // 未就绪，非阻塞加入缓冲队列
            // 使用 providerScope 避免在主线程使用 runBlocking
            providerScope.launch {
                bufferLock.withLock {
                    eventBuffer.add(event)
                }
            }
        }
    }

    /**
     * 子类实现具体的上报逻辑
     */
    protected abstract fun doLogEvent(event: AnalyticsEvent)

    /**
     * 上报缓冲的事件
     */
    private suspend fun flushBufferedEvents() {
        val events = bufferLock.withLock {
            eventBuffer.toList().also { eventBuffer.clear() }
        }
        events.forEach { doLogEvent(it) }
    }

    /**
     * 清理资源（可选，用于测试或应用退出）
     */
    open fun cleanup() {
        providerScope.cancel()
    }
}

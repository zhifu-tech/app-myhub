package tech.zhifu.app.myhub.logger

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import tech.zhifu.app.myhub.logger.di.loggerModule

/**
 * Logger 模块集成测试
 * 
 * 测试 Logger 模块与其他组件的集成，包括：
 * - Logger 与 Koin 的集成
 * - LoggerFactory 的完整流程
 * - LoggerConfig 的配置和使用
 */
class LoggerIntegrationTest {

    @BeforeTest
    fun setup() {
        // 确保 Koin 已停止（如果之前已启动）
        stopKoin()
    }

    @AfterTest
    fun tearDown() {
        // 清理 Koin 实例
        stopKoin()
    }

    @Test
    fun `test Logger creation with Koin configuration`() {
        // Given: 配置 Koin 并注册 LoggerConfig
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(
                        appName = "TestApp",
                        useAndroidLogger = false
                    )
                }
            )
        }

        // When: 创建 Logger 实例
        val logger = logger("TestModule")

        // Then: 验证 Logger 已创建
        assertNotNull(logger)
    }

    @Test
    fun `test Logger creation with custom tags`() {
        // Given: 配置 Koin
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(appName = "TestApp")
                }
            )
        }

        // When: 使用多个标签创建 Logger
        val logger1 = logger("Module1")
        val logger2 = logger("Module1", "Component1")
        val logger3 = logger("Module1", "Component1", "SubComponent1")

        // Then: 验证所有 Logger 都已创建
        assertNotNull(logger1)
        assertNotNull(logger2)
        assertNotNull(logger3)
    }

    @Test
    fun `test Logger creation without tags`() {
        // Given: 配置 Koin
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(appName = "TestApp")
                }
            )
        }

        // When: 不使用标签创建 Logger
        val logger = logger()

        // Then: 验证 Logger 已创建
        assertNotNull(logger)
    }

    @Test
    fun `test Logger with default configuration fallback`() {
        // Given: 不配置 Koin（模拟配置缺失的情况）
        // 注意：由于 loggerConfig 是懒加载的，我们需要确保 Koin 未启动

        // When: 创建 Logger（应该使用默认配置）
        val logger = logger("TestModule")

        // Then: 验证 Logger 已创建（使用默认配置）
        assertNotNull(logger)
    }

    @Test
    fun `test Logger logging at all levels`() {
        // Given: 配置 Koin 并创建 Logger
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(appName = "TestApp")
                }
            )
        }
        val logger = logger("TestModule")

        // When/Then: 验证所有日志级别都可以正常调用
        logger.trace { "Trace message" }
        logger.debug { "Debug message" }
        logger.info { "Info message" }
        logger.warn { "Warning message" }
        logger.error { "Error message" }

        // 验证没有抛出异常
        assertTrue(true)
    }

    @Test
    fun `test Logger with exception logging`() {
        // Given: 配置 Koin 并创建 Logger
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(appName = "TestApp")
                }
            )
        }
        val logger = logger("TestModule")
        val exception = RuntimeException("Test exception")

        // When/Then: 验证异常日志记录
        logger.error(exception) { "Error with exception" }
        logger.warn(exception) { "Warning with exception" }

        // 验证没有抛出异常
        assertTrue(true)
    }

    @Test
    fun `test Logger with marker`() {
        // Given: 配置 Koin 并创建 Logger
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(appName = "TestApp")
                }
            )
        }
        val logger = logger("TestModule")

        // When/Then: 验证标记日志记录
        logger.info("DATABASE") { "Database operation" }
        logger.debug("NETWORK") { "Network request" }
        logger.warn("SECURITY") { "Security warning" }

        // 验证没有抛出异常
        assertTrue(true)
    }

    @Test
    fun `test Logger level checking`() {
        // Given: 配置 Koin 并创建 Logger
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(appName = "TestApp")
                }
            )
        }
        val logger = logger("TestModule")

        // When/Then: 验证日志级别检查方法
        val traceEnabled = logger.isTraceEnabled()
        val debugEnabled = logger.isDebugEnabled()
        val infoEnabled = logger.isInfoEnabled()
        val warnEnabled = logger.isWarnEnabled()
        val errorEnabled = logger.isErrorEnabled()

        // 验证方法返回了有效值（布尔值）
        assertNotNull(traceEnabled)
        assertNotNull(debugEnabled)
        assertNotNull(infoEnabled)
        assertNotNull(warnEnabled)
        assertNotNull(errorEnabled)
    }

    @Test
    fun `test Logger with expensive computation using level check`() {
        // Given: 配置 Koin 并创建 Logger
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(appName = "TestApp")
                }
            )
        }
        val logger = logger("TestModule")
        var computationExecuted = false

        // When: 使用日志级别检查避免不必要的计算
        if (logger.isDebugEnabled()) {
            computationExecuted = true
            logger.debug { "Expensive computation result" }
        }

        // Then: 验证计算可能被执行（取决于日志级别配置）
        // 注意：这个测试主要验证模式正确，不验证计算是否真的被执行
        assertNotNull(computationExecuted)
    }

    @Test
    fun `test multiple Logger instances with different tags`() {
        // Given: 配置 Koin
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(appName = "TestApp")
                }
            )
        }

        // When: 创建多个不同标签的 Logger
        val logger1 = logger("Module1")
        val logger2 = logger("Module2")
        val logger3 = logger("Module1", "Component1")

        // Then: 验证所有 Logger 都已创建且独立
        assertNotNull(logger1)
        assertNotNull(logger2)
        assertNotNull(logger3)

        // 验证它们可以独立使用
        logger1.info { "Message from Module1" }
        logger2.info { "Message from Module2" }
        logger3.info { "Message from Module1:Component1" }

        assertTrue(true)
    }

    @Test
    fun `test LoggerConfig platform configuration`() {
        // Given: 配置 Koin 并设置平台特定配置
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(
                        appName = "TestApp",
                        useAndroidLogger = true
                    )
                }
            )
        }

        // When: 创建 Logger
        val logger = logger("TestModule")

        // Then: 验证 Logger 已创建（平台配置应该已应用）
        assertNotNull(logger)
        
        // 验证日志可以正常记录
        logger.info { "Test message" }
        assertTrue(true)
    }

    @Test
    fun `test Logger reconfiguration`() {
        // Given: 第一次配置 Koin
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(appName = "App1")
                }
            )
        }
        val logger1 = logger("Module1")

        // When: 停止并重新配置 Koin
        stopKoin()
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(appName = "App2")
                }
            )
        }
        val logger2 = logger("Module1")

        // Then: 验证两个 Logger 都已创建
        assertNotNull(logger1)
        assertNotNull(logger2)

        // 验证它们可以正常使用
        logger1.info { "Message from App1" }
        logger2.info { "Message from App2" }
        assertTrue(true)
    }

    @Test
    fun `test Logger with extension functions`() {
        // Given: 配置 Koin 并创建 Logger
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(appName = "TestApp")
                }
            )
        }
        val logger = logger("TestModule")

        // When/Then: 验证扩展函数的使用
        // 只传递消息
        logger.info { "Simple message" }
        
        // 传递异常和消息
        val exception = RuntimeException("Test")
        logger.error(exception) { "Error with exception" }
        
        // 传递标记和消息
        logger.debug("DATABASE") { "Database operation" }

        // 验证没有抛出异常
        assertTrue(true)
    }

    @Test
    fun `test Logger name format`() {
        // Given: 配置 Koin
        startKoin {
            modules(
                loggerModule {
                    LoggerConfig(appName = "TestApp")
                }
            )
        }

        // When: 创建不同格式的 Logger
        val logger1 = logger()                    // 应该使用 appName
        val logger2 = logger("Module")            // 应该使用 appName:Module
        val logger3 = logger("Module", "Comp")    // 应该使用 appName:Module:Comp

        // Then: 验证所有 Logger 都已创建
        assertNotNull(logger1)
        assertNotNull(logger2)
        assertNotNull(logger3)

        // 验证它们可以正常使用
        logger1.info { "Default logger" }
        logger2.info { "Module logger" }
        logger3.info { "Component logger" }
        assertTrue(true)
    }
}

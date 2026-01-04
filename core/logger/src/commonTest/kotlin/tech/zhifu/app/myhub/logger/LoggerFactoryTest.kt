package tech.zhifu.app.myhub.logger

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * LoggerFactory 单元测试
 */
class LoggerFactoryTest {

    @BeforeTest
    fun setup() {
        // 启动 Koin 并注册 LoggerConfig
        startKoin {
            modules(
                module {
                    single<LoggerConfig> {
                        LoggerConfig(appName = "TestApp")
                    }
                }
            )
        }
    }

    @AfterTest
    fun tearDown() {
        // 清理 Koin
        stopKoin()
    }

    @Test
    fun `test logger factory creates logger with app name`() {
        // When
        val logger = logger()

        // Then
        assertNotNull(logger)
    }

    @Test
    fun `test logger factory creates logger with tags`() {
        // When
        val logger = logger("Tag1", "Tag2")

        // Then
        assertNotNull(logger)
    }

    @Test
    fun `test logger factory creates logger with single tag`() {
        // When
        val logger = logger("Module")

        // Then
        assertNotNull(logger)
    }

    @Test
    fun `test logger factory creates logger with empty tags`() {
        // When
        val logger = logger(*emptyArray())

        // Then
        assertNotNull(logger)
    }

    @Test
    fun `test logger factory creates multiple loggers`() {
        // When
        val logger1 = logger("Module1")
        val logger2 = logger("Module2")

        // Then
        assertNotNull(logger1)
        assertNotNull(logger2)
    }
}

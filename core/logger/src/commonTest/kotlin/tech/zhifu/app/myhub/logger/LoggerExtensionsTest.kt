package tech.zhifu.app.myhub.logger

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Logger 扩展函数单元测试
 */
class LoggerExtensionsTest {

    @Test
    fun `test trace extension with message only`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When/Then - 验证方法调用不会抛出异常
        // 注意：当日志级别被禁用时，消息 lambda 可能不会被调用
        logger.trace {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test trace extension with throwable`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)
        val throwable = RuntimeException("Test error")

        // When/Then - 验证方法调用不会抛出异常
        logger.trace(throwable) {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test debug extension with message only`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When/Then - 验证方法调用不会抛出异常
        logger.debug {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test debug extension with throwable`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)
        val throwable = RuntimeException("Test error")

        // When/Then - 验证方法调用不会抛出异常
        logger.debug(throwable) {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test info extension with message only`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When/Then - 验证方法调用不会抛出异常
        // 注意：当日志级别被禁用时，消息 lambda 可能不会被调用
        logger.info {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test info extension with throwable`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)
        val throwable = RuntimeException("Test error")

        // When/Then - 验证方法调用不会抛出异常
        logger.info(throwable) {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test warn extension with message only`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When/Then - 验证方法调用不会抛出异常
        // 注意：当日志级别被禁用时，消息 lambda 可能不会被调用
        logger.warn {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test warn extension with throwable`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)
        val throwable = RuntimeException("Test error")

        // When/Then - 验证方法调用不会抛出异常
        logger.warn(throwable) {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test error extension with message only`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When/Then - 验证方法调用不会抛出异常
        // 注意：当日志级别被禁用时，消息 lambda 可能不会被调用
        logger.error {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test error extension with throwable`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)
        val throwable = RuntimeException("Test error")

        // When/Then - 验证方法调用不会抛出异常
        logger.error(throwable) {
            "Test message"
        }
        assertTrue(true)
    }
}

package tech.zhifu.app.myhub.logger

import io.github.oshai.kotlinlogging.KMarkerFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * LoggerImpl 单元测试
 * 
 * 注意：由于 KLogger 接口复杂，我们使用真实的 KotlinLogging 实例进行测试
 */
class LoggerImplTest {

    @Test
    fun `test LoggerImpl creation`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")

        // When
        val logger = LoggerImpl(kLogger)

        // Then
        assertNotNull(logger)
    }

    @Test
    fun `test isTraceEnabled`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When
        val result = logger.isTraceEnabled()

        // Then - 验证方法可以正常调用（返回值取决于日志配置）
        assertNotNull(result)
    }

    @Test
    fun `test isDebugEnabled`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When
        val result = logger.isDebugEnabled()

        // Then
        assertNotNull(result)
    }

    @Test
    fun `test isInfoEnabled`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When
        val result = logger.isInfoEnabled()

        // Then
        assertNotNull(result)
    }

    @Test
    fun `test isWarnEnabled`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When
        val result = logger.isWarnEnabled()

        // Then
        assertNotNull(result)
    }

    @Test
    fun `test isErrorEnabled`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When
        val result = logger.isErrorEnabled()

        // Then
        assertNotNull(result)
    }

    @Test
    fun `test trace with message`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When/Then - 验证方法调用不会抛出异常
        // 注意：当日志级别被禁用时，消息 lambda 可能不会被调用
        logger.trace(null, null) {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test debug with message`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When/Then - 验证方法调用不会抛出异常
        logger.debug(null, null) {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test info with message`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When/Then - 验证方法调用不会抛出异常
        // 注意：当日志级别被禁用时，消息 lambda 可能不会被调用
        logger.info(null, null) {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test warn with message`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When/Then - 验证方法调用不会抛出异常
        // 注意：当日志级别被禁用时，消息 lambda 可能不会被调用
        logger.warn(null, null) {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test error with message`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When/Then - 验证方法调用不会抛出异常
        // 注意：当日志级别被禁用时，消息 lambda 可能不会被调用
        logger.error(null, null) {
            "Test message"
        }
        assertTrue(true)
    }

    @Test
    fun `test trace with throwable`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)
        val throwable = RuntimeException("Test error")

        // When/Then - 验证没有抛出异常
        logger.trace(null, throwable) { "Test message" }
        assertTrue(true)
    }

    @Test
    fun `test error with throwable`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)
        val throwable = RuntimeException("Test error")

        // When/Then - 验证没有抛出异常
        logger.error(null, throwable) { "Test message" }
        assertTrue(true)
    }

    @Test
    fun `test trace with marker string`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When/Then - 验证没有抛出异常
        logger.trace("TEST_MARKER", null) { "Test message" }
        assertTrue(true)
    }

    @Test
    fun `test debug with marker object`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)
        val marker = KMarkerFactory.getMarker("TEST_MARKER")

        // When/Then - 验证没有抛出异常
        logger.debug(marker, null) { "Test message" }
        assertTrue(true)
    }

    @Test
    fun `test info with null marker`() {
        // Given
        val kLogger = KotlinLogging.logger("test-logger")
        val logger = LoggerImpl(kLogger)

        // When/Then - 验证没有抛出异常
        logger.info(null, null) { "Test message" }
        assertTrue(true)
    }
}

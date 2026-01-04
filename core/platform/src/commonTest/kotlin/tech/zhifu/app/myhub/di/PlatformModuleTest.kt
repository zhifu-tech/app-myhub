package tech.zhifu.app.myhub.di

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * PlatformModule 单元测试
 * 测试各平台的 platformModule() 实现
 */
class PlatformModuleTest {

    @BeforeTest
    fun setup() {
        // 启动 Koin 上下文
        startKoin {
            modules(platformModule())
        }
    }

    @AfterTest
    fun tearDown() {
        // 清理 Koin 上下文
        stopKoin()
    }

    @Test
    fun `test platformModule returns non-null module`() {
        // When
        val module = platformModule()

        // Then
        assertNotNull(module)
    }

    @Test
    fun `test platformModule can be registered in Koin`() {
        // Given
        stopKoin() // 清理之前的 Koin 实例

        // When
        startKoin {
            modules(platformModule())
        }

        // Then
        // 如果没有抛出异常，说明模块注册成功
        assertNotNull(platformModule())
    }

    @Test
    fun `test platformModule can be called multiple times`() {
        // When
        val module1 = platformModule()
        val module2 = platformModule()

        // Then
        assertNotNull(module1)
        assertNotNull(module2)
        // 每次调用都应该返回有效的模块实例
    }
}


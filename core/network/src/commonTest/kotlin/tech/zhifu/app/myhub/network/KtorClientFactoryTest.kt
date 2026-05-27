package tech.zhifu.app.myhub.network

import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * KtorClientFactory 和 createHttpClient 测试
 */
class KtorClientFactoryTest {

    @Test
    fun `test createHttpClient without auth`() {
        // When
        val httpClient = createHttpClient()

        // Then
        assertNotNull(httpClient)
    }

    @Test
    fun `test createHttpClient returns configured HttpClient`() {
        // When
        val httpClient = createHttpClient()

        // Then - 验证 HttpClient 已创建且不为空
        assertNotNull(httpClient)
        // 注意：实际的 HTTP 请求测试应该在集成测试中使用 MockEngine 进行
        // 这里主要验证函数可以正常调用并返回配置好的 HttpClient
    }
}


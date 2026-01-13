# Core Network Test Module

本模块用于**规范**和**实现** MyHub 应用的网络测试基础设施（Network Test Infra），为各功能模块**提供统一的 Mock HttpClient 测试工具**。它基于 **Ktor Client Mock** 引擎，实现了**Mock HTTP 请求**、**JSON 序列化**、**跨平台支持**等特性，并提供了面向 KMP 场景的**统一测试接口**，方便在 **Android、iOS、JVM、JS、WASM** 等多端项目的单元测试中集成和使用。

## 📋 功能特性

- ✅ **Mock HttpClient**: 提供创建 Mock HttpClient 的工具函数
- ✅ **测试支持**: 用于单元测试中模拟 HTTP 请求和响应
- ✅ **JSON 序列化**: 自动配置 JSON 序列化/反序列化
- ✅ **跨平台**: 支持所有 Kotlin Multiplatform 平台
- ✅ **单元测试**: 包含完整的单元测试覆盖

## 🎯 核心组件

### createMockHttpClient

提供创建 Mock HttpClient 的工具函数，用于测试中模拟 HTTP 请求和响应。

**函数签名：**

```kotlin
fun createMockHttpClient(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
): HttpClient
```

**配置内容：**

- **MockEngine**: 使用 Ktor Mock 引擎模拟 HTTP 请求
- **ContentNegotiation**: JSON 序列化/反序列化
    - `ignoreUnknownKeys = true` - 忽略未知字段
    - `isLenient = true` - 宽松模式
    - `encodeDefaults = true` - 编码默认值
- **DefaultRequest**: 默认请求头
    - `Content-Type: application/json`
    - `Accept: application/json`

## 📝 使用示例

### 基本使用

```kotlin
import tech.zhifu.app.myhub.network.test.createMockHttpClient
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf

val httpClient = createMockHttpClient { request ->
    when (request.url.encodedPath) {
        "/api/cards" -> respond(
            content = """[{"id":"1","title":"Test"}]""",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
        else -> respond(
            content = "",
            status = HttpStatusCode.NotFound
        )
    }
}
```

### 处理不同的状态码

```kotlin
val httpClient = createMockHttpClient { request ->
    when (request.url.encodedPath) {
        "/api/success" -> respond(
            content = """{"status":"ok"}""",
            status = HttpStatusCode.OK
        )
        "/api/not-found" -> respond(
            content = "",
            status = HttpStatusCode.NotFound
        )
        "/api/server-error" -> respond(
            content = """{"error":"internal error"}""",
            status = HttpStatusCode.InternalServerError
        )
        else -> respond(
            content = "",
            status = HttpStatusCode.BadRequest
        )
    }
}
```

### 验证请求参数

```kotlin
val httpClient = createMockHttpClient { request ->
    // 验证请求路径
    assertEquals("/api/cards", request.url.encodedPath)

    // 验证查询参数
    assertEquals("param=value", request.url.encodedQuery)

    respond(
        content = """{"id":"1"}""",
        status = HttpStatusCode.OK
    )
}
```

### 使用序列化对象

```kotlin
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TestData(
    val id: String,
    val name: String,
    val value: Int = 0
)

val testData = TestData("1", "test", 42)
val httpClient = createMockHttpClient { request ->
    respond(
        content = Json.encodeToString(TestData.serializer(), testData),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
}

// 使用
val response = httpClient.get("https://example.com/api/data")
val result: TestData = response.body()
```

## 📁 模块结构

```
core/network-test/
├── src/
│   ├── commonMain/
│   │   └── kotlin/tech/zhifu/app/myhub/network/test/
│   │       └── KtorMockClientFactory.kt  # createMockHttpClient 函数
│   └── commonTest/
│       └── kotlin/tech/zhifu/app/myhub/network/test/
│           └── KtorMockClientFactoryTest.kt  # 单元测试
└── build.gradle.kts
```

## 🔧 依赖关系

### 依赖的模块

- `core:network` - 网络模块（提供 HttpClient 类型）

### 外部依赖

- `ktor-client-mock` - Ktor Mock 引擎
- `ktor-client-core` - Ktor 客户端核心
- `ktor-client-content-negotiation` - 内容协商
- `ktor-serialization-kotlinx-json` - JSON 序列化

### 测试依赖

- `kotlin-test` - Kotlin 测试框架
- `kotlinx-coroutines-test` - 协程测试支持

## 🧪 测试

模块包含完整的单元测试：

- **KtorMockClientFactoryTest**: 测试 `createMockHttpClient` 函数
    - 验证 HttpClient 创建和配置
    - 测试不同 HTTP 状态码处理
    - 测试 JSON 序列化配置
    - 测试默认请求头配置
    - 测试 URL 处理
    - 测试空响应处理
    - 测试未知字段处理
    - 测试多次请求处理

运行测试：

```bash
./gradlew :core:network-test:test
```

## ⚠️ 注意事项

1. **仅用于测试**: 此模块仅用于测试，不应在生产代码中使用
2. **Mock HttpClient**: Mock HttpClient 已配置好 JSON 序列化和默认请求头
3. **函数导入**: 使用 `createMockHttpClient` 函数时需要导入：
   ```kotlin
   import tech.zhifu.app.myhub.network.test.createMockHttpClient
   ```
4. **响应处理**: handler 函数接收 `HttpRequestData` 并返回 `HttpResponseData`
5. **协程支持**: handler 函数是 suspend 函数，支持异步操作

## 🔗 相关模块

- `core:network` - 网络模块，提供实际的 HttpClient 实现
- `core:datastore-datasource-remote` - 远程数据源实现，使用本模块进行测试

## 📚 更多示例

查看 `core:datastore-datasource-remote` 模块的测试文件，了解更详细的使用示例：

- `RemoteCardDataSourceTest.kt`
- `RemoteTagDataSourceTest.kt`
- `RemoteTemplateDataSourceTest.kt`
- `RemoteUserDataSourceTest.kt`
- `RemoteStatisticsDataSourceTest.kt`

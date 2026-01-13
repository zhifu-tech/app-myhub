# Core Network Module

本模块用于**规范**和**实现** MyHub 应用的网络层基础设施（Network Infra），为各功能模块**提供统一的跨平台 HTTP 客户端能力**。它基于 **Ktor Client** 和 **Kotlin Multiplatform expect/actual 机制**，实现了**类型安全的 HTTP 请求**、**JSON 序列化**、**统一错误处理**、**配置管理**等特性，并提供了面向 KMP 场景的**统一网络层接口**，方便在 **Android、iOS、JVM、JS、WASM** 等多端项目中集成和使用。

## 核心组件

### 1. ApiConfig

统一的 API 配置管理对象：

- **`BASE_URL`**：API 基础 URL（支持动态设置）
- **`setBaseUrl(url)`**：设置 API 基础 URL
- **API 路径常量**：`CARDS_PATH`、`TAGS_PATH`、`TEMPLATES_PATH` 等
- **超时配置**：`CONNECT_TIMEOUT`、`SOCKET_TIMEOUT`

### 2. KtorClientFactory

跨平台的 Ktor Client 引擎工厂：

- **`createEngine()`**：创建平台特定的网络引擎
- **平台支持**：Android（OkHttp）、iOS（NSURLSession）、JVM（Java NIO）、Web（Fetch API）

### 3. createHttpClient

创建配置好的 HttpClient 函数：

- **ContentNegotiation**：JSON 序列化/反序列化配置
- **DefaultRequest**：默认请求头（Content-Type、Accept）
- **Logging**：请求/响应日志记录

### 4. NetworkModule

Koin 依赖注入模块：

- **`KtorClientFactory`**：单例工厂
- **`HttpClient`**：单例 HTTP 客户端

### 5. 异常类

统一的错误处理：

- **`ApiException`**：API 返回错误状态码时抛出
- **`NetworkException`**：网络连接错误时抛出

## 使用示例

```kotlin
// 1. 配置 API 基础 URL
import tech.zhifu.app.myhub.network.ApiConfig

ApiConfig.setBaseUrl("https://api.example.com")

// 2. 创建 HttpClient（通过 Koin）
import tech.zhifu.app.myhub.network.di.networkModule
import io.ktor.client.HttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

val appModule = module {
    includes(networkModule)
}

class MyService : KoinComponent {
    private val httpClient: HttpClient by inject()

    suspend fun getCards(): List<CardDto> {
        val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}")
        return response.body()
    }
}

// 3. 错误处理
import tech.zhifu.app.myhub.network.ApiException
import tech.zhifu.app.myhub.network.NetworkException

suspend fun fetchData(): Result<DataDto> {
    return try {
        val response = httpClient.get(url)
        val data: DataDto = response.body()
        Result.success(data)
    } catch (e: ApiException) {
        // 处理 API 错误
        Result.failure(e)
    } catch (e: NetworkException) {
        // 处理网络错误
        Result.failure(e)
    }
}
```

## 文档

- [MyHub 网络层模块方案设计](./docs/myhub-network-infra-v1.0.md)

# MyHub 网络层模块方案设计

**方案名称**：Network Infra v1  
**文档版本**：v1.0  
**文档类型**：技术方案设计文档  
**创建日期**：2026-01-13  
**锁定日期**：2026-01-13  
**最后更新**：2026-01-13  
**作者**：MyHub Development Team  
**评审状态**：🟢 通过  
**方案状态**：🔒 已锁定

---

## 📋 文档目录

1. [修改历史](#修改历史)
2. [方案状态摘要](#-方案状态摘要)
3. [问题背景](#1-问题背景)
4. [设计目标](#2-设计目标)
5. [技术调研](#3-技术调研)
6. [架构设计](#4-架构设计)
7. [实现细节](#5-实现细节)
8. [实施计划](#6-实施计划)
9. [风险评估](#7-风险评估)
10. [附录](#8-附录)

---

## 📊 方案状态摘要

**当前状态**：

- **评审状态**：🟢 通过（文档已通过评审，可以进入实施阶段）
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Network Infra v1 的基线设计
- **锁定日期**：2026-01-13
- **当前进度**：所有阶段已完成，方案设计已确定并锁定

**状态说明**：

- **评审状态**：用于标识文档的评审进度
  - 🟢 通过：文档已通过评审，可以进入实施阶段
  - 🟡 待评审：文档正在等待评审或评审进行中
  - 🔴 需修改：文档评审后需要修改
- **方案状态**：用于标识方案的实施进度
  - 🔒 已锁定：方案设计已确定，不允许随意修改
  - 📝 进行中：方案设计正在进行中，可以修改
  - ⏸️ 暂停：方案设计暂时停止，保留当前状态
- 详细状态定义请参考 [MyHub 架构设计文档规范](../../../docs/myhub-infra-rules.md)

---

## 修改历史

| 版本 | 日期       | 修改内容                           | 修改原因           |
| ---- | ---------- | ---------------------------------- | ------------------ |
| v1.0 | 2026-01-13 | 初始方案设计                       | 新建               |
| v1.0 | 2026-01-13 | 完成架构设计文档                   | 完善文档           |
| v1.0 | 2026-01-13 | 更新状态：评审通过、方案已锁定     | 状态更新：评审通过 |

---

## 1. 问题背景

### 1.1 用户场景

在 MyHub 应用的开发和运行过程中，需要进行网络请求以获取远程数据。典型的场景包括：

1. **HTTP 请求**：应用需要向服务器发送 HTTP 请求，获取数据或提交数据
2. **JSON 序列化**：应用需要将对象序列化为 JSON 发送，或将 JSON 响应反序列化为对象
3. **错误处理**：应用需要统一处理网络错误和 API 错误
4. **跨平台支持**：应用需要在 Android、iOS、JVM、Web 等多个平台上使用统一的网络 API
5. **配置管理**：应用需要根据不同的构建变体（dev/prod）使用不同的 API 基础 URL
6. **日志记录**：开发阶段需要记录网络请求和响应的详细信息，便于调试

### 1.2 问题根因

在引入统一的网络层模块之前，MyHub 应用面临以下问题：

1. **网络库选择分散**：各模块可能使用不同的网络库（如 Android 使用 OkHttp，iOS 使用 URLSession），导致代码重复和不一致
2. **序列化配置分散**：各模块可能使用不同的 JSON 序列化配置，导致序列化行为不一致
3. **错误处理不统一**：各模块可能使用不同的错误处理方式，导致错误处理逻辑分散
4. **平台特定代码耦合**：业务代码直接使用平台特定的网络 API，导致跨平台代码难以维护
5. **配置管理混乱**：API 基础 URL 等配置分散在各处，难以统一管理
6. **测试困难**：网络请求难以测试，特别是单元测试

### 1.3 影响范围

- **开发效率**：缺乏统一的网络层导致开发人员需要了解多个平台的网络 API，降低开发效率
- **代码维护**：网络相关代码分散，增加维护成本和出错风险
- **跨平台兼容性**：直接使用平台特定网络 API 导致代码难以在不同平台间复用
- **错误处理**：错误处理不统一导致用户体验差，问题排查困难
- **测试困难**：网络请求难以测试，影响代码质量

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **统一的 HTTP 客户端**：提供统一的 `HttpClient` 接口，隐藏平台实现细节
- ✅ **跨平台支持**：支持 Android、iOS、JVM、JS、WASM 等多个平台
- ✅ **JSON 序列化**：自动处理 JSON 序列化/反序列化，支持类型安全的 API
- ✅ **统一错误处理**：提供统一的异常类型（`ApiException`、`NetworkException`），便于错误处理
- ✅ **配置管理**：提供 `ApiConfig` 统一管理 API 配置（基础 URL、路径、超时等）
- ✅ **依赖注入集成**：提供 `NetworkModule` Koin 模块，便于依赖注入
- ✅ **请求日志**：内置请求/响应日志记录，便于调试
- ✅ **类型安全**：使用 Kotlin 的类型系统，提供类型安全的 API

### 2.2 非功能目标

- ✅ **性能优化**：使用高效的网络引擎，减少网络请求开销
- ✅ **易用性**：提供简洁、易用的 API，降低学习成本
- ✅ **可扩展性**：易于添加新的网络功能（如认证、重试等）
- ✅ **可测试性**：提供可测试的接口，支持单元测试和集成测试
- ✅ **代码复用**：最大化代码复用，减少重复代码

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 Ktor Client

**选择理由**：

- ✅ **KMP 原生支持**：Ktor Client 是 JetBrains 提供的跨平台 HTTP 客户端，原生支持 KMP
- ✅ **多平台引擎**：支持 Android、iOS、JVM、JS、WASM 等多个平台的网络引擎
- ✅ **协程支持**：基于 Kotlin Coroutines，提供异步、非阻塞的网络请求
- ✅ **插件化架构**：支持插件化架构，易于扩展功能
- ✅ **类型安全**：支持类型安全的序列化，与 Kotlinx Serialization 集成良好
- ✅ **活跃维护**：JetBrains 官方维护，更新及时

#### 3.1.2 Kotlinx Serialization

**选择理由**：

- ✅ **KMP 原生支持**：Kotlinx Serialization 是 Kotlin 官方的序列化库，原生支持 KMP
- ✅ **类型安全**：使用 Kotlin 的类型系统，编译时检查，避免运行时错误
- ✅ **性能优化**：编译时生成序列化代码，运行时性能好
- ✅ **Ktor 集成**：与 Ktor Client 集成良好，配置简单

#### 3.1.3 expect/actual 机制

**选择理由**：

- ✅ **KMP 原生支持**：Kotlin Multiplatform 提供的 expect/actual 机制是跨平台抽象的标准方式
- ✅ **编译时检查**：expect/actual 机制在编译时检查，确保所有平台都提供了实现
- ✅ **类型安全**：使用 Kotlin 的类型系统，提供类型安全的 API
- ✅ **零运行时开销**：expect/actual 在编译时解析，运行时无额外开销

### 3.2 平台支持策略

#### 3.2.1 支持的平台

| 平台    | 支持状态    | 说明                     |
| ------- | ----------- | ------------------------ |
| Android | ✅ 完全支持 | 使用 Ktor Android 引擎   |
| iOS     | ✅ 完全支持 | 使用 Ktor Darwin 引擎    |
| JVM     | ✅ 完全支持 | 使用 Ktor CIO 引擎       |
| JS      | ✅ 完全支持 | 使用 Ktor JS 引擎        |
| WASM    | ✅ 完全支持 | 使用 Ktor JS 引擎        |

#### 3.2.2 平台特定实现策略

- **Android**：使用 `ktor-client-android`，基于 OkHttp
- **iOS**：使用 `ktor-client-darwin`，基于 NSURLSession
- **JVM**：使用 `ktor-client-cio`，基于 Java NIO
- **JS/WASM**：使用 `ktor-client-js`，基于 Fetch API

### 3.3 配置管理策略

#### 3.3.1 API 基础 URL 优先级

1. **通过 `ApiConfig.setBaseUrl()` 设置的 URL**（最高优先级）
2. **系统属性 `myhub.api.base.url`**（Android/JVM 平台）
3. **默认值** `http://192.168.0.123:8083`

#### 3.3.2 配置来源

- **应用启动时配置**：通过 `AppBuildConfig.apiBaseUrl` 设置
- **系统属性**：通过 Gradle 属性或运行时设置
- **默认值**：开发环境默认值

---

## 4. 架构设计

### 4.1 整体架构

```text
┌─────────────────────────────────────────────────────────────┐
│                     应用层（Application）                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Feature A   │  │  Feature B   │  │  Feature C   │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
└─────────┼──────────────────┼──────────────────┼──────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
          ┌──────────────────▼──────────────────┐
          │   core:network 模块                 │
          │                                     │
          │  ┌──────────────────────────────┐   │
          │  │    HttpClient API           │   │
          │  │  (统一接口)                  │   │
          │  └──────────────┬─────────────┘   │
          │                 │                  │
          │  ┌──────────────┼──────────────┐   │
          │  │              │              │   │
          │  ▼              ▼              ▼   │
          │  ┌──────────┐  ┌──────────┐  ┌──────────┐
          │  │ApiConfig │  │KtorClient│  │Network  │
          │  │          │  │Factory   │  │Module   │
          │  │          │  │          │  │(DI)     │
          │  └────┬─────┘  └────┬─────┘  └────┬─────┘
          │       │             │             │
          │       │             │             │
          │  ┌────▼─────────────▼─────────────▼─────┐
          │  │     expect/actual 机制               │
          │  │  (平台特定引擎实现)                   │
          │  └──────────────────────────────────────┘
          └──────────────────┬───────────────────────┘
                             │
          ┌──────────────────▼──────────────────┐
          │      KMP Source Sets               │
          │                                     │
          │  ┌──────────────┐                  │
          │  │  commonMain  │                  │
          │  │  (公共接口)  │                  │
          │  └──────┬───────┘                  │
          │         │                           │
          │  ┌───────┼───────┐                 │
          │  │       │       │                 │
          │  ▼       ▼       ▼                 │
          │  ┌──────┐ ┌──────┐ ┌──────┐        │
          │  │android│ │ ios │ │ jvm │ │ js/  │
          │  │ Main  │ │Main │ │Main │ │wasmJs│
          │  └──────┘ └──────┘ └──────┘ └──────┘
          └─────────────────────────────────────┘
                             │
          ┌──────────────────▼──────────────────┐
          │      平台特定引擎                    │
          │  - Android: OkHttp                  │
          │  - iOS: NSURLSession                │
          │  - JVM: Java NIO                    │
          │  - Web: Fetch API                   │
          └─────────────────────────────────────┘
```

### 4.2 核心组件设计

#### 4.2.1 ApiConfig

`ApiConfig` 对象提供统一的 API 配置管理：

```kotlin
package tech.zhifu.app.myhub.network

object ApiConfig {
    val BASE_URL: String
    fun setBaseUrl(url: String)
    
    // API 路径常量
    const val CARDS_PATH = "/api/cards"
    const val TAGS_PATH = "/api/tags"
    const val TEMPLATES_PATH = "/api/templates"
    const val USERS_PATH = "/api/users"
    const val STATISTICS_PATH = "/api/statistics"
    
    // 超时配置
    const val CONNECT_TIMEOUT = 30_000L
    const val SOCKET_TIMEOUT = 30_000L
}
```

**设计要点**：

- 使用 `object` 单例模式，全局唯一
- `BASE_URL` 支持动态设置，优先级明确
- 提供 API 路径常量，避免硬编码
- 提供超时配置常量，便于统一管理

#### 4.2.2 KtorClientFactory

`KtorClientFactory` 提供跨平台的 Ktor Client 引擎工厂：

```kotlin
package tech.zhifu.app.myhub.network

import io.ktor.client.engine.HttpClientEngine

expect class KtorClientFactory() {
    fun createEngine(): HttpClientEngine
}
```

**设计要点**：

- 使用 `expect class` 定义工厂接口
- 各平台提供 `actual` 实现，返回平台特定的引擎
- 隐藏平台实现细节，提供统一接口

#### 4.2.3 createHttpClient

`createHttpClient()` 函数创建配置好的 HttpClient：

```kotlin
package tech.zhifu.app.myhub.network

fun createHttpClient(factory: KtorClientFactory): HttpClient {
    return HttpClient(factory.createEngine()) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        
        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
        }
        
        install(Logging) {
            level = LogLevel.INFO
        }
    }
}
```

**设计要点**：

- 统一配置 JSON 序列化
- 统一配置默认请求头
- 统一配置日志记录
- 返回配置好的 HttpClient 实例

#### 4.2.4 NetworkException

`NetworkException` 和 `ApiException` 提供统一的错误处理：

```kotlin
package tech.zhifu.app.myhub.network

class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)
```

**设计要点**：

- `ApiException`：API 返回错误状态码时抛出
- `NetworkException`：网络连接错误时抛出
- 提供统一的错误处理接口

#### 4.2.5 NetworkModule

`NetworkModule` 提供 Koin 依赖注入模块：

```kotlin
package tech.zhifu.app.myhub.network.di

val networkModule = module {
    single<KtorClientFactory> {
        KtorClientFactory()
    }
    
    single<HttpClient> {
        createHttpClient(get())
    }
}
```

**设计要点**：

- 提供 `KtorClientFactory` 单例
- 提供 `HttpClient` 单例
- 便于依赖注入集成

### 4.3 模块结构

```text
core/network/
├── build.gradle.kts              # 模块构建配置
├── README.md                     # 模块说明文档
├── docs/                         # 架构设计文档
│   └── myhub-network-infra-v1.0.md
└── src/
    ├── commonMain/               # 公共接口和实现
    │   └── kotlin/tech/zhifu/app/myhub/network/
    │       ├── ApiConfig.kt                    # API 配置
    │       ├── KtorClientFactory.kt            # Ktor Client 工厂接口
    │       ├── NetworkException.kt             # 异常定义
    │       └── di/
    │           └── NetworkModule.kt            # Koin DI 模块
    │
    ├── androidMain/              # Android 平台特定实现
    │   └── kotlin/tech/zhifu/app/myhub/network/
    │       └── KtorClientFactory.android.kt    # Android 引擎实现
    │
    ├── iosMain/                  # iOS 平台特定实现
    │   └── kotlin/tech/zhifu/app/myhub/network/
    │       └── KtorClientFactory.ios.kt        # iOS 引擎实现
    │
    ├── jvmMain/                  # JVM 平台特定实现
    │   └── kotlin/tech/zhifu/app/myhub/network/
    │       └── KtorClientFactory.jvm.kt        # JVM 引擎实现
    │
    ├── jsMain/                   # JS 平台特定实现
    │   └── kotlin/tech/zhifu/app/myhub/network/
    │       └── KtorClientFactory.js.kt         # JS 引擎实现
    │
    ├── wasmJsMain/               # WASM 平台特定实现
    │   └── kotlin/tech/zhifu/app/myhub/network/
    │       └── KtorClientFactory.wasmJs.kt     # WASM 引擎实现
    │
    └── commonTest/               # 公共测试代码
        └── kotlin/tech/zhifu/app/myhub/network/
            ├── ApiConfigTest.kt                # ApiConfig 测试
            ├── KtorClientFactoryTest.kt        # KtorClientFactory 测试
            └── NetworkExceptionTest.kt          # 异常类测试
```

---

## 5. 实现细节

### 5.1 关键 API 设计

#### 5.1.1 ApiConfig 实现

```kotlin
package tech.zhifu.app.myhub.network

import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn
import tech.zhifu.app.myhub.system.getSystemProperty
import kotlin.concurrent.Volatile

object ApiConfig {
    private const val DEFAULT_PORT = 8083
    private const val DEFAULT_REMOTE_IP = "192.168.0.123"

    @Volatile
    private var _baseUrl: String? = null

    val BASE_URL: String
        get() {
            if (_baseUrl != null) {
                logger.info { "Using configured API base URL: $_baseUrl" }
                return _baseUrl!!
            }

            val systemPropertyUrl = getSystemProperty("myhub.api.base.url")
            if (!systemPropertyUrl.isNullOrBlank()) {
                logger.info { "Using system property API base URL: $systemPropertyUrl" }
                return systemPropertyUrl
            }

            val defaultUrl = "http://$DEFAULT_REMOTE_IP:$DEFAULT_PORT"
            logger.warn { "Using default API base URL: $defaultUrl" }
            return defaultUrl
        }

    fun setBaseUrl(url: String) {
        _baseUrl = url
    }

    const val CARDS_PATH = "/api/cards"
    const val TAGS_PATH = "/api/tags"
    const val TEMPLATES_PATH = "/api/templates"
    const val USERS_PATH = "/api/users"
    const val STATISTICS_PATH = "/api/statistics"

    const val CONNECT_TIMEOUT = 30_000L
    const val SOCKET_TIMEOUT = 30_000L
}
```

#### 5.1.2 KtorClientFactory 实现

**公共接口**：

```kotlin
package tech.zhifu.app.myhub.network

import io.ktor.client.engine.HttpClientEngine

expect class KtorClientFactory() {
    fun createEngine(): HttpClientEngine
}
```

**Android 实现**：

```kotlin
package tech.zhifu.app.myhub.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual class KtorClientFactory {
    actual fun createEngine(): HttpClientEngine {
        return Android.create()
    }
}
```

**iOS 实现**：

```kotlin
package tech.zhifu.app.myhub.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual class KtorClientFactory {
    actual fun createEngine(): HttpClientEngine {
        return Darwin.create()
    }
}
```

**JVM 实现**：

```kotlin
package tech.zhifu.app.myhub.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual class KtorClientFactory {
    actual fun createEngine(): HttpClientEngine {
        return CIO.create()
    }
}
```

**JS/WASM 实现**：

```kotlin
package tech.zhifu.app.myhub.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual class KtorClientFactory {
    actual fun createEngine(): HttpClientEngine {
        return Js.create()
    }
}
```

### 5.2 使用示例

#### 5.2.1 基本使用

```kotlin
import tech.zhifu.app.myhub.network.*
import io.ktor.client.call.body
import io.ktor.client.request.get

// 配置 API 基础 URL
ApiConfig.setBaseUrl("https://api.example.com")

// 创建 HttpClient
val factory = KtorClientFactory()
val httpClient = createHttpClient(factory)

// 发起请求
val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}")
val cards: List<CardDto> = response.body()
```

#### 5.2.2 在 Koin 中使用

```kotlin
import tech.zhifu.app.myhub.network.di.networkModule
import io.ktor.client.HttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

val appModule = module {
    includes(networkModule)

    single<CardService> {
        CardServiceImpl(httpClient = get())
    }
}

class CardServiceImpl(
    private val httpClient: HttpClient
) : CardService {
    suspend fun getCards(): List<CardDto> {
        val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}")
        return response.body()
    }
}
```

#### 5.2.3 错误处理

```kotlin
import tech.zhifu.app.myhub.network.ApiException
import tech.zhifu.app.myhub.network.NetworkException
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

suspend fun fetchData(): Result<DataDto> {
    return try {
        val response: HttpResponse = httpClient.get(url)
        
        if (response.status == HttpStatusCode.OK) {
            val data: DataDto = response.body()
            Result.success(data)
        } else {
            Result.failure(ApiException("API returned ${response.status}", null))
        }
    } catch (e: ApiException) {
        // 处理 API 错误（如 404, 500 等）
        logger.error(e) { "API error: ${e.message}" }
        Result.failure(e)
    } catch (e: NetworkException) {
        // 处理网络错误（如连接超时、无网络等）
        logger.error(e) { "Network error: ${e.message}" }
        Result.failure(e)
    } catch (e: Exception) {
        // 处理其他异常
        logger.error(e) { "Unexpected error: ${e.message}" }
        Result.failure(NetworkException("Unexpected error", e))
    }
}
```

### 5.3 测试策略

#### 5.3.1 单元测试

**ApiConfigTest**：

```kotlin
package tech.zhifu.app.myhub.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApiConfigTest {
    @Test
    fun testSetBaseUrl() {
        ApiConfig.setBaseUrl("https://api.example.com")
        assertEquals("https://api.example.com", ApiConfig.BASE_URL)
    }

    @Test
    fun testApiPaths() {
        assertEquals("/api/cards", ApiConfig.CARDS_PATH)
        assertEquals("/api/tags", ApiConfig.TAGS_PATH)
        assertEquals("/api/templates", ApiConfig.TEMPLATES_PATH)
        assertEquals("/api/users", ApiConfig.USERS_PATH)
        assertEquals("/api/statistics", ApiConfig.STATISTICS_PATH)
    }

    @Test
    fun testTimeouts() {
        assertEquals(30_000L, ApiConfig.CONNECT_TIMEOUT)
        assertEquals(30_000L, ApiConfig.SOCKET_TIMEOUT)
    }
}
```

**KtorClientFactoryTest**：

```kotlin
package tech.zhifu.app.myhub.network

import io.ktor.client.engine.HttpClientEngine
import kotlin.test.Test
import kotlin.test.assertNotNull

class KtorClientFactoryTest {
    @Test
    fun testCreateEngine() {
        val factory = KtorClientFactory()
        val engine = factory.createEngine()
        assertNotNull(engine)
    }
}
```

**NetworkExceptionTest**：

```kotlin
package tech.zhifu.app.myhub.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NetworkExceptionTest {
    @Test
    fun testApiException() {
        val exception = ApiException("API error", null)
        assertEquals("API error", exception.message)
        assertNotNull(exception)
    }

    @Test
    fun testNetworkException() {
        val cause = Exception("Original error")
        val exception = NetworkException("Network error", cause)
        assertEquals("Network error", exception.message)
        assertEquals(cause, exception.cause)
    }
}
```

---

## 6. 实施计划

### 6.1 实施阶段

#### 阶段 1：基础架构搭建（已完成）

- ✅ 创建 `core/network` 模块
- ✅ 定义公共接口（KtorClientFactory、ApiConfig）
- ✅ 实现 Android 平台支持
- ✅ 实现 iOS 平台支持
- ✅ 实现 JVM 平台支持
- ✅ 实现 JS 平台支持
- ✅ 实现 WASM 平台支持
- ✅ 实现 JSON 序列化配置
- ✅ 实现错误处理
- ✅ 实现依赖注入模块
- ✅ 编写单元测试

#### 阶段 2：文档完善（已完成）

- ✅ 创建架构设计文档
- ✅ 完善使用文档
- ✅ 添加代码示例
- ✅ 添加最佳实践指南

#### 阶段 3：功能扩展（持续进行）

- 🔄 添加认证支持（如需要）
- 🔄 添加请求重试机制（如需要）
- 🔄 添加请求缓存（如需要）
- 🔄 添加请求拦截器（如需要）

**说明**：功能扩展将根据实际需求持续进行，不设固定时间表。

### 6.2 里程碑

| 里程碑             | 目标日期   | 状态           |
| ------------------ | ---------- | -------------- |
| 基础架构完成       | 2026-01-13 | ✅ 已完成       |
| 文档完善           | 2026-01-13 | ✅ 已完成       |
| 功能扩展（如需要） | 持续进行   | 🔄 持续进行     |

---

## 7. 风险评估

### 7.1 技术风险

#### 风险 1：Ktor Client API 变更

**风险描述**：Ktor Client 仍在快速发展中，API 可能发生变更，影响模块实现。

**影响程度**：中

**应对措施**：

- 使用稳定的 Ktor Client API，避免使用实验性 API
- 定期更新 Ktor Client 版本，及时适配 API 变更
- 编写测试用例，确保模块实现正确
- 关注 Ktor Client 的更新日志和迁移指南

#### 风险 2：性能问题

**风险描述**：网络请求可能影响应用性能，特别是在移动平台上。

**影响程度**：低

**应对措施**：

- 使用高效的网络引擎（如 OkHttp、NSURLSession）
- 合理配置超时时间，避免长时间等待
- 使用协程进行异步请求，避免阻塞主线程
- 进行性能测试，确保不影响应用性能

### 7.2 维护风险

#### 风险 1：配置管理复杂

**风险描述**：API 配置管理可能变得复杂，特别是多环境配置。

**影响程度**：低

**应对措施**：

- 保持配置优先级清晰
- 使用 `AppBuildConfig` 统一管理配置
- 提供清晰的配置文档
- 定期审查配置管理逻辑

#### 风险 2：错误处理不完善

**风险描述**：错误处理可能不完善，导致用户体验差。

**影响程度**：中

**应对措施**：

- 提供统一的异常类型
- 编写错误处理最佳实践文档
- 在业务层统一处理错误
- 定期审查错误处理逻辑

### 7.3 兼容性风险

#### 风险 1：新平台支持

**风险描述**：未来可能需要支持新的平台（如 watchOS、tvOS 等）。

**影响程度**：低

**应对措施**：

- 使用 KMP 的 expect/actual 机制，易于添加新平台支持
- 保持接口稳定，新平台只需实现 actual 类
- 编写平台实现指南，降低新平台接入成本

#### 风险 2：序列化兼容性

**风险描述**：JSON 序列化配置可能与服务端不兼容。

**影响程度**：低

**应对措施**：

- 使用宽松的序列化配置（`ignoreUnknownKeys = true`、`isLenient = true`）
- 与服务端协商序列化规范
- 提供序列化配置文档

---

## 8. 附录

### 8.1 相关文档

- [MyHub 基础设施文档](../../../docs/myhub-infra.md)
- [MyHub 架构设计文档规范](../../../docs/myhub-infra-rules.md)
- [Ktor Client 官方文档](https://ktor.io/docs/client.html)
- [Kotlinx Serialization 官方文档](https://kotlinlang.org/docs/serialization.html)

### 8.2 参考实现

- `core/network` 模块实现代码
- 其他 core 模块的架构设计文档（如 `core/logger`、`core/platform`）

### 8.3 术语表

| 术语              | 说明                                           |
| ----------------- | ---------------------------------------------- |
| KMP               | Kotlin Multiplatform，Kotlin 多平台框架        |
| Ktor Client       | JetBrains 提供的跨平台 HTTP 客户端             |
| HttpClient        | Ktor Client 的 HTTP 客户端实例                 |
| HttpClientEngine  | Ktor Client 的网络引擎，平台特定实现           |
| expect/actual      | KMP 提供的跨平台抽象机制                       |
| ApiConfig         | API 配置对象，管理 API 基础 URL 和路径          |
| ApiException      | API 错误异常，API 返回错误状态码时抛出          |
| NetworkException  | 网络错误异常，网络连接错误时抛出                |
| NetworkModule     | Koin 依赖注入模块，提供 HttpClient 和 Factory   |

### 8.4 常见问题

#### Q1: 如何配置 API 基础 URL？

**A**: 可以通过以下方式配置：

1. **应用启动时设置**（推荐）：

   ```kotlin
   ApiConfig.setBaseUrl(AppBuildConfig.apiBaseUrl)
   ```

2. **系统属性**（Android/JVM）：

   ```bash
   -Pmyhub.api.base.url=https://api.example.com
   ```

3. **默认值**：`http://192.168.0.123:8083`

#### Q2: 如何处理网络错误？

**A**: 使用统一的异常类型：

```kotlin
try {
    val response = httpClient.get(url)
    val data: DataDto = response.body()
} catch (e: ApiException) {
    // 处理 API 错误（如 404, 500 等）
} catch (e: NetworkException) {
    // 处理网络错误（如连接超时、无网络等）
}
```

#### Q3: 如何添加自定义请求头？

**A**: 可以在创建 HttpClient 后添加插件，或在请求时添加：

```kotlin
// 方式 1：在请求时添加
val response = httpClient.get(url) {
    headers {
        append("Authorization", "Bearer $token")
    }
}

// 方式 2：添加插件（全局）
HttpClient(engine) {
    install(DefaultRequest) {
        headers {
            append("Authorization", "Bearer $token")
        }
    }
}
```

#### Q4: 如何测试网络请求？

**A**: 使用 Ktor Client Mock：

```kotlin
import io.ktor.client.engine.mock.*
import io.ktor.http.*

val mockEngine = MockEngine { request ->
    respond(
        content = """{"id": 1, "name": "Test"}""",
        status = HttpStatusCode.OK,
        headers = headersOf("Content-Type" to listOf("application/json"))
    )
}

val httpClient = HttpClient(mockEngine)
```

---

## 文档结束

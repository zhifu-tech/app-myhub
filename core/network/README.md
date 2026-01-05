# core:network

网络层实现模块，提供基于 Ktor Client 的 HTTP 客户端功能。

## 📋 功能特性

- ✅ **跨平台支持**：支持 Android、iOS、JVM、JS、WASM 平台
- ✅ **Ktor Client 集成**：使用 Ktor Client 进行类型安全的 HTTP 请求
- ✅ **JSON 序列化**：自动处理 JSON 序列化/反序列化
- ✅ **错误处理**：统一的异常处理机制（ApiException、NetworkException）
- ✅ **依赖注入**：提供 Koin DI 模块，便于集成
- ✅ **可配置 API**：支持动态配置 API 基础 URL
- ✅ **请求日志**：内置请求/响应日志记录
- ✅ **单元测试**：完整的单元测试覆盖

## 🎯 核心组件

### 1. ApiConfig

API 配置类，提供 API 基础 URL 和路径配置。

**主要功能：**

- 基础 URL 配置（支持动态设置）
- API 路径定义
- 超时配置

**URL 优先级：**

1. 通过 `setBaseUrl()` 设置的 URL
2. 系统属性 `myhub.api.base.url`（Android/JVM 平台）
3. 默认值 `http://192.168.0.123:8083`

**使用示例：**

```kotlin
// 设置自定义 API 基础 URL
ApiConfig.setBaseUrl("https://api.example.com")

// 获取当前 BASE_URL
val baseUrl = ApiConfig.BASE_URL

// 使用 API 路径常量
val cardsUrl = "${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}"
```

**API 路径常量：**

- `CARDS_PATH = "/api/cards"`
- `TAGS_PATH = "/api/tags"`
- `TEMPLATES_PATH = "/api/templates"`
- `USERS_PATH = "/api/users"`
- `STATISTICS_PATH = "/api/statistics"`

**超时配置：**

- `CONNECT_TIMEOUT = 30_000L` (30 秒)
- `SOCKET_TIMEOUT = 30_000L` (30 秒)

### 2. KtorClientFactory

跨平台的 Ktor Client 引擎工厂，各平台提供具体的引擎实现：

- **Android**: `Android` 引擎
- **iOS**: `Darwin` 引擎
- **Desktop (JVM)**: `CIO` 引擎
- **Web (JS/WASM)**: `Js` 引擎

**使用示例：**

```kotlin
val factory = KtorClientFactory()
val engine = factory.createEngine()
```

### 3. createHttpClient

创建配置好的 HttpClient 函数。

**配置内容：**

- **ContentNegotiation**: JSON 序列化/反序列化
    - `ignoreUnknownKeys = true` - 忽略未知字段
    - `isLenient = true` - 宽松模式
    - `encodeDefaults = true` - 编码默认值
- **DefaultRequest**: 默认请求头
    - `Content-Type: application/json`
    - `Accept: application/json`
- **Logging**: 请求/响应日志（INFO 级别）

**使用示例：**

```kotlin
val factory = KtorClientFactory()
val httpClient = createHttpClient(factory)
```

### 4. NetworkModule

Koin 依赖注入模块，提供：

- `KtorClientFactory` 单例
- `HttpClient` 单例

**使用示例：**

```kotlin
val appModule = module {
    includes(networkModule)

    // 使用 HttpClient
    single<MyService> {
        MyServiceImpl(httpClient = get())
    }
}
```

### 5. 异常类

#### ApiException

API 返回错误状态码时抛出。

```kotlin
throw ApiException("API error occurred", cause)
```

#### NetworkException

网络连接错误时抛出。

```kotlin
throw NetworkException("Network error occurred", cause)
```

## 📁 模块结构

```
core/network/
├── src/
│   ├── commonMain/
│   │   └── kotlin/tech/zhifu/app/myhub/network/
│   │       ├── ApiConfig.kt              # API 配置
│   │       ├── KtorClientFactory.kt      # Ktor Client 工厂（expect）
│   │       ├── NetworkException.kt       # 异常定义
│   │       └── di/
│   │           └── NetworkModule.kt      # Koin DI 模块
│   ├── androidMain/
│   │   └── kotlin/.../KtorClientFactory.android.kt
│   ├── iosMain/
│   │   └── kotlin/.../KtorClientFactory.ios.kt
│   ├── jvmMain/
│   │   └── kotlin/.../KtorClientFactory.jvm.kt
│   ├── jsMain/
│   │   └── kotlin/.../KtorClientFactory.js.kt
│   ├── wasmJsMain/
│   │   └── kotlin/.../KtorClientFactory.wasmJs.kt
│   └── commonTest/
│       └── kotlin/tech/zhifu/app/myhub/network/
│           ├── ApiConfigTest.kt          # ApiConfig 单元测试
│           ├── KtorClientFactoryTest.kt  # KtorClientFactory 单元测试
│           └── NetworkExceptionTest.kt   # 异常类单元测试
└── build.gradle.kts
```

## 🔧 依赖关系

### 依赖的模块

- `core:platform` - 平台相关功能（系统属性等）
- `core:logger` - 日志功能

### 外部依赖

- `ktor-client-core` - Ktor 客户端核心
- `ktor-client-content-negotiation` - 内容协商
- `ktor-serialization-kotlinx-json` - JSON 序列化
- `ktor-client-logging` - 日志插件
- `kotlinx-coroutines-core` - 协程支持
- `koin-core` - 依赖注入

### 平台特定依赖

- Android: `ktor-client-android`
- iOS: `ktor-client-darwin`
- JVM: `ktor-client-cio`
- JS/WASM: `ktor-client-js`

## 📝 使用示例

### 基本使用

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

### 在 Koin 中使用

```kotlin
import tech.zhifu.app.myhub.network.di.networkModule

val appModule = module {
    includes(networkModule)

    // 使用 HttpClient
    single<CardService> {
        CardServiceImpl(httpClient = get())
    }
}
```

### 错误处理

```kotlin
import tech.zhifu.app.myhub.network.ApiException
import tech.zhifu.app.myhub.network.NetworkException

try {
    val response = httpClient.get(url)
    val data: Data = response.body()
} catch (e: ApiException) {
    // 处理 API 错误（如 404, 500 等）
    println("API error: ${e.message}")
} catch (e: NetworkException) {
    // 处理网络错误（如连接超时、无网络等）
    println("Network error: ${e.message}")
}
```

## 🧪 测试

模块包含完整的单元测试：

- **ApiConfigTest**: 测试 API 配置功能
- **KtorClientFactoryTest**: 测试 HttpClient 创建
- **NetworkExceptionTest**: 测试异常类

运行测试：

```bash
./gradlew :core:network:test
```

## ⚠️ 注意事项

1. **超时配置**: 默认连接超时和请求超时都是 30 秒
2. **错误处理**: 所有网络操作都应该捕获 `NetworkException` 和 `ApiException`
3. **序列化**: 使用 `kotlinx.serialization` 进行 JSON 序列化
4. **日志**: 生产环境可以禁用或降低日志级别
5. **API URL 配置**: 建议在应用启动时通过 `ApiConfig.setBaseUrl()` 设置，而不是依赖默认值
6. **线程安全**: `ApiConfig.setBaseUrl()` 使用 `@Volatile` 保证线程安全

## 🔗 相关模块

- `core:network-test` - 网络测试工具模块，提供 Mock HttpClient
- `core:datastore-datasource-remote` - 远程数据源实现，使用本模块的 HttpClient

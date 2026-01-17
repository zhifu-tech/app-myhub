# MyHub 远程数据源模块方案设计

**方案名称**：Datastore Datasource Remote Infra v1  
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
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Datastore Datasource Remote Infra v1 的基线设计
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

| 版本   | 日期         | 修改内容            | 修改原因      |
|------|------------|-----------------|-----------|
| v1.0 | 2026-01-13 | 初始方案设计          | 新建        |
| v1.0 | 2026-01-13 | 完成架构设计文档        | 完善文档      |
| v1.0 | 2026-01-13 | 更新状态：评审通过、方案已锁定 | 状态更新：评审通过 |

---

## 1. 问题背景

### 1.1 用户场景

在 MyHub 应用的开发和运行过程中，需要与服务器 API 进行通信。典型的场景包括：

1. **数据同步**：从服务器获取数据并同步到本地
2. **数据上传**：将本地数据上传到服务器
3. **搜索功能**：在服务器端搜索数据
4. **用户认证**：用户登录和认证

### 1.2 问题根因

在引入统一的远程数据源模块之前，MyHub 应用面临以下问题：

1. **HTTP 请求逻辑分散**：各模块可能使用不同的方式发送 HTTP 请求，导致代码重复
2. **错误处理不一致**：不同模块的错误处理方式不一致
3. **序列化问题**：JSON 序列化/反序列化逻辑分散
4. **API 配置分散**：API 基础 URL 等配置分散在各处

### 1.3 影响范围

- **代码维护**：HTTP 请求逻辑分散，增加维护成本
- **错误处理**：错误处理不一致导致用户体验差
- **测试困难**：网络请求难以测试

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **统一的远程数据源接口**：提供统一的接口访问远程数据
- ✅ **Ktor Client 集成**：使用 Ktor Client 进行类型安全的 HTTP 请求
- ✅ **JSON 序列化**：自动处理 JSON 序列化/反序列化
- ✅ **错误处理**：统一的异常处理机制
- ✅ **依赖注入**：提供 Koin DI 模块，便于集成
- ✅ **可配置 API**：支持动态配置 API 基础 URL
- ✅ **跨平台支持**：支持所有平台（Android、iOS、JVM、JS、WASM）

### 2.2 非功能目标

- ✅ **类型安全**：使用 Kotlin 类型系统确保数据操作的类型安全
- ✅ **易于测试**：提供清晰的接口，易于 Mock
- ✅ **性能优化**：使用 Ktor Client 的连接池和缓存

### 2.3 模块特性说明

**重要说明**：`datastore/datasource-remote` 模块是一个**单一功能模块**，专注于远程数据源的实现。

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 HTTP 客户端：Ktor Client

**选择理由**：

- ✅ **KMP 原生支持**：Ktor Client 完全支持 KMP
- ✅ **平台特定引擎**：各平台提供对应的引擎实现
- ✅ **协程支持**：原生支持 Kotlin Coroutines
- ✅ **插件系统**：丰富的插件生态

**平台引擎选择**：

| 平台      | 引擎类型      | 说明                           |
|---------|-----------|------------------------------|
| Android | `Android` | 使用 Android HttpURLConnection |
| iOS     | `Darwin`  | 使用 NSURLSession              |
| JVM     | `CIO`     | 使用 Kotlin 协程 IO              |
| JS/WASM | `Js`      | 使用 Fetch API                 |

#### 3.1.2 JSON 序列化：kotlinx.serialization

**选择理由**：

- ✅ **KMP 原生支持**：kotlinx.serialization 完全支持 KMP
- ✅ **类型安全**：编译时生成序列化代码
- ✅ **Ktor 集成**：与 Ktor Client 完美集成

### 3.2 架构模式

#### 3.2.1 Repository 模式

**设计原则**：

- **DataSource 接口**：定义数据访问接口
- **DataSourceImpl**：实现数据访问逻辑
- **统一错误处理**：使用自定义异常类

**优势**：

- ✅ **关注点分离**：数据访问逻辑与业务逻辑分离
- ✅ **易于测试**：接口清晰，易于 Mock
- ✅ **易于扩展**：可以轻松添加新的数据源实现

---

## 4. 架构设计

### 4.1 模块结构

```text
datastore/datasource-remote/
├── src/
│   ├── commonMain/
│   │   └── kotlin/tech/zhifu/app/myhub/datastore/
│   │       ├── datasource/
│   │       │   ├── RemoteDataSource.kt              # 数据源接口定义
│   │       │   ├── di/
│   │       │   │   └── RemoteDataSourceModule.kt    # Koin DI 模块
│   │       │   └── impl/
│   │       │       ├── RemoteCardDataSourceImpl.kt
│   │       │       ├── RemoteTagDataSourceImpl.kt
│   │       │       ├── RemoteTemplateDataSourceImpl.kt
│   │       │       ├── RemoteUserDataSourceImpl.kt
│   │       │       └── RemoteStatisticsDataSourceImpl.kt
│   │       └── network/
│   │           ├── ApiConfig.kt                      # API 配置
│   │           ├── KtorClientFactory.kt             # Ktor Client 工厂
│   │           ├── NetworkException.kt               # 异常定义
│   │           └── di/
│   │               └── NetworkModule.kt              # 网络模块
│   ├── androidMain/
│   │   └── kotlin/.../KtorClientFactory.android.kt
│   ├── iosMain/
│   │   └── kotlin/.../KtorClientFactory.ios.kt
│   ├── jvmMain/
│   │   └── kotlin/.../KtorClientFactory.jvm.kt
│   ├── jsMain/
│   │   └── kotlin/.../KtorClientFactory.js.kt
│   └── wasmJsMain/
│       └── kotlin/.../KtorClientFactory.wasmJs.kt
└── build.gradle.kts
```

### 4.2 核心组件

#### 4.2.1 RemoteCardDataSource（卡片数据源）

**接口**：

```kotlin
interface RemoteCardDataSource {
    suspend fun getAllCards(): List<CardDto>
    suspend fun getCardById(id: String): CardDto?
    suspend fun searchCards(filter: SearchFilter): List<CardDto>
    suspend fun createCard(request: CreateCardRequest): CardDto
    suspend fun updateCard(id: String, request: UpdateCardRequest): CardDto
    suspend fun deleteCard(id: String)
    suspend fun toggleFavorite(id: String): CardDto
}
```

**实现**：`RemoteCardDataSourceImpl`

#### 4.2.2 ApiConfig（API 配置）

**功能**：

- 管理 API 基础 URL
- 提供 API 路径常量
- 支持动态配置

**定义**：

```kotlin
object ApiConfig {
    var BASE_URL: String = "http://localhost:8083"
    
    fun setBaseUrl(url: String) {
        BASE_URL = url
    }
    
    const val CARDS_PATH = "/api/cards"
    const val TAGS_PATH = "/api/tags"
    // ...
}
```

#### 4.2.3 KtorClientFactory（Ktor Client 工厂）

**功能**：

- 创建配置好的 Ktor Client 实例
- 配置 JSON 序列化
- 配置日志记录
- 配置超时设置

**平台实现**：

- 各平台使用对应的引擎（Android、Darwin、CIO、Js）

#### 4.2.4 NetworkException（网络异常）

**类型**：

- `ApiException`：API 错误（如 404、500）
- `NetworkException`：网络错误（如连接超时、无网络）

---

## 5. 实现细节

### 5.1 HTTP 请求实现

**示例：RemoteCardDataSourceImpl**：

```kotlin
class RemoteCardDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteCardDataSource {
    
    override suspend fun getAllCards(): List<CardDto> {
        return httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}")
            .body<List<CardDto>>()
    }
    
    override suspend fun createCard(request: CreateCardRequest): CardDto {
        return httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<CardDto>()
    }
}
```

### 5.2 错误处理

**实现**：

```kotlin
suspend fun <T> handleRequest(block: suspend () -> T): T {
    return try {
        block()
    } catch (e: ClientRequestException) {
        throw ApiException("API error: ${e.response.status}", e)
    } catch (e: IOException) {
        throw NetworkException("Network error", e)
    }
}
```

**使用**：

```kotlin
override suspend fun getAllCards(): List<CardDto> {
    return handleRequest {
        httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}")
            .body<List<CardDto>>()
    }
}
```

### 5.3 Ktor Client 配置

**配置**：

```kotlin
fun createHttpClient(): HttpClient {
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
}
```

---

## 6. 实施计划

### 6.1 实施阶段

#### 阶段 1：核心数据源实现（已完成）

- ✅ RemoteCardDataSource 实现
- ✅ RemoteTagDataSource 实现
- ✅ RemoteTemplateDataSource 实现
- ✅ RemoteUserDataSource 实现
- ✅ RemoteStatisticsDataSource 实现

#### 阶段 2：网络模块实现（已完成）

- ✅ ApiConfig 实现
- ✅ KtorClientFactory 实现
- ✅ NetworkException 实现

#### 阶段 3：依赖注入集成（已完成）

- ✅ RemoteDataSourceModule 实现
- ✅ NetworkModule 实现
- ✅ Koin 集成

### 6.2 里程碑

| 里程碑     | 目标日期       | 状态    |
|---------|------------|-------|
| 核心数据源完成 | 2026-01-13 | ✅ 已完成 |
| 网络模块完成  | 2026-01-13 | ✅ 已完成 |
| 依赖注入完成  | 2026-01-13 | ✅ 已完成 |

---

## 7. 风险评估

### 7.1 技术风险

#### 7.1.1 网络错误处理风险

**风险描述**：网络请求可能失败，需要妥善处理错误

**影响**：高

**缓解措施**：

- ✅ 统一的异常处理机制
- ✅ 重试机制
- ✅ 错误日志记录

#### 7.1.2 API 兼容性风险

**风险描述**：API 变更可能导致客户端错误

**影响**：中

**缓解措施**：

- ✅ 版本化 API
- ✅ 向后兼容
- ✅ 错误处理

### 7.2 业务风险

#### 7.2.1 数据同步风险

**风险描述**：本地和远程数据可能不一致

**影响**：中

**缓解措施**：

- ✅ 数据同步策略
- ✅ 冲突解决机制
- ✅ 数据校验

---

## 8. 附录

### 8.1 相关文档

- [MyHub 网络层模块方案设计](../../network/docs/myhub-network-infra-v1.0.md)
- [MyHub 数据模型模块方案设计](../datastore-model/docs/myhub-datastore-model-infra-v1.0.md)
- [Ktor Client 官方文档](https://ktor.io/docs/client.html)

### 8.2 代码示例

#### 8.2.1 基本使用

```kotlin
class CardRepository(
    private val cardDataSource: RemoteCardDataSource
) {
    suspend fun getAllCards(): List<CardDto> {
        return cardDataSource.getAllCards()
    }
    
    suspend fun createCard(title: String, content: String): CardDto {
        val request = CreateCardRequest(
            type = "quote",
            title = title,
            content = content
        )
        return cardDataSource.createCard(request)
    }
}
```

### 8.3 术语表

| 术语               | 说明                         |
|------------------|----------------------------|
| RemoteDataSource | 远程数据源接口，负责与服务器 API 通信      |
| Ktor Client      | Kotlin 官方 HTTP 客户端库，支持 KMP |
| ApiException     | API 错误异常，表示服务器返回错误状态码      |
| NetworkException | 网络错误异常，表示网络连接问题            |

### 8.4 常见问题

#### Q1: 如何配置 API 基础 URL？

**A**: 使用 `ApiConfig.setBaseUrl(url)` 方法，或在应用启动时通过系统属性配置。

#### Q2: 如何处理网络错误？

**A**: 使用 `try-catch` 捕获 `ApiException` 和 `NetworkException`，根据错误类型进行处理。

---

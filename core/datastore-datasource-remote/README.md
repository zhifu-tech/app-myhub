# core:datastore-datasource-remote

远程数据源实现模块，提供基于 Ktor Client 的 HTTP API 通信功能。

## 📋 功能特性

- ✅ **跨平台支持**：支持 Android、iOS、JVM、JS、WASM 平台
- ✅ **Ktor Client 集成**：使用 Ktor Client 进行类型安全的 HTTP 请求
- ✅ **JSON 序列化**：自动处理 JSON 序列化/反序列化
- ✅ **错误处理**：统一的异常处理机制
- ✅ **依赖注入**：提供 Koin DI 模块，便于集成
- ✅ **可配置 API**：支持动态配置 API 基础 URL
- ✅ **请求日志**：内置请求/响应日志记录

## 🎯 支持的数据源

### 1. RemoteCardDataSource（卡片数据源）

提供卡片的 CRUD 操作和搜索功能。

**接口方法：**

- `getAllCards(): List<CardDto>` - 获取所有卡片
- `getCardById(id: String): CardDto?` - 根据 ID 获取卡片
- `searchCards(filter: SearchFilter): List<CardDto>` - 搜索卡片
- `createCard(request: CreateCardRequest): CardDto` - 创建卡片
- `updateCard(id: String, request: UpdateCardRequest): CardDto` - 更新卡片
- `deleteCard(id: String)` - 删除卡片
- `toggleFavorite(id: String): CardDto` - 切换收藏状态

**实现类：** `RemoteCardDataSourceImpl`

### 2. RemoteTagDataSource（标签数据源）

提供标签的 CRUD 操作。

**接口方法：**

- `getAllTags(): List<Tag>` - 获取所有标签
- `getTagById(id: String): Tag?` - 根据 ID 获取标签
- `createTag(tag: Tag): Tag` - 创建标签
- `updateTag(tag: Tag): Tag` - 更新标签
- `deleteTag(id: String)` - 删除标签

**实现类：** `RemoteTagDataSourceImpl`

### 3. RemoteTemplateDataSource（模板数据源）

提供模板的 CRUD 操作。

**接口方法：**

- `getAllTemplates(): List<Template>` - 获取所有模板
- `getTemplateById(id: String): Template?` - 根据 ID 获取模板
- `createTemplate(template: Template): Template` - 创建模板
- `updateTemplate(template: Template): Template` - 更新模板
- `deleteTemplate(id: String)` - 删除模板

**实现类：** `RemoteTemplateDataSourceImpl`

### 4. RemoteUserDataSource（用户数据源）

提供用户信息的获取和更新功能。

**接口方法：**

- `getCurrentUser(): User` - 获取当前用户
- `updateUser(user: User): User` - 更新用户信息

**实现类：** `RemoteUserDataSourceImpl`

### 5. RemoteStatisticsDataSource（统计数据源）

提供统计数据的获取功能。

**接口方法：**

- `getStatistics(): Statistics` - 获取统计数据

**实现类：** `RemoteStatisticsDataSourceImpl`

## 📁 模块结构

```
core/datastore-datasource-remote/
├── src/
│   ├── commonMain/
│   │   └── kotlin/
│   │       └── tech/zhifu/app/myhub/datastore/
│   │           ├── datasource/
│   │           │   ├── RemoteDataSource.kt              # 数据源接口定义
│   │           │   ├── di/
│   │           │   │   └── RemoteDataSourceModule.kt    # Koin DI 模块
│   │           │   └── impl/
│   │           │       ├── RemoteCardDataSourceImpl.kt
│   │           │       ├── RemoteTagDataSourceImpl.kt
│   │           │       ├── RemoteTemplateDataSourceImpl.kt
│   │           │       ├── RemoteUserDataSourceImpl.kt
│   │           │       └── RemoteStatisticsDataSourceImpl.kt
│   │           └── network/
│   │               ├── ApiConfig.kt                      # API 配置
│   │               ├── KtorClientFactory.kt             # Ktor Client 工厂
│   │               ├── NetworkException.kt               # 异常定义
│   │               └── di/
│   │                   └── NetworkModule.kt              # 网络模块
│   ├── androidMain/
│   │   └── kotlin/
│   │       └── .../KtorClientFactory.android.kt
│   ├── iosMain/
│   │   └── kotlin/
│   │       └── .../KtorClientFactory.ios.kt
│   ├── jvmMain/
│   │   └── kotlin/
│   │       └── .../KtorClientFactory.jvm.kt
│   ├── jsMain/
│   │   └── kotlin/
│   │       └── .../KtorClientFactory.js.kt
│   ├── wasmJsMain/
│   │   └── kotlin/
│   │       └── .../KtorClientFactory.wasmJs.kt
│   └── commonTest/
│       └── kotlin/
│           └── tech/zhifu/app/myhub/datastore/datasource/
│               ├── RemoteCardDataSourceTest.kt
└── build.gradle.kts
```

## 🔧 依赖关系

### 依赖的模块

- `core:datastore-model` - 数据模型定义
- `core:platform` - 平台相关功能（系统属性等）
- `core:logger` - 日志功能

### 依赖的库

- `ktor-client-core` - Ktor Client 核心库
- `ktor-client-content-negotiation` - 内容协商插件
- `ktor-serialization-kotlinx-json` - JSON 序列化
- `ktor-client-logging` - 日志插件
- `kotlinx-coroutines-core` - 协程支持
- `kotlinx-serialization-json` - JSON 序列化
- `koin-core` - 依赖注入

### 平台特定依赖

- **Android**: `ktor-client-android`
- **iOS**: `ktor-client-darwin`
- **JVM**: `ktor-client-cio`
- **JS/WASM**: `ktor-client-js`

## 📖 使用示例

### 1. 依赖注入配置

在 Koin 模块中引入 `remoteDataSourceModule`：

```kotlin
import tech.zhifu.app.myhub.datastore.datasource.di.remoteDataSourceModule

startKoin {
    modules(
        // ... 其他模块
        remoteDataSourceModule
    )
}
```

### 2. 配置 API 基础 URL

在应用启动时配置 API 基础 URL：

```kotlin
import tech.zhifu.app.myhub.network.ApiConfig

// 在应用初始化时调用
ApiConfig.setBaseUrl("https://api.example.com")
```

或者通过系统属性配置（Android 平台）：

```kotlin
// 在 AndroidManifest.xml 中设置
<meta-data
    android:name="myhub.api.base.url"
    android:value="https://api.example.com" />
```

### 3. 使用卡片数据源

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
            content = content,
            author = "User",
            tags = emptyList()
        )
        return cardDataSource.createCard(request)
    }

    suspend fun searchCards(query: String): List<CardDto> {
        val filter = SearchFilter(
            query = query,
            cardTypes = emptyList(),
            tags = emptyList(),
            isFavorite = null,
            isTemplate = null,
            sortBy = SortBy.CREATED_AT_DESC
        )
        return cardDataSource.searchCards(filter)
    }
}
```

### 4. 错误处理

```kotlin
class CardViewModel(
    private val cardDataSource: RemoteCardDataSource
) {
    suspend fun loadCards() {
        try {
            val cards = cardDataSource.getAllCards()
            // 处理成功结果
        } catch (e: ApiException) {
            // 处理 API 错误（如 404, 500 等）
            when {
                e.message?.contains("404") == true -> {
                    // 处理未找到资源
                }
                e.message?.contains("500") == true -> {
                    // 处理服务器错误
                }
            }
        } catch (e: NetworkException) {
            // 处理网络错误（如连接超时、无网络等）
            // 可以提示用户检查网络连接
        }
    }
}
```

### 5. 在 ViewModel 中使用

```kotlin
class DashboardViewModel(
    private val cardDataSource: RemoteCardDataSource
) : ViewModel() {
    
    private val _cards = MutableStateFlow<List<CardDto>>(emptyList())
    val cards: StateFlow<List<CardDto>> = _cards.asStateFlow()

    fun loadCards() {
        viewModelScope.launch {
            try {
                _cards.value = cardDataSource.getAllCards()
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
}
```

## 🌐 API 端点

所有 API 端点基于 `ApiConfig.BASE_URL`：

### 卡片相关

- `GET /api/cards` - 获取所有卡片
- `GET /api/cards/{id}` - 获取指定卡片
- `GET /api/cards/search?q=...&types=...&tags=...&favorite=...&template=...&sort=...` - 搜索卡片
- `POST /api/cards` - 创建卡片
- `PUT /api/cards/{id}` - 更新卡片
- `DELETE /api/cards/{id}` - 删除卡片
- `POST /api/cards/{id}/favorite` - 切换收藏状态

### 标签相关

- `GET /api/tags` - 获取所有标签
- `GET /api/tags/{id}` - 获取指定标签
- `POST /api/tags` - 创建标签
- `PUT /api/tags/{id}` - 更新标签
- `DELETE /api/tags/{id}` - 删除标签

### 模板相关

- `GET /api/templates` - 获取所有模板
- `GET /api/templates/{id}` - 获取指定模板
- `POST /api/templates` - 创建模板
- `PUT /api/templates/{id}` - 更新模板
- `DELETE /api/templates/{id}` - 删除模板

### 用户相关

- `GET /api/users` - 获取当前用户
- `PUT /api/users` - 更新用户信息

### 统计相关

- `GET /api/statistics` - 获取统计数据

## 🔄 数据流

```
Repository Layer
    ↓
RemoteDataSource (Interface)
    ↓
RemoteDataSourceImpl (Implementation)
    ↓
HttpClient (Ktor Client)
    ↓
HTTP Request
    ↓
API Server
```

## ⚙️ 配置说明

### ApiConfig

`ApiConfig` 对象提供 API 配置功能：

- **BASE_URL**: API 基础 URL，优先级：
  1. 通过 `setBaseUrl()` 设置的 URL
  2. 系统属性 `myhub.api.base.url`
  3. 默认值 `http://localhost:8083`

- **API 路径常量**：
  - `CARDS_PATH = "/api/cards"`
  - `TAGS_PATH = "/api/tags"`
  - `TEMPLATES_PATH = "/api/templates"`
  - `USERS_PATH = "/api/users"`
  - `STATISTICS_PATH = "/api/statistics"`

- **超时配置**：
  - `CONNECT_TIMEOUT = 30_000L` (30秒)
  - `SOCKET_TIMEOUT = 30_000L` (30秒)

### HttpClient 配置

创建的 `HttpClient` 包含以下配置：

- **ContentNegotiation**: JSON 序列化/反序列化
  - `ignoreUnknownKeys = true` - 忽略未知键
  - `isLenient = true` - 宽松模式
  - `encodeDefaults = true` - 编码默认值

- **DefaultRequest**: 默认请求头
  - `Content-Type: application/json`
  - `Accept: application/json`

- **Logging**: 请求/响应日志（INFO 级别）

## 🧪 测试

模块包含完整的单元测试，使用 Ktor Client Mock 引擎进行测试。

### 运行测试

```bash
# 运行所有平台的测试
./gradlew :core:datastore-datasource-remote:allTests

# 运行特定平台的测试
./gradlew :core:datastore-datasource-remote:jvmTest
./gradlew :core:datastore-datasource-remote:jsTest
./gradlew :core:datastore-datasource-remote:iosSimulatorArm64Test
```

### 测试示例

```kotlin
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest

class RemoteCardDataSourceTest {
    @Test
    fun `test getAllCards success`() = runTest {
        // Given
        val httpClient = createMockHttpClient { request ->
            respond(
                content = Json.encodeToString(expectedCards),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource = RemoteCardDataSourceImpl(httpClient)

        // When
        val result = dataSource.getAllCards()

        // Then
        assertEquals(2, result.size)
    }
}
```

## 🚨 错误处理

### ApiException

当 API 返回错误状态码时抛出：

```kotlin
try {
    val card = cardDataSource.getCardById("999")
} catch (e: ApiException) {
    // 处理 API 错误（如 404, 500 等）
    when {
        e.message?.contains("404") == true -> {
            // 资源未找到
        }
        e.message?.contains("500") == true -> {
            // 服务器错误
        }
    }
}
```

### NetworkException

当网络连接错误时抛出：

```kotlin
try {
    val cards = cardDataSource.getAllCards()
} catch (e: NetworkException) {
    // 处理网络错误（如连接超时、无网络等）
    // 可以提示用户检查网络连接
}
```

## 📝 注意事项

1. **API URL 配置**：建议在应用启动时通过 `ApiConfig.setBaseUrl()` 设置正确的 API 基础 URL
2. **错误处理**：所有网络操作都可能抛出异常，需要妥善处理
3. **协程支持**：所有数据操作都是挂起函数，需要在协程中调用
4. **JSON 序列化**：确保数据模型正确实现 `@Serializable` 注解
5. **平台特定引擎**：各平台需要提供对应的 Ktor Client 引擎实现
6. **超时处理**：默认超时时间为 30 秒，可根据需要调整

## 🔗 相关模块

- `core:datastore-model` - 数据模型定义
- `core:datastore-datasource-local` - 本地数据源实现
- `core:platform` - 平台相关功能
- `core:logger` - 日志功能

## 📚 技术栈

- **Kotlin Multiplatform** - 跨平台支持
- **Ktor Client** - HTTP 客户端库
- **Kotlin Coroutines** - 异步操作支持
- **Kotlinx Serialization** - JSON 序列化
- **Koin** - 依赖注入框架


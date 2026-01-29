# datastore:datasource-local

本地数据源实现模块，提供基于 SQLDelight 的本地数据存储功能。

## 命名约定

- 所有 DataSource 接口与文件名统一为 `*DataSource`（驼峰 DataSource），例如 `LocalUserDataSource.kt`、`LocalCardDataSource.kt`。

## 测试策略

- 上层（Repository/Store）推荐使用 **Fake** 实现（如 `FakeLocalCardDataSource`）注入；本模块测试可使用 SQLDelight 的 **in-memory driver** 对重点 CRUD 与 observe* 做集成测试。详见 [Datasource 代码组织改进计划](../../docs/datasource-code-organization-improvement-plan.md) 第 3.5.1 节。

## 📋 功能特性

- ✅ **跨平台支持**：支持 Android、iOS、JVM、JS、WASM 平台
- ✅ **SQLDelight 集成**：使用 SQLDelight 进行类型安全的数据库操作
- ✅ **响应式数据流**：提供 `Flow` 支持，实现数据变更的实时监听
- ✅ **用户隔离**：所有数据操作都基于 `userId` 进行隔离
- ✅ **依赖注入**：提供 Koin DI 模块，便于集成
- ✅ **类型安全**：使用 Kotlin 类型系统确保数据操作的类型安全

## 🎯 支持的数据源

### 1. LocalCardDataSource（卡片数据源）

提供卡片的 CRUD 操作和实时监听功能。

**接口方法：**

- `getAllCards(userId: String): List<Card>` - 获取所有卡片
- `getCardById(id: String, userId: String): Card?` - 根据 ID 获取卡片
- `insertCard(card: Card, userId: String)` - 插入卡片
- `updateCard(card: Card, userId: String)` - 更新卡片
- `deleteCard(id: String, userId: String)` - 删除卡片
- `deleteAllCards(userId: String)` - 删除所有卡片
- `observeCards(userId: String): Flow<List<Card>>` - 监听卡片列表变化

**实现类：** `LocalCardDataSourceImpl`

### 2. LocalTagDataSource（标签数据源）

提供标签的 CRUD 操作和实时监听功能。

**接口方法：**

- `getAllTags(userId: String): List<Tag>` - 获取所有标签
- `getTagById(id: String, userId: String): Tag?` - 根据 ID 获取标签
- `getTagByName(name: String, userId: String): Tag?` - 根据名称获取标签
- `insertTag(tag: Tag, userId: String)` - 插入标签
- `updateTag(tag: Tag, userId: String)` - 更新标签
- `deleteTag(id: String, userId: String)` - 删除标签
- `observeTags(userId: String): Flow<List<Tag>>` - 监听标签列表变化

**实现类：** `LocalTagDataSourceImpl`

### 3. LocalTemplateDataSource（模板数据源）

提供模板的 CRUD 操作和实时监听功能。

**接口方法：**

- `getAllTemplates(userId: String): List<Template>` - 获取所有模板
- `getTemplateById(id: String, userId: String): Template?` - 根据 ID 获取模板
- `insertTemplate(template: Template, userId: String)` - 插入模板
- `updateTemplate(template: Template, userId: String)` - 更新模板
- `deleteTemplate(id: String, userId: String)` - 删除模板
- `observeTemplates(userId: String): Flow<List<Template>>` - 监听模板列表变化

**实现类：** `LocalTemplateDataSourceImpl`

### 4. LocalUserDataSource（用户数据源）

提供用户信息的存储和实时监听功能。

**接口方法：**

- `getCurrentUser(): User?` - 获取当前用户
- `saveUser(user: User)` - 保存用户信息
- `clearUser()` - 清除用户信息
- `observeUser(): Flow<User?>` - 监听用户信息变化

**实现类：** `LocalUserDataSourceImpl`

### 5. LocalStatisticsDataSource（统计数据源）

提供统计数据的存储功能。

**接口方法：**

- `getStatistics(userId: String): Statistics?` - 获取统计数据
- `saveStatistics(statistics: Statistics, userId: String)` - 保存统计数据
- `clearStatistics(userId: String)` - 清除统计数据

**实现类：** `LocalStatisticsDataSourceImpl`

### 6. UserContextProvider（用户上下文提供者）

提供当前用户 ID 的获取功能，用于在数据访问时自动获取用户上下文。

**接口方法：**

- `getCurrentUserId(): String?` - 获取当前用户 ID，如果用户未登录则返回 null

**实现类：** `UserContextProviderImpl`

## 📁 模块结构

```
datastore/datasource-local/
├── src/
│   ├── commonMain/
│   │   └── kotlin/
│   │       └── tech/zhifu/app/myhub/datastore/datasource/
│   │           ├── LocalDataSource.kt              # 数据源接口定义
│   │           ├── UserContextProvider.kt         # 用户上下文提供者接口
│   │           ├── di/
│   │           │   └── LocalDataSourceModule.kt   # Koin DI 模块
│   │           └── impl/
│   │               ├── LocalCardDataSourceImpl.kt
│   │               ├── LocalTagDataSourceImpl.kt
│   │               ├── LocalTemplateDataSourceImpl.kt
│   │               ├── LocalUserDataSourceImpl.kt
│   │               ├── LocalStatisticsDataSourceImpl.kt
│   │               └── UserContextProviderImpl.kt
│   └── commonTest/
│       └── kotlin/
│           └── tech/zhifu/app/myhub/datastore/datasource/
│               ├── LocalCardDataSourceTest.kt
│               ├── LocalTagDataSourceTest.kt
│               ├── LocalTemplateDataSourceTest.kt
│               ├── LocalUserDataSourceTest.kt
│               └── LocalStatisticsDataSourceTest.kt
└── build.gradle.kts
```

## 🔧 依赖关系

### 依赖的模块

- `datastore:model` - 数据模型定义
- `datastore:database` - SQLDelight 数据库定义

### 依赖的库

- `kotlinx-coroutines-core` - 协程支持
- `kotlinx-serialization-json` - JSON 序列化
- `sqldelight-coroutines` - SQLDelight 协程扩展
- `koin-core` - 依赖注入

## 📖 使用示例

### 1. 依赖注入配置

在 Koin 模块中引入 `localDataSourceModule`：

```kotlin
import tech.zhifu.app.myhub.datastore.datasource.di.localDataSourceModule

startKoin {
    modules(
        // ... 其他模块
        localDataSourceModule
    )
}
```

### 2. 使用卡片数据源

```kotlin
class CardRepository(
    private val cardDataSource: LocalCardDataSource
) {
    suspend fun getAllCards(userId: String): List<Card> {
        return cardDataSource.getAllCards(userId)
    }

    suspend fun insertCard(card: Card, userId: String) {
        cardDataSource.insertCard(card, userId)
    }

    fun observeCards(userId: String): Flow<List<Card>> {
        return cardDataSource.observeCards(userId)
    }
}
```

### 3. 使用用户上下文提供者

```kotlin
class SomeService(
    private val userContextProvider: UserContextProvider,
    private val cardDataSource: LocalCardDataSource
) {
    suspend fun getCurrentUserCards(): List<Card> {
        val userId = userContextProvider.getCurrentUserId()
            ?: throw IllegalStateException("User not logged in")
        return cardDataSource.getAllCards(userId)
    }
}
```

### 4. 响应式数据监听

```kotlin
class CardViewModel(
    private val cardDataSource: LocalCardDataSource,
    private val userId: String
) {
    val cards: Flow<List<Card>> = cardDataSource.observeCards(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
```

## 🧪 测试

模块包含完整的单元测试，使用 `datastore:database-test` 提供的测试工具。

### 运行测试

```bash
# 运行所有平台的测试
./gradlew :datastore:datasource-local:allTests

# 运行特定平台的测试
./gradlew :datastore:datasource-local:jvmTest
./gradlew :datastore:datasource-local:jsTest
./gradlew :datastore:datasource-local:iosSimulatorArm64Test
```

### 测试示例

```kotlin
import tech.zhifu.app.myhub.datastore.database.runDatabaseTest

class LocalCardDataSourceTest {
    @Test
    fun `test insert and get card`() = runDatabaseTest { database ->
        val dataSource = LocalCardDataSourceImpl(database)
        val card = Card(
            id = "card-1",
            type = CardType.QUOTE,
            title = "Test Card",
            // ... 其他字段
        )

        dataSource.insertCard(card, TEST_USER_ID)
        val retrieved = dataSource.getCardById("card-1", TEST_USER_ID)

        assertNotNull(retrieved)
        assertEquals(card.id, retrieved?.id)
    }
}
```

## 🔄 数据流

```
Repository Layer
    ↓
LocalDataSource (Interface)
    ↓
LocalDataSourceImpl (Implementation)
    ↓
MyHubDatabase (SQLDelight)
    ↓
SQLite Database
```

## 📝 注意事项

1. **用户隔离**：所有数据操作都需要提供 `userId` 参数，确保数据隔离
2. **事务处理**：复杂操作（如插入卡片及其关联数据）会自动使用事务
3. **响应式更新**：使用 `observe*` 方法可以实时监听数据变化
4. **空值处理**：查询方法可能返回 `null`，需要妥善处理
5. **协程支持**：所有数据操作都是挂起函数，需要在协程中调用

## 🔗 相关模块

- `datastore:model` - 数据模型定义
- `datastore:database` - SQLDelight 数据库定义
- `datastore:database-test` - 数据库测试工具
- `datastore:database-client` - 数据库驱动工厂

## 📚 技术栈

- **Kotlin Multiplatform** - 跨平台支持
- **SQLDelight** - 类型安全的 SQL 查询
- **Kotlin Coroutines** - 异步操作支持
- **Kotlin Flow** - 响应式数据流
- **Koin** - 依赖注入框架

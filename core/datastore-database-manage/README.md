# Database Manage Module

数据库管理模块，用于从 JSON 文件加载初始化数据到数据库。

## 📋 功能

- 从 JSON 文件加载数据并写入数据库
- 支持按表分类的数据文件（Card, Tag, User, Template）
- 支持从资源文件或文件系统加载数据
- 支持重复加载（可以多次写入）
- 支持清空数据后重新加载

## 🏗️ 模块结构

```
datastore-database-manage/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   └── DatabaseManager.kt          # 核心管理类
│   │   │   └── CardDataLoader.kt          # 卡片数据加载器
│   │   │   └── TagDataLoader.kt           # 标签数据加载器
│   │   │   └── UserDataLoader.kt          # 用户数据加载器
│   │   │   └── TemplateDataLoader.kt      # 模板数据加载器
│   │   └── resources/
│   │       └── database/
│   │           └── init/
│   │               ├── card.json          # 卡片数据
│   │               ├── tag.json            # 标签数据
│   │               ├── user.json           # 用户数据
│   │               └── template.json      # 模板数据
│   ├── androidMain/                       # Android 平台实现
│   ├── iosMain/                           # iOS 平台实现
│   ├── jvmMain/                           # JVM/Server 平台实现
│   └── jsMain/                            # JS 平台实现
```

## 🚀 使用方法

### 使用依赖注入（推荐）

```kotlin
import org.koin.core.context.GlobalContext
import tech.zhifu.app.myhub.datastore.database.manage.DatabaseManager
import tech.zhifu.app.myhub.datastore.database.manage.di.databaseManagerModule

// 在 Koin 初始化时添加模块
startKoin {
    modules(
        databaseModule,        // 数据库模块（提供 MyHubDatabase）
        databaseManagerModule  // 数据库管理器模块（提供 DatabaseManager）
    )
}

// 使用依赖注入获取实例
val databaseManager = GlobalContext.get().get<DatabaseManager>()

// 加载所有表的数据（从资源文件）
databaseManager.loadAllData()

// 加载指定表的数据
databaseManager.loadTableData("card")
```

### 直接创建实例

```kotlin
import tech.zhifu.app.myhub.datastore.database.manage.DatabaseManager
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

// 创建数据库管理器
val databaseManager = DatabaseManager(database)

// 加载所有表的数据（从资源文件）
databaseManager.loadAllData()

// 加载指定表的数据
databaseManager.loadTableData("card")

// 从文件路径加载数据
databaseManager.loadFromFile("/path/to/data/card.json", "card")
```

### 清空后重新加载

```kotlin
// 清空所有数据后重新加载
databaseManager.loadAllData(clearBeforeLoad = true)

// 清空指定表后重新加载
databaseManager.loadTableData("card", clearBeforeLoad = true)
```

### Android 平台特殊配置

在 Android 平台，需要先初始化 ResourceLoader：

```kotlin
import tech.zhifu.app.myhub.resource.ResourceLoader

// 在 Application 或 Activity 中初始化
ResourceLoader.init(context)
```

**注意**：`ResourceLoader` 位于 `core:platform` 模块中，可在所有模块中使用。

## 📝 JSON 数据格式

### Card JSON 格式

```json
{
  "cards": [
    {
      "id": "card-001",
      "type": "QUOTE",
      "title": "编程名言",
      "content": "代码是写给人看的，只是偶尔在机器上运行。",
      "author": "Harold Abelson",
      "tags": ["编程", "名言"],
      "isFavorite": true,
      "isTemplate": false,
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z",
      "metadata": {
        "quoteAuthor": "Harold Abelson",
        "quoteCategory": "Programming"
      }
    }
  ]
}
```

### Tag JSON 格式

```json
{
  "tags": [
    {
      "id": "tag-001",
      "name": "编程",
      "color": "#FF5733",
      "description": "与编程相关的卡片",
      "cardCount": 0,
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

### User JSON 格式

```json
{
  "users": [
    {
      "id": "user-001",
      "username": "default",
      "email": "user@example.com",
      "displayName": "Default User",
      "createdAt": "2024-01-01T00:00:00Z",
      "preferences": {
        "theme": "dark",
        "language": "zh",
        "defaultCardType": "QUOTE",
        "autoSync": true,
        "syncInterval": 3600000
      }
    }
  ]
}
```

### Template JSON 格式

```json
{
  "templates": [
    {
      "id": "template-001",
      "name": "引言模板",
      "description": "用于创建引言卡片的模板",
      "cardType": "QUOTE",
      "defaultContent": "在这里输入引言内容",
      "defaultTags": ["名言"],
      "usageCount": 0,
      "isSystemTemplate": true,
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

## 🔧 依赖

- `core:platform` - 平台模块（提供 ResourceLoader）
- `core:datastore-database` - 数据库模块（提供 MyHubDatabase）
- `core:datastore-model` - 数据模型模块
- `kotlinx-serialization-json` - JSON 序列化
- `kotlinx-datetime` - 日期时间处理
- `kotlinx-coroutines` - 协程支持
- `koin-core` - 依赖注入（用于 DI 模块）

## 📦 DI 模块

模块提供了 `databaseManagerModule`，可以在 Koin 中使用：

```kotlin
import tech.zhifu.app.myhub.datastore.database.manage.di.databaseManagerModule

startKoin {
    modules(
        databaseModule,        // 必须先提供 MyHubDatabase
        databaseManagerModule  // 提供 DatabaseManager
    )
}
```

`databaseManagerModule` 提供：
- `DatabaseManager` - 单例实例，依赖 `MyHubDatabase`

## 📌 注意事项

1. **数据加载顺序**：加载所有数据时，会按依赖顺序加载：User -> Tag -> Card -> Template
2. **重复加载**：可以多次调用加载方法，数据会被重复插入（除非使用 `clearBeforeLoad = true`）
3. **事务处理**：每个表的数据加载都在事务中完成，确保数据一致性
4. **平台差异**：
   - Android: 需要先调用 `ResourceLoader.init(context)`（ResourceLoader 位于 `core:platform` 模块）
   - JS: 资源加载需要特殊处理（可能需要 HTTP 请求）
   - iOS/JVM: 直接支持资源文件加载

## 🔮 后续扩展

该模块设计为可扩展的，后续可以添加：
- 数据导出功能
- 数据迁移功能
- 数据验证功能
- 批量操作功能


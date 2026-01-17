# datastore:database-client

数据库客户端驱动工厂模块，提供跨平台的数据库驱动创建功能。

## 📋 功能特性

- ✅ **跨平台支持**：支持 Android、iOS、JVM、JS、WASM 平台
- ✅ **统一接口**：通过 `expect/actual` 机制提供统一的 `DatabaseDriverFactory` 接口
- ✅ **依赖注入**：提供 Koin DI 模块，便于集成
- ✅ **自动初始化**：Web 平台（JS/WASM）自动处理数据库架构创建
- ✅ **外键支持**：自动启用外键约束以支持级联删除

## 🎯 支持的平台

- **Android** - 使用 `AndroidSqliteDriver`，数据库存储在应用私有目录
- **iOS** - 使用 `NativeSqliteDriver`，数据库存储在应用沙盒目录
- **JVM** - 使用 `JdbcSqliteDriver`，数据库存储在用户主目录（`~/.myhub/myhub.db`）
- **JS** - 使用 `WebWorkerDriver`（SQL.js），在 Web Worker 中运行
- **WASM** - 使用 `WebWorkerDriver`（SQL.js），在 Web Worker 中运行

## 📁 模块结构

```
datastore/database-client/
├── src/
│   ├── commonMain/
│   │   └── kotlin/
│   │       └── database/
│   │           ├── DatabaseDriverFactory.kt      # expect 接口定义
│   │           └── di/
│   │               └── DatabaseModule.kt        # Koin DI 模块
│   ├── androidMain/
│   │   └── kotlin/
│   │       └── database/
│   │           └── DatabaseDriverFactory.android.kt  # Android 实现
│   ├── iosMain/
│   │   └── kotlin/
│   │       └── database/
│   │           └── DatabaseDriverFactory.ios.kt      # iOS 实现
│   ├── jvmMain/
│   │   └── kotlin/
│   │       └── database/
│   │           └── DatabaseDriverFactory.jvm.kt     # JVM 实现
│   ├── jsMain/
│   │   └── kotlin/
│   │       └── database/
│   │           └── DatabaseDriverFactory.js.kt       # JS 实现
│   └── wasmJsMain/
│       └── kotlin/
│           └── database/
│               └── DatabaseDriverFactory.wasmJs.kt    # WASM 实现
└── build.gradle.kts
```

## 🚀 使用方法

### 使用依赖注入（推荐）

```kotlin
import org.koin.core.context.startKoin
import tech.zhifu.app.myhub.datastore.database.di.databaseModule
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

// 在应用启动时初始化 Koin
startKoin {
    modules(
        databaseModule  // 提供 DatabaseDriverFactory 和 MyHubDatabase
    )
}

// 在需要的地方注入使用
class MyRepository(
    private val database: MyHubDatabase
) {
    suspend fun getAllCards() {
        val cards = database.cardQueries.selectAll("user-1").awaitAsList()
        // ...
    }
}
```

### 直接创建实例

```kotlin
import tech.zhifu.app.myhub.datastore.database.DatabaseDriverFactory
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

// 创建驱动工厂
val driverFactory = DatabaseDriverFactory()

// 创建数据库实例
val database = MyHubDatabase(driverFactory.createDriver())

// 使用数据库
val cards = database.cardQueries.selectAll("user-1").awaitAsList()
```

### Android 平台特殊说明

Android 平台的 `DatabaseDriverFactory` 需要 `Context` 参数：

```kotlin
import android.content.Context
import tech.zhifu.app.myhub.datastore.database.DatabaseDriverFactory

// 创建驱动工厂（需要 Context）
val driverFactory = DatabaseDriverFactory(context)

// 创建数据库实例
val database = MyHubDatabase(driverFactory.createDriver())
```

在 Koin 中使用时，需要确保 `Context` 已经注册：

```kotlin
val androidModule = module {
    single<Context> { androidContext() }  // 提供 Context
    includes(databaseModule)  // 自动使用 Context 创建 DatabaseDriverFactory
}
```

## 🔧 平台特定实现

### Android

- **驱动类型**：`AndroidSqliteDriver`
- **数据库位置**：应用私有目录（`/data/data/<package>/databases/myhub.db`）
- **特点**：
    - 使用 Android 系统 SQLite
    - 自动处理数据库迁移
    - 需要 `Context` 参数

### iOS

- **驱动类型**：`NativeSqliteDriver`
- **数据库位置**：应用沙盒目录
- **特点**：
    - 使用 iOS 系统 SQLite
    - 自动处理数据库迁移
    - 无需额外参数

### JVM (Desktop)

- **驱动类型**：`JdbcSqliteDriver`
- **数据库位置**：用户主目录（`~/.myhub/myhub.db`）
- **特点**：
    - 使用 SQLite JDBC 驱动
    - 自动创建数据库目录
    - 自动处理数据库迁移

### JS (Web)

- **驱动类型**：`WebWorkerDriver`（使用 `createDefaultWebWorkerDriver()`）
- **数据库位置**：内存数据库（IndexedDB 持久化由 SQLDelight 处理）
- **特点**：
    - 在 Web Worker 中运行 SQL.js
    - 异步操作（必须使用 `await`）
    - 自动初始化数据库架构
    - 自动启用外键约束

### WASM (WebAssembly)

- **驱动类型**：`WebWorkerDriver`（使用 `createDefaultWebWorkerDriver()`）
- **数据库位置**：内存数据库（IndexedDB 持久化由 SQLDelight 处理）
- **特点**：
    - 在 Web Worker 中运行 SQL.js
    - 异步操作（必须使用 `await`）
    - 自动初始化数据库架构
    - 自动启用外键约束
    - 使用 `createDefaultWebWorkerDriver()` 避免 WASM JS interop 限制

## 📦 依赖

### Common 依赖

- `datastore:database` - 数据库 Schema 定义（提供 `MyHubDatabase`）
- `koin-core` - 依赖注入框架

### 平台特定依赖

- **Android**: `sqldelight-android` - SQLDelight Android 驱动
- **iOS**: `sqldelight-native` - SQLDelight Native 驱动
- **JVM**: `sqldelight-sqlite` - SQLDelight SQLite 驱动
- **JS**: `sqldelight-web` - SQLDelight Web 驱动
- **WASM**: `sqldelight-web` - SQLDelight Web 驱动

## 🔌 DI 模块

模块提供了 `databaseDriverFactoryModule()` 和 `databaseModule`：

### `databaseDriverFactoryModule()`

提供 `DatabaseDriverFactory` 单例实例：

```kotlin
val databaseDriverFactoryModule = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory()  // 或 DatabaseDriverFactory(context) for Android
    }
}
```

### `databaseModule`

提供 `MyHubDatabase` 单例实例，依赖 `DatabaseDriverFactory`：

```kotlin
val databaseModule = module {
    includes(databaseDriverFactoryModule())
    single<MyHubDatabase> {
        val driverFactory = get<DatabaseDriverFactory>()
        MyHubDatabase(driverFactory.createDriver())
    }
}
```

## ⚙️ 配置说明

### Web 平台（JS/WASM）

Web 平台使用 `createDefaultWebWorkerDriver()` 创建驱动，这是 SQLDelight 提供的便捷方法，会自动处理：

- Worker 脚本的加载
- 数据库架构的创建
- 外键约束的启用

**注意**：Web 平台的数据库操作都是异步的，必须使用 `await` 方法：

```kotlin
// ✅ 正确：使用 await
val cards = database.cardQueries.selectAll("user-1").awaitAsList()

// ❌ 错误：直接调用（返回 QueryResult，不是实际数据）
val cards = database.cardQueries.selectAll("user-1")  // 这是 QueryResult，不是 List
```

### Android 平台

Android 平台需要 `Context` 来访问应用私有目录：

```kotlin
// 在 Koin 中注册 Context
val androidModule = module {
    single<Context> { androidContext() }
    includes(databaseModule)
}
```

### JVM 平台

JVM 平台会在用户主目录创建 `.myhub` 目录，数据库文件存储在 `~/.myhub/myhub.db`：

```kotlin
// 数据库路径：~/.myhub/myhub.db
// 如果目录不存在，会自动创建
```

## 📌 注意事项

1. **Web 平台异步操作**：
    - JS/WASM 平台的所有数据库操作都是异步的
    - 必须使用 `awaitAsList()`, `awaitAsOne()`, `awaitAsOneOrNull()` 等方法
    - 不能直接使用同步方法

2. **数据库初始化**：
    - Web 平台（JS/WASM）会自动初始化数据库架构
    - 其他平台需要手动调用 `Schema.create()` 或依赖 SQLDelight 的自动迁移

3. **外键约束**：
    - Web 平台自动启用外键约束
    - 其他平台需要在创建数据库后手动启用：`PRAGMA foreign_keys = ON;`

4. **Android Context**：
    - Android 平台的 `DatabaseDriverFactory` 需要 `Context` 参数
    - 在 Koin 中使用时，确保 `Context` 已注册

5. **WASM JS Interop 限制**：
    - WASM 平台使用 `createDefaultWebWorkerDriver()` 避免 JS interop 限制
    - 这是 SQLDelight 2.1.0+ 推荐的 WASM 使用方式

## 🔗 相关模块

- `datastore:database` - 数据库 Schema 定义
- `datastore:database-test` - 数据库测试工具
- `datastore:datasource-local` - 本地数据源（使用此模块提供的数据库）

## 📚 参考文档

- [SQLDelight 官方文档](https://cashapp.github.io/sqldelight/)
- [SQLDelight Web 平台文档](https://cashapp.github.io/sqldelight/js_sqlite/)
- [Koin 官方文档](https://insert-koin.io/)


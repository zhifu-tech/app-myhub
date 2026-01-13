# MyHub 数据库客户端模块方案设计

**方案名称**：Datastore Database Client Infra v1  
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
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Datastore Database Client Infra v1 的基线设计
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

在 MyHub 应用的开发和运行过程中，需要创建和管理数据库驱动。典型的场景包括：

1. **数据库驱动创建**：不同平台需要不同的数据库驱动实现
2. **依赖注入集成**：需要与 Koin 等 DI 框架集成
3. **跨平台抽象**：需要统一的接口隐藏平台实现细节
4. **数据库初始化**：Web 平台需要自动初始化数据库架构

### 1.2 问题根因

在引入统一的数据库客户端模块之前，MyHub 应用面临以下问题：

1. **驱动创建分散**：各模块可能使用不同的方式创建数据库驱动，导致代码重复
2. **平台特定代码耦合**：业务代码直接使用平台特定的驱动，导致跨平台代码难以维护
3. **依赖注入配置分散**：数据库驱动的 DI 配置分散在各处，难以统一管理
4. **Web 平台特殊处理**：Web 平台的数据库初始化需要特殊处理

### 1.3 影响范围

- **代码维护**：驱动创建代码分散，增加维护成本
- **跨平台兼容性**：直接使用平台特定驱动导致代码难以在不同平台间复用
- **测试困难**：平台特定代码难以测试

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **统一的驱动工厂接口**：通过 `expect/actual` 机制提供统一的 `DatabaseDriverFactory` 接口
- ✅ **跨平台支持**：支持 Android、iOS、JVM、JS、WASM 平台
- ✅ **依赖注入集成**：提供 Koin DI 模块，便于集成
- ✅ **自动初始化**：Web 平台（JS/WASM）自动处理数据库架构创建
- ✅ **外键支持**：自动启用外键约束以支持级联删除

### 2.2 非功能目标

- ✅ **易于使用**：提供简洁的 API
- ✅ **平台抽象**：隐藏平台实现细节
- ✅ **性能优化**：使用平台原生的数据库驱动

### 2.3 模块特性说明

**重要说明**：`core/datastore-database-client` 模块是一个**单一功能模块**，专注于数据库驱动的创建和管理。

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 数据库驱动：SQLDelight

**选择理由**：

- ✅ **KMP 原生支持**：SQLDelight 完全支持 KMP
- ✅ **平台特定驱动**：各平台提供对应的驱动实现
- ✅ **类型安全**：编译时生成类型安全的查询接口

**平台驱动选择**：

| 平台 | 驱动类型 | 说明 |
| ---- | -------- | ---- |
| Android | `AndroidSqliteDriver` | 使用 Android 系统 SQLite |
| iOS | `NativeSqliteDriver` | 使用 iOS 系统 SQLite |
| JVM | `JdbcSqliteDriver` | 使用 SQLite JDBC 驱动 |
| JS | `WebWorkerDriver` | 在 Web Worker 中运行 SQL.js |
| WASM | `WebWorkerDriver` | 在 Web Worker 中运行 SQL.js |

### 3.2 架构模式

#### 3.2.1 expect/actual 模式

**设计原则**：

- **commonMain**：定义期望类 `DatabaseDriverFactory`
- **平台实现**：各平台提供具体实现
- **统一接口**：通过 `createDriver()` 方法提供统一的接口

**优势**：

- ✅ **平台抽象**：隐藏平台特定实现细节
- ✅ **统一 API**：业务代码使用统一的 API
- ✅ **易于扩展**：添加新平台只需实现 expect 类

---

## 4. 架构设计

### 4.1 模块结构

```text
core/datastore-database-client/
├── src/
│   ├── commonMain/
│   │   └── kotlin/tech/zhifu/app/myhub/datastore/database/
│   │       ├── DatabaseDriverFactory.kt      # expect 接口定义
│   │       └── di/
│   │           └── DatabaseModule.kt          # Koin DI 模块
│   ├── androidMain/
│   │   └── kotlin/.../DatabaseDriverFactory.android.kt
│   ├── iosMain/
│   │   └── kotlin/.../DatabaseDriverFactory.ios.kt
│   ├── jvmMain/
│   │   └── kotlin/.../DatabaseDriverFactory.jvm.kt
│   ├── jsMain/
│   │   └── kotlin/.../DatabaseDriverFactory.js.kt
│   └── wasmJsMain/
│       └── kotlin/.../DatabaseDriverFactory.wasmJs.kt
└── build.gradle.kts
```

### 4.2 核心组件

#### 4.2.1 DatabaseDriverFactory（期望类）

**定义**：

```kotlin
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
```

**功能**：

- 创建平台特定的数据库驱动
- 返回 `SqlDriver` 实例

**平台实现**：

- **Android**：使用 `AndroidSqliteDriver`，需要 `Context` 参数
- **iOS**：使用 `NativeSqliteDriver`，数据库存储在应用沙盒目录
- **JVM**：使用 `JdbcSqliteDriver`，数据库存储在用户主目录
- **JS/WASM**：使用 `WebWorkerDriver`，在 Web Worker 中运行

#### 4.2.2 DatabaseModule（Koin 模块）

**功能**：

- 提供 `DatabaseDriverFactory` 单例
- 提供 `MyHubDatabase` 单例

**定义**：

```kotlin
val databaseModule = module {
    includes(databaseDriverFactoryModule())
    single<MyHubDatabase> {
        val driverFactory = get<DatabaseDriverFactory>()
        MyHubDatabase(driverFactory.createDriver())
    }
}
```

### 4.3 平台特定实现

#### 4.3.1 Android 实现

**特点**：

- 使用 `AndroidSqliteDriver`
- 需要 `Context` 参数
- 数据库存储在应用私有目录

**实现**：

```kotlin
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = MyHubDatabase.Schema.synchronous(),
            context = context,
            name = "myhub.db"
        )
    }
}
```

#### 4.3.2 iOS 实现

**特点**：

- 使用 `NativeSqliteDriver`
- 数据库存储在应用沙盒目录
- 无需额外参数

#### 4.3.3 JVM 实现

**特点**：

- 使用 `JdbcSqliteDriver`
- 数据库存储在用户主目录（`~/.myhub/myhub.db`）
- 自动创建数据库目录

#### 4.3.4 JS/WASM 实现

**特点**：

- 使用 `WebWorkerDriver`（`createDefaultWebWorkerDriver()`）
- 在 Web Worker 中运行 SQL.js
- 自动初始化数据库架构
- 自动启用外键约束

---

## 5. 实现细节

### 5.1 数据库驱动创建

**Android**：

```kotlin
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = MyHubDatabase.Schema.synchronous(),
            context = context,
            name = "myhub.db"
        )
    }
}
```

**JVM**：

```kotlin
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbPath = File(System.getProperty("user.home"), ".myhub")
        dbPath.mkdirs()
        val dbFile = File(dbPath, "myhub.db")
        return JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
            .also { MyHubDatabase.Schema.create(it) }
    }
}
```

**JS/WASM**：

```kotlin
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = createDefaultWebWorkerDriver()
        MyHubDatabase.Schema.create(driver)
        driver.execute(null, "PRAGMA foreign_keys = ON", 0)
        return driver
    }
}
```

### 5.2 依赖注入配置

**Android**：

```kotlin
actual fun databaseDriverFactoryModule(): Module = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory(get<Context>())
    }
}
```

**其他平台**：

```kotlin
actual fun databaseDriverFactoryModule(): Module = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory()
    }
}
```

---

## 6. 实施计划

### 6.1 实施阶段

#### 阶段 1：核心功能实现（已完成）

- ✅ 定义 `DatabaseDriverFactory` 期望类
- ✅ Android 平台实现
- ✅ iOS 平台实现
- ✅ JVM 平台实现
- ✅ JS 平台实现
- ✅ WASM 平台实现

#### 阶段 2：依赖注入集成（已完成）

- ✅ `databaseDriverFactoryModule` 实现
- ✅ `databaseModule` 实现
- ✅ Koin 集成

### 6.2 里程碑

| 里程碑           | 目标日期   | 状态     |
| ---------------- | ---------- | -------- |
| 核心功能完成     | 2026-01-13 | ✅ 已完成 |
| 平台实现完成     | 2026-01-13 | ✅ 已完成 |
| 依赖注入完成     | 2026-01-13 | ✅ 已完成 |

---

## 7. 风险评估

### 7.1 技术风险

#### 7.1.1 Web 平台异步操作风险

**风险描述**：Web 平台的数据库操作都是异步的，必须使用 `await` 方法

**影响**：中

**缓解措施**：

- ✅ 文档说明 Web 平台异步操作要求
- ✅ 提供示例代码
- ✅ 编译时检查

#### 7.1.2 Android Context 依赖风险

**风险描述**：Android 平台需要 `Context` 参数，DI 配置需要确保 Context 已注册

**影响**：低

**缓解措施**：

- ✅ 文档说明 Android 平台特殊要求
- ✅ DI 模块自动处理 Context 依赖

### 7.2 维护风险

#### 7.2.1 平台特定代码维护

**风险描述**：不同平台的实现需要分别维护

**影响**：低

**缓解措施**：

- ✅ 使用 expect/actual 模式统一接口
- ✅ 充分的测试覆盖
- ✅ 文档说明平台差异

---

## 8. 附录

### 8.1 相关文档

- [MyHub 数据库模块方案设计](../datastore-database/docs/myhub-datastore-database-infra-v1.0.md)
- [MyHub 数据库测试模块方案设计](../datastore-database-test/docs/myhub-datastore-database-test-infra-v1.0.md)
- [SQLDelight 官方文档](https://cashapp.github.io/sqldelight/)

### 8.2 代码示例

#### 8.2.1 基本使用

```kotlin
val driverFactory = DatabaseDriverFactory()
val database = MyHubDatabase(driverFactory.createDriver())
```

#### 8.2.2 依赖注入使用

```kotlin
startKoin {
    modules(
        databaseModule  // 提供 DatabaseDriverFactory 和 MyHubDatabase
    )
}

class MyRepository(
    private val database: MyHubDatabase
) {
    suspend fun getAllCards() {
        val cards = database.cardQueries.selectAll("user-1").awaitAsList()
    }
}
```

### 8.3 术语表

| 术语           | 说明                                                         |
| -------------- | ------------------------------------------------------------ |
| DatabaseDriverFactory | 数据库驱动工厂，负责创建平台特定的数据库驱动                  |
| SqlDriver      | SQLDelight 的数据库驱动接口                                  |
| WebWorkerDriver | SQLDelight 提供的 Web 平台数据库驱动，在 Web Worker 中运行 |
| expect/actual  | Kotlin Multiplatform 的跨平台抽象机制                        |

### 8.4 常见问题

#### Q1: Android 平台为什么需要 Context？

**A**: Android 平台的 `AndroidSqliteDriver` 需要 `Context` 来访问应用私有目录，用于存储数据库文件。

#### Q2: Web 平台的数据库操作为什么是异步的？

**A**: Web 平台使用 Web Worker 运行 SQL.js，所有操作都是异步的，必须使用 `await` 方法。

#### Q3: 如何自定义数据库路径？

**A**: 修改对应平台的 `DatabaseDriverFactory` 实现，指定数据库文件路径。

---

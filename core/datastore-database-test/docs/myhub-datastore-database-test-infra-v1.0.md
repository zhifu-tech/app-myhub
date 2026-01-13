# MyHub 数据库测试模块方案设计

**方案名称**：Datastore Database Test Infra v1  
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
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Datastore Database Test Infra v1 的基线设计
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

在 MyHub 应用的开发和测试过程中，需要为数据库相关的单元测试提供测试工具。典型的场景包括：

1. **测试数据库创建**：每个测试用例需要独立的数据库实例，避免测试间相互影响
2. **测试隔离**：确保每个测试用例都有干净的数据环境
3. **跨平台测试**：在 KMP 项目中，测试工具需要在所有平台（Android、iOS、JVM、JS、WASM）上工作
4. **自动清理**：测试结束后自动清理数据库，避免手动清理的繁琐
5. **外键约束**：测试数据库需要启用外键约束以支持级联删除等操作

### 1.2 问题根因

在引入统一的数据库测试工具模块之前，MyHub 应用面临以下问题：

1. **测试数据库创建分散**：各测试模块可能使用不同的方式创建测试数据库，导致代码重复和不一致
2. **测试隔离不足**：测试用例之间可能共享数据库实例，导致测试相互影响
3. **平台特定代码**：不同平台需要不同的数据库驱动，缺乏统一的抽象
4. **清理机制不完善**：测试结束后需要手动清理数据，容易遗漏
5. **WASM 平台限制**：WASM 平台的 JS interop 限制导致数据库测试困难

### 1.3 影响范围

- **测试效率**：缺乏统一的测试工具导致测试编写效率低
- **测试可靠性**：测试隔离不足导致测试结果不可靠
- **代码维护**：测试代码重复，增加维护成本
- **跨平台兼容性**：不同平台的测试实现不一致

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **测试数据库创建**：为每个测试用例提供独立的测试数据库实例
- ✅ **跨平台支持**：支持所有平台（Android、iOS、JVM、JS、WASM）
- ✅ **自动清理**：测试结束后自动清理数据库数据
- ✅ **外键约束**：自动启用外键约束以支持级联删除等操作
- ✅ **内存数据库**：所有平台都使用内存数据库，不会持久化数据
- ✅ **测试隔离**：确保每个测试用例都有独立的数据库实例

### 2.2 非功能目标

- ✅ **易于使用**：提供简洁的 API，易于在测试中使用
- ✅ **性能优化**：使用内存数据库，测试执行速度快
- ✅ **平台兼容性**：处理不同平台的限制和差异

### 2.3 模块特性说明

**重要说明**：`core/datastore-database-test` 模块是一个**测试工具模块**，专门用于单元测试，不包含生产代码。

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 数据库驱动：SQLDelight

**选择理由**：

- ✅ **KMP 原生支持**：SQLDelight 完全支持 KMP
- ✅ **平台特定驱动**：各平台提供对应的驱动实现
- ✅ **内存数据库支持**：所有平台都支持内存数据库

**平台驱动选择**：

| 平台    | 驱动类型              | 说明                                    |
| ------- | --------------------- | --------------------------------------- |
| Android | `AndroidSqliteDriver` | 使用 Android 系统 SQLite                |
| iOS     | `NativeSqliteDriver`  | 使用 iOS 系统 SQLite                    |
| JVM     | `JdbcSqliteDriver`    | 使用 SQLite JDBC 驱动                   |
| JS      | `WebWorkerDriver`     | 在 Web Worker 中运行 SQL.js             |
| WASM    | `WebWorkerDriver`     | 在 Web Worker 中运行 SQL.js（有限支持） |

#### 3.1.2 测试框架：kotlinx.coroutines.test

**选择理由**：

- ✅ **协程支持**：支持异步测试
- ✅ **跨平台支持**：在所有平台上工作一致
- ✅ **简洁 API**：`runTest` 函数简洁易用

### 3.2 架构模式

#### 3.2.1 expect/actual 模式

**设计原则**：

- **commonMain**：定义期望函数 `createTestDatabase()` 和 `destroyTestDatabase()`
- **平台实现**：各平台提供具体实现
- **统一接口**：通过 `runDatabaseTest` 函数提供统一的测试接口

**优势**：

- ✅ **平台抽象**：隐藏平台特定实现细节
- ✅ **统一 API**：测试代码使用统一的 API
- ✅ **易于扩展**：添加新平台只需实现 expect 函数

---

## 4. 架构设计

### 4.1 模块结构

```text
core/datastore-database-test/
├── src/
│   ├── commonMain/
│   │   └── kotlin/tech/zhifu/app/myhub/datastore/database/
│   │       └── DatabaseTestHelper.kt      # 期望函数和测试辅助函数
│   ├── androidMain/
│   │   └── kotlin/.../DatabaseTestHelper.android.kt
│   ├── iosMain/
│   │   └── kotlin/.../DatabaseTestHelper.ios.kt
│   ├── jvmMain/
│   │   └── kotlin/.../DatabaseTestHelper.jvm.kt
│   ├── jsMain/
│   │   └── kotlin/.../DatabaseTestHelper.js.kt
│   ├── wasmJsMain/
│   │   └── kotlin/.../DatabaseTestHelper.wasmJs.kt
│   └── commonTest/
│       └── kotlin/.../DatabaseTest.kt      # 数据库基础功能测试
└── build.gradle.kts
```

### 4.2 核心组件

#### 4.2.1 createTestDatabase（期望函数）

**定义**：

```kotlin
expect suspend fun createTestDatabase(): MyHubDatabase
```

**功能**：

- 创建内存数据库实例
- 启用外键约束（`PRAGMA foreign_keys = ON`）
- 返回 `MyHubDatabase` 实例

**平台实现**：

- **Android/JVM**：使用 `JdbcSqliteDriver.IN_MEMORY`
- **iOS**：使用 `NativeSqliteDriver` 创建 `:memory:test_${随机数}`
- **JS/WASM**：使用 `WebWorkerDriver` 在 Web Worker 中运行

#### 4.2.2 destroyTestDatabase（期望函数）

**定义**：

```kotlin
expect fun destroyTestDatabase(database: MyHubDatabase)
```

**功能**：

- 关闭数据库驱动
- 销毁数据库实例

**平台实现**：

- **Android/JVM**：使用反射访问 driver 并关闭
- **iOS**：空实现（使用唯一的内存数据库名称确保隔离）
- **JS/WASM**：空实现（内存数据库自动销毁）

#### 4.2.3 runDatabaseTest（测试辅助函数）

**定义**：

```kotlin
fun runDatabaseTest(
    block: suspend CoroutineScope.(MyHubDatabase) -> Unit
) = runTest {
    val db = createTestDatabase()
    try {
        block(db)
    } finally {
        destroyTestDatabase(db)
    }
}
```

**功能**：

- 自动创建测试数据库
- 执行测试代码
- 测试结束后自动清理

**WASM 平台处理**：

- 自动检测 WASM 平台并跳过测试
- 避免 WASM JS interop 限制导致的错误

### 4.3 平台特定实现

#### 4.3.1 Android/JVM 实现

**特点**：

- 使用 `JdbcSqliteDriver.IN_MEMORY` 创建内存数据库
- 使用反射访问 driver 并关闭
- 每个连接都创建独立的内存数据库

#### 4.3.2 iOS 实现

**特点**：

- 使用 `NativeSqliteDriver` 创建 `:memory:test_${随机数}`
- 为每个测试生成唯一的内存数据库名称
- Kotlin/Native 不支持反射，使用唯一名称确保隔离

#### 4.3.3 JS/WASM 实现

**特点**：

- JS 平台：使用 `WebWorkerDriver` 完全支持数据库测试
- WASM 平台：数据库测试自动跳过（由于 JS interop 限制）
- 使用 `js()` 函数访问 JS 特定 API

---

## 5. 实现细节

### 5.1 测试数据库创建

**Android/JVM**：

```kotlin
actual suspend fun createTestDatabase(): MyHubDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    MyHubDatabase.Schema.create(driver)
    driver.execute(null, "PRAGMA foreign_keys = ON", 0)
    return MyHubDatabase(driver)
}
```

**iOS**：

```kotlin
actual suspend fun createTestDatabase(): MyHubDatabase {
    val randomId = (0..1000000).random()
    val driver = NativeSqliteDriver(
        MyHubDatabase.Schema,
        ":memory:test_$randomId"
    )
    driver.execute(null, "PRAGMA foreign_keys = ON", 0)
    return MyHubDatabase(driver)
}
```

**JS**：

```kotlin
actual suspend fun createTestDatabase(): MyHubDatabase {
    val driver = createDefaultWebWorkerDriver()
    MyHubDatabase.Schema.create(driver)
    driver.execute(null, "PRAGMA foreign_keys = ON", 0)
    return MyHubDatabase(driver)
}
```

### 5.2 测试数据库销毁

**Android/JVM**：

```kotlin
actual fun destroyTestDatabase(database: MyHubDatabase) {
    val driver = database.driver
    driver.close()
}
```

**iOS/JS/WASM**：

```kotlin
actual fun destroyTestDatabase(database: MyHubDatabase) {
    // 空实现：使用唯一的内存数据库名称确保隔离
    // 或内存数据库自动销毁
}
```

### 5.3 WASM 平台处理

**跳过机制**：

```kotlin
fun runDatabaseTest(
    block: suspend CoroutineScope.(MyHubDatabase) -> Unit
) = runTest {
    // WASM 平台跳过数据库测试
    if (isWasmPlatform()) {
        println("Skipping database test on WASM platform")
        return@runTest
    }
    // ... 正常测试逻辑
}
```

---

## 6. 实施计划

### 6.1 实施阶段

#### 阶段 1：核心功能实现（已完成）

- ✅ 定义 `createTestDatabase` 和 `destroyTestDatabase` 期望函数
- ✅ 实现 `runDatabaseTest` 测试辅助函数
- ✅ Android/JVM 平台实现
- ✅ iOS 平台实现
- ✅ JS 平台实现
- ✅ WASM 平台处理（跳过机制）

#### 阶段 2：测试完善（已完成）

- ✅ 数据库基础功能测试
- ✅ 外键约束测试
- ✅ 级联删除测试
- ✅ 用户数据隔离测试

### 6.2 里程碑

| 里程碑       | 目标日期   | 状态      |
| ------------ | ---------- | --------- |
| 核心功能完成 | 2026-01-13 | ✅ 已完成 |
| 平台实现完成 | 2026-01-13 | ✅ 已完成 |
| 测试完善完成 | 2026-01-13 | ✅ 已完成 |

---

## 7. 风险评估

### 7.1 技术风险

#### 7.1.1 WASM 平台限制

**风险描述**：WASM 平台的 JS interop 限制导致数据库测试困难

**影响**：中

**缓解措施**：

- ✅ 自动检测 WASM 平台并跳过测试
- ✅ 推荐使用 JS 平台进行 Web 数据库测试
- ✅ 文档说明 WASM 平台限制

#### 7.1.2 测试隔离风险

**风险描述**：测试用例之间可能共享数据库实例

**影响**：高

**缓解措施**：

- ✅ 每个测试用例都创建独立的数据库实例
- ✅ iOS 平台使用唯一的内存数据库名称
- ✅ 测试结束后自动清理

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
- [MyHub 数据库客户端模块方案设计](../datastore-database-client/docs/myhub-datastore-database-client-infra-v1.0.md)
- [SQLDelight 官方文档](https://cashapp.github.io/sqldelight/)

### 8.2 代码示例

#### 8.2.1 基本使用

```kotlin
@Test
fun `test insert card`() = runDatabaseTest { database ->
    val dataSource = LocalCardDataSourceImpl(database)

    val card = Card(
        id = "card-1",
        type = CardType.QUOTE,
        content = "Test content",
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now()
    )

    dataSource.insertCard(card, TEST_USER_ID)
    val result = dataSource.getCardById("card-1", TEST_USER_ID)

    assertNotNull(result)
    assertEquals("Test content", result?.content)
}
```

#### 8.2.2 测试事务

```kotlin
@Test
fun `test transaction rollback`() = runDatabaseTest { database ->
    database.transaction {
        database.cardQueries.insertCard(...)
        throw RuntimeException("Test rollback")
    }

    val result = database.cardQueries.selectById("card-1", TEST_USER_ID)
        .awaitAsOneOrNull()
    assertEquals(null, result)
}
```

### 8.3 术语表

| 术语            | 说明                                                       |
| --------------- | ---------------------------------------------------------- |
| 内存数据库      | 不持久化到磁盘的数据库，仅存在于内存中                     |
| 测试隔离        | 每个测试用例都有独立的数据库实例，互不影响                 |
| expect/actual   | Kotlin Multiplatform 的跨平台抽象机制                      |
| WebWorkerDriver | SQLDelight 提供的 Web 平台数据库驱动，在 Web Worker 中运行 |
| JS interop      | Kotlin/WASM 与 JavaScript 的互操作机制                     |

### 8.4 常见问题

#### Q1: 为什么 WASM 平台跳过数据库测试？

**A**: WASM 平台的 JS interop 限制导致无法可靠地访问 SQLDelight Driver 实例。推荐使用 JS 平台（`jsTest`）进行 Web 数据库测试。

#### Q2: iOS 平台如何确保测试隔离？

**A**: iOS 平台为每个测试生成唯一的内存数据库名称（`:memory:test_${随机数}`），确保测试隔离。

#### Q3: 如何手动创建和销毁测试数据库？

**A**: 可以使用 `createTestDatabase()` 和 `destroyTestDatabase()` 函数，但推荐使用 `runDatabaseTest` 函数自动管理。

---

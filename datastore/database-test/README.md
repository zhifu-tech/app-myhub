# datastore:database-test

数据库测试工具模块，提供跨平台的测试数据库创建和测试辅助函数。

## 📋 功能特性

- ✅ **测试数据库创建**：为每个测试用例提供独立的测试数据库实例
- ✅ **跨平台支持**：支持所有平台（Android、iOS、JVM、JS、WASM）
- ✅ **自动清理**：测试结束后自动清理数据库数据（通过关闭 driver 或使用唯一名称）
- ✅ **外键约束**：自动启用外键约束以支持级联删除等操作
- ✅ **内存数据库**：所有平台都使用内存数据库，不会持久化数据
- ✅ **测试隔离**：iOS 平台使用唯一的内存数据库名称确保测试隔离
- ⚠️ **WASM 支持**：WASM 平台数据库测试会自动跳过（由于 JS interop 限制）
- ✅ **平台特定实现**：JS 和 WASM 分别实现（webMain 有访问限制）

## 🎯 支持的平台

- **Android** - Android 平台（使用 JdbcSqliteDriver）
- **iOS** - iOS 平台（使用 NativeSqliteDriver）
- **JVM** - 桌面应用（使用 JdbcSqliteDriver）
- **JS** - Web 应用（使用 WebWorkerDriver）
- **WASM** - Web 应用（数据库测试自动跳过，推荐使用 JS 平台）

## 📁 模块结构

```text
datastore/database-test/src/
├── commonMain/
│   └── DatabaseTestHelper.kt      # 期望函数和测试辅助函数
├── jsMain/                         # JS 平台特定代码
│   └── DatabaseTestHelper.js.kt   # JS 平台测试数据库创建
├── wasmJsMain/                     # WASM 平台特定代码
│   └── DatabaseTestHelper.wasmJs.kt  # WASM 平台测试数据库创建
├── androidMain/
│   └── DatabaseTestHelper.android.kt
├── iosMain/
│   └── DatabaseTestHelper.ios.kt
├── jvmMain/
│   └── DatabaseTestHelper.jvm.kt
└── commonTest/
    └── DatabaseTest.kt             # 数据库基础功能测试
```

### 源集说明

- **commonMain**：期望函数和测试辅助函数
    - `createTestDatabase()` - 期望函数，各平台提供实现，创建内存数据库
    - `destroyTestDatabase()` - 期望函数，各平台提供实现，销毁数据库（关闭 driver）
    - `runDatabaseTest()` - 测试辅助函数，自动创建和清理数据库
- **jsMain**：JS 平台特定实现
    - `DatabaseTestHelper.js.kt` - 使用 WebWorkerDriver 创建测试数据库（使用 `js()` 函数）
- **wasmJsMain**：WASM 平台特定实现
    - `DatabaseTestHelper.wasmJs.kt` - 使用 WebWorkerDriver 创建测试数据库（使用 `js()` 函数）
- **androidMain**：Android 平台实现（使用 JdbcSqliteDriver）
- **iosMain**：iOS 平台实现（使用 NativeSqliteDriver，创建唯一的内存数据库名称）
- **jvmMain**：JVM 平台实现（使用 JdbcSqliteDriver）

## 🔧 主要 API

### runDatabaseTest

为每个测试用例提供独立的数据库实例，测试结束后自动清理：

```kotlin
@Test
fun `test insert card`() = runDatabaseTest { database ->
    val dataSource = LocalCardDataSourceImpl(database)

    // 测试代码...
    val card = dataSource.getCardById("card-1")
    assertEquals("Test Content", card?.content)
}
```

### createTestDatabase

直接创建测试数据库（不自动清理）：

```kotlin
@Test
fun `test custom database setup`() = runTest {
    val database = createTestDatabase()

    // 自定义测试逻辑...

    // 手动清理
    destroyTestDatabase(database)
}
```

### destroyTestDatabase

销毁测试数据库（关闭 driver）：

```kotlin
// 各平台实现方式不同：
// - Android/JVM: 使用反射访问 driver 并关闭
// - iOS: 空实现（使用唯一的内存数据库名称确保隔离）
// - JS/WASM: 空实现（内存数据库自动销毁）
destroyTestDatabase(database)
```

## 🧪 测试

模块包含完整的数据库功能测试：

- **DatabaseTest** - 数据库基础功能测试
    - Schema 创建测试
    - 插入和查询测试
    - 事务回滚测试
    - 外键约束测试
    - 级联删除测试
    - 用户数据隔离测试

### 运行测试

```bash
# 运行所有平台的测试
./gradlew :datastore:database-test:allTests

# 运行特定平台的测试
./gradlew :datastore:database-test:jvmTest
./gradlew :datastore:database-test:jsTest
# 注意：wasmJsTest 已被禁用（由于 JS interop 限制）
# 数据库测试在 WASM 平台会自动跳过，推荐使用 jsTest 进行 Web 数据库测试
./gradlew :datastore:database-test:iosSimulatorArm64Test
```

## 📝 设计说明

### JS/WASM 平台实现

- JS 和 WASM 分别实现，因为 `webMain` 无法访问 `js()` 等 JS 特定 API
- **JS 平台**：使用 `WebWorkerDriver`，完全支持数据库测试 ✅
- **WASM 平台**：由于 WASM 的 JS interop 限制，数据库测试会被自动跳过 ⚠️
    - WASM 的 JS interop 只支持 primitive、string 和 function 类型
    - `js()` 函数只能在顶层属性初始化器或顶层函数体中使用
    - 无法在 `suspend` 函数、`expect-actual` 或测试环境中可靠地访问 JS 全局对象
    - **推荐方案**：使用 JS 平台（`jsTest`）进行 Web 数据库测试
    - **WASM 测试**：只运行 UI、纯逻辑或 Fake DB 测试
- 使用 `js()` 函数访问 `import.meta.url` 等 JS 特定语法

#### WASM 测试跳过机制

WASM 平台的数据库测试通过以下方式自动跳过：

1. **代码层面**：`runDatabaseTest` 函数会自动检测 WASM 平台并跳过测试执行
2. **Gradle 层面**：WASM 测试任务（`wasmJsBrowserTest`、`wasmJsTest`、`compileTestKotlinWasmJs`）被禁用，避免编译错误

```kotlin
// runDatabaseTest 会自动跳过 WASM 平台
@Test
fun `test database`() = runDatabaseTest { database ->
    // 在 WASM 平台上，这个测试会被自动跳过
    // 不会执行任何代码，也不会抛出错误
}
```

### 平台特定实现

- **Android/JVM**：使用 `JdbcSqliteDriver` 创建内存数据库（`IN_MEMORY`）
    - 使用反射访问 driver 并关闭数据库
    - 每个连接都创建独立的内存数据库
- **iOS**：使用 `NativeSqliteDriver` 创建内存数据库（`:memory:test_${随机数}`）
    - 为每个测试生成唯一的内存数据库名称，确保测试隔离
    - Kotlin/Native 不支持反射，无法直接关闭 driver，但使用唯一名称确保隔离
    - SQLite 的内存数据库使用 `:memory:` 前缀，不同名称创建独立的内存数据库
- **JS/WASM**：使用 `WebWorkerDriver` 在 Web Worker 中运行 SQL.js
    - JS 平台完全支持数据库测试
    - WASM 平台数据库测试自动跳过（由于 JS interop 限制）

### 测试数据库特性

1. **内存数据库**：所有平台都使用内存数据库，不会持久化数据
    - Android/JVM：使用 `JdbcSqliteDriver.IN_MEMORY`
    - iOS：使用 `:memory:test_${随机数}` 创建唯一的内存数据库
    - JS/WASM：使用 `WebWorkerDriver` 在 Web Worker 中运行
2. **外键约束**：自动启用 `PRAGMA foreign_keys = ON`，支持级联删除等操作
3. **独立实例**：每个测试用例都获得全新的数据库实例
    - iOS 平台通过唯一的内存数据库名称确保隔离
    - 其他平台通过关闭 driver 确保隔离
4. **自动清理**：`runDatabaseTest` 会在测试结束后自动清理数据
    - Android/JVM：通过反射关闭 driver
    - iOS：使用唯一的内存数据库名称，连接关闭后自动销毁
    - JS：内存数据库在测试结束后自动销毁

### JS 平台特殊处理

由于 JS 平台的异步特性：

- `@BeforeTest` 在 JS 中是 "fire-and-forget"，不会被测试框架等待
- 因此使用 `runDatabaseTest` 函数，在测试函数内部创建和清理数据库
- 确保每个测试用例都有独立的数据库实例

## 🔗 相关模块

- `datastore:database` - 数据库 Schema 定义
- `datastore:database-client` - 客户端数据库驱动
- `datastore:datasource-local` - 本地数据源（使用此模块进行测试）

## 📚 使用示例

### 基本使用

```kotlin
class CardDataSourceTest {
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

        dataSource.insertCard(card)

        val result = dataSource.getCardById("card-1")
        assertNotNull(result)
        assertEquals("Test content", result?.content)
    }
}
```

### 测试事务

```kotlin
@Test
fun `test transaction rollback`() = runDatabaseTest { database ->
    database.transaction {
        database.cardQueries.insertCard(...)
        throw RuntimeException("Test rollback")
    }

    // 验证数据已回滚
    val result = database.cardQueries.selectById("card-1").awaitAsOneOrNull()
    assertEquals(null, result)
}
```

### 测试外键约束

```kotlin
@Test
fun `test foreign key constraints`() = runDatabaseTest { database ->
    // 创建用户
    database.userQueries.insertUser(...)

    // 创建卡片（关联用户）
    database.cardQueries.insertCard(..., user_id = "user-1")

    // 验证关联关系
    val card = database.cardQueries.selectById("card-1", "user-1").awaitAsOneOrNull()
    assertNotNull(card)
}
```

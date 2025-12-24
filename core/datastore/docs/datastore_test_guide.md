# Datastore 测试指南

## 📋 概述

本文档描述了如何运行和编写 `core:datastore` 模块的数据库相关测试。

## 🏗️ 测试结构

```
commonTest/
└── kotlin/tech/zhifu/app/myhub/datastore/
    ├── datasource/
    │   └── LocalCardDataSourceTest.kt      # LocalCardDataSource 测试
    ├── repository/
    │   └── CardRepositoryTest.kt           # CardRepository 测试
    └── database/
        └── DatabaseTest.kt                  # 数据库基础功能测试
```

## 🚀 运行测试

### 运行所有测试

```bash
# 运行所有平台的测试
./gradlew :core:datastore:allTests

# 运行特定平台的测试
./gradlew :core:datastore:testDebugUnitTest          # Android
./gradlew :core:datastore:jvmTest                    # JVM/Desktop
./gradlew :core:datastore:iosSimulatorArm64Test      # iOS
./gradlew :core:datastore:jsBrowserTest              # Web (JS)
```

### 运行特定测试类

```bash
# Android
./gradlew :core:datastore:testDebugUnitTest --tests "LocalCardDataSourceTest"

# JVM
./gradlew :core:datastore:jvmTest --tests "CardRepositoryTest"

# Web (JS)
./gradlew :core:datastore:jsBrowserTest --tests "LocalCardDataSourceTest"
```

## 🛠️ 测试辅助类

### DatabaseTestHelper

跨平台的数据库测试辅助类，提供：

- `createTestDatabase()`: 创建测试数据库（内存数据库，用于单元测试）
- `clearTestDatabase()`: 清空测试数据库
- `runDatabaseTest()`: 运行数据库测试的便捷函数（自动创建和清理）

各平台实现：

- Android: `DatabaseTestHelper.android.kt` - 使用 JdbcSqliteDriver 内存数据库
- iOS: `DatabaseTestHelper.ios.kt` - 使用 NativeSqliteDriver
- JVM: `DatabaseTestHelper.jvm.kt` - 使用 JdbcSqliteDriver 内存数据库
- Web: `DatabaseTestHelper.js.kt` - 使用 WebWorkerDriver（异步）

## 💡 测试示例

### LocalCardDataSource 测试

测试本地数据源的基本 CRUD 操作：

```kotlin
import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalCardDataSourceImpl

class LocalCardDataSourceTest {
    @Test
    fun `test insert and get card`() = runDatabaseTest { database ->
        val dataSource = LocalCardDataSourceImpl(database)
        val card = createTestCard("1", CardType.QUOTE)
        dataSource.insertCard(card)
        val result = dataSource.getCardById("1")
        assertNotNull(result)
    }
}
```

### CardRepository 测试

测试 Repository 层的业务逻辑：

```kotlin
import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalCardDataSourceImpl
import tech.zhifu.app.myhub.datastore.repository.impl.CardRepositoryImpl

class CardRepositoryTest {
    @Test
    fun `test create card`() = runDatabaseTest { database ->
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)

        val card = createTestCard("1", CardType.QUOTE)
        val result = repository.createCard(card)
        assertTrue(result.isSuccess)
    }
}
```

### 数据库基础功能测试

测试数据库 schema 和基础功能：

```kotlin
class DatabaseTest {
    @Test
    fun `test database schema creation`() = runDatabaseTest { database ->
        val result = database.cardQueries.selectAll().awaitAsList()
        assertNotNull(result)
        assertEquals(0, result.size)
    }
}
```

## ✅ 测试最佳实践

1. **使用 `runDatabaseTest`**：测试时使用 `runDatabaseTest { database -> ... }` 自动创建和清理测试数据库，避免影响实际数据
   - ✅ **推荐**：使用 `runDatabaseTest`，它已经处理了平台差异（特别是 Kotlin/JS 的异步问题）
   - ❌ **不推荐**：在 `@BeforeTest` 中使用 `runTest`，在 Kotlin/JS 环境下不会被等待完成
2. **自动清理**：`runDatabaseTest` 会在测试结束后自动调用 `clearTestDatabase()` 清理数据
3. **独立实例**：每个测试用例都会获得一个全新的数据库实例，确保测试之间相互独立
4. **独立测试**：每个测试应该是独立的，不依赖其他测试的执行顺序
5. **测试边界情况**：测试空数据、null 值、边界条件等
6. **测试事务**：验证事务的提交和回滚
7. **测试外键约束**：验证 CASCADE 删除等约束

> **注意**：关于 Kotlin/JS 测试的特殊情况，请参考 [FAQ - Kotlin/JS 测试问题](../../../docs/FAQ.md#q1-为什么在-kotlinjs-测试中beforetest-里的-runtest--不会等待完成)

## 📊 测试覆盖范围

### LocalCardDataSource 测试覆盖

- ✅ 插入和获取卡片
- ✅ 获取所有卡片
- ✅ 更新卡片
- ✅ 删除卡片
- ✅ 观察卡片 Flow
- ✅ 卡片标签
- ✅ 卡片元数据
- ✅ 待办清单项
- ✅ 收藏功能
- ✅ 删除所有卡片

### CardRepository 测试覆盖

- ✅ 创建卡片
- ✅ 获取所有卡片
- ✅ 根据 ID 获取卡片
- ✅ 更新卡片
- ✅ 删除卡片
- ✅ 切换收藏状态
- ✅ 获取收藏的卡片
- ✅ 根据类型获取卡片
- ✅ 搜索卡片（按查询）
- ✅ 搜索卡片（按类型）
- ✅ 搜索卡片（按收藏）
- ✅ 搜索卡片排序

### 数据库基础功能测试覆盖

- ✅ Schema 创建
- ✅ 插入和查询
- ✅ 事务回滚
- ✅ 外键约束
- ✅ CASCADE 删除

## 🔄 持续集成

测试应该在 CI/CD 流程中自动运行：

```yaml
# 示例 GitHub Actions
- name: Run tests
  run: |
    ./gradlew :core:datastore:allTests
```

## 🔧 故障排除

### 测试失败：数据库锁定

如果遇到数据库锁定错误，确保：

1. 每个测试后清理数据库
2. 使用内存数据库而不是文件数据库
3. 测试之间没有共享状态

### 测试失败：找不到表

确保：

1. Schema 已正确创建
2. 迁移文件格式正确
3. 数据库驱动正确初始化

### 测试失败：时间相关错误

确保：

1. 使用 `kotlin.time.Clock.System.now()` 而不是 `System.currentTimeMillis()`（跨平台兼容）
2. 使用 `kotlin.time.Instant` 而不是 `kotlinx.datetime.Instant`
3. 时间戳格式正确
4. 时区处理一致

### 测试失败：Kotlin/JS 平台异步问题

如果测试在 JS 平台失败，可能是因为 `@BeforeTest` 中的异步操作没有被等待：

**错误示例：**

```kotlin
@BeforeTest
fun setup() = runTest {  // ❌ 在 JS 平台不会被等待
    database = createTestDatabase()
}
```

**正确做法：**

```kotlin
@Test
fun `test something`() = runDatabaseTest { database ->  // ✅ 推荐
    // 测试代码
}
```

更多信息请参考 [FAQ](../../../docs/FAQ.md#q1-为什么在-kotlinjs-测试中beforetest-里的-runtest--不会等待完成)

## 📚 参考资源

- [Kotlin Test 文档](https://kotlinlang.org/api/latest/kotlin.test/)
- [SQLDelight 测试文档](https://cashapp.github.io/sqldelight/2.0/testing/)
- [Kotlin Coroutines 测试](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [架构设计文档](./datastore_architecture.md) - 了解整体架构
- [待办事项](./datastore_todos.md) - 查看待完善的功能

---

**最后更新**: 2025-01-20  
**维护者**: MyHub Team

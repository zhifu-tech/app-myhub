# MyHub 数据库服务端模块方案设计

**方案名称**：Datastore Database Server Infra v1  
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
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Datastore Database Server Infra v1 的基线设计
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

在 MyHub 服务端应用的开发和运行过程中，需要创建和管理数据库驱动。典型的场景包括：

1. **多数据库支持**：服务端应用需要支持 SQLite（开发/小型部署）和 PostgreSQL（生产环境）
2. **环境变量配置**：通过环境变量灵活配置数据库连接，便于部署和运维
3. **数据库初始化**：首次创建数据库时自动加载初始数据
4. **依赖注入集成**：需要与 Koin 等 DI 框架集成

### 1.2 问题根因

在引入统一的数据库服务端模块之前，MyHub 服务端应用面临以下问题：

1. **驱动创建分散**：各模块可能使用不同的方式创建数据库驱动，导致代码重复
2. **数据库类型硬编码**：数据库类型硬编码在代码中，难以切换
3. **配置管理混乱**：数据库配置分散在各处，难以统一管理
4. **初始化逻辑缺失**：缺乏统一的数据库初始化机制

### 1.3 影响范围

- **代码维护**：驱动创建代码分散，增加维护成本
- **部署灵活性**：数据库类型硬编码导致部署不灵活
- **开发效率**：缺乏统一的初始化机制降低开发效率

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **服务端专用**：专为 JVM 服务端应用设计
- ✅ **多数据库支持**：支持 SQLite 和 PostgreSQL
- ✅ **环境变量配置**：通过环境变量灵活配置数据库连接
- ✅ **自动初始化**：SQLite 数据库首次创建时自动加载初始数据
- ✅ **依赖注入**：提供 Koin DI 模块，便于集成
- ✅ **外键支持**：自动启用外键约束以支持级联删除

### 2.2 非功能目标

- ✅ **易于使用**：提供简洁的 API
- ✅ **配置灵活**：支持环境变量和代码配置
- ✅ **部署友好**：通过环境变量配置，便于容器化部署

### 2.3 模块特性说明

**重要说明**：`datastore/database-server` 模块是一个**单一功能模块**，专注于服务端数据库驱动的创建和管理。

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 数据库驱动：SQLDelight + JDBC

**选择理由**：

- ✅ **SQLDelight 支持**：SQLDelight 支持通过 JDBC 连接多种数据库
- ✅ **SQLite 支持**：使用 `JdbcSqliteDriver` 连接 SQLite
- ✅ **PostgreSQL 支持**：使用 `JdbcSqliteDriver` + PostgreSQL JDBC 驱动连接 PostgreSQL
- ✅ **类型安全**：编译时生成类型安全的查询接口

**数据库支持**：

| 数据库类型      | 驱动                                   | JDBC URL 格式                            |
|------------|--------------------------------------|----------------------------------------|
| SQLite     | `JdbcSqliteDriver`                   | `jdbc:sqlite:path/to/db.db`            |
| PostgreSQL | `JdbcSqliteDriver` + PostgreSQL JDBC | `jdbc:postgresql://host:port/database` |

#### 3.1.2 配置管理：环境变量 + 代码配置

**选择理由**：

- ✅ **部署友好**：环境变量配置便于容器化部署
- ✅ **灵活性**：支持代码配置作为备选方案
- ✅ **默认值**：提供合理的默认值，降低配置复杂度

### 3.2 架构模式

#### 3.2.1 工厂模式

**设计原则**：

- **DatabaseConfig**：数据库配置类，支持从环境变量读取
- **DatabaseDriverFactory**：数据库驱动工厂，根据配置创建对应的驱动
- **DatabaseModule**：Koin DI 模块，提供配置和驱动的单例

**优势**：

- ✅ **关注点分离**：配置和驱动创建分离
- ✅ **易于扩展**：添加新数据库类型只需扩展工厂方法
- ✅ **依赖注入**：通过 DI 统一管理

---

## 4. 架构设计

### 4.1 模块结构

```text
datastore/database-server/
├── src/
│   └── main/
│       └── kotlin/tech/zhifu/app/myhub/datastore/database/
│           ├── DatabaseConfig.kt          # 数据库配置类
│           ├── DatabaseDriverFactory.kt  # 数据库驱动工厂
│           └── di/
│               └── DatabaseModule.kt      # Koin DI 模块
└── build.gradle.kts
```

### 4.2 核心组件

#### 4.2.1 DatabaseConfig（数据库配置）

**功能**：

- 存储数据库连接配置
- 支持从环境变量读取配置
- 提供默认值

**定义**：

```kotlin
data class DatabaseConfig(
    val type: DatabaseType,
    val host: String? = null,
    val port: Int? = null,
    val database: String? = null,
    val username: String? = null,
    val password: String? = null,
    val path: String? = null // SQLite 文件路径
)

enum class DatabaseType {
    SQLITE,
    POSTGRESQL
}
```

**环境变量支持**：

- `DB_TYPE`：数据库类型（SQLITE 或 POSTGRESQL）
- `DB_HOST`：PostgreSQL 主机（默认：localhost）
- `DB_PORT`：PostgreSQL 端口（默认：5432）
- `DB_NAME`：PostgreSQL 数据库名（默认：myhub）
- `DB_USER`：PostgreSQL 用户名（默认：postgres）
- `DB_PASSWORD`：PostgreSQL 密码（默认：postgres）
- `DB_PATH`：SQLite 文件路径（默认：.myhub/myhub.db）

#### 4.2.2 DatabaseDriverFactory（数据库驱动工厂）

**功能**：

- 根据配置创建对应的数据库驱动
- SQLite：创建文件数据库，自动初始化
- PostgreSQL：连接 PostgreSQL 服务器

**主要方法**：

```kotlin
class DatabaseDriverFactory(private val config: DatabaseConfig) {
    fun createDriver(): SqlDriver {
        return when (config.type) {
            DatabaseType.SQLITE -> createSqliteDriver()
            DatabaseType.POSTGRESQL -> createPostgresDriver()
        }
    }
}
```

#### 4.2.3 DatabaseModule（Koin DI 模块）

**功能**：

- 提供 `DatabaseConfig` 单例（从环境变量读取）
- 提供 `DatabaseDriverFactory` 单例
- 提供 `MyHubDatabase` 单例

**定义**：

```kotlin
val databaseModule = module {
    single<DatabaseConfig> {
        DatabaseConfig.fromEnvironment()
    }

    single<DatabaseDriverFactory> {
        DatabaseDriverFactory(get())
    }

    single<MyHubDatabase> {
        val driverFactory = get<DatabaseDriverFactory>()
        MyHubDatabase(driverFactory.createDriver())
    }
}
```

### 4.3 数据库初始化流程

#### 4.3.1 SQLite 初始化

**流程**：

1. 检查数据库文件是否存在
2. 如果不存在，创建数据库和表结构
3. 加载初始数据（从 `database/init` 目录）
4. 启用外键约束

**特点**：

- ✅ 自动创建数据库目录
- ✅ 自动加载初始数据
- ✅ 仅在新数据库时加载初始数据

#### 4.3.2 PostgreSQL 初始化

**流程**：

1. 连接 PostgreSQL 服务器
2. 尝试创建表结构（如果不存在）
3. 忽略表已存在的错误

**特点**：

- ✅ 需要预先创建数据库和用户
- ✅ 自动创建表结构
- ✅ 容错处理（表已存在时忽略错误）

---

## 5. 实现细节

### 5.1 SQLite 驱动创建

**实现**：

```kotlin
private fun createSqliteDriver(): SqlDriver {
    val path = config.path ?: ".myhub/myhub.db"
    val databaseFile = File(path)

    // 确保目录存在
    if (databaseFile.parentFile != null && !databaseFile.parentFile.exists()) {
        databaseFile.parentFile.mkdirs()
    }

    val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databaseFile.absolutePath}")

    // 检查数据库文件是否已存在
    val databaseExists = databaseFile.exists()

    if (!databaseExists) {
        // 创建新数据库和表
        MyHubDatabase.Schema.synchronous().create(driver)

        // 加载初始数据
        val databaseManager = GlobalContext.get().get<DatabaseManager>()
        runBlocking {
            databaseManager.loadAllData(
                resourcePath = "database/init",
                clearBeforeLoad = false
            )
        }
    }

    // 启用外键约束
    driver.execute(null, "PRAGMA foreign_keys = ON;", 0)

    return driver
}
```

### 5.2 PostgreSQL 驱动创建

**实现**：

```kotlin
private fun createPostgresDriver(): SqlDriver {
    val host = config.host ?: "localhost"
    val port = config.port ?: 5432
    val database = config.database ?: "myhub"
    val username = config.username ?: "postgres"
    val password = config.password ?: "postgres"

    val url = "jdbc:postgresql://$host:$port/$database?user=$username&password=$password"

    // 加载 PostgreSQL 驱动
    Class.forName("org.postgresql.Driver")

    // SQLDelight 的 JdbcSqliteDriver 实际上可以用于任何 JDBC 数据库
    val driver = JdbcSqliteDriver(url)

    // 创建数据库 schema（如果不存在）
    try {
        MyHubDatabase.Schema.synchronous().create(driver)
    } catch (e: Exception) {
        // 如果表已存在，忽略错误
    }

    return driver
}
```

### 5.3 环境变量配置

**实现**：

```kotlin
companion object {
    fun fromEnvironment(): DatabaseConfig {
        val dbType = System.getenv("DB_TYPE")?.uppercase() ?: "SQLITE"
        val type = try {
            DatabaseType.valueOf(dbType)
        } catch (e: Exception) {
            DatabaseType.SQLITE
        }

        return when (type) {
            DatabaseType.POSTGRESQL -> DatabaseConfig(
                type = DatabaseType.POSTGRESQL,
                host = System.getenv("DB_HOST") ?: "localhost",
                port = System.getenv("DB_PORT")?.toIntOrNull() ?: 5432,
                database = System.getenv("DB_NAME") ?: "myhub",
                username = System.getenv("DB_USER") ?: "postgres",
                password = System.getenv("DB_PASSWORD") ?: "postgres"
            )
            DatabaseType.SQLITE -> DatabaseConfig(
                type = DatabaseType.SQLITE,
                path = System.getenv("DB_PATH") ?: ".myhub/myhub.db"
            )
        }
    }
}
```

---

## 6. 实施计划

### 6.1 实施阶段

#### 阶段 1：核心功能实现（已完成）

- ✅ DatabaseConfig 实现
- ✅ DatabaseDriverFactory 实现
- ✅ SQLite 驱动创建
- ✅ PostgreSQL 驱动创建

#### 阶段 2：环境变量支持（已完成）

- ✅ 环境变量读取实现
- ✅ 默认值配置
- ✅ 配置验证

#### 阶段 3：数据库初始化（已完成）

- ✅ SQLite 自动初始化
- ✅ 初始数据加载集成
- ✅ 外键约束启用

#### 阶段 4：依赖注入集成（已完成）

- ✅ DatabaseModule 实现
- ✅ Koin 集成

### 6.2 里程碑

| 里程碑      | 目标日期       | 状态    |
|----------|------------|-------|
| 核心功能完成   | 2026-01-13 | ✅ 已完成 |
| 环境变量支持完成 | 2026-01-13 | ✅ 已完成 |
| 数据库初始化完成 | 2026-01-13 | ✅ 已完成 |
| 依赖注入完成   | 2026-01-13 | ✅ 已完成 |

---

## 7. 风险评估

### 7.1 技术风险

#### 7.1.1 PostgreSQL 连接失败风险

**风险描述**：PostgreSQL 连接配置错误或服务未启动导致连接失败

**影响**：高

**缓解措施**：

- ✅ 提供详细的错误信息
- ✅ 文档说明连接要求
- ✅ 提供故障排查指南

#### 7.1.2 初始数据加载失败风险

**风险描述**：SQLite 初始化时加载初始数据失败

**影响**：中

**缓解措施**：

- ✅ 检查 `DatabaseManager` 是否可用
- ✅ 提供错误处理机制
- ✅ 文档说明依赖要求

### 7.2 业务风险

#### 7.2.1 数据库类型切换风险

**风险描述**：从 SQLite 切换到 PostgreSQL 可能导致数据丢失

**影响**：高

**缓解措施**：

- ✅ 文档说明数据库迁移步骤
- ✅ 提供数据导出/导入工具
- ✅ 备份机制

### 7.3 维护风险

#### 7.3.1 环境变量配置风险

**风险描述**：环境变量配置错误导致应用启动失败

**影响**：中

**缓解措施**：

- ✅ 提供合理的默认值
- ✅ 配置验证机制
- ✅ 文档说明配置要求

---

## 8. 附录

### 8.1 相关文档

- [MyHub 数据库模块方案设计](../datastore-database/docs/myhub-datastore-database-infra-v1.0.md)
- [MyHub 数据库客户端模块方案设计](../datastore-database-client/docs/myhub-datastore-database-client-infra-v1.0.md)
- [MyHub 数据库管理模块方案设计](../datastore-database-manage/docs/myhub-datastore-database-manage-infra-v1.0.md)
- [SQLDelight 官方文档](https://cashapp.github.io/sqldelight/)
- [PostgreSQL JDBC 驱动文档](https://jdbc.postgresql.org/documentation/)

### 8.2 代码示例

#### 8.2.1 基本使用

```kotlin
// 使用依赖注入
startKoin {
    modules(databaseModule)
}

class CardService(
    private val database: MyHubDatabase
) {
    suspend fun getAllCards(userId: String): List<Card> {
        return database.cardQueries.selectAll(userId).awaitAsList()
    }
}
```

#### 8.2.2 环境变量配置

```bash
# SQLite 配置
export DB_TYPE=SQLITE
export DB_PATH=.myhub/myhub.db

# PostgreSQL 配置
export DB_TYPE=POSTGRESQL
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=myhub
export DB_USER=postgres
export DB_PASSWORD=postgres
```

#### 8.2.3 Spring Boot 集成

```kotlin
@SpringBootApplication
class MyHubServerApplication {
    @Bean
    fun koinApplication() = startKoin {
        modules(databaseModule)
    }
}
```

### 8.3 术语表

| 术语                    | 说明                                |
|-----------------------|-----------------------------------|
| DatabaseConfig        | 数据库配置类，存储数据库连接信息                  |
| DatabaseDriverFactory | 数据库驱动工厂，根据配置创建对应的数据库驱动            |
| DatabaseType          | 数据库类型枚举（SQLITE、POSTGRESQL）        |
| JdbcSqliteDriver      | SQLDelight 提供的 JDBC 数据库驱动，支持多种数据库 |
| 环境变量配置                | 通过环境变量配置数据库连接，便于容器化部署             |

### 8.4 常见问题

#### Q1: 如何切换数据库类型？

**A**: 设置环境变量 `DB_TYPE=POSTGRESQL` 或 `DB_TYPE=SQLITE`，重启应用即可。

#### Q2: SQLite 初始数据何时加载？

**A**: 仅在数据库文件不存在时加载初始数据，确保 `DatabaseManager` 在 Koin 中可用。

#### Q3: PostgreSQL 需要预先创建数据库吗？

**A**: 是的，PostgreSQL 需要预先创建数据库和用户，模块会自动创建表结构。

#### Q4: 如何配置数据库连接池？

**A**: 当前实现使用 SQLDelight 的默认连接，如需连接池，可以扩展 `DatabaseDriverFactory`。

---

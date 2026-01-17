# datastore:database-server

数据库服务端驱动工厂模块，提供服务端应用的数据库驱动创建功能。

## 📋 功能特性

- ✅ **服务端专用**：专为 JVM 服务端应用设计
- ✅ **多数据库支持**：支持 SQLite 和 PostgreSQL
- ✅ **环境变量配置**：通过环境变量灵活配置数据库连接
- ✅ **自动初始化**：SQLite 数据库首次创建时自动加载初始数据
- ✅ **依赖注入**：提供 Koin DI 模块，便于集成
- ✅ **外键支持**：自动启用外键约束以支持级联删除

## 🎯 支持的数据库

### SQLite

- **驱动类型**：`JdbcSqliteDriver`
- **数据库位置**：文件系统（默认：`.myhub/myhub.db`）
- **特点**：
    - 轻量级，无需额外服务
    - 适合开发和小型部署
    - 首次创建时自动加载初始数据
    - 自动创建数据库目录

### PostgreSQL

- **驱动类型**：`JdbcSqliteDriver`（通过 JDBC URL 连接 PostgreSQL）
- **数据库位置**：PostgreSQL 服务器
- **特点**：
    - 企业级数据库，支持高并发
    - 适合生产环境
    - 需要预先创建数据库和用户
    - 通过 JDBC URL 连接

## 📁 模块结构

```
datastore/database-server/
├── src/
│   └── main/
│       └── kotlin/
│           └── database/
│               ├── DatabaseConfig.kt          # 数据库配置类
│               ├── DatabaseDriverFactory.kt  # 数据库驱动工厂
│               └── di/
│                   └── DatabaseModule.kt     # Koin DI 模块
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
        databaseModule  // 提供 DatabaseConfig、DatabaseDriverFactory 和 MyHubDatabase
    )
}

// 在需要的地方注入使用
class CardService(
    private val database: MyHubDatabase
) {
    suspend fun getAllCards(): List<Card> {
        return database.cardQueries.selectAll("user-1").awaitAsList()
    }
}
```

### 直接创建实例

```kotlin
import tech.zhifu.app.myhub.datastore.database.DatabaseConfig
import tech.zhifu.app.myhub.datastore.database.DatabaseDriverFactory
import tech.zhifu.app.myhub.datastore.database.DatabaseType
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

// 创建配置
val config = DatabaseConfig(
    type = DatabaseType.SQLITE,
    path = ".myhub/myhub.db"
)

// 创建驱动工厂
val driverFactory = DatabaseDriverFactory(config)

// 创建数据库实例
val database = MyHubDatabase(driverFactory.createDriver())

// 使用数据库
val cards = database.cardQueries.selectAll("user-1").awaitAsList()
```

## ⚙️ 配置说明

### 环境变量配置

模块支持通过环境变量配置数据库连接，使用 `DatabaseConfig.fromEnvironment()` 自动读取：

#### SQLite 配置

```bash
# 数据库类型（可选，默认为 SQLITE）
export DB_TYPE=SQLITE

# 数据库文件路径（可选，默认为 .myhub/myhub.db）
export DB_PATH=.myhub/myhub.db
```

#### PostgreSQL 配置

```bash
# 数据库类型
export DB_TYPE=POSTGRESQL

# 数据库连接信息（可选，有默认值）
export DB_HOST=localhost          # 默认：localhost
export DB_PORT=5432              # 默认：5432
export DB_NAME=myhub             # 默认：myhub
export DB_USER=postgres          # 默认：postgres
export DB_PASSWORD=postgres      # 默认：postgres
```

### 代码配置

```kotlin
// SQLite 配置
val sqliteConfig = DatabaseConfig(
    type = DatabaseType.SQLITE,
    path = ".myhub/myhub.db"  // 可选，默认为 .myhub/myhub.db
)

// PostgreSQL 配置
val postgresConfig = DatabaseConfig(
    type = DatabaseType.POSTGRESQL,
    host = "localhost",         // 可选，默认为 localhost
    port = 5432,               // 可选，默认为 5432
    database = "myhub",         // 可选，默认为 myhub
    username = "postgres",      // 可选，默认为 postgres
    password = "postgres"       // 可选，默认为 postgres
)
```

## 🔧 数据库初始化

### SQLite 自动初始化

当使用 SQLite 且数据库文件不存在时，模块会自动：

1. 创建数据库文件
2. 创建所有表结构
3. 加载初始数据（从 `database/init` 目录）
4. 启用外键约束

**注意**：初始数据加载需要 `DatabaseManager` 在 Koin 中可用。确保 `datastore:database-manage` 模块已正确配置。

### PostgreSQL 手动初始化

PostgreSQL 需要手动创建数据库和用户：

```sql
-- 创建数据库
CREATE DATABASE myhub;

-- 创建用户（可选）
CREATE USER myhub_user WITH PASSWORD 'your_password';

-- 授予权限
GRANT ALL PRIVILEGES ON DATABASE myhub TO myhub_user;
```

模块会尝试自动创建表结构，但如果遇到错误（如表已存在），会忽略错误。

## 📦 依赖

### 核心依赖

- `datastore:database` - 数据库 Schema 定义（提供 `MyHubDatabase`）
- `datastore:database-manage` - 数据库管理工具（用于初始数据加载）
- `sqldelight-sqlite` - SQLDelight SQLite 驱动
- `postgresql` - PostgreSQL JDBC 驱动
- `koin-core` - 依赖注入框架

## 🔌 DI 模块

模块提供了 `databaseModule`，包含以下组件：

### `DatabaseConfig`

数据库配置单例，从环境变量读取：

```kotlin
single<DatabaseConfig> {
    DatabaseConfig.fromEnvironment()
}
```

### `DatabaseDriverFactory`

数据库驱动工厂单例，依赖 `DatabaseConfig`：

```kotlin
single<DatabaseDriverFactory> {
    DatabaseDriverFactory(get())
}
```

### `MyHubDatabase`

数据库实例单例，依赖 `DatabaseDriverFactory`：

```kotlin
single<MyHubDatabase> {
    val driverFactory = get<DatabaseDriverFactory>()
    MyHubDatabase(driverFactory.createDriver())
}
```

## 📌 注意事项

1. **SQLite 初始数据加载**：
    - 仅在数据库文件不存在时加载初始数据
    - 需要 `DatabaseManager` 在 Koin 中可用
    - 确保 `datastore:database-manage` 模块已正确配置

2. **PostgreSQL 连接**：
    - 需要预先创建数据库和用户
    - 确保 PostgreSQL 服务正在运行
    - 检查网络连接和防火墙设置

3. **环境变量优先级**：
    - 环境变量配置优先于代码配置
    - 未设置的环境变量使用默认值

4. **数据库迁移**：
    - SQLite 自动处理数据库迁移
    - PostgreSQL 可能需要手动处理迁移
    - 建议使用 SQLDelight 的迁移机制

5. **外键约束**：
    - SQLite 自动启用外键约束
    - PostgreSQL 默认启用外键约束（数据库级别）

6. **线程安全**：
    - `DatabaseDriverFactory` 和 `MyHubDatabase` 都是线程安全的
    - 可以在多线程环境中使用

## 🔗 相关模块

- `datastore:database` - 数据库 Schema 定义
- `datastore:database-client` - 客户端数据库驱动（对比参考）
- `datastore:database-manage` - 数据库管理工具（初始数据加载）
- `datastore:repository-server` - 服务端数据仓库（使用此模块）

## 📚 使用示例

### Spring Boot 应用

```kotlin
@SpringBootApplication
class MyHubServerApplication {
    @Bean
    fun koinApplication() = startKoin {
        modules(
            databaseModule,
            // 其他模块...
        )
    }
}

@Service
class CardService(
    private val database: MyHubDatabase
) {
    suspend fun getAllCards(userId: String): List<Card> {
        return database.cardQueries.selectAll(userId).awaitAsList()
    }
}
```

### Ktor 应用

```kotlin
fun Application.module() {
    install(Koin) {
        modules(
            databaseModule,
            // 其他模块...
        )
    }
    
    routing {
        get("/cards") {
            val database = get<MyHubDatabase>()
            val cards = database.cardQueries.selectAll("user-1").awaitAsList()
            call.respond(cards)
        }
    }
}
```

### 命令行应用

```kotlin
fun main() {
    startKoin {
        modules(databaseModule)
    }
    
    val database = get<MyHubDatabase>()
    val cards = runBlocking {
        database.cardQueries.selectAll("user-1").awaitAsList()
    }
    println("Found ${cards.size} cards")
}
```

## 🔍 故障排查

### SQLite 数据库文件未创建

- 检查文件路径权限
- 确保目录存在或可创建
- 查看应用日志中的错误信息

### PostgreSQL 连接失败

- 检查 PostgreSQL 服务是否运行：`pg_isready`
- 验证连接信息（主机、端口、用户名、密码）
- 检查防火墙设置
- 查看 PostgreSQL 日志：`tail -f /var/log/postgresql/postgresql-*.log`

### 初始数据未加载

- 确保 `DatabaseManager` 在 Koin 中可用
- 检查 `datastore:database-manage` 模块是否正确配置
- 验证 `database/init` 目录中的 JSON 文件存在

### 环境变量未生效

- 确认环境变量已正确设置：`echo $DB_TYPE`
- 检查应用启动时是否读取了环境变量
- 使用 `DatabaseConfig.fromEnvironment()` 打印配置信息

## 📚 参考文档

- [SQLDelight 官方文档](https://cashapp.github.io/sqldelight/)
- [PostgreSQL JDBC 驱动文档](https://jdbc.postgresql.org/documentation/)
- [Koin 官方文档](https://insert-koin.io/)

## 🔄 与客户端模块的区别

| 特性    | `datastore:database-client` | `datastore:database-server` |
|-------|-----------------------------|-----------------------------|
| 平台支持  | Android、iOS、JVM、JS、WASM     | JVM 仅                       |
| 数据库类型 | SQLite（平台特定）                | SQLite、PostgreSQL           |
| 配置方式  | 代码配置                        | 环境变量 + 代码配置                 |
| 初始数据  | 不自动加载                       | SQLite 自动加载                 |
| 使用场景  | 客户端应用                       | 服务端应用                       |


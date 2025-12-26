# 数据库持久化配置

Server 模块现在支持数据库持久化，可以使用 SQLite 或 PostgreSQL 作为数据存储。

## 📋 支持的数据库

- **SQLite** - 轻量级文件数据库（默认）
- **PostgreSQL** - 生产环境推荐的关系型数据库

## 🔧 配置方式

数据库配置通过环境变量进行设置。

### SQLite 配置（默认）

如果不设置任何环境变量，服务器将使用 SQLite：

```bash
# 默认配置（可选）
export DB_TYPE=SQLITE
export DB_PATH=.myhub/myhub.db  # 数据库文件路径，默认为 .myhub/myhub.db
```

### PostgreSQL 配置

要使用 PostgreSQL，需要设置以下环境变量：

```bash
export DB_TYPE=POSTGRESQL
export DB_HOST=localhost        # PostgreSQL 主机地址，默认 localhost
export DB_PORT=5432             # PostgreSQL 端口，默认 5432
export DB_NAME=myhub            # 数据库名称，默认 myhub
export DB_USER=postgres         # 数据库用户名，默认 postgres
export DB_PASSWORD=postgres     # 数据库密码，默认 postgres
```

## 🚀 运行服务器

### 使用 SQLite（默认）

```bash
./gradlew :server:run
```

### 使用 PostgreSQL

```bash
export DB_TYPE=POSTGRESQL
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=myhub
export DB_USER=postgres
export DB_PASSWORD=your_password

./gradlew :server:run
```

## 📦 数据库 Schema

Server 使用 SQLDelight 管理数据库 schema，schema 定义在 `core/datastore` 模块中：

- **Card** - 卡片表
- **Tag** - 标签表
- **Template** - 模板表
- **User** - 用户表
- **Statistics** - 统计信息表

数据库 schema 会在首次启动时自动创建。

## 🔄 数据库迁移

SQLDelight 会自动处理数据库迁移。当 schema 发生变化时，SQLDelight 会：

1. 检测 schema 版本变化
2. 自动执行迁移脚本
3. 保持数据完整性

## 📝 注意事项

### SQLite

- SQLite 数据库文件存储在本地文件系统中
- 默认路径：`.myhub/myhub.db`
- 适合开发和单机部署
- 支持外键约束（已启用）

### PostgreSQL

- 需要先创建数据库：
  ```sql
  CREATE DATABASE myhub;
  ```
- 确保 PostgreSQL 服务正在运行
- 确保用户有足够的权限创建表
- 适合生产环境和高并发场景

## 🧪 测试

### 测试 SQLite

```bash
# 使用默认 SQLite 配置
./gradlew :server:run
```

### 测试 PostgreSQL

```bash
# 设置 PostgreSQL 环境变量
export DB_TYPE=POSTGRESQL
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=myhub_test
export DB_USER=postgres
export DB_PASSWORD=postgres

./gradlew :server:run
```

## 🔍 验证数据库连接

启动服务器后，可以通过健康检查端点验证数据库连接：

```bash
curl http://localhost:8083/health
```

如果数据库连接正常，将返回：

```json
{
  "status": "ok",
  "version": "1.0.0"
}
```

## 📚 相关文档

- [Server 架构设计](architecture.md)
- [SQLDelight 官方文档](https://cashapp.github.io/sqldelight/)
- [PostgreSQL 官方文档](https://www.postgresql.org/docs/)


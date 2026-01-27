# MyHub Server

MyHub 服务器模块，基于 Ktor 框架实现的 RESTful API 服务器，为 MyHub 客户端应用提供数据同步和业务逻辑服务。

## 📋 概述

Server 模块是 MyHub 项目的后端服务，负责：

- 提供 RESTful API 接口
- 管理卡片、标签、模板、用户等数据
- 处理数据同步和统计信息
- 支持多客户端（Android、iOS、Desktop、Web）的数据访问

## 🛠️ 技术栈

- **Ktor** - Kotlin 异步 Web 框架
- **Netty** - 高性能网络服务器引擎
- **Logback** - 日志框架
- **Kotlin** - 编程语言（JVM 17+）

## 🚀 快速开始

### 环境要求

- **JDK 17+**
- **Gradle 8.0+**

### 运行服务器

#### 使用 .env 文件配置（推荐）

首先创建 `.env` 文件：

```bash
# 进入 server 目录
cd server

# 复制示例配置文件
cp .env.example .env

# 编辑 .env 文件，根据需要修改配置
# 开发环境默认使用 SQLite，无需修改
# 生产环境需要配置 PostgreSQL 相关参数
```

然后运行服务器：

```bash
# 开发模式运行（使用 SQLite，默认）
./gradlew :server:run

# 使用 PostgreSQL（需要在 .env 中配置）
./gradlew :server:run
```

#### 使用环境变量配置

```bash
# 开发模式运行（使用 SQLite，默认）
./gradlew :server:run

# 使用 PostgreSQL
export DB_TYPE=POSTGRESQL
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=myhub
export DB_USER=postgres
export DB_PASSWORD=your_password
./gradlew :server:run

# 构建可执行 JAR
./gradlew :server:build
java -jar server/build/libs/server-1.0.0.jar
```

### Docker 部署

Server 模块支持 Docker 容器化部署，提供了完整的 Docker 配置文件。

#### 使用 Docker Compose（推荐）

使用 Docker Compose 可以一键启动应用和数据库。

**首次使用前，请先配置 .env 文件：**

```bash
# 进入 server 目录
cd server

# 复制示例配置文件（如果还没有）
cp .env.example .env

# 编辑 .env 文件，根据需要修改配置
# 默认配置已适合开发环境，生产环境请修改数据库密码等敏感信息
```

**使用 PostgreSQL（生产环境推荐）：**

```bash
# 进入 server 目录
cd server

# 确保 .env 文件中配置了 PostgreSQL 相关参数
# DB_TYPE=POSTGRESQL
# DB_HOST=postgres
# DB_PORT=5432
# DB_NAME=myhub
# DB_USER=myhub_user
# DB_PASSWORD=myhub_password

# 启动所有服务（应用 + PostgreSQL）
docker-compose up -d

# 查看日志
docker-compose logs -f myhub-server

# 停止所有服务
docker-compose down

# 停止并删除数据卷（注意：会删除数据库数据）
docker-compose down -v
```

**使用 SQLite（开发/测试环境）：**

```bash
# 进入 server 目录
cd server

# 确保 .env 文件中配置了 SQLite（默认配置）
# DB_TYPE=SQLITE
# DB_PATH=/app/data/myhub.db

# 启动服务（仅应用，使用 SQLite）
docker-compose -f docker-compose.sqlite.yml up -d

# 查看日志
docker-compose -f docker-compose.sqlite.yml logs -f myhub-server

# 停止服务
docker-compose -f docker-compose.sqlite.yml down
```

**注意**：Docker Compose 会自动读取 `.env` 文件中的环境变量。如果 `.env` 文件不存在，将使用 docker-compose.yml 中的默认值。

#### 单独构建和运行 Docker 镜像

```bash
# 构建镜像（从项目根目录）
docker build -f server/Dockerfile -t myhub-server:latest .

# 使用 SQLite 运行
docker run -d \
  --name myhub-server \
  -p 8083:8083 \
  -v $(pwd)/server/data:/app/data \
  -e DB_TYPE=SQLITE \
  -e DB_PATH=/app/data/myhub.db \
  myhub-server:latest

# 使用 PostgreSQL 运行（需要先启动 PostgreSQL）
docker run -d \
  --name myhub-server \
  -p 8083:8083 \
  -e DB_TYPE=POSTGRESQL \
  -e DB_HOST=your-postgres-host \
  -e DB_PORT=5432 \
  -e DB_NAME=myhub \
  -e DB_USER=postgres \
  -e DB_PASSWORD=your_password \
  myhub-server:latest
```

#### 环境变量配置

可以通过 `.env` 文件或环境变量配置服务器和数据库。推荐使用 `.env` 文件进行配置。

**配置方式优先级**（从高到低）：

1. Docker Compose 中的 `environment:` 直接定义
2. `.env` 文件中的配置
3. 系统环境变量
4. 代码中的默认值

**服务器配置：**

- `SERVER_PORT` - 服务器端口（默认：8083）

**数据库配置：**

- `DB_TYPE` - 数据库类型：`SQLITE` 或 `POSTGRESQL`（默认：SQLITE）
- `DB_HOST` - PostgreSQL 主机地址（PostgreSQL 模式，默认：localhost）
- `DB_PORT` - PostgreSQL 端口（PostgreSQL 模式，默认：5432）
- `DB_NAME` - 数据库名称（PostgreSQL 模式，默认：myhub）
- `DB_USER` - 数据库用户名（PostgreSQL 模式，默认：postgres）
- `DB_PASSWORD` - 数据库密码（PostgreSQL 模式，**必须设置**）
- `DB_PATH` - SQLite 数据库文件路径（SQLite 模式，默认：.myhub/myhub.db）

**PostgreSQL 服务配置**（仅用于 docker-compose.yml）：

- `POSTGRES_DB` - PostgreSQL 数据库名称（默认：myhub）
- `POSTGRES_USER` - PostgreSQL 用户名（默认：myhub_user）
- `POSTGRES_PASSWORD` - PostgreSQL 密码（默认：myhub_password）

**JWT 认证配置**：

- `JWT_SECRET` - JWT 签名密钥（**必须设置**，至少 32 个字符）
- `JWT_ISSUER` - Token 发行者（可选，默认：myhub-api）
- `JWT_AUDIENCE` - Token 受众（可选，默认：myhub-client）

**快速配置 JWT**：

```bash
# 1. 复制配置文件（如果还没有）
cp .env.example .env

# 2. .env 文件中已包含一个生成的示例 Secret，可以直接使用（开发环境）
# 生产环境请重新生成：./scripts/generate-jwt-secret.sh
```

详细的环境变量配置说明请参考：

- [环境变量配置指南](docs/environment-variables.md)
- [JWT 配置指南](docs/jwt-setup-guide.md)
- [JWT 认证实现文档](docs/jwt-authentication-implementation.md)

#### 健康检查

Docker 容器包含健康检查功能，可以通过以下方式查看：

```bash
# 查看容器健康状态
docker ps

# 查看健康检查日志
docker inspect myhub-server | grep -A 10 Health
```

健康检查端点：`http://localhost:8083/health`

### 默认配置

- **端口**: 8083（定义在 `core:platform` 模块的 `Constants.kt`）
- **主机**: 0.0.0.0（监听所有网络接口）
- **数据库**: SQLite（默认，文件路径：`.myhub/myhub.db`）
- **开发模式**: 支持热重载和详细日志

### 数据库配置

Server 支持 SQLite 和 PostgreSQL 两种数据库。详细配置说明请参考 [数据库配置文档](docs/database.md)。

## 📡 API 端点

### 基础路径

所有 API 端点都以 `/api` 为前缀。

### 卡片 API (`/api/cards`)

- `GET /api/cards` - 获取所有卡片
- `GET /api/cards/{id}` - 根据 ID 获取卡片
- `POST /api/cards` - 创建新卡片
- `PUT /api/cards/{id}` - 更新卡片
- `DELETE /api/cards/{id}` - 删除卡片
- `POST /api/cards/{id}/favorite` - 切换收藏状态

### 标签 API (`/api/tags`)

- `GET /api/tags` - 获取所有标签
- `GET /api/tags/{id}` - 根据 ID 获取标签
- `POST /api/tags` - 创建新标签
- `PUT /api/tags/{id}` - 更新标签
- `DELETE /api/tags/{id}` - 删除标签

### 模板 API (`/api/templates`)

- `GET /api/templates` - 获取所有模板
- `GET /api/templates/{id}` - 根据 ID 获取模板
- `POST /api/templates` - 创建新模板
- `PUT /api/templates/{id}` - 更新模板
- `DELETE /api/templates/{id}` - 删除模板

### 用户 API (`/api/users`)

- `GET /api/users/current` - 获取当前用户信息
- `PUT /api/users/current` - 更新当前用户信息

### 统计 API (`/api/statistics`)

- `GET /api/statistics` - 获取统计信息

## 🧪 测试

```bash
# 运行所有测试
./gradlew :server:test

# 运行特定测试类
./gradlew :server:test --tests "ApplicationTest"
```

## 📦 项目结构

```
server/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── tech/zhifu/app/myhub/
│   │   │       └── Application.kt          # 主应用入口
│   │   └── resources/
│   │       └── logback.xml                 # 日志配置
│   └── test/
│       └── kotlin/
│           └── tech/zhifu/app/myhub/
│               └── ApplicationTest.kt      # 测试代码
├── build.gradle.kts                        # 构建配置
├── README.md                                # 本文档
└── docs/
    ├── architecture.md                      # 架构设计文档
    └── todos.md                             # 待办事项文档
```

## 🔗 相关文档

- [架构设计文档](docs/architecture.md) - 详细的架构设计和实现说明
- [待办事项](docs/todos.md) - 功能开发计划和待办任务
- [MyHub 整体架构](../../docs/myhub_architecture.md) - 项目整体架构

## 📝 开发说明

### 模块依赖

Server 模块依赖：

- `core:platform` - 平台抽象层（获取 SERVER_PORT 等常量）

### 日志配置

日志配置位于 `src/main/resources/logback.xml`，支持：

- 控制台输出
- 可配置的日志级别
- 线程信息输出

### 开发模式

使用 `--development` 参数运行服务器时，Ktor 会启用：

- 热重载（代码变更自动重启）
- 详细的错误信息
- 开发友好的日志输出

## 🔧 配置

### 端口配置

服务器端口定义在 `core:platform` 模块的 `Constants.kt` 中：

```kotlin
const val SERVER_PORT = 8083
```

### 环境变量

可以通过环境变量覆盖默认配置（待实现）：

- `SERVER_PORT` - 服务器端口
- `SERVER_HOST` - 服务器主机地址

## 📄 许可证

[待添加]

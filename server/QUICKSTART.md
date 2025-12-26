# MyHub Server 快速开始指南

## 🚀 使用 .env 文件配置（推荐）

### 1. 初始化配置

```bash
# 进入 server 目录
cd server

# 复制示例配置文件
cp .env.example .env

# 编辑 .env 文件（可选，默认配置已适合开发环境）
# 默认使用 SQLite，无需修改即可开始使用
```

### 2. 开发环境（SQLite）

`.env` 文件默认配置已适合开发环境：

```bash
# 直接运行（使用 SQLite）
./gradlew :server:run

# 或使用 Docker Compose
docker-compose -f docker-compose.sqlite.yml up -d
```

### 3. 生产环境（PostgreSQL）

编辑 `.env` 文件，取消注释并配置 PostgreSQL：

```bash
# 编辑 .env 文件
nano .env

# 修改以下配置：
DB_TYPE=POSTGRESQL
DB_HOST=postgres
DB_PORT=5432
DB_NAME=myhub
DB_USER=myhub_user
DB_PASSWORD=your_secure_password

# PostgreSQL 服务配置（用于 docker-compose.yml）
POSTGRES_DB=myhub
POSTGRES_USER=myhub_user
POSTGRES_PASSWORD=your_secure_password
```

然后启动服务：

```bash
# 使用 Docker Compose（推荐）
docker-compose up -d

# 或直接运行（需要先启动 PostgreSQL）
./gradlew :server:run
```

## 📝 配置说明

### 环境变量优先级

1. Docker Compose `environment:` 直接定义（最高优先级）
2. `.env` 文件中的配置
3. 系统环境变量
4. 代码中的默认值（最低优先级）

### 常用配置项

| 变量名 | 说明 | 默认值 | 必需 |
|--------|------|--------|------|
| `SERVER_PORT` | 服务器端口 | `8083` | 否 |
| `DB_TYPE` | 数据库类型 | `SQLITE` | 否 |
| `DB_PATH` | SQLite 数据库路径 | `.myhub/myhub.db` | SQLite 模式 |
| `DB_HOST` | PostgreSQL 主机 | `localhost` | PostgreSQL 模式 |
| `DB_PORT` | PostgreSQL 端口 | `5432` | PostgreSQL 模式 |
| `DB_NAME` | 数据库名称 | `myhub` | PostgreSQL 模式 |
| `DB_USER` | 数据库用户名 | `postgres` | PostgreSQL 模式 |
| `DB_PASSWORD` | 数据库密码 | - | PostgreSQL 模式（必需） |

## 🔒 安全提示

1. **永远不要提交 `.env` 文件到版本控制**
   - `.env` 文件已添加到 `.gitignore`
   - 只提交 `.env.example` 作为模板

2. **生产环境使用强密码**
   - 修改 `DB_PASSWORD` 和 `POSTGRES_PASSWORD`
   - 使用密码管理器生成强密码

3. **限制文件权限**
   ```bash
   chmod 600 .env  # 仅所有者可读写
   ```

## 📚 更多信息

- [环境变量配置指南](docs/environment-variables.md) - 详细的配置方式说明
- [数据库配置文档](docs/database.md) - 数据库配置详细说明
- [README.md](README.md) - 完整的项目文档



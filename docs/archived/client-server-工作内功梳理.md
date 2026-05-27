# 客户端与服务端工作内功梳理

本文档用于快速理解当前仓库中“客户端”和“服务端”两部分各自的职责范围、核心能力与关键模块位置，方便后续迭代时快速定位。

## 1. 客户端（Client）

### 核心职责
- 多端 UI（Android / iOS / Desktop / Web）的统一实现与交付。
- 本地数据管理（离线优先、缓存、同步）。
- 业务功能模块化（Dashboard / Profile / Settings 等）。
- 跨平台基础能力封装（日志、网络、平台适配）。

### 主要模块（从仓库结构拆解）
- `composeApp/`
  - 跨平台 UI 主体与业务协调层（Compose Multiplatform）。
- `androidApp/`、`iosApp/`
  - 原生入口与平台工程配置。
- `feature/`
  - 业务功能模块（dashboard、profile、settings 等）。
- `component/`
  - 可复用 UI 组件（card、mixed）。
- `core/`
  - `analytics` / `logger` / `network` / `platform` / `platform-compose` 等。
- `datastore/`
  - 数据模型、数据源、Repository、同步逻辑等。

### 关键能力链路（数据流）
1. UI 触发操作（Compose + ViewModel）。
2. Repository 层统一读取策略（本地优先 + 远端同步）。
3. SQLDelight 本地持久化 + Ktor Client 远程请求。
4. Flow 驱动 UI 状态更新。

### 技术栈摘要
- Kotlin Multiplatform + Compose Multiplatform
- SQLDelight（本地）
- Ktor Client（远程）
- Koin（依赖注入）
- kotlinx.serialization / Flow

### 客户端常见落点
1. 新 UI 或交互：`feature/<module>` + `component/`
2. 新数据字段：`datastore/` 全链路 + 相关 feature
3. 新平台配置：`androidApp/` 或 `iosApp/` 或 `composeApp/`

## 2. 服务端（Server）

### 核心职责
- 提供 RESTful API（卡片、标签、卡集、模板、用户、认证、同步）。
- 管理业务数据与持久化（SQLite 或 PostgreSQL）。
- 认证、异常处理、日志与运行时配置。

### 主要模块
- `server/`
  - Ktor Server 主服务模块。
  - Docker 配置（`Dockerfile`、`docker-compose*.yml`）。
  - `.env` 运行配置与启动脚本。
- `datastore/`
  - `repository-server` / `database-server`：服务端数据访问与持久化定义。

### 关键能力链路（请求流）
1. HTTP 请求进入 Ktor 路由。
2. 认证/鉴权处理（JWT + StatusPages）。
3. 调用 repository-server 完成业务逻辑。
4. 数据落库（SQLite / PostgreSQL）。
5. 返回 API 响应。

### 技术栈摘要
- Ktor Server + Netty
- Koin（服务端依赖注入）
- Logback（日志）
- SQLite / PostgreSQL

### 服务端常见落点
1. 新 API：`server/src` 路由 + `datastore/repository-server`
2. 新数据表：`datastore/database-server` + 同步模型
3. 部署配置：`server/Dockerfile` + `server/docker-compose*.yml`

## 3. 客户端与服务端的边界

### 客户端负责
- UI/交互
- 离线缓存策略
- 本地状态/数据一致性
- 端上性能与体验

### 服务端负责
- 权限/认证/同步
- 数据归档与跨端一致性
- 业务规则与集中式计算

## 4. 快速定位清单

### 客户端新增字段
1. `datastore/database`（schema + SQLDelight）
2. `datastore/model`（domain 模型）
3. `datastore/datasource-local` + `datastore/datasource-remote`
4. `datastore/repository-client`
5. `feature` / `composeApp` 读取与写回

### 服务端新增字段
1. `datastore/database-server`（schema）
2. `datastore/repository-server`（数据访问）
3. `server/src`（API 路由与 DTO）
4. 同步协议（如有）

## 5. 参考入口
- 根目录 `README.md`
- `docs/myhub_architecture.md`
- `server/README.md`


# Server 架构设计

## 📋 概述

本文档描述了 MyHub Server 模块的架构设计，包括技术选型、模块结构、API 设计和数据流等。

## 🏗️ 整体架构

### 架构分层

```
┌─────────────────────────────────────────────────────────┐
│                    API Layer（按领域分包）                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Cards API  │  │   Tags API   │  │ Collections  │  │
│  │  (api.card)  │  │  (api.tag)   │  │  (api.*)     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ CardTemplates│  │   Users API  │  │  Auth / Sync │  │
│  │(api.template)│  │  (api.user)  │  │ (api.auth等) │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└───────────────────────┬───────────────────────────────────┘
                        │
┌───────────────────────▼───────────────────────────────────┐
│              Service Layer（server 内）                    │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  CardService | TagService | CardTemplateService     │  │
│  │  UserService | CollectionService | SyncService      │  │
│  │  - 业务逻辑处理、数据验证和转换、异常处理                 │  │
│  └─────────────────────────────────────────────────────┘  │
└───────────────────────┬───────────────────────────────────┘
                        │
┌───────────────────────▼───────────────────────────────────┐
│              Data Layer（datastore 模块）                   │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  Repository（repository-server-api + repository-server）│
│  │  - CardRepository | TagRepository | CardTemplateRepository│
│  │  - UserRepository | CollectionRepository | SyncRepository│
│  └─────────────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  Data Source                                        │  │
│  │  - database-server（SQLDelight / SQLite 或 PostgreSQL）│  │
│  │  - datasource-local                                 │  │
│  └─────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────┘
```

## 🔧 技术栈

### 核心框架

- **Ktor** - Kotlin 异步 Web 框架
    - 轻量级、高性能
    - 完全异步、非阻塞
    - 支持协程
    - 丰富的插件生态

### 服务器引擎

- **Netty** - 高性能网络服务器
    - 事件驱动、非阻塞 I/O
    - 高并发支持
    - 生产环境验证

### 日志

- **Logback** - 日志框架
    - 高性能日志记录
    - 灵活的配置
    - 与 SLF4J 集成

## 📡 API 设计

### RESTful API 规范

遵循 RESTful 设计原则：

- **资源导向** - URL 表示资源
- **HTTP 方法** - GET、POST、PUT、DELETE
- **状态码** - 标准 HTTP 状态码
- **JSON 格式** - 请求和响应使用 JSON

### API 端点设计

#### 卡片 API

```
GET    /api/cards              # 获取所有卡片（支持查询参数）
GET    /api/cards/{id}        # 获取指定卡片
POST   /api/cards             # 创建新卡片
PUT    /api/cards/{id}        # 更新卡片
DELETE /api/cards/{id}        # 删除卡片
POST   /api/cards/{id}/favorite  # 切换收藏状态
```

#### 标签 API

```
GET    /api/tags              # 获取所有标签
GET    /api/tags/{id}         # 获取指定标签
POST   /api/tags              # 创建新标签
PUT    /api/tags/{id}         # 更新标签
DELETE /api/tags/{id}         # 删除标签
```

#### 模板 API

```
GET    /api/templates         # 获取所有模板
GET    /api/templates/{id}    # 获取指定模板
POST   /api/templates         # 创建新模板
PUT    /api/templates/{id}    # 更新模板
DELETE /api/templates/{id}    # 删除模板
```

#### 用户 API

```
GET    /api/users/current     # 获取当前用户
PUT    /api/users/current     # 更新当前用户
```

#### 同步 API

```
POST   /api/sync              # 推送同步数据
GET    /api/sync/changes      # 获取同步变更（需 userId、entityType 等参数）
```

> **说明**：统计 API（Statistics）当前未实现；模板相关 API 命名为 **CardTemplates**（/api/templates 路由由 CardTemplatesApi 提供）。

### 请求/响应格式

#### 创建卡片请求示例

```json
POST /api/cards
Content-Type: application/json

{
    "type": "QUOTE",
    "title": "Inspirational Quote",
    "content": "The only way to do great work is to love what you do.",
    "author": "Steve Jobs",
    "tags": [
        "inspiration",
        "motivation"
    ],
    "isFavorite": false,
    "metadata": {
       "quoteAuthor": "Steve Jobs",
       "quoteCategory": "Motivation"
    }
}
```

#### 响应示例

```json
{
  "id": "card-123",
  "type": "QUOTE",
  "title": "Inspirational Quote",
  "content": "The only way to do great work is to love what you do.",
  "author": "Steve Jobs",
  "tags": [
    "inspiration",
    "motivation"
  ],
  "isFavorite": false,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z",
  "metadata": {
    "quoteAuthor": "Steve Jobs",
    "quoteCategory": "Motivation"
  }
}
```

### 错误响应格式

```json
{
  "error": {
    "code": "CARD_NOT_FOUND",
    "message": "Card with id 'card-123' not found",
    "timestamp": "2024-01-01T00:00:00Z"
  }
}
```

## 🔄 数据流

### 请求处理流程

```
1. 客户端请求
   ↓
2. Ktor Routing (路由匹配)
   ↓
3. Content Negotiation (内容协商)
   ↓
4. Authentication (身份验证 - 待实现)
   ↓
5. Service Layer (业务逻辑处理)
   ↓
6. Repository Layer (数据访问)
   ↓
7. Data Source (数据库/缓存)
   ↓
8. Response (JSON 序列化)
   ↓
9. 客户端响应
```

### 数据同步流程

```
客户端 ←→ Server API ←→ Repository ←→ Database
   ↓                      ↓
本地缓存              数据验证和转换
```

## 📦 模块结构

### 当前结构（与实现一致）

**server 模块**（API、Service、Auth、DI、异常配置均在 server 内）：

```
server/
├── src/main/kotlin/tech/zhifu/app/myhub/
│   ├── Application.kt              # 应用入口 + Ktor 插件 + 路由挂载
│   ├── api/                         # API 层（按领域分包）
│   │   ├── auth/   AuthApi.kt       # 认证（login、refresh）
│   │   ├── card/   CardsApi.kt
│   │   ├── collection/ CollectionsApi.kt
│   │   ├── sync/   SyncApi.kt
│   │   ├── tag/    TagsApi.kt
│   │   ├── template/ CardTemplatesApi.kt   # 卡片模板（/api/templates）
│   │   └── user/   UsersApi.kt
│   ├── auth/                        # JWT 认证
│   ├── di/   Koin.kt                 # Koin 模块组装
│   ├── exception/                   # StatusPages 统一异常处理
│   └── service/                     # 业务逻辑层（CardService、UserService 等）
└── src/main/resources/
    └── logback.xml
```

**数据层**（在 datastore 中，非 server 内）：

- **repository-server-api**：服务端 Repository 接口定义（server 依赖接口）
- **repository-server**：服务端 Repository 实现（依赖 database-server、datasource-local）
- **database-server**：服务端数据库驱动与配置（SQLite/PostgreSQL）
- **model、model-dto**：领域模型与 DTO、异常类（client/server 共用）

## 🔌 Ktor 插件配置

### 已配置插件

- **Routing** - URL 路由
- **Content Negotiation** - JSON 序列化/反序列化（待实现）
- **CORS** - 跨域支持（待实现）
- **Call Logging** - 请求日志（待实现）

### 推荐插件（待实现）

- **Authentication** - 身份验证
- **Status Pages** - 统一错误处理
- **Compression** - 响应压缩
- **Metrics** - 性能监控
- **Rate Limiting** - 限流保护

## 🗄️ 数据存储

### 当前状态

- 使用内存存储（临时）
- 无持久化

### 推荐方案

#### 选项 1: SQLDelight + PostgreSQL

- **优点**: 与客户端使用相同的数据模型
- **缺点**: 需要额外的数据库服务器

#### 选项 2: SQLDelight + SQLite

- **优点**: 轻量级，易于部署
- **缺点**: 并发性能有限

#### 选项 3: Exposed + PostgreSQL

- **优点**: Kotlin DSL，类型安全
- **缺点**: 需要额外的 ORM 学习

### 推荐：SQLDelight + PostgreSQL

- 与客户端数据模型一致
- 支持复杂查询
- 生产环境验证
- 良好的性能

## 🔐 安全设计

### 身份验证（待实现）

- **JWT Token** - 无状态身份验证
- **OAuth 2.0** - 第三方登录支持（可选）

### 授权（待实现）

- **RBAC** - 基于角色的访问控制
- **资源级权限** - 细粒度权限控制

### 安全措施（待实现）

- **HTTPS** - TLS/SSL 加密
- **输入验证** - 防止注入攻击
- **Rate Limiting** - 防止暴力攻击
- **CORS** - 跨域安全配置

## 📊 性能优化

### 缓存策略（待实现）

- **Redis** - 分布式缓存
- **内存缓存** - 热点数据缓存
- **CDN** - 静态资源加速（如需要）

### 数据库优化（待实现）

- **连接池** - 数据库连接复用
- **索引优化** - 查询性能优化
- **分页查询** - 大数据集处理
- **读写分离** - 高并发场景（可选）

## 🧪 测试策略

### 单元测试

- Service 层业务逻辑测试
- Repository 层数据访问测试
- 工具函数测试

### 集成测试

- API 端点测试
- 数据库集成测试
- 端到端测试

### 测试工具

- **Ktor Test Host** - API 测试
- **MockK** - Mock 框架
- **Testcontainers** - 数据库测试容器（可选）

## 📈 监控和日志

### 日志

- **结构化日志** - JSON 格式
- **日志级别** - DEBUG、INFO、WARN、ERROR
- **请求追踪** - 请求 ID 追踪

### 监控（待实现）

- **健康检查** - `/health` 端点
- **指标收集** - Prometheus 指标
- **性能监控** - APM 工具集成

## 🔄 部署

### 开发环境

```bash
./gradlew :server:run
```

### 生产环境

```bash
# 构建 JAR
./gradlew :server:build

# 运行
java -jar server/build/libs/server-1.0.0.jar
```

### Docker 部署（待实现）

```dockerfile
FROM openjdk:17-jre-slim
COPY server/build/libs/server-1.0.0.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 🔗 与客户端集成

### API 客户端

客户端通过 `datastore` 模块的 `RemoteCardDataSource` 等接口与服务器通信。

### 数据同步

- **拉取同步** - 客户端主动拉取数据
- **推送同步** - 服务器推送更新（WebSocket - 待实现）

## 📚 参考资源

- [Ktor 官方文档](https://ktor.io/docs/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [RESTful API 设计最佳实践](https://restfulapi.net/)


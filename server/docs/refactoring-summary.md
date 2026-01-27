# Server 重构总结

> 按照 RESTful API 最佳实践重构 server 实现

## 📋 重构概述

本次重构按照 `docs/api-design-best-practices.md` 中的建议，彻底重新构建了 server 实现，使其符合 RESTful API 设计规范和业内最佳实践。

---

## ✅ 已完成的重构内容

### 1. DTO 层（Request/Response 模型）

**位置**：`datastore/model/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/model/dto/`

**设计原则**：

- ✅ DTO 放在 `datastore/model` 模块，便于客户端和服务端共享
- ✅ 每个 DTO 类一个文件，符合开闭原则
- ✅ 使用 `@Serializable` 注解支持 JSON 序列化

**新增文件**：

- `CardResponse.kt` - 卡片响应 DTO
- `TagResponse.kt` - 标签响应 DTO
- `CreateCardRequest.kt` - 创建卡片请求 DTO（包含验证逻辑）
- `UpdateCardRequest.kt` - 完整更新卡片请求 DTO（包含验证逻辑）
- `PartialUpdateCardRequest.kt` - 部分更新卡片请求 DTO（包含验证逻辑）
- `PaginationInfo.kt` - 分页信息
- `PaginatedResponse.kt` - 分页响应（泛型）
- `ErrorResponse.kt` - 错误响应
- `ErrorDetail.kt` - 错误详情
- `CardResponseExtensions.kt` - Domain 到 DTO 的转换扩展函数

**特点**：

- ✅ 使用 `@Serializable` 注解支持 JSON 序列化
- ✅ 包含请求验证逻辑
- ✅ 标准化的分页响应格式
- ✅ 统一的错误响应格式
- ✅ 符合开闭原则（每个类一个文件）
- ✅ 客户端和服务端共享（放在 datastore/model 模块）

---

### 2. API 路由重构（RESTful 设计）

**位置**：`server/src/main/kotlin/tech/zhifu/app/myhub/api/CardsApi.kt`

**重构前**：

```
GET  /api/cards/fetchCards?userId={userId}
GET  /api/cards/fetchCard?cardId={id}
POST /api/cards/upsertCard
DELETE /api/cards/deleteCard?cardId={id}
```

**重构后**（符合 RESTful 规范）：

```
GET    /api/cards?userId={userId}&page=1&limit=20&type={type}&isFavorite={bool}
GET    /api/cards/{id}?userId={userId}
POST   /api/cards?userId={userId}
PUT    /api/cards/{id}?userId={userId}
PATCH  /api/cards/{id}?userId={userId}
DELETE /api/cards/{id}?userId={userId}
```

**同步 API 重构**（符合 RESTful 规范）：

```
重构前：
POST /api/sync/push
GET  /api/sync/pull?userId={userId}&entityType={type}&since={token}&limit={limit}

重构后：
POST /api/sync                    - 推送同步数据（创建同步操作）
GET  /api/sync/changes?userId={userId}&entityType={type}&since={token}&limit={limit}  - 获取同步变更（查询同步资源）
```

**改进点**：

- ✅ URL 使用资源式设计（名词），不再包含动词
- ✅ 资源 ID 使用路径参数（`/api/cards/{id}`），而不是查询参数
- ✅ 正确使用 HTTP 方法语义（GET、POST、PUT、PATCH、DELETE）
- ✅ 支持分页和筛选查询参数
- ✅ 统一的错误处理

**同步 API 改进**：

- ✅ 移除动词 `/push` 和 `/pull`，改为资源导向设计
- ✅ `POST /api/sync` - 推送同步数据（创建同步操作）
- ✅ `GET /api/sync/changes` - 获取同步变更（查询同步资源，使用子资源 `/changes`）
- ✅ 改进错误响应格式，提供更清晰的错误信息

**标签 API 重构**（符合 RESTful 规范）：

```
GET    /api/tags              - 获取用户的所有标签
GET    /api/tags/{id}         - 获取指定标签
POST   /api/tags              - 创建标签
PUT    /api/tags/{id}         - 完整更新标签
DELETE /api/tags/{id}         - 删除标签
```

**卡集 API 重构**（符合 RESTful 规范）：

```
GET    /api/collections              - 获取用户的所有卡集
GET    /api/collections/{id}         - 获取指定卡集
POST   /api/collections              - 创建卡集
PUT    /api/collections/{id}         - 完整更新卡集
DELETE /api/collections/{id}         - 删除卡集
```

**卡片模板 API 重构**（符合 RESTful 规范）：

```
GET    /api/templates              - 获取所有模板（支持 type 查询参数筛选）
GET    /api/templates/{id}         - 获取指定模板
POST   /api/templates              - 创建模板
PUT    /api/templates/{id}         - 完整更新模板
DELETE /api/templates/{id}         - 删除模板
```

**用户 API 重构**（符合 RESTful 规范）：

```
重构前：
PUT /api/users/fetchUser?userId={userId}

重构后：
GET    /api/users/{id}                    - 获取指定用户
POST   /api/users                         - 创建用户
PUT    /api/users/{id}                    - 完整更新用户
DELETE /api/users/{id}                    - 删除用户
GET    /api/users/{id}/preferences        - 获取用户偏好设置
PUT    /api/users/{id}/preferences        - 更新用户偏好设置
```

**所有 API 的共同改进**：

- ✅ 使用认证中间件（`authenticate("auth-bearer")`）
- ✅ 从 JWT token 中获取 userId，不再使用查询参数
- ✅ 统一的错误处理（使用统一的异常类型）
- ✅ 统一的响应格式（使用 Response DTO）
- ✅ 权限验证（确保用户只能操作自己的资源）

---

### 3. Service 层重构

**位置**：`server/src/main/kotlin/tech/zhifu/app/myhub/service/CardService.kt`

**新增功能**：

- ✅ `getCards()` - 支持分页和筛选
- ✅ `getCard()` - 获取单个卡片（带权限验证）
- ✅ `createCard()` - 创建卡片（带验证）
- ✅ `updateCard()` - 完整更新卡片（带权限验证）
- ✅ `partialUpdateCard()` - 部分更新卡片（PATCH 支持）
- ✅ `deleteCard()` - 删除卡片（带权限验证）

**改进点**：

- ✅ 输入验证（内容长度、必填字段等）
- ✅ 权限验证（确保用户只能操作自己的资源）
- ✅ 分页支持（page、limit、total、totalPages）
- ✅ 筛选支持（type、isFavorite）
- ✅ 统一的错误处理

---

### 4. Repository 层扩展

**位置**：

- `datastore/repository-server-api/src/main/kotlin/.../CardRepository.kt`
- `datastore/repository-server/src/main/kotlin/.../CardRepositoryImpl.kt`

**新增方法**：

- ✅ `getCards(userId, page, limit, type, isFavorite)` - 支持分页和筛选
- ✅ `countCards(userId, type, isFavorite)` - 统计卡片数量（用于分页）
- ✅ `upsertCard(card)` - 创建或更新卡片

**改进点**：

- ✅ 支持分页查询
- ✅ 支持筛选（按类型、收藏状态）
- ✅ 实现 upsert 逻辑（存在则更新，不存在则创建）

---

### 5. 错误处理统一

**位置**：

- `server/src/main/kotlin/tech/zhifu/app/myhub/exception/ForbiddenException.kt`
- `server/src/main/kotlin/tech/zhifu/app/myhub/Application.kt`

**新增异常类型**：

- ✅ `ForbiddenException` - 权限不足异常（403）

**统一错误响应格式**：

```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Card with id 'card-123' not found",
    "timestamp": "2026-01-27T10:00:00Z",
    "path": "/api/cards/card-123"
  }
}
```

**错误码**：

- `NOT_FOUND` - 资源未找到（404）
- `VALIDATION_ERROR` - 验证错误（400）
- `FORBIDDEN` - 权限不足（403）
- `BAD_REQUEST` - 请求错误（400）
- `API_ERROR` - API 错误（自定义状态码）
- `INTERNAL_SERVER_ERROR` - 服务器错误（500）

---

### 6. Application.kt 更新

**改进点**：

- ✅ 添加 PATCH 方法支持（CORS 配置）
- ✅ 统一错误处理（使用新的 ErrorResponse 格式）
- ✅ 添加 Authorization Header 支持（为未来认证做准备）
- ✅ 详细的错误日志记录

---

## 📊 API 使用示例

### 获取卡片列表（带分页和筛选）

```http
GET /api/cards?userId=user-123&page=1&limit=20&type=QUOTE&isFavorite=true
```

**响应**：

```json
{
  "data": [
    {
      "id": "card-1",
      "type": "QUOTE",
      "title": "Inspirational Quote",
      "content": "The only way to do great work is to love what you do.",
      "userId": "user-123",
      "tags": [],
      "createdAt": "2026-01-27T10:00:00Z",
      "updatedAt": "2026-01-27T10:00:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 100,
    "totalPages": 5
  }
}
```

### 创建卡片

```http
POST /api/cards?userId=user-123
Content-Type: application/json

{
  "type": "QUOTE",
  "title": "New Quote",
  "content": "This is a new quote",
  "tagIds": []
}
```

**响应**（201 Created）：

```json
{
  "id": "card-456",
  "type": "QUOTE",
  "title": "New Quote",
  "content": "This is a new quote",
  "userId": "user-123",
  "tags": [],
  "createdAt": "2026-01-27T10:00:00Z",
  "updatedAt": "2026-01-27T10:00:00Z"
}
```

### 部分更新卡片

```http
PATCH /api/cards/card-456?userId=user-123
Content-Type: application/json

{
  "title": "Updated Title"
}
```

**响应**（200 OK）：

```json
{
  "id": "card-456",
  "type": "QUOTE",
  "title": "Updated Title",
  "content": "This is a new quote",
  "userId": "user-123",
  "tags": [],
  "createdAt": "2026-01-27T10:00:00Z",
  "updatedAt": "2026-01-27T10:01:00Z"
}
```

---

## 🔄 待完成的工作

### 1. 认证和授权

**当前状态**：✅ 已实现完整的 JWT Token 认证系统

**已实现**：

- ✅ 实现 JWT Token 认证（使用 Auth0 JWT 库）
- ✅ Access Token 和 Refresh Token 机制
- ✅ Token 过期检查（Access Token: 15分钟，Refresh Token: 7天）
- ✅ Token 刷新功能（`POST /api/auth/refresh`）
- ✅ 从 token 中获取 userId，不再使用查询参数
- ✅ 添加认证中间件（Ktor JWT 插件）

**实现细节**：

- `JwtConfig` - JWT 配置和 token 生成/验证
- `TokenService` - Token 服务（生成、验证、刷新）
- `AuthenticationConfig` - JWT 认证配置和扩展函数
- `AuthApi` - 认证 API
    - `POST /api/auth/login` - 登录获取 tokens
    - `POST /api/auth/refresh` - 刷新 access token
- `UnauthorizedException` - 未授权异常

**Token 设计**：

- **Access Token**：15 分钟有效期，用于访问 API
- **Refresh Token**：7 天有效期，用于刷新 Access Token

**使用方式**：

1. 客户端调用 `POST /api/auth/login` 获取 access token 和 refresh token
2. 在后续请求的 Header 中添加：`Authorization: Bearer {accessToken}`
3. 当 Access Token 过期时，使用 Refresh Token 调用 `POST /api/auth/refresh` 获取新的 Access Token
4. 服务端自动从 JWT token 中提取 userId，无需在查询参数中传递

**详细文档**：参见 `server/docs/jwt-authentication-implementation.md`

### 2. 标签管理

**当前状态**：✅ 标签关联功能已实现

**已实现**：

- ✅ 扩展 LocalTagDataSource 添加 `getTagByName` 和 `updateTag` 方法
- ✅ 实现 TagRepository 接口（`datastore/repository-server-api`）
- ✅ 实现 TagRepositoryImpl（`datastore/repository-server`），包含：
    - `getTag` - 根据 ID 获取标签
    - `getTags` - 获取用户所有标签
    - `getTagByName` - 根据名称获取标签
    - `upsertTag` - 创建或更新标签
    - `deleteTag` - 删除标签
    - `ensureTags` - 确保标签存在（如果不存在则自动创建，根据标签名称匹配）
    - `getTagsByIds` - 根据标签 ID 列表批量获取标签
- ✅ 实现标签与卡片的关联（通过 `card_tag` 表，在 LocalCardDataSource 中已实现）
- ✅ 更新 CardRepositoryImpl 使用 TagRepository 的 `ensureTags` 方法，确保卡片关联的标签存在
- ✅ 在 CardService 中集成标签关联功能：
    - 使用 `getTagsByIds` 从 `tagIds` 解析 Tag 对象
    - 支持创建、更新、部分更新卡片时的标签关联
    - 验证标签存在性和用户权限（确保标签属于当前用户）
- ✅ 更新 RepositoryModule 注册 TagRepository
- ✅ 更新 ServiceModule 注入 TagRepository 到 CardService
- ✅ 更新依赖注入（RepositoryModule 和 ServiceModule）

**实现细节**：

- `TagRepository` - 标签仓库接口，提供标签的 CRUD 操作
- `TagRepositoryImpl` - 标签仓库实现，使用 LocalTagDataSource
- `ensureTags` - 根据标签名称自动创建标签（如果不存在）
- `getTagsByIds` - 根据标签 ID 列表批量获取标签
- 在 `CardService` 中，创建/更新卡片时会自动处理标签关联
- 标签关联通过 `card_tag` 表实现多对多关系

### 3. 数据库优化

**当前状态**：✅ 数据库层面的分页和筛选已实现

**已实现**：

- ✅ 在数据库层面实现分页（LIMIT/OFFSET）
    - 在 `card_with_metadata.sq` 中添加 `selectCardWithMetadataByUserIdWithFilters` 查询
    - 支持 `LIMIT` 和 `OFFSET` 参数进行分页
- ✅ 添加数据库索引优化查询
    - 添加复合索引 `idx_card_user_id_type`：优化按用户和类型筛选
    - 添加复合索引 `idx_card_user_id_updated_at`：优化按用户和更新时间排序
    - 利用现有的 `idx_user_card_user_favorite` 索引优化收藏筛选
- ✅ 实现真正的筛选查询（WHERE 子句）
    - 支持按 `type` 筛选（可选）
    - 支持按 `isFavorite` 筛选（可选）
    - 使用 `INNER JOIN user_card` 获取收藏状态
    - 使用 `WHERE` 子句在数据库层面进行筛选，而不是在内存中
- ✅ 实现计数查询（用于分页）
    - 添加 `countCardWithMetadataByUserIdWithFilters` 查询
    - 支持相同的筛选条件，返回符合条件的卡片总数
- ✅ 更新 LocalCardDataSource 接口和实现
    - 添加 `getCards(userId, page, limit, type, isFavorite)` 方法
    - 添加 `countCards(userId, type, isFavorite)` 方法
- ✅ 更新 CardRepositoryImpl 使用数据库层面的查询
    - 移除内存中的筛选和分页逻辑
    - 直接调用 LocalCardDataSource 的新方法

**性能优化**：

- 查询性能：从 O(n) 内存筛选优化为 O(log n) 数据库索引查询
- 内存使用：只加载当前页的数据，而不是所有数据
- 分页效率：使用数据库 LIMIT/OFFSET，减少数据传输量

### 4. 其他 Repository

**当前状态**：✅ 所有 Repository 已实现

**已实现**：

- ✅ TagRepository（已完成）
- ✅ CollectionRepository（已完成）
    - 实现 `getCollections` - 获取用户所有卡集
    - 实现 `getCollectionById` - 根据 ID 获取卡集
    - 实现 `createCollection` - 创建卡集
    - 实现 `updateCollection` - 更新卡集
    - 实现 `deleteCollection` - 删除卡集
    - 扩展 `LocalCollectionDataSource` 添加 `updateCollection` 方法
- ✅ CardTemplateRepository（已完成）
    - 实现 `getTemplates` - 获取所有模板
    - 实现 `getTemplateById` - 根据 ID 获取模板
    - 实现 `getTemplatesByType` - 根据类型获取模板
    - 实现 `createTemplate` - 创建模板
    - 实现 `updateTemplate` - 更新模板
    - 实现 `deleteTemplate` - 删除模板
    - 扩展 `LocalCardTemplateDataSource` 添加完整 CRUD 方法
- ✅ UserRepository 扩展（已完成）
    - 扩展 `getUserById` - 根据 ID 获取用户（已存在）
    - 实现 `upsertUser` - 创建或更新用户
    - 实现 `deleteUser` - 删除用户
    - 实现 `getUserPreferences` - 获取用户偏好设置
    - 实现 `upsertUserPreferences` - 创建或更新用户偏好设置
    - 扩展 `LocalUserDataSource` 添加 `updateUser` 和 `updateUserPreferences` 方法
- ✅ 更新 RepositoryModule 注册所有新 Repository

### 5. Service 层标准化

**当前状态**：✅ 所有 Service 已标准化实现

**已实现**：

- ✅ TagService（已完成）
    - 实现 `getTags` - 获取用户所有标签
    - 实现 `getTag` - 获取指定标签（带权限验证）
    - 实现 `createTag` - 创建标签（带验证和重复检查）
    - 实现 `updateTag` - 更新标签（带权限验证）
    - 实现 `deleteTag` - 删除标签（带权限验证）
    - 遵循 CardService 的标准模式
- ✅ CollectionService（已完成）
    - 实现 `getCollections` - 获取用户所有卡集
    - 实现 `getCollection` - 获取指定卡集（带权限验证）
    - 实现 `createCollection` - 创建卡集（带验证）
    - 实现 `updateCollection` - 更新卡集（带权限验证）
    - 实现 `deleteCollection` - 删除卡集（带权限验证）
    - 遵循 CardService 的标准模式
- ✅ CardTemplateService（已完成）
    - 实现 `getTemplates` - 获取所有模板
    - 实现 `getTemplatesByType` - 根据类型获取模板
    - 实现 `getTemplate` - 获取指定模板
    - 实现 `createTemplate` - 创建模板（带验证）
    - 实现 `updateTemplate` - 更新模板
    - 实现 `deleteTemplate` - 删除模板
    - 遵循 CardService 的标准模式
- ✅ UserService 标准化（已完成）
    - 重构为标准化 Service，遵循 CardService 的模式
    - 实现 `getUser` - 获取指定用户
    - 实现 `createUser` - 创建用户（带验证）
    - 实现 `updateUser` - 更新用户
    - 实现 `deleteUser` - 删除用户
    - 实现 `getUserPreferences` - 获取用户偏好设置
    - 实现 `upsertUserPreferences` - 创建或更新用户偏好设置
- ✅ 创建所有必要的 DTO
    - `TagResponse`, `CreateTagRequest`, `UpdateTagRequest`
    - `CollectionResponse`, `CreateCollectionRequest`, `UpdateCollectionRequest`
    - `CardTemplateResponse`, `CreateCardTemplateRequest`, `UpdateCardTemplateRequest`
    - `UserResponse`, `CreateUserRequest`, `UpdateUserRequest`
    - `DTOExtensions` - Domain 到 DTO 的转换扩展函数
- ✅ 更新 ServiceModule 注册所有新 Service

**设计特点**：

- ✅ 统一的验证逻辑（Request DTO 包含 validate 方法）
- ✅ 统一的权限验证（确保用户只能操作自己的资源）
- ✅ 统一的错误处理（使用 NotFoundException、ForbiddenException）
- ✅ 统一的响应格式（使用 Response DTO）
- ✅ 遵循 RESTful API 最佳实践

---

## 📝 迁移指南

### 对于客户端开发者

**旧 API（已废弃）**：

```
GET  /api/cards/fetchCards?userId={userId}
GET  /api/cards/fetchCard?cardId={id}
POST /api/cards/upsertCard
DELETE /api/cards/deleteCard?cardId={id}
```

**新 API（RESTful 设计）**：

```
GET    /api/cards?userId={userId}&page=1&limit=20&type={type}&isFavorite={bool}
GET    /api/cards/{id}?userId={userId}
POST   /api/cards?userId={userId}
PUT    /api/cards/{id}?userId={userId}
PATCH  /api/cards/{id}?userId={userId}
DELETE /api/cards/{id}?userId={userId}
```

**同步 API 改进**：

```
旧 API（不符合 RESTful）：
POST /api/sync/push
GET  /api/sync/pull?userId={userId}&entityType={type}&since={token}&limit={limit}

新 API（符合 RESTful）：
POST /api/sync                    - 推送同步数据（创建同步操作）
GET  /api/sync/changes?userId={userId}&entityType={type}&since={token}&limit={limit}  - 获取同步变更（查询同步资源）
```

**主要变化**：

1. URL 路径从动词式（`/fetchCards`, `/push`, `/pull`）改为资源式（`/cards`, `/sync`, `/sync/changes`）
2. 资源 ID 使用路径参数（`/api/cards/{id}`），而不是查询参数
3. 正确使用 HTTP 方法语义（GET、POST、PUT、PATCH、DELETE）
4. 添加分页参数（page、limit）
5. 支持筛选查询参数（type、isFavorite）
6. 响应格式包含分页信息
7. 同步 API 使用子资源 `/changes` 表示变更列表，更符合 RESTful 设计

### 向后兼容

**建议**：

1. 保持旧 API 一段时间（标记为 deprecated）
2. 逐步迁移客户端到新 API
3. 在文档中明确标注新旧 API 的差异

---

## ✅ 验收标准

- [x] API 设计符合 RESTful 规范
- [x] 使用标准 HTTP 方法（GET、POST、PUT、PATCH、DELETE）
- [x] 资源 ID 使用路径参数
- [x] 支持分页和筛选
- [x] 统一的错误响应格式
- [x] 输入验证和权限验证
- [x] 标准化的 DTO 层

---

**文档版本**: v1.0  
**创建日期**: 2026-01-27  
**状态**: 重构完成

# API 设计最佳实践与建议

> 基于业内标准和当前实现的改进建议

## 📋 一、业内规范的 API 接口定义最佳实践

### 1.1 RESTful API 设计原则

#### 核心原则

1. **资源导向（Resource-Oriented）**
   - URL 应该表示资源，而不是操作
   - 使用名词，避免动词
   - 资源应该是复数形式

2. **HTTP 方法语义化**
   - `GET` - 获取资源（幂等、安全）
   - `POST` - 创建资源（非幂等）
   - `PUT` - 完整更新资源（幂等）
   - `PATCH` - 部分更新资源（幂等）
   - `DELETE` - 删除资源（幂等）

3. **状态码标准化**
   - `200 OK` - 成功
   - `201 Created` - 创建成功
   - `204 No Content` - 删除成功
   - `400 Bad Request` - 请求错误
   - `401 Unauthorized` - 未认证
   - `403 Forbidden` - 无权限
   - `404 Not Found` - 资源不存在
   - `409 Conflict` - 资源冲突
   - `500 Internal Server Error` - 服务器错误

### 1.2 标准 RESTful API 设计模式

#### ✅ 推荐的标准设计

```
# 资源集合操作
GET    /api/cards              # 获取卡片列表（支持查询参数）
POST   /api/cards              # 创建新卡片

# 单个资源操作
GET    /api/cards/{id}         # 获取指定卡片
PUT    /api/cards/{id}         # 完整更新卡片
PATCH  /api/cards/{id}         # 部分更新卡片
DELETE /api/cards/{id}         # 删除卡片

# 子资源操作
GET    /api/cards/{id}/tags    # 获取卡片的标签
POST   /api/cards/{id}/tags    # 为卡片添加标签
DELETE /api/cards/{id}/tags/{tagId}  # 删除卡片的标签

# 特殊操作（使用动词，但作为子资源）
POST   /api/cards/{id}/favorite  # 收藏卡片（动作）
POST   /api/cards/{id}/unfavorite # 取消收藏（动作）
```

#### ❌ 不推荐的设计（当前实现）

```
GET  /api/cards/fetchCards?userId={userId}  # ❌ 动词在URL中
GET  /api/cards/fetchCard?cardId={id}       # ❌ 动词在URL中
POST /api/cards/upsertCard                  # ❌ 动词在URL中
DELETE /api/cards/deleteCard?cardId={id}    # ❌ 动词在URL中
```

### 1.3 查询参数设计

#### 标准查询参数

```
# 分页
GET /api/cards?page=1&limit=20

# 筛选
GET /api/cards?userId=123&type=QUOTE&isFavorite=true

# 排序
GET /api/cards?sortBy=createdAt&order=desc

# 搜索
GET /api/cards?q=keyword&searchFields=title,content

# 组合使用
GET /api/cards?userId=123&type=QUOTE&page=1&limit=20&sortBy=createdAt&order=desc
```

### 1.4 请求/响应设计

#### 创建资源（POST）

**请求**：
```http
POST /api/cards
Content-Type: application/json

{
  "type": "QUOTE",
  "title": "Inspirational Quote",
  "content": "The only way to do great work is to love what you do.",
  "userId": "user-123"
}
```

**响应**（201 Created）：
```json
{
  "id": "card-456",
  "type": "QUOTE",
  "title": "Inspirational Quote",
  "content": "The only way to do great work is to love what you do.",
  "userId": "user-123",
  "createdAt": "2026-01-27T10:00:00Z",
  "updatedAt": "2026-01-27T10:00:00Z"
}
```

#### 更新资源（PUT/PATCH）

**PUT - 完整更新**：
```http
PUT /api/cards/card-456
Content-Type: application/json

{
  "type": "QUOTE",
  "title": "Updated Quote",
  "content": "Updated content",
  "userId": "user-123"
}
```

**PATCH - 部分更新**：
```http
PATCH /api/cards/card-456
Content-Type: application/json

{
  "title": "Updated Title"
}
```

#### 列表响应（带分页）

```json
{
  "data": [
    {
      "id": "card-1",
      "title": "Card 1",
      ...
    },
    {
      "id": "card-2",
      "title": "Card 2",
      ...
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

### 1.5 错误响应设计

#### 标准错误格式

```json
{
  "error": {
    "code": "CARD_NOT_FOUND",
    "message": "Card with id 'card-123' not found",
    "details": {
      "resource": "Card",
      "resourceId": "card-123"
    },
    "timestamp": "2026-01-27T10:00:00Z",
    "path": "/api/cards/card-123"
  }
}
```

---

## 🎯 二、资深后端工程师的建议

### 2.1 当前实现的问题分析

#### 问题 1：URL 设计不符合 RESTful 规范

**当前设计**：
```
GET  /api/cards/fetchCards?userId={userId}
GET  /api/cards/fetchCard?cardId={id}
POST /api/cards/upsertCard
DELETE /api/cards/deleteCard?cardId={id}
```

**问题**：
1. ❌ URL 中包含动词（`fetch`, `upsert`, `delete`）
2. ❌ 资源 ID 通过查询参数传递，而不是路径参数
3. ❌ 不符合 RESTful 设计原则，可读性和可维护性差
4. ❌ 不利于 API 版本管理和文档生成

**建议改进**：
```
GET    /api/cards?userId={userId}        # 列表查询
GET    /api/cards/{id}                   # 单个资源
POST   /api/cards                        # 创建
PUT    /api/cards/{id}                   # 完整更新
PATCH  /api/cards/{id}                   # 部分更新
DELETE /api/cards/{id}                   # 删除
```

#### 问题 2：Upsert 操作的语义不清晰

**当前设计**：
```
POST /api/cards/upsertCard  # 创建或更新
```

**问题**：
1. ❌ `upsert` 不是标准的 HTTP 语义
2. ❌ 客户端无法明确知道是创建还是更新
3. ❌ 不符合 RESTful 原则

**建议方案 A：分离创建和更新**（推荐）
```
POST   /api/cards          # 创建（返回 201 Created）
PUT    /api/cards/{id}     # 完整更新（返回 200 OK）
PATCH  /api/cards/{id}     # 部分更新（返回 200 OK）
```

**建议方案 B：如果必须使用 Upsert**
```
PUT /api/cards/{id}  # 如果存在则更新，不存在则创建
# 返回 200 OK（更新）或 201 Created（创建）
```

#### 问题 3：用户隔离设计不清晰

**当前设计**：
```
GET /api/cards/fetchCards?userId={userId}
```

**问题**：
1. ❌ 用户 ID 通过查询参数传递，安全性差
2. ❌ 应该通过认证机制获取当前用户，而不是参数传递

**建议改进**：
```
# 方案 A：通过认证获取用户（推荐）
GET /api/cards  # 自动获取当前登录用户的卡片

# 方案 B：如果需要支持管理员查看其他用户
GET /api/users/{userId}/cards  # 用户资源下的子资源
```

### 2.2 架构层面的建议

#### 建议 1：统一 API 版本管理

```kotlin
// 建议的版本管理
route("/api/v1") {
    cardsApi(...)
    usersApi(...)
    ...
}

// 或者使用 Header
// Accept: application/vnd.myhub.v1+json
```

#### 建议 2：统一错误处理

```kotlin
// 建议的错误处理结构
sealed class ApiError(
    val code: String,
    val message: String,
    val statusCode: HttpStatusCode
) {
    object CardNotFound : ApiError(
        code = "CARD_NOT_FOUND",
        message = "Card not found",
        statusCode = HttpStatusCode.NotFound
    )
    
    object ValidationError : ApiError(
        code = "VALIDATION_ERROR",
        message = "Validation failed",
        statusCode = HttpStatusCode.BadRequest
    )
}
```

#### 建议 3：请求验证和响应标准化

```kotlin
// 建议的请求 DTO
@Serializable
data class CreateCardRequest(
    val type: CardType,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false
)

// 建议的响应 DTO
@Serializable
data class CardResponse(
    val id: String,
    val type: CardType,
    val title: String,
    val content: String,
    val userId: String,
    val tags: List<String>,
    val isFavorite: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

// 建议的分页响应
@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val pagination: PaginationInfo
)

@Serializable
data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val total: Long,
    val totalPages: Int
)
```

#### 建议 4：认证和授权

```kotlin
// 建议的认证中间件
fun Application.configureAuthentication() {
    install(Authentication) {
        bearer("auth-bearer") {
            authenticate { tokenCredential ->
                // 验证 token 并获取用户信息
                val user = validateToken(tokenCredential.token)
                UserPrincipal(user)
            }
        }
    }
}

// 在路由中使用
authenticate("auth-bearer") {
    route("/api/cards") {
        get {
            val user = call.principal<UserPrincipal>()?.user
            // 自动使用当前用户，不需要 userId 参数
        }
    }
}
```

### 2.3 具体改进建议

#### 改进建议 1：重构 CardsApi

**当前实现**：
```kotlin
fun Route.cardsApi(cardService: CardService) {
    route("/api/cards") {
        get("/fetchCards") {
            val userId = call.request.queryParameters["userId"]
            val cards = cardService.getCards(userId)
            call.respond(cards)
        }
    }
}
```

**建议改进**：
```kotlin
fun Route.cardsApi(cardService: CardService) {
    route("/api/cards") {
        // GET /api/cards - 获取当前用户的卡片列表
        get {
            val user = call.principal<UserPrincipal>()?.user
                ?: throw UnauthorizedException()
            
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
            val type = call.request.queryParameters["type"]?.let { CardType.valueOf(it) }
            val isFavorite = call.request.queryParameters["isFavorite"]?.toBoolean()
            
            val result = cardService.getCards(
                userId = user.id,
                page = page,
                limit = limit,
                type = type,
                isFavorite = isFavorite
            )
            
            call.respond(HttpStatusCode.OK, result)
        }
        
        // GET /api/cards/{id} - 获取指定卡片
        get("{id}") {
            val id = call.parameters["id"] ?: throw BadRequestException("Card ID is required")
            val user = call.principal<UserPrincipal>()?.user
                ?: throw UnauthorizedException()
            
            val card = cardService.getCard(id, user.id)
                ?: throw NotFoundException("Card", id)
            
            call.respond(HttpStatusCode.OK, card)
        }
        
        // POST /api/cards - 创建卡片
        post {
            val user = call.principal<UserPrincipal>()?.user
                ?: throw UnauthorizedException()
            
            val request = call.receive<CreateCardRequest>()
            val card = cardService.createCard(request, user.id)
            
            call.respond(HttpStatusCode.Created, card)
        }
        
        // PUT /api/cards/{id} - 完整更新卡片
        put("{id}") {
            val id = call.parameters["id"] ?: throw BadRequestException("Card ID is required")
            val user = call.principal<UserPrincipal>()?.user
                ?: throw UnauthorizedException()
            
            val request = call.receive<UpdateCardRequest>()
            val card = cardService.updateCard(id, request, user.id)
            
            call.respond(HttpStatusCode.OK, card)
        }
        
        // PATCH /api/cards/{id} - 部分更新卡片
        patch("{id}") {
            val id = call.parameters["id"] ?: throw BadRequestException("Card ID is required")
            val user = call.principal<UserPrincipal>()?.user
                ?: throw UnauthorizedException()
            
            val request = call.receive<PartialUpdateCardRequest>()
            val card = cardService.partialUpdateCard(id, request, user.id)
            
            call.respond(HttpStatusCode.OK, card)
        }
        
        // DELETE /api/cards/{id} - 删除卡片
        delete("{id}") {
            val id = call.parameters["id"] ?: throw BadRequestException("Card ID is required")
            val user = call.principal<UserPrincipal>()?.user
                ?: throw UnauthorizedException()
            
            cardService.deleteCard(id, user.id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
```

#### 改进建议 2：Service 层设计

```kotlin
class CardService(
    private val cardRepository: CardRepository
) {
    suspend fun getCards(
        userId: String,
        page: Int = 1,
        limit: Int = 20,
        type: CardType? = null,
        isFavorite: Boolean? = null
    ): PaginatedResponse<CardResponse> {
        val cards = cardRepository.getCards(
            userId = userId,
            page = page,
            limit = limit,
            type = type,
            isFavorite = isFavorite
        )
        
        val total = cardRepository.countCards(userId, type, isFavorite)
        
        return PaginatedResponse(
            data = cards.map { it.toResponse() },
            pagination = PaginationInfo(
                page = page,
                limit = limit,
                total = total,
                totalPages = (total + limit - 1) / limit
            )
        )
    }
    
    suspend fun createCard(request: CreateCardRequest, userId: String): CardResponse {
        // 验证
        validateCardRequest(request)
        
        // 创建
        val card = request.toDomain(userId = userId)
        val created = cardRepository.insertCard(card)
        
        return created.toResponse()
    }
    
    suspend fun updateCard(
        id: String,
        request: UpdateCardRequest,
        userId: String
    ): CardResponse {
        // 验证存在性和权限
        val existing = cardRepository.getCard(id)
            ?: throw NotFoundException("Card", id)
        
        if (existing.userId != userId) {
            throw ForbiddenException("Not authorized to update this card")
        }
        
        // 更新
        val updated = existing.copy(
            title = request.title,
            content = request.content,
            // ...
            updatedAt = Clock.System.now()
        )
        
        val saved = cardRepository.updateCard(updated)
        return saved.toResponse()
    }
}
```

### 2.4 性能优化建议

#### 建议 1：添加缓存层

```kotlin
class CardService(
    private val cardRepository: CardRepository,
    private val cache: Cache<String, CardResponse>
) {
    suspend fun getCard(id: String, userId: String): CardResponse? {
        val cacheKey = "card:$id:$userId"
        
        return cache.get(cacheKey) ?: run {
            val card = cardRepository.getCard(id)
                ?.takeIf { it.userId == userId }
                ?.toResponse()
            
            card?.let { cache.put(cacheKey, it, duration = 5.minutes) }
            card
        }
    }
}
```

#### 建议 2：数据库查询优化

```kotlin
// 使用索引优化查询
// 在数据库层面添加索引：
// CREATE INDEX idx_cards_user_id ON cards(user_id);
// CREATE INDEX idx_cards_user_type ON cards(user_id, type);
// CREATE INDEX idx_cards_user_favorite ON cards(user_id, is_favorite);
```

### 2.5 安全性建议

#### 建议 1：输入验证

```kotlin
fun validateCardRequest(request: CreateCardRequest) {
    require(request.title.isNotBlank()) {
        "Title cannot be blank"
    }
    require(request.content.isNotBlank()) {
        "Content cannot be blank"
    }
    require(request.title.length <= 200) {
        "Title cannot exceed 200 characters"
    }
    require(request.content.length <= 10000) {
        "Content cannot exceed 10000 characters"
    }
}
```

#### 建议 2：速率限制

```kotlin
// 使用 Ktor 的 RateLimiter 插件
install(RateLimiter) {
    registerLimit(
        limit = 100,
        window = 1.minutes,
        keyGenerator = { call -> call.principal<UserPrincipal>()?.user?.id }
    )
}
```

---

## 📝 三、总结

### 3.1 核心改进点

1. **URL 设计**：从动词式改为资源式
2. **HTTP 方法**：正确使用 GET、POST、PUT、PATCH、DELETE
3. **资源 ID**：从查询参数改为路径参数
4. **用户隔离**：通过认证机制而非参数传递
5. **错误处理**：统一错误响应格式
6. **分页支持**：标准化的分页响应
7. **安全性**：添加认证、授权、验证、速率限制

### 3.2 迁移建议

如果当前已有客户端在使用现有 API，建议：

1. **保持向后兼容**：同时支持新旧 API
2. **版本管理**：使用 `/api/v1` 和 `/api/v2` 区分
3. **逐步迁移**：先实现新 API，再逐步迁移客户端
4. **文档更新**：及时更新 API 文档

---

**文档版本**: v1.0  
**创建日期**: 2026-01-27  
**状态**: 建议文档

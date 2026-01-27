# 认证和授权使用指南

> Bearer Token 认证实现说明

## 📋 概述

服务端已实现 Bearer Token 认证机制，所有需要认证的 API 都需要在请求头中携带有效的 token。

---

## 🔐 认证流程

### 1. 登录获取 Token

**请求**：

```http
POST /api/auth/login
Content-Type: application/json

{
  "userId": "user-123"
}
```

**响应**（200 OK）：

```json
{
  "token": "user-123",
  "userId": "user-123",
  "username": "testuser"
}
```

### 2. 使用 Token 访问 API

**请求头**：

```http
Authorization: Bearer {token}
```

**示例**：

```http
GET /api/cards?page=1&limit=20
Authorization: Bearer user-123
```

---

## 📡 API 端点

### 认证相关 API

#### POST /api/auth/login

**描述**：登录并获取 token

**请求体**：

```json
{
  "userId": "user-123"
}
```

**响应**：

```json
{
  "token": "user-123",
  "userId": "user-123",
  "username": "testuser"
}
```

**状态码**：

- `200 OK` - 登录成功
- `400 Bad Request` - 请求参数错误
- `404 Not Found` - 用户不存在
- `401 Unauthorized` - 登录失败

### 需要认证的 API

以下 API 都需要在请求头中携带 `Authorization: Bearer {token}`：

- `GET /api/cards` - 获取卡片列表
- `GET /api/cards/{id}` - 获取指定卡片
- `POST /api/cards` - 创建卡片
- `PUT /api/cards/{id}` - 完整更新卡片
- `PATCH /api/cards/{id}` - 部分更新卡片
- `DELETE /api/cards/{id}` - 删除卡片

**注意**：所有需要认证的 API 都会自动从 token 中提取 userId，**不再需要**在查询参数中传递 `userId`。

---

## 🔧 实现细节

### TokenService

**位置**：`server/src/main/kotlin/tech/zhifu/app/myhub/auth/TokenService.kt`

**功能**：

- `validateToken(token: String): User?` - 验证 token 并返回用户信息
- `generateToken(user: User): String` - 生成 token

**当前实现**（临时方案）：

- Token 验证：简单地从 token 中提取 userId，然后从数据库查询用户
- Token 生成：直接返回 userId 作为 token

**未来改进**：

- 使用 JWT（JSON Web Token）实现真正的 token 验证
- 添加 token 过期机制
- 添加 token 刷新机制
- 添加 token 撤销机制

### UserPrincipal

**位置**：`server/src/main/kotlin/tech/zhifu/app/myhub/auth/UserPrincipal.kt`

**功能**：存储认证后的用户信息，实现 `Principal` 接口

### AuthenticationConfig

**位置**：`server/src/main/kotlin/tech/zhifu/app/myhub/auth/AuthenticationConfig.kt`

**功能**：

- 配置 Ktor Authentication 插件
- 提供扩展函数：`getCurrentUser()` 和 `getCurrentUserId()`

### 认证中间件

**使用方式**：

```kotlin
authenticate("auth-bearer") {
    route("/api/cards") {
        get {
            val userId = call.getCurrentUserId() // 自动从 token 获取
            // ...
        }
    }
}
```

---

## 📝 客户端使用示例

### Kotlin (Ktor Client)

```kotlin
// 1. 登录获取 token
val loginResponse = httpClient.post("${baseUrl}/api/auth/login") {
    contentType(ContentType.Application.Json)
    setBody(LoginRequest(userId = "user-123"))
}.body<LoginResponse>()

val token = loginResponse.token

// 2. 使用 token 访问 API
val cards = httpClient.get("${baseUrl}/api/cards?page=1&limit=20") {
    header("Authorization", "Bearer $token")
}.body<PaginatedResponse<CardResponse>>()
```

### JavaScript/Fetch

```javascript
// 1. 登录获取 token
const loginResponse = await fetch('http://localhost:8083/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ userId: 'user-123' })
});
const { token } = await loginResponse.json();

// 2. 使用 token 访问 API
const cardsResponse = await fetch('http://localhost:8083/api/cards?page=1&limit=20', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const cards = await cardsResponse.json();
```

### cURL

```bash
# 1. 登录获取 token
TOKEN=$(curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-123"}' | jq -r '.token')

# 2. 使用 token 访问 API
curl -X GET "http://localhost:8083/api/cards?page=1&limit=20" \
  -H "Authorization: Bearer $TOKEN"
```

---

## ⚠️ 错误处理

### 401 Unauthorized

**原因**：

- 未提供 Authorization header
- Token 无效或已过期
- Token 格式错误

**响应**：

```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Authentication required",
    "timestamp": "2026-01-27T10:00:00Z",
    "path": "/api/cards"
  }
}
```

### 403 Forbidden

**原因**：

- 用户没有权限访问该资源
- 尝试访问其他用户的资源

**响应**：

```json
{
  "error": {
    "code": "FORBIDDEN",
    "message": "Not authorized to access this card",
    "timestamp": "2026-01-27T10:00:00Z",
    "path": "/api/cards/card-123"
  }
}
```

---

## 🔄 迁移指南

### 从查询参数迁移到 Bearer Token

**旧方式**（已废弃）：

```http
GET /api/cards?userId=user-123
```

**新方式**：

```http
GET /api/cards
Authorization: Bearer user-123
```

**主要变化**：

1. ✅ 不再需要在 URL 中传递 `userId` 参数
2. ✅ 使用 `Authorization: Bearer {token}` header
3. ✅ 更安全（token 不会出现在 URL 中）
4. ✅ 符合 RESTful 最佳实践

---

## 🚀 未来改进计划

### 1. JWT Token 实现

**计划**：

- 使用 JWT 库（如 `io.jsonwebtoken:jjwt`）
- 实现 token 签名和验证
- 添加 token 过期时间
- 在 token payload 中存储用户信息

### 2. Token 刷新机制

**计划**：

- 实现 refresh token
- 添加 token 刷新 API
- 自动刷新过期的 token

### 3. Token 撤销机制

**计划**：

- 实现 token 黑名单
- 支持登出功能
- 支持撤销所有 token

### 4. 密码认证

**计划**：

- 实现密码验证
- 添加密码加密存储
- 支持密码重置

---

**文档版本**: v1.0  
**创建日期**: 2026-01-27  
**状态**: 已实现基础认证功能

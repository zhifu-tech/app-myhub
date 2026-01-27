# JWT 认证实现文档

> 完整的 JWT Token 认证系统实现说明

## 📋 概述

已实现完整的 JWT（JSON Web Token）认证系统，包括：

- ✅ JWT token 生成和验证
- ✅ Access Token 和 Refresh Token 机制
- ✅ Token 过期检查
- ✅ Token 刷新功能
- ✅ 从 `.env` 文件读取配置

## ⚙️ 配置说明

### 环境变量配置

JWT 配置通过环境变量读取，支持以下方式：

1. **`.env` 文件**（推荐，开发环境）
2. **系统环境变量**（生产环境）
3. **默认值**（仅用于开发测试）

**必需配置**：

- `JWT_SECRET` - JWT 签名密钥（**必须设置**，至少 32 个字符）

**可选配置**：

- `JWT_ISSUER` - Token 发行者（默认：myhub-api）
- `JWT_AUDIENCE` - Token 受众（默认：myhub-client）

### 快速配置

1. **复制示例配置文件**：
   ```bash
   cd server
   cp .env.example .env
   ```

2. **生成 JWT Secret**（生产环境必须）：
   ```bash
   # 方式 1: 使用脚本
   ./scripts/generate-jwt-secret.sh
   
   # 方式 2: 使用 OpenSSL
   openssl rand -base64 32
   ```

3. **更新 `.env` 文件**：
   ```bash
   # 编辑 .env 文件，将生成的 secret 填入
   JWT_SECRET=your-generated-secret-here
   ```

**注意**：`.env` 文件已添加到 `.gitignore`，不会被提交到版本控制。

---

## 🔐 JWT Token 设计

### Token 类型

#### Access Token（访问令牌）

**用途**：用于访问受保护的 API 资源

**特点**：

- 过期时间：15 分钟
- 包含用户信息（userId, username）
- 类型标识：`type: "access"`

**Payload 结构**：

```json
{
  "iss": "myhub-api",
  "aud": "myhub-client",
  "userId": "user-123",
  "username": "testuser",
  "type": "access",
  "iat": 1706342400,
  "exp": 1706343300
}
```

#### Refresh Token（刷新令牌）

**用途**：用于刷新过期的 Access Token

**特点**：

- 过期时间：7 天
- 只包含 userId
- 类型标识：`type: "refresh"`

**Payload 结构**：

```json
{
  "iss": "myhub-api",
  "aud": "myhub-client",
  "userId": "user-123",
  "type": "refresh",
  "iat": 1706342400,
  "exp": 1706947200
}
```

---

## 🏗️ 架构设计

### 核心组件

#### 1. JwtConfig

**位置**：`server/src/main/kotlin/tech/zhifu/app/myhub/auth/JwtConfig.kt`

**功能**：

- JWT 配置（secret, issuer, audience）
- Token 生成（access token 和 refresh token）
- Token 验证

**配置项**：

- `JWT_SECRET` - JWT 签名密钥（从环境变量读取，必需）
- `JWT_ISSUER` - Token 发行者（从环境变量读取，默认：myhub-api）
- `JWT_AUDIENCE` - Token 受众（从环境变量读取，默认：myhub-client）
- `ACCESS_TOKEN_EXPIRATION_MS` - Access Token 过期时间（15 分钟）
- `REFRESH_TOKEN_EXPIRATION_MS` - Refresh Token 过期时间（7 天）

**环境变量配置**：
在 `server/.env` 文件中配置：

```bash
JWT_SECRET=your-secret-key-here
JWT_ISSUER=myhub-api
JWT_AUDIENCE=myhub-client
```

**生成 JWT Secret**：

```bash
# 方式 1: 使用提供的脚本（推荐）
./scripts/generate-jwt-secret.sh

# 方式 2: 使用 OpenSSL
openssl rand -base64 32

# 方式 3: 使用在线工具
# 访问 https://www.grc.com/passwords.htm
```

**快速配置**：

1. 复制 `.env.example` 为 `.env`：`cp .env.example .env`
2. `.env` 文件中已包含一个生成的示例 Secret，可以直接使用（开发环境）
3. 生产环境请重新生成 Secret 并更新配置

详细配置说明请参考：[JWT 配置指南](jwt-setup-guide.md)

#### 2. TokenService

**位置**：`server/src/main/kotlin/tech/zhifu/app/myhub/auth/TokenService.kt`

**功能**：

- `validateAccessToken(token: String): User?` - 验证 access token
- `validateRefreshToken(token: String): User?` - 验证 refresh token
- `generateTokens(user: User): TokenPair` - 生成 token 对
- `refreshAccessToken(refreshToken: String): TokenPair` - 刷新 access token

#### 3. AuthenticationConfig

**位置**：`server/src/main/kotlin/tech/zhifu/app/myhub/auth/AuthenticationConfig.kt`

**功能**：

- 配置 Ktor JWT 认证插件
- 提供扩展函数：`getCurrentUserId()` 和 `getCurrentUser()`

---

## 📡 API 端点

### POST /api/auth/login

**描述**：登录并获取 access token 和 refresh token

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
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900,
  "tokenType": "Bearer",
  "userId": "user-123",
  "username": "testuser"
}
```

**字段说明**：

- `accessToken` - Access Token（15 分钟有效）
- `refreshToken` - Refresh Token（7 天有效）
- `expiresIn` - Access Token 过期时间（秒）
- `tokenType` - Token 类型（固定为 "Bearer"）

### POST /api/auth/refresh

**描述**：使用 refresh token 刷新 access token

**请求**：

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应**（200 OK）：

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900,
  "tokenType": "Bearer"
}
```

**错误响应**（401 Unauthorized）：

```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired refresh token",
    "timestamp": "2026-01-27T10:00:00Z",
    "path": "/api/auth/refresh"
  }
}
```

---

## 🔄 使用流程

### 1. 登录流程

```
客户端
  ↓
POST /api/auth/login { userId }
  ↓
服务端验证用户
  ↓
生成 Access Token（15分钟）+ Refresh Token（7天）
  ↓
返回 Token Pair
  ↓
客户端存储 tokens
```

### 2. 访问 API 流程

```
客户端
  ↓
GET /api/cards
Authorization: Bearer {accessToken}
  ↓
服务端验证 JWT token
  - 验证签名
  - 验证 issuer 和 audience
  - 验证 token 类型（必须是 access）
  - 检查是否过期
  ↓
提取 userId 从 token
  ↓
处理请求并返回数据
```

### 3. Token 刷新流程

```
客户端检测到 Access Token 过期
  ↓
POST /api/auth/refresh { refreshToken }
  ↓
服务端验证 Refresh Token
  - 验证签名
  - 验证 token 类型（必须是 refresh）
  - 检查是否过期
  ↓
生成新的 Access Token
  ↓
返回新的 Access Token
  ↓
客户端更新 Access Token
```

---

## 💻 客户端实现示例

### Kotlin (Ktor Client)

```kotlin
class AuthClient(private val httpClient: HttpClient) {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    
    suspend fun login(userId: String): LoginResponse {
        val response = httpClient.post("${baseUrl}/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(userId = userId))
        }.body<LoginResponse>()
        
        accessToken = response.accessToken
        refreshToken = response.refreshToken
        
        return response
    }
    
    suspend fun refreshAccessToken(): RefreshTokenResponse {
        require(refreshToken != null) { "No refresh token available" }
        
        val response = httpClient.post("${baseUrl}/api/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(refreshToken = refreshToken!!))
        }.body<RefreshTokenResponse>()
        
        accessToken = response.accessToken
        return response
    }
    
    suspend fun <T> requestWithAuth(
        block: suspend HttpRequestBuilder.() -> T
    ): T {
        var token = accessToken
        
        // 如果 token 为空，尝试刷新
        if (token == null && refreshToken != null) {
            val refreshResponse = refreshAccessToken()
            token = refreshResponse.accessToken
        }
        
        require(token != null) { "No access token available" }
        
        return try {
            httpClient.request {
                header("Authorization", "Bearer $token")
                block()
            }
        } catch (e: ClientRequestException) {
            // 如果是 401，尝试刷新 token 后重试
            if (e.response.status == HttpStatusCode.Unauthorized && refreshToken != null) {
                val refreshResponse = refreshAccessToken()
                token = refreshResponse.accessToken
                
                // 重试请求
                httpClient.request {
                    header("Authorization", "Bearer $token")
                    block()
                }
            } else {
                throw e
            }
        }
    }
}

// 使用示例
val authClient = AuthClient(httpClient)
authClient.login("user-123")

val cards = authClient.requestWithAuth {
    get("${baseUrl}/api/cards?page=1&limit=20")
}.body<PaginatedResponse<CardResponse>>()
```

### JavaScript/Fetch

```javascript
class AuthClient {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
    this.accessToken = null;
    this.refreshToken = null;
  }
  
  async login(userId) {
    const response = await fetch(`${this.baseUrl}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ userId })
    });
    
    const data = await response.json();
    this.accessToken = data.accessToken;
    this.refreshToken = data.refreshToken;
    return data;
  }
  
  async refreshAccessToken() {
    if (!this.refreshToken) {
      throw new Error('No refresh token available');
    }
    
    const response = await fetch(`${this.baseUrl}/api/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ refreshToken: this.refreshToken })
    });
    
    if (!response.ok) {
      throw new Error('Token refresh failed');
    }
    
    const data = await response.json();
    this.accessToken = data.accessToken;
    return data;
  }
  
  async requestWithAuth(url, options = {}) {
    if (!this.accessToken) {
      await this.refreshAccessToken();
    }
    
    const response = await fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        'Authorization': `Bearer ${this.accessToken}`
      }
    });
    
    // 如果是 401，尝试刷新 token 后重试
    if (response.status === 401 && this.refreshToken) {
      await this.refreshAccessToken();
      return fetch(url, {
        ...options,
        headers: {
          ...options.headers,
          'Authorization': `Bearer ${this.accessToken}`
        }
      });
    }
    
    return response;
  }
}

// 使用示例
const authClient = new AuthClient('http://localhost:8083');
await authClient.login('user-123');

const cardsResponse = await authClient.requestWithAuth(
  `${authClient.baseUrl}/api/cards?page=1&limit=20`
);
const cards = await cardsResponse.json();
```

---

## ⚠️ 错误处理

### 401 Unauthorized

**原因**：

- Access Token 无效或已过期
- Refresh Token 无效或已过期
- Token 格式错误
- Token 签名验证失败

**响应**：

```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired token",
    "timestamp": "2026-01-27T10:00:00Z",
    "path": "/api/cards"
  }
}
```

**处理建议**：

1. 如果是 Access Token 过期，使用 Refresh Token 刷新
2. 如果是 Refresh Token 过期，需要重新登录
3. 如果是其他错误，检查 token 格式和签名

---

## 🔒 安全考虑

### 1. Secret Key 管理

**当前实现**：✅ 已从环境变量读取（`.env` 文件）

**配置方式**：

1. 在 `server/.env` 文件中设置 `JWT_SECRET`
2. 使用提供的脚本生成：`./scripts/generate-jwt-secret.sh`
3. 或手动生成：`openssl rand -base64 32`

**生产环境建议**：

- 使用密钥管理服务（如 AWS Secrets Manager、HashiCorp Vault）
- 定期轮换 Secret Key
- 不同环境使用不同的 Secret Key

### 2. Token 存储

**客户端建议**：

- Access Token：存储在内存中（不持久化）
- Refresh Token：安全存储（加密的本地存储或安全 Cookie）

### 3. HTTPS

**要求**：生产环境必须使用 HTTPS，防止 token 被窃取

### 4. Token 撤销

**未来实现**：

- Token 黑名单机制
- 登出时撤销 token
- 支持撤销所有用户的 token

---

## 📊 Token 过期时间配置

### 当前配置

- **Access Token**：15 分钟
- **Refresh Token**：7 天

### 配置建议

**开发环境**：

- Access Token：1 小时（便于调试）
- Refresh Token：30 天

**生产环境**：

- Access Token：15 分钟（平衡安全性和用户体验）
- Refresh Token：7 天（或根据业务需求调整）

**修改方式**：
在 `JwtConfig.kt` 中修改常量：

```kotlin
const val ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000L  // 15 分钟
const val REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L  // 7 天
```

---

## 🧪 测试示例

### 使用 cURL 测试

```bash
# 1. 登录获取 tokens
LOGIN_RESPONSE=$(curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-123"}')

ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
REFRESH_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.refreshToken')

# 2. 使用 Access Token 访问 API
curl -X GET "http://localhost:8083/api/cards?page=1&limit=20" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# 3. 刷新 Access Token
REFRESH_RESPONSE=$(curl -X POST http://localhost:8083/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}")

NEW_ACCESS_TOKEN=$(echo $REFRESH_RESPONSE | jq -r '.accessToken')
```

---

## ✅ 实现检查清单

- [x] JWT token 生成（Access Token + Refresh Token）
- [x] JWT token 验证（签名、过期、类型检查）
- [x] Access Token 过期机制（15 分钟）
- [x] Refresh Token 过期机制（7 天）
- [x] Token 刷新 API
- [x] Ktor JWT 认证插件配置
- [x] 自动从 token 提取 userId
- [x] 错误处理（401 Unauthorized）

---

## 🚀 未来改进

### 1. Token 撤销机制

- [ ] 实现 token 黑名单
- [ ] 支持登出功能
- [ ] 支持撤销所有 token

### 2. 密码认证

- [ ] 实现密码验证
- [ ] 添加密码加密存储
- [ ] 支持密码重置

### 3. 多设备管理

- [ ] 支持多设备登录
- [ ] 设备管理功能
- [ ] 远程登出功能

### 4. 安全增强

- [ ] 从环境变量读取 Secret Key
- [ ] 实现 token 轮换
- [ ] 添加速率限制

---

**文档版本**: v1.0  
**创建日期**: 2026-01-27  
**状态**: 已实现完整的 JWT 认证系统

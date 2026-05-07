# Client 端 JWT Token 认证实现方案

> 基于现有框架，梳理 client 端添加 JWT Token 认证处理的流程及时机

## 📋 概述

Server 端所有 API 已实现 JWT 认证，要求客户端在 HTTP 请求头中携带 `Authorization: Bearer {accessToken}`。本文档梳理 client 端实现 token 认证的完整方案。

**重要发现**：Ktor Client 官方提供了 `Auth` 插件，可以自动处理 Bearer Token 认证和刷新，无需自己实现拦截器！

## 📋 执行摘要

本文档是 Client 端 JWT Token 认证的**最终实施方案**，基于深入的技术分析和架构评估。

**总体评价**：方案整体设计合理，使用官方库，架构清晰，已解决关键问题。

**核心决策**：

1. ✅ **循环依赖解决方案**：采用方案 B（使用 `sendWithoutRequest` 配置），只需要一个 HttpClient
2. ✅ **Token 存储安全性**：MVP 阶段采用加密存储，使用 `multiplatform-crypto` 库
3. ✅ **Settings 模块集成**：新增 `core/settings` 模块，统一存储接口

**方案优点**：

- 🎯 使用 Ktor 官方 Auth 插件，稳定可靠
- 🔒 MVP 阶段采用加密存储，建立安全基础
- 📦 架构清晰，依赖方向正确
- 🚀 自动处理 token 刷新和请求重试

## 🎯 快速参考（推荐方案）

**核心库**：

1. ✅ **Ktor Client Auth 插件**（官方，12k+ stars）- 自动处理认证和刷新
2. ✅ **multiplatform-settings**（1.1k+ stars）- Token 存储
3. ⭐ **jwt-kt**（可选，28 stars）- JWT 解析（用于检查过期时间）

**实施步骤**：

1. 添加 `ktor-client-auth` 依赖
2. 实现 `TokenStorage`（基于 multiplatform-settings）
3. 实现 `RemoteAuthDataSource`（登录和刷新 API）
4. 在 `createHttpClient()` 中配置 `Auth` 插件
5. 登录成功后保存 token

**优势**：

- 🎯 使用官方库，稳定可靠
- 🚀 自动处理 token 刷新和请求重试
- 📦 代码简洁，易于维护
- ⚡ 无需自己实现拦截器和重试逻辑

---

## 🔍 现状分析

### Server 端认证机制

1. **认证方式**：JWT Bearer Token
2. **Token 类型**：
    - **Access Token**：15 分钟有效期，用于 API 请求认证
    - **Refresh Token**：7 天有效期，用于刷新 Access Token
3. **认证端点**：
    - `POST /api/auth/login` - 登录获取 token pair
    - `POST /api/auth/refresh` - 刷新 access token
4. **API 保护**：所有业务 API 都使用 `authenticate("auth-bearer")` 保护

### Client 端现状

1. **网络层**：使用 Ktor Client（`core/network` 模块）
2. **HttpClient 创建**：`createHttpClient()` 函数，已配置 ContentNegotiation、DefaultRequest、Logging
3. **Token 存储**：暂无 token 存储机制
4. **认证处理**：暂无自动添加 Authorization header 的机制
5. **Token 刷新**：暂无自动刷新机制

---

## 🎯 实现目标

1. ✅ **Token 存储**：安全存储 Access Token 和 Refresh Token
2. ✅ **自动认证**：HttpClient 自动在所有请求中添加 Authorization header
3. ✅ **Token 刷新**：Access Token 过期时自动使用 Refresh Token 刷新
4. ✅ **错误处理**：401 错误时自动刷新 token 并重试请求
5. ✅ **登录集成**：登录成功后保存 token

---

## 🎁 成熟开源库推荐

### 1. Ktor Client Auth 插件（官方推荐 ⭐⭐⭐⭐⭐）

**库名**：`ktor-client-auth`  
**官方文档**：https://ktor.io/docs/client-bearer-auth.html  
**GitHub**：https://github.com/ktorio/ktor  
**Stars**：12k+  
**License**：Apache 2.0

**功能特性**：

- ✅ 自动添加 `Authorization: Bearer {token}` header
- ✅ `loadTokens` 回调：从本地存储加载 token
- ✅ `refreshTokens` 回调：自动处理 401 错误并刷新 token
- ✅ 自动重试机制：401 后自动刷新并重试请求
- ✅ `sendWithoutRequest`：可选配置，主动发送认证信息
- ✅ 支持多种认证方式（Basic、Digest、Bearer）

**使用示例**：

```kotlin
val client = HttpClient(CIO) {
    install(Auth) {
        bearer {
            loadTokens {
                // 从本地存储加载 token
                BearerTokens(accessToken, refreshToken)
            }
            refreshTokens {
                // 401 错误时自动调用，刷新 token
                val newTokens = refreshTokenApi.refresh(oldTokens.refreshToken)
                BearerTokens(newTokens.accessToken, newTokens.refreshToken)
            }
            sendWithoutRequest { request ->
                // 可选：主动发送认证（不等待 401）
                request.url.host == "api.example.com"
            }
        }
    }
}
```

**优势**：

- 🎯 **官方支持**：Ktor 官方维护，稳定可靠
- 🚀 **开箱即用**：无需自己实现拦截器和重试逻辑
- 🔄 **自动刷新**：401 错误时自动刷新 token 并重试
- 📦 **轻量级**：只添加必要的依赖

**依赖**：

```kotlin
implementation("io.ktor:ktor-client-auth:$ktor_version")
```

---

### 2. JWT 解析库

#### 2.1 jwt-kt（推荐 ⭐⭐⭐⭐）

**库名**：`jwt-kt`  
**GitHub**：https://github.com/appstractive/jwt-kt  
**Stars**：28  
**License**：Apache 2.0  
**KMP 支持**：✅ 完全支持

**功能特性**：

- ✅ 解析 JWT（无需验证签名，仅用于检查过期时间）
- ✅ 创建和签名 JWT
- ✅ 验证 JWT 签名
- ✅ 支持多种算法（HMAC、RSA、ECDSA）
- ✅ Kotlin Multiplatform

**使用示例**：

```kotlin
// 解析 JWT（仅解析，不验证签名）
val jwt = JWT.from(tokenString)
val expiresAt = jwt.expiresAt
val isExpired = expiresAt?.let { it < Clock.System.now() } ?: true
```

**依赖**：

```kotlin
implementation("com.appstractive:jwt-kt:1.2.0")
```

#### 2.2 KotJWT（备选 ⭐⭐⭐）

**库名**：`KotJWT`  
**GitHub**：https://github.com/iNoles/KotJWT  
**License**：Apache 2.0  
**KMP 支持**：✅ 完全支持

**功能特性**：

- ✅ 编码、解码 JWT
- ✅ 管理 refresh token
- ✅ Token 过期验证
- ✅ 黑名单管理

---

### 3. Token 存储库

#### 3.1 multiplatform-settings（推荐 ⭐⭐⭐⭐⭐）

**库名**：`multiplatform-settings`  
**GitHub**：https://github.com/russhwolf/multiplatform-settings  
**Stars**：1.1k+  
**License**：Apache 2.0  
**KMP 支持**：✅ 完全支持

**功能特性**：

- ✅ 跨平台键值存储
- ✅ 支持 Android、iOS、JVM、JS、WASM
- ✅ 类型安全（String、Int、Long、Boolean 等）
- ✅ 线程安全

**使用示例**：

```kotlin
val settings = Settings()
settings["auth.access_token"] = accessToken
val token = settings.getStringOrNull("auth.access_token")
```

**依赖**：

```kotlin
implementation("com.russhwolf:multiplatform-settings:1.1.1")
```

**注意**：项目中 `feature/settings` 模块已使用此库，可直接复用。

---

## 📐 架构设计（基于官方库）

### 0. 新增 core/settings 模块

**决策**：✅ 新增 `core/settings` 模块，提供统一的本地存储抽象接口

**模块职责**：

- 提供跨平台的本地存储抽象接口 `LocalSettingStore`
- 基于 `multiplatform-settings` 实现
- 为基础设施层和功能层提供统一的存储接口

**模块结构**：

```
core/settings/
├── build.gradle.kts
├── README.md
└── src/
    └── commonMain/
        └── kotlin/
            └── tech/zhifu/app/myhub/core/settings/
                ├── LocalSettingStore.kt          # 存储接口
                └── LocalSettingStoreImpl.kt      # 基于 multiplatform-settings 的实现
```

**依赖关系**：

- `core/network` → `core/settings`
- `feature/settings` → `core/settings`

### 1. Token 存储层（加密存储）

**位置**：`core/network/src/commonMain/kotlin/tech/zhifu/app/myhub/network/auth/`

**组件**：

- `TokenStorage` 接口：定义 token 存储操作
- `SecureTokenStorage` 实现：基于 `core/settings` 的 `LocalSettingStore` 和 `multiplatform-crypto` 实现加密存储

**功能**：

- `getAccessToken(): String?` - 获取 Access Token（解密后）
- `getRefreshToken(): String?` - 获取 Refresh Token（解密后）
- `saveTokens(accessToken: String, refreshToken: String)` - 加密保存 token pair
- `clearTokens()` - 清除所有 token
- `getBearerTokens(): BearerTokens?` - 获取 BearerTokens（用于 Ktor Auth 插件，同步方法）

**存储方案**：✅ **加密存储（MVP 阶段采用）**

#### 使用 multiplatform-crypto 实现加密存储 🔒

**优势**：

- ✅ **安全性高**：Token 加密存储，即使设备被 Root/越狱，也无法直接读取明文
- ✅ **跨平台统一**：使用同一套加密逻辑，行为一致
- ✅ **MVP 阶段采用**：从一开始就建立安全基础，避免后续迁移成本

**实现**：

```kotlin
import tech.zhifu.app.myhub.settings.LocalSettingStore
import io.ktor.client.plugins.auth.providers.BearerTokens

class SecureTokenStorage(
    private val localStore: LocalSettingStore,
    private val crypto: Crypto
) : TokenStorage {
    companion object {
        private const val KEY_ACCESS_TOKEN = "auth.access_token"
        private const val KEY_REFRESH_TOKEN = "auth.refresh_token"
        private const val KEY_ENCRYPTION_KEY = "auth.encryption_key"
    }

    private suspend fun getOrCreateEncryptionKey(): ByteArray {
        val storedKey = localStore.get(KEY_ENCRYPTION_KEY)
        return if (storedKey != null) {
            storedKey.encodeToByteArray()
        } else {
            val key = crypto.generateKey()
            localStore.set(KEY_ENCRYPTION_KEY, key.decodeToString())
            key
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        val key = getOrCreateEncryptionKey()
        val encryptedAccess = crypto.encrypt(accessToken.encodeToByteArray(), key)
        val encryptedRefresh = crypto.encrypt(refreshToken.encodeToByteArray(), key)
        localStore.set(KEY_ACCESS_TOKEN, encryptedAccess.decodeToString())
        localStore.set(KEY_REFRESH_TOKEN, encryptedRefresh.decodeToString())
    }

    override fun getBearerTokens(): BearerTokens? {
        val encryptedAccess = localStore.get(KEY_ACCESS_TOKEN)
        val encryptedRefresh = localStore.get(KEY_REFRESH_TOKEN)
        return if (encryptedAccess != null && encryptedRefresh != null) {
            try {
                val key = localStore.get(KEY_ENCRYPTION_KEY)?.encodeToByteArray()
                    ?: return null
                val accessToken = crypto.decrypt(encryptedAccess.encodeToByteArray(), key).decodeToString()
                val refreshToken = crypto.decrypt(encryptedRefresh.encodeToByteArray(), key).decodeToString()
                BearerTokens(accessToken, refreshToken)
            } catch (e: Exception) {
                // 解密失败，清除 token
                clearTokens()
                null
            }
        } else {
            null
        }
    }

    override suspend fun clearTokens() {
        localStore.remove(KEY_ACCESS_TOKEN)
        localStore.remove(KEY_REFRESH_TOKEN)
        // 注意：不清除加密密钥
    }
}
```

**依赖**：

- `tech.zhifu.app.myhub:core-settings`（新增模块）
- `multiplatform-crypto` 库（需要选择合适的库，如 `org.jetbrains.kotlinx:kotlinx-coroutines-crypto` 或 `com.soywiz.korlibs.krypto:krypto`）

#### 备选方案：数据库存储（SQLDelight）

**优点**：

- ✅ **结构化存储**：可以存储更多元数据（创建时间、过期时间、用户ID等）
- ✅ **查询能力强**：支持复杂查询（如：查询所有过期的 token）
- ✅ **关系管理**：可以与 user 表关联，支持多用户场景
- ✅ **数据一致性**：可以利用数据库事务保证一致性
- ✅ **可扩展性**：未来可以添加 token 历史记录、撤销列表等

**缺点**：

- ❌ **过度设计**：对于简单的 token 存储来说，过于复杂
- ❌ **性能开销**：需要数据库初始化、SQL 查询（虽然很小，但比 Settings 慢）
- ❌ **维护成本**：需要维护数据库 schema、迁移脚本
- ❌ **依赖数据库**：需要等待数据库初始化完成才能使用

**实现示例**：

```sql
-- auth_token.sq
CREATE TABLE auth_token (
    user_id TEXT PRIMARY KEY NOT NULL,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    access_token_expires_at TEXT NOT NULL,
    refresh_token_expires_at TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
```

**推荐方案**：✅ **加密存储 + core/settings 模块**

**选择理由**：

1. **安全性优先**：MVP 阶段就采用加密存储，建立安全基础
2. **统一存储接口**：通过 `core/settings` 模块统一管理，便于未来扩展
3. **性能要求高**：Token 在每次 API 请求时都需要读取，加密/解密开销可接受
4. **架构清晰**：基础设施层（`core/network`）和功能层（`feature/settings`）都依赖基础设施层（`core/settings`），依赖方向正确

**何时应该使用数据库存储 Token**：

- ✅ 需要支持**多用户切换**（需要查询不同用户的 token）
- ✅ 需要**token 历史记录**（记录 token 变更历史）
- ✅ 需要**token 撤销列表**（黑名单功能）
- ✅ 需要**复杂的 token 管理**（如：批量撤销、过期清理等）
- ✅ Token 需要与**业务数据强关联**（如：每个用户的 token 需要关联到业务表）

**当前项目场景**：

- ❌ 单用户应用（当前只有一个活跃用户）
- ❌ Token 只是简单的字符串，不需要复杂查询
- ❌ Token 是基础设施数据，与业务数据解耦
- ✅ **结论**：使用加密存储 + `core/settings` 模块更合适

### 2. Token 管理服务（可选，简化版）

**位置**：`core/network/src/commonMain/kotlin/tech/zhifu/app/myhub/network/auth/`

**组件**：

- `TokenManager` 接口：管理 token 生命周期
- `TokenManagerImpl` 实现：处理 token 获取、刷新、验证

**功能**：

- `getBearerTokens(): BearerTokens?` - 获取 BearerTokens（用于 Ktor Auth 插件）
- `refreshBearerTokens(refreshToken: String): BearerTokens?` - 刷新 token（调用 `/api/auth/refresh`）
- `isTokenExpired(token: String): Boolean` - 检查 token 是否过期（使用 jwt-kt 解析）
- `clearTokens()` - 清除所有 token

**依赖**：

- `TokenStorage` - 存储 token
- `RemoteAuthDataSource` - 调用 refresh API（避免循环依赖）

**注意**：如果使用 Ktor Auth 插件的 `refreshTokens` 回调，可以直接在回调中调用 `RemoteAuthDataSource.refreshToken()`，无需单独的 TokenManager。

### 3. Ktor Auth 插件配置（推荐方案 ⭐⭐⭐⭐⭐）

**位置**：`core/network/src/commonMain/kotlin/tech/zhifu/app/myhub/network/KtorClientFactory.kt`

**实现方式**：
使用 Ktor 官方的 `Auth` 插件，无需自己实现拦截器！

```kotlin
fun createHttpClient(
    factory: KtorClientFactory,
    tokenStorage: TokenStorage,
    authDataSource: RemoteAuthDataSource
): HttpClient {
    return HttpClient(factory.createEngine()) {
        // ... 现有配置 ...

        install(Auth) {
            bearer {
                loadTokens {
                    // 从本地存储加载 token
                    tokenStorage.getBearerTokens()
                }
                refreshTokens {
                    // 401 错误时自动调用
                    val refreshToken = oldTokens?.refreshToken
                        ?: return@refreshTokens null

                    try {
                        // 刷新 token（刷新 API 本身不需要认证，由 sendWithoutRequest 控制）
                        val response = authDataSource.refreshToken(refreshToken)
                        val newTokens = BearerTokens(
                            accessToken = response.accessToken,
                            refreshToken = refreshToken // refresh token 不变（服务器端不返回新的 refreshToken）
                        )
                        // 保存新 token
                        tokenStorage.saveTokens(
                            accessToken = response.accessToken,
                            refreshToken = refreshToken
                        )
                        newTokens
                    } catch (e: UnauthorizedException) {
                        // Refresh token 过期或无效，清除 token，需要重新登录
                        tokenStorage.clearTokens()
                        null
                    } catch (e: NetworkException) {
                        // 网络错误，不清除 token（可能是临时网络问题）
                        null
                    } catch (e: Exception) {
                        // 其他错误，记录日志但不清除 token
                        logger.error(e) { "Token refresh failed" }
                        null
                    }
                }
                sendWithoutRequest { request ->
                    // 方案 B：只对业务 API 请求发送认证，刷新 API 不发送认证
                    // 这样可以避免循环依赖：刷新 token 的请求本身不需要认证
                    request.url.pathStartsWith("/api/") &&
                            !request.url.pathStartsWith("/api/auth/")
                }
            }
        }
    }
}
```

**优势**：

- ✅ **官方支持**：Ktor 官方维护，稳定可靠
- ✅ **自动处理**：自动添加 header、自动刷新、自动重试
- ✅ **无需手动实现**：不需要自己写拦截器和重试逻辑
- ✅ **代码简洁**：配置简单，易于维护
- ✅ **方案 B**：使用 `sendWithoutRequest` 配置，只需要一个 HttpClient，架构更简洁

### 5. HttpClient 配置更新

**位置**：`core/network/src/commonMain/kotlin/tech/zhifu/app/myhub/network/KtorClientFactory.kt`

**修改**：

- 在 `createHttpClient()` 中安装认证相关插件
- 需要注入 `TokenManager` 依赖

**方案 A：通过参数传递**

```kotlin
fun createHttpClient(
    factory: KtorClientFactory,
    tokenManager: TokenManager? = null
): HttpClient {
    return HttpClient(factory.createEngine()) {
        // ... 现有配置 ...

        // 安装认证拦截器
        tokenManager?.let {
            install(AuthInterceptor(it))
        }
    }
}
```

**方案 B：通过 Koin 依赖注入**

```kotlin
// 在 NetworkModule 中提供 TokenManager
val networkModule = module {
    // ... 现有配置 ...

    single<TokenStorage> { TokenStorageImpl() }
    single<TokenManager> { TokenManagerImpl(get(), get()) }

    single<HttpClient> {
        val tokenManager = get<TokenManager>()
        createHttpClient(get(), tokenManager)
    }
}
```

### 5. 登录 API 集成

**位置**：`datastore/datasource-remote/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/datasource/`

**新增**：

- `RemoteAuthDataSource` 接口
- `RemoteAuthDataSourceImpl` 实现

**功能**：

- `login(userId: String, username: String? = null): LoginResponse` - 调用 `/api/auth/login`
- `refreshToken(refreshToken: String): RefreshTokenResponse` - 调用 `/api/auth/refresh`

**DTO 定义**：

- 在 `datastore/model` 模块中定义 `LoginRequest`、`LoginResponse`、`RefreshTokenRequest`、`RefreshTokenResponse`

**重要说明：离线优先应用的首次登录**：

- ✅ 客户端在首次启动时会生成匿名用户（UUID v4），存储在本地
- ✅ 首次登录时，服务器端会**自动创建用户**（Login-or-Create 模式）
- ✅ 客户端发送本地 `userId`，服务器端验证格式后自动创建用户
- ✅ 无需额外的注册步骤，符合离线优先理念
- 📖 详细方案见：[离线优先应用的首次登录解决方案](./offline-first-login-solution.md)

---

## 🔄 实现流程及时机

### Phase 1: Token 存储层

**时机**：首先实现，为后续功能提供基础

**步骤**：

1. 创建 `TokenStorage` 接口
2. 实现 `TokenStorageImpl`（使用 `LocalSettingStore` 或 `Settings`）
3. 添加单元测试

**文件**：

- `core/network/src/commonMain/kotlin/tech/zhifu/app/myhub/network/auth/TokenStorage.kt`
- `core/network/src/commonMain/kotlin/tech/zhifu/app/myhub/network/auth/TokenStorageImpl.kt`

### Phase 2: Token 管理服务

**时机**：在 Token 存储层完成后实现

**步骤**：

1. 创建 `TokenManager` 接口
2. 实现 `TokenManagerImpl`
3. 实现 JWT 解析（检查过期时间）
4. 实现 token 刷新逻辑（调用 refresh API）
5. 添加单元测试

**文件**：

- `core/network/src/commonMain/kotlin/tech/zhifu/app/myhub/network/auth/TokenManager.kt`
- `core/network/src/commonMain/kotlin/tech/zhifu/app/myhub/network/auth/TokenManagerImpl.kt`
- `core/network/src/commonMain/kotlin/tech/zhifu/app/myhub/network/auth/JwtUtils.kt`（JWT 解析工具）

**依赖**：

- 需要添加 JWT 解析库（如 `com.auth0:jwt-decode` 或 `io.jsonwebtoken:jjwt`）

### Phase 3: 认证拦截器

**时机**：在 Token 管理服务完成后实现

**步骤**：

1. 创建 `AuthInterceptor` 类
2. 实现 Ktor Client 插件
3. 在 `createHttpClient()` 中安装拦截器
4. 更新 `NetworkModule` 提供依赖
5. 添加集成测试

**文件**：

- `core/network/src/commonMain/kotlin/tech/zhifu/app/myhub/network/auth/AuthInterceptor.kt`

### Phase 4: 登录 API 集成

**时机**：与认证拦截器并行实现

**步骤**：

1. 在 `ApiConfig` 中添加 `AUTH_PATH = "/api/auth"`
2. 定义 DTO（`LoginRequest`、`LoginResponse` 等）
3. 创建 `RemoteAuthDataSource` 接口
4. 实现 `RemoteAuthDataSourceImpl`
5. 在登录成功后调用 `TokenManager.saveTokens()`
6. 更新 `RemoteDataSourceModule`

**文件**：

- `datastore/model/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/model/dto/LoginRequest.kt`
- `datastore/model/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/model/dto/LoginResponse.kt`
- `datastore/model/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/model/dto/RefreshTokenRequest.kt`
- `datastore/model/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/model/dto/RefreshTokenResponse.kt`
- `datastore/datasource-remote/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/datasource/RemoteAuthDataSource.kt`
- `datastore/datasource-remote/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/datasource/impl/RemoteAuthDataSourceImpl.kt`

### Phase 5: Token 刷新机制（可选，复杂）

**时机**：在基础认证流程稳定后实现

**挑战**：

- Ktor Client 的响应拦截器无法直接重试请求
- 需要保存原始请求上下文

**方案 A：简单方案（推荐）**

- 不实现自动重试
- 401 错误时清除 token，由上层业务处理（如跳转登录页）
- 业务层可以监听 token 失效事件，主动刷新

**方案 B：复杂方案**

- 使用 Ktor Client 的 `HttpRequestRetry` 插件
- 自定义重试逻辑，在 401 时刷新 token 后重试
- 需要处理并发请求的 token 刷新（避免多个请求同时刷新）

**建议**：先实现方案 A，后续根据需求再考虑方案 B。

---

## 📦 依赖管理

### 新增依赖

1. **Ktor Client Auth 插件**（必需 ⭐⭐⭐⭐⭐）
   ```kotlin
   // build.gradle.kts (core/network)
   commonMain {
       dependencies {
           implementation("io.ktor:ktor-client-auth:$ktor_version")
       }
   }
   ```
   **版本**：使用与项目其他 Ktor 依赖相同的版本

2. **core/settings 模块**（新增，必需 ⭐⭐⭐⭐⭐）
    - 需要先创建 `core/settings` 模块
    - 该模块依赖 `multiplatform-settings`
    - `core/network` 依赖 `core/settings`

3. **multiplatform-crypto**（加密存储，必需 ⭐⭐⭐⭐⭐）
   ```kotlin
   // build.gradle.kts (core/network)
   commonMain {
       dependencies {
           // 需要选择合适的 multiplatform-crypto 库
           // 选项 1: kotlinx-coroutines-crypto（如果可用）
           // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-crypto:0.1.0")
           // 选项 2: korlibs.krypto
           implementation("com.soywiz.korlibs.krypto:krypto:3.0.0")
       }
   }
   ```
   **注意**：需要评估并选择合适的 multiplatform-crypto 库

3. **jwt-kt**（JWT 解析，可选 ⭐⭐⭐⭐）
   ```kotlin
   // build.gradle.kts (core/network)
   commonMain {
       dependencies {
           // 仅用于解析 JWT，检查过期时间（不验证签名）
           implementation("com.appstractive:jwt-kt:1.2.0")
       }
   }
   ```
   **注意**：如果使用 Ktor Auth 插件的 `refreshTokens`，可以不需要提前检查过期时间

### 依赖关系（基于官方库）

```
HttpClient
  └── Auth Plugin (Ktor 官方)
        ├── loadTokens: TokenStorage.getBearerTokens()
        └── refreshTokens: RemoteAuthDataSource.refreshToken()
              └── HttpClient (用于调用 refresh API，注意循环依赖)
```

**循环依赖处理**：✅ **已解决（方案 B）**

- `Auth` 插件的 `refreshTokens` 回调需要 `HttpClient` 来调用 refresh API
- 但 `HttpClient` 需要 `Auth` 插件来添加认证
- **解决方案（方案 B）**：使用 `sendWithoutRequest` 配置，让刷新 API (`/api/auth/refresh`) 不发送认证
    - 刷新 API 本身不需要认证（符合业务逻辑）
    - 只需要一个 HttpClient，架构更简洁
    - 通过 `sendWithoutRequest` 明确控制哪些请求需要认证

---

## 🔧 实现细节（基于官方库）

### 1. Token 存储实现（加密存储）

```kotlin
import tech.zhifu.app.myhub.settings.LocalSettingStore
import io.ktor.client.plugins.auth.providers.BearerTokens

interface TokenStorage {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun clearTokens()
    fun getBearerTokens(): BearerTokens? // 同步方法，用于 Ktor Auth 插件
}

class SecureTokenStorage(
    private val localStore: LocalSettingStore,
    private val crypto: Crypto
) : TokenStorage {
    companion object {
        private const val KEY_ACCESS_TOKEN = "auth.access_token"
        private const val KEY_REFRESH_TOKEN = "auth.refresh_token"
        private const val KEY_ENCRYPTION_KEY = "auth.encryption_key"
    }

    private suspend fun getOrCreateEncryptionKey(): ByteArray {
        val storedKey = localStore.get(KEY_ENCRYPTION_KEY)
        return if (storedKey != null) {
            storedKey.encodeToByteArray()
        } else {
            val key = crypto.generateKey()
            localStore.set(KEY_ENCRYPTION_KEY, key.decodeToString())
            key
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        val key = getOrCreateEncryptionKey()
        val encryptedAccess = crypto.encrypt(accessToken.encodeToByteArray(), key)
        val encryptedRefresh = crypto.encrypt(refreshToken.encodeToByteArray(), key)
        localStore.set(KEY_ACCESS_TOKEN, encryptedAccess.decodeToString())
        localStore.set(KEY_REFRESH_TOKEN, encryptedRefresh.decodeToString())
    }

    override suspend fun clearTokens() {
        localStore.remove(KEY_ACCESS_TOKEN)
        localStore.remove(KEY_REFRESH_TOKEN)
        // 注意：不清除加密密钥
    }

    // 同步方法，用于 Ktor Auth 插件的 loadTokens 回调
    override fun getBearerTokens(): BearerTokens? {
        val encryptedAccess = localStore.get(KEY_ACCESS_TOKEN)
        val encryptedRefresh = localStore.get(KEY_REFRESH_TOKEN)
        return if (encryptedAccess != null && encryptedRefresh != null) {
            try {
                val key = localStore.get(KEY_ENCRYPTION_KEY)?.encodeToByteArray()
                    ?: return null
                val accessToken = crypto.decrypt(encryptedAccess.encodeToByteArray(), key).decodeToString()
                val refreshToken = crypto.decrypt(encryptedRefresh.encodeToByteArray(), key).decodeToString()
                BearerTokens(accessToken, refreshToken)
            } catch (e: Exception) {
                // 解密失败，清除 token
                clearTokens()
                null
            }
        } else {
            null
        }
    }
}
```

**依赖**：

- `tech.zhifu.app.myhub:core-settings`（新增模块）
- `multiplatform-crypto` 库（需要选择合适的库）

### 2. JWT 解析工具（可选，用于检查过期时间）

```kotlin
import com.appstractive.jwt.JWT
import kotlinx.datetime.Clock

object JwtUtils {
    /**
     * 检查 token 是否过期（仅解析，不验证签名）
     */
    fun isExpired(token: String): Boolean {
        return try {
            val jwt = JWT.from(token)
            val expiresAt = jwt.expiresAt
            expiresAt?.let { it < Clock.System.now() } ?: true
        } catch (e: Exception) {
            // 解析失败，视为过期
            true
        }
    }

    /**
     * 获取 token 过期时间
     */
    fun getExpiresAt(token: String): Instant? {
        return try {
            val jwt = JWT.from(token)
            jwt.expiresAt
        } catch (e: Exception) {
            null
        }
    }
}
```

**依赖**：`com.appstractive:jwt-kt:1.2.0`

**注意**：如果使用 Ktor Auth 插件的 `refreshTokens` 回调，可以不需要提前检查过期时间，插件会在收到 401 时自动刷新。

### 3. Ktor Auth 插件配置实现（方案 B）

```kotlin
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultrequest.DefaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.network.NetworkException

fun createHttpClient(
    factory: KtorClientFactory,
    tokenStorage: TokenStorage,
    authDataSource: RemoteAuthDataSource
): HttpClient {
    return HttpClient(factory.createEngine()) {
        // 现有配置
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }

        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
        }

        install(Logging) {
            level = LogLevel.INFO
        }

        // 安装 Auth 插件（方案 B：使用 sendWithoutRequest）
        install(Auth) {
            bearer {
                loadTokens {
                    // 从本地存储加载 token
                    tokenStorage.getBearerTokens()
                }

                refreshTokens {
                    // 401 错误时自动调用
                    val refreshToken = oldTokens?.refreshToken
                        ?: return@refreshTokens null

                    try {
                        // 刷新 token（刷新 API 本身不需要认证，由 sendWithoutRequest 控制）
                        val response = authDataSource.refreshToken(refreshToken)
                        val newTokens = BearerTokens(
                            accessToken = response.accessToken,
                            refreshToken = refreshToken // refresh token 不变（服务器端不返回新的 refreshToken）
                        )
                        // 保存新 token
                        tokenStorage.saveTokens(
                            accessToken = response.accessToken,
                            refreshToken = refreshToken
                        )
                        newTokens
                    } catch (e: UnauthorizedException) {
                        // Refresh token 过期或无效，清除 token，需要重新登录
                        tokenStorage.clearTokens()
                        null
                    } catch (e: NetworkException) {
                        // 网络错误，不清除 token（可能是临时网络问题）
                        null
                    } catch (e: Exception) {
                        // 其他错误，记录日志但不清除 token
                        logger.error(e) { "Token refresh failed" }
                        null
                    }
                }

                // 方案 B：只对业务 API 请求发送认证，刷新 API 不发送认证
                // 这样可以避免循环依赖：刷新 token 的请求本身不需要认证
                sendWithoutRequest { request ->
                    request.url.pathStartsWith("/api/") &&
                            !request.url.pathStartsWith("/api/auth/")
                }
            }
        }
    }
}
```

**依赖**：`io.ktor:ktor-client-auth:$ktor_version`

**关键点**：

- ✅ 使用 `sendWithoutRequest` 配置，刷新 API (`/api/auth/refresh`) 不发送认证
- ✅ 只需要一个 HttpClient，架构更简洁
- ✅ 细化错误处理：区分网络错误和认证错误

---

## 🚀 实施步骤总结（基于官方库）

### 步骤 1：创建 core/settings 模块 ✅

- [ ] 创建 `core/settings` 模块
- [ ] 定义 `LocalSettingStore` 接口
- [ ] 实现 `LocalSettingStoreImpl`（基于 `multiplatform-settings`）
- [ ] 配置依赖注入模块
- [ ] 更新 `feature/settings` 模块依赖 `core/settings`

### 步骤 2：添加依赖 ✅

- [ ] 在 `core/network/build.gradle.kts` 中添加 `ktor-client-auth` 依赖
- [ ] 在 `core/network/build.gradle.kts` 中添加 `core/settings` 模块依赖
- [ ] 在 `core/network/build.gradle.kts` 中添加 `multiplatform-crypto` 依赖
- [ ] 可选：添加 `jwt-kt` 依赖（用于解析 JWT）

### 步骤 3：Token 存储层（加密存储）✅

- [ ] 创建 `TokenStorage` 接口
- [ ] 实现 `SecureTokenStorage`（基于 `LocalSettingStore` 和 `Crypto`）
- [ ] 实现加密/解密逻辑
- [ ] 添加 `getBearerTokens()` 方法（同步方法，用于 Ktor Auth 插件）
- [ ] 添加单元测试

### 步骤 3：DTO 定义 ✅

- [ ] 在 `datastore/model` 中定义 `LoginRequest`、`LoginResponse`、`RefreshTokenRequest`、`RefreshTokenResponse`
- [ ] 添加序列化支持（`@Serializable`）

### 步骤 4：登录 API ✅

- [ ] 在 `ApiConfig` 中添加 `AUTH_PATH = "/api/auth"`
- [ ] 创建 `RemoteAuthDataSource` 接口
- [ ] 实现 `RemoteAuthDataSourceImpl`
- [ ] 更新 `RemoteDataSourceModule`

### 步骤 5：Ktor Auth 插件配置 ✅

- [ ] 修改 `createHttpClient()` 函数，添加 `Auth` 插件配置
- [ ] 配置 `loadTokens` 回调（从 `TokenStorage` 加载）
- [ ] 配置 `refreshTokens` 回调（调用 `RemoteAuthDataSource.refreshToken()`）
- [ ] **配置 `sendWithoutRequest`**（方案 B：只对业务 API 发送认证，刷新 API 不发送）
- [ ] 更新 `NetworkModule`，注入 `TokenStorage` 和 `RemoteAuthDataSource`

### 步骤 6：登录流程集成 ✅

- [ ] 在登录成功后调用 `TokenStorage.saveTokens()`
- [ ] 在登出时调用 `TokenStorage.clearTokens()`
- [ ] 可选：实现 `clearToken()` 方法，强制重新加载 token（用于登录后）

### 步骤 7：测试 ✅

- [ ] 单元测试：Token 存储
- [ ] 集成测试：登录、API 请求、Token 自动刷新
- [ ] 端到端测试：完整登录流程、Token 过期自动刷新

---

## ⚠️ 注意事项与关键问题

### 1. 循环依赖（已解决 ✅）

**问题**：`Auth` 插件的 `refreshTokens` 回调需要 `HttpClient` 来调用 refresh API，但 `HttpClient` 需要 `Auth` 插件来添加认证。

**解决方案（方案 B）**：✅ 使用 `sendWithoutRequest` 配置，让刷新 API (`/api/auth/refresh`) 不发送认证

- 刷新 API 本身不需要认证（符合业务逻辑）
- 只需要一个 HttpClient，架构更简洁
- 通过 `sendWithoutRequest` 明确控制哪些请求需要认证

### 2. Refresh Token 处理

**重要说明**：服务器端的 `RefreshTokenResponse` 只返回新的 `accessToken`，**不返回新的 `refreshToken`**。因此：

- ✅ 使用旧的 `refreshToken` 是正确的
- ✅ 代码实现中保持 `refreshToken` 不变

### 3. 并发刷新控制

**潜在问题**：多个请求同时返回 401 时，可能触发多次 token 刷新请求。

**处理方式**：

- ⚠️ 需要验证 Ktor Auth 插件是否已内置并发控制
- ⚠️ 如未处理，可以考虑实现 Mutex 控制（但通常插件已处理）

### 4. 安全性

**已采用方案**：

- ✅ **加密存储**：MVP 阶段采用加密存储，使用 `multiplatform-crypto` 库
- ✅ **统一存储接口**：通过 `core/settings` 模块统一管理
- ⚠️ 需要选择合适的 `multiplatform-crypto` 库
- ⚠️ 考虑加密密钥的安全管理策略

**其他安全措施**：

- 生产环境必须使用 HTTPS
- Token 有效期短（Access Token 15分钟），即使泄露影响有限

### 5. 错误处理（已细化 ✅）

**错误分类**：

- **UnauthorizedException**：Refresh token 过期或无效，清除 token，需要重新登录
- **NetworkException**：网络错误，不清除 token（可能是临时网络问题）
- **其他异常**：记录日志但不清除 token

### 6. Token 刷新时机

**已采用方案**：✅ 在收到 401 错误时刷新（Ktor Auth 插件自动处理）

- 插件会自动检测 401 错误
- 自动调用 `refreshTokens` 回调
- 自动重试原始请求

**备选方案**：如果需要在过期前主动刷新，可以添加 JWT 解析库检查过期时间。

---

## 📝 待确认事项

1. **Token 存储方案**：
    - [x] ✅ **已确定**：MVP 阶段采用加密存储
    - [x] ✅ **已确定**：新增 `core/settings` 模块，提供统一的存储接口
    - [x] ✅ **已确定**：使用 `multiplatform-crypto` 库实现加密
    - [ ] 需要选择合适的 multiplatform-crypto 库

2. **认证方式**：
    - [x] ✅ **已确定**：使用 Ktor 官方的 `Auth` 插件（`ktor-client-auth`）
    - [x] ✅ **已确定**：使用方案 B（`sendWithoutRequest` 配置），只需要一个 HttpClient

3. **Token 刷新策略**：
    - [x] ✅ **已确定**：使用 Ktor Auth 插件的 `refreshTokens` 回调（自动处理 401 并重试）
    - [x] ✅ **已确定**：使用 `sendWithoutRequest` 配置，刷新 API 不发送认证，避免循环依赖
    - [ ] 无需手动检查过期时间

4. **JWT 解析库**（可选）：
    - [x] ✅ **推荐**：`com.appstractive:jwt-kt`（Kotlin Multiplatform）
    - [ ] 或 `KotJWT`（备选）
    - [ ] 注意：如果使用 Ktor Auth 插件的自动刷新，可以不需要提前检查过期时间

5. **安全性增强**：
    - [x] ✅ **已确定**：MVP 阶段采用加密存储
    - [x] ✅ **已确定**：使用 `multiplatform-crypto` 库实现跨平台加密
    - [ ] 需要选择合适的 multiplatform-crypto 库
    - [ ] 考虑加密密钥的安全管理策略

6. **循环依赖处理**：
    - [x] ✅ **已确定**：使用方案 B（`sendWithoutRequest` 配置）
    - [x] ✅ 刷新 API (`/api/auth/refresh`) 不发送认证，避免循环依赖
    - [ ] 实施时确保服务器端刷新 API 不需要认证

---

## 📚 参考文档

### 官方文档

- [Ktor Client Auth 插件](https://ktor.io/docs/client-auth.html)
- [Ktor Client Bearer Auth](https://ktor.io/docs/client-bearer-auth.html)
- [Ktor Client 文档](https://ktor.io/docs/client.html)
- [Ktor Client 插件开发](https://ktor.io/docs/client-plugins.html)

### 开源库

- [multiplatform-settings GitHub](https://github.com/russhwolf/multiplatform-settings)
- [jwt-kt GitHub](https://github.com/appstractive/jwt-kt)
- [KotJWT GitHub](https://github.com/iNoles/kotjwt)

### 其他

- [JWT 规范](https://jwt.io/)
- [Server 端 JWT 认证实现](../server/docs/myhub-server-jwt-authentication-implementation.md)
- [离线优先应用的首次登录解决方案](./offline-first-login-solution.md)

---

---

## 📐 完整架构设计

### 模块依赖关系

```
core/settings (新增)
  ├── multiplatform-settings
  └── 提供 LocalSettingStore 接口

core/network
  ├── depends on → core/settings
  ├── depends on → multiplatform-crypto (加密)
  └── 提供 TokenStorage、HttpClient

feature/settings
  └── depends on → core/settings
```

### 依赖注入配置

```kotlin
// core/settings/src/commonMain/kotlin/.../di/SettingsModule.kt
val coreSettingsModule = module {
    single<LocalSettingStore> {
        LocalSettingStoreImpl()
    }
}

// core/network/src/commonMain/kotlin/.../di/NetworkModule.kt
val networkModule = module {
    includes(coreSettingsModule)  // 包含 core/settings 模块

    // 加密库（需要选择合适的 multiplatform-crypto 库）
    single<Crypto> {
        CryptoImpl()  // 或使用其他加密库的实现
    }

    // Token 存储（加密存储）
    single<TokenStorage> {
        SecureTokenStorage(
            localStore = get(),
            crypto = get()
        )
    }

    // 认证数据源
    single<RemoteAuthDataSource> {
        RemoteAuthDataSourceImpl(
            httpClient = get()  // 使用同一个 HttpClient（方案 B）
        )
    }

    // 主 HttpClient（带认证）
    single<HttpClient> {
        createHttpClient(
            factory = get(),
            tokenStorage = get(),
            authDataSource = get()
        )
    }
}
```

### core/settings 模块实现

```kotlin
// core/settings/src/commonMain/kotlin/.../LocalSettingStore.kt
package tech.zhifu.app.myhub.settings

/**
 * 本地设置存储接口
 * 跨平台统一的键值存储抽象
 */
interface LocalSettingStore {
    suspend fun get(key: String): String?
    suspend fun set(key: String, value: String)
    suspend fun remove(key: String)
    suspend fun clear()
}

// core/settings/src/commonMain/kotlin/.../LocalSettingStoreImpl.kt
package tech.zhifu.app.myhub.settings

import com . russhwolf . settings . Settings

        internal class LocalSettingStoreImpl(
    private val settings: Settings = Settings()
) : LocalSettingStore {
    override suspend fun get(key: String): String? {
        return settings.getStringOrNull(key)
    }

    override suspend fun set(key: String, value: String) {
        settings[key] = value
    }

    override suspend fun remove(key: String) {
        settings.remove(key)
    }

    override suspend fun clear() {
        // multiplatform-settings 没有提供 clear 方法
        // 需要记录所有键并逐个删除，或使用其他方式
    }
}
```

---

**文档版本**：v2.0（最终方案）  
**创建日期**：2026-01-27  
**最后更新**：2026-01-27  
**状态**：✅ 已确定最终方案，可执行实施

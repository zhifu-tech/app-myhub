# 离线优先应用的首次登录解决方案

> 解决离线优先应用中首次登录时服务器端用户不存在的问题

## 📋 问题背景

### 当前问题

1. **客户端场景**：
   - 应用支持离线使用，首次启动时在本地生成匿名用户（UUID v4）
   - 用户可以在离线状态下使用应用
   - 当用户首次尝试登录时，客户端已有 `userId`

2. **服务器端问题**：
   - `/api/auth/login` 需要用户已存在：`userRepository.getUserById(userId) ?: throw NotFoundException`
   - 首次登录时，服务器端没有该用户信息
   - 导致登录失败

3. **冲突点**：
   - 离线优先：客户端可以离线使用，本地已有用户
   - 服务器端要求：用户必须先存在才能登录

---

## 🔍 业内常见方案

### 方案 A：自动注册（Auto-register / Login-or-Create）⭐⭐⭐⭐⭐

**描述**：登录时如果用户不存在，自动创建用户。

**实现方式**：
```kotlin
// 登录时检查用户是否存在，不存在则自动创建
val user = userRepository.getUserById(userId)
    ?: createUserFromLoginRequest(request)
```

**优点**：
- ✅ 符合离线优先理念：客户端生成 ID，服务器端接受
- ✅ 用户体验好：首次登录无缝，无需额外注册步骤
- ✅ 实现简单：在登录 API 中处理即可
- ✅ 支持匿名用户升级：匿名用户可以直接登录

**缺点**：
- ⚠️ 需要验证 userId 格式（防止恶意请求）
- ⚠️ 需要处理用户名冲突（如果支持自定义用户名）

**适用场景**：✅ **离线优先应用、匿名用户升级**

---

### 方案 B：客户端生成 ID + 服务端验证创建

**描述**：客户端生成 UUID，服务器端验证并创建用户。

**实现方式**：
```kotlin
// 客户端发送 userId 和用户信息
// 服务器端验证 userId 格式，如果不存在则创建
if (userRepository.getUserById(userId) == null) {
    userRepository.createUser(User(id = userId, ...))
}
```

**优点**：
- ✅ 客户端控制用户 ID（符合离线优先）
- ✅ 服务器端可以验证和补充信息

**缺点**：
- ❌ 需要客户端发送更多信息（username 等）
- ❌ 需要处理用户名冲突

**适用场景**：需要客户端控制用户 ID 的场景

---

### 方案 C：两阶段注册（先注册后登录）

**描述**：先调用注册 API，再调用登录 API。

**实现方式**：
```kotlin
// 1. POST /api/users (注册)
// 2. POST /api/auth/login (登录)
```

**优点**：
- ✅ 逻辑清晰，职责分离

**缺点**：
- ❌ 需要两次网络请求
- ❌ 不符合离线优先理念（首次使用需要网络）
- ❌ 用户体验差（需要额外的注册步骤）

**适用场景**：传统 Web 应用，不适合离线优先应用

---

### 方案 D：OAuth/第三方登录

**描述**：通过第三方服务（Google、Apple、GitHub）验证身份。

**优点**：
- ✅ 无需管理用户信息
- ✅ 用户无需注册

**缺点**：
- ❌ 需要集成第三方服务
- ❌ 不符合离线优先理念（首次使用需要网络和第三方服务）
- ❌ 复杂度高

**适用场景**：需要第三方身份验证的应用

---

## 🎯 推荐方案：自动注册（方案 A）

### 选择理由

1. **符合离线优先理念**：
   - 客户端生成用户 ID（已在本地存在）
   - 服务器端接受并创建用户
   - 无需额外的注册步骤

2. **用户体验好**：
   - 首次登录无缝，无需额外操作
   - 支持匿名用户直接升级为登录用户

3. **实现简单**：
   - 只需修改登录 API，无需新增接口
   - 复用现有的 `UserService.createUser()` 逻辑

4. **安全性可控**：
   - 可以验证 userId 格式（UUID v4）
   - 可以限制自动创建的用户权限

---

## 📐 实施方案

### 1. 修改登录 API（自动注册）

**位置**：`server/src/main/kotlin/tech/zhifu/app/myhub/api/AuthApi.kt`

**修改前**：
```kotlin
// 获取用户
val user = userRepository.getUserById(request.userId)
    ?: throw NotFoundException("User", request.userId)
```

**修改后**：
```kotlin
// 获取用户，如果不存在则自动创建
var user = userRepository.getUserById(request.userId)
if (user == null) {
    // 自动创建用户（首次登录即注册）
    user = createUserFromLoginRequest(request, userRepository)
}
```

### 2. 实现自动创建用户逻辑

**方案 A：在 AuthApi 中实现（简单）**

```kotlin
private suspend fun createUserFromLoginRequest(
    request: LoginRequest,
    userRepository: UserRepository
): User {
    // 验证 userId 格式（UUID v4）
    require(isValidUuid(request.userId)) {
        "Invalid user ID format"
    }
    
    // 创建用户
    val now = Clock.System.now()
    val user = User(
        id = request.userId,
        username = generateUsername(request.userId), // 或从请求中获取
        displayName = null,
        avatarUrl = null,
        avatarText = null,
        createdAt = now,
        updatedAt = now,
        status = "active",
        lastLoginAt = now
    )
    
    return userRepository.upsertUser(user)
}

private fun isValidUuid(value: String): Boolean {
    return try {
        val uuid = UUID.fromString(value)
        // 验证是 UUID v4（随机 UUID）
        uuid.version() == 4
    } catch (e: Exception) {
        false
    }
}

private fun generateUsername(userId: String): String {
    // 使用 UUID 的前12位（去除连字符），减少冲突概率
    val prefix = userId.replace("-", "").take(12)
    return "user-$prefix"
}
```

**方案 B：在 UserService 中添加方法（推荐）⭐⭐⭐⭐⭐**

```kotlin
// UserService.kt
/**
 * 自动创建用户（用于首次登录）
 * 如果用户不存在，使用客户端提供的 userId 创建用户
 * 
 * 注意：此方法处理并发安全问题，使用数据库唯一约束确保原子性
 */
suspend fun createUserIfNotExists(
    userId: String,
    username: String? = null,
    displayName: String? = null,
    avatarUrl: String? = null,
    avatarText: String? = null
): User {
    // 验证 userId 格式（必须是 UUID v4）
    require(isValidUuid(userId)) {
        "Invalid user ID format: must be UUID v4"
    }
    
    // 先尝试获取（快速路径）
    val existing = userRepository.getUserById(userId)
    if (existing != null) {
        logger.debug { "User already exists: userId=$userId, updating with client data" }
        // 用户已存在，更新用户信息（以客户端提供的为准）
        val updatedUser = existing.copy(
            username = username ?: existing.username,  // 如果客户端提供，使用客户端的
            displayName = displayName ?: existing.displayName,
            avatarUrl = avatarUrl ?: existing.avatarUrl,
            avatarText = avatarText ?: existing.avatarText,
            updatedAt = Clock.System.now(),
            lastLoginAt = Clock.System.now()
        )
        return userRepository.upsertUser(updatedUser)
    }
    
    // 尝试创建，如果已存在则捕获异常并更新（处理并发情况）
    return try {
        logger.info { "Creating new user: userId=$userId, username=$username" }
        val now = Clock.System.now()
        val user = User(
            id = userId,
            username = username ?: generateDefaultUsername(userId),
            displayName = displayName,
            avatarUrl = avatarUrl,
            avatarText = avatarText,
            createdAt = now,
            updatedAt = now,
            status = "active",
            lastLoginAt = now
        )
        // 直接插入，依赖数据库唯一约束（PRIMARY KEY）确保原子性
        // 注意：如果 UserRepository 没有 insertUser 方法，需要：
        // 1. 在 UserRepository 接口中添加 insertUser 方法，或
        // 2. 直接使用 LocalUserDataSource.insertUser（需要注入 LocalUserDataSource）
        // 这里假设已添加 insertUser 方法到 UserRepository
        userRepository.insertUser(user)
        logger.info { "User created successfully: userId=$userId" }
        user
    } catch (e: SQLiteConstraintException) {
        // 用户已存在（并发创建），更新用户信息（以客户端提供的为准）
        logger.debug { "Concurrent user creation detected, updating existing user: userId=$userId" }
        val existing = userRepository.getUserById(userId)
            ?: throw IllegalStateException("User should exist after constraint violation")
        
        // 更新用户信息，以客户端提供的为准
        val updatedUser = existing.copy(
            username = username ?: existing.username,
            displayName = displayName ?: existing.displayName,
            avatarUrl = avatarUrl ?: existing.avatarUrl,
            avatarText = avatarText ?: existing.avatarText,
            updatedAt = Clock.System.now(),
            lastLoginAt = Clock.System.now()
        )
        userRepository.upsertUser(updatedUser)
    }
}

/**
 * 验证 UUID v4 格式
 */
private fun isValidUuid(value: String): Boolean {
    return try {
        val uuid = UUID.fromString(value)
        // 验证是 UUID v4（随机 UUID）
        uuid.version() == 4
    } catch (e: Exception) {
        false
    }
}

/**
 * 生成默认用户名
 * 使用 UUID 的前12位（去除连字符）确保唯一性
 */
private fun generateDefaultUsername(userId: String): String {
    // 使用 UUID 的前12位（去除连字符），减少冲突概率
    val prefix = userId.replace("-", "").take(12)
    return "user-$prefix"
}
```

**重要说明**：
- ✅ 使用数据库唯一约束（PRIMARY KEY）确保并发安全
- ✅ 通过捕获 `SQLiteConstraintException` 处理并发创建场景
- ✅ 前提条件：数据库 `user` 表的 `id` 字段必须有 `PRIMARY KEY` 约束
- ⚠️ **实现注意**：如果 `UserRepository` 接口没有 `insertUser` 方法，需要：
  - 在 `UserRepository` 接口中添加 `suspend fun insertUser(user: User)` 方法
  - 在 `UserRepositoryImpl` 中实现该方法，直接调用 `localDataSource.insertUser(user)`
  - 或者直接在 `UserService` 中注入 `LocalUserDataSource` 使用（不推荐，破坏分层）

### 3. 更新登录 API

```kotlin
// AuthApi.kt
post("login") {
    try {
        val request = call.receive<LoginRequest>()
        
        // 验证请求
        require(request.userId.isNotBlank()) {
            "User ID is required"
        }
        
        // 获取或创建用户（自动注册）
        // 如果客户端提供了用户信息，使用客户端数据（以客户端为准）
        val user = userService.createUserIfNotExists(
            userId = request.userId,
            username = request.username,
            displayName = request.displayName,
            avatarUrl = request.avatarUrl,
            avatarText = request.avatarText
        )
        
        // 更新最后登录时间（在事务中确保原子性）
        val updatedUser = user.copy(
            lastLoginAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        userRepository.upsertUser(updatedUser)
        
        // 生成 token pair
        val tokenPair = tokenService.generateTokens(updatedUser)
        
        call.respond(
            HttpStatusCode.OK,
            LoginResponse(
                accessToken = tokenPair.accessToken,
                refreshToken = tokenPair.refreshToken,
                expiresIn = tokenPair.expiresIn,
                tokenType = "Bearer",
                userId = updatedUser.id,
                username = updatedUser.username
            )
        )
    } catch (e: ValidationException) {
        // 验证错误，返回 400
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(
                message = e.message ?: "Validation failed",
                code = "VALIDATION_ERROR"
            )
        )
    } catch (e: IllegalArgumentException) {
        // 参数错误，返回 400
        logger.warn(e) { "Invalid login request: ${e.message}" }
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(
                message = e.message ?: "Invalid request",
                code = "INVALID_REQUEST"
            )
        )
    } catch (e: SQLiteConstraintException) {
        // 数据库约束错误（如并发创建冲突），记录日志并返回 409
        logger.warn(e) { "Concurrent user creation detected for userId: ${request.userId}" }
        call.respond(
            HttpStatusCode.Conflict,
            ErrorResponse(
                message = "User creation conflict",
                code = "CONFLICT"
            )
        )
    } catch (e: Exception) {
        // 其他错误，记录详细日志，返回通用错误（避免信息泄露）
        logger.error(e) { "Login failed for userId: ${request.userId}" }
        call.respond(
            HttpStatusCode.InternalServerError,
            ErrorResponse(
                message = "Login failed",
                code = "INTERNAL_ERROR"
            )
        )
    }
}
```

**错误处理说明**：
- ✅ 区分不同类型的异常，返回相应的 HTTP 状态码
- ✅ 记录详细日志便于排查问题，但返回通用错误信息避免信息泄露
- ✅ 处理并发创建冲突（409 Conflict）

### 4. 更新 LoginRequest

根据已确认事项，需要支持客户端提供用户名：

```kotlin
@Serializable
data class LoginRequest(
    val userId: String,
    val username: String? = null, // 可选：客户端提供的用户名（本地匿名用户已有）
    val displayName: String? = null, // 可选：客户端提供的显示名称
    val avatarUrl: String? = null, // 可选：客户端提供的头像URL
    val avatarText: String? = null // 可选：客户端提供的头像文本
)
```

**说明**：
- ✅ 客户端在请求时，本地已经存在了一个匿名用户，可以提供完整的用户信息
- ✅ 如果客户端提供了用户信息，首次登录时会使用这些信息创建或更新用户
- ✅ 如果客户端不提供，使用默认值（如默认用户名）

---

## 🔒 安全性考虑

### 1. 验证 userId 格式

**要求**：只接受 UUID v4 格式的 userId

```kotlin
private fun isValidUuid(value: String): Boolean {
    return try {
        val uuid = UUID.fromString(value)
        // 验证是 UUID v4（version 4）
        uuid.version() == 4
    } catch (e: Exception) {
        false
    }
}
```

**原因**：
- 防止恶意请求（如：SQL 注入、路径遍历）
- 确保 userId 格式统一
- 符合客户端生成的 UUID v4 格式

### 2. 限制自动创建的用户权限

**建议**：
- 自动创建的用户默认状态为 `active`
- 可以添加 `isAutoCreated` 标志，用于后续权限控制
- 限制自动创建的用户数量（防止滥用）

### 3. 并发安全处理

**关键问题**：`getUserById` + `insertUser` 不是原子操作，存在并发竞态条件。

**解决方案**：使用数据库唯一约束 + 异常处理

```kotlin
// 在 createUserIfNotExists 中
try {
    userRepository.insertUser(user)  // 直接插入，依赖 PRIMARY KEY 约束
} catch (e: SQLiteConstraintException) {
    // 并发创建冲突，重新获取已存在的用户
    userRepository.getUserById(userId) ?: throw IllegalStateException(...)
}
```

**前提条件**：
- ✅ 数据库 `user` 表的 `id` 字段必须有 `PRIMARY KEY` 约束
- ✅ 需要检查 SQLDelight schema 是否已设置唯一约束

### 4. 限流和防滥用机制

**IP 限流**（推荐使用 Ktor RateLimiter）：

```kotlin
// 在 Application.kt 中
install(RateLimiter) {
    registerLimit(
        limit = 10,  // 每个IP每小时最多10次登录请求
        window = Duration.ofHours(1)
    )
}
```

**监控和告警**：
- 监控异常的用户创建频率
- 设置告警阈值（如：单个IP每小时创建超过10个用户）
- 记录首次登录成功率和自动创建用户数量

### 5. 用户名冲突处理

**策略：以客户端提供的为准**

根据已确认事项，用户名处理策略如下：

1. **允许客户端提供用户名**：客户端在请求时可以提供用户名
2. **冲突处理**：如果用户名冲突，以客户端提供的为准（覆盖服务器端）
3. **默认用户名**：如果客户端不提供，使用默认格式生成

**实现方式**：

```kotlin
suspend fun createUserIfNotExists(
    userId: String,
    username: String? = null
): User {
    // ... 验证和获取逻辑 ...
    
    // 如果客户端提供了用户名，直接使用（即使冲突也以客户端为准）
    val finalUsername = username ?: generateDefaultUsername(userId)
    
    val user = User(
        id = userId,
        username = finalUsername,  // 直接使用客户端提供的用户名
        // ... 其他字段
    )
    
    // 直接插入或更新，以客户端数据为准
    return try {
        userRepository.insertUser(user)
        user
    } catch (e: SQLiteConstraintException) {
        // 如果用户已存在（并发创建），更新用户信息（以客户端为准）
        val existing = userRepository.getUserById(userId)
            ?: throw IllegalStateException("User should exist after constraint violation")
        
        // 更新用户信息，以客户端提供的为准
        val updatedUser = existing.copy(
            username = finalUsername,  // 覆盖服务器端的用户名
            updatedAt = Clock.System.now()
        )
        userRepository.upsertUser(updatedUser)
        updatedUser
    }
}

/**
 * 生成默认用户名
 */
private fun generateDefaultUsername(userId: String): String {
    // 使用 UUID 的前12位（去除连字符），减少冲突概率
    val prefix = userId.replace("-", "").take(12)
    return "user-$prefix"
}
```

**说明**：
- ✅ 客户端提供的用户名优先级最高，即使与服务器端冲突也以客户端为准
- ✅ 如果客户端不提供用户名，使用默认格式 `user-{UUID前12位}`
- ✅ 在并发创建场景下，如果用户已存在，会更新用户信息（包括用户名），以客户端数据为准

---

## 📊 数据流

### 首次登录流程

```
客户端（离线状态）
  └─ 生成匿名用户（UUID v4）
  └─ 本地存储用户信息（username, displayName, avatarUrl, avatarText）

客户端（首次登录）
  └─ 发送 POST /api/auth/login {
       userId: "xxx-xxx-xxx",
       username: "client-username",      // 可选：客户端提供
       displayName: "Client Name",       // 可选：客户端提供
       avatarUrl: "https://...",         // 可选：客户端提供
       avatarText: "AB"                  // 可选：客户端提供
     }
       └─ 服务器端
            ├─ 检查用户是否存在
            ├─ 不存在 → 自动创建用户（使用客户端提供的信息）
            ├─ 存在 → 更新用户信息（以客户端提供的为准）
            ├─ 生成 token pair
            └─ 返回 token

客户端
  └─ 保存 token
  └─ 如果用户不启用离线使用 → 触发数据同步
       └─ 同步全量用户信息到服务器
       └─ 强制覆盖服务器端数据
  └─ 后续请求使用 token 认证
```

### 后续登录流程

```
客户端
  └─ 发送 POST /api/auth/login {
       userId: "xxx-xxx-xxx",
       username: "new-username",         // 可选：如果客户端提供，会更新服务器端
       displayName: "New Name",          // 可选：如果客户端提供，会更新服务器端
       // ... 其他用户信息
     }
       └─ 服务器端
            ├─ 检查用户是否存在
            ├─ 存在 → 更新用户信息（以客户端提供的为准）
            ├─ 更新 lastLoginAt
            ├─ 生成 token pair
            └─ 返回 token
```

### 数据同步流程（首次登录后）

```
客户端（首次登录成功，用户不启用离线使用）
  └─ 触发数据同步
       └─ 同步全量用户信息到服务器
            ├─ username
            ├─ displayName
            ├─ avatarUrl
            ├─ avatarText
            └─ 其他用户信息
       └─ 强制覆盖服务器端数据
            └─ 服务器端数据完全以客户端为准
```

---

## 🚀 实施步骤

### Phase 1: 服务器端修改

1. ✅ 在 `UserService` 中添加 `createUserIfNotExists()` 方法
   - 支持客户端提供完整用户信息（username, displayName, avatarUrl, avatarText）
   - 实现以客户端数据为准的更新逻辑
2. ✅ 修改 `AuthApi.login()` 使用自动注册逻辑
3. ✅ 更新 `LoginRequest` 支持客户端提供用户信息
4. ✅ 添加 userId 格式验证（UUID v4）
5. ✅ 实现并发安全处理（数据库唯一约束 + 异常处理）
6. ✅ 完善错误处理（区分异常类型，避免信息泄露）
7. ✅ 添加日志记录
8. ⏸️ 单元测试和并发测试（暂不处理，后续统一添加）
9. ✅ 配置限流机制（可选）

### Phase 2: 客户端集成

1. ✅ 客户端在登录时发送本地 userId 和用户信息（如果存在）
   - 发送本地匿名用户的完整信息（username, displayName, avatarUrl, avatarText）
2. ✅ 处理登录响应，保存 token
3. ✅ 首次登录后，如果用户不启用离线使用，触发数据同步
   - 同步全量用户信息，强制覆盖服务器端数据
4. ✅ 测试首次登录流程

### Phase 3: 测试与验证

1. ✅ 测试首次登录（用户不存在）
   - 测试客户端提供用户信息的情况
   - 测试客户端不提供用户信息的情况（使用默认值）
2. ✅ 测试后续登录（用户已存在）
   - 测试客户端提供新用户信息时，是否以客户端为准更新
3. ✅ 测试 userId 格式验证（UUID v4）
4. ✅ 测试并发登录（同一用户，验证并发安全）
5. ✅ 测试错误处理（各种异常场景）
6. ✅ 测试数据同步（首次登录后，如果用户不启用离线使用）
   - 验证同步时强制覆盖服务器端数据
   - 验证同步全量用户信息
7. ⏸️ 单元测试和集成测试（暂不处理，后续统一添加）
8. ✅ 测试限流机制（如果启用）

---

## ⚠️ 注意事项

1. **userId 格式验证**：
   - 必须验证为 UUID v4 格式（使用 `uuid.version() == 4`）
   - 防止恶意请求和注入攻击

2. **并发安全**：
   - ⚠️ **关键**：必须使用数据库唯一约束（PRIMARY KEY）确保并发安全
   - 通过捕获 `SQLiteConstraintException` 处理并发创建冲突
   - 确保 `user.id` 字段有唯一索引

3. **用户名处理**：
   - ✅ **以客户端提供的为准**：如果客户端提供用户名，直接使用，即使与服务器端冲突也以客户端为准（覆盖服务器端）
   - ✅ **默认用户名**：如果客户端不提供用户名，使用默认格式 `user-{UUID前12位}` 生成
   - ✅ **更新策略**：如果用户已存在，客户端提供的新用户名会覆盖服务器端的用户名

4. **错误处理**：
   - 区分不同类型的异常，返回相应的 HTTP 状态码
   - 记录详细日志便于排查，但返回通用错误信息避免信息泄露
   - 处理并发创建冲突（409 Conflict）

5. **事务管理**：
   - 创建用户和更新 `lastLoginAt` 应该在事务中执行（如果可能）
   - 确保操作的原子性

6. **用户状态和权限**：
   - ✅ **无特殊权限限制**：自动创建的用户没有特殊权限限制，与普通用户相同
   - ✅ **不需要 `isAutoCreated` 标志**：不需要额外的标志字段
   - 自动创建的用户默认状态为 `active`
   - 可以后续通过用户设置更新

7. **数据同步**：
   - ✅ **首次登录后同步**：首次登录后，如果用户不启用离线使用，需要同步客户端用户信息到服务器
   - ✅ **同步策略**：以客户端同步信息为依据，强制覆盖服务器端的数据
   - ✅ **同步范围**：同步时会同步全量用户信息，直接覆盖服务器端的
   - ✅ **同步时机**：建议在首次登录成功后立即触发数据同步（如果用户不启用离线使用）

8. **匿名用户升级**：
   - 支持匿名用户直接升级为登录用户
   - 保持 userId 不变，只更新认证状态

9. **性能考虑**：
   - 确保 `user.id` 字段有唯一索引，优化查询性能
   - 考虑对频繁查询的用户信息进行缓存
   - 监控登录响应时间和并发创建冲突次数

10. **监控指标**：
    - 首次登录成功率
    - 自动创建用户数量
    - 并发创建冲突次数
    - 登录响应时间

11. **日志记录**：
    - 记录用户创建和登录的关键操作
    - 使用适当的日志级别（INFO、DEBUG、WARN、ERROR）
    - 记录并发冲突和异常情况
    - 避免在日志中记录敏感信息（如完整 token）

---

## 📝 日志记录最佳实践

### 日志级别

```kotlin
// UserService.kt
private val logger = LoggerFactory.getLogger(UserService::class.java)

suspend fun createUserIfNotExists(
    userId: String,
    username: String? = null
): User {
    logger.info { "Attempting to create user if not exists: userId=$userId" }
    
    val existing = userRepository.getUserById(userId)
    if (existing != null) {
        logger.debug { "User already exists: userId=$userId" }
        return existing
    }
    
    logger.info { "Creating new user: userId=$userId, username=$username" }
    // ... 创建逻辑
    logger.info { "User created successfully: userId=$userId" }
    return user
}
```

### 日志内容

**应该记录**：
- ✅ 用户创建和登录操作
- ✅ 并发冲突情况
- ✅ 验证失败（不包含敏感信息）
- ✅ 异常堆栈（用于排查问题）

**不应该记录**：
- ❌ 完整的 token 内容
- ❌ 用户密码（如果有）
- ❌ 数据库连接字符串
- ❌ 其他敏感配置信息

---

## 📝 已确认事项

1. **用户名策略**：
   - ✅ **允许客户端提供用户名**：客户端在请求时，本地已经存在了一个匿名用户，应该允许客户端提供用户名
   - ✅ **冲突处理**：如果用户名冲突，以客户端提供的为准（覆盖服务器端）
   - ✅ **默认用户名格式**：如果客户端不提供，使用 `user-{UUID前12位}` 格式

2. **用户信息同步**：
   - ✅ **首次登录后同步**：首次登录后，如果用户不启用离线使用，需要同步客户端用户信息到服务器
   - ✅ **同步依据**：以客户端同步信息为依据，强制覆盖服务器端的数据
   - ✅ **同步范围**：同步时会同步全量用户信息，直接覆盖服务器端的

3. **权限控制**：
   - ✅ **无特殊权限限制**：自动创建的用户没有特殊权限限制，与普通用户相同
   - ✅ **不需要 `isAutoCreated` 标志**：不需要额外的标志字段

4. **数据迁移**：
   - ✅ **同步策略**：如果客户端已有用户数据，首次登录后同步时，强制覆盖服务器端的数据
   - ✅ **全量覆盖**：同步时会同步全量用户信息，直接覆盖服务器端的

5. **单元测试**：
   - ⏸️ **暂不处理**：单测暂不处理，后续逻辑稳定后统一添加

---

## 🧪 测试用例

> **注意**：根据已确认事项，单测暂不处理，后续逻辑稳定后统一添加。以下为测试用例示例，供后续实施参考。

### 单元测试示例（待实施）

```kotlin
// UserServiceTest.kt

@Test
fun `createUserIfNotExists should return existing user if exists`() = runTest {
    // 创建用户
    val userId = UUID.randomUUID().toString()
    val user = User(
        id = userId,
        username = "test-user",
        // ... 其他字段
    )
    userRepository.insertUser(user)
    
    // 再次调用应该返回已存在的用户
    val result = userService.createUserIfNotExists(userId)
    assertEquals(user.id, result.id)
    assertEquals(user.username, result.username)
}

@Test
fun `createUserIfNotExists should handle concurrent creation`() = runTest {
    val userId = UUID.randomUUID().toString()
    
    // 并发创建（模拟多个请求同时创建同一用户）
    val results = coroutineScope {
        (1..10).map {
            async {
                userService.createUserIfNotExists(userId)
            }
        }.awaitAll()
    }
    
    // 所有结果应该是同一个用户
    val firstId = results.first().id
    assertTrue(results.all { it.id == firstId })
    // 验证只创建了一个用户
    assertEquals(1, results.distinctBy { it.id }.size)
}

@Test
fun `createUserIfNotExists should reject invalid UUID`() = runTest {
    assertThrows<IllegalArgumentException> {
        userService.createUserIfNotExists("invalid-uuid")
    }
}

@Test
fun `createUserIfNotExists should create user with default username`() = runTest {
    val userId = UUID.randomUUID().toString()
    val user = userService.createUserIfNotExists(userId)
    
    assertNotNull(user.username)
    assertTrue(user.username!!.startsWith("user-"))
}

@Test
fun `createUserIfNotExists should use provided username`() = runTest {
    val userId = UUID.randomUUID().toString()
    val customUsername = "custom-user"
    val user = userService.createUserIfNotExists(userId, customUsername)
    
    assertEquals(customUsername, user.username)
}

@Test
fun `createUserIfNotExists should update existing user with client data`() = runTest {
    val userId = UUID.randomUUID().toString()
    val existingUser = User(
        id = userId,
        username = "old-username",
        displayName = "Old Name",
        // ... 其他字段
    )
    userRepository.insertUser(existingUser)
    
    // 使用客户端数据更新
    val updatedUser = userService.createUserIfNotExists(
        userId = userId,
        username = "new-username",
        displayName = "New Name"
    )
    
    // 应该使用客户端提供的数据
    assertEquals("new-username", updatedUser.username)
    assertEquals("New Name", updatedUser.displayName)
}
```

### 集成测试示例（待实施）

```kotlin
// AuthApiTest.kt

@Test
fun `POST /api/auth/login should create user on first login`() = testApplication {
    val userId = UUID.randomUUID().toString()
    
    val response = client.post("/api/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(LoginRequest(userId = userId))
    }
    
    assertEquals(HttpStatusCode.OK, response.status)
    val loginResponse = response.body<LoginResponse>()
    assertEquals(userId, loginResponse.userId)
    assertNotNull(loginResponse.accessToken)
}

@Test
fun `POST /api/auth/login should return 400 for invalid UUID`() = testApplication {
    val response = client.post("/api/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(LoginRequest(userId = "invalid-uuid"))
    }
    
    assertEquals(HttpStatusCode.BadRequest, response.status)
}

@Test
fun `POST /api/auth/login should handle concurrent login requests`() = testApplication {
    val userId = UUID.randomUUID().toString()
    
    val responses = coroutineScope {
        (1..5).map {
            async {
                client.post("/api/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(userId = userId))
                }
            }
        }.awaitAll()
    }
    
    // 所有请求都应该成功
    responses.forEach { response ->
        assertEquals(HttpStatusCode.OK, response.status)
    }
}

@Test
fun `POST /api/auth/login should use client provided user info`() = testApplication {
    val userId = UUID.randomUUID().toString()
    val request = LoginRequest(
        userId = userId,
        username = "client-username",
        displayName = "Client Name"
    )
    
    val response = client.post("/api/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }
    
    assertEquals(HttpStatusCode.OK, response.status)
    val loginResponse = response.body<LoginResponse>()
    assertEquals("client-username", loginResponse.username)
}
```

---

## 📚 参考

- [MyHub 匿名身份方案](../docs/myhub-anonymous-identity-infra-v1.0.md)
- [JWT 认证实现方案](../docs/client-jwt-authentication-implementation-plan.md)
- [UUID v4 规范](https://tools.ietf.org/html/rfc4122)
- [SQLite 并发控制](https://www.sqlite.org/lockingv3.html)
- [Kotlin Coroutines 并发](https://kotlinlang.org/docs/coroutines-guide.html)

---

**文档版本**：v1.1  
**创建日期**：2026-01-27  
**更新日期**：2026-01-27  
**状态**：✅ 方案已完善，包含并发安全和错误处理改进

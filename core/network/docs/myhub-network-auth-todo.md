# Network Auth Module

JWT Token 认证模块，提供 Token 存储、加密和自动刷新功能。

## 核心组件

### TokenStorage

Token 存储接口，提供加密存储 Access Token 和 Refresh Token。

### SecureTokenStorage

基于 `LocalSettingStore` 和 `Crypto` 的加密 Token 存储实现。

### Crypto

加密接口，用于加密和解密敏感数据。

### CryptoImpl

简单的 XOR 加密实现（MVP 阶段）。生产环境应使用 AES 等更安全的算法。

## 使用示例

### 登录并保存 Token

```kotlin
import tech.zhifu.app.myhub.datastore.repository.auth.AuthService

class LoginViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository
) {
    suspend fun login() {
        val user = userRepository.getUser()
        val response = authService.login(
            userId = user.id,
            username = user.username,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            avatarText = user.avatarText
        )
        // Token 已自动保存到 TokenStorage
    }
}
```

### 登出

```kotlin
suspend fun logout() {
    authService.logout() // 清除所有 token
}
```

### 检查登录状态

```kotlin
suspend fun isLoggedIn(): Boolean {
    return authService.isLoggedIn()
}
```

## 自动认证

配置了 Ktor Auth 插件后，所有 API 请求会自动添加 `Authorization: Bearer {accessToken}` header。

## 自动刷新

当 Access Token 过期（返回 401）时，Ktor Auth 插件会自动：

1. 使用 Refresh Token 调用 `/api/auth/refresh`
2. 获取新的 Access Token
3. 保存新的 Token
4. 自动重试原始请求

## 注意事项

1. **加密密钥**：加密密钥存储在本地，首次使用时自动生成
2. **Token 清除**：登出或刷新失败时会清除 token
3. **循环依赖**：刷新 API 不需要认证，通过 `sendWithoutRequest` 配置避免循环依赖

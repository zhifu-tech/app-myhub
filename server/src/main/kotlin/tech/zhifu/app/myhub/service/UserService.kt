package tech.zhifu.app.myhub.service

import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.model.dto.CreateUserRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateUserRequest
import tech.zhifu.app.myhub.datastore.model.dto.UserResponse
import tech.zhifu.app.myhub.datastore.model.dto.toResponse
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.exception.NotFoundException
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import java.util.UUID
import kotlin.time.Clock

/**
 * 用户服务
 * 处理用户相关的业务逻辑
 */
class UserService(
    private val userRepository: UserRepository
) {
    /**
     * 获取指定用户
     */
    suspend fun getUser(userId: String): UserResponse {
        val user = userRepository.getUserById(userId)
            ?: throw NotFoundException("User", userId)

        return user.toResponse()
    }

    /**
     * 创建用户
     */
    suspend fun createUser(request: CreateUserRequest): UserResponse {
        // 验证请求
        request.validate()

        // 创建用户
        val now = Clock.System.now()
        val user = User(
            id = generateUserId(),
            username = request.username.trim(),
            displayName = request.displayName.trim(),
            avatarUrl = request.avatarUrl,
            avatarText = request.avatarText,
            createdAt = now,
            updatedAt = now,
            status = request.status,
            lastLoginAt = now
        )

        val created = userRepository.upsertUser(user)
        return created.toResponse()
    }

    /**
     * 更新用户
     */
    suspend fun updateUser(
        id: String,
        request: UpdateUserRequest
    ): UserResponse {
        // 验证请求
        request.validate()

        // 检查用户是否存在
        val existing = userRepository.getUserById(id)
            ?: throw NotFoundException("User", id)

        // 更新用户
        val updated = existing.copy(
            username = request.username.trim(),
            displayName = request.displayName.trim(),
            avatarUrl = request.avatarUrl,
            avatarText = request.avatarText,
            status = request.status,
            updatedAt = Clock.System.now()
        )

        val saved = userRepository.upsertUser(updated)
        return saved.toResponse()
    }

    /**
     * 删除用户
     */
    suspend fun deleteUser(id: String) {
        // 检查用户是否存在
        val existing = userRepository.getUserById(id)
            ?: throw NotFoundException("User", id)

        userRepository.deleteUser(id)
    }

    /**
     * 获取用户偏好设置
     */
    suspend fun getUserPreferences(userId: String): UserPreferences {
        return userRepository.getUserPreferences(userId)
            ?: throw NotFoundException("UserPreferences", userId)
    }

    /**
     * 创建或更新用户偏好设置
     */
    suspend fun upsertUserPreferences(
        userId: String,
        preferences: UserPreferences
    ): UserPreferences {
        // 验证用户是否存在
        val user = userRepository.getUserById(userId)
            ?: throw NotFoundException("User", userId)

        // 确保 userId 匹配
        val preferencesWithUserId = preferences.copy(userId = userId)

        return userRepository.upsertUserPreferences(preferencesWithUserId)
    }

    /**
     * 自动创建用户（用于首次登录）
     * 如果用户不存在，使用客户端提供的 userId 创建用户
     *
     * 注意：此方法处理并发安全问题，使用数据库唯一约束确保原子性
     *
     * @param userId 客户端提供的用户 ID（必须是 UUID v4）
     * @param username 客户端提供的用户名（可选）
     * @param displayName 客户端提供的显示名称（可选）
     * @param avatarUrl 客户端提供的头像URL（可选）
     * @param avatarText 客户端提供的头像文本（可选）
     * @return 用户对象（已存在或新创建的）
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
                username = username ?: existing.username,
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
                displayName = displayName.orEmpty(),
                avatarUrl = avatarUrl.orEmpty(),
                avatarText = avatarText.orEmpty(),
                createdAt = now,
                updatedAt = now,
                status = "active",
                lastLoginAt = now
            )
            // 直接插入，依赖数据库唯一约束（PRIMARY KEY）确保原子性
            userRepository.insertUser(user)
            logger.info { "User created successfully: userId=$userId" }
            user
        } catch (e: Exception) {
            // 检查是否是唯一约束冲突（并发创建）
            val isConstraintViolation = e.message?.contains("UNIQUE constraint", ignoreCase = true) == true
                || e.message?.contains("PRIMARY KEY", ignoreCase = true) == true
                || e.cause?.message?.contains("UNIQUE constraint", ignoreCase = true) == true
                || e.cause?.message?.contains("PRIMARY KEY", ignoreCase = true) == true

            if (isConstraintViolation) {
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
            } else {
                // 其他异常，重新抛出
                throw e
            }
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
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 生成默认用户名
     * 使用 UUID 的前12位（去除连字符），减少冲突概率
     */
    private fun generateDefaultUsername(userId: String): String {
        val prefix = userId.replace("-", "").take(12)
        return "user-$prefix"
    }

    /**
     * 生成用户 ID
     */
    private fun generateUserId(): String {
        return "user-${System.currentTimeMillis()}-${(0..9999).random()}"
    }
}

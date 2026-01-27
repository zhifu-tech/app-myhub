package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

/**
 * 用户仓库接口（服务端）
 */
interface UserRepository {
    /**
     * 根据 ID 获取用户
     */
    suspend fun getUserById(userId: String): User?

    /**
     * 创建或更新用户
     */
    suspend fun upsertUser(user: User): User

    /**
     * 插入用户（仅创建，如果已存在会抛出异常）
     * 用于并发安全的用户创建
     */
    suspend fun insertUser(user: User)

    /**
     * 删除用户
     */
    suspend fun deleteUser(userId: String)

    /**
     * 获取用户偏好设置
     */
    suspend fun getUserPreferences(userId: String): UserPreferences?

    /**
     * 创建或更新用户偏好设置
     */
    suspend fun upsertUserPreferences(preferences: UserPreferences): UserPreferences
}

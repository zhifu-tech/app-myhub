package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.UserRepository

/**
 * 用户仓库实现（服务端）
 * 使用 LocalUserDataSource 实现，避免代码重复
 */
class UserRepositoryImpl(
    private val localDataSource: LocalUserDataSource
) : UserRepository {

    override suspend fun getUserById(userId: String): User? {
        return localDataSource.getUser(userId)
    }

    override suspend fun upsertUser(user: User): User {
        // 检查用户是否存在
        val existing = localDataSource.getUser(user.id)
        if (existing != null) {
            // 如果存在，更新
            try {
                localDataSource.updateUser(user)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to update user: ${e.message}", e)
            }
        } else {
            // 如果不存在，创建
            try {
                localDataSource.insertUser(user)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to create user: ${e.message}", e)
            }
        }
        return user
    }

    override suspend fun insertUser(user: User) {
        // 直接插入，依赖数据库唯一约束（PRIMARY KEY）确保并发安全
        // 如果用户已存在，会抛出异常（由调用方处理）
        localDataSource.insertUser(user)
    }

    override suspend fun deleteUser(userId: String) {
        localDataSource.deleteUser(userId)
    }

    override suspend fun getUserPreferences(userId: String): UserPreferences? {
        return localDataSource.getUserPreferences(userId)
    }

    override suspend fun upsertUserPreferences(preferences: UserPreferences): UserPreferences {
        // 检查偏好设置是否存在
        val existing = localDataSource.getUserPreferences(preferences.userId)
        if (existing != null) {
            // 如果存在，更新
            try {
                localDataSource.updateUserPreferences(preferences)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to update user preferences: ${e.message}", e)
            }
        } else {
            // 如果不存在，创建
            try {
                localDataSource.insertUserPreferences(preferences)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to create user preferences: ${e.message}", e)
            }
        }
        return preferences
    }
}

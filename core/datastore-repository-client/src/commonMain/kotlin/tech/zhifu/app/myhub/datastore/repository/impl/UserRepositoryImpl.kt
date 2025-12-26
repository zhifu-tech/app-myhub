package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.model.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.ReactiveUserRepository

/**
 * 用户仓库实现（客户端）
 * 实现本地和远程数据源的协调，支持响应式接口
 */
class UserRepositoryImpl(
    private val localDataSource: LocalUserDataSource,
    private val remoteDataSource: RemoteUserDataSource
) : ReactiveUserRepository {

    override suspend fun getCurrentUser(): User? {
        // 先从本地获取
        val localUser = localDataSource.getCurrentUser()
        if (localUser != null) {
            return localUser
        }

        // 如果本地没有，从远程获取
        return try {
            val remoteUser = remoteDataSource.getCurrentUser()
            localDataSource.saveUser(remoteUser)
            remoteUser
        } catch (_: Exception) {
            null
        }
    }

    override fun observeCurrentUser(): Flow<User?> {
        return localDataSource.observeUser()
    }

    override suspend fun updateUser(user: User): User {
        // 先更新本地
        localDataSource.saveUser(user)

        // 然后同步到远程
        return try {
            val remoteUser = remoteDataSource.updateUser(user)
            localDataSource.saveUser(remoteUser)
            remoteUser
        } catch (_: Exception) {
            // 如果远程同步失败，返回本地用户
            user
        }
    }

    /**
     * 更新用户偏好设置（客户端特有方法）
     */
    suspend fun updatePreferences(preferences: UserPreferences): UserPreferences {
        val currentUser = getCurrentUser()
            ?: throw IllegalStateException("No user found")

        val updatedUser = currentUser.copy(preferences = preferences)
        updateUser(updatedUser)
        return preferences
    }

    /**
     * 获取用户偏好设置（客户端特有方法）
     */
    suspend fun getPreferences(): UserPreferences? {
        return getCurrentUser()?.preferences
    }

    /**
     * 登出（客户端特有方法）
     */
    suspend fun logout() {
        localDataSource.clearUser()
    }
}


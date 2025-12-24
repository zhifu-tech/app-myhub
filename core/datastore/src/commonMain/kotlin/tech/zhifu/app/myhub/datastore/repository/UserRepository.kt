package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.model.UserPreferences

/**
 * 用户仓库接口
 */
interface UserRepository {
    /**
     * 获取当前用户
     */
    fun getCurrentUser(): Flow<User?>

    /**
     * 更新用户信息
     */
    suspend fun updateUser(user: User): Result<User>

    /**
     * 更新用户偏好设置
     */
    suspend fun updatePreferences(preferences: UserPreferences): Result<UserPreferences>

    /**
     * 获取用户偏好设置
     */
    suspend fun getPreferences(): UserPreferences?

    /**
     * 登出
     */
    suspend fun logout(): Result<Unit>
}


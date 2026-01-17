package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.User

/**
 * 用户仓库接口（基础接口，同步风格）
 */
interface UserRepository {
    suspend fun getCurrentUser(): User?
    suspend fun updateUser(user: User): User
}

/**
 * 响应式用户仓库接口（扩展接口，客户端使用）
 */
interface ReactiveUserRepository : UserRepository {
    fun observeCurrentUser(): Flow<User?>
}


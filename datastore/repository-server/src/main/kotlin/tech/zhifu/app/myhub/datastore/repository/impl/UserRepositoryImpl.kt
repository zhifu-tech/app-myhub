package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import kotlin.time.Clock

/**
 * 用户仓库实现（服务端）
 * 使用 LocalUserDataSource 实现，避免代码重复
 */
class UserRepositoryImpl(
    private val localDataSource: LocalUserDataSource
) : UserRepository {

    // 注意：初始化默认用户需要在 Application 启动时调用
    suspend fun initializeDefaultUserIfNeeded() {
        val existing = localDataSource.getCurrentUser()
        if (existing == null) {
            val defaultUser = User(
                id = "user-1",
                username = "default",
                email = null,
                displayName = "Default User",
                avatarUrl = null,
                createdAt = Clock.System.now(),
                preferences = null
            )
            localDataSource.saveUser(defaultUser)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return localDataSource.getCurrentUser()
    }

    override suspend fun updateUser(user: User): User {
        localDataSource.saveUser(user)
        return user
    }
}


package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository

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
}

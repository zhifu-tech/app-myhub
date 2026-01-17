package tech.zhifu.app.myhub.datastore.datasource.impl

import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.model.User

/**
 * 远程用户数据源占位实现（仅用于测试）
 */
class RemoteUserDataSourceStub : RemoteUserDataSource {
    override suspend fun getCurrentUser(): User {
        throw IllegalStateException("No user found")
    }

    override suspend fun updateUser(user: User): User {
        return user
    }
}


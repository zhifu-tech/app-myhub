package tech.zhifu.app.myhub.datastore.datasource.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.UserContextProvider

/**
 * 用户上下文提供者实现（客户端）
 * 从 LocalUserDataSource 获取当前用户ID
 */
class UserContextProviderImpl(
    private val userDataSource: LocalUserDataSource
) : UserContextProvider {
    override suspend fun getCurrentUserId(): String? {
        return userDataSource.getCurrentUser()?.id ?: "user-001"
    }
}



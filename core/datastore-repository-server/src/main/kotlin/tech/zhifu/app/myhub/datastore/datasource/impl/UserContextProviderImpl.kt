package tech.zhifu.app.myhub.datastore.datasource.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.UserContextProvider

/**
 * 用户上下文提供者实现（服务端）
 * 从 LocalUserDataSource 获取当前用户ID
 *
 * 注意：当前实现使用默认用户，后续可以扩展为从请求中获取用户ID
 */
class UserContextProviderImpl(
    private val userDataSource: LocalUserDataSource
) : UserContextProvider {
    override suspend fun getCurrentUserId(): String? {
        return userDataSource.getCurrentUser()?.id ?: "user-001"
    }
}


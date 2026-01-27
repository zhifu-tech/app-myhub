package tech.zhifu.app.myhub.datastore.datasource.impl

import tech.zhifu.app.myhub.datastore.datasource.RemoteAuthDataSource
import tech.zhifu.app.myhub.network.auth.TokenRefreshProvider

/**
 * TokenRefreshProvider 适配器
 * 将 RemoteAuthDataSource 适配为 TokenRefreshProvider，用于 core/network 模块
 */
internal class TokenRefreshProviderAdapter(
    private val remoteAuthDataSource: RemoteAuthDataSource
) : TokenRefreshProvider {
    override suspend fun refreshToken(refreshToken: String): String {
        val response = remoteAuthDataSource.refreshToken(refreshToken)
        return response.accessToken
    }
}

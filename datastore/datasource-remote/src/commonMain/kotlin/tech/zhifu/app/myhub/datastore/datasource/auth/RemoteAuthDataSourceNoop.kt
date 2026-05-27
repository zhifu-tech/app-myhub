package tech.zhifu.app.myhub.datastore.datasource.auth

import tech.zhifu.app.myhub.datastore.model.dto.LoginResponse
import tech.zhifu.app.myhub.datastore.model.dto.RefreshTokenResponse

class RemoteAuthDataSourceNoop : RemoteAuthDataSource {
    override suspend fun login(
        userId: String,
        username: String?,
        displayName: String?,
        avatarUrl: String?,
        avatarText: String?
    ): LoginResponse = LoginResponse(
        accessToken = "dev-access-token",
        refreshToken = "dev-refresh-token",
        expiresIn = 0,
        tokenType = "Bearer",
        userId = userId,
        username = username ?: "dev-$userId"
    )

    override suspend fun refreshToken(refreshToken: String): RefreshTokenResponse =
        RefreshTokenResponse(
            accessToken = "dev-access-token",
            expiresIn = 0,
            tokenType = "Bearer"
        )
}

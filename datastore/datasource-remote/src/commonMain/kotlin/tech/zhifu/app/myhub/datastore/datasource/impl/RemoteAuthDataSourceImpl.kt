package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import tech.zhifu.app.myhub.datastore.datasource.RemoteAuthDataSource
import tech.zhifu.app.myhub.datastore.model.dto.LoginRequest
import tech.zhifu.app.myhub.datastore.model.dto.LoginResponse
import tech.zhifu.app.myhub.datastore.model.dto.RefreshTokenRequest
import tech.zhifu.app.myhub.datastore.model.dto.RefreshTokenResponse
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.network.ApiException
import tech.zhifu.app.myhub.network.NetworkException

/**
 * 认证数据源实现
 */
class RemoteAuthDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteAuthDataSource {

    override suspend fun login(
        userId: String,
        username: String?,
        displayName: String?,
        avatarUrl: String?,
        avatarText: String?
    ): LoginResponse = try {
        val request = LoginRequest(
            userId = userId,
            username = username,
            displayName = displayName,
            avatarUrl = avatarUrl,
            avatarText = avatarText
        )

        val response: io.ktor.client.statement.HttpResponse = httpClient.post(
            "${ApiConfig.BASE_URL}${ApiConfig.AUTH_PATH}/login"
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        when (response.status) {
            HttpStatusCode.OK -> response.body<LoginResponse>()
            else -> throw ApiException(
                "Login failed with status ${response.status.value}",
                null
            )
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Exception) {
        throw NetworkException("Network error during login: ${e.message}", e)
    }

    override suspend fun refreshToken(refreshToken: String): RefreshTokenResponse = try {
        val request = RefreshTokenRequest(refreshToken = refreshToken)

        val response: io.ktor.client.statement.HttpResponse = httpClient.post(
            "${ApiConfig.BASE_URL}${ApiConfig.AUTH_PATH}/refresh"
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        when (response.status) {
            HttpStatusCode.OK -> response.body<RefreshTokenResponse>()
            HttpStatusCode.Unauthorized -> throw ApiException(
                "Refresh token expired or invalid",
                null
            )
            else -> throw ApiException(
                "Token refresh failed with status ${response.status.value}",
                null
            )
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Exception) {
        throw NetworkException("Network error during token refresh: ${e.message}", e)
    }
}

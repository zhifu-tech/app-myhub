package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.network.ApiConfig
import tech.zhifu.app.myhub.datastore.network.ApiException
import tech.zhifu.app.myhub.datastore.network.NetworkException

/**
 * 远程用户数据源实现（使用Ktor Client）
 */
class RemoteUserDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteUserDataSource {

    override suspend fun getCurrentUser(): User {
        return try {
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.USERS_PATH}/current")
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                else -> throw ApiException("Failed to fetch current user: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching current user", e)
        }
    }

    override suspend fun updateUser(user: User): User {
        return try {
            // 构建更新请求
            val request = UpdateUserRequest(
                username = user.username,
                email = user.email,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
                preferences = user.preferences?.let { prefs ->
                    UpdateUserPreferencesRequest(
                        theme = prefs.theme,
                        language = prefs.language,
                        defaultCardType = prefs.defaultCardType?.name?.lowercase(),
                        autoSync = prefs.autoSync,
                        syncInterval = prefs.syncInterval
                    )
                }
            )
            val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.USERS_PATH}/current") {
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                else -> throw ApiException("Failed to update user: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while updating user", e)
        }
    }
}

/**
 * 更新用户请求
 */
@Serializable
data class UpdateUserRequest(
    val username: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val preferences: UpdateUserPreferencesRequest? = null
)

/**
 * 更新用户偏好请求
 */
@Serializable
data class UpdateUserPreferencesRequest(
    val theme: String? = null,
    val language: String? = null,
    val defaultCardType: String? = null,
    val autoSync: Boolean? = null,
    val syncInterval: Long? = null
)


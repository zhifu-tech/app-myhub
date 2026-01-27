package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.network.ApiException
import tech.zhifu.app.myhub.network.NetworkException

class RemoteUserDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteUserDataSource {

    override suspend fun fetchUser(userId: String): User? {
        return try {
            val response: HttpResponse = httpClient.get(
                "${ApiConfig.BASE_URL}${ApiConfig.USERS_PATH}/fetchUser?userId=${userId}"
            )
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                else -> throw ApiException("Failed to fetch current user: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching current user", e)
        }
    }

    override suspend fun updateUser(user: User): User = try {
        val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.USERS_PATH}/${user.id}") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }
        when (response.status) {
            HttpStatusCode.OK -> response.body()
            else -> throw ApiException("Failed to update user: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while updating user", e)
    }

    override suspend fun updateUserPreferences(userId: String, preferences: UserPreferences): UserPreferences = try {
        val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.USERS_PATH}/$userId/preferences") {
            contentType(ContentType.Application.Json)
            setBody(preferences)
        }
        when (response.status) {
            HttpStatusCode.OK -> response.body()
            else -> throw ApiException("Failed to update user preferences: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while updating user preferences", e)
    }
}

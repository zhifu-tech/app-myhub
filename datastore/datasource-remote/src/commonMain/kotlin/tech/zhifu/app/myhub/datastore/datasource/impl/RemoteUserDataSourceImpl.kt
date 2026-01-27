package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.model.dto.CreateUserRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateUserRequest
import tech.zhifu.app.myhub.datastore.model.dto.UserResponse
import tech.zhifu.app.myhub.datastore.model.dto.toDomain
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.network.ApiException
import tech.zhifu.app.myhub.network.NetworkException

class RemoteUserDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteUserDataSource {

    override suspend fun getUser(id: String): User? {
        return try {
            val response: HttpResponse = httpClient.get(
                "${ApiConfig.BASE_URL}${ApiConfig.USERS_PATH}/$id"
            )
            when (response.status) {
                HttpStatusCode.OK -> {
                    val userResponse: UserResponse = response.body()
                    userResponse.toDomain()
                }
                HttpStatusCode.NotFound -> null
                else -> throw ApiException("Failed to fetch user: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching user", e)
        }
    }

    override suspend fun createUser(user: User): User = try {
        val request = CreateUserRequest(
            username = user.username,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            avatarText = user.avatarText,
            status = user.status
        )
        val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.USERS_PATH}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status) {
            HttpStatusCode.Created -> {
                val userResponse: UserResponse = response.body()
                userResponse.toDomain()
            }
            else -> throw ApiException("Failed to create user: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while creating user", e)
    }

    override suspend fun updateUser(id: String, user: User): User = try {
        val request = UpdateUserRequest(
            username = user.username,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            avatarText = user.avatarText,
            status = user.status
        )
        val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.USERS_PATH}/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                val userResponse: UserResponse = response.body()
                userResponse.toDomain()
            }
            else -> throw ApiException("Failed to update user: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while updating user", e)
    }

    override suspend fun deleteUser(id: String) = try {
        val response: HttpResponse = httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.USERS_PATH}/$id")
        when (response.status) {
            HttpStatusCode.OK, HttpStatusCode.NoContent -> Unit
            HttpStatusCode.NotFound -> Unit // 已删除，视为成功
            else -> throw ApiException("Failed to delete user: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while deleting user", e)
    }

    override suspend fun getUserPreferences(userId: String): UserPreferences? = try {
        val response: HttpResponse = httpClient.get(
            "${ApiConfig.BASE_URL}${ApiConfig.USERS_PATH}/$userId/preferences"
        )
        when (response.status) {
            HttpStatusCode.OK -> response.body<UserPreferences>()
            HttpStatusCode.NotFound -> null
            else -> throw ApiException("Failed to get user preferences: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while getting user preferences", e)
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

package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.network.ApiException
import tech.zhifu.app.myhub.network.NetworkException

/**
 * 远程用户数据源实现（使用Ktor Client）
 */
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
}

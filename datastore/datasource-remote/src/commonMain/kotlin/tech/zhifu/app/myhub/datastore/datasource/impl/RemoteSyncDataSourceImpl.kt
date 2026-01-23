package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLPath
import tech.zhifu.app.myhub.datastore.datasource.RemoteSyncDataSource
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.network.ApiException
import tech.zhifu.app.myhub.network.NetworkException
import tech.zhifu.app.myhub.sync.SyncOutboxUploadItem
import tech.zhifu.app.myhub.sync.SyncPullResponse
import tech.zhifu.app.myhub.sync.SyncPushRequest
import tech.zhifu.app.myhub.sync.SyncPushResponse

class RemoteSyncDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteSyncDataSource {
    override suspend fun pullChanges(
        userId: String,
        entityType: String,
        sinceToken: String?,
        limit: Int
    ): SyncPullResponse = try {
        val query = buildString {
            append("?userId=")
            append(userId.encodeURLPath())
            append("&entityType=")
            append(entityType.encodeURLPath())
            if (!sinceToken.isNullOrBlank()) {
                append("&since=")
                append(sinceToken.encodeURLPath())
            }
            append("&limit=")
            append(limit)
        }
        val response: HttpResponse = httpClient.get(
            "${ApiConfig.BASE_URL}${ApiConfig.SYNC_PATH}/pull$query"
        )
        when (response.status) {
            HttpStatusCode.OK -> response.body()
            else -> throw ApiException("Failed to pull sync changes: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while pulling sync changes", e)
    }

    override suspend fun pushOutbox(
        userId: String,
        items: List<SyncOutboxUploadItem>
    ): SyncPushResponse = try {
        val response: HttpResponse = httpClient.post(
            "${ApiConfig.BASE_URL}${ApiConfig.SYNC_PATH}/push"
        ) {
            setBody(SyncPushRequest(userId = userId, items = items))
        }
        when (response.status) {
            HttpStatusCode.OK -> response.body()
            else -> throw ApiException("Failed to push sync outbox: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while pushing sync outbox", e)
    }
}

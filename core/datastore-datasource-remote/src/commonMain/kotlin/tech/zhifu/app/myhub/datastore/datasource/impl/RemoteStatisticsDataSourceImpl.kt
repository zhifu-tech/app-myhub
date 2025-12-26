package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import tech.zhifu.app.myhub.datastore.datasource.RemoteStatisticsDataSource
import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.datastore.network.ApiConfig
import tech.zhifu.app.myhub.datastore.network.ApiException
import tech.zhifu.app.myhub.datastore.network.NetworkException

/**
 * 远程统计信息数据源实现（使用Ktor Client）
 */
class RemoteStatisticsDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteStatisticsDataSource {

    override suspend fun getStatistics(): Statistics {
        return try {
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.STATISTICS_PATH}")
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                else -> throw ApiException("Failed to fetch statistics: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching statistics", e)
        }
    }
}


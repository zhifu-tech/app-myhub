package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.network.ApiException
import tech.zhifu.app.myhub.network.NetworkException

/**
 * 远程卡片数据源实现（使用Ktor Client）
 */
class RemoteCardDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteCardDataSource {

    override suspend fun getCards(userId: String): List<Card> = try {
        val response: HttpResponse = httpClient.get(
            "${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/fetchCards?userId=$userId"
        )
        when (response.status) {
            HttpStatusCode.OK -> response.body()
            else -> throw ApiException("Failed to fetch cards: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while fetching cards", e)
    }

    override suspend fun getCardById(id: String): Card? = try {
        val response: HttpResponse = httpClient.get(
            "${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/fetchCard?cardId=$id"
        )
        when (response.status) {
            HttpStatusCode.OK -> response.body()
            HttpStatusCode.NotFound -> null
            else -> throw ApiException("Failed to fetch card: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while fetching card", e)
    }
}

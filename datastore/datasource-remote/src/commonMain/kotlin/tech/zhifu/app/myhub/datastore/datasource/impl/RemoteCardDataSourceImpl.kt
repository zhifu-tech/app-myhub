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
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.network.ApiException
import tech.zhifu.app.myhub.network.NetworkException

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

    override suspend fun upsertCard(card: Card): Card = try {
        val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/${card.id}") {
            contentType(ContentType.Application.Json)
            setBody(card)
        }
        when (response.status) {
            HttpStatusCode.OK, HttpStatusCode.Created -> response.body()
            else -> throw ApiException("Failed to upsert card: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while upserting card", e)
    }

    override suspend fun deleteCard(cardId: String) = try {
        val response: HttpResponse = httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/$cardId")
        when (response.status) {
            HttpStatusCode.OK, HttpStatusCode.NoContent -> Unit
            HttpStatusCode.NotFound -> Unit // 已删除，视为成功
            else -> throw ApiException("Failed to delete card: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while deleting card", e)
    }
}

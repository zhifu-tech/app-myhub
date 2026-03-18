package tech.zhifu.app.myhub.datastore.datasource.card

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.content
import tech.zhifu.app.myhub.datastore.model.dto.CardResponse
import tech.zhifu.app.myhub.datastore.model.dto.CreateCardRequest
import tech.zhifu.app.myhub.datastore.model.dto.PaginatedResponse
import tech.zhifu.app.myhub.datastore.model.dto.PartialUpdateCardRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateCardRequest
import tech.zhifu.app.myhub.datastore.model.dto.toDomain
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.network.NetworkException

class RemoteCardDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteCardDataSource {

    override suspend fun getCards(
        userId: String,
        page: Int,
        limit: Int,
        type: String?,
        isFavorite: Boolean?
    ): List<Card> = try {
        val query = buildString {
            append("?page=")
            append(page)
            append("&limit=")
            append(limit)
            type?.let {
                append("&type=")
                append(it.encodeURLPath())
            }
            isFavorite?.let {
                append("&isFavorite=")
                append(it)
            }
        }
        val response: HttpResponse = httpClient.get(
            "${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}$query"
        )
        when (response.status) {
            HttpStatusCode.OK -> {
                val paginatedResponse: PaginatedResponse<CardResponse> = response.body()
                paginatedResponse.data.map { it.toDomain() }
            }

            else -> throw ApiException(response.status, "Failed to fetch cards: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while fetching cards", e)
    }

    override suspend fun getCardById(id: String): Card? = try {
        val response: HttpResponse = httpClient.get(
            "${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/$id"
        )
        when (response.status) {
            HttpStatusCode.OK -> {
                val cardResponse: CardResponse = response.body()
                cardResponse.toDomain()
            }

            HttpStatusCode.NotFound -> null
            else -> throw ApiException(response.status, "Failed to fetch card: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while fetching card", e)
    }

    override suspend fun createCard(card: Card): Card = try {
        val request = CreateCardRequest(
            type = card.type.wire,
            title = card.metadata.content?.title,
            content = card.metadata.content?.content.orEmpty(),
            tagIds = card.tags.map { it.id }
        )
        val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status) {
            HttpStatusCode.Created -> {
                val cardResponse: CardResponse = response.body()
                cardResponse.toDomain()
            }

            else -> throw ApiException(response.status, "Failed to create card: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while creating card", e)
    }

    override suspend fun updateCard(card: Card): Card = try {
        val request = UpdateCardRequest(
            type = card.type.wire,
            title = card.metadata.content?.title,
            content = card.metadata.content?.content.orEmpty(),
            tagIds = card.tags.map { it.id }
        )
        val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/${card.id}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                val cardResponse: CardResponse = response.body()
                cardResponse.toDomain()
            }

            else -> throw ApiException(response.status, "Failed to update card: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while updating card", e)
    }

    override suspend fun partialUpdateCard(card: Card): Card = try {
        val request = PartialUpdateCardRequest(
            type = card.type.wire,
            title = card.metadata.content?.title,
            content = card.metadata.content?.content.orEmpty(),
            tagIds = card.tags.map { it.id }
        )
        val response: HttpResponse = httpClient.patch("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/${card.id}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                val cardResponse: CardResponse = response.body()
                cardResponse.toDomain()
            }

            else -> throw ApiException(response.status, "Failed to partially update card: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while partially updating card", e)
    }

    override suspend fun deleteCard(cardId: String) = try {
        val response: HttpResponse = httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/$cardId")
        when (response.status) {
            HttpStatusCode.OK, HttpStatusCode.NoContent -> Unit
            HttpStatusCode.NotFound -> Unit // 已删除，视为成功
            else -> throw ApiException(response.status, "Failed to delete card: ${response.status}")
        }
    } catch (e: Exception) {
        if (e is ApiException) throw e
        throw NetworkException("Network error while deleting card", e)
    }
}

package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.dto.CardDto
import tech.zhifu.app.myhub.datastore.dto.CreateCardRequest
import tech.zhifu.app.myhub.datastore.dto.UpdateCardRequest
import tech.zhifu.app.myhub.datastore.model.SearchFilter
import tech.zhifu.app.myhub.datastore.network.ApiConfig

/**
 * 远程卡片数据源实现（使用Ktor Client）
 */
class RemoteCardDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteCardDataSource {

    override suspend fun getAllCards(): List<CardDto> {
        return try {
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}")
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                else -> throw ApiException("Failed to fetch cards: ${response.status}")
            }
        } catch (e: Exception) {
            throw NetworkException("Network error while fetching cards", e)
        }
    }

    override suspend fun getCardById(id: String): CardDto? {
        return try {
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/$id")
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

    override suspend fun searchCards(filter: SearchFilter): List<CardDto> {
        return try {
            // 构建查询参数
            val queryParams = buildString {
                append("?")
                filter.query?.let { append("q=$it&") }
                if (filter.cardTypes.isNotEmpty()) {
                    append("types=${filter.cardTypes.joinToString(",") { it.name.lowercase() }}&")
                }
                if (filter.tags.isNotEmpty()) {
                    append("tags=${filter.tags.joinToString(",")}&")
                }
                filter.isFavorite?.let { append("favorite=$it&") }
                filter.isTemplate?.let { append("template=$it&") }
                append("sort=${filter.sortBy.name}")
            }

            val response: HttpResponse = httpClient.get(
                "${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/search$queryParams"
            )
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                else -> throw ApiException("Failed to search cards: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while searching cards", e)
        }
    }

    override suspend fun createCard(request: CreateCardRequest): CardDto {
        return try {
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}") {
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.Created, HttpStatusCode.OK -> response.body()
                else -> throw ApiException("Failed to create card: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while creating card", e)
        }
    }

    override suspend fun updateCard(id: String, request: UpdateCardRequest): CardDto {
        return try {
            val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/$id") {
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                HttpStatusCode.NotFound -> throw ApiException("Card not found: $id")
                else -> throw ApiException("Failed to update card: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while updating card", e)
        }
    }

    override suspend fun deleteCard(id: String) {
        try {
            val response: HttpResponse = httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/$id")
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> {
                    // 成功删除
                }

                HttpStatusCode.NotFound -> {
                    // 卡片不存在，视为成功
                }

                else -> throw ApiException("Failed to delete card: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while deleting card", e)
        }
    }

    override suspend fun toggleFavorite(id: String): CardDto {
        return try {
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/$id/favorite")
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                HttpStatusCode.NotFound -> throw ApiException("Card not found: $id")
                else -> throw ApiException("Failed to toggle favorite: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while toggling favorite", e)
        }
    }
}

/**
 * API异常
 */
class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 网络异常
 */
class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)


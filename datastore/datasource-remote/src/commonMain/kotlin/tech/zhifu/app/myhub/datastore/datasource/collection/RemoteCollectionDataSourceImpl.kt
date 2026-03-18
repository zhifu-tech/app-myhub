package tech.zhifu.app.myhub.datastore.datasource.collection

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
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.dto.CollectionResponse
import tech.zhifu.app.myhub.datastore.model.dto.CreateCollectionRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateCollectionRequest
import tech.zhifu.app.myhub.datastore.model.dto.toDomain
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.network.NetworkException

class RemoteCollectionDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteCollectionDataSource {

    override suspend fun getCollections(
        userId: String,
        page: Int?,
        pageSize: Int?
    ): List<Collection> {
        return try {
            val query = buildString {
                page?.let {
                    append("?page=")
                    append(it)
                }
                pageSize?.let {
                    if (page != null) {
                        append("&pageSize=")
                    } else {
                        append("?pageSize=")
                    }
                    append(it)
                }
            }
            val response: HttpResponse = httpClient.get(
                "${ApiConfig.BASE_URL}${ApiConfig.COLLECTIONS_PATH}$query"
            )
            when (response.status) {
                HttpStatusCode.OK -> {
                    val collectionResponses: List<CollectionResponse> = response.body()
                    collectionResponses.map { it.toDomain() }
                }

                else -> throw ApiException(response.status, "Failed to fetch collections: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching collections", e)
        }
    }

    override suspend fun getCollectionById(id: String, userId: String): Collection? {
        return try {
            val response: HttpResponse = httpClient.get(
                "${ApiConfig.BASE_URL}${ApiConfig.COLLECTIONS_PATH}/$id"
            )
            when (response.status) {
                HttpStatusCode.OK -> {
                    val collectionResponse: CollectionResponse = response.body()
                    collectionResponse.toDomain()
                }

                HttpStatusCode.NotFound -> null
                else -> throw ApiException(response.status, "Failed to fetch collection: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching collection", e)
        }
    }

    override suspend fun createCollection(collection: Collection, userId: String): Collection {
        return try {
            val request = CreateCollectionRequest(
                name = collection.name,
                topic = collection.topic,
                description = collection.description
            )
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.COLLECTIONS_PATH}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.Created -> {
                    val collectionResponse: CollectionResponse = response.body()
                    collectionResponse.toDomain()
                }

                else -> throw ApiException(response.status, "Failed to create collection: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while creating collection", e)
        }
    }

    override suspend fun updateCollection(id: String, collection: Collection, userId: String): Collection {
        return try {
            val request = UpdateCollectionRequest(
                name = collection.name,
                topic = collection.topic,
                description = collection.description
            )
            val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.COLLECTIONS_PATH}/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val collectionResponse: CollectionResponse = response.body()
                    collectionResponse.toDomain()
                }

                HttpStatusCode.NotFound -> throw ApiException(response.status, "Collection not found: $id")
                else -> throw ApiException(response.status, "Failed to update collection: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while updating collection", e)
        }
    }

    override suspend fun deleteCollection(id: String, userId: String) {
        try {
            val response: HttpResponse = httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.COLLECTIONS_PATH}/$id")
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> {
                    // 成功删除
                }

                HttpStatusCode.NotFound -> {
                    // 卡集不存在，视为成功
                }

                else -> throw ApiException(response.status, "Failed to delete collection: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while deleting collection", e)
        }
    }
}

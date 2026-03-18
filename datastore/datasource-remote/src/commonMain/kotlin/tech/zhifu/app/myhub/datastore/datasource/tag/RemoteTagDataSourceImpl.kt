package tech.zhifu.app.myhub.datastore.datasource.tag

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
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.model.dto.CreateTagRequest
import tech.zhifu.app.myhub.datastore.model.dto.TagResponse
import tech.zhifu.app.myhub.datastore.model.dto.UpdateTagRequest
import tech.zhifu.app.myhub.datastore.model.dto.toDomain
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.network.NetworkException

/**
 * 远程标签数据源实现（使用Ktor Client）
 */
class RemoteTagDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteTagDataSource {

    override suspend fun getTags(userId: String): List<Tag> {
        return try {
            val response: HttpResponse = httpClient.get(
                "${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}"
            )
            when (response.status) {
                HttpStatusCode.OK -> {
                    val tagResponses: List<TagResponse> = response.body()
                    // TagResponse 不包含 userId，需要从参数传入
                    tagResponses.map { tagResponse ->
                        tagResponse.toDomain().copy(userId = userId)
                    }
                }

                else -> throw ApiException(response.status, "Failed to fetch tags: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching tags", e)
        }
    }

    override suspend fun getTagById(id: String, userId: String): Tag? {
        return try {
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}/$id")
            when (response.status) {
                HttpStatusCode.OK -> {
                    val tagResponse: TagResponse = response.body()
                    tagResponse.toDomain().copy(userId = userId)
                }

                HttpStatusCode.NotFound -> null
                else -> throw ApiException(response.status, "Failed to fetch tag: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching tag", e)
        }
    }

    override suspend fun createTag(tag: Tag, userId: String): Tag {
        return try {
            val request = CreateTagRequest(
                name = tag.name,
                color = tag.color,
                description = tag.description
            )
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.Created -> {
                    val tagResponse: TagResponse = response.body()
                    tagResponse.toDomain().copy(userId = userId)
                }

                else -> throw ApiException(response.status, "Failed to create tag: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while creating tag", e)
        }
    }

    override suspend fun updateTag(id: String, tag: Tag, userId: String): Tag {
        return try {
            val request = UpdateTagRequest(
                name = tag.name,
                color = tag.color,
                description = tag.description
            )
            val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val tagResponse: TagResponse = response.body()
                    tagResponse.toDomain().copy(userId = userId)
                }

                HttpStatusCode.NotFound -> throw ApiException(response.status, "Tag not found: $id")
                else -> throw ApiException(response.status, "Failed to update tag: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while updating tag", e)
        }
    }

    override suspend fun deleteTag(id: String, userId: String) {
        try {
            val response: HttpResponse = httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}/$id")
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> {
                    // 成功删除
                }

                HttpStatusCode.NotFound -> {
                    // 标签不存在，视为成功
                }

                else -> throw ApiException(response.status, "Failed to delete tag: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while deleting tag", e)
        }
    }
}


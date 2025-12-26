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
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.datastore.datasource.RemoteTagDataSource
import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.datastore.network.ApiConfig
import tech.zhifu.app.myhub.datastore.network.ApiException
import tech.zhifu.app.myhub.datastore.network.NetworkException

/**
 * 远程标签数据源实现（使用Ktor Client）
 */
class RemoteTagDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteTagDataSource {

    override suspend fun getAllTags(): List<Tag> {
        return try {
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}")
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                else -> throw ApiException("Failed to fetch tags: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching tags", e)
        }
    }

    override suspend fun getTagById(id: String): Tag? {
        return try {
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}/$id")
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                HttpStatusCode.NotFound -> null
                else -> throw ApiException("Failed to fetch tag: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching tag", e)
        }
    }

    override suspend fun createTag(tag: Tag): Tag {
        return try {
            // 构建创建请求
            val request = CreateTagRequest(
                name = tag.name,
                color = tag.color,
                description = tag.description
            )
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}") {
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.Created, HttpStatusCode.OK -> response.body()
                else -> throw ApiException("Failed to create tag: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while creating tag", e)
        }
    }

    override suspend fun updateTag(tag: Tag): Tag {
        return try {
            // 构建更新请求
            val request = UpdateTagRequest(
                name = tag.name,
                color = tag.color,
                description = tag.description
            )
            val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}/${tag.id}") {
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                HttpStatusCode.NotFound -> throw ApiException("Tag not found: ${tag.id}")
                else -> throw ApiException("Failed to update tag: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while updating tag", e)
        }
    }

    override suspend fun deleteTag(id: String) {
        try {
            val response: HttpResponse = httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.TAGS_PATH}/$id")
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> {
                    // 成功删除
                }

                HttpStatusCode.NotFound -> {
                    // 标签不存在，视为成功
                }

                else -> throw ApiException("Failed to delete tag: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while deleting tag", e)
        }
    }
}

/**
 * 创建标签请求
 */
@Serializable
data class CreateTagRequest(
    val name: String,
    val color: String? = null,
    val description: String? = null
)

/**
 * 更新标签请求
 */
@Serializable
data class UpdateTagRequest(
    val name: String? = null,
    val color: String? = null,
    val description: String? = null
)


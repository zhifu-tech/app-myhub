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
import tech.zhifu.app.myhub.datastore.datasource.RemoteTemplateDataSource
import tech.zhifu.app.myhub.datastore.model.CardMetadataDto
import tech.zhifu.app.myhub.datastore.model.toDto
import tech.zhifu.app.myhub.datastore.model.Template
import tech.zhifu.app.myhub.datastore.network.ApiConfig
import tech.zhifu.app.myhub.datastore.network.ApiException
import tech.zhifu.app.myhub.datastore.network.NetworkException

/**
 * 远程模板数据源实现（使用Ktor Client）
 */
class RemoteTemplateDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteTemplateDataSource {

    override suspend fun getAllTemplates(): List<Template> {
        return try {
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}")
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                else -> throw ApiException("Failed to fetch templates: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching templates", e)
        }
    }

    override suspend fun getTemplateById(id: String): Template? {
        return try {
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}/$id")
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                HttpStatusCode.NotFound -> null
                else -> throw ApiException("Failed to fetch template: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching template", e)
        }
    }

    override suspend fun createTemplate(template: Template): Template {
        return try {
            // 构建创建请求
            val request = CreateTemplateRequest(
                name = template.name,
                description = template.description,
                cardType = template.cardType.name.lowercase(),
                previewImageUrl = template.previewImageUrl,
                defaultContent = template.defaultContent,
                defaultMetadata = template.defaultMetadata?.toDto(),
                defaultTags = template.defaultTags.takeIf { it.isNotEmpty() },
                isSystemTemplate = template.isSystemTemplate
            )
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}") {
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.Created, HttpStatusCode.OK -> response.body()
                else -> throw ApiException("Failed to create template: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while creating template", e)
        }
    }

    override suspend fun updateTemplate(template: Template): Template {
        return try {
            // 构建更新请求
            val request = UpdateTemplateRequest(
                name = template.name,
                description = template.description,
                cardType = template.cardType.name.lowercase(),
                previewImageUrl = template.previewImageUrl,
                defaultContent = template.defaultContent,
                defaultMetadata = template.defaultMetadata?.toDto(),
                defaultTags = template.defaultTags.takeIf { it.isNotEmpty() }
            )
            val response: HttpResponse =
                httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}/${template.id}") {
                    setBody(request)
                }
            when (response.status) {
                HttpStatusCode.OK -> response.body()
                HttpStatusCode.NotFound -> throw ApiException("Template not found: ${template.id}")
                else -> throw ApiException("Failed to update template: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while updating template", e)
        }
    }

    override suspend fun deleteTemplate(id: String) {
        try {
            val response: HttpResponse = httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}/$id")
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> {
                    // 成功删除
                }

                HttpStatusCode.NotFound -> {
                    // 模板不存在，视为成功
                }

                else -> throw ApiException("Failed to delete template: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while deleting template", e)
        }
    }
}

/**
 * 创建模板请求
 */
@Serializable
data class CreateTemplateRequest(
    val name: String,
    val description: String? = null,
    val cardType: String,
    val previewImageUrl: String? = null,
    val defaultContent: String? = null,
    val defaultMetadata: CardMetadataDto? = null,
    val defaultTags: List<String>? = null,
    val isSystemTemplate: Boolean? = null
)

/**
 * 更新模板请求
 */
@Serializable
data class UpdateTemplateRequest(
    val name: String? = null,
    val description: String? = null,
    val cardType: String? = null,
    val previewImageUrl: String? = null,
    val defaultContent: String? = null,
    val defaultMetadata: CardMetadataDto? = null,
    val defaultTags: List<String>? = null
)


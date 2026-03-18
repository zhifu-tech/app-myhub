package tech.zhifu.app.myhub.datastore.datasource.card

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
import io.ktor.http.encodeURLPath
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import tech.zhifu.app.myhub.datastore.model.dto.CardTemplateResponse
import tech.zhifu.app.myhub.datastore.model.dto.CreateCardTemplateRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateCardTemplateRequest
import tech.zhifu.app.myhub.datastore.model.dto.toDomain
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.network.NetworkException

class RemoteCardTemplateDataSourceImpl(
    private val httpClient: HttpClient
) : RemoteCardTemplateDataSource {

    override suspend fun getTemplates(type: String?): List<CardTemplate> {
        return try {
            val url = if (type != null) {
                "${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}?type=${type.encodeURLPath()}"
            } else {
                "${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}"
            }
            val response: HttpResponse = httpClient.get(url)
            when (response.status) {
                HttpStatusCode.OK -> {
                    val templateResponses: List<CardTemplateResponse> = response.body()
                    templateResponses.map { it.toDomain() }
                }

                else -> throw ApiException(response.status, "Failed to fetch templates: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching templates", e)
        }
    }

    override suspend fun getTemplateById(id: String): CardTemplate? {
        return try {
            val response: HttpResponse = httpClient.get(
                "${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}/$id"
            )
            when (response.status) {
                HttpStatusCode.OK -> {
                    val templateResponse: CardTemplateResponse = response.body()
                    templateResponse.toDomain()
                }

                HttpStatusCode.NotFound -> null
                else -> throw ApiException(response.status, "Failed to fetch template: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while fetching template", e)
        }
    }

    override suspend fun createTemplate(template: CardTemplate): CardTemplate {
        return try {
            val request = CreateCardTemplateRequest(
                type = template.type,
                title = template.title,
                content = template.content,
                description = template.description
            )
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.Created -> {
                    val templateResponse: CardTemplateResponse = response.body()
                    templateResponse.toDomain()
                }

                else -> throw ApiException(response.status, "Failed to create template: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while creating template", e)
        }
    }

    override suspend fun updateTemplate(id: String, template: CardTemplate): CardTemplate {
        return try {
            val request = UpdateCardTemplateRequest(
                type = template.type,
                title = template.title,
                content = template.content,
                description = template.description
            )
            val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.TEMPLATES_PATH}/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val templateResponse: CardTemplateResponse = response.body()
                    templateResponse.toDomain()
                }

                HttpStatusCode.NotFound -> throw ApiException(response.status, "Template not found: $id")
                else -> throw ApiException(response.status, "Failed to update template: ${response.status}")
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

                else -> throw ApiException(response.status, "Failed to delete template: ${response.status}")
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw NetworkException("Network error while deleting template", e)
        }
    }
}

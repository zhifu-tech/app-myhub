package tech.zhifu.app.myhub.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import tech.zhifu.app.myhub.datastore.model.toDomain
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Template
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.service.TemplateService
import kotlin.time.Clock

/**
 * 模板 API 路由
 */
fun Route.templatesApi(templateService: TemplateService) {
    route("/api/templates") {
        // GET /api/templates - 获取所有模板
        get {
            try {
                val templates = templateService.getAllTemplates()
                call.respond(HttpStatusCode.OK, templates)
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to get templates: ${e.message}", e)
            }
        }

        // GET /api/templates/{id} - 获取指定模板
        get("{id}") {
            try {
                val id = call.parameters["id"] ?: throw tech.zhifu.app.myhub.exception.ValidationException("Template ID is required")
                val template = templateService.getTemplateById(id)
                    ?: throw tech.zhifu.app.myhub.exception.NotFoundException("Template", id)
                call.respond(HttpStatusCode.OK, template)
            } catch (e: ApiException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to get template: ${e.message}", e)
            }
        }

        // POST /api/templates - 创建新模板
        post {
            try {
                val request = call.receive<CreateTemplateRequest>()
                val template = Template(
                    id = generateId(),
                    name = request.name,
                    description = request.description,
                    cardType = CardType.valueOf(request.cardType.uppercase()),
                    previewImageUrl = request.previewImageUrl,
                    defaultContent = request.defaultContent,
                    defaultMetadata = request.defaultMetadata?.toDomain(),
                    defaultTags = request.defaultTags ?: emptyList(),
                    usageCount = 0,
                    isSystemTemplate = request.isSystemTemplate ?: false,
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now()
                )
                val created = templateService.createTemplate(template)
                call.respond(HttpStatusCode.Created, created)
            } catch (e: tech.zhifu.app.myhub.exception.ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to create template: ${e.message}", e)
            }
        }

        // PUT /api/templates/{id} - 更新模板
        put("{id}") {
            try {
                val id = call.parameters["id"] ?: throw tech.zhifu.app.myhub.exception.ValidationException("Template ID is required")
                val request = call.receive<UpdateTemplateRequest>()
                val existing = templateService.getTemplateById(id)
                    ?: throw tech.zhifu.app.myhub.exception.NotFoundException("Template", id)

                val updated = existing.copy(
                    name = request.name ?: existing.name,
                    description = request.description ?: existing.description,
                    cardType = request.cardType?.let { CardType.valueOf(it.uppercase()) } ?: existing.cardType,
                    previewImageUrl = request.previewImageUrl ?: existing.previewImageUrl,
                    defaultContent = request.defaultContent ?: existing.defaultContent,
                    defaultMetadata = request.defaultMetadata?.toDomain() ?: existing.defaultMetadata,
                    defaultTags = request.defaultTags ?: existing.defaultTags,
                    updatedAt = Clock.System.now()
                )

                val template = templateService.updateTemplate(updated)
                call.respond(HttpStatusCode.OK, template)
            } catch (e: tech.zhifu.app.myhub.exception.NotFoundException) {
                throw e
            } catch (e: tech.zhifu.app.myhub.exception.ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to update template: ${e.message}", e)
            }
        }

        // DELETE /api/templates/{id} - 删除模板
        delete("{id}") {
            try {
                val id = call.parameters["id"] ?: throw tech.zhifu.app.myhub.exception.ValidationException("Template ID is required")
                val deleted = templateService.deleteTemplate(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    throw tech.zhifu.app.myhub.exception.NotFoundException("Template", id)
                }
            } catch (e: tech.zhifu.app.myhub.exception.NotFoundException) {
                throw e
            } catch (e: tech.zhifu.app.myhub.exception.ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to delete template: ${e.message}", e)
            }
        }
    }
}

/**
 * 创建模板请求
 */
@kotlinx.serialization.Serializable
data class CreateTemplateRequest(
    val name: String,
    val description: String? = null,
    val cardType: String,
    val previewImageUrl: String? = null,
    val defaultContent: String? = null,
    val defaultMetadata: tech.zhifu.app.myhub.datastore.model.CardMetadataDto? = null,
    val defaultTags: List<String>? = null,
    val isSystemTemplate: Boolean? = null
)

/**
 * 更新模板请求
 */
@kotlinx.serialization.Serializable
data class UpdateTemplateRequest(
    val name: String? = null,
    val description: String? = null,
    val cardType: String? = null,
    val previewImageUrl: String? = null,
    val defaultContent: String? = null,
    val defaultMetadata: tech.zhifu.app.myhub.datastore.model.CardMetadataDto? = null,
    val defaultTags: List<String>? = null
)

private fun generateId(): String {
    return "template-${System.currentTimeMillis()}-${(0..9999).random()}"
}


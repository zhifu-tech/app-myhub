package tech.zhifu.app.myhub.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import tech.zhifu.app.myhub.datastore.model.dto.CreateCardTemplateRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateCardTemplateRequest
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.exception.NotFoundException
import tech.zhifu.app.myhub.exception.UnauthorizedException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.service.CardTemplateService

/**
 * 卡片模板 API 路由（RESTful 设计）
 *
 * GET    /api/templates              - 获取所有模板
 * GET    /api/templates?type={type}  - 根据类型获取模板
 * GET    /api/templates/{id}         - 获取指定模板
 * POST   /api/templates              - 创建模板
 * PUT    /api/templates/{id}         - 完整更新模板
 * DELETE /api/templates/{id}         - 删除模板
 */
fun Route.cardTemplatesApi(templateService: CardTemplateService) {
    authenticate("auth-bearer") {
        route("/api/templates") {
            // GET /api/templates - 获取所有模板（支持按类型筛选）
            get {
                try {
                    val type = call.request.queryParameters["type"]

                    val templates = if (type != null) {
                        templateService.getTemplatesByType(type)
                    } else {
                        templateService.getTemplates()
                    }

                    call.respond(HttpStatusCode.OK, templates)
                } catch (e: UnauthorizedException) {
                    throw e
                } catch (e: Exception) {
                    throw ApiException(
                        HttpStatusCode.InternalServerError,
                        "Failed to get templates: ${e.message}",
                        e
                    )
                }
            }

            // GET /api/templates/{id} - 获取指定模板
            get("{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw ValidationException("Template ID is required")

                    val template = templateService.getTemplate(id)
                    call.respond(HttpStatusCode.OK, template)
                } catch (e: UnauthorizedException) {
                    throw e
                } catch (e: NotFoundException) {
                    throw e
                } catch (e: ValidationException) {
                    throw e
                } catch (e: Exception) {
                    throw ApiException(
                        HttpStatusCode.InternalServerError,
                        "Failed to get template: ${e.message}",
                        e
                    )
                }
            }

            // POST /api/templates - 创建模板
            post {
                try {
                    val request = call.receive<CreateCardTemplateRequest>()
                    val template = templateService.createTemplate(request)

                    call.respond(HttpStatusCode.Created, template)
                } catch (e: UnauthorizedException) {
                    throw e
                } catch (e: ValidationException) {
                    throw e
                } catch (e: Exception) {
                    throw ApiException(
                        HttpStatusCode.InternalServerError,
                        "Failed to create template: ${e.message}",
                        e
                    )
                }
            }

            // PUT /api/templates/{id} - 完整更新模板
            put("{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw ValidationException("Template ID is required")

                    val request = call.receive<UpdateCardTemplateRequest>()
                    val template = templateService.updateTemplate(id, request)

                    call.respond(HttpStatusCode.OK, template)
                } catch (e: UnauthorizedException) {
                    throw e
                } catch (e: NotFoundException) {
                    throw e
                } catch (e: ValidationException) {
                    throw e
                } catch (e: Exception) {
                    throw ApiException(
                        HttpStatusCode.InternalServerError,
                        "Failed to update template: ${e.message}",
                        e
                    )
                }
            }

            // DELETE /api/templates/{id} - 删除模板
            delete("{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw ValidationException("Template ID is required")

                    templateService.deleteTemplate(id)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: UnauthorizedException) {
                    throw e
                } catch (e: NotFoundException) {
                    throw e
                } catch (e: ValidationException) {
                    throw e
                } catch (e: Exception) {
                    throw ApiException(
                        HttpStatusCode.InternalServerError,
                        "Failed to delete template: ${e.message}",
                        e
                    )
                }
            }
        }
    }
}

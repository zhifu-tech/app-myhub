package tech.zhifu.app.myhub.api.tag

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
import org.koin.ktor.ext.get
import tech.zhifu.app.myhub.auth.getCurrentUserId
import tech.zhifu.app.myhub.datastore.model.dto.CreateTagRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateTagRequest
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.exception.ForbiddenException
import tech.zhifu.app.myhub.exception.NotFoundException
import tech.zhifu.app.myhub.exception.UnauthorizedException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.service.TagService

/**
 * 标签 API 路由（领域：tag）
 *
 * GET    /api/tags              - 获取用户的所有标签
 * GET    /api/tags/{id}         - 获取指定标签
 * POST   /api/tags              - 创建标签
 * PUT    /api/tags/{id}         - 完整更新标签
 * DELETE /api/tags/{id}         - 删除标签
 */
fun Route.tagsApi() {
    authenticate("auth-bearer") {
        route("/api/tags") {
            get {
                val tagService = call.application.get<TagService>()
                try {
                    val userId = call.getCurrentUserId()
                    val tags = tagService.getTags(userId)
                    call.respond(HttpStatusCode.OK, tags)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to get tags: ${e.message}", e) }
            }

            get("{id}") {
                val tagService = call.application.get<TagService>()
                try {
                    val id = call.parameters["id"] ?: throw ValidationException("Tag ID is required")
                    val userId = call.getCurrentUserId()
                    val tag = tagService.getTag(id, userId)
                    call.respond(HttpStatusCode.OK, tag)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: NotFoundException) { throw e }
                catch (e: ForbiddenException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to get tag: ${e.message}", e) }
            }

            post {
                val tagService = call.application.get<TagService>()
                try {
                    val userId = call.getCurrentUserId()
                    val request = call.receive<CreateTagRequest>()
                    val tag = tagService.createTag(request, userId)
                    call.respond(HttpStatusCode.Created, tag)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: IllegalArgumentException) { throw ValidationException(e.message ?: "Invalid request") }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to create tag: ${e.message}", e) }
            }

            put("{id}") {
                val tagService = call.application.get<TagService>()
                try {
                    val id = call.parameters["id"] ?: throw ValidationException("Tag ID is required")
                    val userId = call.getCurrentUserId()
                    val request = call.receive<UpdateTagRequest>()
                    val tag = tagService.updateTag(id, request, userId)
                    call.respond(HttpStatusCode.OK, tag)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: NotFoundException) { throw e }
                catch (e: ForbiddenException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: IllegalArgumentException) { throw ValidationException(e.message ?: "Invalid request") }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to update tag: ${e.message}", e) }
            }

            delete("{id}") {
                val tagService = call.application.get<TagService>()
                try {
                    val id = call.parameters["id"] ?: throw ValidationException("Tag ID is required")
                    val userId = call.getCurrentUserId()
                    tagService.deleteTag(id, userId)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: NotFoundException) { throw e }
                catch (e: ForbiddenException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to delete tag: ${e.message}", e) }
            }
        }
    }
}

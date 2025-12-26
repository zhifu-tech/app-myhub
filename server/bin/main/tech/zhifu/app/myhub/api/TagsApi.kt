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
import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.service.TagService

/**
 * 标签 API 路由
 */
fun Route.tagsApi(tagService: TagService) {
    route("/api/tags") {
        // GET /api/tags - 获取所有标签
        get {
            try {
                val tags = tagService.getAllTags()
                call.respond(HttpStatusCode.OK, tags)
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to get tags: ${e.message}", e)
            }
        }

        // GET /api/tags/{id} - 获取指定标签
        get("{id}") {
            try {
                val id = call.parameters["id"] ?: throw tech.zhifu.app.myhub.exception.ValidationException("Tag ID is required")
                val tag = tagService.getTagById(id)
                    ?: throw tech.zhifu.app.myhub.exception.NotFoundException("Tag", id)
                call.respond(HttpStatusCode.OK, tag)
            } catch (e: ApiException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to get tag: ${e.message}", e)
            }
        }

        // POST /api/tags - 创建新标签
        post {
            try {
                val request = call.receive<CreateTagRequest>()
                val tag = tagService.createTag(
                    name = request.name,
                    color = request.color,
                    description = request.description
                )
                call.respond(HttpStatusCode.Created, tag)
            } catch (e: tech.zhifu.app.myhub.exception.ConflictException) {
                throw e
            } catch (e: tech.zhifu.app.myhub.exception.ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to create tag: ${e.message}", e)
            }
        }

        // PUT /api/tags/{id} - 更新标签
        put("{id}") {
            try {
                val id = call.parameters["id"] ?: throw tech.zhifu.app.myhub.exception.ValidationException("Tag ID is required")
                val request = call.receive<UpdateTagRequest>()
                val existing = tagService.getTagById(id)
                    ?: throw tech.zhifu.app.myhub.exception.NotFoundException("Tag", id)

                val updated = existing.copy(
                    name = request.name ?: existing.name,
                    color = request.color ?: existing.color,
                    description = request.description ?: existing.description
                )

                val tag = tagService.updateTag(updated)
                call.respond(HttpStatusCode.OK, tag)
            } catch (e: tech.zhifu.app.myhub.exception.NotFoundException) {
                throw e
            } catch (e: tech.zhifu.app.myhub.exception.ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to update tag: ${e.message}", e)
            }
        }

        // DELETE /api/tags/{id} - 删除标签
        delete("{id}") {
            try {
                val id = call.parameters["id"] ?: throw tech.zhifu.app.myhub.exception.ValidationException("Tag ID is required")
                val deleted = tagService.deleteTag(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    throw tech.zhifu.app.myhub.exception.NotFoundException("Tag", id)
                }
            } catch (e: tech.zhifu.app.myhub.exception.NotFoundException) {
                throw e
            } catch (e: tech.zhifu.app.myhub.exception.ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to delete tag: ${e.message}", e)
            }
        }
    }
}

/**
 * 创建标签请求
 */
@kotlinx.serialization.Serializable
data class CreateTagRequest(
    val name: String,
    val color: String? = null,
    val description: String? = null
)

/**
 * 更新标签请求
 */
@kotlinx.serialization.Serializable
data class UpdateTagRequest(
    val name: String? = null,
    val color: String? = null,
    val description: String? = null
)


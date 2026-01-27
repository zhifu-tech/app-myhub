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
import tech.zhifu.app.myhub.auth.getCurrentUserId
import tech.zhifu.app.myhub.datastore.model.dto.CreateCollectionRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateCollectionRequest
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.exception.ForbiddenException
import tech.zhifu.app.myhub.exception.NotFoundException
import tech.zhifu.app.myhub.exception.UnauthorizedException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.service.CollectionService

/**
 * 卡集 API 路由（RESTful 设计）
 *
 * GET    /api/collections              - 获取用户的所有卡集
 * GET    /api/collections/{id}         - 获取指定卡集
 * POST   /api/collections              - 创建卡集
 * PUT    /api/collections/{id}         - 完整更新卡集
 * DELETE /api/collections/{id}         - 删除卡集
 */
fun Route.collectionsApi(collectionService: CollectionService) {
    authenticate("auth-bearer") {
        route("/api/collections") {
            // GET /api/collections - 获取用户的所有卡集
            get {
                try {
                    // 从认证中获取 userId
                    val userId = call.getCurrentUserId()

                    val collections = collectionService.getCollections(userId)
                    call.respond(HttpStatusCode.OK, collections)
                } catch (e: UnauthorizedException) {
                    throw e
                } catch (e: Exception) {
                    throw ApiException(
                        HttpStatusCode.InternalServerError,
                        "Failed to get collections: ${e.message}",
                        e
                    )
                }
            }

            // GET /api/collections/{id} - 获取指定卡集
            get("{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw ValidationException("Collection ID is required")

                    // 从认证中获取 userId
                    val userId = call.getCurrentUserId()

                    val collection = collectionService.getCollection(id, userId)
                    call.respond(HttpStatusCode.OK, collection)
                } catch (e: UnauthorizedException) {
                    throw e
                } catch (e: NotFoundException) {
                    throw e
                } catch (e: ForbiddenException) {
                    throw e
                } catch (e: ValidationException) {
                    throw e
                } catch (e: Exception) {
                    throw ApiException(
                        HttpStatusCode.InternalServerError,
                        "Failed to get collection: ${e.message}",
                        e
                    )
                }
            }

            // POST /api/collections - 创建卡集
            post {
                try {
                    // 从认证中获取 userId
                    val userId = call.getCurrentUserId()

                    val request = call.receive<CreateCollectionRequest>()
                    val collection = collectionService.createCollection(request, userId)

                    call.respond(HttpStatusCode.Created, collection)
                } catch (e: UnauthorizedException) {
                    throw e
                } catch (e: ValidationException) {
                    throw e
                } catch (e: Exception) {
                    throw ApiException(
                        HttpStatusCode.InternalServerError,
                        "Failed to create collection: ${e.message}",
                        e
                    )
                }
            }

            // PUT /api/collections/{id} - 完整更新卡集
            put("{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw ValidationException("Collection ID is required")

                    // 从认证中获取 userId
                    val userId = call.getCurrentUserId()

                    val request = call.receive<UpdateCollectionRequest>()
                    val collection = collectionService.updateCollection(id, request, userId)

                    call.respond(HttpStatusCode.OK, collection)
                } catch (e: UnauthorizedException) {
                    throw e
                } catch (e: NotFoundException) {
                    throw e
                } catch (e: ForbiddenException) {
                    throw e
                } catch (e: ValidationException) {
                    throw e
                } catch (e: Exception) {
                    throw ApiException(
                        HttpStatusCode.InternalServerError,
                        "Failed to update collection: ${e.message}",
                        e
                    )
                }
            }

            // DELETE /api/collections/{id} - 删除卡集
            delete("{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw ValidationException("Collection ID is required")

                    // 从认证中获取 userId
                    val userId = call.getCurrentUserId()

                    collectionService.deleteCollection(id, userId)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: UnauthorizedException) {
                    throw e
                } catch (e: NotFoundException) {
                    throw e
                } catch (e: ForbiddenException) {
                    throw e
                } catch (e: ValidationException) {
                    throw e
                } catch (e: Exception) {
                    throw ApiException(
                        HttpStatusCode.InternalServerError,
                        "Failed to delete collection: ${e.message}",
                        e
                    )
                }
            }
        }
    }
}

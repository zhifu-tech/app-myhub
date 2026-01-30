package tech.zhifu.app.myhub.api.card

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.get
import tech.zhifu.app.myhub.auth.getCurrentUserId
import tech.zhifu.app.myhub.datastore.model.dto.CreateCardRequest
import tech.zhifu.app.myhub.datastore.model.dto.PartialUpdateCardRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateCardRequest
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.exception.ForbiddenException
import tech.zhifu.app.myhub.exception.NotFoundException
import tech.zhifu.app.myhub.exception.UnauthorizedException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.service.CardService

/**
 * 卡片 API 路由（领域：card）
 *
 * GET    /api/cards              - 获取卡片列表（支持分页和筛选）
 * GET    /api/cards/{id}         - 获取指定卡片
 * POST   /api/cards              - 创建卡片
 * PUT    /api/cards/{id}         - 完整更新卡片
 * PATCH  /api/cards/{id}         - 部分更新卡片
 * DELETE /api/cards/{id}         - 删除卡片
 */
fun Route.cardsApi() {
    authenticate("auth-bearer") {
        route("/api/cards") {
            get {
                val cardService = call.application.get<CardService>()
                try {
                    val userId = call.getCurrentUserId()
                    val page = call.request.queryParameters["page"]?.toIntOrNull()
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                    val type = call.request.queryParameters["type"]
                    val isFavorite = call.request.queryParameters["isFavorite"]?.toBoolean()
                    val result = cardService.getCards(userId = userId, page = page, limit = limit, type = type, isFavorite = isFavorite)
                    call.respond(HttpStatusCode.OK, result)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to get cards: ${e.message}", e) }
            }

            get("{id}") {
                val cardService = call.application.get<CardService>()
                try {
                    val id = call.parameters["id"] ?: throw ValidationException("Card ID is required")
                    val userId = call.getCurrentUserId()
                    val card = cardService.getCard(id, userId)
                    call.respond(HttpStatusCode.OK, card)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: NotFoundException) { throw e }
                catch (e: ForbiddenException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to get card: ${e.message}", e) }
            }

            post {
                val cardService = call.application.get<CardService>()
                try {
                    val userId = call.getCurrentUserId()
                    val request = call.receive<CreateCardRequest>()
                    val card = cardService.createCard(request, userId)
                    call.respond(HttpStatusCode.Created, card)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to create card: ${e.message}", e) }
            }

            put("{id}") {
                val cardService = call.application.get<CardService>()
                try {
                    val id = call.parameters["id"] ?: throw ValidationException("Card ID is required")
                    val userId = call.getCurrentUserId()
                    val request = call.receive<UpdateCardRequest>()
                    val card = cardService.updateCard(id, request, userId)
                    call.respond(HttpStatusCode.OK, card)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: NotFoundException) { throw e }
                catch (e: ForbiddenException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to update card: ${e.message}", e) }
            }

            patch("{id}") {
                val cardService = call.application.get<CardService>()
                try {
                    val id = call.parameters["id"] ?: throw ValidationException("Card ID is required")
                    val userId = call.getCurrentUserId()
                    val request = call.receive<PartialUpdateCardRequest>()
                    val card = cardService.partialUpdateCard(id, request, userId)
                    call.respond(HttpStatusCode.OK, card)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: NotFoundException) { throw e }
                catch (e: ForbiddenException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to update card: ${e.message}", e) }
            }

            delete("{id}") {
                val cardService = call.application.get<CardService>()
                try {
                    val id = call.parameters["id"] ?: throw ValidationException("Card ID is required")
                    val userId = call.getCurrentUserId()
                    cardService.deleteCard(id, userId)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: NotFoundException) { throw e }
                catch (e: ForbiddenException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to delete card: ${e.message}", e) }
            }
        }
    }
}

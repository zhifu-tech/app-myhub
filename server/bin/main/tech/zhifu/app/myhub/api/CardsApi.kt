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
import tech.zhifu.app.myhub.datastore.model.CreateCardRequest
import tech.zhifu.app.myhub.datastore.model.UpdateCardRequest
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.service.CardService

/**
 * 卡片 API 路由
 */
fun Route.cardsApi(cardService: CardService) {
    route("/api/cards") {
        // GET /api/cards - 获取所有卡片
        get {
            try {
                val cards = cardService.getAllCards()
                call.respond(HttpStatusCode.OK, cards)
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to get cards: ${e.message}", e)
            }
        }

        // GET /api/cards/search - 搜索卡片
        get("/search") {
            try {
                val query = call.request.queryParameters["q"]
                val types = call.request.queryParameters["types"]
                val tags = call.request.queryParameters["tags"]
                val favorite = call.request.queryParameters["favorite"]?.toBoolean()
                val template = call.request.queryParameters["template"]?.toBoolean()
                val sortBy = call.request.queryParameters["sort"]

                val cards = cardService.searchCards(
                    query = query,
                    types = types,
                    tags = tags,
                    favorite = favorite,
                    template = template,
                    sortBy = sortBy
                )
                call.respond(HttpStatusCode.OK, cards)
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to search cards: ${e.message}", e)
            }
        }

        // GET /api/cards/{id} - 获取指定卡片
        get("{id}") {
            try {
                val id = call.parameters["id"] ?: throw tech.zhifu.app.myhub.exception.ValidationException("Card ID is required")
                val card = cardService.getCardById(id)
                    ?: throw tech.zhifu.app.myhub.exception.NotFoundException("Card", id)
                call.respond(HttpStatusCode.OK, card)
            } catch (e: ApiException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to get card: ${e.message}", e)
            }
        }

        // POST /api/cards - 创建新卡片
        post {
            try {
                val request = call.receive<CreateCardRequest>()
                val card = cardService.createCard(request)
                call.respond(HttpStatusCode.Created, card)
            } catch (e: tech.zhifu.app.myhub.exception.ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to create card: ${e.message}", e)
            }
        }

        // PUT /api/cards/{id} - 更新卡片
        put("{id}") {
            try {
                val id = call.parameters["id"] ?: throw tech.zhifu.app.myhub.exception.ValidationException("Card ID is required")
                val request = call.receive<UpdateCardRequest>()
                val card = cardService.updateCard(id, request)
                call.respond(HttpStatusCode.OK, card)
            } catch (e: tech.zhifu.app.myhub.exception.NotFoundException) {
                throw e
            } catch (e: tech.zhifu.app.myhub.exception.ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to update card: ${e.message}", e)
            }
        }

        // DELETE /api/cards/{id} - 删除卡片
        delete("{id}") {
            try {
                val id = call.parameters["id"] ?: throw tech.zhifu.app.myhub.exception.ValidationException("Card ID is required")
                val deleted = cardService.deleteCard(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    throw tech.zhifu.app.myhub.exception.NotFoundException("Card", id)
                }
            } catch (e: tech.zhifu.app.myhub.exception.NotFoundException) {
                throw e
            } catch (e: tech.zhifu.app.myhub.exception.ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to delete card: ${e.message}", e)
            }
        }

        // POST /api/cards/{id}/favorite - 切换收藏状态
        post("{id}/favorite") {
            try {
                val id = call.parameters["id"] ?: throw tech.zhifu.app.myhub.exception.ValidationException("Card ID is required")
                val card = cardService.toggleFavorite(id)
                call.respond(HttpStatusCode.OK, card)
            } catch (e: tech.zhifu.app.myhub.exception.NotFoundException) {
                throw e
            } catch (e: tech.zhifu.app.myhub.exception.ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to toggle favorite: ${e.message}", e)
            }
        }
    }
}


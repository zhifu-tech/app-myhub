package tech.zhifu.app.myhub.api.capture

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.get
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.service.media.image.CaptureImageGenerationRequest
import tech.zhifu.app.myhub.service.media.image.CaptureImageGenerationResponse
import tech.zhifu.app.myhub.service.media.image.CaptureImageGenerationService

fun Route.captureImageGenerationApi() {
    route("/api/capture") {
        post("/image-generation") {
            val service = call.application.get<CaptureImageGenerationService>()
            try {
                val request = call.receive<CaptureImageGenerationSubmitRequest>()
                val response = service.generate(
                    CaptureImageGenerationRequest(
                        prompt = request.prompt,
                        language = request.language,
                        draftId = request.draftId,
                        size = request.size,
                        quality = request.quality,
                    )
                )
                call.respond(
                    HttpStatusCode.OK,
                    CaptureImageGenerationResponsePayload(
                        imageBase64 = response.imageBase64,
                        mimeType = response.mimeType,
                        revisedPrompt = response.revisedPrompt,
                    )
                )
            } catch (e: IllegalArgumentException) {
                throw ValidationException(e.message ?: "Invalid image generation request")
            } catch (e: ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(
                    HttpStatusCode.InternalServerError,
                    "Failed to generate capture image: ${e.message}",
                    e,
                )
            }
        }
    }
}

@Serializable
data class CaptureImageGenerationSubmitRequest(
    val prompt: String,
    val language: String,
    val draftId: String,
    val size: String = "1536x1024",
    val quality: String = "medium",
)

@Serializable
data class CaptureImageGenerationResponsePayload(
    val imageBase64: String,
    val mimeType: String = "image/png",
    val revisedPrompt: String? = null,
)

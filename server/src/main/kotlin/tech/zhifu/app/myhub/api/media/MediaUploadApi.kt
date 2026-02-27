package tech.zhifu.app.myhub.api.media

import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpHeaders
import io.ktor.server.request.receive
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respond
import io.ktor.server.response.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.utils.io.readAvailable
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.get
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.service.media.MediaUploadService
import java.time.format.DateTimeFormatter

fun Route.mediaUploadApi() {
    route("/api/media") {
        post("/upload-sessions") {
            val service = call.application.get<MediaUploadService>()
            try {
                val request = call.receive<CreateUploadSessionRequest>()
                val session = service.createUploadSession(
                    fileName = request.fileName,
                    mimeType = request.mimeType,
                    fileSize = request.fileSize,
                    source = request.source
                )
                call.respond(
                    HttpStatusCode.Created,
                    CreateUploadSessionResponse(
                        sessionId = session.sessionId,
                        mediaId = session.mediaId,
                        uploadMethod = session.uploadMethod,
                        uploadUrl = session.uploadUrl,
                        uploadHeaders = mapOf("Content-Type" to session.mimeType),
                        expiresAt = DateTimeFormatter.ISO_INSTANT.format(session.expiresAt)
                    )
                )
            } catch (e: IllegalArgumentException) {
                throw ValidationException(e.message ?: "Invalid upload session request")
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to create upload session: ${e.message}", e)
            }
        }

        put("/upload-sessions/{sessionId}/binary") {
            val service = call.application.get<MediaUploadService>()
            try {
                val sessionId = call.parameters["sessionId"] ?: throw ValidationException("sessionId is required")
                val channel = call.receiveChannel()
                val bytes = readAllBytes(channel)
                val contentType = call.request.headers[HttpHeaders.ContentType]
                val uploaded = service.uploadBinary(
                    sessionId = sessionId,
                    bytes = bytes,
                    contentType = contentType
                )
                call.response.header(HttpHeaders.ETag, "\"${uploaded.etag}\"")
                call.respond(
                    HttpStatusCode.OK,
                    UploadBinaryResponse(
                        etag = uploaded.etag,
                        checksumSha256 = uploaded.checksumSha256,
                        size = uploaded.size
                    )
                )
            } catch (e: ValidationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                throw ValidationException(e.message ?: "Invalid upload binary request")
            } catch (e: IllegalStateException) {
                throw ValidationException(e.message ?: "Upload binary state invalid")
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to upload media binary: ${e.message}", e)
            }
        }

        post("/upload-sessions/{sessionId}/complete") {
            val service = call.application.get<MediaUploadService>()
            try {
                val sessionId = call.parameters["sessionId"] ?: throw ValidationException("sessionId is required")
                val request = call.receive<CompleteUploadRequest>()
                val media = service.completeUpload(
                    sessionId = sessionId,
                    etag = request.etag,
                    checksumSha256 = request.checksumSha256
                )
                call.respond(
                    HttpStatusCode.OK,
                    CompleteUploadResponse(
                        mediaId = media.mediaId,
                        status = media.status.name.lowercase(),
                        remoteUri = media.remoteUri,
                        publicPreviewUrl = media.remoteUri
                    )
                )
            } catch (e: ValidationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                throw ValidationException(e.message ?: "Invalid complete upload request")
            } catch (e: IllegalStateException) {
                throw ValidationException(e.message ?: "Invalid upload session state")
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to complete upload session: ${e.message}", e)
            }
        }

        get("/{mediaId}") {
            val service = call.application.get<MediaUploadService>()
            try {
                val mediaId = call.parameters["mediaId"] ?: throw ValidationException("mediaId is required")
                val media = service.getMedia(mediaId) ?: throw ValidationException("media not found")
                call.respond(
                    HttpStatusCode.OK,
                    MediaStatusResponse(
                        mediaId = media.mediaId,
                        status = media.status.name.lowercase(),
                        mimeType = media.mimeType,
                        size = media.fileSize,
                        remoteUri = media.remoteUri
                    )
                )
            } catch (e: ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to get media status: ${e.message}", e)
            }
        }
    }
}

@Serializable
data class CreateUploadSessionRequest(
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val source: String
)

@Serializable
data class CreateUploadSessionResponse(
    val sessionId: String,
    val mediaId: String,
    val uploadMethod: String,
    val uploadUrl: String,
    val uploadHeaders: Map<String, String> = emptyMap(),
    val expiresAt: String
)

@Serializable
data class CompleteUploadRequest(
    val etag: String,
    val checksumSha256: String? = null
)

@Serializable
data class UploadBinaryResponse(
    val etag: String,
    val checksumSha256: String,
    val size: Long
)

@Serializable
data class CompleteUploadResponse(
    val mediaId: String,
    val status: String,
    val remoteUri: String,
    val publicPreviewUrl: String
)

@Serializable
data class MediaStatusResponse(
    val mediaId: String,
    val status: String,
    val mimeType: String,
    val size: Long,
    val remoteUri: String
)

private suspend fun readAllBytes(channel: io.ktor.utils.io.ByteReadChannel): ByteArray {
    val buffer = ByteArray(8192)
    val output = java.io.ByteArrayOutputStream()
    while (true) {
        val read = channel.readAvailable(buffer, 0, buffer.size)
        if (read <= 0) break
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}

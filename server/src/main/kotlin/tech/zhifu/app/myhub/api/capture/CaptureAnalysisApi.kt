package tech.zhifu.app.myhub.api.capture

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.get
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.service.media.CaptureAnalysisMediaRef
import tech.zhifu.app.myhub.service.media.CaptureAnalysisRequest
import tech.zhifu.app.myhub.service.media.CaptureAnalysisService

fun Route.captureAnalysisApi() {
    route("/api/capture") {
        post("/analysis") {
            val service = call.application.get<CaptureAnalysisService>()
            try {
                val request = call.receive<CaptureAnalysisSubmitRequest>()
                val accepted = service.submit(
                    request = CaptureAnalysisRequest(
                        inputText = request.inputText,
                        intent = request.intent,
                        sourceForm = request.sourceForm,
                        media = request.media.map {
                            CaptureAnalysisMediaRef(
                                mediaId = it.mediaId,
                                type = it.type,
                                remoteUri = it.remoteUri,
                                mimeType = it.mimeType
                            )
                        }
                    )
                )
                call.respond(
                    HttpStatusCode.Accepted,
                    CaptureAnalysisAcceptedResponse(
                        jobId = accepted.jobId,
                        status = accepted.status.name.lowercase(),
                        retryAfterMs = accepted.retryAfterMs
                    )
                )
            } catch (e: IllegalArgumentException) {
                throw ValidationException(e.message ?: "Invalid analysis request")
            } catch (e: Exception) {
                throw ApiException(
                    HttpStatusCode.InternalServerError,
                    "Failed to submit capture analysis: ${e.message}",
                    e
                )
            }
        }

        get("/analysis/{jobId}") {
            val service = call.application.get<CaptureAnalysisService>()
            try {
                val jobId = call.parameters["jobId"] ?: throw ValidationException("jobId is required")
                val job = service.getJob(jobId)
                call.respond(
                    HttpStatusCode.OK,
                    CaptureAnalysisJobResponse(
                        jobId = job.jobId,
                        status = job.status.name.lowercase(),
                        progress = job.progress,
                        errorCode = job.errorCode,
                        errorMessage = job.errorMessage,
                        result = job.result?.let {
                            CaptureReviewDataResponse(
                                text = it.text,
                                title = it.title,
                                sourceForm = it.sourceForm,
                                styleOptions = it.styleOptions.map { style ->
                                    StyleOptionResponse(
                                        label = style.label,
                                        colorHex = style.colorHex
                                    )
                                },
                                selectedStyleIndex = it.selectedStyleIndex,
                                tags = it.tags,
                                tagQuery = it.tagQuery,
                                code = it.code,
                                codeLanguage = it.codeLanguage,
                                imageOcrSummary = it.imageOcrSummary,
                                imageOcrInfo = it.imageOcrInfo,
                                videoMetadataSummary = it.videoMetadataSummary,
                                videoMetadataInfo = it.videoMetadataInfo,
                                primaryContentType = it.primaryContentType
                            )
                        }
                    )
                )
            } catch (e: ValidationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                throw ValidationException(e.message ?: "Invalid analysis job id")
            } catch (e: Exception) {
                throw ApiException(
                    HttpStatusCode.InternalServerError,
                    "Failed to get capture analysis job: ${e.message}",
                    e
                )
            }
        }
    }
}

@Serializable
data class CaptureAnalysisSubmitRequest(
    val inputText: String,
    val intent: String,
    val sourceForm: String,
    val media: List<CaptureAnalysisMediaRefRequest>
)

@Serializable
data class CaptureAnalysisMediaRefRequest(
    val mediaId: String,
    val type: String,
    val remoteUri: String,
    val mimeType: String
)

@Serializable
data class CaptureAnalysisAcceptedResponse(
    val jobId: String,
    val status: String,
    val retryAfterMs: Int
)

@Serializable
data class CaptureAnalysisJobResponse(
    val jobId: String,
    val status: String,
    val progress: Int,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val result: CaptureReviewDataResponse? = null
)

@Serializable
data class CaptureReviewDataResponse(
    val text: String,
    val title: String,
    val sourceForm: String,
    val styleOptions: List<StyleOptionResponse>,
    val selectedStyleIndex: Int,
    val tags: List<String>,
    val tagQuery: String,
    val code: String? = null,
    val codeLanguage: String? = null,
    val imageOcrSummary: String? = null,
    val imageOcrInfo: String? = null,
    val videoMetadataSummary: String? = null,
    val videoMetadataInfo: String? = null,
    val primaryContentType: String
)

@Serializable
data class StyleOptionResponse(
    val label: String,
    val colorHex: String
)

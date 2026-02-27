package tech.zhifu.app.myhub.datastore.repository.capture

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.network.createHttpClient

class HttpCaptureRepository(
    private val httpClient: HttpClient = createHttpClient()
) : CaptureRepository {

    override suspend fun createUploadSession(request: CreateUploadSessionRequest): MediaUploadSessionResult {
        return try {
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}/api/media/upload-sessions") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.Created -> {
                    val body: CreateUploadSessionResponse = response.body()
                    MediaUploadSessionResult(
                        sessionId = body.sessionId,
                        mediaId = body.mediaId,
                        uploadUrl = body.uploadUrl,
                        uploadMethod = body.uploadMethod
                    )
                }

                else -> throw IllegalStateException("Failed to create upload session: ${response.status}")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Network error while creating upload session", e)
        }
    }

    override suspend fun uploadBinary(uploadUrl: String, mimeType: String, bytes: ByteArray): UploadBinaryResult {
        return try {
            val resolvedUploadUrl = if (uploadUrl.startsWith("http://") || uploadUrl.startsWith("https://")) {
                uploadUrl
            } else {
                "${ApiConfig.BASE_URL}${if (uploadUrl.startsWith("/")) uploadUrl else "/$uploadUrl"}"
            }
            val response: HttpResponse = httpClient.put(resolvedUploadUrl) {
                contentType(ContentType.parse(mimeType))
                setBody(bytes)
            }
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created, HttpStatusCode.NoContent -> {
                    val etag = response.headers[HttpHeaders.ETag]
                        ?.trim()
                        ?.removePrefix("\"")
                        ?.removeSuffix("\"")
                    val body = runCatching { response.body<UploadBinaryResponse>() }.getOrNull()
                    val resolvedEtag = body?.etag ?: etag
                    if (resolvedEtag.isNullOrBlank()) {
                        throw IllegalStateException("Upload response missing etag")
                    }
                    UploadBinaryResult(
                        etag = resolvedEtag,
                        checksumSha256 = body?.checksumSha256
                    )
                }

                else -> throw IllegalStateException("Failed to upload binary: ${response.status}")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Network error while uploading binary", e)
        }
    }

    override suspend fun completeUpload(sessionId: String, etag: String, checksum: String?): MediaUploadCompleteResult {
        return try {
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}/api/media/upload-sessions/$sessionId/complete") {
                contentType(ContentType.Application.Json)
                setBody(
                    CompleteUploadRequest(
                        etag = etag,
                        checksumSha256 = checksum
                    )
                )
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val body: CompleteUploadResponse = response.body()
                    MediaUploadCompleteResult(
                        mediaId = body.mediaId,
                        remoteUri = body.remoteUri,
                        status = body.status
                    )
                }

                else -> throw IllegalStateException("Failed to complete upload: ${response.status}")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Network error while completing upload", e)
        }
    }

    override suspend fun submitAnalysis(request: CaptureAnalysisSubmitRequest): CaptureAnalysisSubmitResult {
        return try {
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}/api/capture/analysis") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.Accepted -> {
                    val body: CaptureAnalysisAcceptedResponse = response.body()
                    CaptureAnalysisSubmitResult(
                        jobId = body.jobId,
                        status = body.status.toAnalysisStatus(),
                        retryAfterMs = body.retryAfterMs,
                        result = null
                    )
                }

                HttpStatusCode.OK -> {
                    val body: CaptureReviewDataPayload = response.body()
                    CaptureAnalysisSubmitResult(
                        jobId = null,
                        status = AnalysisStatus.Succeeded,
                        retryAfterMs = null,
                        result = body
                    )
                }

                else -> throw IllegalStateException("Failed to submit analysis: ${response.status}")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Network error while submitting analysis", e)
        }
    }

    override suspend fun queryAnalysis(jobId: String): CaptureAnalysisJobResult {
        return try {
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}/api/capture/analysis/$jobId")
            when (response.status) {
                HttpStatusCode.OK -> {
                    val body: CaptureAnalysisJobResponse = response.body()
                    CaptureAnalysisJobResult(
                        jobId = body.jobId,
                        status = body.status.toAnalysisStatus(),
                        progress = body.progress,
                        errorCode = body.errorCode,
                        errorMessage = body.errorMessage,
                        result = body.result
                    )
                }

                else -> throw IllegalStateException("Failed to query analysis job: ${response.status}")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Network error while querying analysis job", e)
        }
    }
}

@Serializable
private data class CreateUploadSessionResponse(
    val sessionId: String,
    val mediaId: String,
    val uploadMethod: String,
    val uploadUrl: String
)

@Serializable
private data class CompleteUploadRequest(
    val etag: String,
    val checksumSha256: String? = null
)

@Serializable
private data class CompleteUploadResponse(
    val mediaId: String,
    val status: String,
    val remoteUri: String
)

@Serializable
private data class UploadBinaryResponse(
    val etag: String,
    val checksumSha256: String? = null
)

@Serializable
private data class CaptureAnalysisAcceptedResponse(
    val jobId: String,
    val status: String,
    val retryAfterMs: Int
)

@Serializable
private data class CaptureAnalysisJobResponse(
    val jobId: String,
    val status: String,
    val progress: Int,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val result: CaptureReviewDataPayload? = null
)

private fun String.toAnalysisStatus(): AnalysisStatus {
    return when (lowercase()) {
        "queued" -> AnalysisStatus.Queued
        "running" -> AnalysisStatus.Running
        "succeeded" -> AnalysisStatus.Succeeded
        "failed" -> AnalysisStatus.Failed
        else -> AnalysisStatus.Failed
    }
}

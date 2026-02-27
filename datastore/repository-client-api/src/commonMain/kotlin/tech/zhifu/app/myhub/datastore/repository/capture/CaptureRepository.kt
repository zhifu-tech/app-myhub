package tech.zhifu.app.myhub.datastore.repository.capture

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface CaptureRepository {
    suspend fun createUploadSession(request: CreateUploadSessionRequest): MediaUploadSessionResult
    suspend fun uploadBinary(uploadUrl: String, mimeType: String, bytes: ByteArray): UploadBinaryResult
    suspend fun completeUpload(sessionId: String, etag: String, checksum: String? = null): MediaUploadCompleteResult
    suspend fun submitAnalysis(request: CaptureAnalysisSubmitRequest): CaptureAnalysisSubmitResult
    suspend fun queryAnalysis(jobId: String): CaptureAnalysisJobResult
}

enum class AnalysisStatus {
    Queued,
    Running,
    Succeeded,
    Failed
}

@Serializable
data class CreateUploadSessionRequest(
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val source: String
)

data class MediaUploadSessionResult(
    val sessionId: String,
    val mediaId: String,
    val uploadUrl: String,
    val uploadMethod: String
)

data class MediaUploadCompleteResult(
    val mediaId: String,
    val remoteUri: String,
    val status: String
)

data class UploadBinaryResult(
    val etag: String,
    val checksumSha256: String?
)

data class CaptureAnalysisSubmitResult(
    val jobId: String?,
    val status: AnalysisStatus,
    val retryAfterMs: Int?,
    val result: CaptureReviewDataPayload?
)

data class CaptureAnalysisJobResult(
    val jobId: String,
    val status: AnalysisStatus,
    val progress: Int?,
    val errorCode: String?,
    val errorMessage: String?,
    val result: CaptureReviewDataPayload?
)

@Serializable
data class CaptureAnalysisSubmitRequest(
    val inputText: String,
    val intent: String,
    val sourceForm: String,
    val media: List<CaptureAnalysisMediaRef>
)

@Serializable
data class CaptureAnalysisMediaRef(
    val mediaId: String,
    val type: String,
    val remoteUri: String,
    val mimeType: String
)

@Serializable
data class CaptureReviewDataPayload(
    val text: String,
    val title: String,
    val sourceForm: String,
    val styleOptions: List<StyleOptionPayload>,
    val selectedStyleIndex: Int,
    val tags: List<String>,
    val tagQuery: String,
    val code: String? = null,
    val codeLanguage: String? = null,
    val imageOcrSummary: String? = null,
    val imageOcrInfo: String? = null,
    val videoMetadataSummary: String? = null,
    val videoMetadataInfo: String? = null,
    val primaryContentType: String = "Text"
)

@Serializable
data class StyleOptionPayload(
    val label: String,
    @SerialName("colorHex") val colorHex: String
)

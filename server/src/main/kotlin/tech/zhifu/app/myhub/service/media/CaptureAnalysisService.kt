package tech.zhifu.app.myhub.service.media

import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.service.media.analysis.AnalysisProvider
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class CaptureAnalysisService(
    private val mediaUploadService: MediaUploadService,
    private val analysisProvider: AnalysisProvider
) {

    private val jobs = ConcurrentHashMap<String, CaptureAnalysisJob>()
    private val executor = Executors.newFixedThreadPool(2)

    fun submit(request: CaptureAnalysisRequest): CaptureAnalysisJobAccepted {
        request.media.forEach { media ->
            require(mediaUploadService.isUploaded(media.mediaId, media.remoteUri)) {
                "media ${media.mediaId} is not uploaded"
            }
        }
        val jobId = "job_${UUID.randomUUID().toString().replace("-", "")}" 
        val now = Instant.now()
        jobs[jobId] = CaptureAnalysisJob(
            jobId = jobId,
            status = CaptureJobStatus.Queued,
            progress = 0,
            createdAt = now,
            updatedAt = now,
            request = request,
            result = null,
            errorCode = null,
            errorMessage = null
        )
        processJobAsync(jobId)
        return CaptureAnalysisJobAccepted(
            jobId = jobId,
            status = CaptureJobStatus.Queued,
            retryAfterMs = 1200
        )
    }

    fun getJob(jobId: String): CaptureAnalysisJob {
        return jobs[jobId] ?: throw IllegalArgumentException("analysis job not found")
    }

    private fun buildResult(request: CaptureAnalysisRequest): CaptureReviewDataPayload {
        val model = analysisProvider.analyze(request)
        val imageRef = request.media.firstOrNull { it.type.equals("image", ignoreCase = true) }
        val videoRef = request.media.firstOrNull { it.type.equals("video", ignoreCase = true) }
        return CaptureReviewDataPayload(
            text = model.text?.ifBlank { null } ?: request.inputText.ifBlank { "*Captured content is ready for review.*" },
            title = model.title?.ifBlank { null } ?: "Captured Insight",
            sourceForm = normalizeSourceForm(model.sourceForm ?: request.sourceForm),
            styleOptions = defaultStyleOptions(),
            selectedStyleIndex = 2,
            tags = model.tags.ifEmpty { listOf("Design", "Capture") },
            tagQuery = "",
            code = model.code,
            codeLanguage = model.codeLanguage,
            imageOcrSummary = model.imageOcrSummary ?: imageRef?.let { "Image OCR Summary" },
            imageOcrInfo = model.imageOcrInfo ?: imageRef?.let { "OCR extracted text from ${it.mediaId}" },
            videoMetadataSummary = model.videoMetadataSummary ?: videoRef?.let { "Video Metadata Summary" },
            videoMetadataInfo = model.videoMetadataInfo ?: videoRef?.let { "Video metadata extracted from ${it.mediaId}" },
            primaryContentType = normalizePrimaryType(
                primary = model.primaryContentType,
                hasImage = imageRef != null,
                hasVideo = videoRef != null
            )
        )
    }

    private fun normalizeSourceForm(sourceForm: String): String {
        return when (sourceForm.lowercase()) {
            "link" -> "link"
            "own" -> "own"
            else -> "extract"
        }
    }

    private fun normalizePrimaryType(primary: String?, hasImage: Boolean, hasVideo: Boolean): String {
        if (primary != null) {
            return when (primary.lowercase()) {
                "text" -> "Text"
                "code" -> "Code"
                "image" -> "Image"
                "video" -> "Video"
                else -> "Text"
            }
        }
        return if (hasVideo) "Video" else if (hasImage) "Image" else "Text"
    }

    private fun processJobAsync(jobId: String) {
        executor.submit {
            val existing = jobs[jobId] ?: return@submit
            jobs[jobId] = existing.copy(
                status = CaptureJobStatus.Running,
                progress = 60,
                updatedAt = Instant.now()
            )
            val running = jobs[jobId] ?: return@submit
            try {
                val result = buildResult(running.request)
                jobs[jobId] = running.copy(
                    status = CaptureJobStatus.Succeeded,
                    progress = 100,
                    updatedAt = Instant.now(),
                    result = result,
                    errorCode = null,
                    errorMessage = null
                )
            } catch (e: Exception) {
                logger.error(e) { "capture analysis provider failed: jobId=$jobId" }
                jobs[jobId] = running.copy(
                    status = CaptureJobStatus.Failed,
                    progress = 100,
                    updatedAt = Instant.now(),
                    errorCode = "provider_error",
                    errorMessage = e.message ?: "analysis provider failed"
                )
            }
        }
    }

    private fun defaultStyleOptions(): List<StyleOptionPayload> {
        return listOf(
            StyleOptionPayload("Rose", "#F2B8B5"),
            StyleOptionPayload("Amber", "#E6C975"),
            StyleOptionPayload("Violet", "#B4A3FF"),
            StyleOptionPayload("Sky", "#AECBFA"),
            StyleOptionPayload("Mint", "#6DD58C"),
            StyleOptionPayload("Lilac", "#E8DEF8"),
            StyleOptionPayload("Ice", "#C2E7FF"),
            StyleOptionPayload("Blush", "#FAD2E1"),
            StyleOptionPayload("Orchid", "#EFE5FD"),
            StyleOptionPayload("Denim", "#D4E4FF"),
            StyleOptionPayload("Sand", "#FDE7AA"),
            StyleOptionPayload("Jade", "#C6F6D5")
        )
    }
}

enum class CaptureJobStatus {
    Queued,
    Running,
    Succeeded,
    Failed
}

data class CaptureAnalysisRequest(
    val inputText: String,
    val intent: String,
    val sourceForm: String,
    val media: List<CaptureAnalysisMediaRef>
)

data class CaptureAnalysisMediaRef(
    val mediaId: String,
    val type: String,
    val remoteUri: String,
    val mimeType: String
)

data class CaptureAnalysisJobAccepted(
    val jobId: String,
    val status: CaptureJobStatus,
    val retryAfterMs: Int
)

data class CaptureAnalysisJob(
    val jobId: String,
    val status: CaptureJobStatus,
    val progress: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val request: CaptureAnalysisRequest,
    val result: CaptureReviewDataPayload?,
    val errorCode: String?,
    val errorMessage: String?
)

data class CaptureReviewDataPayload(
    val text: String,
    val title: String,
    val sourceForm: String,
    val styleOptions: List<StyleOptionPayload>,
    val selectedStyleIndex: Int,
    val tags: List<String>,
    val tagQuery: String,
    val code: String?,
    val codeLanguage: String?,
    val imageOcrSummary: String?,
    val imageOcrInfo: String?,
    val videoMetadataSummary: String?,
    val videoMetadataInfo: String?,
    val primaryContentType: String
)

data class StyleOptionPayload(
    val label: String,
    val colorHex: String
)

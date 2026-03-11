package tech.zhifu.app.myhub.feature.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.util.toPlayableUrl
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadata
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.repository.capture.AnalysisStatus
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureAnalysisMediaRef
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureAnalysisSubmitRequest
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureRepository
import tech.zhifu.app.myhub.datastore.repository.capture.CreateUploadSessionRequest
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.capture.model.toReviewCtx
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import kotlin.random.Random
import kotlin.time.Clock

class CaptureViewModel(
    private val captureRepository: CaptureRepository,
    private val cardRepository: CardRepository,
    private val userRepository: UserRepository
) : ContainerHost<CaptureUiState, CaptureSideEffect>, ViewModel() {

    private val logger = logger("Capture")

    override val container: Container<CaptureUiState, CaptureSideEffect> = container(
        initialState = CaptureUiState.Input()
    )

    val uiState: CaptureUiState
        get() = container.stateFlow.value

    @Composable
    fun <R> collectFieldAsState(selector: (CaptureUiState) -> R): State<R> {
        return container.stateFlow
            .map(selector)
            .distinctUntilChanged()
            .collectAsState(initial = selector(uiState))
    }

    private val _captureCompleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val captureCompleted: SharedFlow<Unit> = _captureCompleted.asSharedFlow()

    fun updateInputText(text: String) = intent {
        reduce {
            when (val current = state) {
                is CaptureUiState.Input -> current.copy(input = current.input.copy(text = text))
                else -> current
            }
        }
    }

    fun onInputFocusChanged(focused: Boolean) = intent {
        reduce {
            when (val current = state) {
                is CaptureUiState.Input -> current.copy(input = current.input.copy(focused = focused))
                else -> current
            }
        }
    }

    fun updateReviewText(text: String) = updateReview { it.copy(text = text) }

    fun updateReviewTitle(title: String) = updateReview { it.copy(title = title) }

    fun updateReviewSourceForm(form: CardSource) = updateReview { it.copy(source = form) }

    fun updateReviewStyle(index: Int) {
        val review = uiState.review ?: return
        if (index !in review.styleOptions.indices) return
        updateReview { it.copy(selectedStyleIndex = index) }
    }

    fun updateIntent(intent: CardType) = intent {
        reduce {
            when (val current = state) {
                is CaptureUiState.Input -> current.copy(intent = intent)
                is CaptureUiState.Processing -> current.copy(intent = intent)
                is CaptureUiState.Review -> current.copy(intent = intent)
                is CaptureUiState.Publishing -> current.copy(intent = intent)
            }
        }
    }

    fun updateReviewCodeLanguage(language: String) = updateReview { it.copy(codeLanguage = language) }

    fun updateReviewPrimaryContent(type: ReviewContentType) = updateReview { it.copy(primaryContentType = type) }

    fun updateReviewTagQuery(query: String) = updateReview { it.copy(tagQuery = query) }

    fun addReviewTag(tag: String) {
        val review = uiState.review ?: return
        val normalized = tag.trim().trimStart('#')
        if (normalized.isBlank()) return
        if (review.tags.any { it.equals(normalized, ignoreCase = true) }) return
        updateReview { it.copy(tags = it.tags + normalized, tagQuery = "") }
    }

    fun removeReviewTag(tag: String) = updateReview {
        it.copy(tags = it.tags.filterNot { name -> name.equals(tag, ignoreCase = true) })
    }

    fun addMediaItem(item: MediaItem) = addMediaItems(listOf(item))

    fun addMediaItems(items: List<MediaItem>) {
        if (items.isEmpty()) return
        val current = uiState as? CaptureUiState.Input ?: return
        val media = current.media

        val existingIds = media.items.map { it.id }.toHashSet()
        val uniqueItems = items.filterNot { existingIds.contains(it.id) }
        if (uniqueItems.isEmpty()) return

        val updatedMedia = media.copy(
            items = media.items + uniqueItems,
            uploadStates = media.uploadStates + uniqueItems.associate { item ->
                item.id to CaptureMediaUploadState(mediaItemId = item.id)
            }
        )

        intent {
            reduce {
                val state = state as? CaptureUiState.Input ?: return@reduce state
                state.copy(media = updatedMedia)
            }
        }

        uniqueItems.forEach { item ->
            viewModelScope.launch { uploadMedia(item) }
        }
    }

    fun removeMediaItem(id: String) {
        val current = uiState
        val media = when (current) {
            is CaptureUiState.Input -> current.media
            is CaptureUiState.Processing -> current.media
            else -> return
        }
        val updatedMedia = media.copy(
            items = media.items.filter { it.id != id },
            uploadStates = media.uploadStates - id
        )
        intent {
            reduce {
                when (val state = state) {
                    is CaptureUiState.Input -> state.copy(media = updatedMedia)
                    is CaptureUiState.Processing -> state.copy(media = updatedMedia)
                    else -> state
                }
            }
        }
    }

    fun startCapture() {
        runCaptureFlow()
    }

    fun retryAiCapture() {
        val current = uiState as? CaptureUiState.Input ?: return
        if (current.error !is CaptureError.AiFailed) return
        runCaptureFlow()
    }

    fun backToInputFromAiFailed() = intent {
        reduce {
            val current = state as? CaptureUiState.Input ?: return@reduce state
            current.copy(
                input = current.input.copy(focused = true),
                error = null
            )
        }
    }

    fun completeCaptureAndExit() {
        runPublishFlow()
    }

    fun retryPostPublish() {
        val current = uiState as? CaptureUiState.Review ?: return
        if (current.error !is CaptureError.PostFailed) return
        runPublishFlow()
    }

    fun backToReview() = intent {
        reduce {
            val current = state as? CaptureUiState.Review ?: return@reduce state
            current.copy(error = null)
        }
    }

    fun clearError() = intent {
        reduce {
            when (val current = state) {
                is CaptureUiState.Input -> current.copy(error = null)
                is CaptureUiState.Review -> current.copy(error = null)
                else -> current
            }
        }
    }

    private fun updateReview(transform: (ReviewCtx) -> ReviewCtx) {
        val current = uiState as? CaptureUiState.Review ?: return
        val updatedReview = transform(current.review)
        logger.debug { "update review: $updatedReview" }
        intent {
            reduce {
                val state = state as? CaptureUiState.Review ?: return@reduce state
                state.copy(review = updatedReview, error = null)
            }
        }
    }

    private fun runCaptureFlow() {
        val start = uiState as? CaptureUiState.Input ?: return

        if (!start.canCapture) return
        if (start.hasUploadingMedia) {
            intent {
                reduce {
                    val state = state as? CaptureUiState.Input ?: return@reduce state
                    state.copy(error = CaptureError.UploadFailed("Media upload in progress. Please wait before capture."))
                }
            }
            return
        }

        viewModelScope.launch {
            val processing = CaptureUiState.Processing(
                intent = start.intent,
                input = start.input.copy(focused = false),
                media = start.media,
                analysis = AnalysisCtx(status = AnalysisStatus.Queued, progress = 0)
            )
            intent {
                reduce { processing }
            }

            try {
                val submitRequest = CaptureAnalysisSubmitRequest(
                    inputText = processing.input.text,
                    intent = processing.intent.wire,
                    sourceForm = CardSource.Extract.wire,
                    media = buildAnalysisMediaRefs(processing.media)
                )
                val submit = captureRepository.submitAnalysis(submitRequest)
                val submitResult = submit.result

                if (submitResult != null) {
                    val review = hydrateReviewCtx(submitResult.toReviewCtx(), processing.media)
                    intent {
                        reduce {
                            CaptureUiState.Review(
                                intent = processing.intent,
                                review = review,
                                error = null
                            )
                        }
                    }
                    return@launch
                }

                val jobId = submit.jobId
                if (jobId.isNullOrBlank()) {
                    throw IllegalStateException("Analysis job id is missing")
                }

                val accepted = processing.copy(
                    analysis = processing.analysis.copy(
                        jobId = jobId,
                        status = submit.status,
                        progress = 5
                    )
                )
                intent {
                    reduce { accepted }
                }
                pollAnalysisResult(jobId = jobId, retryAfterMs = submit.retryAfterMs ?: 800, initialState = accepted)
            } catch (e: Exception) {
                val message = e.message ?: "Failed to run capture analysis"
                logger.debug { "startCapture failed: $message" }
                val latest = uiState as? CaptureUiState.Processing ?: processing
                intent {
                    reduce {
                        CaptureUiState.Input(
                            intent = latest.intent,
                            input = latest.input.copy(focused = true),
                            media = latest.media,
                            error = CaptureError.AiFailed(message)
                        )
                    }
                }
            }
        }
    }

    private fun runPublishFlow() {
        val reviewState = uiState as? CaptureUiState.Review ?: return
        val intent = reviewState.intent
        val review = reviewState.review

        viewModelScope.launch {
            intent {
                reduce {
                    CaptureUiState.Publishing(
                        intent = intent,
                        review = review
                    )
                }
            }

            try {
                val now = Clock.System.now()
                val user = userRepository.getUserOrNull()
                    ?: throw IllegalStateException("User not found")
                val card = Card(
                    id = generateId("card"),
                    type = intent,
                    source = review.source,
                    carriers = buildCarriers(review),
                    userId = user.id,
                    createdAt = now,
                    updatedAt = now,
                    metadata = buildCardMetadata(review),
                    tags = buildCardTags(review.tags, user.id, now)
                )
                cardRepository.insertCard(card)

                delay(900)
                _captureCompleted.tryEmit(Unit)
                intent {
                    reduce { CaptureUiState.Input() }
                }
            } catch (e: Exception) {
                val message = e.message ?: "Failed to save capture"
                intent {
                    reduce {
                        CaptureUiState.Review(
                            intent = intent,
                            review = review,
                            error = CaptureError.PostFailed(message)
                        )
                    }
                }
            }
        }
    }

    private fun buildReviewCtx(input: InputCtx, media: MediaCtx): ReviewCtx {
        val imageItem = media.items.firstOrNull { !it.isVideo }
        val videoItem = media.items.firstOrNull { it.isVideo }
        val hasCode = input.text.let {
            it.contains("```") ||
                it.contains("function") ||
                it.contains("fun ")
        }
        return ReviewCtx(
            text = input.text.ifBlank {
                "*The details are not the details. They make the design.*"
            },
            title = "Design Philosophy",
            source = CardSource.Extract,
            styleOptions = listOf(
                StyleOption(label = "Rose", color = Color(0xFFF2B8B5)),
                StyleOption(label = "Amber", color = Color(0xFFE6C975)),
                StyleOption(label = "Violet", color = Color(0xFFB4A3FF)),
                StyleOption(label = "Sky", color = Color(0xFFAECBFA)),
                StyleOption(label = "Mint", color = Color(0xFF6DD58C)),
                StyleOption(label = "Lilac", color = Color(0xFFE8DEF8)),
                StyleOption(label = "Ice", color = Color(0xFFC2E7FF)),
                StyleOption(label = "Blush", color = Color(0xFFFAD2E1)),
                StyleOption(label = "Orchid", color = Color(0xFFEFE5FD)),
                StyleOption(label = "Denim", color = Color(0xFFD4E4FF)),
                StyleOption(label = "Sand", color = Color(0xFFFDE7AA)),
                StyleOption(label = "Jade", color = Color(0xFFC6F6D5))
            ),
            selectedStyleIndex = 0,
            tags = listOf("Design", "Architecture"),
            tagQuery = "Arch",
            code = if (hasCode) {
                """
                function bubbleSort(arr) {
                    const len = arr.length;
                    for (let i = 0; i < len; i++) {
                        // Sorting logic...
                    }
                }
                """.trimIndent()
            } else {
                null
            },
            codeLanguage = if (hasCode) "JavaScript" else null,
            imageItem = imageItem,
            imageOcrSummary = imageItem?.let { "Brutalist Architecture" },
            imageOcrInfo = imageItem?.let { "A study in raw concrete and geometric forms." },
            videoItem = videoItem,
            videoMetadataSummary = videoItem?.let { "Theory Lecture" },
            videoMetadataInfo = videoItem?.let { "This a design philosophy lector by YuG" },
            primaryContentType = ReviewContentType.Text
        )
    }

    private suspend fun uploadMedia(item: MediaItem) {
        try {
            val bytes = item.file.readBytes()
            val mimeType = item.file.mimeType()?.toString() ?: if (item.isVideo) "video/mp4" else "image/jpeg"
            updateMediaUploadState(item.id) {
                it.copy(
                    state = MediaUploadLifecycle.Uploading,
                    progress = 10,
                    errorMessage = null
                )
            }
            val session = captureRepository.createUploadSession(
                CreateUploadSessionRequest(
                    fileName = item.name,
                    mimeType = mimeType,
                    fileSize = bytes.size.toLong(),
                    source = "capture"
                )
            )
            updateMediaUploadState(item.id) {
                it.copy(
                    state = MediaUploadLifecycle.Uploading,
                    progress = 65,
                    sessionId = session.sessionId,
                    mediaId = session.mediaId
                )
            }
            val uploadResult = captureRepository.uploadBinary(
                uploadUrl = session.uploadUrl,
                mimeType = mimeType,
                bytes = bytes
            )
            updateMediaUploadState(item.id) {
                it.copy(
                    state = MediaUploadLifecycle.Uploading,
                    progress = 90
                )
            }
            val complete = captureRepository.completeUpload(
                sessionId = session.sessionId,
                etag = uploadResult.etag,
                checksum = uploadResult.checksumSha256
            )
            updateMediaUploadState(item.id) {
                it.copy(
                    state = MediaUploadLifecycle.Uploaded,
                    progress = 100,
                    mediaId = complete.mediaId,
                    remoteUri = complete.remoteUri
                )
            }
        } catch (e: Exception) {
            updateMediaUploadState(item.id) {
                it.copy(
                    state = MediaUploadLifecycle.Failed,
                    errorMessage = e.message ?: "Upload failed"
                )
            }
        }
    }

    private fun updateMediaUploadState(
        mediaItemId: String,
        transform: (CaptureMediaUploadState) -> CaptureMediaUploadState
    ) {
        val current = uiState
        val media = when (current) {
            is CaptureUiState.Input -> current.media
            is CaptureUiState.Processing -> current.media
            else -> return
        }
        val old = media.uploadStates[mediaItemId] ?: CaptureMediaUploadState(mediaItemId = mediaItemId)
        val updatedMedia = media.copy(uploadStates = media.uploadStates + (mediaItemId to transform(old)))
        intent {
            reduce {
                when (val state = state) {
                    is CaptureUiState.Input -> state.copy(media = updatedMedia)
                    is CaptureUiState.Processing -> state.copy(media = updatedMedia)
                    else -> state
                }
            }
        }
    }

    private suspend fun pollAnalysisResult(
        jobId: String,
        retryAfterMs: Int,
        initialState: CaptureUiState.Processing
    ) {
        repeat(120) {
            delay(retryAfterMs.toLong().coerceAtLeast(300L))
            val job = captureRepository.queryAnalysis(jobId)
            val current = uiState as? CaptureUiState.Processing ?: return
            intent {
                reduce {
                    val state = state as? CaptureUiState.Processing ?: return@reduce state
                    state.copy(
                        analysis = state.analysis.copy(
                            status = job.status,
                            progress = job.progress
                        )
                    )
                }
            }
            when (job.status) {
                AnalysisStatus.Queued,
                AnalysisStatus.Running -> Unit

                AnalysisStatus.Succeeded -> {
                    val review = hydrateReviewCtx(
                        remote = job.result?.toReviewCtx() ?: buildReviewCtx(
                            input = initialState.input,
                            media = initialState.media
                        ),
                        media = initialState.media
                    )
                    intent {
                        reduce {
                            CaptureUiState.Review(
                                intent = current.intent,
                                review = review,
                                error = null
                            )
                        }
                    }
                    return
                }

                AnalysisStatus.Failed -> {
                    val message = job.errorMessage ?: "Capture analysis failed"
                    intent {
                        reduce {
                            CaptureUiState.Input(
                                intent = current.intent,
                                input = current.input.copy(focused = true),
                                media = current.media,
                                error = CaptureError.AiFailed(message)
                            )
                        }
                    }
                    return
                }
            }
        }

        val current = uiState as? CaptureUiState.Processing ?: return
        val timeoutMessage = "Capture analysis timeout"
        intent {
            reduce {
                CaptureUiState.Input(
                    intent = current.intent,
                    input = current.input.copy(focused = true),
                    media = current.media,
                    error = CaptureError.AiFailed(timeoutMessage)
                )
            }
        }
    }

    private fun buildAnalysisMediaRefs(media: MediaCtx): List<CaptureAnalysisMediaRef> {
        return media.items.mapNotNull { item ->
            val upload = media.uploadStates[item.id] ?: return@mapNotNull null
            if (upload.state != MediaUploadLifecycle.Uploaded) return@mapNotNull null
            val mediaId = upload.mediaId ?: return@mapNotNull null
            val remoteUri = upload.remoteUri ?: return@mapNotNull null
            CaptureAnalysisMediaRef(
                mediaId = mediaId,
                type = if (item.isVideo) "video" else "image",
                remoteUri = remoteUri,
                mimeType = if (item.isVideo) "video/mp4" else "image/jpeg"
            )
        }
    }

    private fun hydrateReviewCtx(remote: ReviewCtx, media: MediaCtx): ReviewCtx {
        val imageItem = media.items.firstOrNull { !it.isVideo }
        val videoItem = media.items.firstOrNull { it.isVideo }
        return remote.copy(
            imageItem = imageItem,
            videoItem = videoItem
        )
    }

    private fun buildCarriers(review: ReviewCtx): String {
        val carriers = mutableListOf("text")
        if (!review.code.isNullOrBlank()) carriers += "code"
        if (review.imageItem != null) carriers += "image"
        if (review.videoItem != null) carriers += "video"
        return carriers.distinct().joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
    }

    private fun buildCardMetadata(review: ReviewCtx): List<CardMetadata> {
        val metadata = mutableListOf<CardMetadata>()
        val selectedStyle = review.styleOptions.getOrNull(review.selectedStyleIndex)
        metadata += CardMetadata.Attribution(
            styleKey = selectedStyle?.label,
            styleColor = selectedStyle?.color?.toHexString()
        )
        metadata += CardMetadata.Content(
            title = review.title,
            summary = review.text.take(200),
            content = review.text
        )
        if (!review.code.isNullOrBlank()) {
            metadata += CardMetadata.Code(
                language = review.codeLanguage,
                snippet = review.code
            )
        }
        if (review.imageItem != null) {
            metadata += CardMetadata.CarrierImage(
                url = review.imageItem.file.toPlayableUrl(),
                thumbnailUrl = review.imageItem.file.toPlayableUrl()
            )
        }
        if (review.videoItem != null) {
            metadata += CardMetadata.CarrierVideo(
                videoUrl = review.videoItem.file.toPlayableUrl(),
                durationSeconds = null,
                platform = "local",
                coverImageUrl = null
            )
        }
        return metadata
    }

    private fun Color.toHexString(): String {
        val argb = toArgb()
        val rgb = argb and 0x00FFFFFF
        return "#${rgb.toString(16).padStart(6, '0').uppercase()}"
    }

    private fun buildCardTags(names: List<String>, userId: String, now: kotlin.time.Instant): List<Tag> {
        return names.distinctBy { it.lowercase() }
            .filter { it.isNotBlank() }
            .map { name ->
                Tag(
                    id = generateId("tag"),
                    name = name,
                    color = null,
                    description = null,
                    userId = userId,
                    createdAt = now,
                    updatedAt = now,
                    cardCount = 0
                )
            }
    }

    private fun generateId(prefix: String): String {
        val bytes = ByteArray(12)
        Random.nextBytes(bytes)
        val hex = bytes.joinToString(separator = "") { b -> b.toUByte().toString(16).padStart(2, '0') }
        return "${prefix}_$hex"
    }
}

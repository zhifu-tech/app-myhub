package tech.zhifu.app.myhub.feature.capture

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState.readyIdle())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()
    private val _captureCompleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val captureCompleted: SharedFlow<Unit> = _captureCompleted.asSharedFlow()

    fun updateInputText(text: String) {
        val current = _uiState.value
        _uiState.value = when (current.state) {
            CaptureState.ReadyIdle,
            CaptureState.ReadyFocused -> current.copy(input = current.input.copy(text = text))

            CaptureState.AiFailed -> CaptureUiState.readyFocused(
                intent = current.intent,
                input = current.input.copy(text = text, focused = true),
                media = current.media,
                error = null
            )

            else -> current
        }
    }

    fun onInputFocusChanged(focused: Boolean) {
        val current = _uiState.value
        _uiState.value = when (current.state) {
            CaptureState.ReadyIdle -> if (focused) {
                CaptureUiState.readyFocused(
                    intent = current.intent,
                    input = current.input.copy(focused = true),
                    media = current.media,
                    error = current.error
                )
            } else {
                current
            }

            CaptureState.ReadyFocused -> if (!focused) {
                CaptureUiState.readyIdle(
                    intent = current.intent,
                    input = current.input.copy(focused = false),
                    media = current.media,
                    error = current.error
                )
            } else {
                current
            }

            CaptureState.AiFailed -> if (focused) {
                CaptureUiState.readyFocused(
                    intent = current.intent,
                    input = current.input.copy(focused = true),
                    media = current.media,
                    error = null
                )
            } else {
                current
            }

            else -> current
        }
    }

    fun updateReviewText(text: String) = updateReview { it.copy(text = text) }

    fun updateReviewTitle(title: String) = updateReview { it.copy(title = title) }

    fun updateReviewSourceForm(form: CardSource) = updateReview { it.copy(source = form) }

    fun updateReviewStyle(index: Int) {
        val review = _uiState.value.review ?: return
        if (index !in review.styleOptions.indices) return
        updateReview { it.copy(selectedStyleIndex = index) }
    }

    fun updateIntent(intent: CardType) {
        _uiState.value = _uiState.value.copy(intent = intent)
    }

    fun updateReviewCodeLanguage(language: String) = updateReview { it.copy(codeLanguage = language) }

    fun updateReviewPrimaryContent(type: ReviewContentType) = updateReview { it.copy(primaryContentType = type) }

    fun updateReviewTagQuery(query: String) = updateReview { it.copy(tagQuery = query) }

    fun addReviewTag(tag: String) {
        val review = _uiState.value.review ?: return
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
        val current = _uiState.value
        if (
            current.state != CaptureState.ReadyIdle &&
            current.state != CaptureState.ReadyFocused &&
            current.state != CaptureState.AiFailed
        ) return
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

        _uiState.value = if (current.state == CaptureState.AiFailed) {
            CaptureUiState.readyFocused(
                intent = current.intent,
                input = current.input.copy(focused = true),
                media = updatedMedia,
                error = null
            )
        } else {
            current.copy(media = updatedMedia)
        }

        uniqueItems.forEach { item ->
            viewModelScope.launch { uploadMedia(item) }
        }
    }

    fun removeMediaItem(id: String) {
        val current = _uiState.value
        if (
            current.state != CaptureState.ReadyIdle &&
            current.state != CaptureState.ReadyFocused &&
            current.state != CaptureState.AiProcessing &&
            current.state != CaptureState.AiFailed
        ) return
        val updatedMedia = current.media.copy(
            items = current.media.items.filter { it.id != id },
            uploadStates = current.media.uploadStates - id
        )
        _uiState.value = current.copy(media = updatedMedia)
    }

    fun startCapture() {
        runCaptureFlow()
    }

    fun retryAiCapture() {
        if (_uiState.value.state != CaptureState.AiFailed) return
        runCaptureFlow()
    }

    fun backToInputFromAiFailed() {
        val current = _uiState.value.takeIf { it.state == CaptureState.AiFailed } ?: return
        _uiState.value = CaptureUiState.readyFocused(
            intent = current.intent,
            input = current.input.copy(focused = true),
            media = current.media,
            error = null
        )
    }

    fun completeCaptureAndExit() {
        runPublishFlow()
    }

    fun retryPostPublish() {
        if (_uiState.value.state != CaptureState.PostFailed) return
        runPublishFlow()
    }

    fun backToReview() {
        val current = _uiState.value.takeIf { it.state == CaptureState.PostFailed } ?: return
        val review = current.review ?: return
        _uiState.value = CaptureUiState.reviewEditing(
            intent = current.intent,
            review = review,
            error = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun updateReview(transform: (ReviewCtx) -> ReviewCtx) {
        val current = _uiState.value
        if (current.state != CaptureState.ReviewEditing) return
        val review = current.review ?: return
        val updatedReview = transform(review)
        logger.debug { "update review: $updatedReview" }
        _uiState.value = current.copy(review = updatedReview)
    }

    private fun runCaptureFlow() {
        val current = _uiState.value
        val start = when (current.state) {
            CaptureState.ReadyIdle,
            CaptureState.ReadyFocused -> current

            CaptureState.AiFailed -> CaptureUiState.readyFocused(
                intent = current.intent,
                input = current.input.copy(focused = true),
                media = current.media,
                error = null
            )

            else -> return
        }

        if (!start.canCapture) return
        if (start.hasUploadingMedia) {
            _uiState.value = start.copy(
                error = CaptureError.UploadFailed("Media upload in progress. Please wait before capture.")
            )
            return
        }

        viewModelScope.launch {
            val processing = CaptureUiState.aiProcessing(
                intent = start.intent,
                input = start.input.copy(focused = false),
                media = start.media,
                error = null
            )
            _uiState.value = processing

            try {
                val submitRequest = CaptureAnalysisSubmitRequest(
                    inputText = processing.input.text,
                    intent = processing.intent.wire,
                    sourceForm = CardSource.Extract.wire,
                    media = buildAnalysisMediaRefs(processing)
                )
                val submit = captureRepository.submitAnalysis(submitRequest)
                val submitResult = submit.result

                if (submitResult != null) {
                    val review = hydrateReviewCtx(submitResult.toReviewCtx(), processing)
                    _uiState.value = CaptureUiState.reviewEditing(
                        intent = processing.intent,
                        review = review,
                        error = null
                    )
                    return@launch
                }

                val jobId = submit.jobId
                if (jobId.isNullOrBlank()) {
                    throw IllegalStateException("Analysis job id is missing")
                }

                val accepted = processing.copy(
                    analysis = (processing.analysis ?: AnalysisCtx()).copy(
                        jobId = jobId,
                        status = submit.status,
                        progress = 5
                    )
                )
                _uiState.value = accepted
                pollAnalysisResult(jobId = jobId, retryAfterMs = submit.retryAfterMs ?: 800, initialState = accepted)
            } catch (e: Exception) {
                val message = e.message ?: "Failed to run capture analysis"
                logger.debug { "startCapture failed: $message" }
                val latest = _uiState.value.takeIf { it.state == CaptureState.AiProcessing } ?: processing
                _uiState.value = CaptureUiState.aiFailed(
                    intent = latest.intent,
                    input = latest.input,
                    media = latest.media,
                    message = message
                )
            }
        }
    }

    private fun runPublishFlow() {
        val current = _uiState.value
        if (current.state != CaptureState.ReviewEditing && current.state != CaptureState.PostFailed) return
        val review = current.review ?: return
        val intent = current.intent

        viewModelScope.launch {
            _uiState.value = CaptureUiState.postProcessing(
                intent = intent,
                review = review,
                error = null
            )

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

                _uiState.value = CaptureUiState.postSuccess(
                    intent = intent,
                    review = review,
                    error = null
                )
                delay(900)
                _captureCompleted.tryEmit(Unit)
            } catch (e: Exception) {
                val message = e.message ?: "Failed to save capture"
                _uiState.value = CaptureUiState.postFailed(
                    intent = intent,
                    review = review,
                    message = message
                )
            }
        }
    }

    private fun buildReviewCtx(state: CaptureUiState): ReviewCtx {
        val imageItem = state.media.items.firstOrNull { !it.isVideo }
        val videoItem = state.media.items.firstOrNull { it.isVideo }
        val hasCode = state.input.text.let {
            it.contains("```") ||
                it.contains("function") ||
                it.contains("fun ")
        }
        return ReviewCtx(
            text = state.input.text.ifBlank {
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
        val current = _uiState.value
        if (
            current.state != CaptureState.ReadyIdle &&
            current.state != CaptureState.ReadyFocused &&
            current.state != CaptureState.AiProcessing &&
            current.state != CaptureState.AiFailed
        ) return
        val media = current.media
        val old = media.uploadStates[mediaItemId] ?: CaptureMediaUploadState(mediaItemId = mediaItemId)
        val updatedMedia = media.copy(uploadStates = media.uploadStates + (mediaItemId to transform(old)))
        _uiState.value = current.copy(media = updatedMedia)
    }

    private suspend fun pollAnalysisResult(
        jobId: String,
        retryAfterMs: Int,
        initialState: CaptureUiState
    ) {
        repeat(120) {
            delay(retryAfterMs.toLong().coerceAtLeast(300L))
            val job = captureRepository.queryAnalysis(jobId)
            val current = _uiState.value.takeIf { it.state == CaptureState.AiProcessing } ?: return
            _uiState.value = current.copy(
                analysis = (current.analysis ?: AnalysisCtx()).copy(
                    status = job.status,
                    progress = job.progress
                )
            )
            when (job.status) {
                AnalysisStatus.Queued,
                AnalysisStatus.Running -> Unit

                AnalysisStatus.Succeeded -> {
                    val review = hydrateReviewCtx(
                        remote = job.result?.toReviewCtx() ?: buildReviewCtx(initialState),
                        state = initialState
                    )
                    _uiState.value = CaptureUiState.reviewEditing(
                        intent = current.intent,
                        review = review,
                        error = null
                    )
                    return
                }

                AnalysisStatus.Failed -> {
                    val message = job.errorMessage ?: "Capture analysis failed"
                    _uiState.value = CaptureUiState.aiFailed(
                        intent = current.intent,
                        input = current.input,
                        media = current.media,
                        message = message
                    )
                    return
                }
            }
        }

        val current = _uiState.value.takeIf { it.state == CaptureState.AiProcessing } ?: return
        val timeoutMessage = "Capture analysis timeout"
        _uiState.value = CaptureUiState.aiFailed(
            intent = current.intent,
            input = current.input,
            media = current.media,
            message = timeoutMessage
        )
    }

    private fun buildAnalysisMediaRefs(state: CaptureUiState): List<CaptureAnalysisMediaRef> {
        return state.media.items.mapNotNull { item ->
            val upload = state.media.uploadStates[item.id] ?: return@mapNotNull null
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

    private fun hydrateReviewCtx(remote: ReviewCtx, state: CaptureUiState): ReviewCtx {
        val imageItem = state.media.items.firstOrNull { !it.isVideo }
        val videoItem = state.media.items.firstOrNull { it.isVideo }
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

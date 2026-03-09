package tech.zhifu.app.myhub.feature.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.MediaPicker
import tech.zhifu.app.myhub.component.media.MediaPreviewer
import tech.zhifu.app.myhub.component.media.component.MediaPreviewDialog
import tech.zhifu.app.myhub.component.media.util.toMediaItem
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.feature.capture.app.CaptureTopAppBar
import tech.zhifu.app.myhub.feature.capture.input.InputSection
import tech.zhifu.app.myhub.feature.capture.preview.PreviewPanel
import tech.zhifu.app.myhub.feature.capture.review.ReviewSection
import tech.zhifu.app.myhub.ui.LocalWindowSizeClass
import tech.zhifu.app.myhub.ui.isWidthAtLeastExpanded

@Composable
fun CaptureScreen(
    modifier: Modifier = Modifier,
    viewModel: CaptureViewModel = koinViewModel(),
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val windowSizeClass = LocalWindowSizeClass.current
    val usePermanentDrawer = windowSizeClass.isWidthAtLeastExpanded()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val reviewRichTextState = rememberRichTextState()
    val uiScope = rememberCoroutineScope()
    val mediaPicker: MediaPicker = koinInject()
    val mediaPreviewer: MediaPreviewer = koinInject()

    LaunchedEffect(Unit) {
        viewModel.captureCompleted.collect {
            onClose()
        }
    }

    val onPickMedia: () -> Unit = {
        uiScope.launch {
            val files = mediaPicker.pickImagesAndVideos()
            if (files.isEmpty()) return@launch
            val items = coroutineScope {
                files.map { file ->
                    async { file.toMediaItem() }
                }.awaitAll()
            }
            viewModel.addMediaItems(items)
        }
    }
    val previewPanel: @Composable () -> Unit = {
        PreviewPanel(
            uiState = uiState,
            reviewRichTextState = reviewRichTextState
        )
    }
    val mainContent: @Composable () -> Unit = {
        CaptureScreenContent(
            uiState = uiState,
            mediaPreviewer = mediaPreviewer,
            reviewRichTextState = reviewRichTextState,
            onUpdateIntent = viewModel::updateIntent,
            onStartCapture = viewModel::startCapture,
            onCompleteCapture = viewModel::completeCaptureAndExit,
            onUpdateInput = viewModel::updateInputText,
            onInputFocusChanged = viewModel::onInputFocusChanged,
            onRemoveMedia = viewModel::removeMediaItem,
            onPickMedia = onPickMedia,
            onRetryAi = viewModel::retryAiCapture,
            onEditInputFromAiFailed = viewModel::backToInputFromAiFailed,
            onUpdateReviewText = viewModel::updateReviewText,
            onUpdateReviewTitle = viewModel::updateReviewTitle,
            onUpdateReviewSource = viewModel::updateReviewSourceForm,
            onUpdateReviewStyle = viewModel::updateReviewStyle,
            onUpdateReviewCodeLanguage = viewModel::updateReviewCodeLanguage,
            onUpdateReviewTagQuery = viewModel::updateReviewTagQuery,
            onAddReviewTag = viewModel::addReviewTag,
            onRemoveReviewTag = viewModel::removeReviewTag,
            onUpdateReviewPrimary = viewModel::updateReviewPrimaryContent,
            onRetryPost = viewModel::retryPostPublish,
            onBackToReview = viewModel::backToReview,
            onClearError = viewModel::clearError,
            onClose = onClose
        )
    }

    // TRICKY: 使用 RTL 仅为了让 NavigationDrawer 的抽屉出现在右侧；主内容区再恢复 LTR，避免 AppBar 左右颠倒
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (usePermanentDrawer) {
            PermanentNavigationDrawer(
                modifier = modifier.fillMaxSize(),
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(420.dp)
                            .shadow(elevation = 24.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                            .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        CompositionLocalProvider(value = LocalLayoutDirection provides LayoutDirection.Ltr) {
                            previewPanel()
                        }
                    }
                }
            ) {
                CompositionLocalProvider(value = LocalLayoutDirection provides LayoutDirection.Ltr) {
                    mainContent()
                }
            }
        } else {
            ModalNavigationDrawer(
                modifier = modifier.fillMaxSize(),
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(420.dp)
                            .shadow(elevation = 24.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                            .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        CompositionLocalProvider(value = LocalLayoutDirection provides LayoutDirection.Ltr) {
                            previewPanel()
                        }
                    }
                }
            ) {
                CompositionLocalProvider(value = LocalLayoutDirection provides LayoutDirection.Ltr) {
                    mainContent()
                }
            }
        }
    }
}

@Composable
private fun CaptureScreenContent(
    uiState: CaptureUiState,
    mediaPreviewer: MediaPreviewer,
    reviewRichTextState: RichTextState,
    onUpdateIntent: (CardType) -> Unit,
    onStartCapture: () -> Unit,
    onCompleteCapture: () -> Unit,
    onUpdateInput: (String) -> Unit,
    onInputFocusChanged: (Boolean) -> Unit,
    onRemoveMedia: (String) -> Unit,
    onPickMedia: () -> Unit,
    onRetryAi: () -> Unit,
    onEditInputFromAiFailed: () -> Unit,
    onUpdateReviewText: (String) -> Unit,
    onUpdateReviewTitle: (String) -> Unit,
    onUpdateReviewSource: (CardSource) -> Unit,
    onUpdateReviewStyle: (Int) -> Unit,
    onUpdateReviewCodeLanguage: (String) -> Unit,
    onUpdateReviewTagQuery: (String) -> Unit,
    onAddReviewTag: (String) -> Unit,
    onRemoveReviewTag: (String) -> Unit,
    onUpdateReviewPrimary: (ReviewContentType) -> Unit,
    onRetryPost: () -> Unit,
    onBackToReview: () -> Unit,
    onClearError: () -> Unit,
    onClose: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val horizontalPadding = if (windowSizeClass.isWidthAtLeastExpanded()) 40.dp else 24.dp
    var previewItem by remember { mutableStateOf<MediaItem?>(null) }

    Scaffold(
        topBar = {
            Column {
                CaptureTopAppBar(
                    horizontalPadding = horizontalPadding,
                    canCapture = uiState.canCapture,
                    isWaiting = uiState.isTopBarWaiting,
                    intent = uiState.intent,
                    showIntentSelector = uiState.showIntentSelector,
                    intentEnabled = uiState.canEditReview,
                    onClose = onClose,
                    onIntentChange = onUpdateIntent,
                    onCapture = if (uiState.isInputPhase) {
                        onStartCapture
                    } else {
                        onCompleteCapture
                    }
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues = padding)
        ) {
            if (uiState.isInputPhase) {
                InputSection(
                    state = uiState.state,
                    error = uiState.error,
                    inputText = uiState.input.text,
                    mediaItems = uiState.media.items,
                    canEditInput = uiState.canEditInput,
                    horizontalPadding = horizontalPadding,
                    onInputChange = onUpdateInput,
                    onInputFocusChanged = onInputFocusChanged,
                    onRemoveMedia = onRemoveMedia,
                    onAddMedia = onPickMedia,
                    onPreviewMedia = { previewItem = it },
                    onRetry = onRetryAi,
                    onEditInput = onEditInputFromAiFailed,
                    onClearError = onClearError
                )
            }
            if (uiState.isReviewPhase) {
                uiState.review?.let { review ->
                    ReviewSection(
                        state = uiState.state,
                        error = uiState.error,
                        review = review,
                        reviewRichTextState = reviewRichTextState,
                        horizontalPadding = horizontalPadding,
                        isProcessing = !uiState.canEditReview,
                        onReviewTextChange = onUpdateReviewText,
                        onReviewTitleChange = onUpdateReviewTitle,
                        onReviewSourceFormChange = onUpdateReviewSource,
                        onReviewStyleChange = onUpdateReviewStyle,
                        onReviewCodeLanguageChange = onUpdateReviewCodeLanguage,
                        onReviewTagQueryChange = onUpdateReviewTagQuery,
                        onAddReviewTag = onAddReviewTag,
                        onRemoveReviewTag = onRemoveReviewTag,
                        onReviewPrimaryChange = onUpdateReviewPrimary,
                        onPreviewMedia = { previewItem = it },
                        onRetry = onRetryPost,
                        onBackToReview = onBackToReview
                    )
                }
            }

            previewItem?.let { item ->
                MediaPreviewDialog(
                    item = item,
                    mediaPreviewer = mediaPreviewer,
                    onDismiss = { previewItem = null }
                )
            }
        }
    }
}

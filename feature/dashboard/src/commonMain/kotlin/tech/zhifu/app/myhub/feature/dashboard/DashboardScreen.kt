package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.component.card.CardComponent
import tech.zhifu.app.myhub.component.card.CardStyles
import tech.zhifu.app.myhub.component.card.getContentPreview
import tech.zhifu.app.myhub.component.card.typeIconColor
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.domain.ReviewProgress
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_asset_collections
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_cards_to_review
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_curated_library
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_focus_and_review
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_good_evening
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_latest_captures
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_more_options
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_new_capture
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_no_cards_to_review
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_recently_added
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_refresh
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_search
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_view_all
import tech.zhifu.app.myhub.ui.LocalWindowSizeClass
import tech.zhifu.app.myhub.ui.isWidthCompact
import tech.zhifu.app.myhub.ui.isWidthExpanded
import tech.zhifu.app.myhub.ui.isWidthLarge
import tech.zhifu.app.myhub.ui.isWidthMedium


@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = koinInject<DashboardViewModel>(),
    onNavigateToCardDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val sbHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel.navigateToLogin) {
        viewModel.navigateToLogin.collect {
            sbHostState.showSnackbar(
                message = "登录已过期，请重新登录",
                duration = SnackbarDuration.Short
            )
            onNavigateToLogin()
        }
    }
    val sizeClass = LocalWindowSizeClass.current
    val columns = when {
        sizeClass.isWidthCompact() -> 1
        sizeClass.isWidthMedium() -> 2
        sizeClass.isWidthExpanded() -> 3
        sizeClass.isWidthLarge() -> 4
        else -> 3
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val contentState = uiState as? DashboardUiState.Content
    val isRefreshing = contentState?.isRefreshing ?: false
    val showAppBarReviewEntrance = (contentState?.reviewProgress?.total ?: 0) > 0

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(sbHostState) },
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.feature_dashboard_good_evening),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                subtitle = {
                    Row(
                        modifier = Modifier.heightIn(max = 28.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if ((contentState?.reviewCardsCount ?: 0) > 0) {
                                stringResource(
                                    Res.string.feature_dashboard_cards_to_review,
                                    contentState?.reviewCardsCount ?: 0
                                )
                            } else {
                                stringResource(Res.string.feature_dashboard_no_cards_to_review)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (showAppBarReviewEntrance) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            TextButton(
                                onClick = { viewModel.startReview() },
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = stringResource(Res.string.feature_dashboard_focus_and_review),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {},
                actions = {
                    TooltipIconButton(
                        icon = Icons.Filled.Search,
                        tooltipText = stringResource(Res.string.feature_dashboard_search),
                        contentDescription = stringResource(Res.string.feature_dashboard_search),
                        onClick = { /* TODO */ }
                    )
                    TooltipIconButton(
                        icon = Icons.Filled.Refresh,
                        tooltipText = stringResource(Res.string.feature_dashboard_refresh),
                        contentDescription = stringResource(Res.string.feature_dashboard_refresh),
                        content = { RefreshButton(isLoading = isRefreshing, onRefresh = { viewModel.refresh() }) }
                    )
                    TooltipIconButton(
                        icon = Icons.Filled.MoreVert,
                        tooltipText = stringResource(Res.string.feature_dashboard_more_options),
                        contentDescription = stringResource(Res.string.feature_dashboard_more_options),
                        onClick = { /* TODO */ }
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        content = { innerPadding ->
            when (val state = uiState) {
                is DashboardUiState.InitialLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is DashboardUiState.Content -> {
                    val pullToRefreshState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        modifier = Modifier.fillMaxSize(),
                        isRefreshing = state.isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        state = pullToRefreshState
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(columns),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = DashboardLayout.ContentPaddingH,
                                    end = DashboardLayout.ContentPaddingH,
                                    top = if (state.showFocusReview && state.reviewProgress.total > 0) DashboardLayout.FocusReviewReservedTop else DashboardLayout.FocusReviewTopPadding,
                                    bottom = DashboardLayout.FocusReviewTopPadding
                                ),
                                verticalItemSpacing = DashboardLayout.GridSpacing,
                                horizontalArrangement = Arrangement.spacedBy(DashboardLayout.GridSpacing)
                            ) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    AssetCollectionsModule(
                                        collections = state.collections,
                                        onCollectionClick = { /* TODO: 导航到 Collection 详情 */ },
                                        onViewAllClick = { /* TODO: 导航到所有 Collections */ },
                                        onLoadMore = { viewModel.loadMoreCollections() },
                                        isLoadingMore = state.isLoadingMoreCollections,
                                        hasMore = state.hasMoreCollections,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = DashboardLayout.SectionBottomSpacing)
                                    )
                                }

                                item(span = StaggeredGridItemSpan.FullLine) {
                                    LatestCapturesTitleRow(
                                        onViewAllClick = { /* TODO: 导航到全部 Captures */ },
                                        modifier = Modifier.padding(bottom = DashboardLayout.SectionTitleSpacing)
                                    )
                                }

                                items(
                                    items = state.recentCards,
                                    key = { it.id }
                                ) { card ->
                                    LatestCaptureCardItem(
                                        card = card,
                                        onCardClick = { onNavigateToCardDetail(card.id) },
                                        onEdit = { viewModel.editCard(card.id) },
                                        onFavorite = { viewModel.toggleFavorite(card.id) }
                                    )
                                }

                                item(span = StaggeredGridItemSpan.SingleLane) {
                                    NewCaptureCard(
                                        onClick = { /* TODO: 导航到新建卡片页面 */ },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (state.hasMoreCards) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        if (state.isLoadingMoreCards) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                            }
                                        } else {
                                            Box(modifier = Modifier.size(0.dp)) {
                                                LaunchedEffect(Unit) { viewModel.loadMoreCards() }
                                            }
                                        }
                                    }
                                }
                            }

                            if (state.showFocusReview && state.reviewProgress.total > 0) {
                                FocusReviewModule(
                                    reviewProgress = state.reviewProgress,
                                    onStartReview = { viewModel.startReview() },
                                    onDismiss = { viewModel.dismissFocusReview() },
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(
                                            start = DashboardLayout.ContentPaddingH,
                                            end = DashboardLayout.ContentPaddingH,
                                            top = DashboardLayout.FocusReviewTopPadding
                                        )
                                        .zIndex(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

private fun Modifier.cardInteraction(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
) = hoverable(interactionSource).clickable(
    interactionSource = interactionSource,
    indication = null,
    onClick = onClick
)

private object DashboardLayout {
    val ContentPaddingH = 32.dp
    val SectionTitleSpacing = 24.dp
    val SectionBottomSpacing = 36.dp
    val GridSpacing = 20.dp
    val FocusReviewTopPadding = 16.dp
    val FocusReviewReservedTop = 120.dp
    val CollectionTitleTop = 12.dp
    val CollectionTitlePaddingH = 4.dp
}


@Composable
private fun RefreshButton(
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    val rotationAngle = if (isLoading) {
        val infiniteTransition = rememberInfiniteTransition(label = "refresh_rotation")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
    } else {
        animateFloatAsState(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 200, easing = LinearEasing),
            label = "rotation_reset"
        )
    }

    IconButton(
        onClick = onRefresh,
        enabled = !isLoading,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(20.dp)
                .rotate(rotationAngle.value)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TooltipIconButton(
    icon: ImageVector,
    tooltipText: String,
    contentDescription: String,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(tooltipText) } },
        state = rememberTooltipState(),
    ) {
        if (content != null) content() else IconButton(onClick = onClick ?: {}) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}

@Composable
fun FocusReviewModule(
    reviewProgress: ReviewProgress,
    onStartReview: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.wrapContentWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressWithText(
                progress = reviewProgress.progress,
                text = "${reviewProgress.completed}/${reviewProgress.total}",
                modifier = Modifier.size(64.dp)
            )
            Column(
                modifier = Modifier.wrapContentWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "focus & review",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
                Button(
                    onClick = onStartReview,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = "Start Review",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CircularProgressWithText(
    progress: Float,
    text: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val progressColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 6.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            val sweepAngle = 360f * progress
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetCollectionsModule(
    collections: List<Collection>,
    onCollectionClick: (Collection) -> Unit,
    onViewAllClick: () -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.feature_dashboard_asset_collections),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = { PlainTooltip { Text(stringResource(Res.string.feature_dashboard_curated_library)) } },
                    state = rememberTooltipState(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            TextButton(
                onClick = onViewAllClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = stringResource(Res.string.feature_dashboard_view_all),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(DashboardLayout.SectionTitleSpacing))
        val cardWidth = 280.dp
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(end = if (hasMore && isLoadingMore) 56.dp else 0.dp)
        ) {
            items(
                items = collections,
                key = { it.id }
            ) { collection ->
                CollectionCard(
                    collection = collection,
                    onClick = { onCollectionClick(collection) },
                    modifier = Modifier.width(cardWidth)
                )
            }
            if (hasMore && isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .width(cardWidth)
                            .height(250.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        LaunchedEffect(hasMore, isLoadingMore) {
            if (hasMore && !isLoadingMore) onLoadMore()
        }
    }
}

@Composable
fun CollectionCard(
    collection: Collection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val liftY = animateFloatAsState(
        targetValue = if (isHovered) -4f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "collection_lift"
    )
    Column(
        modifier = modifier
            .cardInteraction(interactionSource, onClick)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = liftY.value },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                1.dp,
                if (isHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1E1F23))
                    .padding(12.dp)
            ) {
                val line0 = collection.cards.getOrNull(0)?.getContentPreview(80).orEmpty()
                val line1 = collection.cards.getOrNull(1)?.getContentPreview(30).orEmpty()
                val line2 = collection.cards.getOrNull(2)?.getContentPreview(30).orEmpty()
                val hasPreview = collection.cards.isNotEmpty()
                if (hasPreview) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            if (line0.isNotEmpty()) {
                                Text(
                                    text = line0,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                if (line1.isNotEmpty()) {
                                    Text(
                                        text = line1,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                if (line2.isNotEmpty()) {
                                    Text(
                                        text = line2,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = DashboardLayout.CollectionTitleTop,
                    start = DashboardLayout.CollectionTitlePaddingH,
                    end = DashboardLayout.CollectionTitlePaddingH
                ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = collection.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${collection.cardCount} Assets",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatestCapturesTitleRow(
    onViewAllClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.feature_dashboard_latest_captures),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                tooltip = { PlainTooltip { Text(stringResource(Res.string.feature_dashboard_recently_added)) } },
                state = rememberTooltipState(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        TextButton(
            onClick = onViewAllClick,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(Res.string.feature_dashboard_view_all),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun LatestCaptureCardItem(
    card: Card,
    onCardClick: () -> Unit,
    onEdit: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = card.typeIconColor
    val captureInteractionSource = remember { MutableInteractionSource() }
    val captureHovered by captureInteractionSource.collectIsHoveredAsState()
    val captureLiftY = animateFloatAsState(
        targetValue = if (captureHovered) -4f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "capture_lift"
    )

    val cardShape = RoundedCornerShape(CardStyles.CornerRadius)
    val borderWidth = if (captureHovered) 2.dp else 1.dp
    val borderColor = if (captureHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = captureLiftY.value }
            .border(borderWidth, borderColor, cardShape)
            .clip(cardShape)
            .hoverable(captureInteractionSource)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    accentColor,
                    RoundedCornerShape(topStart = CardStyles.CornerRadius, bottomStart = CardStyles.CornerRadius)
                )
        )
        CardComponent(
            card = card,
            onEdit = { onEdit() },
            onFavorite = { onFavorite() },
            onCardClick = { onCardClick() },
            modifier = Modifier.weight(1f),
            suppressDefaultBorder = true // 由外层 Row 统一绘制边框，避免双线
        )
    }
}

@Composable
fun NewCaptureCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val shape = RoundedCornerShape(CardStyles.CornerRadius)
    val newCaptureInteractionSource = remember { MutableInteractionSource() }
    val newCaptureHovered by newCaptureInteractionSource.collectIsHoveredAsState()
    Card(
        modifier = modifier
            .height(200.dp)
            .clip(shape)
            .cardInteraction(newCaptureInteractionSource, onClick)
            .drawBehind {
                val strokeWidthPx = 2.dp.toPx()
                val inset = strokeWidthPx / 2f
                val insetCornerRadius = (CardStyles.CornerRadius.toPx() - inset).coerceAtLeast(0f)
                // Stroke 居中于 path，内缩半线宽使虚线外缘贴齐卡片边缘，消除 gap
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = strokeWidthPx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 8.dp.toPx()))
                    ),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - 2 * inset, size.height - 2 * inset),
                    cornerRadius = CornerRadius(insetCornerRadius)
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = shape,
        border = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (newCaptureHovered) Modifier.background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape
                    ) else Modifier
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DashboardLayout.SectionTitleSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "New Capture",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.feature_dashboard_new_capture),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

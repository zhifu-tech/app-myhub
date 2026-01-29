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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.component.card.CardComponent
import tech.zhifu.app.myhub.component.card.getContentPreview
import tech.zhifu.app.myhub.component.card.typeIconColor
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.domain.ReviewProgress
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_cards_to_review
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_good_evening
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_no_cards_to_review
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

    val snackbarHostState = remember { SnackbarHostState() }
    // 401 未授权时提示并跳转登录
    LaunchedEffect(viewModel.navigateToLogin) {
        viewModel.navigateToLogin.collect {
            snackbarHostState.showSnackbar(
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
        else -> 3 // 默认值，实际上不会到达这里
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // 从 state 中获取数据（用于 AppBar 显示）
    val contentState = uiState as? DashboardUiState.Content
    val isRefreshing = contentState?.isRefreshing ?: false
    val showAppBarReviewEntrance = (contentState?.reviewProgress?.total ?: 0) > 0

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                    text = "Focus & Review",
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
                    // 搜索按钮
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above
                        ),
                        tooltip = { PlainTooltip { Text("Search") } },
                        state = rememberTooltipState(),
                    ) {
                        IconButton(onClick = { /* TODO: 打开搜索 */ }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search"
                            )
                        }
                    }
                    // 刷新按钮
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above
                        ),
                        tooltip = { PlainTooltip { Text("Refresh") } },
                        state = rememberTooltipState(),
                    ) {
                        RefreshButton(
                            isLoading = isRefreshing,
                            onRefresh = { viewModel.refresh() }
                        )
                    }
                    // 更多选项按钮
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above
                        ),
                        tooltip = { PlainTooltip { Text("More options") } },
                        state = rememberTooltipState(),
                    ) {
                        IconButton(onClick = { /* TODO: 打开更多选项 */ }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                    }
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
                        // 可以在这里添加加载指示器
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is DashboardUiState.Content -> {
                    // 下拉刷新：使用 Material3 PullToRefreshBox
                    val pullToRefreshState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        modifier = Modifier.fillMaxSize(),
                        isRefreshing = state.isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        state = pullToRefreshState
                    ) {
                        // Box 应用 innerPadding，使整块内容（含浮动模块）从 AppBar 下方开始，避免被遮挡
                        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                            // 使用瀑布流：FullLine 放整行模块，卡片项自然形成瀑布流
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(columns),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 32.dp,
                                    end = 32.dp,
                                    top = if (state.showFocusReview && state.reviewProgress.total > 0) 120.dp else 16.dp,
                                    bottom = 16.dp
                                ),
                                verticalItemSpacing = 20.dp,
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Asset Collections 模块
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
                                            .padding(bottom = 36.dp)
                                    )
                                }

                                // Latest Captures 标题行
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    LatestCapturesTitleRow(
                                        onViewAllClick = { /* TODO: 导航到全部 Captures */ },
                                        modifier = Modifier.padding(bottom = 4.dp) // 与卡片区 24.dp
                                    )
                                }

                                // Latest Captures 卡片：瀑布流
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

                                // New Capture 占位
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    NewCaptureCard(
                                        onClick = { /* TODO: 导航到新建卡片页面 */ },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Load more
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

                            // Focus & Review 模块：浮动在内容之上，位于 AppBar 下方（避免被遮挡），宽度包裹内容
                            if (state.showFocusReview && state.reviewProgress.total > 0) {
                                FocusReviewModule(
                                    reviewProgress = state.reviewProgress,
                                    onStartReview = { viewModel.startReview() },
                                    onDismiss = { viewModel.dismissFocusReview() },
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(start = 32.dp, end = 32.dp, top = 16.dp)
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

@Composable
private fun RefreshButton(
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    // 根据加载状态使用不同的动画, 在加载时显示旋转动画并禁用按钮
    val rotationAngle = if (isLoading) {
        // 加载时使用无限旋转动画
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
        // 停止加载时平滑回到 0 度
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
            // 左侧：圆形进度指示器
            CircularProgressWithText(
                progress = reviewProgress.progress,
                text = "${reviewProgress.completed}/${reviewProgress.total}",
                modifier = Modifier.size(64.dp)
            )

            // 中间：文本和按钮
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

            // 右侧：关闭按钮
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

/**
 * 圆形进度指示器，带文本
 */
@Composable
private fun CircularProgressWithText(
    progress: Float,
    text: String,
    modifier: Modifier = Modifier
) {
    // 在 @Composable 上下文中获取颜色
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val progressColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 绘制圆形进度条
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 6.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 背景圆
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 进度圆
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

        // 中心文本
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

// ==================== Asset Collections Module ====================

/**
 * Asset Collections 模块
 * 根据设计稿实现：标题 + info 图标 + "View All" 链接 + Collection 卡片网格
 */
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
        // 标题行
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
                    text = "Asset Collections",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                    tooltip = { PlainTooltip { Text("Your curated library of cards") } },
                    state = rememberTooltipState(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Info",
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
                    text = "View All",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // mb-6 = 24.dp

        // 设计稿：横向一排 Collection 卡片（LazyRow），固定宽度 4:3，rounded-3xl
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
        if (hasMore && !isLoadingMore) {
            LaunchedEffect(Unit) { onLoadMore() }
        }
    }
}

/**
 * Collection 卡片组件
 * 设计稿像素级：卡片本体仅包含预览区（背景+细边框）；标题与数量在卡片下方、直接落在页面背景上，无独立背景/边框；整块可点击，hover 仅卡片抬起。
 */
@Composable
fun CollectionCard(
    collection: Collection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewLines: List<String>? = null
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
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        // 卡片本体：仅预览区；hover 时选中光圈 ring-1 ring-primary/40（设计稿）
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                val useLocalPreview = previewLines != null && previewLines.size >= 3
                val pl = previewLines
                val line0 =
                    if (useLocalPreview && pl != null) pl[0] else collection.cards.getOrNull(0)?.getContentPreview(80)
                        .orEmpty()
                val line1 =
                    if (useLocalPreview && pl != null) pl[1] else collection.cards.getOrNull(1)?.getContentPreview(30)
                        .orEmpty()
                val line2 =
                    if (useLocalPreview && pl != null) pl[2] else collection.cards.getOrNull(2)?.getContentPreview(30)
                        .orEmpty()
                val hasPreview = useLocalPreview || collection.cards.isNotEmpty()
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
        // 标题与数量：在卡片下方，直接落在页面背景上，无边框、无卡片背景（设计稿像素级）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
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

// ==================== Latest Captures Module ====================

/**
 * Latest Captures 标题行：标题 + info + View All（用于瀑布流布局中 FullLine）
 */
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
                text = "Latest Captures",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above
                ),
                tooltip = { PlainTooltip { Text("Recently added cards across all categories") } },
                state = rememberTooltipState(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info",
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
                text = "View All",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 单张 Latest Capture 卡片（左侧类型色条 + CardComponent），用于瀑布流一项
 * 设计稿：单一边框，hover 时变色（border-slate-800/50 → hover:border-primary），避免光圈与固有边框双线
 */
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
    // 始终一层边框：未 hover = 灰（设计稿 border-slate-800/50），hover = primary，避免双光圈
    val borderColor = if (captureHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = captureLiftY.value }
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .hoverable(captureInteractionSource)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    accentColor,
                    RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                )
        )
        CardComponent(
            card = card,
            onEdit = { onEdit() },
            onFavorite = { onFavorite() },
            onCardClick = { onCardClick() },
            modifier = Modifier.weight(1f),
            suppressDefaultBorder = true
        )
    }
}

/**
 * New Capture 占位符卡片
 * 根据设计稿实现：虚线边框 + 加号图标 + "New Capture" 文本
 */
@Composable
fun NewCaptureCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier
            .height(200.dp) // min-h-[200px]
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 8.dp.toPx()))
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = shape,
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // p-6 = 24.dp
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 加号图标
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

            Spacer(modifier = Modifier.height(12.dp)) // gap-3 = 12.dp

            // "New Capture" 文本
            Text(
                text = "New Capture",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

package tech.zhifu.app.myhub.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.component.card.CardComponent
import tech.zhifu.app.myhub.component.card.displayTitle
import tech.zhifu.app.myhub.component.card.formatUpdatedTime
import tech.zhifu.app.myhub.component.card.getContentPreview
import tech.zhifu.app.myhub.component.card.typeIconColor
import tech.zhifu.app.myhub.component.card.typeIconText
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_cards_to_review
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_days_ago
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_favorites
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_good_evening
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_hours_ago
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_just_now
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_last_synced
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_minutes_ago
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_never_synced
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_no_cards_to_review
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_recent_edits
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_search_placeholder
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_total
import tech.zhifu.app.myhub.ui.WindowSizeClass
import tech.zhifu.app.myhub.ui.isCompact
import tech.zhifu.app.myhub.ui.isExpanded
import tech.zhifu.app.myhub.ui.isMedium
import tech.zhifu.app.myhub.ui.windowSizeClass
import kotlin.time.Clock

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = koinInject<DashboardViewModel>(),
    onNavigateToCardDetail: ((String) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val sizeClass = windowSizeClass()
    val columns = when {
        sizeClass.isCompact -> 1
        sizeClass.isMedium -> 2
        sizeClass.isExpanded -> 3
        else -> 3 // 默认值，实际上不会到达这里
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is DashboardUiState.InitialLoading -> {
                // 显示初始加载状态（无数据）
                Column(
                    modifier = Modifier.fillMaxSize(),
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
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 顶部头部（sticky）
                    DashboardHeader(
                        recentEditsCount = state.statistics.recentEdits,
                        lastSyncTime = state.lastSyncTime,
                        isLoading = state.isRefreshing, // 显示刷新动画
                        onRefresh = { viewModel.refresh() },
                        sizeClass = sizeClass
                    )

                    // 搜索栏和工具栏
                    DashboardToolbar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        statistics = state.statistics,
                        sizeClass = sizeClass,
                        viewType = state.viewType,
                        onViewTypeChange = { viewModel.setViewType(it) }
                    )

                    // 根据视图类型显示不同的布局
                    when (state.viewType) {
                        ViewType.GRID -> {
                            // 1列和2列：统计卡片在Grid内部，作为第一个item，随列表滚动
                            // 3列：统计信息在工具栏中显示（已实现），Grid内部不显示
                            DashboardGridView(
                                cards = state.recentCards,
                                columns = columns,
                                statistics = if (sizeClass.isExpanded) null else state.statistics, // 1列和2列时显示统计卡片
                                sizeClass = sizeClass,
                                onEdit = { viewModel.editCard(it.id) },
                                onFavorite = { viewModel.toggleFavorite(it.id) },
                                onCardClick = {
                                    onNavigateToCardDetail?.invoke(it.id) ?: viewModel.viewCard(it.id)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            )
                        }

                        ViewType.LIST -> {
                            DashboardListView(
                                cards = state.recentCards,
                                columns = columns,
                                statistics = if (sizeClass.isExpanded) null else state.statistics, // 1列和2列时显示统计卡片
                                sizeClass = sizeClass,
                                onEdit = { viewModel.editCard(it.id) },
                                onFavorite = { viewModel.toggleFavorite(it.id) },
                                onCardClick = {
                                    onNavigateToCardDetail?.invoke(it.id) ?: viewModel.viewCard(it.id)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            )
                        }
                    }

                    // 错误提示（如果有错误且不在刷新中）
                    if (state.error != null && !state.isRefreshing) {
                        // TODO: 可以在这里添加错误提示 UI
                    }
                }
            }
        }
    }
}

// ==================== Header Components ====================

@Composable
fun DashboardHeader(
    recentEditsCount: Int,
    lastSyncTime: Long?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    sizeClass: WindowSizeClass
) {
    // 格式化最后同步时间
    val syncTimeText = when {
        lastSyncTime == null -> stringResource(Res.string.feature_dashboard_never_synced)
        else -> {
            val now = Clock.System.now().toEpochMilliseconds()
            val diff = now - lastSyncTime
            when {
                diff < 60_000 -> stringResource(Res.string.feature_dashboard_just_now)
                diff < 3_600_000 -> stringResource(Res.string.feature_dashboard_minutes_ago, (diff / 60_000))
                diff < 86_400_000 -> stringResource(Res.string.feature_dashboard_hours_ago, (diff / 3_600_000))
                else -> stringResource(Res.string.feature_dashboard_days_ago, (diff / 86_400_000))
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f), // backdrop-blur effect
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp) // px-6 py-4
        ) {
            // 统一布局：所有尺寸都使用水平布局，保持一致性
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.feature_dashboard_good_evening),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (recentEditsCount > 0) {
                            stringResource(Res.string.feature_dashboard_cards_to_review, recentEditsCount)
                        } else {
                            stringResource(Res.string.feature_dashboard_no_cards_to_review)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp) // mt-1
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // gap-3
                ) {
                    Text(
                        text = stringResource(Res.string.feature_dashboard_last_synced, syncTimeText),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    // 刷新按钮（带旋转动画）
                    RefreshButton(
                        isLoading = isLoading,
                        onRefresh = onRefresh
                    )
                }
            }
        }
    }
}

/**
 * 刷新按钮组件
 * 在加载时显示旋转动画并禁用按钮
 */
@Composable
private fun RefreshButton(
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    // 根据加载状态使用不同的动画
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

// ==================== Toolbar Components ====================

/**
 * 视图类型切换按钮组
 */
@Composable
private fun ViewTypeToggle(
    currentViewType: ViewType,
    onViewTypeChange: (ViewType) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ViewTypeToggleButton(
                viewType = ViewType.GRID,
                icon = Icons.Default.GridView,
                contentDescription = "Grid View",
                isSelected = currentViewType == ViewType.GRID,
                onClick = { onViewTypeChange(ViewType.GRID) }
            )
            ViewTypeToggleButton(
                viewType = ViewType.LIST,
                icon = Icons.AutoMirrored.Filled.ViewList,
                contentDescription = "List View",
                isSelected = currentViewType == ViewType.LIST,
                onClick = { onViewTypeChange(ViewType.LIST) }
            )
        }
    }
}

/**
 * 视图类型切换按钮
 */
@Composable
private fun ViewTypeToggleButton(
    viewType: ViewType,
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) {
            MaterialTheme.colorScheme.surface
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(36.dp),
        tonalElevation = if (isSelected) 1.dp else 0.dp
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DashboardToolbar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statistics: Statistics,
    sizeClass: WindowSizeClass,
    viewType: ViewType,
    onViewTypeChange: (ViewType) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 搜索栏和工具栏行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 搜索栏
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .height(44.dp)
                            .widthIn(min = if (sizeClass.isCompact) 150.dp else 200.dp),
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.feature_dashboard_search_placeholder),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    // 工具栏（统计信息和视图切换）
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 统计信息（仅3列时显示，与搜索栏同一行）
                        // 2列时统计信息在Grid上方显示，不在这里显示
                        if (sizeClass.isExpanded) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF10b981)) // emerald-500
                                        )
                                        Text(
                                            text = "${statistics.totalCards} ${stringResource(Res.string.feature_dashboard_total)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(16.dp)
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFf59e0b)) // amber-500
                                        )
                                        Text(
                                            text = "${statistics.favoriteCards} ${stringResource(Res.string.feature_dashboard_favorites)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // 视图切换
                        ViewTypeToggle(
                            currentViewType = viewType,
                            onViewTypeChange = onViewTypeChange
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                thickness = 1.dp
            )
        }
    }
}

// ==================== Statistics Components ====================

/**
 * 统计卡片行（移动端：3列网格布局）
 *
 * HTML规则：grid grid-cols-3 gap-4 mb-8
 * - 3列网格布局
 * - gap-4 = 16.dp
 * - mb-8 = 32.dp (底部间距)
 */
@Composable
fun StatsCardsRow(
    statistics: Statistics,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp) // gap-4 = 16.dp
    ) {
        StatCard(
            label = stringResource(Res.string.feature_dashboard_total),
            value = "${statistics.totalCards}",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = stringResource(Res.string.feature_dashboard_recent_edits),
            value = "${statistics.recentEdits}",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = stringResource(Res.string.feature_dashboard_favorites),
            value = "${statistics.favoriteCards}",
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 统计卡片可滑动行（中型和扩展布局：横向滑动）
 */
@Composable
fun StatsCardsScrollableRow(
    statistics: Statistics
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        item {
            StatCard(
                label = stringResource(Res.string.feature_dashboard_total),
                value = "${statistics.totalCards}",
                modifier = Modifier.width(140.dp)
            )
        }
        item {
            StatCard(
                label = stringResource(Res.string.feature_dashboard_recent_edits),
                value = "${statistics.recentEdits}",
                modifier = Modifier.width(140.dp)
            )
        }
        item {
            StatCard(
                label = stringResource(Res.string.feature_dashboard_favorites),
                value = "${statistics.favoriteCards}",
                modifier = Modifier.width(140.dp)
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ==================== View Components ====================

/**
 * Grid 视图组件（瀑布流布局）
 *
 * 根据设计稿规则：
 * - 1列：统计卡片在 Grid 内部，作为第一个 item，随列表滚动
 * - 2列和3列：统计信息在工具栏中（已实现），Grid 内部不显示
 * - 使用 gap-6 (24.dp) 和 space-y-6 (24.dp)
 */
@Composable
fun DashboardGridView(
    cards: List<Card>,
    columns: Int,
    statistics: Statistics?,
    sizeClass: WindowSizeClass,
    onEdit: (Card) -> Unit,
    onFavorite: (Card) -> Unit,
    onCardClick: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    // 直接使用 LazyVerticalStaggeredGrid，统计卡片作为第一个 item
    // 注意：不能在 LazyColumn 中嵌套 LazyVerticalStaggeredGrid，会导致无限高度约束错误
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp), // gap-6 = 24.dp
        verticalItemSpacing = 24.dp, // space-y-6 = 24.dp
        modifier = modifier
    ) {
        // 统计卡片：1列和2列时在 Grid 内部，作为第一个 item，随列表滚动
        // 3列时统计信息在工具栏中显示，Grid 内部不显示
        if (statistics != null && !sizeClass.isExpanded) {
            // 使用 item 让统计卡片占据整行（跨所有列）
            // 对于 LazyVerticalStaggeredGrid，使用 span 参数让 item 跨越多列
            item(span = StaggeredGridItemSpan.FullLine) {
                StatsCardsRow(
                    statistics = statistics,
                    modifier = Modifier.padding(
                        bottom = if (sizeClass.isCompact) 12.dp else 24.dp // 单列时缩小间距为1/2
                    )
                )
            }
        }

        // 卡片列表
        items(cards) { card ->
            CardComponent(
                card = card,
                onEdit = { onEdit(card) },
                onFavorite = { onFavorite(card) },
                onCardClick = { onCardClick(card) }
            )
        }
    }
}

/**
 * List 视图组件
 */
@Composable
fun DashboardListView(
    cards: List<Card>,
    columns: Int,
    statistics: Statistics?,
    sizeClass: WindowSizeClass,
    onEdit: (Card) -> Unit,
    onFavorite: (Card) -> Unit,
    onCardClick: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    // 列表内容
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        // 统计卡片：1列和2列时在 List 内部，作为第一个 item，随列表滚动
        // 3列时统计信息在工具栏中显示，List 内部不显示
        if (statistics != null && !sizeClass.isExpanded) {
            item {
                StatsCardsRow(
                    statistics = statistics,
                    modifier = Modifier.padding(
                        bottom = if (sizeClass.isCompact) 12.dp else 24.dp // 单列时缩小间距为1/2
                    )
                )
            }
        }

        // 列表头部（仅在桌面端显示，在统计信息之后）
        if (!sizeClass.isCompact) {
            item {
                ListViewHeader(sizeClass = sizeClass)
            }
        }

        items(cards) { card ->
            ListViewItem(
                card = card,
                sizeClass = sizeClass,
                onEdit = { onEdit(card) },
                onFavorite = { onFavorite(card) },
                onCardClick = { onCardClick(card) }
            )
        }
    }
}

// ==================== List View Components ====================

/**
 * 列表头部（表头）
 */
@Composable
fun ListViewHeader(
    sizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name & Content 列
            Text(
                text = "Name & Content",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(if (sizeClass.isCompact) 1f else 0.4f)
            )

            // Tags 列（桌面端显示）
            if (!sizeClass.isCompact) {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.3f)
                )
            }

            // Last Modified 列（桌面端显示）
            if (!sizeClass.isCompact) {
                Text(
                    text = "Last Modified",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.2f)
                )
            }

            // Actions 列（桌面端显示）
            if (!sizeClass.isCompact) {
                Box(
                    modifier = Modifier.weight(0.1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Actions",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // 分隔线
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 24.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    )
}

/**
 * 列表项组件
 */
@Composable
fun ListViewItem(
    card: Card,
    sizeClass: WindowSizeClass,
    onEdit: (Card) -> Unit,
    onFavorite: (Card) -> Unit,
    onCardClick: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // 获取卡片显示信息（使用 Card 扩展方法）
    // @Composable 函数需要提前定义，确保在 Composable 上下文中正确调用
    val formattedDate = card.formatUpdatedTime()

    // 多次使用的值提前定义，避免重复计算
    val iconColor = card.typeIconColor

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCardClick(card) }
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isHovered) {
            MaterialTheme.colorScheme.surface
        } else {
            Color.Transparent
        },
        border = if (isHovered) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        } else {
            BorderStroke(1.dp, Color.Transparent)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标和标题/内容
            Row(
                modifier = Modifier.weight(if (sizeClass.isCompact) 1f else 0.4f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(iconColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = card.typeIconText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = iconColor
                        )
                    }
                }

                // 标题和内容预览
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = card.displayTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isHovered) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1
                    )
                    Text(
                        text = card.getContentPreview(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Tags（桌面端显示）
            if (!sizeClass.isCompact) {
                Row(
                    modifier = Modifier.weight(0.3f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    card.tags.take(2).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 最后修改时间
            if (!sizeClass.isCompact) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.2f)
                )
            }

            // 操作按钮（桌面端显示）
            if (!sizeClass.isCompact) {
                val buttonAlpha = if (isHovered) 1f else 0.6f
                Row(
                    modifier = Modifier.weight(0.1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onFavorite(card) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = if (card.isFavorite) {
                                Color(0xFFFFB020)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = buttonAlpha)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { onEdit(card) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = buttonAlpha),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

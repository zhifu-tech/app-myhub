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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
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
import tech.zhifu.app.myhub.component.CardComponent
import tech.zhifu.app.myhub.component.card.formatUpdatedTime
import tech.zhifu.app.myhub.component.card.getContentPreview
import tech.zhifu.app.myhub.component.card.getDisplayTitle
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
    viewModel: DashboardViewModel = koinInject<DashboardViewModel>()
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部头部（sticky）
            DashboardHeader(
                recentEditsCount = uiState.statistics.recentEdits,
                lastSyncTime = uiState.lastSyncTime,
                isLoading = uiState.isLoading,
                onRefresh = { viewModel.refresh() }
            )

            // 搜索栏和工具栏
            DashboardToolbar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                statistics = uiState.statistics,
                sizeClass = sizeClass,
                viewType = uiState.viewType,
                onViewTypeChange = { viewModel.setViewType(it) }
            )

            // 统计卡片（移动端显示）
            if (sizeClass.isCompact) {
                StatsCardsRow(statistics = uiState.statistics)
            }

            // 根据视图类型显示不同的布局
            when (uiState.viewType) {
                ViewType.GRID -> {
                    DashboardGridView(
                        cards = uiState.recentCards,
                        columns = columns,
                        onEdit = { viewModel.editCard(it.id) },
                        onFavorite = { viewModel.toggleFavorite(it.id) },
                        onCardClick = { viewModel.viewCard(it.id) },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                }

                ViewType.LIST -> {
                    DashboardListView(
                        cards = uiState.recentCards,
                        sizeClass = sizeClass,
                        onEdit = { viewModel.editCard(it.id) },
                        onFavorite = { viewModel.toggleFavorite(it.id) },
                        onCardClick = { viewModel.viewCard(it.id) },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
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
    onRefresh: () -> Unit
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
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
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
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                    // 统计信息（桌面端显示）
                    if (!sizeClass.isCompact) {
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
    }
}

// ==================== Statistics Components ====================

@Composable
fun StatsCardsRow(
    statistics: Statistics
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
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

@Composable
private fun StatCard(
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
 */
@Composable
fun DashboardGridView(
    cards: List<Card>,
    columns: Int,
    onEdit: (Card) -> Unit,
    onFavorite: (Card) -> Unit,
    onCardClick: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalItemSpacing = 24.dp,
        modifier = modifier
    ) {
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
    sizeClass: WindowSizeClass,
    onEdit: (Card) -> Unit,
    onFavorite: (Card) -> Unit,
    onCardClick: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 列表头部（仅在桌面端显示）
        if (!sizeClass.isCompact) {
            ListViewHeader(sizeClass = sizeClass)
        }

        // 列表内容
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
}

// ==================== List View Components ====================

/**
 * 列表头部（表头）
 */
@Composable
private fun ListViewHeader(
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
private fun ListViewItem(
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
    val cardTitle = card.getDisplayTitle()
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
                        text = cardTitle,
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

package tech.zhifu.app.myhub.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.component.CardComponent
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
    val columns = when (sizeClass) {
        WindowSizeClass.Compact -> 1
        WindowSizeClass.Medium -> 2
        WindowSizeClass.Expanded -> 3
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
                onRefresh = { viewModel.refresh() }
            )

            // 搜索栏和工具栏
            DashboardToolbar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                statistics = uiState.statistics,
                sizeClass = sizeClass
            )

            // 统计卡片（移动端显示）
            if (sizeClass == WindowSizeClass.Compact) {
                StatsCardsRow(statistics = uiState.statistics)
            }

            // 内容卡片瀑布流布局（可滚动区域）
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(columns),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalItemSpacing = 24.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(uiState.recentCards) { card ->
                    CardComponent(
                        card = card,
                        onEdit = { viewModel.editCard(it.id) },
                        onFavorite = { viewModel.toggleFavorite(it.id) },
                        onCardClick = { viewModel.viewCard(it.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(
    recentEditsCount: Int,
    lastSyncTime: Long?,
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
                diff < 3_600_000 -> stringResource(Res.string.feature_dashboard_minutes_ago, diff / 60_000)
                diff < 86_400_000 -> stringResource(Res.string.feature_dashboard_hours_ago, diff / 3_600_000)
                else -> stringResource(Res.string.feature_dashboard_days_ago, diff / 86_400_000)
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
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardToolbar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statistics: tech.zhifu.app.myhub.datastore.model.Statistics,
    sizeClass: WindowSizeClass
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
                        .weight(1f)
                        .height(44.dp),
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.feature_dashboard_search_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
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
                    singleLine = true
                )

                // 工具栏（统计信息和视图切换）
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 统计信息（桌面端显示）
                    if (sizeClass != WindowSizeClass.Compact) {
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
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(36.dp),
                                tonalElevation = 1.dp
                            ) {
                                IconButton(
                                    onClick = { /* TODO */ },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = "Grid View",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { /* TODO */ },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ViewList,
                                    contentDescription = "List View",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCardsRow(
    statistics: tech.zhifu.app.myhub.datastore.model.Statistics
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


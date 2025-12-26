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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.components.ArticleCard
import tech.zhifu.app.myhub.components.ChecklistCard
import tech.zhifu.app.myhub.components.CodeCard
import tech.zhifu.app.myhub.components.DictionaryCard
import tech.zhifu.app.myhub.components.IdeaCard
import tech.zhifu.app.myhub.components.QuoteCard
import tech.zhifu.app.myhub.ui.utils.ClipboardManager
import tech.zhifu.app.myhub.ui.utils.WindowSizeClass
import tech.zhifu.app.myhub.ui.utils.windowSizeClass
import tech.zhifu.app.myhub.ui.utils.rememberClipboardManager
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部头部
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
            sizeClass = sizeClass,
            onExport = { viewModel.exportData() },
            onImport = { viewModel.showImportDialog() }
        )

        // 统计卡片（移动端显示）
        if (sizeClass == WindowSizeClass.Compact) {
            StatsCardsRow(statistics = uiState.statistics)
        }

        // 内容卡片网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            items(listOf("quote", "code", "idea", "article", "dictionary", "checklist")) { type ->
                when (type) {
                    "quote" -> QuoteCard()
                    "code" -> CodeCard()
                    "idea" -> IdeaCard()
                    "article" -> ArticleCard()
                    "dictionary" -> DictionaryCard()
                    "checklist" -> ChecklistCard()
                }
            }
        }
    }

    // 导出对话框
    if (uiState.showExportDialog) {
        ExportDialog(
            jsonData = uiState.exportedJson ?: "",
            onDismiss = { viewModel.closeExportDialog() }
        )
    }

    // 导入对话框
    if (uiState.showImportDialog) {
        ImportDialog(
            jsonText = uiState.importJson,
            error = uiState.importError,
            preview = uiState.importPreview,
            isLoading = uiState.isLoading,
            onJsonChange = { viewModel.updateImportJson(it) },
            onPreview = { viewModel.previewImportData() },
            onImport = { viewModel.importData() },
            onDismiss = { viewModel.closeImportDialog() }
        )
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
        lastSyncTime == null -> "Never synced"
        else -> {
            val now = Clock.System.now().toEpochMilliseconds()
            val diff = now - lastSyncTime
            when {
                diff < 60_000 -> "Just now" // 1分钟内
                diff < 3_600_000 -> "${diff / 60_000} minutes ago" // 1小时内
                diff < 86_400_000 -> "${diff / 3_600_000} hours ago" // 24小时内
                else -> "${diff / 86_400_000} days ago"
            }
        }
    }

    // 获取问候语
    val greeting = when {
        else -> "Good evening, Scholar" // 可以根据时间调整
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (recentEditsCount > 0) {
                        "You have $recentEditsCount cards to review today."
                    } else {
                        "No cards to review today."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Last synced: $syncTimeText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    sizeClass: WindowSizeClass,
    onExport: () -> Unit = {},
    onImport: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 搜索栏
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search card content, tags, or authors...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // 工具栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 统计信息（桌面端显示）
                if (sizeClass != WindowSizeClass.Compact) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "${statistics.totalCards} Total",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.height(16.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFFB020))
                            )
                            Text(
                                text = "${statistics.favoriteCards} Favorites",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // 导出/导入按钮和视图切换
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 导出按钮
                    IconButton(onClick = onExport) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "导出数据",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // 导入按钮
                    IconButton(onClick = onImport) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "导入数据",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // 视图切换
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                IconButton(onClick = { /* TODO */ }) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = "Grid View",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(onClick = { /* TODO */ }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ViewList,
                                    contentDescription = "List View",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        StatCard("Total Cards", "${statistics.totalCards}", Modifier.weight(1f))
        StatCard("Recent Edits", "${statistics.recentEdits}", Modifier.weight(1f))
        StatCard("Favorites", "${statistics.favoriteCards}", Modifier.weight(1f))
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * 导出数据对话框
 */
@Composable
fun ExportDialog(
    jsonData: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = rememberClipboardManager()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("导出数据")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "数据已导出为 JSON 格式，您可以复制以下内容并保存到文件中：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = jsonData,
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    readOnly = true,
                    maxLines = 15,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    clipboardManager.copyToClipboard(jsonData)
                    onDismiss()
                }
            ) {
                Text("复制")
            }
        }
    )
}

/**
 * 导入数据对话框
 */
@Composable
fun ImportDialog(
    jsonText: String,
    error: String?,
    preview: DashboardExportData?,
    isLoading: Boolean,
    onJsonChange: (String) -> Unit,
    onPreview: () -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("导入数据")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "请粘贴 JSON 格式的数据：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = jsonText,
                    onValueChange = onJsonChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = {
                        Text("粘贴 JSON 数据...")
                    },
                    maxLines = 10,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                // 错误信息
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // 预览信息
                if (preview != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "数据预览：",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "卡片数量: ${preview.cards.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "总卡片数: ${preview.statistics.totalCards}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "收藏数: ${preview.statistics.favoriteCards}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPreview,
                    enabled = jsonText.isNotBlank() && !isLoading
                ) {
                    Text("预览")
                }
                Button(
                    onClick = onImport,
                    enabled = preview != null && !isLoading
                ) {
                    Text(if (isLoading) "导入中..." else "导入")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

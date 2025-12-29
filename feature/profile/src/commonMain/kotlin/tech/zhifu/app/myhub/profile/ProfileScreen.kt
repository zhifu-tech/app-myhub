package tech.zhifu.app.myhub.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import tech.zhifu.app.myhub.component.mixed.Avatar
import tech.zhifu.app.myhub.feature.profile.resources.Res
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_about
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_account_management
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_cancel
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_data_management
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_edit_avatar_url
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_edit_display_name
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_edit_email
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_edit_profile
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_favorite_cards
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_joined
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_loading
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_no_user
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_recent_edits
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_save
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_settings
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_statistics
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_title
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_total_cards
import tech.zhifu.app.myhub.feature.profile.resources.feature_profile_version

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinInject<ProfileViewModel>(),
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.feature_profile_title)) }
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.user == null) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.feature_profile_loading),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else if (uiState.user == null) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.feature_profile_no_user),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            val user = uiState.user
            if (user != null) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 用户信息卡片
                    ProfileHeaderCard(
                        user = user,
                        onEditClick = { viewModel.startEditProfile() }
                    )

                    // 统计数据卡片
                    ProfileStatsCard(
                        statistics = uiState.statistics
                    )

                    // 配置入口列表
                    ProfileSettingsList(
                        onSettingsClick = onNavigateToSettings
                    )

                    // 关于信息
                    ProfileAboutSection()
                }
            }
        }
    }

    // 编辑对话框
    uiState.editProfileDialog?.let { dialogState ->
        EditProfileDialog(
            state = dialogState,
            onDismiss = { viewModel.cancelEditProfile() },
            onSave = { displayName, email, avatarUrl ->
                viewModel.saveProfile(displayName, email, avatarUrl)
            }
        )
    }
}

/**
 * 用户信息卡片
 * 按照设计图：居中垂直布局，深色背景，头像居中带编辑图标
 */
@Composable
private fun ProfileHeaderCard(
    user: tech.zhifu.app.myhub.datastore.model.User,
    onEditClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 头像（居中，带编辑图标）
            Box(
                modifier = Modifier.size(96.dp)
            ) {
                Avatar(size = 96.dp)
                // 编辑图标（右下角）
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(Res.string.feature_profile_edit_profile),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.size(8.dp))

            // 姓名（大号粗体）
            Text(
                text = user.displayName ?: user.username,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 用户名（小号灰色）
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.size(8.dp))

            // 简介（暂时使用 email 或占位文本）
            Text(
                text = user.email ?: "Knowledge enthusiast. Creating beautiful study cards.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
            )

            Spacer(modifier = Modifier.size(16.dp))

            // 底部元数据（加入日期和位置）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 加入日期
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stringResource(Res.string.feature_profile_joined)} ${formatDate(user.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.size(24.dp))

                // 位置（暂时隐藏，因为 User 模型没有 location 字段）
                // Row(
                //     verticalAlignment = Alignment.CenterVertically,
                //     horizontalArrangement = Arrangement.spacedBy(4.dp)
                // ) {
                //     Icon(
                //         imageVector = Icons.Default.LocationOn,
                //         contentDescription = null,
                //         modifier = Modifier.size(16.dp),
                //         tint = MaterialTheme.colorScheme.onSurfaceVariant
                //     )
                //     Text(
                //         text = "Beijing, CN",
                //         style = MaterialTheme.typography.bodySmall,
                //         color = MaterialTheme.colorScheme.onSurfaceVariant
                //     )
                // }
            }
        }
    }
}

/**
 * 统计数据卡片
 */
@Composable
private fun ProfileStatsCard(
    statistics: tech.zhifu.app.myhub.datastore.model.Statistics?
) {
    if (statistics == null) return

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.feature_profile_statistics),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.size(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatItem(
                    label = stringResource(Res.string.feature_profile_total_cards),
                    value = "${statistics.totalCards}",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = stringResource(Res.string.feature_profile_favorite_cards),
                    value = "${statistics.favoriteCards}",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = stringResource(Res.string.feature_profile_recent_edits),
                    value = "${statistics.recentEdits}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(
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
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 配置入口列表
 */
@Composable
private fun ProfileSettingsList(
    onSettingsClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 应用设置
            Surface(
                onClick = onSettingsClick,
                color = Color.Transparent
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.feature_profile_settings)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            // 数据管理（占位符）
            Surface(
                onClick = { /* TODO */ },
                color = Color.Transparent
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.feature_profile_data_management)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            // 账户管理（占位符）
            Surface(
                onClick = { /* TODO */ },
                color = Color.Transparent
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.feature_profile_account_management)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            // 关于
            Surface(
                onClick = { /* TODO */ },
                color = Color.Transparent
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.feature_profile_about)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

/**
 * 关于信息
 */
@Composable
private fun ProfileAboutSection() {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.feature_profile_about),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "${stringResource(Res.string.feature_profile_version)} 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 编辑资料对话框
 */
@Composable
private fun EditProfileDialog(
    state: EditProfileDialogState,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var displayName by remember { mutableStateOf(state.displayName) }
    var email by remember { mutableStateOf(state.email) }
    var avatarUrl by remember { mutableStateOf(state.avatarUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.feature_profile_edit_profile)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text(stringResource(Res.string.feature_profile_edit_display_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(Res.string.feature_profile_edit_email)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = avatarUrl,
                    onValueChange = { avatarUrl = it },
                    label = { Text(stringResource(Res.string.feature_profile_edit_avatar_url)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(displayName, email, avatarUrl) }) {
                Text(stringResource(Res.string.feature_profile_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.feature_profile_cancel))
            }
        }
    )
}

/**
 * 格式化日期为 "Jan 2024" 格式
 */
private fun formatDate(instant: kotlin.time.Instant): String {
    val epochSeconds = instant.epochSeconds
    // 计算年份和月份
    val totalDays = epochSeconds / (24 * 3600)
    val year = 1970 + (totalDays / 365.25).toInt()
    val dayOfYear = (totalDays % 365.25).toInt()
    val month = (dayOfYear / 30.44).toInt() + 1

    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    val monthName = if (month in 1..12) monthNames[month - 1] else "Jan"
    return "$monthName $year"
}


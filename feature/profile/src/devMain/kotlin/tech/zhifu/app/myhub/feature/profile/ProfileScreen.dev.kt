package tech.zhifu.app.myhub.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.theme.AppTheme
import kotlin.time.Clock

/**
 * Preview 函数 - ProfileScreen 浅色主题（有用户数据）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ProfileScreenLightPreview() {
    AppTheme(darkTheme = false) {
        val sampleUser = createSampleUser()
        val sampleStatistics = createSampleStatistics()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile") }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileHeaderCard(
                    user = sampleUser,
                    onEditClick = {}
                )

                ProfileStatsCard(
                    statistics = sampleStatistics
                )

                ProfileSettingsList(
                    onSettingsClick = {}
                )

                ProfileAboutSection()
            }
        }
    }
}

/**
 * Preview 函数 - ProfileScreen 深色主题（有用户数据）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ProfileScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        val sampleUser = createSampleUser()
        val sampleStatistics = createSampleStatistics()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("个人资料") }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileHeaderCard(
                    user = sampleUser,
                    onEditClick = {}
                )

                ProfileStatsCard(
                    statistics = sampleStatistics
                )

                ProfileSettingsList(
                    onSettingsClick = {}
                )

                ProfileAboutSection()
            }
        }
    }
}

/**
 * Preview 函数 - ProfileScreen 加载状态
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ProfileScreenLoadingPreview() {
    AppTheme(darkTheme = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile") }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/**
 * Preview 函数 - ProfileScreen 无用户状态
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ProfileScreenNoUserPreview() {
    AppTheme(darkTheme = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile") }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No user data",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/**
 * Preview 函数 - ProfileHeaderCard
 */
@Preview
@Composable
private fun ProfileHeaderCardPreview() {
    AppTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileHeaderCard(
                user = createSampleUser(),
                onEditClick = {}
            )
        }
    }
}

/**
 * Preview 函数 - ProfileStatsCard
 */
@Preview
@Composable
private fun ProfileStatsCardPreview() {
    AppTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileStatsCard(
                statistics = createSampleStatistics()
            )
        }
    }
}

/**
 * Preview 函数 - ProfileSettingsList
 */
@Preview
@Composable
private fun ProfileSettingsListPreview() {
    AppTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileSettingsList(
                onSettingsClick = {}
            )
        }
    }
}

/**
 * Preview 函数 - ProfileAboutSection
 */
@Preview
@Composable
private fun ProfileAboutSectionPreview() {
    AppTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileAboutSection()
        }
    }
}

/**
 * Preview 函数 - EditProfileDialog
 */
@Preview
@Composable
private fun EditProfileDialogPreview() {
    AppTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            EditProfileDialog(
                state = EditProfileDialogState(
                    displayName = "John Doe",
                    email = "john@example.com",
                    avatarUrl = "https://example.com/avatar.jpg"
                ),
                onDismiss = {},
                onSave = { _, _, _ -> }
            )
        }
    }
}

/**
 * 创建示例用户数据
 */
private fun createSampleUser(): User {
    val now = Clock.System.now()
    return User(
        id = "preview-user-1",
        username = "johndoe",
        email = "john@example.com",
        displayName = "John Doe",
        avatarUrl = "https://example.com/avatar.jpg",
        createdAt = now,
        preferences = null
    )
}

/**
 * 创建示例统计信息
 */
private fun createSampleStatistics(): Statistics {
    return Statistics(
        totalCards = 42,
        favoriteCards = 8,
        recentEdits = 5,
        cardsByType = emptyMap(),
        cardsByTag = emptyMap(),
        lastSyncTime = Clock.System.now().toEpochMilliseconds()
    )
}

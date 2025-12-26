package tech.zhifu.app.myhub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import tech.zhifu.app.local.LocalAppEnvironment
import tech.zhifu.app.local.LocalAppTheme
import tech.zhifu.app.local.customAppLocale
import tech.zhifu.app.local.customAppThemeIsDark
import tech.zhifu.app.myhub.config.AppBuildConfig
import tech.zhifu.app.myhub.config.BuildConfigUsage
import tech.zhifu.app.myhub.dashboard.DashboardScreen
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.navigation.AppNavigationBar
import tech.zhifu.app.myhub.navigation.AppNavigationRail
import tech.zhifu.app.myhub.navigation.Screen
import tech.zhifu.app.myhub.placeholder.PlaceholderScreen
import tech.zhifu.app.myhub.settings.SettingsManager
import tech.zhifu.app.myhub.settings.SettingsScreen
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.utils.ProvideAppLanguage
import tech.zhifu.app.myhub.ui.utils.ProvideWindowSizeClass
import tech.zhifu.app.myhub.ui.utils.WindowSizeClass
import tech.zhifu.app.myhub.ui.utils.calculateWindowSizeClass
import tech.zhifu.app.myhub.ui.utils.getWindowSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(
    windowSize: DpSize? = null
) {
    // 启动时加载配置
    LaunchedEffect(Unit) {
        // 记录当前变体配置（仅在开发环境）
        if (AppBuildConfig.enableLogging) {
            logger.info { "=== App Build Config ===" }
            logger.info { "Environment: ${AppBuildConfig.environment}" }
            logger.info { "Version Type: ${AppBuildConfig.versionType}" }
            logger.info { "API Base URL: ${AppBuildConfig.apiBaseUrl}" }
            logger.info { "App Name: ${AppBuildConfig.appName}" }
            logger.info { "Application ID Suffix: ${AppBuildConfig.applicationIdSuffix}" }
            logger.info { "Enable Logging: ${AppBuildConfig.enableLogging}" }
            logger.info { "Enable Debug Features: ${AppBuildConfig.enableDebugFeatures}" }
            logger.info { "========================" }
        }

        val config = SettingsManager.loadConfig()
        // 如果本地没有语言设置（第一次启动），尝试从服务器获取
        if (config.language == null) {
            SettingsManager.initFromServer { serverConfig ->
                // initFromServer 内部会调用 saveConfig 并同步状态
            }
        } else {
            // 如果本地有，直接同步到全局状态
            customAppLocale = config.language
            customAppThemeIsDark = config.isDarkMode
        }
    }

    LocalAppEnvironment {
        ProvideAppLanguage {
            val isDark = LocalAppTheme.current

            AppTheme(darkTheme = isDark) {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

                val actualWindowSize = windowSize ?: getWindowSize()
                val sizeClass = calculateWindowSizeClass(actualWindowSize)

                ProvideWindowSizeClass(sizeClass) {
                    when (sizeClass) {
                        WindowSizeClass.Compact -> {
                            Scaffold(
                                topBar = {
                                    TopAppBar(
                                        title = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Surface(
                                                    modifier = Modifier.size(32.dp),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = MaterialTheme.shapes.small
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoStories,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.padding(6.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = AppBuildConfig.appName,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    // 显示变体信息（仅在开发环境显示）
                                                    if (AppBuildConfig.enableDebugFeatures) {
                                                        Text(
                                                            text = BuildConfigUsage.getEnvironmentDescription(),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    )
                                },
                                bottomBar = {
                                    AppNavigationBar(
                                        currentScreen = currentScreen,
                                        onNavigate = { currentScreen = it }
                                    )
                                }
                            ) { padding ->
                                Box(modifier = Modifier.padding(padding)) {
                                    ScreenContent(currentScreen)
                                }
                            }
                        }

                        WindowSizeClass.Medium -> {
                            Row(modifier = Modifier.fillMaxSize()) {
                                AppNavigationRail(
                                    currentScreen = currentScreen,
                                    onNavigate = { currentScreen = it },
                                    isExpanded = false
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    ScreenContent(currentScreen)
                                }
                            }
                        }

                        WindowSizeClass.Expanded -> {
                            Row(modifier = Modifier.fillMaxSize()) {
                                AppNavigationRail(
                                    currentScreen = currentScreen,
                                    onNavigate = { currentScreen = it },
                                    isExpanded = true
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    ScreenContent(currentScreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenContent(screen: Screen) {
    when (screen) {
        is Screen.Dashboard -> DashboardScreen()
        is Screen.Settings -> SettingsScreen()
        else -> PlaceholderScreen(screen)
    }
}

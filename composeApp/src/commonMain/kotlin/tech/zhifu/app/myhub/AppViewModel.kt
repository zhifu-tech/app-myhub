package tech.zhifu.app.myhub

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.analytics.AnalyticsEvent
import tech.zhifu.app.myhub.analytics.AnalyticsService
import tech.zhifu.app.myhub.analytics.AnalyticsValue
import tech.zhifu.app.myhub.config.AppBuildConfig
import tech.zhifu.app.myhub.local.customAppLocale
import tech.zhifu.app.myhub.local.customAppThemeIsDark
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.navigation.Screen
import tech.zhifu.app.myhub.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.settings.settings.languageSetting
import tech.zhifu.app.myhub.settings.settings.themeSetting
import androidx.window.core.layout.WindowSizeClass

/**
 * 应用级 ViewModel
 *
 * 职责：
 * - 管理应用级状态（导航、主题、语言等）
 * - 处理应用初始化
 * - 协调各模块的状态
 *
 * 注意：WindowSizeClass 不再存储在 ViewModel 中，而是从 Composable 上下文获取
 */
class AppViewModel(
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope,
    private val analyticsService: AnalyticsService? = null
) {
    private val logger = logger("App")

    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Loading)
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var currentScreen: Screen = Screen.Dashboard
    private var isDarkTheme: Boolean = true

    init {
        // 监听主题设置变化
        observeThemeSetting()
    }

    /**
     * 初始化应用
     */
    fun initialize() {
        coroutineScope.launch {
            try {
                // 记录应用启动事件
                analyticsService?.logEvent(
                    AnalyticsEvent(
                        name = "app_started",
                        parameters = mapOf(
                            "environment" to AnalyticsValue.Str(AppBuildConfig.appEnv),
                            "version_type" to AnalyticsValue.Str(AppBuildConfig.appTier)
                        )
                    )
                )

                // 记录构建配置
                if (AppBuildConfig.enableLogging) {
                    logBuildConfig()
                }

                // 加载设置（初始值）
                loadSettings()

                // 切换到就绪状态
                _uiState.value = AppUiState.Ready(
                    currentScreen = currentScreen,
                    isDarkTheme = isDarkTheme
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to initialize app: ${e.message}" }
                _uiState.value = AppUiState.Error(e)
            }
        }
    }

    /**
     * 监听主题设置变化
     */
    private fun observeThemeSetting() {
        val themeSetting = settingsRepository.themeSetting
        themeSetting?.observe()
            ?.onStart {
                // 初始值
                coroutineScope.launch {
                    try {
                        val initialValue = themeSetting.get()
                        isDarkTheme = initialValue
                        customAppThemeIsDark = initialValue
                        updateReadyState()
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to get initial theme: ${e.message}" }
                    }
                }
            }
            ?.catch { e ->
                logger.error(e) { "Failed to observe theme setting: ${e.message}" }
            }
            ?.onEach { isDark ->
                logger.info { "Theme changed: isDark=$isDark" }
                isDarkTheme = isDark
                customAppThemeIsDark = isDark
                updateReadyState()
            }
            ?.launchIn(coroutineScope)
    }

    /**
     * 导航到指定屏幕
     */
    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            currentScreen = screen
            // 记录屏幕切换事件
            analyticsService?.setScreen(screen.route, screen::class.simpleName)
            analyticsService?.logEvent(
                AnalyticsEvent.screenView(
                    screenName = screen.route,
                    screenClass = screen::class.simpleName
                )
            )
            updateReadyState()
        }
    }

    /**
     * 切换主题
     */
    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        coroutineScope.launch {
            saveThemeSetting(isDarkTheme)
        }
        updateReadyState()
    }

    /**
     * 重试初始化
     */
    fun retry() {
        _uiState.value = AppUiState.Loading
        initialize()
    }

    /**
     * 处理窗口大小类别变化
     *
     * 当窗口大小改变时调用，可以用于：
     * - 记录分析事件
     * - 更新相关状态
     * - 日志记录
     */
    fun onWindowSizeClassChanged(windowSizeClass: WindowSizeClass) {
        coroutineScope.launch {
            try {
                // 记录窗口大小变化事件（可选）
                if (AppBuildConfig.enableLogging) {
                    logger.info {
                        "Window size class changed: " +
                        "width=${windowSizeClass.minWidthDp}, " +
                        "height=${windowSizeClass.minHeightDp}, " +
                        "minWidthDp=${windowSizeClass.minWidthDp}, " +
                        "minHeightDp=${windowSizeClass.minHeightDp}"
                    }
                }

                // 可以在这里添加其他处理逻辑，例如：
                // - 记录分析事件
                // - 更新某些状态
                // - 触发布局调整等
            } catch (e: Exception) {
                logger.error(e) { "Failed to handle window size class change: ${e.message}" }
            }
        }
    }

    private fun logBuildConfig() {
        logger.info { "=== App Build Config ===" }
        logger.info { "App Name: ${AppBuildConfig.APP_NAME}" }
        logger.info { "Enable Logging: ${AppBuildConfig.enableLogging}" }
        logger.info { "Enable Debug Features: ${AppBuildConfig.enableDebugFeatures}" }
        logger.info { "========================" }
    }

    private suspend fun loadSettings() {
        try {
            val theme = settingsRepository.themeSetting
            val language = settingsRepository.languageSetting

            isDarkTheme = theme?.get() ?: true
            val languageCode = language?.get() ?: "en"

            // 同步到全局状态（如果需要）
            customAppLocale = languageCode
            customAppThemeIsDark = isDarkTheme

            logger.info { "Settings loaded: language=$languageCode, darkMode=$isDarkTheme" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load settings: ${e.message}" }
            // 使用默认值
            isDarkTheme = true
            customAppLocale = "en"
            customAppThemeIsDark = true
        }
    }

    private suspend fun saveThemeSetting(isDark: Boolean) {
        try {
            settingsRepository.themeSetting?.set(isDark)
            customAppThemeIsDark = isDark
        } catch (e: Exception) {
            logger.error(e) { "Failed to save theme setting: ${e.message}" }
        }
    }

    private fun updateReadyState() {
        val currentState = _uiState.value
        if (currentState is AppUiState.Ready) {
            _uiState.value = AppUiState.Ready(
                currentScreen = currentScreen,
                isDarkTheme = isDarkTheme
            )
        }
    }
}


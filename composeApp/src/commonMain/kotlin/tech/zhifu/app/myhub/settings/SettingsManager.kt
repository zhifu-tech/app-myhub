package tech.zhifu.app.myhub.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import tech.zhifu.app.local.customAppLocale
import tech.zhifu.app.local.customAppThemeIsDark

object SettingsManager {
    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }
    private const val KEY_CONFIG = "app_config"

    /**
     * 加载本地配置
     */
    fun loadConfig(): AppConfig {
        val saved = settings.getStringOrNull(KEY_CONFIG)
        return if (saved != null) {
            try {
                json.decodeFromString<AppConfig>(saved)
            } catch (e: Exception) {
                AppConfig()
            }
        } else {
            AppConfig()
        }
    }

    /**
     * 保存配置并同步到全局状态
     */
    fun saveConfig(config: AppConfig) {
        settings[KEY_CONFIG] = json.encodeToString(config)
        // 同步到 Compose 状态
        customAppLocale = config.language
        customAppThemeIsDark = config.isDarkMode
    }

    /**
     * 从服务器初始化配置（如果本地不存在）
     */
    fun initFromServer(onComplete: (AppConfig) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            // TODO: 调用 Ktor 客户端从服务器获取配置
            // 模拟延迟
            delay(1000)
            val serverConfig = AppConfig(language = "en", isDarkMode = true)
            saveConfig(serverConfig)
            onComplete(serverConfig)
        }
    }
}

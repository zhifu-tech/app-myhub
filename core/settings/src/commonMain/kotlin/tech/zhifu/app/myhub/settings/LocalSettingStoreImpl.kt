package tech.zhifu.app.myhub.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 本地设置存储实现
 * 基于 multiplatform-settings 实现
 */
internal class LocalSettingStoreImpl(
    private val settings: Settings = Settings()
) : LocalSettingStore {


    override suspend fun get(key: String): String? {
        return withContext(Dispatchers.Default) {
            settings.getStringOrNull(key)
        }
    }

    override fun getSync(key: String): String? {
        return settings.getStringOrNull(key)
    }

    override suspend fun set(key: String, value: String) {
        withContext(Dispatchers.Default) {
            settings[key] = value
        }
    }

    override fun setSync(key: String, value: String) {
        settings[key] = value
    }

    override suspend fun remove(key: String) {
        withContext(Dispatchers.Default) {
            settings.remove(key)
        }
    }

    override fun removeSync(key: String) {
        settings.remove(key)
    }

    override suspend fun clear() {
        withContext(Dispatchers.Default) {
            // multiplatform-settings 没有提供 clear 方法
            // 需要记录所有键并逐个删除，或使用其他方式
            // 这里暂时不实现，因为通常不需要清空所有设置
        }
    }
}

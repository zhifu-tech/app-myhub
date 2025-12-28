package tech.zhifu.app.myhub.settings.data.store

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 本地设置存储实现
 */
class LocalSettingStoreImpl(
    private val settings: Settings = Settings()
) : LocalSettingStore {

    override suspend fun get(key: String): String? {
        return withContext(Dispatchers.Default) {
            settings.getStringOrNull(key)
        }
    }

    override suspend fun set(key: String, value: String) {
        withContext(Dispatchers.Default) {
            settings[key] = value
        }
    }

    override suspend fun remove(key: String) {
        withContext(Dispatchers.Default) {
            settings.remove(key)
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.Default) {
            // multiplatform-settings 没有提供 clear 方法，需要手动删除所有键
            // 这里暂时不实现，因为通常不需要清空所有设置
        }
    }
}


package tech.zhifu.app.myhub.feature.settings.test

import tech.zhifu.app.myhub.feature.settings.data.store.LocalSettingStore

/**
 * Mock LocalSettingStore 用于测试
 */
class MockLocalSettingStore : LocalSettingStore {
    private val storage = mutableMapOf<String, String>()

    override suspend fun get(key: String): String? {
        return storage[key]
    }

    override suspend fun set(key: String, value: String) {
        storage[key] = value
    }

    override suspend fun remove(key: String) {
        storage.remove(key)
    }

    override suspend fun clear() {
        storage.clear()
    }
}

package tech.zhifu.app.myhub.feature.settings.data.impl

import tech.zhifu.app.myhub.feature.settings.data.store.BooleanSettingSerializer
import tech.zhifu.app.myhub.feature.settings.domain.SettingScope
import tech.zhifu.app.myhub.feature.settings.test.MockLocalSettingStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * 设置仓库实现测试
 */
class SettingsRepositoryImplTest {

    @Test
    fun `test register and get setting`() {
        // Given
        val repository = SettingsRepositoryImpl()
        val mockLocalStore = MockLocalSettingStore()
        val setting = SettingImpl(
            key = "test.setting",
            scope = SettingScope.APP,
            defaultValue = true,
            localStore = mockLocalStore,
            userRepository = null,
            serializer = BooleanSettingSerializer()
        )

        // When
        repository.register(setting)
        val result = repository.get<Boolean>("test.setting")

        // Then
        assertNotNull(result)
        assertEquals("test.setting", result.key)
    }

    @Test
    fun `test get non-existent setting returns null`() {
        // Given
        val repository = SettingsRepositoryImpl()

        // When
        val result = repository.get<Boolean>("non.existent")

        // Then
        assertNull(result)
    }

    @Test
    fun `test register multiple settings`() {
        // Given
        val repository = SettingsRepositoryImpl()
        val mockLocalStore = MockLocalSettingStore()

        val setting1 = SettingImpl(
            key = "setting1",
            scope = SettingScope.APP,
            defaultValue = true,
            localStore = mockLocalStore,
            userRepository = null,
            serializer = BooleanSettingSerializer()
        )

        val setting2 = SettingImpl(
            key = "setting2",
            scope = SettingScope.APP,
            defaultValue = "default",
            localStore = mockLocalStore,
            userRepository = null,
            serializer = tech.zhifu.app.myhub.settings.data.store.StringSettingSerializer()
        )

        // When
        repository.register(setting1)
        repository.register(setting2)

        val result1 = repository.get<Boolean>("setting1")
        val result2 = repository.get<String>("setting2")

        // Then
        assertNotNull(result1)
        assertNotNull(result2)
        assertEquals("setting1", result1.key)
        assertEquals("setting2", result2.key)
    }

    @Test
    fun `test getAll returns all registered settings`() {
        // Given
        val repository = SettingsRepositoryImpl()
        val mockLocalStore = MockLocalSettingStore()

        val setting1 = SettingImpl(
            key = "setting1",
            scope = SettingScope.APP,
            defaultValue = true,
            localStore = mockLocalStore,
            userRepository = null,
            serializer = BooleanSettingSerializer()
        )

        val setting2 = SettingImpl(
            key = "setting2",
            scope = SettingScope.USER,
            defaultValue = "default",
            localStore = mockLocalStore,
            userRepository = null,
            serializer = tech.zhifu.app.myhub.settings.data.store.StringSettingSerializer()
        )

        // When
        repository.register(setting1)
        repository.register(setting2)
        val allSettings = repository.getAll()

        // Then
        assertEquals(2, allSettings.size)
        assertEquals(setOf("setting1", "setting2"), allSettings.map { it.key }.toSet())
    }

    @Test
    fun `test register overwrites existing setting`() {
        // Given
        val repository = SettingsRepositoryImpl()
        val mockLocalStore = MockLocalSettingStore()

        val setting1 = SettingImpl(
            key = "test.setting",
            scope = SettingScope.APP,
            defaultValue = true,
            localStore = mockLocalStore,
            userRepository = null,
            serializer = BooleanSettingSerializer()
        )

        val setting2 = SettingImpl(
            key = "test.setting",
            scope = SettingScope.APP,
            defaultValue = false,
            localStore = mockLocalStore,
            userRepository = null,
            serializer = BooleanSettingSerializer()
        )

        // When
        repository.register(setting1)
        repository.register(setting2)
        val result = repository.get<Boolean>("test.setting")

        // Then
        assertNotNull(result)
        assertEquals(false, result.defaultValue) // Should be the second setting
    }
}


# Settings 模块架构设计

## 📋 概述

Settings 模块负责管理应用的所有设置项，包括应用级设置（如默认语言、主题）和用户级设置（如用户偏好、个性化配置）。本模块采用分层架构设计，支持多数据源优先级，确保设置的灵活性和可扩展性。

## 🎯 设计目标

1. **单一职责**：每个设置项独立管理，职责清晰
2. **类型安全**：使用泛型接口，编译期类型检查
3. **响应式**：基于 Flow/StateFlow 的响应式更新
4. **多数据源**：支持系统默认、本地存储、用户偏好等多数据源
5. **可扩展**：易于添加新的设置项，无需修改现有代码
6. **测试友好**：每个设置项可独立测试

## 🏗️ 架构设计

### 分层架构

```text
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  SettingsScreen, ThemeToggle, LanguageSelector, etc.    │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Settings Repository Layer                   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  SettingsRepository                              │   │
│  │  - register(setting: Setting<T>)                 │   │
│  │  - get<T>(key: String): Setting<T>?             │   │
│  │  - getAll(): List<Setting<*>>                    │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Setting Interface Layer                    │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Setting<T> (泛型接口)                            │   │
│  │  - observe(): Flow<T>                           │   │
│  │  - get(): T                                     │   │
│  │  - set(value: T)                                │   │
│  │  - reset()                                      │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
┌───────▼──────┐ ┌──▼──────────┐ ┌──────────▼──────────┐
│  Local Store │ │ User Prefs  │ │  System Defaults    │
│  (Settings)  │ │ (Repository)│ │  (Hardcoded)        │
└──────────────┘ └─────────────┘ └─────────────────────┘
```

### 数据源优先级

设置值的解析遵循以下优先级（从高到低）：

1. **运行时覆盖** (RUNTIME) - 内存中的临时值
2. **用户偏好** (USER_PREFERENCE) - 从 UserRepository 获取的用户设置
3. **应用默认** (APP_DEFAULT) - 本地存储的应用级设置
4. **系统默认** (SYSTEM_DEFAULT) - 硬编码的默认值

## 📦 核心组件

### 1. 设置作用域 (SettingScope)

```kotlin
/**
 * 设置项作用域
 */
enum class SettingScope {
    APP,      // 应用级设置（所有用户共享）
    USER,     // 用户级设置（每个用户独立）
    SESSION   // 会话级设置（运行时临时，不持久化）
}
```

### 2. 设置项接口 (Setting)

```kotlin
/**
 * 设置项接口（泛型，类型安全）
 */
interface Setting<T> {
    val key: String
    val scope: SettingScope
    val defaultValue: T

    /**
     * 获取当前值（响应式）
     */
    fun observe(): Flow<T>

    /**
     * 获取当前值（同步）
     */
    suspend fun get(): T

    /**
     * 设置值
     */
    suspend fun set(value: T)

    /**
     * 重置为默认值
     */
    suspend fun reset()
}
```

### 3. 设置仓库 (SettingsRepository)

```kotlin
/**
 * 设置仓库接口
 * 负责注册和管理所有设置项
 */
interface SettingsRepository {
    /**
     * 注册设置项
     */
    fun <T> register(setting: Setting<T>)

    /**
     * 获取设置项
     */
    fun <T> get(key: String): Setting<T>?

    /**
     * 获取所有设置项
     */
    fun getAll(): List<Setting<*>>
}
```

### 4. 本地存储 (LocalSettingStore)

```kotlin
/**
 * 本地设置存储接口
 * 使用 multiplatform-settings 实现跨平台存储
 */
interface LocalSettingStore {
    suspend fun get(key: String): String?
    suspend fun set(key: String, value: String)
    suspend fun remove(key: String)
    suspend fun clear()
}
```

## 📁 模块结构

```text
feature/settings/
├── domain/
│   ├── Setting.kt                    # 设置项接口
│   ├── SettingScope.kt              # 设置作用域枚举
│   └── SettingsRepository.kt         # 设置仓库接口
│
├── data/
│   ├── store/
│   │   ├── LocalSettingStore.kt      # 本地存储接口
│   │   └── LocalSettingStoreImpl.kt  # 本地存储实现
│   │
│   ├── resolver/
│   │   └── SettingValueResolver.kt   # 值解析器（多数据源优先级）
│   │
│   └── impl/
│       ├── SettingImpl.kt            # 设置项实现
│       └── SettingsRepositoryImpl.kt # 仓库实现
│
├── settings/                         # 具体设置项实现
│   ├── ThemeSetting.kt              # 主题设置
│   ├── LanguageSetting.kt           # 语言设置
│   ├── AutoSyncSetting.kt           # 自动同步设置
│   └── ...                          # 其他设置项
│
└── ui/
    ├── SettingsViewModel.kt          # ViewModel
    ├── SettingsUiState.kt            # UI 状态
    └── SettingsScreen.kt             # UI 组件
```

## 🔧 实现细节

### 设置值解析器

设置值解析器负责按照优先级从多个数据源获取值：

```kotlin
class SettingValueResolver<T>(
    private val key: String,
    private val scope: SettingScope,
    private val defaultValue: T,
    private val localStore: LocalSettingStore,
    private val userRepository: UserRepository?,
    private val serializer: SettingSerializer<T>
) {
    suspend fun resolve(): T {
        return when (scope) {
            SettingScope.USER -> {
                // 1. 尝试从用户偏好获取
                userRepository?.getCurrentUser()?.preferences?.let {
                    extractFromUserPreferences(it)
                }
                // 2. 尝试从本地存储获取
                ?: localStore.get(key)?.let { serializer.deserialize(it) }
                // 3. 使用默认值
                ?: defaultValue
            }
            SettingScope.APP -> {
                // 1. 从本地存储获取
                localStore.get(key)?.let { serializer.deserialize(it) }
                // 2. 使用默认值
                ?: defaultValue
            }
            SettingScope.SESSION -> {
                // 仅从内存获取（不持久化）
                defaultValue
            }
        }
    }
}
```

### 具体设置项示例

#### 主题设置 (ThemeSetting)

```kotlin
/**
 * 主题设置
 * 作用域：USER（用户级设置）
 * 数据源：用户偏好 > 本地存储 > 默认值（深色模式）
 */
class ThemeSetting(
    localStore: LocalSettingStore,
    userRepository: UserRepository?
) : Setting<Boolean> {
    override val key = "theme.is_dark"
    override val scope = SettingScope.USER
    override val defaultValue = true

    private val setting = SettingImpl(
        key = key,
        scope = scope,
        defaultValue = defaultValue,
        localStore = localStore,
        userRepository = userRepository,
        serializer = BooleanSettingSerializer()
    )

    override fun observe(): Flow<Boolean> = setting.observe()
    override suspend fun get(): Boolean = setting.get()

    override suspend fun set(value: Boolean) {
        setting.set(value)
        // 同步到用户偏好
        userRepository?.getCurrentUser()?.let { user ->
            val updatedPrefs = user.preferences?.copy(
                theme = if (value) "dark" else "light"
            ) ?: UserPreferences(theme = if (value) "dark" else "light")
            userRepository.updateUser(user.copy(preferences = updatedPrefs))
        }
    }

    override suspend fun reset() = setting.reset()
}
```

#### 语言设置 (LanguageSetting)

```kotlin
/**
 * 语言设置
 * 作用域：USER（用户级设置）
 * 数据源：用户偏好 > 本地存储 > 默认值（英语）
 */
class LanguageSetting(
    localStore: LocalSettingStore,
    userRepository: UserRepository?
) : Setting<String> {
    override val key = "language.code"
    override val scope = SettingScope.USER
    override val defaultValue = "en"

    // 类似实现...
}
```

## 💡 使用示例

### 1. 初始化设置系统

```kotlin
// 在 Koin 模块中注册
val settingsModule = module {
    // 本地存储
    single<LocalSettingStore> { LocalSettingStoreImpl(get()) }

    // 设置仓库
    single<SettingsRepository> {
        val repository = SettingsRepositoryImpl()
        val localStore = get<LocalSettingStore>()
        val userRepository = getOrNull<UserRepository>()

        // 注册设置项
        repository.register(ThemeSetting(localStore, userRepository))
        repository.register(LanguageSetting(localStore, userRepository))
        repository.register(AutoSyncSetting(localStore, userRepository))

        repository
    }
}
```

### 2. 在 ViewModel 中使用

```kotlin
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) {
    private val themeSetting = settingsRepository.get<Boolean>("theme.is_dark")
    private val languageSetting = settingsRepository.get<String>("language.code")

    val uiState = combine(
        themeSetting?.observe() ?: flowOf(true),
        languageSetting?.observe() ?: flowOf("en")
    ) { theme, language ->
        SettingsUiState(
            isDarkMode = theme,
            currentLanguage = Language.entries.find { it.code == language }
                ?: Language.English
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun updateTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeSetting?.set(isDark)
        }
    }

    fun updateLanguage(language: Language) {
        viewModelScope.launch {
            languageSetting?.set(language.code)
        }
    }
}
```

### 3. 在 UI 中使用

```kotlin
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        // 主题设置
        Switch(
            checked = uiState.isDarkMode,
            onCheckedChange = { viewModel.updateTheme(it) }
        )

        // 语言设置
        LanguageSelector(
            currentLanguage = uiState.currentLanguage,
            onLanguageSelected = { viewModel.updateLanguage(it) }
        )
    }
}
```

## 🔄 数据同步策略

### 用户级设置同步

当设置项的作用域为 `SettingScope.USER` 时：

1. **设置值**：同时保存到本地存储和用户偏好（UserRepository）
2. **获取值**：优先从用户偏好获取，其次从本地存储获取
3. **同步时机**：
   - 设置值时立即同步到服务器
   - 应用启动时从服务器拉取最新设置

### 应用级设置

当设置项的作用域为 `SettingScope.APP` 时：

1. **设置值**：仅保存到本地存储
2. **获取值**：从本地存储获取，不存在则使用默认值
3. **同步时机**：无需同步到服务器

## 📊 设置项分类

### 用户级设置 (USER)

- **主题设置** (`theme.is_dark`) - 深色/浅色模式
- **语言设置** (`language.code`) - 显示语言
- **自动同步** (`sync.auto`) - 是否自动同步
- **同步间隔** (`sync.interval`) - 同步间隔时间

### 应用级设置 (APP)

- **默认卡片类型** (`card.default_type`) - 创建卡片时的默认类型
- **搜索历史** (`search.history_enabled`) - 是否保存搜索历史
- **通知设置** (`notification.enabled`) - 是否启用通知

### 会话级设置 (SESSION)

- **当前视图模式** (`view.mode`) - 列表/网格视图
- **筛选条件** (`filter.active`) - 当前激活的筛选条件

## 🧪 测试

### 单元测试示例

```kotlin
class ThemeSettingTest {
    @Test
    fun `should return default value when no value is set`() = runTest {
        val setting = ThemeSetting(mockLocalStore, null)
        assertEquals(true, setting.get())
    }

    @Test
    fun `should return value from local store`() = runTest {
        val localStore = mockLocalStore {
            on { get("theme.is_dark") } doReturn "false"
        }
        val setting = ThemeSetting(localStore, null)
        assertEquals(false, setting.get())
    }

    @Test
    fun `should prioritize user preference over local store`() = runTest {
        val user = User(
            id = "1",
            username = "test",
            preferences = UserPreferences(theme = "light")
        )
        val userRepository = mockUserRepository {
            on { getCurrentUser() } doReturn user
        }
        val setting = ThemeSetting(mockLocalStore, userRepository)
        assertEquals(false, setting.get())
    }
}
```

## 🚀 迁移计划

### 阶段 1：基础架构搭建

- [x] 定义 Setting 接口和 SettingScope
- [x] 实现 LocalSettingStore
- [x] 实现 SettingsRepository
- [ ] 实现 SettingValueResolver

### 阶段 2：核心设置项迁移

- [ ] 迁移主题设置 (ThemeSetting)
- [ ] 迁移语言设置 (LanguageSetting)
- [ ] 更新 SettingsViewModel 使用新架构

### 阶段 3：扩展设置项

- [ ] 添加自动同步设置
- [ ] 添加其他用户偏好设置
- [ ] 添加应用级设置

### 阶段 4：清理旧代码

- [x] 移除 SettingsManager
- [x] 移除 AppConfig
- [x] 更新所有引用

## 📚 参考

### 业内实践

- **Android**: DataStore, SharedPreferences
- **iOS**: UserDefaults, Settings Bundle
- **Flutter**: shared_preferences + Provider/Riverpod
- **React Native**: AsyncStorage + Context/Redux

### 设计原则

- **单一职责原则**：每个设置项独立管理
- **开闭原则**：对扩展开放，对修改关闭
- **依赖倒置原则**：依赖抽象接口而非具体实现
- **接口隔离原则**：细粒度的接口设计

## 🔗 相关文档

- [项目整体架构](../../docs/myhub_architecture.md)
- [数据层架构](../../core/datastore/docs/datastore_architecture.md)
- [用户偏好设计](../../core/datastore-database/docs/user_association_design.md)

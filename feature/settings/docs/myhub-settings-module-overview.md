# Feature Settings Module

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
├── src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   └── tech/zhifu/app/myhub/settings/
│   │   │       ├── SettingsScreen.kt          # UI 组件
│   │   │       ├── SettingsViewModel.kt        # ViewModel
│   │   │       ├── SettingsUiState.kt          # UI 状态
│   │   │       ├── settings/                   # 设置项实现
│   │   │       ├── data/                       # 数据层
│   │   │       ├── domain/                     # 领域层
│   │   │       └── di/
│   │   │           └── SettingsModule.kt      # 依赖注入模块
│   │   └── composeResources/
│   │       └── values/
│   │           ├── strings.xml                 # 默认字符串资源
│   │           ├── values-zh-rCN/
│   │           │   └── strings.xml            # 简体中文
│   │           ├── values-zh-rTW/
│   │           │   └── strings.xml            # 繁体中文
│   │           └── values-ja/
│   │               └── strings.xml             # 日语
│   └── devMain/                                # 预览支持
│       └── kotlin/
│           └── tech/zhifu/app/myhub/settings/
│               └── SettingsScreen.dev.kt       # Preview 函数
├── build.gradle.kts                            # 构建配置
└── README.md                                   # 本文档
```

## 🧪 Preview 支持

Settings 模块提供了完整的 Preview 支持，位于 `devMain` source set 中。

### Preview 文件结构

```text
src/devMain/kotlin/tech/zhifu/app/myhub/settings/
└── SettingsScreen.dev.kt
```

### Preview 特性

Preview 文件包含以下预览场景：

#### SettingsScreen 完整预览

- **浅色主题** - 展示 Settings 在浅色主题下的外观
- **深色主题** - 展示 Settings 在深色主题下的外观

### 使用 Preview

在 Android Studio 或 IntelliJ IDEA 中：

1. 打开 `SettingsScreen.dev.kt` 文件
2. 点击 Preview 函数左侧的预览图标
3. 查看 Settings 在不同主题下的外观

### Preview 环境初始化

Preview 文件使用示例数据，无需额外的依赖初始化。

## 📁 模块结构（旧版）

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
│   │   ├── LocalSettingStoreImpl.kt  # 本地存储实现
│   │   └── SettingSerializer.kt      # 设置值序列化器
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
│   └── ...                          # 其他设置项
│
├── di/
│   └── SettingsModule.kt            # Koin 依赖注入模块
│
├── SettingsViewModel.kt              # ViewModel
├── SettingsUiState.kt                # UI 状态
└── SettingsScreen.kt                 # UI 组件
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
    private val serializer: SettingSerializer<T>,
    private val userPreferenceExtractor: ((UserPreferences) -> T?)? = null
) {
    /**
     * 解析设置值
     */
    suspend fun resolve(): T {
        return when (scope) {
            SettingScope.USER -> {
                // 1. 尝试从用户偏好获取
                userRepository?.getCurrentUser()?.preferences?.let { prefs ->
                    userPreferenceExtractor?.invoke(prefs)
                }
                // 2. 尝试从本地存储获取
                    ?: localStore.get(key)?.let {
                        serializer.deserialize(it)
                    }
                    // 3. 使用默认值
                    ?: defaultValue
            }

            SettingScope.APP -> {
                // 1. 从本地存储获取
                localStore.get(key)?.let {
                    serializer.deserialize(it)
                }
                // 2. 使用默认值
                    ?: defaultValue
            }

            SettingScope.SESSION -> {
                // 仅从内存获取（不持久化）
                defaultValue
            }
        }
    }

    /**
     * 保存设置值到本地存储
     */
    suspend fun saveToLocal(value: T) {
        when (scope) {
            SettingScope.USER, SettingScope.APP -> {
                localStore.set(key, serializer.serialize(value))
            }
            SettingScope.SESSION -> {
                // 会话级设置不持久化
            }
        }
    }
}
```

### 设置项实现 (SettingImpl)

`SettingImpl` 是 `Setting<T>` 接口的通用实现，负责管理单个设置项的生命周期：

```kotlin
class SettingImpl<T>(
    override val key: String,
    override val scope: SettingScope,
    override val defaultValue: T,
    private val localStore: LocalSettingStore,
    private val userRepository: UserRepository?,
    private val serializer: SettingSerializer<T>,
    private val userPreferenceExtractor: ((UserPreferences) -> T?)? = null,
    private val userPreferenceUpdater: ((UserPreferences, T) -> UserPreferences)? = null
) : Setting<T> {
    private val resolver = SettingValueResolver(...)
    private val _value = MutableStateFlow<T>(defaultValue)
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isInitialized = false

    override fun observe(): StateFlow<T> {
        // 延迟加载：首次观察时异步加载值
        if (!isInitialized) {
            coroutineScope.launch {
                try {
                    _value.value = resolver.resolve()
                    isInitialized = true
                } catch (e: Exception) {
                    _value.value = defaultValue
                    isInitialized = true
                }
            }
        }
        return _value.asStateFlow()
    }

    override suspend fun get(): T {
        val currentValue = resolver.resolve()
        _value.value = currentValue
        isInitialized = true
        return currentValue
    }

    override suspend fun set(value: T) {
        // 保存到本地存储
        resolver.saveToLocal(value)

        // 如果作用域是 USER，同步到用户偏好
        if (scope == SettingScope.USER && userPreferenceUpdater != null) {
            userRepository?.getCurrentUser()?.let { user ->
                val updatedPrefs = userPreferenceUpdater.invoke(
                    user.preferences ?: UserPreferences(),
                    value
                )
                userRepository.updateUser(user.copy(preferences = updatedPrefs))
            }
        }

        // 更新状态流
        _value.update { value }
    }
}
```

**关键特性**：

- **延迟初始化**：值在首次 `observe()` 或 `get()` 调用时异步加载，避免在构造函数中调用 suspend 函数
- **响应式更新**：使用 `StateFlow` 提供响应式更新
- **多数据源支持**：通过 `SettingValueResolver` 从多个数据源获取值
- **自动同步**：USER 作用域的设置会自动同步到用户偏好

### 具体设置项示例

#### 主题设置 (ThemeSetting)

```kotlin
/**
 * 主题设置
 * 作用域：USER（用户级设置）
 * 数据源：用户偏好 > 本地存储 > 默认值（深色模式）
 */
internal class ThemeSetting(
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
        serializer = BooleanSettingSerializer(),
        userPreferenceExtractor = { prefs ->
            when (prefs.theme.lowercase()) {
                "dark" -> true
                "light" -> false
                else -> null // 返回 null 会继续查找下一个数据源
            }
        },
        userPreferenceUpdater = { prefs, value ->
            prefs.copy(theme = if (value) "dark" else "light")
        }
    )

    override fun observe() = setting.observe()
    override suspend fun get() = setting.get()
    override suspend fun set(value: Boolean) = setting.set(value)
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

    private val setting = SettingImpl(
        key = key,
        scope = scope,
        defaultValue = defaultValue,
        localStore = localStore,
        userRepository = userRepository,
        serializer = StringSettingSerializer(),
        userPreferenceExtractor = { prefs ->
            prefs.language.takeIf { it.isNotBlank() }
        },
        userPreferenceUpdater = { prefs, value ->
            prefs.copy(language = value)
        }
    )

    override fun observe() = setting.observe()
    override suspend fun get() = setting.get()
    override suspend fun set(value: String) = setting.set(value)
    override suspend fun reset() = setting.reset()
}
```

## 💡 使用示例

### 1. 初始化设置系统

```kotlin
// 在 Koin 模块中注册（feature/settings/src/commonMain/kotlin/tech/zhifu/app/myhub/settings/di/SettingsModule.kt）
fun settingsModule() = module {
    // 本地设置存储
    single<LocalSettingStore> {
        LocalSettingStoreImpl()
    }

    // 设置仓库（注册所有设置项）
    single<SettingsRepository> {
        val repository = SettingsRepositoryImpl()
        val localStore = get<LocalSettingStore>()
        val userRepository: UserRepository? = try {
            getOrNull<UserRepository>()
        } catch (e: Exception) {
            null
        }

        // 注册设置项
        repository.register(ThemeSetting(localStore, userRepository))
        repository.register(LanguageSetting(localStore, userRepository))

        repository
    }

    // Settings ViewModel
    factory {
        SettingsViewModel(
            coroutineScope = get(),
            settingsRepository = get()
        )
    }
}
```

### 2. 在 ViewModel 中使用

```kotlin
class SettingsViewModel(
    private val coroutineScope: CoroutineScope,
    private val settingsRepository: SettingsRepository
) {
    private val themeSetting = settingsRepository.get<Boolean>("theme.is_dark")
    private val languageSetting = settingsRepository.get<String>("language.code")
    private val _showLanguageDialog = MutableStateFlow(false)

    /**
     * UI 状态（响应式）
     * 自动从设置项 Flow 中组合生成
     */
    val uiState: StateFlow<SettingsUiState> = combine(
        themeSetting?.observe() ?: flowOf(true),
        languageSetting?.observe() ?: flowOf("en"),
        _showLanguageDialog
    ) { isDark, languageCode, showDialog ->
        val currentLanguage = languageCode.toLanguage()

        // 同步到全局状态
        customAppLocale = languageCode
        customAppThemeIsDark = isDark

        SettingsUiState(
            currentLanguage = currentLanguage,
            isDarkMode = isDark,
            isLoading = false,
            error = null,
            showLanguageDialog = showDialog
        )
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    init {
        // 初始化时加载设置值
        coroutineScope.launch {
            try {
                themeSetting?.get()
                languageSetting?.get()
            } catch (e: Exception) {
                // 错误处理
            }
        }
    }

    fun updateTheme(isDarkMode: Boolean) {
        coroutineScope.launch {
            themeSetting?.set(isDarkMode)
        }
    }

    fun updateLanguage(language: Language) {
        coroutineScope.launch {
            languageSetting?.set(language.code)
            // 同步更新全局状态
            customAppLocale = language.code
        }
    }
}
```

### 3. 在 UI 中使用

```kotlin
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = koinInject()
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

### 4. 在应用启动时加载设置

```kotlin
@Composable
fun App(
    settingsRepository: SettingsRepository = koinInject<SettingsRepository>()
) {
    LaunchedEffect(Unit) {
        // 使用新的设置架构加载配置
        val themeSetting = settingsRepository.get<Boolean>("theme.is_dark")
        val languageSetting = settingsRepository.get<String>("language.code")

        try {
            // 加载设置值（会自动从多个数据源获取）
            val isDarkMode = themeSetting?.get() ?: true
            val languageCode = languageSetting?.get() ?: "en"

            // 同步到全局状态
            customAppLocale = languageCode
            customAppThemeIsDark = isDarkMode
        } catch (e: Exception) {
            // 错误处理，使用默认值
            customAppLocale = "en"
            customAppThemeIsDark = true
        }
    }

    // ... 应用 UI
}
```

## 🔄 数据同步策略

### 用户级设置同步

当设置项的作用域为 `SettingScope.USER` 时：

1. **设置值**：
    - 保存到本地存储（通过 `resolver.saveToLocal()`）
    - 同步到用户偏好（通过 `userPreferenceUpdater` 更新 `UserRepository`）
    - 更新内存中的 `StateFlow` 值
2. **获取值**：按照优先级顺序：
    - 优先从用户偏好获取（通过 `userPreferenceExtractor`）
    - 其次从本地存储获取
    - 最后使用默认值
3. **同步时机**：
    - 设置值时立即同步到 `UserRepository`（会触发服务器同步）
    - 应用启动时通过 `resolver.resolve()` 从用户偏好或本地存储加载

### 应用级设置

当设置项的作用域为 `SettingScope.APP` 时：

1. **设置值**：仅保存到本地存储
2. **获取值**：从本地存储获取，不存在则使用默认值
3. **同步时机**：无需同步到服务器，仅本地存储

### 会话级设置

当设置项的作用域为 `SettingScope.SESSION` 时：

1. **设置值**：仅更新内存中的 `StateFlow`，不持久化
2. **获取值**：从内存获取，不存在则使用默认值
3. **同步时机**：无需同步，应用重启后恢复默认值

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

Settings 模块包含完整的单元测试套件，覆盖所有核心组件和设置项。

### 测试结构

```text
feature/settings/src/commonTest/kotlin/tech/zhifu/app/myhub/settings/
├── data/
│   ├── impl/
│   │   ├── SettingImplTest.kt              # 设置项实现测试
│   │   └── SettingsRepositoryImplTest.kt    # 设置仓库测试
│   ├── resolver/
│   │   └── SettingValueResolverTest.kt     # 值解析器测试
│   └── store/
│       └── SettingSerializerTest.kt         # 序列化器测试
├── settings/
│   ├── LanguageSettingTest.kt              # 语言设置测试
│   └── ThemeSettingTest.kt                 # 主题设置测试
└── test/
    └── MockHelpers.kt                       # Mock 工具类
```

### 测试覆盖范围

#### 核心组件测试

**SettingSerializerTest** - 序列化器测试

- ✅ Boolean、String、Int、Long 类型的序列化/反序列化
- ✅ 往返测试（序列化后反序列化应得到原值）
- ✅ 错误处理（无效值）

**SettingValueResolverTest** - 值解析器测试

- ✅ USER 作用域：用户偏好 > 本地存储 > 默认值
- ✅ APP 作用域：本地存储 > 默认值
- ✅ SESSION 作用域：仅返回默认值
- ✅ `saveToLocal()` 方法测试（不同作用域的行为）

**SettingImplTest** - 设置项实现测试

- ✅ `get()` 方法：从多个数据源获取值
- ✅ `set()` 方法：保存值并更新 StateFlow
- ✅ `observe()` 方法：响应式更新
- ✅ `reset()` 方法：重置为默认值
- ✅ USER 作用域：自动同步到用户偏好
- ✅ SESSION 作用域：不持久化

**SettingsRepositoryImplTest** - 设置仓库测试

- ✅ 注册和获取设置项
- ✅ 获取不存在的设置项返回 null
- ✅ 注册多个设置项
- ✅ `getAll()` 方法
- ✅ 覆盖已存在的设置项

#### 具体设置项测试

**ThemeSettingTest** - 主题设置测试

- ✅ 默认值（深色模式）
- ✅ 从用户偏好获取（dark/light）
- ✅ 从本地存储获取
- ✅ 设置值并更新用户偏好
- ✅ 重置功能

**LanguageSettingTest** - 语言设置测试

- ✅ 默认值（英语）
- ✅ 从用户偏好获取
- ✅ 从本地存储获取
- ✅ 空白用户偏好的回退
- ✅ 设置值并更新用户偏好

### 测试工具

**MockHelpers** - 共享的 Mock 对象

- `MockLocalSettingStore` - Mock 本地存储实现
- `MockUserRepository` - Mock 用户仓库实现
- `createTestUser()` - 创建测试用户的辅助函数

### 运行测试

```bash
# 运行所有平台的测试
./gradlew :feature:settings:allTests

# 运行特定平台的测试
./gradlew :feature:settings:jvmTest
./gradlew :feature:settings:jsTest
./gradlew :feature:settings:iosSimulatorArm64Test
```

### 测试统计

- **总测试数**：50+ 个测试用例
- **测试覆盖**：所有核心组件和设置项
- **平台支持**：JVM、JS、iOS（所有平台测试通过）

### 单元测试示例

```kotlin
class ThemeSettingTest {
    @Test
    fun `test default value is true`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = ThemeSetting(mockLocalStore, null)

        // When
        val result = setting.get()

        // Then
        assertEquals(true, result)
    }

    @Test
    fun `test get from user preference - dark theme`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val mockUserRepository = MockUserRepository(
            user = createTestUser(
                preferences = UserPreferences(theme = "dark")
            )
        )
        val setting = ThemeSetting(mockLocalStore, mockUserRepository)

        // When
        val result = setting.get()

        // Then
        assertTrue(result)
    }

    @Test
    fun `test set updates user preference`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        var updatedUser: User? = null
        val mockUserRepository = object : UserRepository {
            override suspend fun getCurrentUser(): User? = createTestUser(
                preferences = UserPreferences()
            )
            override suspend fun updateUser(user: User): User {
                updatedUser = user
                return user
            }
        }
        val setting = ThemeSetting(mockLocalStore, mockUserRepository)

        // When
        setting.set(true)

        // Then
        assertNotNull(updatedUser)
        assertEquals("dark", updatedUser.preferences?.theme)
    }
}
```

## 🚀 落地计划

### 阶段 1：基础架构搭建 ✅

- [x] 定义 Setting 接口和 SettingScope
- [x] 实现 LocalSettingStore
- [x] 实现 SettingsRepository
- [x] 实现 SettingValueResolver
- [x] 实现 SettingImpl（延迟加载机制）

### 阶段 2：核心设置项迁移 ✅

- [x] 迁移主题设置 (ThemeSetting)
- [x] 迁移语言设置 (LanguageSetting)
- [x] 更新 SettingsViewModel 使用新架构
- [x] 更新 App.kt 使用新架构加载设置

### 阶段 3：扩展设置项 🔄

- [ ] 添加自动同步设置
- [ ] 添加其他用户偏好设置
- [ ] 添加应用级设置（如默认卡片类型、搜索历史等）

### 阶段 4：清理旧代码 ✅

- [x] 移除 SettingsManager
- [x] 移除 AppConfig
- [x] 更新所有引用
- [x] 更新 Koin 依赖注入配置

### 阶段 5：单元测试 ✅

- [x] 添加 SettingSerializerTest（序列化器测试）
- [x] 添加 SettingValueResolverTest（值解析器测试）
- [x] 添加 SettingImplTest（设置项实现测试）
- [x] 添加 SettingsRepositoryImplTest（设置仓库测试）
- [x] 添加 ThemeSettingTest（主题设置测试）
- [x] 添加 LanguageSettingTest（语言设置测试）
- [x] 创建 MockHelpers（测试工具类）
- [x] 所有平台测试通过（JVM、JS、iOS）

## 📝 当前状态

**已完成**：

- ✅ 基础架构已完全实现并投入使用
- ✅ 主题和语言设置已迁移到新架构
- ✅ SettingsViewModel 和 SettingsScreen 已更新
- ✅ 应用启动时自动加载设置
- ✅ 设置值自动同步到用户偏好（USER 作用域）
- ✅ **完整的单元测试套件（50+ 测试用例）**
- ✅ **所有平台测试通过（JVM、JS、iOS）**
- ✅ Preview 支持（devMain source set）

**待扩展**：

- 🔄 添加更多设置项（自动同步、通知设置等）
- 🔄 添加应用级设置（APP 作用域）
- 🔄 添加会话级设置（SESSION 作用域）

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
- [数据层架构](../../datastore/docs/myhub-datastore-architecture.md)
- [用户偏好设计](../../datastore/database/docs/user_association_design.md)

# KMP 应用多语言支持技术方案

## 📋 概述

本文档描述了基于 Compose Multiplatform Resources 官方方案的 KMP 应用多语言支持技术方案。该方案采用官方推荐的 `Environment` API 来管理语言环境，支持运行时语言切换，并提供跨平台统一的实现方式。

> 📖 **Environment API 官方文档**：  
> [管理本地资源环境 (Compose Resource Environment)](https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html)

### ⚠️ 重要说明：官方 API vs 自定义封装

本文档中提到的代码实现包含两部分：

1. **官方 API**（必须使用）：
   - `org.jetbrains.compose.resources.Environment`：官方环境管理类
   - `org.jetbrains.compose.resources.LocalEnvironment`：官方 CompositionLocal
   - 这些是 Compose Resources 提供的官方 API，必须使用

2. **自定义封装类**（可选实现）：
   - `AppLocaleManager`：建议的自定义封装类，用于简化 Environment 的使用
   - `AppWithLocale`：建议的自定义 Composable，用于简化环境注入
   - 这些是**建议的实现示例**，您也可以直接使用官方 API，不需要这些封装类

**您可以选择**：
- ✅ 直接使用官方 API（`Environment` + `LocalEnvironment`）
- ✅ 使用文档中建议的自定义封装类（`AppLocaleManager` + `AppWithLocale`）
- ✅ 创建自己的封装类

## 🎯 技术方案概述

### 核心原则

1. **官方方案优先**：采用 Compose Multiplatform Resources 官方 API
2. **类型安全**：利用编译时生成的资源类，避免运行时错误
3. **跨平台统一**：使用统一的 API，各平台实现一致
4. **运行时切换**：支持应用内语言切换和系统语言跟随

### 技术栈

- **Compose Multiplatform Resources**：官方资源管理框架
- **Environment API**：运行时环境变量管理
- **CompositionLocal**：Compose 上下文传递机制
- **Multiplatform Settings**：语言偏好持久化存储

## 🏗️ 核心框架支持

### 1. Compose Resources 官方方案

Compose Multiplatform 提供了完整的资源管理解决方案，核心组件包括：

- `compose.components.resources`：资源管理库（必需依赖）
- **`Environment`**：运行时环境变量管理 API
  - 官方文档：[管理本地资源环境](https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html)
  - 用于设置语言、主题等运行时环境变量
- `stringResource()`：字符串资源访问函数
- `ResourceReader`：资源读取器接口
- `LocalEnvironment`：CompositionLocal，用于传递环境变量

### 2. 资源文件结构

遵循 Android 资源目录命名规范，支持语言和地区代码：

```
composeApp/src/commonMain/composeResources/
├── values/
│   └── strings.xml                    # 默认语言（英语）
├── values-zh-rCN/
│   └── strings.xml                    # 简体中文（中国大陆）
├── values-zh-rTW/
│   └── strings.xml                    # 繁体中文（台湾）
├── values-ja/
│   └── strings.xml                    # 日语
└── values-{locale}/                   # 其他语言
    └── strings.xml
```

#### 命名规则

- `values`：默认语言（英语），当找不到匹配语言时使用
- `values-{language}`：仅语言代码（如 `values-ja` 表示日语）
- `values-{language}-r{region}`：语言+地区代码（如 `values-zh-rCN` 表示简体中文）

#### 语言代码规范

使用 **BCP 47** 语言标签标准：

| 语言     | 代码    | 说明     |
| -------- | ------- | -------- |
| 英语     | `en`    | 默认语言 |
| 简体中文 | `zh-CN` | 中国大陆 |
| 繁体中文 | `zh-TW` | 台湾地区 |
| 日语     | `ja`    | 日本     |

## 🔧 实现方案

### 1. Gradle 配置

在 `build.gradle.kts` 中配置 Compose Resources（已配置）：

```kotlin
compose.resources {
    publicResClass = true
    packageOfResClass = "tech.zhifu.app.myhub.resources"
    generateResClass = always
}
```

**配置说明：**

- `publicResClass = true`：生成公共资源类，可在模块间共享
- `packageOfResClass`：指定生成的资源类包名
- `generateResClass = always`：始终生成资源类（即使资源未变化）

**必需依赖：**

```kotlin
commonMain.dependencies {
    // Compose 资源组件（必需：使用 composeResources）
    implementation(compose.components.resources)
}
```

### 2. Environment API 使用

> **官方文档**：
> - [管理本地资源环境 (Compose Resource Environment)](https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html)
> - [Compose Multiplatform 资源概述](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources.html)

`Environment` API 是 Compose Multiplatform Resources 提供的官方 API，用于在运行时动态管理资源环境（如语言、主题等）。通过 `Environment`，可以在不重启应用的情况下切换语言。

**核心 API（官方提供）：**
- `Environment(mapOf("locale" to "zh-CN"))`：创建环境实例（官方 API）
- `LocalEnvironment`：CompositionLocal，用于在 Composition 树中传递环境（官方 API）
- `CompositionLocalProvider(LocalEnvironment provides environment)`：提供环境给子组件（Compose 标准 API）

> **重要说明**：
> - **官方 API**：`Environment` 和 `LocalEnvironment` 是 Compose Resources 提供的官方 API
> - **自定义封装**：下面的 `AppLocaleManager` 和 `AppWithLocale` 是**建议的自定义封装类**，用于更方便地使用官方 API
> - **可选实现**：您也可以直接使用官方 API，不需要这些封装类

#### 2.1 创建 Environment 管理器（自定义封装）

以下是一个建议的自定义封装实现，用于简化 Environment API 的使用：

```kotlin
package tech.zhifu.app.myhub.local

import org.jetbrains.compose.resources.Environment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 应用语言环境管理器（自定义封装类）
 *
 * 这是一个对官方 Environment API 的封装，用于：
 * - 简化 Environment 的创建和管理
 * - 提供语言状态管理（StateFlow）
 * - 统一语言切换逻辑
 *
 * 注意：这是自定义实现，不是官方 API 的一部分
 */
object AppLocaleManager {
    private val _currentLocale = MutableStateFlow<String>("en")
    val currentLocale: StateFlow<String> = _currentLocale.asStateFlow()

    private var currentEnvironment: Environment? = null

    /**
     * 设置当前语言环境
     * @param locale BCP 47 语言标签（如 "zh-CN", "en", "ja"）
     * @return 创建的 Environment 实例
     */
    fun setLocale(locale: String): Environment {
        val env = Environment(
            mapOf("locale" to locale)
        )
        currentEnvironment = env
        _currentLocale.value = locale
        return env
    }

    /**
     * 获取当前 Environment 实例
     */
    fun getCurrentEnvironment(): Environment? = currentEnvironment

    /**
     * 初始化默认语言环境
     */
    fun initializeDefault() {
        // 从系统或设置中读取默认语言
        val defaultLocale = getSystemLocale()
        setLocale(defaultLocale)
    }

    /**
     * 获取系统默认语言（平台特定实现）
     */
    private expect fun getSystemLocale(): String
}
```

#### 2.2 在 Composition 中使用 Environment（自定义封装）

以下是一个建议的自定义 Composable，用于简化 Environment 的使用：

```kotlin
package tech.zhifu.app.myhub.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import org.jetbrains.compose.resources.Environment
import org.jetbrains.compose.resources.LocalEnvironment

/**
 * 提供语言环境的 Composable（自定义封装）
 * 
 * 这是一个对官方 LocalEnvironment 的封装，用于：
 * - 简化 Environment 的创建和注入
 * - 自动根据 locale 创建 Environment
 * 
 * 注意：这是自定义实现，您也可以直接使用官方 API：
 * ```kotlin
 * CompositionLocalProvider(
 *     LocalEnvironment provides Environment(mapOf("locale" to "zh-CN"))
 * ) {
 *     content()
 * }
 * ```
 */
@Composable
fun AppWithLocale(
    locale: String,
    content: @Composable () -> Unit
) {
    val environment = remember(locale) {
        AppLocaleManager.setLocale(locale)
    }

    CompositionLocalProvider(
        LocalEnvironment provides environment
    ) {
        content()
    }
}
```

#### 2.3 直接使用官方 API（无需封装）

您也可以直接使用官方 API，无需自定义封装类：

```kotlin
import org.jetbrains.compose.resources.Environment
import org.jetbrains.compose.resources.LocalEnvironment
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

@Composable
fun App(locale: String) {
    val environment = remember(locale) {
        Environment(mapOf("locale" to locale))
    }
    
    CompositionLocalProvider(
        LocalEnvironment provides environment
    ) {
        // 应用内容
        MainContent()
    }
}
```

**官方 API 说明**：
- `Environment(variables: Map<String, String>)`：创建环境实例，`variables` 中 `"locale"` 键用于指定语言
- `LocalEnvironment`：CompositionLocal，由 Compose Resources 提供
- `CompositionLocalProvider`：Compose 标准 API，用于提供 CompositionLocal 值

### 3. 语言切换流程

```
┌─────────────────────────────────────────────────────────┐
│  用户操作：在设置中选择语言                              │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│  保存到 Settings（LanguageSetting）                     │
│  - 持久化存储用户偏好                                    │
│  - 支持用户级和系统级设置                                │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│  读取 Settings 获取当前语言                              │
│  - 应用启动时初始化                                      │
│  - 监听设置变化                                          │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│  创建 Environment(locale = "zh-CN")                    │
│  - 使用官方 Environment API                              │
│  - 设置 locale 环境变量                                  │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│  通过 CompositionLocalProvider 提供 Environment          │
│  - LocalEnvironment 注入到 Composition 树               │
│  - 所有子 Composable 可访问                              │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│  Compose Resources 自动加载对应语言的资源                │
│  - 根据 Environment.locale 查找资源文件                 │
│  - 自动回退到默认语言（values/strings.xml）              │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│  UI 自动更新（通过 Compose 重组）                        │
│  - stringResource() 返回新语言的字符串                   │
│  - 所有使用资源的 UI 组件自动更新                        │
└─────────────────────────────────────────────────────────┘
```

### 4. 语言状态管理

#### 4.1 语言状态管理器

```kotlin
package tech.zhifu.app.myhub.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.zhifu.app.myhub.settings.data.store.Setting

/**
 * 语言状态管理器
 *
 * 负责管理应用的语言状态，包括：
 * - 从 Settings 读取保存的语言偏好
 * - 监听语言变化
 * - 同步更新 Environment
 */
class LanguageStateManager(
    private val languageSetting: Setting<String>
) {
    private val _currentLocale = MutableStateFlow<String>("en")
    val currentLocale: StateFlow<String> = _currentLocale.asStateFlow()

    /**
     * 初始化语言设置
     * 从 Settings 读取保存的语言偏好，如果不存在则使用系统默认语言
     */
    suspend fun initialize() {
        val saved = languageSetting.get()
        if (saved.isNotBlank()) {
            setLanguage(saved)
        } else {
            val systemLocale = AppLocaleManager.getSystemLocale()
            setLanguage(systemLocale)
        }
    }

    /**
     * 设置语言
     * @param locale BCP 47 语言标签
     */
    suspend fun setLanguage(locale: String) {
        // 保存到 Settings
        languageSetting.set(locale)
        // 更新状态
        _currentLocale.value = locale
        // 更新 Environment
        AppLocaleManager.setLocale(locale)
    }

    /**
     * 观察语言设置变化
     */
    fun observeLanguage(): StateFlow<String> {
        return languageSetting.observe()
    }
}
```

#### 4.2 在应用根组件中使用

```kotlin
@Composable
fun App() {
    val languageManager = remember {
        LanguageStateManager(
            languageSetting = koinInject<LanguageSetting>()
        )
    }

    val currentLocale by languageManager.currentLocale.collectAsState()

    // 初始化语言设置
    LaunchedEffect(Unit) {
        languageManager.initialize()
    }

    // 提供语言环境
    AppWithLocale(locale = currentLocale) {
        // 应用内容
        MainContent()
    }
}
```

## 🌍 平台特定实现

### Android 平台

```kotlin
// core/platform-compose/src/androidMain/kotlin/.../AppLocaleManager.android.kt
actual fun AppLocaleManager.getSystemLocale(): String {
    val locale = java.util.Locale.getDefault()
    return locale.toLanguageTag()
}

// 同步 Android Configuration
@Composable
actual fun AppWithLocale(
    locale: String,
    content: @Composable () -> Unit
) {
    val environment = remember(locale) {
        AppLocaleManager.setLocale(locale)
    }

    // Android 平台需要同步 Configuration
    val configuration = LocalConfiguration.current
    val newConfig = Configuration(configuration).apply {
        setLocale(java.util.Locale.forLanguageTag(locale))
    }

    CompositionLocalProvider(
        LocalEnvironment provides environment,
        LocalConfiguration provides newConfig
    ) {
        content()
    }
}
```

### iOS 平台

```kotlin
// core/platform-compose/src/iosMain/kotlin/.../AppLocaleManager.ios.kt
actual fun AppLocaleManager.getSystemLocale(): String {
    val locale = platform.Foundation.NSLocale.preferredLanguages.firstOrNull()
    return locale?.replace("_", "-") ?: "en"
}
```

### Desktop (JVM) 平台

```kotlin
// core/platform-compose/src/jvmMain/kotlin/.../AppLocaleManager.jvm.kt
actual fun AppLocaleManager.getSystemLocale(): String {
    val locale = java.util.Locale.getDefault()
    return locale.toLanguageTag()
}
```

### Web (JS/Wasm) 平台

```kotlin
// core/platform-compose/src/jsMain/kotlin/.../AppLocaleManager.js.kt
actual fun AppLocaleManager.getSystemLocale(): String {
    val lang = js("navigator.language") as? String
    return lang?.replace("_", "-") ?: "en"
}
```

## 🔄 语言变动支持

### 1. 应用内语言切换（用户主动选择）

**场景**：用户在设置页面选择语言

**实现步骤：**

1. **监听用户选择**

   ```kotlin
   LanguageSelectionDialog(
       currentLanguage = currentLanguage,
       onLanguageSelected = { language ->
           viewModel.updateLanguage(language.code)
       }
   )
   ```

2. **更新语言设置**

   ```kotlin
   suspend fun updateLanguage(locale: String) {
       languageStateManager.setLanguage(locale)
   }
   ```

3. **自动更新 UI**
   - `LanguageStateManager.setLanguage()` 更新 `StateFlow`
   - `AppWithLocale` 的 `locale` 参数变化触发重组
   - 新的 `Environment` 被创建并注入
   - 所有使用 `stringResource()` 的组件自动更新

### 2. 系统语言变化（跟随系统）

**场景**：用户在系统设置中更改语言，应用跟随系统语言

**实现要点：**

#### Android 平台

```kotlin
@Composable
fun App() {
    val configuration = LocalConfiguration.current
    val systemLocale = configuration.locales[0].toLanguageTag()

    // 监听系统语言变化
    LaunchedEffect(systemLocale) {
        if (shouldFollowSystemLanguage()) {
            languageStateManager.setLanguage(systemLocale)
        }
    }

    // ...
}
```

#### iOS 平台

```kotlin
// 监听 NSLocale.currentLocaleDidChangeNotification
// 在 iOS 平台特定代码中实现
```

#### Desktop 平台

```kotlin
// 监听系统语言设置变化
// 在 Desktop 平台特定代码中实现
```

#### Web 平台

```kotlin
// 监听 navigator.language 变化
// 在 Web 平台特定代码中实现
```

### 3. 语言变化监听最佳实践

```kotlin
/**
 * 语言变化监听器
 *
 * 支持两种模式：
 * 1. 应用内切换：用户主动选择语言
 * 2. 系统跟随：跟随系统语言变化
 */
class LanguageChangeListener(
    private val languageStateManager: LanguageStateManager,
    private val shouldFollowSystem: Boolean = false
) {
    /**
     * 监听系统语言变化
     */
    fun observeSystemLanguage() {
        // 平台特定实现
        // Android: LocalConfiguration
        // iOS: NSLocale notifications
        // Desktop: System properties
        // Web: navigator.language
    }

    /**
     * 监听应用内语言变化
     */
    fun observeAppLanguage() {
        languageStateManager.observeLanguage()
            .collect { locale ->
                // 语言已变化，UI 会自动更新
            }
    }
}
```

## 📝 资源文件管理

### 1. 字符串资源定义

**默认语言（values/strings.xml）：**

```xml
<resources>
    <string name="app_name">Study Room</string>
    <string name="dashboard">Dashboard</string>
    <string name="cards_to_review">You have %d cards to review today.</string>
</resources>
```

**简体中文（values-zh-rCN/strings.xml）：**

```xml
<resources>
    <string name="app_name">书斋</string>
    <string name="dashboard">仪表盘</string>
    <string name="cards_to_review">你今天有 %d 张卡片需要复习。</string>
</resources>
```

### 2. 使用资源

#### 基本使用

```kotlin
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.resources.Res

@Composable
fun AppTitle() {
    Text(stringResource(Res.string.app_name))
}
```

#### 参数化字符串

```kotlin
@Composable
fun ReviewCardCount(count: Int) {
    Text(stringResource(Res.string.cards_to_review, count))
}
```

#### 资源类生成

Compose Resources 会在编译时自动生成资源类：

```kotlin
// 自动生成的资源类
package tech.zhifu.app.myhub.resources

object Res {
    object string {
        val app_name: StringResource = StringResource("app_name")
        val dashboard: StringResource = StringResource("dashboard")
        val cards_to_review: StringResource = StringResource("cards_to_review")
    }
}
```

### 3. 资源文件最佳实践

1. **统一管理**：所有字符串资源放在 `composeResources/values-*/strings.xml`
2. **命名规范**：使用下划线命名（如 `app_name`、`cards_to_review`）
3. **参数化支持**：使用格式化字符串（如 `%d`、`%s`）
4. **完整性检查**：确保所有语言版本包含相同的 key
5. **默认语言**：`values/strings.xml` 必须包含所有 key

## 🔄 迁移方案

### 当前实现分析

**现有实现：**

- `LocalAppLocale`：自定义的 CompositionLocal
- `customAppLocale`：全局状态变量
- 平台特定实现：Android、iOS、JVM、Web 分别实现

**问题：**

- 非官方方案，维护成本高
- 平台实现不一致
- 与 Compose Resources 集成不够紧密

### 迁移步骤

#### 步骤 1：保留资源文件结构 ✅

当前资源文件结构已符合规范，无需修改：

- `values/strings.xml`（默认）
- `values-zh-rCN/strings.xml`（简体中文）
- `values-zh-rTW/strings.xml`（繁体中文）
- `values-ja/strings.xml`（日语）

#### 步骤 2：引入 Environment API

创建 `AppLocaleManager` 和 `AppWithLocale`，使用官方 `Environment` API。

#### 步骤 3：替换 LocalAppLocale

- 移除自定义的 `LocalAppLocale`
- 使用 `LocalEnvironment`（Compose Resources 提供）
- 更新所有使用 `LocalAppLocale` 的代码

#### 步骤 4：更新语言切换逻辑

- 使用 `AppLocaleManager.setLocale()` 替代 `customAppLocale`
- 通过 `Environment` 管理语言环境
- 更新 `LanguageStateManager` 集成新 API

#### 步骤 5：测试各平台

- Android：测试语言切换和系统语言跟随
- iOS：测试语言切换功能
- Desktop：测试语言切换功能
- Web：测试语言切换功能

### 迁移检查清单

- [ ] 创建 `AppLocaleManager` 使用 `Environment` API
- [ ] 创建 `AppWithLocale` Composable
- [ ] 更新 `LanguageStateManager` 集成新 API
- [ ] 替换所有 `LocalAppLocale` 为 `LocalEnvironment`
- [ ] 移除 `customAppLocale` 全局变量
- [ ] 更新平台特定实现
- [ ] 测试 Android 平台语言切换
- [ ] 测试 iOS 平台语言切换
- [ ] 测试 Desktop 平台语言切换
- [ ] 测试 Web 平台语言切换
- [ ] 验证资源文件完整性
- [ ] 更新相关文档

## ✅ 最佳实践

### 1. 代码使用规范

#### ✅ 推荐做法

```kotlin
// 使用 stringResource 访问资源
Text(stringResource(Res.string.app_name))

// 参数化字符串
Text(stringResource(Res.string.cards_to_review, count))

// 在 Composable 中使用
@Composable
fun MyScreen() {
    Column {
        Text(stringResource(Res.string.dashboard))
    }
}
```

#### ❌ 不推荐做法

```kotlin
// 硬编码字符串
Text("App Name")

// 直接使用资源 ID（不通过 stringResource）
Text(Res.string.app_name.toString())

// 在非 Composable 函数中使用 stringResource
fun getTitle(): String {
    return stringResource(Res.string.app_name) // ❌ 错误
}
```

### 2. 资源文件管理

1. **统一管理**：所有字符串资源集中在 `composeResources` 目录
2. **命名规范**：使用下划线命名，避免特殊字符
3. **完整性**：确保所有语言版本包含相同的 key
4. **默认值**：`values/strings.xml` 必须包含所有 key
5. **版本控制**：资源文件变更需要版本记录

### 3. 语言代码规范

- 使用 **BCP 47** 标准语言标签
- 语言代码：小写（如 `en`、`zh`、`ja`）
- 地区代码：大写（如 `CN`、`TW`）
- 格式：`{language}` 或 `{language}-{region}`

### 4. 性能优化

1. **资源编译优化**：Compose Resources 在编译时优化资源
2. **延迟加载**：按需加载语言资源
3. **缓存机制**：Environment 实例可复用
4. **避免频繁切换**：减少不必要的语言切换操作

## 🚨 注意事项

### 1. 资源文件要求

- ✅ 所有语言版本必须包含相同的 key
- ✅ `values/strings.xml` 必须包含所有 key（作为默认值）
- ✅ 资源文件命名必须符合 Android 规范
- ❌ 不能缺少默认语言资源文件

### 2. 语言代码要求

- ✅ 使用 BCP 47 标准语言标签
- ✅ 语言代码小写，地区代码大写
- ❌ 不要使用非标准语言代码

### 3. 平台兼容性

- ✅ Android：完全支持
- ✅ iOS：完全支持
- ✅ Desktop (JVM)：完全支持
- ⚠️ Web (JS/Wasm)：需要验证浏览器兼容性

### 4. RTL 语言支持

如果需要支持 RTL（从右到左）语言（如阿拉伯语、希伯来语）：

1. 添加 RTL 语言资源文件（如 `values-ar/strings.xml`）
2. 在 Compose 中使用 `LayoutDirection` 支持
3. 测试 RTL 布局是否正确

### 5. 测试建议

- 测试所有支持的语言切换
- 测试系统语言变化时的行为
- 测试资源缺失时的回退机制
- 测试参数化字符串的格式化
- 测试各平台的语言切换性能

## 📚 参考资源

### 官方文档

#### Environment API 核心文档

- **[管理本地资源环境 (Compose Resource Environment)](https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html)**
  - **Environment API 的官方说明文档** ⭐
  - 详细介绍如何使用 `Environment` 管理语言和主题
  - 包含运行时环境切换的完整示例代码
  - 说明如何通过 `LocalEnvironment` 在 Composition 中使用
  - 文档链接：`https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html`

#### Compose Resources 概述文档

- **[Compose Multiplatform 资源概述](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources.html)**
  - Compose Resources 的总体介绍
  - 资源文件的设置和使用方法
  - 跨平台资源管理的最佳实践
  - 文档链接：`https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources.html`

#### 源代码和示例

- **[Compose Multiplatform Resources GitHub](https://github.com/JetBrains/compose-multiplatform/tree/master/resources)**
  - 源代码仓库
  - 最新更新和示例代码
  - Issue 跟踪和社区讨论
  - 仓库链接：`https://github.com/JetBrains/compose-multiplatform/tree/master/resources`

#### 相关 API 参考

- **`org.jetbrains.compose.resources.Environment`**
  - 环境变量管理类
  - 用于运行时设置语言、主题等环境变量
  - 构造函数：`Environment(variables: Map<String, String>)`
  
- **`org.jetbrains.compose.resources.LocalEnvironment`**
  - CompositionLocal 提供者
  - 用于在 Composition 树中传递 Environment
  - 通过 `CompositionLocalProvider` 注入

- **`org.jetbrains.compose.resources.stringResource()`**
  - 字符串资源访问函数
  - 根据当前 Environment 自动选择对应语言的资源
  - 支持参数化字符串（格式化）

### 相关文档

- [MyHub 架构设计](./myhub_architecture.md)
- [构建变体说明](./BUILD_VARIANTS.md)
- [常见问题](./FAQ.md)

## 📝 更新日志

- **2024-XX-XX**：初始版本，基于 Compose Resources 官方方案制定多语言支持技术方案

---

## 总结

本方案基于 Compose Multiplatform Resources 官方 API，使用 `Environment` 管理语言环境，支持运行时语言切换，提供跨平台统一的实现方式。该方案具有以下优势：

1. **官方支持**：使用官方推荐方案，稳定可靠
2. **类型安全**：编译时生成资源类，避免运行时错误
3. **跨平台统一**：统一的 API，各平台实现一致
4. **性能优化**：资源编译时优化，运行时高效
5. **易于维护**：集中管理资源文件，代码清晰

通过实施本方案，可以实现高效、可靠的多语言支持，提升用户体验。

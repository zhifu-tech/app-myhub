# Core Platform Compose Module

本模块用于**规范**和**实现** MyHub 应用的 Compose UI 平台抽象基础设施（Platform Compose Infra），为各功能模块**提供统一的跨平台 Compose UI 抽象能力**。它基于 **Compose Multiplatform** 和 **Kotlin Multiplatform expect/actual 机制**，实现了**主题管理**、**语言环境**、**窗口尺寸检测**、**手势交互**等特性，并提供了面向 KMP 场景的**统一 Compose UI 平台抽象接口**，方便在 **Android、iOS、JVM、JS、WASM** 等多端项目中集成和使用。

**重要说明**：`core/platform-compose` 是一个**混合（Mixed）模块**，与其他单一功能的 core 模块不同，它包含多个 Compose UI 平台相关的功能集合，这些功能都与 Compose UI 平台相关，属于同一领域，便于统一管理和维护。

## 核心组件

### 1. LocalAppTheme

统一的主题管理 CompositionLocal，提供深色/浅色主题支持：

- **`current`**：获取当前是否为深色主题
- **`provides(value)`**：设置主题值
- **平台支持**：所有平台统一接口，隐藏平台实现细节

### 2. LocalAppLocale

统一的语言环境管理 CompositionLocal，提供多语言支持：

- **`current`**：获取当前语言代码（如 "en"、"zh-CN"）
- **`provides(value)`**：设置语言环境值
- **支持语言**：英语、简体中文、繁体中文、日语

### 3. WindowSizeDetector

跨平台的窗口尺寸检测函数：

- **`getWindowSize()`**：获取当前窗口尺寸（DpSize）
- **`calculateWindowSizeClass()`**：计算窗口尺寸类别（Compact/Medium/Expanded）
- **响应式布局**：支持手机、平板、桌面三种布局模式

### 4. SwipeBackGesture

统一的滑动返回手势 Modifier：

- **`swipeBackGesture(onSwipeBack, enabled)`**：添加滑动返回手势
- **统一交互**：从屏幕左边缘向右滑动返回上一页
- **平台适配**：支持启用/禁用控制，桌面端可禁用

### 5. Language 枚举

语言代码和枚举之间的转换工具：

- **`Language`**：支持的语言枚举（English、SimplifiedChinese、TraditionalChinese、Japanese）
- **`String.toLanguage()`**：字符串转语言枚举
- **`Language.toCode()`**：语言枚举转代码
- **`Language.getLocalizedLabel()`**：获取本地化显示名称

## 使用示例

```kotlin
// 1. 配置主题和语言环境
import tech.zhifu.app.myhub.local.LocalAppEnvironment
import tech.zhifu.app.myhub.theme.AppTheme

@Composable
fun MyApp() {
    LocalAppEnvironment {
        AppTheme {
            // 应用内容
        }
    }
}

// 2. 使用主题
import tech.zhifu.app.myhub.local.LocalAppTheme

@Composable
fun MyScreen() {
    val isDark = LocalAppTheme.current
    
    Surface(
        color = if (isDark) Color.Black else Color.White
    ) {
        // 屏幕内容
    }
}

// 3. 使用语言环境
import tech.zhifu.app.myhub.local.LocalAppLocale
import tech.zhifu.app.myhub.language.Language

@Composable
fun MyScreen() {
    val locale = LocalAppLocale.current // 例如: "en", "zh-CN"
    val language = locale.toLanguage() // Language.English
    
    Text("当前语言: ${language.getLocalizedLabel()}")
}

// 4. 响应式布局
import tech.zhifu.app.myhub.ui.*

@Composable
fun MyScreen() {
    val windowSize = getWindowSize()
    val sizeClass = calculateWindowSizeClass(windowSize)
    
    when {
        sizeClass.isCompact -> {
            // 手机布局
            Column { /* ... */ }
        }
        sizeClass.isMedium -> {
            // 平板布局
            Row { /* ... */ }
        }
        sizeClass.isExpanded -> {
            // 桌面布局
            Row { /* ... */ }
        }
    }
}

// 5. 滑动返回手势
import tech.zhifu.app.myhub.ui.swipeBackGesture

@Composable
fun MyScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .swipeBackGesture(onSwipeBack = onBack)
    ) {
        // 屏幕内容
    }
}
```

## 文档

- [MyHub Compose UI 平台抽象模块方案设计](./docs/myhub-platform-compose-infra-v1.0.md)

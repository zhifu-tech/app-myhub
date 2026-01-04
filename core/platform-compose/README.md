# core:platform-compose

Compose UI 平台抽象模块，提供跨平台的主题、语言环境和窗口大小检测功能。

## 📋 功能特性

- ✅ **主题管理**：跨平台的深色/浅色主题支持
- ✅ **语言环境**：多语言支持（英语、简体中文、繁体中文、日语）
- ✅ **窗口大小检测**：响应式布局的窗口尺寸检测
- ✅ **窗口尺寸类别**：自动计算 Compact/Medium/Expanded 布局类别
- ✅ **WASM 支持**：完整支持 Kotlin/WASM 平台
- ✅ **代码复用**：Web 平台（JS/WASM）公共代码提取到 `webMain`
- ✅ **单元测试**：完整的测试覆盖

## 🎯 支持的平台

- **Android** - Android 平台
- **iOS** - iOS 平台（所有架构）
- **JVM** - 桌面应用（Windows、macOS、Linux）
- **JS** - Web 应用（Kotlin/JS）
- **WASM** - Web 应用（Kotlin/WASM）

## 📁 模块结构

### 源集说明

- **commonMain**：公共接口、期望函数和资源文件
  - `LocalAppLocale.kt` - 语言环境期望接口
  - `LocalAppTheme.kt` - 主题期望接口
  - `WindowSizeDetector.kt` - 窗口大小检测期望接口
  - `WindowSize.kt` - 窗口尺寸类别计算
  - `Language.kt` - 语言枚举和转换
  - `composeResources/` - 多语言资源文件

- **webMain**：Web 平台（JS/WASM）公共代码
  - `LocalAppLocale.web.kt` - Web 平台语言环境实现
  - `LocalAppTheme.web.kt` - Web 平台主题实现

- **jsMain**：JS 平台特定代码
  - `WindowSizeDetector.js.kt` - JS 平台窗口大小检测（使用 `kotlinx.browser.window`）

- **wasmJsMain**：WASM 平台特定代码
  - `WindowSizeDetector.wasmJs.kt` - WASM 平台窗口大小检测（使用 `external val window`）

- **androidMain**：Android 平台特定代码
- **iosMain**：iOS 平台特定代码
- **jvmMain**：JVM 平台特定代码

## 🔧 主要 API

### 主题管理

```kotlin
@Composable
fun MyScreen() {
    val isDark = LocalAppTheme.current
    // 使用主题状态
}
```

### 语言环境

```kotlin
@Composable
fun MyScreen() {
    val locale = LocalAppLocale.current // 例如: "en", "zh-CN"
    // 使用语言环境
}
```

### 窗口大小检测

```kotlin
@Composable
fun MyScreen() {
    val windowSize = getWindowSize()
    val sizeClass = calculateWindowSizeClass(windowSize)
    
    when {
        sizeClass.isCompact -> { /* 手机布局 */ }
        sizeClass.isMedium -> { /* 平板布局 */ }
        sizeClass.isExpanded -> { /* 桌面布局 */ }
    }
}
```

### 语言转换

```kotlin
// 字符串转语言枚举
val language = "zh-CN".toLanguage() // Language.SimplifiedChinese

// 语言枚举转代码
val code = Language.English.toCode() // "en"

// 获取本地化显示名称
@Composable
fun LanguageLabel(language: Language) {
    Text(language.getLocalizedLabel())
}
```

## 🧪 测试

模块包含完整的单元测试：

- **LanguageTest** - 语言转换和枚举测试
- **WindowSizeTest** - 窗口尺寸类别计算测试

### 运行测试

```bash
# 运行所有平台的测试
./gradlew :core:platform-compose:allTests

# 运行特定平台的测试
./gradlew :core:platform-compose:jvmTest
./gradlew :core:platform-compose:jsTest
./gradlew :core:platform-compose:wasmJsTest
```

## 📝 设计说明

### Web 平台代码复用

- JS 和 WASM 的公共代码（`LocalAppLocale`、`LocalAppTheme`）提取到 `webMain`
- 平台特定的实现（`WindowSizeDetector`）分别放在 `jsMain` 和 `wasmJsMain`
  - JS 使用 `kotlinx.browser.window`（JS 特定 API）
  - WASM 使用 `external val window: Window`（WASM 兼容方式）
- 符合 KMP 默认结构：`webMain` 是 `jsMain` 和 `wasmJsMain` 的父级源集

### 窗口大小检测

- **Android**：使用 `LocalConfiguration` 获取屏幕尺寸
- **iOS**：使用 `LocalWindowInfo` 获取窗口尺寸
- **JVM**：返回默认桌面尺寸（实际大小通过参数传入）
- **JS/WASM**：实时监听浏览器窗口 `resize` 事件

### 窗口尺寸类别

- **Compact**：< 600dp（手机）
- **Medium**：600dp - 840dp（平板）
- **Expanded**：> 840dp（桌面）

`calculateWindowSizeClass` 是一个纯函数，不需要 `@Composable` 注解，可以在任何地方调用。

## 🌐 支持的语言

- **English** (`en`) - 英语
- **Simplified Chinese** (`zh-CN`) - 简体中文
- **Traditional Chinese** (`zh-TW`) - 繁体中文
- **Japanese** (`ja`) - 日语

语言资源文件位于 `commonMain/composeResources/` 目录。

## 🔗 相关模块

- `core:platform` - 基础平台抽象
- `composeApp` - Compose 应用主模块


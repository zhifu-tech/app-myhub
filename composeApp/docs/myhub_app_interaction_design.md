# MyHub App 层级交互设计文档

本文档定义了 MyHub 应用层级的交互设计规范，重点关注导航、布局、转场动画和用户交互模式。

## 📋 目录

- [概述](#概述)
- [响应式布局系统](#响应式布局系统)
- [导航系统](#导航系统)
- [屏幕转场动画](#屏幕转场动画)
- [交互模式](#交互模式)
- [主题系统](#主题系统)
- [状态管理](#状态管理)
- [实现检查清单](#实现检查清单)
- [相关文件](#相关文件)

## 📖 概述

MyHub App 层级负责应用的整体架构和交互设计，包括：

1. **响应式布局**：根据窗口大小自动调整布局（Compact/Medium/Expanded）
2. **导航系统**：统一的导航栏和导航轨道
3. **转场动画**：流畅的屏幕切换动画，参考 Apple Music 设计
4. **交互模式**：统一的用户交互方式（如滑动返回）
5. **状态管理**：应用级状态管理（主题、窗口大小、当前屏幕）

### 设计原则

- **一致性**：所有平台和屏幕尺寸保持一致的交互体验
- **流畅性**：使用平滑的动画和过渡效果
- **响应式**：自动适配不同屏幕尺寸和设备类型
- **可访问性**：支持键盘导航和屏幕阅读器

## 📐 响应式布局系统

### 窗口大小类（WindowSizeClass）

应用根据窗口宽度自动分类为三种布局：

| 窗口大小类    | 宽度范围          | 使用场景     | 导航方式       | 顶部栏 | 底部栏 |
|----------|---------------|----------|------------|-----|-----|
| Compact  | < 600dp       | 移动端手机    | 底部导航栏      | ❌ 无 | ✅ 有 |
| Medium   | 600dp - 840dp | 平板/大屏手机  | 左侧导航轨道（折叠） | ❌ 无 | ❌ 无 |
| Expanded | > 840dp       | 桌面端/大屏平板 | 左侧导航轨道（展开） | ❌ 无 | ❌ 无 |

### 布局实现

#### Compact Layout（紧凑布局 - 移动端）

```kotlin
@Composable
private fun CompactLayout(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Scaffold(
        bottomBar = {
            AppNavigationBar(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AppNavigation(
                currentScreen = currentScreen,
                onNavigateBack = { onNavigate(Screen.Dashboard) },
                onNavigate = onNavigate
            )
        }
    }
}
```

**特点：**

- 使用 `Scaffold` 提供底部导航栏
- **不显示顶部栏**（避免与底部导航重复，节省屏幕空间）
- 内容区域自动适配 `Scaffold` 的 padding

#### Medium Layout（中等布局 - 平板）

```kotlin
@Composable
private fun MediumLayout(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        AppNavigationRail(
            currentScreen = currentScreen,
            onNavigate = onNavigate,
            isExpanded = false  // 折叠状态，只显示图标
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AppNavigation(...)
        }
    }
}
```

**特点：**

- 左侧导航轨道（折叠状态，只显示图标）
- 内容区域占据剩余空间
- 导航轨道和内容区域背景色区分

#### Expanded Layout（扩展布局 - 桌面端）

```kotlin
@Composable
private fun ExpandedLayout(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        AppNavigationRail(
            currentScreen = currentScreen,
            onNavigate = onNavigate,
            isExpanded = true  // 展开状态，显示图标和文字
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AppNavigation(...)
        }
    }
}
```

**特点：**

- 左侧导航轨道（展开状态，显示图标和文字标签）
- 内容区域占据剩余空间
- 适合大屏幕，提供更多导航信息

## 🧭 导航系统

### 屏幕定义（Screen）

应用使用密封类定义所有屏幕：

```kotlin
sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object AllCards : Screen("all_cards", "All Cards")
    object New : Screen("new", "New")
    object Templates : Screen("templates", "Templates")
    object Favorites : Screen("favorites", "Favorites")
    object Settings : Screen("settings", "Settings")
    object Profile : Screen("profile", "Profile")

    data class CardDetail(
        val cardId: String
    ) : Screen("card_detail/$cardId", "Card Detail")
}
```

### 导航组件

#### AppNavigationBar（底部导航栏 - Compact 布局）

**位置**：屏幕底部  
**显示时机**：仅 Compact 布局  
**包含项**：

- Dashboard（仪表盘）
- New Card（新建卡片）
- Profile（个人资料）

**实现位置**：`navigation/AppNavigationBar.kt`

#### AppNavigationRail（导航轨道 - Medium/Expanded 布局）

**位置**：屏幕左侧  
**显示时机**：Medium 和 Expanded 布局  
**状态**：

- Medium：折叠状态（`isExpanded = false`），只显示图标
- Expanded：展开状态（`isExpanded = true`），显示图标和文字标签

**包含项**：

- Dashboard（仪表盘）
- All Cards（所有卡片）
- Templates（模板）
- Favorites（收藏）
- Profile（个人资料）
- Settings（设置）

**实现位置**：`navigation/AppNavigationRail.kt`

### 导航流程

#### 主要导航流程

```
Dashboard
  ├─> CardDetail (点击卡片)
  │     └─> Dashboard (返回)
  ├─> Settings (从 Profile 进入)
  └─> Profile (从导航栏进入)
```

#### 导航状态管理

导航状态由 `AppViewModel` 管理：

```kotlin
class AppViewModel {
    private var currentScreen: Screen = Screen.Dashboard

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            currentScreen = screen
            // 记录分析事件
            analyticsService?.setScreen(screen.route, screen::class.simpleName)
            updateReadyState()
        }
    }
}
```

## 🎬 屏幕转场动画

### 动画设计原则

参考 **Apple Music** 的转场动画设计：

- **进入动画**：从卡片位置平滑展开到全屏
- **退出动画**：平滑缩小并淡出
- **缓动曲线**：使用 `FastOutSlowInEasing` 实现自然的动画效果

### 转场类型

#### 1. Dashboard ↔ CardDetail 转场

**从 Dashboard 到 CardDetail（卡片展开）**：

- **进入动画**：缩放（0.85 → 1.0）+ 淡入（450ms）
- **退出动画**：水平滑动（向左 1/4 屏幕宽度）+ 淡出（450ms）

**从 CardDetail 返回 Dashboard（卡片收起）**：

- **进入动画**：水平滑动（从左侧 1/4 屏幕宽度进入）+ 淡入（400ms）
- **退出动画**：缩放（1.0 → 0.9）+ 淡出（400ms）

**实现代码**：

```kotlin
// ScreenTransition.kt
fun cardDetailEnterTransition() = fadeIn(
    animationSpec = tween(450, easing = FastOutSlowInEasing)
) + scaleIn(
    initialScale = 0.85f,
    animationSpec = tween(450, easing = FastOutSlowInEasing)
)

fun dashboardExitTransition() = fadeOut(
    animationSpec = tween(450, easing = FastOutSlowInEasing)
) + slideOutHorizontally(
    targetOffsetX = { -it / 4 },
    animationSpec = tween(450, easing = FastOutSlowInEasing)
)
```

#### 2. 默认转场（其他屏幕）

**进入动画**：淡入 + 从右侧滑入（300ms）  
**退出动画**：淡出 + 向左侧滑出（300ms）

**实现代码**：

```kotlin
fun defaultEnterTransition() = fadeIn(
    animationSpec = tween(300)
) + slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(300)
)

fun defaultExitTransition() = fadeOut(
    animationSpec = tween(300)
) + slideOutHorizontally(
    targetOffsetX = { -it },
    animationSpec = tween(300)
)
```

### 转场实现

使用 `AnimatedContent` 实现屏幕转场：

```kotlin
@Composable
fun AppNavigation(
    currentScreen: Screen,
    onNavigateBack: () -> Unit = {},
    onNavigate: ((Screen) -> Unit)? = null
) {
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            when {
                // Dashboard -> CardDetail
                initialState is Screen.Dashboard && targetState is Screen.CardDetail -> {
                    ScreenTransition.cardDetailEnterTransition() togetherWith
                            ScreenTransition.dashboardExitTransition()
                }
                // CardDetail -> Dashboard
                initialState is Screen.CardDetail && targetState is Screen.Dashboard -> {
                    ScreenTransition.dashboardEnterTransition() togetherWith
                            ScreenTransition.cardDetailExitTransition()
                }
                // 其他导航
                else -> {
                    ScreenTransition.defaultEnterTransition() togetherWith
                            ScreenTransition.defaultExitTransition()
                }
            }.using(
                SizeTransform(clip = false)  // 允许内容大小平滑过渡
            )
        },
        label = "screen_transition"
    ) { screen ->
        // 根据 screen 显示对应内容
    }
}
```

## 👆 交互模式

### 滑动返回手势

**适用范围**：所有支持滑动手势的平台（移动端、平板）  
**触发区域**：屏幕左边缘 20dp 范围内  
**触发条件**：

- 从屏幕左边缘开始滑动
- 水平滑动距离超过 100dp
- 垂直偏移小于 50dp（避免与垂直滚动冲突）

**实现位置**：`core/platform-compose/src/commonMain/kotlin/tech/zhifu/app/myhub/ui/SwipeBackGesture.kt`

**使用方式**：

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .swipeBackGesture(
            onSwipeBack = { onNavigateBack() },
            enabled = true
        )
) {
    // 内容
}
```

**配置参数**：

- `SWIPE_BACK_THRESHOLD_DP = 100f`：滑动阈值
- `EDGE_THRESHOLD_DP = 20f`：边缘触发区域
- `VERTICAL_THRESHOLD_DP = 50f`：垂直偏移阈值

### 导航返回

**返回方式**：

1. **滑动返回**：从屏幕左边缘向右滑动（移动端/平板）
2. **导航栏返回**：点击导航栏的返回按钮（如果存在）
3. **系统返回**：使用系统返回键/手势（Android）

**返回目标**：

- 从 CardDetail 返回 → Dashboard
- 从 Settings 返回 → Profile（如果从 Profile 进入）
- 其他情况 → 返回上一级屏幕

### 主题切换

**切换方式**：

- 通过 Settings 屏幕切换
- 自动跟随系统主题（可选）

**状态管理**：

- 主题状态由 `AppViewModel` 管理
- 通过 `LocalAppTheme` 提供给所有子组件
- 持久化到 Settings Repository

## 🎨 主题系统

### 概述

MyHub 使用 Material Design 3 主题系统，通过自定义的 `AppTheme` 包装 Material3 的 `MaterialTheme`，实现统一的主题管理。主题系统支持深色模式和浅色模式，所有颜色值都定义在 `Theme.kt` 中。

### 主题提供链（Theme Provision Chain）

主题通过 CompositionLocal 机制在整个组件树中传播，形成以下提供链：

```
AppContent (App.kt:111-136)
  └─> AppTheme(darkTheme = isDarkTheme) { ... }  // Theme.kt:76-88
        └─> MaterialTheme(colorScheme = colorScheme, ...)  // Material3 框架
              └─> 通过 CompositionLocal 提供 ColorScheme
                    └─> CompactLayout 中使用 MaterialTheme.colorScheme.background
```

#### 详细流程

**步骤 1：AppContent 提供主题状态**

```kotlin
@Composable
private fun AppContent(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    isDarkTheme: Boolean,  // ← 主题状态从 AppViewModel 传入
    windowSizeClass: WindowSizeClass
) {
    LocalAppEnvironment {
        // 提供应用主题状态（供卡片组件等使用）
        CompositionLocalProvider(LocalAppTheme provides isDarkTheme) {
            // 应用主题
            AppTheme(darkTheme = isDarkTheme) {  // ← 调用 AppTheme
                // ...
            }
        }
    }
}
```

**步骤 2：AppTheme 选择颜色方案**

```kotlin
// Theme.kt:75-88
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 根据 darkTheme 参数选择对应的颜色方案
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = getTypography()

    // 将颜色方案提供给 MaterialTheme
    MaterialTheme(
        colorScheme = colorScheme,  // ← 传入选中的 ColorScheme
        typography = typography,
        content = content
    )
}
```

**步骤 3：MaterialTheme 通过 CompositionLocal 提供 ColorScheme**

Material3 的 `MaterialTheme` 内部使用 `CompositionLocal` 机制，将 `ColorScheme` 提供给整个组件树。所有子组件都可以通过 `MaterialTheme.colorScheme` 访问颜色方案。

**步骤 4：子组件使用主题颜色**

```kotlin
@Composable
private fun CompactLayout(...) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background  // ← 访问背景色
    ) { padding ->
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)  // ← 使用背景色
        ) {
            // ...
        }
    }
}
```

### 颜色方案定义

#### 深色模式（DarkColorScheme）

定义在 `Theme.kt:11-41`：

```kotlin
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    background = Color(0xFF1C1B1F),  // 深色背景
    surface = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    // ... 其他颜色
)
```

#### 浅色模式（LightColorScheme）

定义在 `Theme.kt:43-73`：

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    background = Color(0xFFFFFBFE),  // 浅色背景
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    // ... 其他颜色
)
```

### 主题状态的双重提供机制

MyHub 使用双重提供机制来管理主题状态：

1. **LocalAppTheme**：提供布尔值 `isDarkTheme`，供需要直接判断主题的组件使用（如卡片组件）
2. **MaterialTheme.colorScheme**：提供完整的 `ColorScheme`，供所有 Material3 组件使用

#### LocalAppTheme 的使用

```kotlin
// 在 AppContent 中提供
CompositionLocalProvider(LocalAppTheme provides isDarkTheme) {
    AppTheme(darkTheme = isDarkTheme) {
        // ...
    }
}

// 在组件中使用
@Composable
fun SomeComponent() {
    val isDark = LocalAppTheme.current  // ← 获取主题状态
    val bgColor = if (isDark) Color(0xFF1C1B1F) else Color(0xFFFFFBFE)
}
```

#### MaterialTheme.colorScheme 的使用

```kotlin
// 在组件中使用
@Composable
fun SomeComponent() {
    val bgColor = MaterialTheme.colorScheme.background  // ← 直接使用主题颜色
    Surface(color = MaterialTheme.colorScheme.surface) {
        // ...
    }
}
```

### 主题切换流程

```
用户切换主题
  └─> Settings Screen
        └─> SettingsRepository.saveThemeSetting(isDark)
              └─> AppViewModel.observeThemeSetting()
                    └─> AppViewModel.updateReadyState()
                          └─> AppUiState.Ready(isDarkTheme = newValue)
                                └─> AppContent(isDarkTheme = newValue)
                                      └─> AppTheme(darkTheme = newValue)
                                            └─> MaterialTheme(colorScheme = newColorScheme)
                                                  └─> 所有组件自动更新
```

### 主题颜色映射

| 用途       | 深色模式值     | 浅色模式值     | 访问方式                                     |
|----------|-----------|-----------|------------------------------------------|
| 背景色      | `#1C1B1F` | `#FFFBFE` | `MaterialTheme.colorScheme.background`   |
| 表面色      | `#1C1B1F` | `#FFFBFE` | `MaterialTheme.colorScheme.surface`      |
| 主色       | `#D0BCFF` | `#6750A4` | `MaterialTheme.colorScheme.primary`      |
| 文本色（背景上） | `#E6E1E5` | `#1C1B1F` | `MaterialTheme.colorScheme.onBackground` |
| 文本色（表面上） | `#E6E1E5` | `#1C1B1F` | `MaterialTheme.colorScheme.onSurface`    |
| 边框色      | `#938F99` | `#79747E` | `MaterialTheme.colorScheme.outline`      |

### 关键文件

- **`core/platform-compose/src/commonMain/kotlin/tech/zhifu/app/myhub/theme/Theme.kt`**
    - 定义 `DarkColorScheme` 和 `LightColorScheme`
    - 实现 `AppTheme` 函数
- **`composeApp/src/commonMain/kotlin/tech/zhifu/app/myhub/App.kt`**
    - 在 `AppContent` 中调用 `AppTheme`
    - 在布局组件中使用 `MaterialTheme.colorScheme`
- **`core/platform-compose/src/commonMain/kotlin/tech/zhifu/app/myhub/local/LocalAppTheme.kt`**
    - 定义 `LocalAppTheme` CompositionLocal
    - 提供主题状态给需要直接判断主题的组件

### 最佳实践

1. **优先使用 MaterialTheme.colorScheme**：对于 Material3 组件，直接使用 `MaterialTheme.colorScheme` 中的颜色
2. **使用 LocalAppTheme 进行条件判断**：当需要根据主题选择不同的颜色或样式时，使用 `LocalAppTheme.current`
3. **避免硬编码颜色**：所有颜色都应该来自主题系统，确保主题切换时自动适配
4. **保持颜色语义**：使用语义化的颜色名称（如 `background`、`surface`、`primary`），而不是具体的颜色值

## 🔄 状态管理

### 应用状态（AppUiState）

应用使用密封类管理整体状态：

```kotlin
sealed class AppUiState {
    object Loading : AppUiState()  // 初始化中

    data class Ready(
        val currentScreen: Screen,
        val isDarkTheme: Boolean,
        val windowSizeClass: WindowSizeClass
    ) : AppUiState()  // 就绪状态

    data class Error(
        val error: Throwable
    ) : AppUiState()  // 错误状态
}
```

### 状态流转

```
Loading
  ├─> Ready (初始化成功)
  └─> Error (初始化失败)
        └─> Ready (重试成功)
```

### 状态提供

应用通过 `CompositionLocalProvider` 提供全局状态：

```kotlin
@Composable
private fun AppContent(...) {
    LocalAppEnvironment {
        CompositionLocalProvider(LocalAppTheme provides isDarkTheme) {
            AppTheme(darkTheme = isDarkTheme) {
                ProvideWindowSizeClass(windowSizeClass) {
                    ResponsiveAppLayout(...)
                }
            }
        }
    }
}
```

**提供的状态**：

- `LocalAppTheme`：应用主题状态（dark/light）
- `LocalWindowSizeClass`：窗口大小类（Compact/Medium/Expanded）
- `LocalAppEnvironment`：应用环境上下文

## ✅ 实现检查清单

### 响应式布局

- [x] Compact 布局（移动端）
    - [x] 底部导航栏
    - [x] 无顶部栏
    - [x] 内容区域适配 padding
- [x] Medium 布局（平板）
    - [x] 左侧导航轨道（折叠）
    - [x] 内容区域占据剩余空间
- [x] Expanded 布局（桌面端）
    - [x] 左侧导航轨道（展开）
    - [x] 内容区域占据剩余空间

### 导航系统

- [x] Screen 密封类定义
- [x] AppNavigationBar（底部导航栏）
- [x] AppNavigationRail（导航轨道）
- [x] 导航状态管理
- [x] 导航分析事件记录

### 转场动画

- [x] Dashboard ↔ CardDetail 转场
    - [x] 卡片展开动画
    - [x] 卡片收起动画
- [x] 默认转场动画
- [x] SizeTransform 支持

### 交互模式

- [x] 滑动返回手势
    - [x] 边缘检测
    - [x] 滑动阈值
    - [x] 垂直偏移过滤
- [x] 导航返回逻辑
- [x] 主题切换

### 主题系统

- [x] AppTheme 实现
    - [x] DarkColorScheme 定义
    - [x] LightColorScheme 定义
    - [x] 根据 darkTheme 参数选择颜色方案
- [x] 主题提供链
    - [x] AppContent 调用 AppTheme
    - [x] AppTheme 调用 MaterialTheme
    - [x] MaterialTheme 通过 CompositionLocal 提供 ColorScheme
- [x] LocalAppTheme 提供
    - [x] 在 AppContent 中提供主题状态
    - [x] 供组件直接判断主题使用
- [x] 主题颜色使用
    - [x] CompactLayout 使用 MaterialTheme.colorScheme.background
    - [x] MediumLayout 使用 MaterialTheme.colorScheme.background
    - [x] ExpandedLayout 使用 MaterialTheme.colorScheme.background

### 状态管理

- [x] AppUiState 定义
- [x] AppViewModel 实现
- [x] LocalAppTheme 提供
- [x] LocalWindowSizeClass 提供
- [x] 状态持久化

## 📁 相关文件

### 核心文件

- `App.kt` - 应用主入口和布局系统
- `AppViewModel.kt` - 应用级 ViewModel
- `AppUiState.kt` - 应用状态定义

### 导航相关

- `navigation/Navigation.kt` - Screen 和 NavItem 定义
- `navigation/AppNavigationBar.kt` - 底部导航栏组件
- `navigation/AppNavigationRail.kt` - 导航轨道组件
- `navigation/ScreenTransition.kt` - 转场动画配置

### 组件相关

- `component/AppTopBar.kt` - 顶部栏组件（已移除，紧凑布局不使用）
- `component/AppErrorScreen.kt` - 错误屏幕
- `component/AppLoadingScreen.kt` - 加载屏幕

### 主题相关

- `core/platform-compose/src/commonMain/kotlin/tech/zhifu/app/myhub/theme/Theme.kt` - 主题定义和 AppTheme 实现
- `core/platform-compose/src/commonMain/kotlin/tech/zhifu/app/myhub/local/LocalAppTheme.kt` - 主题状态 CompositionLocal

### 平台相关

- `core/platform-compose/src/commonMain/kotlin/tech/zhifu/app/myhub/ui/SwipeBackGesture.kt` - 滑动返回手势
- `core/platform-compose/src/commonMain/kotlin/tech/zhifu/app/myhub/ui/WindowSize.kt` - 窗口大小检测

## 📝 更新日志

### v1.0 (2024-01-XX)

- ✅ 实现响应式布局系统（Compact/Medium/Expanded）
- ✅ 实现底部导航栏（Compact 布局）
- ✅ 实现导航轨道（Medium/Expanded 布局）
- ✅ 实现屏幕转场动画（参考 Apple Music）
- ✅ 实现滑动返回手势
- ✅ 实现应用级状态管理
- ✅ 移除紧凑布局的顶部栏（避免与底部导航重复）
- ✅ 实现主题系统（AppTheme + MaterialTheme）
- ✅ 实现主题提供链（Theme Provision Chain）
- ✅ 实现 LocalAppTheme 双重提供机制

## 🎯 未来计划

### 短期计划

- [ ] 添加键盘导航支持（桌面端）
- [ ] 优化转场动画性能
- [ ] 添加深链接支持

### 中期计划

- [ ] 支持多窗口模式（桌面端）
- [ ] 添加导航历史记录
- [ ] 实现导航栈管理

### 长期计划

- [ ] 支持自定义导航动画
- [ ] 实现导航路由系统
- [ ] 添加导航分析仪表板

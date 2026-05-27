# KMP 导航库调研与评估

## 📋 目录

1. [可用库概览](#可用库概览)
2. [库详细对比](#库详细对比)
3. [需求匹配度分析](#需求匹配度分析)
4. [推荐方案](#推荐方案)

---

## 可用库概览

### 1. Compose Multiplatform Navigation（官方）⭐

**状态**：Alpha 阶段（1.7.0+），但功能持续完善  
**维护方**：JetBrains（官方）  
**官方文档**：https://kotlinlang.org/docs/multiplatform/compose-navigation.html  
**GitHub**：https://github.com/JetBrains/compose-multiplatform

#### 核心特性（基于官方文档）

- ✅ **官方支持**：JetBrains 官方维护，长期支持有保障
- ✅ **类型安全导航**：自 1.7.0 起支持类型安全的导航（[官方文档](https://kotlinlang.org/docs/multiplatform/compose-navigation-routing.html)）
- ✅ **多平台支持**：Android、iOS、Desktop、Web 全平台支持
- ✅ **自动栈管理**：内置导航栈管理，自动处理创建和销毁
- ✅ **返回栈控制**：提供对返回栈的完全控制（参考 [Android Navigation Back Stack](https://developer.android.com/guide/navigation/backstack)）
- ✅ **深度链接支持**：支持深度链接和结果处理（[官方文档](https://kotlinlang.org/docs/multiplatform/compose-navigation-deep-links.html)）
- ✅ **Web 平台支持**：支持浏览器前进/后退操作，地址栏同步
- ⚠️ **Alpha 阶段**：功能可能不完整，API 可能变化
- ⚠️ **返回手势**：非 Android 平台可能需要手动实现返回手势

#### 依赖

```kotlin
dependencies {
    // Compose Multiplatform Navigation（官方）
    implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
}
```

#### 关键能力

根据官方文档，Compose Multiplatform Navigation 支持：

1. **返回栈管理**：

    - 自动管理导航栈的创建和销毁
    - 支持保存和恢复导航状态
    - 支持自定义返回栈行为

2. **类型安全路由**：

    - 编译时类型检查
    - 类型安全的参数传递
    - 减少运行时错误

3. **深度链接**：

    - 支持应用内深度链接
    - 支持结果处理（Result API）
    - Web 平台支持 URL 路由

4. **多平台适配**：
    - Android：完整的 Navigation 功能
    - iOS：基础导航支持
    - Desktop：窗口导航支持
    - Web：浏览器历史集成

### 2. Navigation 3（官方，KMP 支持）⭐⭐

**状态**：稳定版本，明确支持 KMP  
**维护方**：Google/JetBrains（官方）  
**官方文档**：

- [Android Navigation 3](https://developer.android.com/guide/navigation/navigation-3)
- [Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) ⭐

#### 核心特性

- ✅ **明确支持 KMP**：官方文档明确支持 KMP（[官方证据](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)）
- ✅ **完全控制返回栈**：提供对返回栈的完全控制，导航就像在列表中添加和删除项
- ✅ **多目的地显示**：支持自适应布局，可同时显示多个目的地
- ✅ **自适应布局系统**：支持同时显示多个目的地，支持在不同布局间无缝切换
- ✅ **简单集成**：与 Compose 深度集成
- ✅ **灵活布局**：支持读取多个目的地，适应窗口大小变化
- ✅ **官方维护**：Google/JetBrains 官方维护，长期支持有保障

#### 官方证据

1. **JetBrains 官方文档**（[Kotlin 官方文档](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)）：

    - Navigation 3 已经与 Compose Multiplatform 整合，可以在多平台 KMP 项目中使用
    - 可以在 `commonMain` 的 KMP 模块内引入 Nav3 依赖，在 Android、iOS、Web、Desktop 等平台上共享导航逻辑

2. **Android 官方 Release Notes**（[Android Developers](https://developer.android.com/jetpack/androidx/releases/navigation3)）：

    - Navigation3 Runtime 添加了 **Kotlin Multiplatform Targets（KMP）**
    - 支持多个平台：JVM（Android + Desktop）、Native（Linux、iOS、watchOS、macOS、MinGW）、Web（JavaScript / WasmJS）

3. **Compose Multiplatform 1.10.x**（[Kotlin 官方文档](https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html)）：
    - Compose Multiplatform 在 **1.10.0-beta 及之后版本** 提供 Navigation 3 的 **多平台支持**

#### 支持的平台

| 平台                         | 支持情况   | 说明                                   |
|----------------------------|--------|--------------------------------------|
| **Android**                | ✅ 完全支持 | 包括 UI 和 runtime                      |
| **iOS**                    | ✅ 支持   | KMP Navigation3 runtime，可用于 CMP UI   |
| **Web (JS / Wasm)**        | ✅ 支持   | Runtime 支持；UI 结合 Compose CMP/浏览器库可实现 |
| **Desktop (JVM + Native)** | ✅ 支持   | KMP runtime 支持                       |

#### 依赖

```kotlin
dependencies {
    // Navigation 3（KMP 支持）
    implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0")
    // 或
    implementation("androidx.navigation:navigation-compose-3:1.0.0")
}
```

#### 适用场景

- ✅ KMP 跨平台应用
- ✅ 需要完全控制返回栈的应用
- ✅ 需要复杂自适应布局的应用
- ✅ 需要多目的地同时显示的应用

### 3. Voyager（第三方）

**状态**：稳定版本  
**维护方**：社区（AdrielCafe）  
**GitHub**：https://github.com/adrielcafe/voyager

#### 特点

- ✅ **成熟稳定**：已有多版本发布，社区活跃
- ✅ **功能完整**：支持导航栈、屏幕参数、转场动画等
- ✅ **生命周期管理**：自动管理屏幕生命周期
- ✅ **多平台支持**：Android、iOS、Desktop、Web
- ✅ **类型安全**：支持类型安全的导航
- ⚠️ **第三方库**：非官方维护，依赖社区支持

#### 依赖

```kotlin
dependencies {
    implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")
    implementation("cafe.adriel.voyager:voyager-transitions:1.0.0")
    implementation("cafe.adriel.voyager:voyager-tab-navigator:1.0.0") // 如果需要 Tab 导航
}
```

### 4. Decompose（JetBrains 员工维护）

**状态**：稳定版本  
**维护方**：JetBrains（非官方，但由 JetBrains 员工维护）  
**GitHub**：https://github.com/arkivanov/Decompose

#### 特点

- ✅ **组件化架构**：基于组件化设计，适合大型应用
- ✅ **生命周期管理**：完整的组件生命周期管理
- ✅ **多平台支持**：Android、iOS、Desktop、Web
- ⚠️ **架构复杂**：需要重构现有架构，学习曲线陡峭
- ⚠️ **过度设计**：对于简单导航需求可能过于复杂

#### 依赖

```kotlin
dependencies {
    implementation("com.arkivanov.decompose:decompose:2.0.0")
    implementation("com.arkivanov.decompose:extensions-compose:2.0.0")
}
```

---

## 库详细对比

| 对比维度       | Compose Multiplatform Navigation | Navigation 3         | Voyager        | Decompose         |
|------------|----------------------------------|----------------------|----------------|-------------------|
| **维护方**    | JetBrains（官方）                    | Google/JetBrains（官方） | 社区（AdrielCafe） | JetBrains 员工（非官方） |
| **稳定性**    | ⚠️ Alpha 阶段                      | ✅ 稳定                 | ✅ 稳定           | ✅ 稳定              |
| **KMP 支持** | ✅ 官方支持                           | ✅ 明确支持               | ✅ 支持           | ✅ 支持              |
| **文档质量**   | ⭐⭐⭐⭐ 官方文档完善                      | ⭐⭐⭐⭐ 官方文档            | ⭐⭐⭐⭐ 良好        | ⭐⭐⭐⭐ 良好           |
| **社区支持**   | ⭐⭐⭐ 官方支持                         | ⭐⭐⭐ 官方支持             | ⭐⭐⭐⭐ 活跃        | ⭐⭐⭐ 一般            |
| **学习曲线**   | ⭐⭐⭐ 中等                           | ⭐⭐⭐ 中等               | ⭐⭐⭐⭐ 简单        | ⭐⭐ 陡峭             |
| **集成难度**   | ⭐⭐⭐ 中等                           | ⭐⭐⭐ 中等               | ⭐⭐⭐⭐ 简单        | ⭐⭐ 困难（需重构）        |
| **功能完整性**  | ⭐⭐⭐⭐ 持续完善                        | ⭐⭐⭐⭐⭐ 完整             | ⭐⭐⭐⭐⭐ 完整       | ⭐⭐⭐⭐ 完整           |
| **栈管理**    | ✅ 自动管理                           | ✅ 完全控制               | ✅ 自动管理         | ✅ 自动管理            |
| **生命周期**   | ✅ 自动管理                           | ✅ 自动管理               | ✅ 自动管理         | ✅ 完整管理            |
| **类型安全**   | ✅ 支持（1.7.0+）                     | ✅ 支持                 | ✅ 支持           | ✅ 支持              |
| **深度链接**   | ✅ 支持                             | ✅ 支持                 | ✅ 支持           | ✅ 支持              |
| **返回手势**   | ⚠️ 仅 Android                     | ✅ Android            | ✅ 全平台          | ✅ 全平台             |
| **Tab 导航** | ⚠️ 需自定义实现                        | ⚠️ 需自定义实现            | ✅ 支持           | ✅ 支持              |
| **转场动画**   | ⭐⭐⭐ 基础支持                         | ⭐⭐⭐⭐ 支持              | ⭐⭐⭐⭐⭐ 丰富支持     | ⭐⭐⭐⭐ 支持           |
| **自适应布局**  | ⚠️ 基础支持                          | ✅ 完整支持               | ⚠️ 基础支持        | ✅ 支持              |
| **多目的地显示** | ⚠️ 基础支持                          | ✅ 完整支持               | ⚠️ 基础支持        | ✅ 支持              |

---

## 需求匹配度分析

### MyHub 需求

1. **主导航项独立导航栈**：每个主导航项（Dashboard、Profile、Settings 等）维护独立的导航栈
2. **状态保存与恢复**：切换主导航项时自动保存，切换回时自动恢复
3. **返回操作作用域**：返回操作只在当前主导航项的栈内进行
4. **生命周期管理**：自动管理屏幕的创建和销毁

### 库匹配度

#### Navigation 3（官方）

**匹配度**：⭐⭐⭐⭐⭐（98%）

**优势**：

- ✅ **明确支持 KMP**：官方文档明确支持 KMP（[官方证据](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)）
- ✅ **完全控制返回栈**：提供对返回栈的完全控制，完美匹配需求
- ✅ **自适应布局**：支持多目的地同时显示，适合主导航项独立栈需求
- ✅ **官方维护**：Google/JetBrains 官方维护，长期支持有保障
- ✅ **功能完善**：支持类型安全、深度链接、结果处理等
- ✅ **多平台支持**：Android、iOS、Desktop、Web 全平台支持

**劣势**：

- ⚠️ **API 复杂度**：相比 Compose Multiplatform Navigation，API 稍复杂
- ⚠️ **学习曲线**：需要理解 BackStack 和 NavDisplay 的概念

**实现主导航项独立栈的方案**：

使用 Navigation 3 的 BackStack 和 NavDisplay 实现：

```kotlin
// 使用 Navigation 3 实现主导航项独立栈
import androidx.navigation.compose.navigation3.NavDisplay
import androidx.navigation.compose.navigation3.BackStack
import androidx.navigation.compose.navigation3.rememberBackStack

@Composable
fun AppNavigation() {
    // 为每个主导航项创建独立的 BackStack
    val dashboardBackStack = rememberBackStack(initialKey = Screen.Dashboard)
    val profileBackStack = rememberBackStack(initialKey = Screen.Profile)

    // 当前活跃的主导航项
    var currentPrimaryBackStack by remember { mutableStateOf(dashboardBackStack) }

    // 使用 NavDisplay 显示返回栈
    NavDisplay(
        backStack = currentPrimaryBackStack,
        sceneStrategy = { /* 场景策略，支持自适应布局 */ }
    ) { entry ->
        // 根据返回栈条目显示内容
        when (val screen = entry.key) {
            is Screen.Dashboard -> DashboardScreen()
            is Screen.CardDetail -> CardDetailScreen(screen.cardId)
            // ...
        }
    }
}
```

**结论**：**强烈推荐**。官方库，明确支持 KMP，提供完全控制返回栈，完美匹配需求。

#### Compose Multiplatform Navigation（官方）

**匹配度**：⭐⭐⭐⭐（85%）

**优势**：

- ✅ **官方维护**：JetBrains 官方维护，长期支持有保障
- ✅ **自动栈管理**：自动管理导航栈的创建和销毁，无需手动处理
- ✅ **类型安全**：1.7.0+ 支持类型安全的导航（[官方文档](https://kotlinlang.org/docs/multiplatform/compose-navigation-routing.html)）
- ✅ **深度链接支持**：支持深度链接和结果处理（[官方文档](https://kotlinlang.org/docs/multiplatform/compose-navigation-deep-links.html)）
- ✅ **返回栈控制**：基于 AndroidX Navigation，支持返回栈的完全控制（[参考文档](https://developer.android.com/guide/navigation/backstack)）
- ✅ **多平台支持**：Android、iOS、Desktop、Web 全平台支持
- ✅ **Web 集成**：Web 平台支持浏览器历史同步

**劣势**：

- ⚠️ **Alpha 阶段**：功能可能不完整，API 可能变化（但持续完善中）
- ⚠️ **Tab 导航**：需要自定义实现主导航项独立栈（但可以通过多个 NavController 实现）
- ⚠️ **返回手势**：非 Android 平台可能需要手动实现返回手势

**实现主导航项独立栈的方案**：

虽然 Compose Multiplatform Navigation 不直接支持 Tab Navigator，但可以通过以下方式实现：

```kotlin
// 方案：使用多个 NavController 管理不同主导航项的栈
class AppViewModel {
    // 为每个主导航项创建独立的 NavController
    private val dashboardNavController = rememberNavController()
    private val profileNavController = rememberNavController()
    private val settingsNavController = rememberNavController()

    // 当前活跃的主导航项
    private var currentPrimaryNav: NavController = dashboardNavController

    // 切换主导航项时，保存当前栈状态，切换到新的 NavController
    fun switchPrimaryNav(navController: NavController) {
        currentPrimaryNav = navController
    }
}
```

**结论**：**强烈推荐**。官方库，功能完善，可以通过多个 NavController 实现主导航项独立栈。

#### Voyager

**匹配度**：⭐⭐⭐⭐⭐（95%）

**优势**：

- ✅ 稳定版本，功能完整
- ✅ 支持 Tab Navigator，完美匹配主导航项独立栈需求
- ✅ 自动管理导航栈和生命周期
- ✅ 支持状态保存与恢复
- ✅ 支持返回手势（全平台）
- ✅ 类型安全
- ✅ 丰富的转场动画支持
- ✅ 文档完善，社区活跃

**劣势**：

- ⚠️ 第三方库，非官方维护（但维护活跃）

**结论**：**强烈推荐**。完美匹配需求，特别是 Tab Navigator 功能。

#### Decompose

**匹配度**：⭐⭐⭐（50%）

**优势**：

- ✅ 功能完整
- ✅ 完整的生命周期管理
- ✅ 组件化架构

**劣势**：

- ❌ 需要重构现有架构
- ❌ 学习曲线陡峭
- ❌ 对于导航需求可能过度设计

**结论**：**不推荐**。架构复杂，集成成本高。

---

## 推荐方案

### 方案 A：使用 Navigation 3（官方，强烈推荐）⭐⭐⭐

#### 理由

1. **明确支持 KMP**：官方文档明确支持 KMP（[官方证据](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)）
2. **完全控制返回栈**：提供对返回栈的完全控制，完美匹配需求
3. **自适应布局**：支持多目的地同时显示，适合主导航项独立栈需求
4. **官方维护**：Google/JetBrains 官方维护，长期支持有保障
5. **功能完善**：支持类型安全、深度链接、结果处理等
6. **多平台支持**：Android、iOS、Desktop、Web 全平台支持

#### 实现示例

```kotlin
// 使用 Navigation 3 实现主导航项独立栈
import androidx.navigation.compose.navigation3.NavDisplay
import androidx.navigation.compose.navigation3.BackStack
import androidx.navigation.compose.navigation3.rememberBackStack

@Composable
fun AppNavigation() {
    // 为每个主导航项创建独立的 BackStack
    val dashboardBackStack = rememberBackStack(initialKey = Screen.Dashboard)
    val profileBackStack = rememberBackStack(initialKey = Screen.Profile)
    val settingsBackStack = rememberBackStack(initialKey = Screen.Settings)

    // 当前活跃的主导航项
    var currentPrimaryBackStack by remember { mutableStateOf(dashboardBackStack) }

    // 使用 NavDisplay 显示返回栈
    NavDisplay(
        backStack = currentPrimaryBackStack,
        sceneStrategy = { /* 场景策略，支持自适应布局 */ }
    ) { entry ->
        // 根据返回栈条目显示内容
        when (val screen = entry.key) {
            is Screen.Dashboard -> DashboardScreen(
                onNavigateToCardDetail = { cardId ->
                    currentPrimaryBackStack.push(Screen.CardDetail(cardId))
                }
            )
            is Screen.CardDetail -> CardDetailScreen(
                cardId = screen.cardId,
                onNavigateBack = { currentPrimaryBackStack.pop() }
            )
            is Screen.Profile -> ProfileScreen(
                onNavigateToSettings = {
                    currentPrimaryBackStack.push(Screen.Settings)
                }
            )
            is Screen.Settings -> SettingsScreen(
                onNavigateBack = { currentPrimaryBackStack.pop() }
            )
        }
    }
}
```

#### 优势

- ✅ **明确支持 KMP**：官方文档明确支持 KMP，这是 KMP 项目的首选
- ✅ **完全控制返回栈**：提供对返回栈的完全控制，完美匹配需求
- ✅ **自适应布局**：支持多目的地同时显示，适合主导航项独立栈需求
- ✅ **官方维护**：Google/JetBrains 官方维护，长期支持有保障
- ✅ **功能完善**：支持类型安全、深度链接、结果处理等
- ✅ **多平台支持**：Android、iOS、Desktop、Web 全平台支持
- ✅ **状态保留**：返回栈中的项自动保留状态

#### 实施成本

- **开发工作量**：1-2 个工作日
- **学习成本**：中等（需要理解 BackStack 和 NavDisplay 的概念）
- **迁移成本**：中等（需要重构导航代码，但 API 清晰）

#### 依赖配置

```kotlin
dependencies {
    // Navigation 3（KMP 支持）
    implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0")
    // 或
    implementation("androidx.navigation:navigation-compose-3:1.0.0")
}
```

### 方案 B：使用 Compose Multiplatform Navigation（官方，备选方案）⭐⭐

#### 理由

1. **明确支持 KMP**：官方文档明确支持 KMP（Android、iOS、Desktop、Web）
2. **自动栈管理**：官方库自动处理栈的创建、销毁和生命周期，无需手动管理
3. **功能完善**：支持类型安全导航、深度链接、返回栈控制等
4. **多平台支持**：Android、iOS、Desktop、Web 全平台支持
5. **API 简单**：API 设计简单直观，学习曲线平缓
6. **官方维护**：JetBrains 官方维护，长期支持有保障

#### 实现示例

```kotlin
// 使用 Compose Multiplatform Navigation 实现主导航项独立栈
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    // 为每个主导航项创建独立的 NavController
    val dashboardNavController = rememberNavController()
    val profileNavController = rememberNavController()
    val settingsNavController = rememberNavController()

    // 当前活跃的主导航项
    var currentPrimaryNav by remember { mutableStateOf(dashboardNavController) }

    // 每个 NavController 自动维护独立的返回栈
    when (currentPrimaryNav) {
        dashboardNavController -> {
            NavHost(
                navController = dashboardNavController,
                startDestination = "dashboard"
            ) {
                composable("dashboard") { DashboardScreen() }
                composable("card_detail/{cardId}") { backStackEntry ->
                    val cardId = backStackEntry.arguments?.getString("cardId") ?: ""
                    CardDetailScreen(cardId = cardId)
                }
            }
        }
        // ... 其他主导航项
    }
}
```

#### 优势

- ✅ **明确支持 KMP**：官方文档明确支持 KMP，这是 KMP 项目的首选
- ✅ **自动栈管理**：官方库自动处理栈的创建、销毁和生命周期
- ✅ **API 简单**：API 设计简单直观，学习曲线平缓
- ✅ **功能完善**：支持类型安全导航、深度链接、返回栈控制等
- ✅ **多平台支持**：Android、iOS、Desktop、Web 全平台支持
- ✅ **官方维护**：JetBrains 官方维护，长期支持有保障

#### 实施成本

- **开发工作量**：1-2 个工作日
- **学习成本**：低（API 简单直观）
- **迁移成本**：中等（需要重构导航代码，但 API 清晰）

#### 依赖配置

```kotlin
dependencies {
    // Compose Multiplatform Navigation
    implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
}
```

### 方案 C：使用 Voyager（备选方案）⭐

#### 理由

1. **完美匹配需求**：Voyager 的 Tab Navigator 功能完美支持主导航项独立栈
2. **稳定可靠**：稳定版本，社区活跃，维护良好
3. **功能完整**：支持所有需求功能
4. **集成简单**：API 设计友好，学习曲线平缓
5. **自动管理**：自动处理导航栈的创建、销毁和生命周期

#### 实现示例

```kotlin
// 使用 Voyager 实现主导航项独立栈
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.TabNavigator

// 主导航项定义
sealed class PrimaryTab : Tab {
    object Dashboard : PrimaryTab()
    object Profile : PrimaryTab()
    object Settings : PrimaryTab()
}

// 使用 TabNavigator 管理主导航项
TabNavigator(Dashboard) { tabNavigator ->
    // 每个 Tab 有独立的 Navigator
    when (val tab = tabNavigator.current) {
        is Dashboard -> Navigator(DashboardScreen()) { dashboardNavigator ->
            // Dashboard 的导航栈
            // 可以导航到 CardDetail、Settings 等
        }
        is Profile -> Navigator(ProfileScreen()) { profileNavigator ->
            // Profile 的导航栈
        }
        is Settings -> Navigator(SettingsScreen()) { settingsNavigator ->
            // Settings 的导航栈
        }
    }
}
```

#### 优势

- ✅ **自动栈管理**：Voyager 自动管理每个 Tab 的导航栈
- ✅ **状态保持**：切换 Tab 时自动保存状态，切换回时自动恢复
- ✅ **生命周期管理**：自动处理屏幕的创建和销毁
- ✅ **无需手动管理**：不需要手动创建和销毁栈

#### 实施成本

- **开发工作量**：1-2 个工作日（比自实现更少）
- **学习成本**：低（API 简单直观）
- **迁移成本**：中等（需要重构导航代码）

### 方案 D：自实现（不推荐）

#### 理由

1. **完全控制**：可以完全控制导航逻辑
2. **无外部依赖**：不引入第三方库
3. **轻量级**：只实现需要的功能

#### 劣势

- ❌ **需要手动管理**：需要手动处理栈的创建、销毁、生命周期
- ❌ **潜在问题**：可能出现内存泄漏、状态不一致等问题
- ❌ **维护成本**：需要自己维护和测试

---

## 最终推荐

### 推荐使用 Navigation 3（官方）⭐⭐⭐

**理由**：

1. ✅ **明确支持 KMP**：官方文档明确支持 KMP（[官方证据](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)），这是 KMP 项目的首选
2. ✅ **完全控制返回栈**：提供对返回栈的完全控制，完美匹配需求
3. ✅ **自适应布局**：支持多目的地同时显示，适合主导航项独立栈需求
4. ✅ **官方维护**：Google/JetBrains 官方维护，长期支持有保障
5. ✅ **功能完善**：支持类型安全、深度链接、结果处理等
6. ✅ **多平台支持**：Android、iOS、Desktop、Web 全平台支持
7. ✅ **实施成本低**：API 清晰，文档完善，比自实现更简单
8. ✅ **健壮性更好**：经过官方测试，避免自实现可能出现的问题

### 备选方案

#### 方案 B：Compose Multiplatform Navigation

如果不需要完全控制返回栈，可以使用 Compose Multiplatform Navigation：

- ✅ 明确支持 KMP
- ✅ 自动栈管理，API 简单
- ✅ 学习曲线平缓
- ⚠️ 自适应布局支持较弱

#### 方案 C：Voyager

如果官方库无法满足特定需求，可以考虑 Voyager：

- ✅ 稳定版本，功能完整
- ✅ Tab Navigator 功能完善
- ⚠️ 第三方库，非官方维护

### 实施建议（Navigation 3）

1. **评估阶段**（0.5 天）：

    - 阅读官方文档（[Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)、[Android Navigation 3](https://developer.android.com/guide/navigation/navigation-3)）
    - 创建 POC（概念验证）项目
    - 验证 BackStack 和 NavDisplay 方案是否满足主导航项独立栈需求
    - 评估集成难度

2. **集成阶段**（1-2 天）：

    - 添加 Navigation 3 依赖（`org.jetbrains.androidx.navigation3:navigation3-ui`）
    - 重构导航代码，使用 Navigation 3 API
    - 实现 BackStack 和 NavDisplay 方案（主导航项独立栈）
    - 实现场景策略，支持自适应布局
    - 测试所有导航场景

3. **优化阶段**（0.5 天）：
    - 优化转场动画
    - 实现深度链接支持
    - 性能优化
    - 文档更新

**总工作量**：2-3 个工作日（与自实现相当，但更健壮，且有官方支持）

### 关键参考资料

**Navigation 3**：

- [Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) - JetBrains 官方文档 ⭐
- [Android Navigation 3](https://developer.android.com/guide/navigation/navigation-3) - Android 官方文档
- [Navigation3 Release Notes](https://developer.android.com/jetpack/androidx/releases/navigation3) - Android 官方 Release Notes
- [Compose Multiplatform 1.10.0 What's New](https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html) - Compose Multiplatform 更新说明

**Compose Multiplatform Navigation**：

- [Compose Multiplatform Navigation 官方文档](https://kotlinlang.org/docs/multiplatform/compose-navigation.html)
- [类型安全路由](https://kotlinlang.org/docs/multiplatform/compose-navigation-routing.html)
- [深度链接和结果处理](https://kotlinlang.org/docs/multiplatform/compose-navigation-deep-links.html)
- [Android Navigation Back Stack](https://developer.android.com/guide/navigation/backstack)

---

## 结论

### 强烈建议使用 Navigation 3（官方）⭐⭐⭐

**理由**：

1. ✅ **明确支持 KMP**：官方文档明确支持 KMP（[官方证据](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)），这是 KMP 项目的首选
2. ✅ **完全控制返回栈**：提供对返回栈的完全控制，完美匹配需求
3. ✅ **自适应布局**：支持多目的地同时显示，适合主导航项独立栈需求
4. ✅ **官方维护**：Google/JetBrains 官方维护，长期支持有保障
5. ✅ **功能完善**：支持类型安全、深度链接、结果处理等
6. ✅ **多平台支持**：Android、iOS、Desktop、Web 全平台支持
7. ✅ **实施成本低**：API 清晰，文档完善，比自实现更简单
8. ✅ **健壮性更好**：经过官方测试，避免自实现可能出现的问题

### 关于栈管理问题

**使用 Navigation 3 的优势**：

- ✅ **完全控制返回栈**：可以完全控制返回栈的推入和弹出操作
- ✅ **自动状态保留**：返回栈中的项自动保留状态
- ✅ **生命周期管理**：自动处理屏幕生命周期，避免内存泄漏
- ✅ **经过充分测试**：官方库经过广泛测试，处理各种边界情况

**使用 Compose Multiplatform Navigation 的优势**：

- ✅ **自动创建和销毁**：官方库自动处理栈的创建和销毁，无需手动管理
- ✅ **生命周期管理**：自动处理屏幕生命周期，避免内存泄漏
- ✅ **状态保存和恢复**：内置状态保存和恢复机制
- ✅ **经过充分测试**：官方库经过广泛测试，处理各种边界情况

**自实现的风险**：

- ❌ 需要手动处理栈的创建和销毁时机
- ❌ 需要手动管理生命周期
- ❌ 可能出现内存泄漏
- ❌ 可能出现状态不一致
- ❌ 需要自己处理各种边界情况

### 选择建议

**选择 Navigation 3 的场景**：

- ✅ 需要完全控制返回栈
- ✅ 需要自适应布局和多目的地显示
- ✅ 复杂的导航需求
- ✅ 需要精细控制导航行为

**选择 Compose Multiplatform Navigation 的场景**：

- ✅ 简单的导航需求
- ✅ 需要快速上手
- ✅ 不需要完全控制返回栈
- ✅ 偏好自动栈管理

### 备选方案

如果官方库无法满足特定需求，可以考虑：

1. **Voyager**：稳定版本，Tab Navigator 功能完善
2. **自实现**：完全控制，但需要承担栈管理的所有责任

---

**文档生成时间**：2026-01-10  
**状态**：待评审

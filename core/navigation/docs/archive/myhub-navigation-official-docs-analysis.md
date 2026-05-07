# 官方导航文档技术点分析

**文档版本**：v2.0  
**创建日期**：2026-01-10  
**最后更新**：2026-01-10  
**分析范围**：Compose Multiplatform Navigation 和 Navigation 3 官方文档深度分析

---

## 📋 文档链接清单

### 核心文档

1. [Compose Multiplatform Navigation 基础](https://kotlinlang.org/docs/multiplatform/compose-navigation.html)
2. [Android Navigation Back Stack](https://developer.android.com/guide/navigation/backstack)
3. [Android Navigation 3](https://developer.android.com/guide/navigation/navigation-3)
4. [Compose Multiplatform Navigation Routing](https://kotlinlang.org/docs/multiplatform/compose-navigation-routing.html)
5. [Compose Multiplatform Navigation Deep Links](https://kotlinlang.org/docs/multiplatform/compose-navigation-deep-links.html#result)
6. [Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) ⭐
7. [Navigation3 Release Notes](https://developer.android.com/jetpack/androidx/releases/navigation3) ⭐
8. [Compose Multiplatform 1.10.0 What's New](https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html) ⭐

---

## 执行摘要

### 核心发现

1. ✅ **Navigation 3 在官方层面提供了完整的 Kotlin Multiplatform Runtime 支持**：

    - 通过 Compose Multiplatform 提供跨平台 UI 抽象
    - 返回栈模型和状态管理在所有平台一致
    - **但系统级交互体验仍以 Android 为最完整实现**（需要补充平台特定的 glue code）

2. ✅ **Compose Multiplatform Navigation 明确支持 KMP**：官方文档明确说明，支持 Android、iOS、Desktop、Web

3. ✅ **两个库都可以用于 KMP 项目**：需要根据项目需求选择

### 推荐方案

- **Navigation 3**：适合需要完全控制返回栈和自适应布局的场景
- **Compose Multiplatform Navigation**：适合简单导航场景，API 更简单

---

## 1. Compose Multiplatform Navigation 基础

**链接**：https://kotlinlang.org/docs/multiplatform/compose-navigation.html

### 技术点概括

#### 1.1 核心概念

- **NavController**：导航控制器，管理导航栈和导航操作
- **NavHost**：导航宿主，定义导航图和显示当前目的地
- **NavGraph**：导航图，定义所有可能的目的地及其连接
- **Destination**：目的地，可以是 Composable、嵌套导航图或对话框

#### 1.2 多平台支持

- ✅ **Android**：完整的 Navigation 功能
- ✅ **iOS**：基础导航支持
- ✅ **Desktop**：窗口导航支持
- ✅ **Web**：浏览器历史集成，支持前进/后退按钮

#### 1.2.1 重要说明：Compose Multiplatform Navigation vs AndroidX Navigation

**关键理解**：Compose Multiplatform Navigation 与 AndroidX Navigation Compose **不是同一代码路径**。

| 维度             | AndroidX Navigation Compose | Compose Multiplatform Navigation |
|----------------|-----------------------------|----------------------------------|
| **API 形态**     | NavController / NavHost     | NavController / NavHost（相似）      |
| **实现路径**       | AndroidX 代码库                | **JetBrains 实现**（不同代码路径）         |
| **Android 行为** | 100% AndroidX               | 100% AndroidX（在 Android 平台）      |
| **非 Android**  | N/A                         | JetBrains 实现（行为相似，但非 100% 等价）    |

**这意味着**：

- ✅ API 设计相似，学习成本低
- ✅ Android 平台行为完全一致
- ⚠️ 非 Android 平台的某些特性（如 Nested graph lifecycle、ViewModelStoreOwner 行为、SavedStateHandle 细节）可能行为相似但非 100% 等价
- ⚠️ 需要在实际使用中验证平台特定行为

#### 1.3 核心功能

1. **类型安全导航**（1.7.0+）

    - 编译时类型检查
    - 类型安全的参数传递
    - 减少运行时错误

2. **返回栈管理**

    - 自动管理导航栈
    - 支持保存和恢复导航状态
    - 支持自定义返回栈行为

3. **深度链接**

    - 支持应用内深度链接
    - 支持结果处理（Result API）
    - Web 平台支持 URL 路由

4. **转场动画**
    - 支持自定义转场动画
    - 平台特定的动画效果

#### 1.4 依赖配置

```kotlin
dependencies {
    implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
}
```

#### 1.5 关键 API

```kotlin
// 创建 NavController
val navController = rememberNavController()

// 创建 NavHost
NavHost(
    navController = navController,
    startDestination = "dashboard"
) {
    composable("dashboard") { DashboardScreen() }
    composable("card_detail/{cardId}") { backStackEntry ->
        val cardId = backStackEntry.arguments?.getString("cardId") ?: ""
        CardDetailScreen(cardId = cardId)
    }
}

// 导航操作
navController.navigate("card_detail/$cardId")
navController.popBackStack()
```

---

## 2. Android Navigation Back Stack

**链接**：https://developer.android.com/guide/navigation/backstack

### 技术点概括

#### 2.1 返回栈概念

- **Back Stack**：返回栈，记录用户导航历史
- **Back Stack Entry**：返回栈条目，表示一个目的地
- **Lifecycle**：每个条目都有独立的生命周期

#### 2.2 返回栈管理

1. **自动管理**

    - Navigation 组件自动管理返回栈
    - 导航时自动推入栈
    - 返回时自动弹出栈

2. **状态保存**

    - 自动保存目的地状态
    - 支持 SavedStateHandle
    - 支持 ViewModel 状态保存

3. **自定义行为**
    - 支持自定义返回栈行为
    - 支持清除返回栈
    - 支持弹出到特定目的地

#### 2.3 关键 API

```kotlin
// 获取返回栈
val backStack = navController.backQueue

// 弹出返回栈
navController.popBackStack()

// 弹出到特定目的地
navController.popBackStack("dashboard", inclusive = false)

// 清除返回栈
navController.popBackStack("dashboard", inclusive = true)
```

#### 2.4 生命周期管理

- 每个返回栈条目都有独立的生命周期
- 自动处理生命周期的创建和销毁
- 支持 SavedStateHandle 保存状态

---

## 3. Android Navigation 3

**链接**：https://developer.android.com/guide/navigation/navigation-3

### 技术点概括

#### 3.1 核心特性

1. **完全控制返回栈**

    - 提供对返回栈的完全控制
    - 导航就像在列表中添加和删除项一样简单
    - 每个返回栈条目代表用户导航到的内容

2. **自动 UI 更新**

    - UI 自动响应返回栈变化
    - 支持动画过渡
    - 支持自适应布局

3. **作用域和状态保留**

    - 返回栈中的项有独立的作用域
    - 状态在项在返回栈中时保留
    - 支持状态恢复

4. **自适应布局系统**

    - 支持同时显示多个目的地
    - 支持在不同布局间无缝切换
    - 适应窗口大小变化

5. **元数据机制**
    - 内容可以与父布局通信
    - 支持传递元数据

#### 3.2 实现方式

1. **定义内容**

    - 定义用户可以导航到的内容
    - 每个内容有唯一的 key
    - 提供函数将 key 解析为内容

2. **创建返回栈**

    - 创建返回栈，key 被推入和移除
    - 用户导航时更新返回栈

3. **显示返回栈**

    - 使用 NavDisplay 显示返回栈
    - 返回栈变化时自动更新 UI

4. **场景策略**
    - 修改 NavDisplay 的场景策略
    - 支持自适应布局和不同平台

#### 3.3 关键优势

- ✅ **更简单的 Compose 集成**：与 Compose 深度集成
- ✅ **完全控制返回栈**：提供对返回栈的完全控制
- ✅ **多目的地布局**：可以同时读取多个目的地，适应窗口大小变化

---

## 4. Navigation 3 的 KMP 支持（官方证据）

### 4.1 官方证据确认

**重要结论**：基于官方证据，**Navigation 3 在官方层面提供了完整的 Kotlin Multiplatform Runtime 支持** ✅

#### 证据 1：JetBrains 官方文档

**链接**：[Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)

**关键说明**：

- ✅ Navigation 3 已经与 Compose Multiplatform 整合，可以在多平台 KMP 项目中使用
- ✅ 可以在 `commonMain` 的 KMP 模块内引入 Nav3 依赖，在 Android、iOS、Web、Desktop 等平台上共享导航逻辑
- ✅ Navigation 3 的 API 设计与 Compose Multiplatform 紧密结合
- ✅ 添加 `org.jetbrains.androidx.navigation3:navigation3-ui` 即可在多平台中工作

#### 证据 2：Android 官方 Release Notes

**链接**：[Navigation3 Release Notes](https://developer.android.com/jetpack/androidx/releases/navigation3)

**关键说明**：

- ✅ Navigation3 Runtime 添加了 **Kotlin Multiplatform Targets（KMP）**
- ✅ 支持多个平台：
    - JVM（Android + Desktop）
    - Native（Linux、iOS、watchOS、macOS、MinGW）
    - Web（JavaScript / WasmJS）

#### 证据 3：Compose Multiplatform 1.10.x 更新

**链接**：[Compose Multiplatform 1.10.0 What's New](https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html)

**关键说明**：

- ✅ Compose Multiplatform 在 **1.10.0-beta 及之后版本** 提供 Navigation 3 的 **多平台支持**
- ✅ 发布了对应的 Multiplatform Nav3 库 artifact

### 4.2 支持的平台

| 平台                                | 支持情况    | 说明                                            |
|-----------------------------------|---------|-----------------------------------------------|
| **Android**                       | ✅ 完全支持  | 包括 UI 和 runtime                               |
| **iOS**                           | ✅ 支持    | KMP Navigation3 runtime，可用于 CMP UI            |
| **Web (JS / Wasm)**               | ✅ 支持    | Runtime 支持；UI 结合 Compose CMP/浏览器库可实现          |
| **Desktop (JVM + Native)**        | ✅ 支持    | KMP runtime 支持                                |
| **Navigation3 UI on non-Android** | ⚠️ 部分支持 | 需要 Compose Multiplatform artifacts，有时需要自定义 UI |

**长期价值判断**：

> **Navigation 3 是目前唯一一个官方明确把 WasmJS 纳入目标平台的导航模型。**

**这意味着**：

- ✅ Navigation 3 对 Web/Wasm 的支持是官方长期投入的方向
- ✅ 如果项目需要 Web/Wasm 平台支持，Navigation 3 是更可靠的选择
- ✅ 这反映了 Google/JetBrains 对 Web 平台和 Wasm 技术的长期战略

**判断原则**：

- ✅ 如果 UI 只依赖 `NavDisplay + BackStack` → 可跨平台使用
- ⚠️ 如果依赖 Material / Adaptive / WindowSizeClass → Android 优先，其他平台需要定制

### 4.3 重要边界说明 ⚠️

**关键理解**：Navigation 3 在 KMP 中是 **Runtime-first** 的支持，而不是 Android 体验的完全平移。

#### 4.3.1 支持层级分析

| 层级                            | KMP 支持情况       | 说明                                |
|-------------------------------|----------------|-----------------------------------|
| **BackStack / Key / State**   | ✅ 完全 KMP       | 返回栈模型和状态管理在所有平台一致                 |
| **NavDisplay 抽象**             | ✅ KMP          | UI 显示抽象层支持多平台                     |
| **SceneStrategy**             | ⚠️ 平台差异明显      | 自适应布局策略需要根据平台特性实现                 |
| **Material / Adaptive UI**    | ⚠️ Android 最完整 | Material Design 在 Android 平台体验最完整 |
| **系统 Back / Predictive Back** | ❌ Android Only | 系统级返回手势和预测性返回仅在 Android 平台支持      |

#### 4.3.2 关键免责声明

> **Navigation 3 在 KMP 中提供的是导航模型与状态管理能力的一致性，而不是保证所有平台具备 Android 等价的系统交互体验。**

**这意味着**：

- ✅ 返回栈的状态管理逻辑在所有平台一致
- ✅ 导航 API 和状态保留机制在所有平台可用
- ⚠️ 平台特定的系统交互（如返回手势、预测性返回）需要额外实现
- ⚠️ UI 适配和转场动画可能需要平台特定的定制

### 4.3 依赖配置

```kotlin
dependencies {
    // Navigation 3（KMP 支持）
    implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0")
    // 或
    implementation("androidx.navigation:navigation-compose-3:1.0.0")
}
```

---

## 5. Compose Multiplatform Navigation Routing

**链接**：https://kotlinlang.org/docs/multiplatform/compose-navigation-routing.html

### 技术点概括

#### 5.1 类型安全路由

1. **路由定义**

    - 使用类型安全的方式定义路由
    - 编译时类型检查
    - 减少运行时错误

2. **参数传递**

    - 类型安全的参数传递
    - 支持可选参数
    - 支持默认值

3. **路由解析**
    - 自动路由解析
    - 类型安全的参数提取
    - 错误处理

#### 5.2 实现方式

```kotlin
// 定义路由
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    data class CardDetail(val cardId: String) : Screen("card_detail/$cardId")
}

// 使用类型安全导航
navController.navigate(Screen.CardDetail(cardId).route)
```

#### 5.3 优势

- ✅ **编译时检查**：类型错误在编译时发现
- ✅ **代码提示**：IDE 提供更好的代码提示
- ✅ **重构安全**：重构时自动更新路由引用

---

## 6. Compose Multiplatform Navigation Deep Links

**链接**：https://kotlinlang.org/docs/multiplatform/compose-navigation-deep-links.html#result

### 技术点概括

#### 6.1 深度链接

1. **应用内深度链接**

    - 支持应用内深度链接
    - 支持自定义 URL scheme
    - 支持路径参数

2. **Web 平台支持**

    - 支持浏览器 URL 路由
    - 支持前进/后退按钮
    - 支持地址栏同步

3. **结果处理（Result API）**
    - 支持从目的地返回结果
    - 类型安全的结果传递
    - 支持结果监听

#### 6.2 Result API

```kotlin
// 设置结果
navController.previousBackStackEntry?.savedStateHandle?.set("result", data)

// 获取结果
val result = navController.currentBackStackEntry?.savedStateHandle?.get<Data>("result")
```

#### 6.3 关键特性

- ✅ **类型安全**：结果传递类型安全
- ✅ **状态保存**：结果保存在 SavedStateHandle 中
- ✅ **生命周期感知**：自动处理生命周期

---

## 7. 平台返回机制的差异（重要补充）

### 7.1 问题背景

在 KMP 中，「返回」不是一个统一的概念，不同平台有不同的返回机制。

### 7.2 平台返回机制对比

| 平台          | 返回来源                        | 实现方式           |
|-------------|-----------------------------|----------------|
| **Android** | 系统 Back / Gesture           | 系统级返回按钮和边缘手势   |
| **iOS**     | UINavigationController / 手势 | 系统导航栏返回按钮和滑动手势 |
| **Desktop** | Window close / shortcut     | 窗口关闭按钮和键盘快捷键   |
| **Web**     | Browser history             | 浏览器前进/后退按钮和地址栏 |

### 7.3 关键结论

> **Navigation 3 不负责"平台返回事件的统一"，只负责"返回栈的状态变化模型"。**

**这意味着**：

- ✅ Navigation 3 管理返回栈的状态变化
- ✅ 返回栈的 push/pop 操作在所有平台一致
- ❌ Navigation 3 **不自动处理**平台特定的返回事件（如系统返回按钮、浏览器后退）
- ⚠️ 需要在 App 层编写 glue code 来连接平台返回事件和 Navigation 3 的返回栈操作

### 7.4 实现建议

**对于需要平台返回事件绑定的场景**：

1. **Web 平台**：需要绑定浏览器 history API 与 Navigation 3 返回栈
2. **iOS 平台**：需要处理系统导航栏返回按钮与 Navigation 3 返回栈的同步
3. **Desktop 平台**：需要处理窗口关闭事件与 Navigation 3 返回栈的清理

**示例代码框架**：

```kotlin
// Web 平台：绑定浏览器历史
@Composable
fun WebNavigationGlue(backStack: BackStack<Screen>) {
    LaunchedEffect(backStack) {
        // 监听浏览器前进/后退事件
        // 同步到 Navigation 3 返回栈
    }
}

// iOS 平台：处理系统返回
@Composable
fun iOSNavigationGlue(backStack: BackStack<Screen>) {
    // 处理 iOS 系统返回按钮
    // 调用 backStack.pop()
}
```

---

## 综合技术点总结

### 核心能力对比

| 能力          | Navigation 3 | Compose Multiplatform Navigation |
|-------------|--------------|----------------------------------|
| **KMP 支持**  | ✅ 明确支持       | ✅ 明确支持                           |
| **返回栈管理**   | ✅ 完全控制       | ✅ 自动管理                           |
| **自适应布局**   | ✅ 完整支持       | ⚠️ 基础支持                          |
| **多目的地显示**  | ✅ 支持         | ⚠️ 基础支持                          |
| **类型安全导航**  | ✅ 支持         | ✅ 支持（1.7.0+）                     |
| **深度链接**    | ✅ 支持         | ✅ 支持                             |
| **结果处理**    | ✅ 支持         | ✅ 支持                             |
| **API 复杂度** | ⭐⭐⭐ 中等       | ⭐⭐⭐⭐ 简单                          |
| **学习曲线**    | ⭐⭐⭐ 中等       | ⭐⭐⭐⭐ 平缓                          |

### 关键发现

1. **Navigation 3 在官方层面提供了完整的 Kotlin Multiplatform Runtime 支持** ✅

    - **官方证据**：
        - [Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) - JetBrains 官方文档
        - [Navigation3 Release Notes](https://developer.android.com/jetpack/androidx/releases/navigation3) - Android 官方 Release Notes
        - [Compose Multiplatform 1.10.0 What's New](https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html) - Compose Multiplatform 更新说明
    - **支持层级**：
        - ✅ Runtime 层：完全 KMP 支持（BackStack / Key / State）
        - ✅ UI 抽象层：NavDisplay 支持多平台
        - ⚠️ 系统交互层：Android 最完整，其他平台需要补充实现
    - 支持平台：Android、iOS、Web、Desktop
    - 提供对返回栈的完全控制
    - 支持自适应布局和多目的地显示

2. **Compose Multiplatform Navigation 明确支持 KMP** ✅

    - 官方文档在 kotlinlang.org
    - 支持 Android、iOS、Desktop、Web
    - 提供类型安全导航、深度链接等功能
    - 自动栈管理，易于使用

3. **两个库都可以用于 KMP 项目** ✅

    - **Navigation 3**：提供完全控制返回栈，支持自适应布局
    - **Compose Multiplatform Navigation**：自动栈管理，API 简单
    - 需要根据项目需求选择

4. **与其他 KMP 导航方案的定位关系** ✅
    - **Navigation 3**：UI-first / Stack-driven，更接近"官方版 Decompose-lite"
    - **Compose Multiplatform Navigation**：Graph-first，基于导航图
    - **Decompose**：State machine，组件化架构
    - **Voyager**：Screen abstraction，屏幕抽象

---

## 8. Navigation 3 在 KMP 中"不解决什么"（重要边界说明）

### 8.1 不自动解决的问题

1. **平台返回事件的统一**

    - ❌ 不自动处理系统返回按钮
    - ❌ 不自动处理浏览器前进/后退
    - ❌ 不自动处理 iOS 导航栏返回
    - ✅ 只负责返回栈的状态管理

2. **平台特定的 UI 适配**

    - ❌ 不保证所有平台 Material Design 体验完全一致
    - ❌ 不自动适配平台特定的导航栏样式
    - ⚠️ 需要根据平台特性定制 UI

3. **系统级导航集成**

    - ❌ 不自动集成 Android Predictive Back
    - ❌ 不自动集成 iOS 原生导航手势
    - ⚠️ 需要手动实现平台特定的导航集成

4. **状态序列化策略**
    - ⚠️ 状态序列化策略在不同平台可能有差异
    - ⚠️ 需要根据平台特性选择序列化方案

### 8.2 需要额外实现的能力

| 能力                          | Navigation 3 支持情况 | 需要额外实现 |
|-----------------------------|-------------------|--------|
| 多 BackStack 同步              | ⚠️ 隐含支持           | 需要手动实现 |
| 状态序列化策略                     | ⚠️ 基础支持           | 需要定制   |
| 跨平台 SavedState 行为           | ⚠️ 行为相似           | 需要验证   |
| DeepLink ↔ Web history 的一致性 | ⚠️ 基础支持           | 需要绑定   |
| Android-only 特性边界           | ❌ 不支持             | 需要平台判断 |

### 8.3 关键理解

> **Navigation 3 在 KMP 中的定位，更接近"官方版 Decompose-lite"，而不是 Android Navigation Compose 的升级版。**

**这意味着**：

- ✅ 提供导航模型和状态管理的一致性
- ✅ 提供跨平台的返回栈抽象
- ⚠️ 不提供平台特定的系统集成
- ⚠️ 需要根据项目需求补充平台特定的实现

---

## 方案推荐

### 方案 A：使用 Navigation 3（推荐用于需要完全控制返回栈的场景）⭐⭐⭐

#### 理由

1. ✅ **明确支持 KMP**：官方文档明确支持 KMP（[Kotlin 官方文档](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)）
2. ✅ **完全控制返回栈**：提供对返回栈的完全控制，导航就像在列表中添加和删除项
3. ✅ **自适应布局**：支持多目的地同时显示，适合主导航项独立栈需求
4. ✅ **官方维护**：Google/JetBrains 官方维护，长期支持有保障
5. ✅ **功能完善**：支持类型安全、深度链接、结果处理等

#### 适用场景

- 需要完全控制返回栈的场景
- 需要自适应布局和多目的地显示的场景
- 复杂的导航需求

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
            // ... 其他屏幕
        }
    }
}
```

#### 依赖配置

```kotlin
dependencies {
    // Navigation 3（KMP 支持）
    implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0")
    // 或
    implementation("androidx.navigation:navigation-compose-3:1.0.0")
}
```

---

### 方案 B：使用 Compose Multiplatform Navigation（推荐用于简单导航场景）⭐⭐

#### 理由

1. ✅ **明确支持 KMP**：官方文档明确支持 KMP（Android、iOS、Desktop、Web）
2. ✅ **自动栈管理**：官方库自动处理栈的创建、销毁和生命周期
3. ✅ **功能完善**：支持类型安全导航、深度链接、返回栈控制等
4. ✅ **官方维护**：JetBrains 官方维护，长期支持有保障
5. ✅ **API 简单**：API 设计简单直观，学习曲线平缓

#### 适用场景

- 简单的导航需求
- 需要快速上手的项目
- 不需要完全控制返回栈的场景

#### 实现示例

```kotlin
// 使用 Compose Multiplatform Navigation 实现主导航项独立栈
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
                    CardDetailScreen(cardId = backStackEntry.arguments?.getString("cardId") ?: "")
                }
            }
        }
        // ... 其他主导航项
    }
}
```

**架构差异本质**：

> **Compose Nav：多个 NavController（每个 NavHost 独立）**  
> **Nav3：多个 BackStack，共享 Display（统一显示抽象）**

**这意味着**：

- ✅ Compose Nav 中，每个主导航项有独立的 NavHost，完全隔离
- ✅ Navigation 3 中，多个 BackStack 可以共享同一个 NavDisplay，提供更灵活的显示策略
- ✅ Navigation 3 更适合需要"同时显示多个目的地"或"自适应布局"的场景

#### 依赖配置

```kotlin
dependencies {
    // Compose Multiplatform Navigation
    implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
}
```

---

## 选择建议

### 选择 Navigation 3 的场景

- ✅ 需要完全控制返回栈
- ✅ 需要自适应布局和多目的地显示
- ✅ 复杂的导航需求
- ✅ 需要精细控制导航行为
- ✅ 愿意处理平台特定的返回事件绑定

### 选择 Compose Multiplatform Navigation 的场景

- ✅ 简单的导航需求
- ✅ 需要快速上手
- ✅ 不需要完全控制返回栈
- ✅ 偏好自动栈管理
- ✅ 希望减少平台特定代码

### 与其他方案的对比

| 方案               | 本质定位                    | 适用场景             |
|------------------|-------------------------|------------------|
| **Navigation 3** | UI-first / Stack-driven | 需要完全控制返回栈，复杂导航需求 |
| **Compose Nav**  | Graph-first             | 简单导航，快速上手        |
| **Decompose**    | State machine           | 组件化架构，复杂状态管理     |
| **Voyager**      | Screen abstraction      | 屏幕抽象，简单导航栈管理     |

---

## 风险评估与边界条件

### 技术风险

1. **平台返回事件集成**

    - ⚠️ 需要手动实现平台返回事件与 Navigation 3 的绑定
    - ⚠️ Web 平台需要处理浏览器历史同步
    - ⚠️ iOS 平台需要处理系统导航栏返回

2. **状态序列化差异**

    - ⚠️ 不同平台的状态序列化策略可能有差异
    - ⚠️ 需要验证跨平台状态保存和恢复的一致性

3. **UI 适配复杂度**
    - ⚠️ Material Design 在非 Android 平台体验可能不完全一致
    - ⚠️ 需要根据平台特性定制 UI

### 实施建议

1. **分阶段实施**

    - 第一阶段：实现核心导航功能
    - 第二阶段：补充平台特定的返回事件绑定
    - 第三阶段：优化 UI 适配和转场动画

2. **平台验证**
    - 在每个目标平台验证返回栈行为
    - 验证状态保存和恢复的一致性
    - 测试平台特定的导航集成

---

## 官方证据链接

1. [Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) - JetBrains 官方文档
2. [Navigation3 Release Notes](https://developer.android.com/jetpack/androidx/releases/navigation3) - Android 官方 Release Notes
3. [Compose Multiplatform 1.10.0 What's New](https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html) - Compose Multiplatform 更新说明
4. [Compose Multiplatform Navigation 基础](https://kotlinlang.org/docs/multiplatform/compose-navigation.html)
5. [Android Navigation Back Stack](https://developer.android.com/guide/navigation/backstack)
6. [Android Navigation 3](https://developer.android.com/guide/navigation/navigation-3)
7. [Compose Multiplatform Navigation Routing](https://kotlinlang.org/docs/multiplatform/compose-navigation-routing.html)
8. [Compose Multiplatform Navigation Deep Links](https://kotlinlang.org/docs/multiplatform/compose-navigation-deep-links.html#result)

---

---

## 9. 高级补充（可选）

### 9.1 Navigation 3 与状态管理的关系

**关键理解**：Navigation 3 的 BackStack 更适合作为 **UI Navigation State**，而不是业务状态（Business State）。

**核心概念：Navigation 3 的 Scope 语义**

> **Navigation 3 的 scope ≠ ViewModel scope**  
> 它更接近 **"导航存在性边界"**（Navigation Existence Boundary）

**这意味着**：

- ✅ BackStack 用于管理导航状态（当前屏幕、返回栈历史）
- ✅ BackStack entry 的 scope 表示"该屏幕在返回栈中存在"的生命周期边界
- ❌ 不应将业务数据（如用户信息、API 响应）存储在 BackStack 中
- ✅ 业务状态应使用 ViewModel、StateFlow 或其他状态管理方案
- ✅ Navigation 3 与业务状态管理方案（如 MVI、State Holder）可以很好地配合使用

**为什么重要**：

- ✅ **为什么 Nav3 不替代业务状态**：Scope 只管理导航存在性，不管理业务逻辑状态
- ✅ **为什么 BackStack entry 生命周期很重要**：Entry 的生命周期决定了屏幕的创建、显示、隐藏、销毁时机，这是导航状态管理的核心

**最佳实践**：

```kotlin
// ✅ 正确：BackStack 只存储导航相关的 key
backStack.push(Screen.CardDetail(cardId = "123"))

// ❌ 错误：不要在 BackStack 中存储业务数据
// backStack.push(Screen.CardDetail(cardData = fullCardObject))
```

### 9.2 测试维度（KMP 优势）

Navigation 3 的 BackStack 是纯 Kotlin 实现，非常适合在 `commonTest` 中进行测试。

**可测试内容**：

1. **返回栈操作测试**

   ```kotlin
   @Test
   fun testBackStackPushPop() {
       val backStack = BackStack(initialKey = Screen.Dashboard)
       backStack.push(Screen.CardDetail("123"))
       assertEquals(2, backStack.size)
       backStack.pop()
       assertEquals(1, backStack.size)
   }
   ```

2. **状态恢复测试**
   ```kotlin
   @Test
   fun testStateRestoration() {
       val backStack = BackStack(initialKey = Screen.Dashboard)
       backStack.push(Screen.CardDetail("123"))
       // 模拟状态保存和恢复
       val savedState = backStack.saveState()
       val restoredBackStack = BackStack.restoreState(savedState)
       assertEquals(backStack.size, restoredBackStack.size)
   }
   ```

**优势**：

- ✅ 可以在 `commonTest` 中编写测试，所有平台共享
- ✅ 不依赖平台特定的 UI 框架
- ✅ 测试执行速度快，适合 CI/CD

### 9.3 未来演进判断（趋势分析）

**技术趋势观察**：

从 AndroidX 与 JetBrains 的投入方向看，Navigation 3 更像是**未来多设备 / 自适应导航的基础抽象**，而不是 Compose Navigation 的简单替代。

**证据支撑**：

- ✅ Navigation 3 强调自适应布局和多目的地显示
- ✅ 设计理念更接近"导航状态机"而非"路由图"
- ✅ 与 Compose Multiplatform 的深度整合表明长期投入

**对选型的启示**：

- 如果项目需要**长期维护和演进**，Navigation 3 可能是更好的选择
- 如果项目需要**快速上线**，Compose Multiplatform Navigation 可能更合适
- 如果项目需要**复杂的自适应布局**，Navigation 3 更适合

---

## 附录：快速决策指南

### 何时选择 Navigation 3？

✅ **选择 Navigation 3 如果**：

- 需要完全控制返回栈
- 需要自适应布局和多目的地显示
- 项目需要长期维护和演进
- 愿意处理平台特定的返回事件绑定
- 需要复杂的导航状态管理

### 何时选择 Compose Multiplatform Navigation？

✅ **选择 Compose Multiplatform Navigation 如果**：

- 简单的导航需求
- 需要快速上手
- 不需要完全控制返回栈
- 偏好自动栈管理
- 希望减少平台特定代码

### 何时考虑其他方案？

✅ **考虑 Decompose 如果**：

- 需要组件化架构
- 需要复杂的状态管理
- 需要完整的组件生命周期管理

✅ **考虑 Voyager 如果**：

- 需要简单的屏幕抽象
- 需要 Tab Navigator 等开箱即用功能
- 偏好第三方库的灵活性

---

**文档生成时间**：2026-01-10  
**最后更新**：2026-01-10  
**文档版本**：v2.1（已优化措辞精度，补充高级内容）  
**状态**：可直接用于技术评审和技术选型决策  
**适用对象**：技术团队、架构评审委员会、技术选型决策者

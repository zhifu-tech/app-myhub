# MyHub 导航方案设计

**方案名称**：Navigation Infra v1  
**文档版本**：v1.0（已锁定）  
**文档类型**：技术方案设计文档  
**创建日期**：2026-01-11  
**锁定日期**：2026-01-11  
**最后更新**：2026-01-11  
**作者**：MyHub Development Team  
**评审状态**：🟢 通过（Approved with Minor Fixes）  
**方案状态**：🔒 **已锁定** - 此版本已冻结，作为 Navigation Infra v1 的基线设计

---

## 📋 文档目录

1. [修改历史](#修改历史)
2. [方案状态摘要](#-方案状态摘要)
3. [架构评审反馈与关键修复](#-架构评审反馈与关键修复v10)
4. [问题背景](#1-问题背景)
5. [Now in Android 架构分析](#2-now-in-android-架构分析)
6. [KMP 导航技术方案对比](#3-kmp-导航技术方案对比)
7. [MyHub 导航方案设计](#4-myhub-导航方案设计)
8. [核心概念详解](#5-核心概念详解)
9. [实现细节](#6-实现细节)
10. [实施计划](#7-实施计划)
11. [风险评估](#8-风险评估)
12. [附录](#9-附录)

**相关文档**：

- [官方导航文档技术点分析](./NAVIGATION_OFFICIAL_DOCS_ANALYSIS.md) - 详细技术调研
- [KMP Navigation 能力全景图](./NAVIGATION_CAPABILITY_MAP.md) - 能力对比可视化
- [KMP 导航库调研与评估](./NAVIGATION_LIBRARY_EVALUATION.md) - 库详细对比

---

## 📊 方案状态摘要

**当前状态**：

- **评审状态**：🟢 通过（Approved with Minor Fixes）
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Navigation Infra v1 的基线设计
- **锁定日期**：2026-01-11
- **当前进度**：方案设计已完成并通过评审，已锁定作为基线设计

**状态说明**：

- **评审状态**：用于标识文档的评审进度
  - 🟢 通过：文档已通过评审，可以进入实施阶段
  - 🟡 待评审：文档正在等待评审或评审进行中
  - 🔴 需修改：文档评审后需要修改
- **方案状态**：用于标识方案的实施进度
  - 🔒 已锁定：方案设计已确定，不允许随意修改（如需修改需要提交设计变更申请）
  - 📝 进行中：方案设计正在进行中，可以修改
  - ⏸️ 暂停：方案设计暂时停止，保留当前状态
- 详细状态定义请参考 [MyHub 架构设计文档规范](../../../docs/infra/myhub-infra-rules.md)

---

## 修改历史

| 版本   | 日期         | 修改内容                                | 修改原因 |
|------|------------|-------------------------------------|------|
| v1.0 | 2026-01-11 | 初始方案设计，包含 NIA 分析、KMP 方案对比和 MyHub 设计 | 新建   |

---

## 🔧 架构评审反馈与关键修复（v1.0）

本文档已通过架构评审，并根据评审反馈进行了关键修复。以下是主要修复点：

### ✅ 已修复的关键问题

1. **`rememberAppNavigationState` 的 remember 方式修复**
    - **问题**：原实现中 `rememberNavBackStack` 在 `associateWith` 中调用，remember 作用域不稳定
    - **修复**：使用 `remember(appKeys)` 包装 subStacks 的创建，直接使用 `NavBackStack(initialKey)` 而非 Composable
    - **位置**：`4.2.2 核心组件设计` → `AppNavigationState`

2. **`goBack()` 在 AppKey 底部的行为明确**
    - **问题**：当 AppStack 只剩一个元素时，`removeLastOrNull()` 行为不明确
    - **修复**：明确约束 AppStack 至少保留一个元素，添加平台特定的处理说明
    - **位置**：`4.2.2 核心组件设计` → `AppNavigator.goBack()`

3. **`toEntries` 的 recomposition 成本优化**
    - **问题**：每次 recomposition 都在 mapValues，依赖 Map iteration 顺序
    - **修复**：添加性能说明和缓存策略，明确依赖关系
    - **位置**：`4.2.3 状态管理集成` → `toEntries`

### ✅ 已添加的优化

1. **AppNavKey 使用 sealed interface + data object**
    - **优化**：更贴近 Kotlin 新风格，JVM / Native 更友好
    - **位置**：`4.2.2 核心组件设计` → `AppNavKey`

2. **明确"点击当前 Tab"的行为说明**
    - **优化**：明确说明点击当前已激活的 AppNavKey 会清空 SubStack，遵循 Android BottomNav 标准行为
    - **位置**：`5.4 应用级导航项切换行为`

3. **平台 Back 行为策略枚举**
    - **优化**：定义 `PlatformBackBehavior` 枚举，为未来 Desktop / Web 扩展提供指导
    - **位置**：`5.6 返回操作`

---

## 1. 问题背景

### 1.1 用户场景

用户在 MyHub 应用中的典型导航流程：

```txt
步骤 1: Dashboard → CardDetail（点击卡片进入详情页）
步骤 2: CardDetail → Profile（通过 Navigation Rail 或底部导航进入 Profile）
步骤 3: 在 Profile 页面，点击 Navigation Rail（或底部导航）的 Dashboard Tab
步骤 4: 期望行为 → 回到 Dashboard，但继续展示离开时的 CardDetail
步骤 5: 实际行为 → 回到了 Dashboard 的根屏幕，丢失了 CardDetail 状态 ❌
```

### 1.2 问题根因

**根本原因**：当前实现使用单一全局导航状态，应用级导航项切换时未保存和恢复导航状态。

**技术层面**：

1. `AppViewModel` 中仅维护单一的 `currentScreen` 状态
2. 应用级导航项切换时，直接重置为根屏幕，未保存当前导航栈
3. 缺少应用级导航项级别的导航栈管理机制

**影响范围**：

- 用户体验：导航上下文丢失，需要重新导航到目标页面
- 产品体验：不符合主流应用的用户期望（iOS、Android 等平台的标准行为）

### 1.3 需求目标

1. ✅ **应用级导航项独立导航栈**：每个应用级导航项（Dashboard、Profile 等）维护独立的导航栈
2. ✅ **状态保持与恢复**：切换应用级导航项时在当前 Composable 生命周期内保持状态，切换回时自动恢复
3. ✅ **返回操作作用域**：返回操作只在当前应用级导航项的栈内进行
4. ✅ **生命周期管理**：自动管理屏幕的创建和销毁
5. ✅ **KMP 支持**：支持 Android、iOS、Desktop、Web

---

## 2. Now in Android 架构分析

### 2.1 架构概览

Now in Android (NIA) 采用 **Navigation 3** 作为导航基础，构建了一个**双层返回栈架构**：

```txt
┌─────────────────────────────────────────────────────────┐
│                    NavigationState                      │
│  ┌──────────────────────────────────────────────────┐  │
│  │  TopLevelStack (顶层返回栈)                      │  │
│  │  [ForYouNavKey, BookmarksNavKey, InterestsNavKey]│  │
│  └──────────────────────────────────────────────────┘  │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │  SubStacks (子返回栈 Map)                        │  │
│  │  ForYouNavKey → NavBackStack<NavKey>            │  │
│  │  BookmarksNavKey → NavBackStack<NavKey>         │  │
│  │  InterestsNavKey → NavBackStack<NavKey>         │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 核心组件

#### 2.2.1 NavigationState

**位置**：`core/navigation/src/main/kotlin/.../NavigationState.kt`

**职责**：

- 管理顶层返回栈（TopLevelStack）
- 管理每个顶层目的地的子返回栈（SubStacks）
- 提供当前活跃的顶层键和当前键的访问

**关键代码**：

```kotlin
class NavigationState(
    val startKey: NavKey,
    val topLevelStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    val currentTopLevelKey: NavKey by derivedStateOf { topLevelStack.last() }
    val currentSubStack: NavBackStack<NavKey>
        get() = subStacks[currentTopLevelKey] ?: error("...")
    val currentKey: NavKey by derivedStateOf { currentSubStack.last() }
}
```

**设计特点**：

- ✅ **状态持久化**：使用 `rememberNavBackStack` 确保配置变更和进程死亡后状态保持
- ✅ **响应式**：使用 `derivedStateOf` 自动计算当前状态
- ✅ **类型安全**：所有导航键都实现 `NavKey` 接口

#### 2.2.2 Navigator

**位置**：`core/navigation/src/main/kotlin/.../Navigator.kt`

**职责**：

- 处理导航事件（前进和返回）
- 根据导航键类型执行不同的导航逻辑
- 管理返回栈的 push/pop 操作

**关键代码**：

```kotlin
class Navigator(val state: NavigationState) {
    fun navigate(key: NavKey) {
        when (key) {
            state.currentTopLevelKey -> clearSubStack()
            in state.topLevelKeys -> goToTopLevel(key)
            else -> goToKey(key)
        }
    }

    fun goBack() {
        when (state.currentKey) {
            state.startKey -> error("Cannot go back from start")
            state.currentTopLevelKey -> {
                // 在顶层键的底部，返回到上一个顶层栈
                state.topLevelStack.removeLastOrNull()
            }
            else -> state.currentSubStack.removeLastOrNull()
        }
    }
}
```

**导航逻辑**：

1. **导航到当前顶层键**：清空子栈（回到顶层根页面）
2. **导航到其他顶层键**：切换到该顶层键，保持其子栈状态
3. **导航到子键**：在当前子栈中 push 新键

#### 2.2.3 NavKey 体系

**设计模式**：每个功能模块定义自己的 NavKey

**示例**：

- `ForYouNavKey` (object) - 为你推荐
- `BookmarksNavKey` (object) - 书签
- `InterestsNavKey(data class)` - 兴趣（带参数）
- `TopicNavKey(data class)` - 话题详情（带参数）
- `SearchNavKey` (object) - 搜索

**特点**：

- ✅ **类型安全**：使用 sealed class / data class 确保类型安全
- ✅ **模块化**：每个 feature 模块定义自己的 NavKey
- ✅ **参数传递**：通过 data class 的构造函数传递参数

### 2.3 技术栈

#### 2.3.1 核心依赖

```kotlin
// build.gradle.kts
dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.savedstate.compose)
    implementation(libs.androidx.lifecycle.viewModel.navigation3)
}
```

**版本信息**（来自 Now in Android 的 `libs.versions.toml`）：

- `androidxNavigation3 = "1.0.0"`
- `androidxSavedStateCompose = "1.3.1"`
- `androidxLifecycleViewModelNavigation3 = "2.10.0"`

**依赖说明**：

- Now in Android 使用 `androidx.navigation3`（Android 专用）
- MyHub 需要使用 `org.jetbrains.androidx.navigation3`（KMP 支持）

#### 2.3.2 关键技术点

1. **Navigation 3 Runtime**

    - `NavBackStack<T>` - 返回栈抽象
    - `NavKey` - 导航键接口
    - `NavEntry` - 返回栈条目
    - `rememberNavBackStack` - 状态持久化

2. **状态管理装饰器**

    - `rememberSaveableStateHolderNavEntryDecorator` - 保存 Compose 状态
    - `rememberViewModelStoreNavEntryDecorator` - 管理 ViewModel 作用域

3. **UI 渲染**
    - `NavDisplay` - 显示返回栈内容
    - `entryProvider` - 提供导航条目到 Composable 的映射
    - `rememberListDetailSceneStrategy` - 自适应布局策略

### 2.4 架构设计模式

#### 2.4.1 双层返回栈模式

**应用级返回栈**（Now in Android 中称为 TopLevelStack）：

- 管理应用级导航项之间的切换（ForYou → Bookmarks → Interests）
- 使用 `NavBackStack<NavKey>` 存储应用级键序列

**子返回栈**：

- 每个应用级键维护独立的子返回栈
- 子返回栈管理该功能内部的导航（如：ForYou → TopicDetail）

**优势**：

- ✅ **状态隔离**：每个应用级导航项的状态独立管理
- ✅ **状态保持**：切换应用级导航项时，子栈状态自动保持
- ✅ **返回逻辑清晰**：返回操作只在当前栈内进行

#### 2.4.2 Entry Provider 模式

**设计**：每个功能模块提供自己的 Entry Provider

**示例**：

```kotlin
// feature/foryou/impl/navigation/ForYouEntryProvider.kt
fun EntryProviderScope<NavKey>.forYouEntry(navigator: Navigator) {
    entry<ForYouNavKey> {
        ForYouRoute(navigator = navigator)
    }
}
```

**优势**：

- ✅ **模块化**：每个模块独立管理自己的导航映射
- ✅ **解耦**：App 层只需组合各个 Entry Provider
- ✅ **可测试**：每个 Entry Provider 可以独立测试

#### 2.4.3 状态装饰器模式

**设计**：使用装饰器模式为 NavEntry 添加状态管理能力

**实现**：

```kotlin
val decorators = listOf(
    rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
    rememberViewModelStoreNavEntryDecorator<NavKey>(),
)
val decoratedEntries = rememberDecoratedNavEntries(
    backStack = stack,
    entryDecorators = decorators,
    entryProvider = entryProvider,
)
```

**作用**：

- ✅ **状态保存**：自动保存和恢复 Compose 状态
- ✅ **ViewModel 作用域**：为每个 NavEntry 提供独立的 ViewModel 作用域
- ✅ **生命周期管理**：自动处理配置变更和进程死亡

### 2.5 使用流程示例

#### 场景：Dashboard → CardDetail → Profile → 点击 Dashboard Tab

```txt
1. 初始状态：
   TopLevelStack: [ForYouNavKey]
   SubStacks: {
     ForYouNavKey → [ForYouNavKey]
   }

2. 导航到 TopicDetail：
   TopLevelStack: [ForYouNavKey]
   SubStacks: {
     ForYouNavKey → [ForYouNavKey, TopicNavKey("123")]
   }

3. 导航到 Bookmarks（顶层切换）：
   TopLevelStack: [ForYouNavKey, BookmarksNavKey]
   SubStacks: {
     ForYouNavKey → [ForYouNavKey, TopicNavKey("123")]  // 状态保持
     BookmarksNavKey → [BookmarksNavKey]
   }

4. 点击 ForYou Tab（返回之前的顶层）：
   TopLevelStack: [ForYouNavKey, BookmarksNavKey]  // 不变
   SubStacks: {
     ForYouNavKey → [ForYouNavKey, TopicNavKey("123")]  // 自动恢复
     BookmarksNavKey → [BookmarksNavKey]
   }
   // 当前显示：TopicNavKey("123") ✅
```

### 2.6 架构优势总结

1. ✅ **状态保持**：切换应用级导航项时自动保持子栈状态
2. ✅ **类型安全**：使用 sealed class / data class 确保类型安全
3. ✅ **模块化**：每个功能模块独立管理导航映射
4. ✅ **可测试**：核心逻辑（Navigator、NavigationState）可独立测试
5. ✅ **官方支持**：基于 Navigation 3，官方长期维护
6. ✅ **自适应布局**：支持 Material 3 Adaptive，适配不同屏幕尺寸

---

## 3. KMP 导航技术方案对比

### 3.1 主流方案概览

| 方案                                   | 维护方                  | 状态             | KMP 支持 | 核心特点               |
|--------------------------------------|----------------------|----------------|--------|--------------------|
| **Navigation 3**                     | Google/JetBrains     | Stable (1.0.0) | ✅ 完全支持 | 返回栈驱动，完全控制         |
| **Compose Multiplatform Navigation** | JetBrains            | Alpha (2.9.1)  | ✅ 完全支持 | 导航图驱动，自动管理         |
| **Decompose**                        | Arkadii Ivanov       | Stable         | ✅ 完全支持 | 组件化架构，状态机          |
| **Voyager**                          | Kotlin Multiplatform | Stable         | ✅ 完全支持 | 屏幕抽象，Tab Navigator |

### 3.2 详细对比

#### 3.2.1 Navigation 3

**定位**：官方导航库，返回栈驱动

**优势**：

- ✅ **官方维护**：Google/JetBrains 官方支持
- ✅ **完全控制**：提供对返回栈的完全控制
- ✅ **自适应布局**：支持 Material 3 Adaptive，多目的地显示
- ✅ **KMP 支持**：Runtime 完全支持 KMP（Android、iOS、Web、Desktop）
- ✅ **状态管理**：自动管理状态保存和恢复
- ✅ **WasmJS 官方支持**：是目前唯一官方明确支持 WasmJS 的导航库

**劣势**：

- ⚠️ **学习曲线**：API 相对复杂，需要理解返回栈模型
- ⚠️ **平台集成**：需要手动处理平台特定的返回事件（Web history、iOS 返回等）

**适用场景**：

- 需要完全控制返回栈
- 需要自适应布局和多目的地显示
- 复杂的导航需求
- 长期维护的项目

**技术栈**：

```kotlin
dependencies {
    // Navigation 3（KMP 支持）
    runtimeOnly("org.jetbrains.androidx.navigation3:navigation3-ui:1.1.0-alpha01")
}
```

**重要说明**：

- ✅ **KMP 支持**：`org.jetbrains.androidx.navigation3` 版本支持 KMP（Android、iOS、Web、Desktop）
- ✅ **WasmJS 官方支持**：Navigation 3 是目前唯一官方明确支持 WasmJS 的导航库
- ⚠️ **平台差异**：Navigation 3 在 KMP 中是 Runtime-first 支持，系统级交互（如返回手势）需要平台特定实现

#### 3.2.2 Compose Multiplatform Navigation

**定位**：JetBrains 官方导航库，导航图驱动

**优势**：

- ✅ **官方维护**：JetBrains 官方支持
- ✅ **API 简单**：基于导航图，API 直观易用
- ✅ **自动管理**：自动处理返回栈的创建和销毁
- ✅ **类型安全**：1.7.0+ 支持类型安全导航
- ✅ **KMP 支持**：完全支持 KMP

**劣势**：

- ⚠️ **Alpha 阶段**：功能可能不完整，API 可能变化
- ⚠️ **灵活性**：相比 Navigation 3，对返回栈的控制较少
- ⚠️ **多返回栈**：需要手动管理多个 NavController 来实现应用级导航项独立栈

**适用场景**：

- 简单的导航需求
- 需要快速上手
- 不需要完全控制返回栈
- 偏好自动栈管理

**技术栈**：

```kotlin
dependencies {
    // Compose Multiplatform Navigation
    implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
}
```

**重要说明**：

- ✅ **KMP 支持**：完全支持 KMP（Android、iOS、Desktop、Web）
- ⚠️ **Alpha 阶段**：功能可能不完整，API 可能变化
- ⚠️ **实现路径**：与 AndroidX Navigation Compose 不是同一代码路径，非 Android 平台行为相似但非 100% 等价

#### 3.2.3 Decompose

**定位**：组件化架构，状态机驱动

**优势**：

- ✅ **组件化**：完整的组件生命周期管理
- ✅ **状态机**：基于状态机的导航模型
- ✅ **KMP 支持**：完全支持 KMP
- ✅ **可测试**：纯 Kotlin 实现，易于测试

**劣势**：

- ❌ **学习曲线陡峭**：需要理解组件化架构
- ❌ **集成成本**：需要重构现有架构
- ❌ **过度设计**：对于简单导航需求可能过度设计

**适用场景**：

- 需要组件化架构
- 需要复杂的状态管理
- 需要完整的组件生命周期管理

#### 3.2.4 Voyager

**定位**：屏幕抽象，简单易用

**优势**：

- ✅ **简单易用**：API 简单直观
- ✅ **Tab Navigator**：开箱即用的 Tab Navigator
- ✅ **KMP 支持**：完全支持 KMP
- ✅ **转场动画**：丰富的转场动画支持

**劣势**：

- ⚠️ **第三方库**：非官方维护（但维护活跃）
- ⚠️ **功能限制**：相比官方库，功能可能不够完善

**适用场景**：

- 需要简单的屏幕抽象
- 需要 Tab Navigator 功能
- 偏好第三方库的灵活性

### 3.3 方案对比矩阵

| 维度                | Navigation 3 | Compose Nav | Decompose | Voyager |
|-------------------|--------------|-------------|-----------|---------|
| **官方支持**          | ✅✅✅          | ✅✅          | ❌         | ❌       |
| **KMP 支持**        | ✅✅✅          | ✅✅✅         | ✅✅✅       | ✅✅✅     |
| **WasmJS 支持**     | ✅✅✅          | ⚠️ 基础       | ⚠️ 待验证    | ⚠️ 待验证  |
| **返回栈控制**         | ✅✅✅          | ✅✅          | ✅✅        | ✅✅      |
| **状态保持**          | ✅✅✅          | ✅✅          | ✅✅✅       | ✅✅      |
| **自适应布局**         | ✅✅✅          | ⚠️ 基础       | ✅ 支持      | ⚠️ 基础   |
| **多目的地显示**        | ✅✅✅          | ⚠️ 基础       | ✅ 支持      | ⚠️ 基础   |
| **类型安全导航**        | ✅✅✅          | ✅✅✅         | ✅✅✅       | ✅✅✅     |
| **深度链接**          | ✅✅✅          | ✅✅✅         | ✅✅✅       | ✅✅✅     |
| **Tab Navigator** | ⚠️ 需自定义      | ⚠️ 需自定义     | ⚠️ 需自定义   | ✅✅✅     |
| **API 简单性**       | ✅✅           | ✅✅✅         | ✅         | ✅✅✅     |
| **学习曲线**          | ⭐⭐⭐          | ⭐⭐⭐⭐        | ⭐⭐        | ⭐⭐⭐⭐    |
| **应用级导航项独立栈**     | ✅✅✅          | ✅✅          | ✅✅        | ✅✅✅     |
| **长期维护**          | ✅✅✅          | ✅✅          | ✅         | ✅       |

### 3.4 推荐方案排序

**基于 MyHub 需求（应用级导航项独立栈 + KMP 支持）**：

1. **🥇 Navigation 3** - 最佳选择

    - ✅ 官方支持，长期维护有保障
    - ✅ 完美支持应用级导航项独立栈（双层返回栈）
    - ✅ 状态保持机制完善
    - ✅ 支持自适应布局（未来扩展）
    - ✅ WasmJS 官方支持

2. **🥈 Compose Multiplatform Navigation** - 备选方案

    - ✅ 官方支持
    - ⚠️ 需要手动管理多个 NavController
    - ⚠️ Alpha 阶段，API 可能变化

3. **🥉 Voyager** - 快速原型

    - ✅ Tab Navigator 开箱即用
    - ⚠️ 第三方库，长期维护不确定

4. **Decompose** - 不推荐
    - ❌ 需要重构现有架构
    - ❌ 学习曲线陡峭

### 3.5 Navigation 3 在 KMP 中的重要边界说明

#### 3.5.1 支持层级分析

| 层级                            | KMP 支持情况       | 说明                                |
|-------------------------------|----------------|-----------------------------------|
| **BackStack / Key / State**   | ✅ 完全 KMP       | 返回栈模型和状态管理在所有平台一致                 |
| **NavDisplay 抽象**             | ✅ KMP          | UI 显示抽象层支持多平台                     |
| **SceneStrategy**             | ⚠️ 平台差异明显      | 自适应布局策略需要根据平台特性实现                 |
| **Material / Adaptive UI**    | ⚠️ Android 最完整 | Material Design 在 Android 平台体验最完整 |
| **系统 Back / Predictive Back** | ❌ Android Only | 系统级返回手势和预测性返回仅在 Android 平台支持      |

#### 3.5.2 关键理解

> **Navigation 3 在 KMP 中提供的是导航模型与状态管理能力的一致性，而不是保证所有平台具备 Android 等价的系统交互体验。**

**这意味着**：

- ✅ 返回栈的状态管理逻辑在所有平台一致
- ✅ 导航 API 和状态保留机制在所有平台可用
- ⚠️ 平台特定的系统交互（如返回手势、预测性返回）需要额外实现
- ⚠️ UI 适配和转场动画可能需要平台特定的定制

#### 3.5.3 平台返回机制差异

| 平台          | 返回来源                        | 实现方式           |
|-------------|-----------------------------|----------------|
| **Android** | 系统 Back / Gesture           | 系统级返回按钮和边缘手势   |
| **iOS**     | UINavigationController / 手势 | 系统导航栏返回按钮和滑动手势 |
| **Desktop** | Window close / shortcut     | 窗口关闭按钮和键盘快捷键   |
| **Web**     | Browser history             | 浏览器前进/后退按钮和地址栏 |

**关键结论**：

> **Navigation 3 不负责"平台返回事件的统一"，只负责"返回栈的状态变化模型"。**

**需要额外实现的能力**：

- ⚠️ **Web 平台**：需要绑定浏览器 history API 与 Navigation 3 返回栈
- ⚠️ **iOS 平台**：需要处理系统导航栏返回按钮与 Navigation 3 返回栈的同步
- ⚠️ **Desktop 平台**：需要处理窗口关闭事件与 Navigation 3 返回栈的清理

---

## 4. MyHub 导航方案设计

### 4.1 需求分析

**核心需求**：

1. ✅ **应用级导航项独立导航栈**：Dashboard、Profile 等维护独立的导航栈
2. ✅ **状态保持与恢复**：切换应用级导航项时自动保持状态，切换回时自动恢复
3. ✅ **返回操作作用域**：返回操作只在当前应用级导航项的栈内进行
4. ✅ **KMP 支持**：支持 Android、iOS、Desktop、Web

**用户场景**：

```txt
Dashboard → CardDetail(card-1) → Profile → 点击 Dashboard Tab
期望：返回到 CardDetail(card-1) ✅
```

### 4.2 方案设计：基于 Navigation 3

#### 4.2.1 架构设计

**采用 Now in Android 的双层返回栈架构**：

```txt
┌─────────────────────────────────────────────────────────┐
│                  AppNavigationState                     │
│  ┌──────────────────────────────────────────────────┐  │
│  │  AppStack                                         │  │
│  │  [DashboardNavKey, ProfileNavKey]                 │  │
│  └──────────────────────────────────────────────────┘  │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │  SubStacks                                        │  │
│  │  DashboardNavKey → [Dashboard, CardDetail("1")] │  │
│  │  ProfileNavKey → [Profile]                       │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

#### 4.2.2 核心组件设计

#### 1. NavKey 定义

```kotlin
// core/navigation/src/commonMain/kotlin/AppNavKey.kt

import androidx.navigation3.runtime.NavKey

// 应用级导航键
sealed interface AppNavKey : NavKey {
    data object Dashboard : AppNavKey
    data object Profile : AppNavKey
}

// 功能导航键
sealed interface FeatureNavKey : NavKey {
    data class CardDetail(val cardId: String) : FeatureNavKey
    data class AllCards(val filter: String? = null) : FeatureNavKey
    // ... 其他功能键
}
```

#### 2. AppNavigationState

```kotlin
// core/navigation/src/commonMain/kotlin/AppNavigationState.kt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

@Composable
fun rememberAppNavigationState(
    startKey: NavKey,
    appKeys: Set<NavKey>,
): AppNavigationState {
    val appStack = rememberNavBackStack(startKey)

    // ⚠️ 关键修复：使用 remember 包装 subStacks 的创建，避免在 map 中直接调用 Composable
    // 参考 NIA 的实现方式，确保 remember 作用域稳定
    val subStacks = remember(appKeys) {
        appKeys.associateWith { key ->
            NavBackStack(initialKey = key)
        }
    }

    return remember(startKey, appKeys) {
        AppNavigationState(
            startKey = startKey,
            appStack = appStack,
            subStacks = subStacks,
        )
    }
}

class AppNavigationState(
    val startKey: NavKey,
    val appStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    val currentAppKey: NavKey by derivedStateOf {
        appStack.last()
    }

    val appKeys: Set<NavKey>
        get() = subStacks.keys

    val currentSubStack: NavBackStack<NavKey>
        get() = subStacks[currentAppKey]
            ?: error("Sub stack for $currentAppKey does not exist")

    val currentKey: NavKey by derivedStateOf {
        currentSubStack.last()
    }
}
```

#### 3. AppNavigator

```kotlin
// core/navigation/src/commonMain/kotlin/AppNavigator.kt

import androidx.navigation3.runtime.NavKey

class AppNavigator(val state: AppNavigationState) {
    /**
     * 导航到指定的 NavKey
     *
     * 行为说明：
     * - 如果导航到当前 AppKey → 清空当前 SubStack，返回到 AppKey 的根屏幕
     *   （遵循 Android BottomNav 再点回到 root 的标准行为）
     * - 如果导航到其他 AppKey → 切换到该 AppKey，保持其 SubStack 状态
     * - 如果导航到 FeatureKey → 在当前 SubStack 中导航
     */
    fun navigate(key: NavKey) {
        when (key) {
            state.currentAppKey -> clearSubStack()
            in state.appKeys -> goToApp(key)
            else -> goToKey(key)
        }
    }

    fun goBack() {
        when (state.currentKey) {
            state.startKey -> {
                // 已在起始键，无法返回
                // ⚠️ 关键约束：App 级 root 不可被 pop
                // 在 Desktop / Web 平台，可能需要委托给平台处理（如关闭窗口）
                error("Cannot go back from start key")
            }
            state.currentAppKey -> {
                // 在应用键的底部，尝试返回到上一个应用栈
                // ⚠️ 关键修复：明确约束 AppStack 至少保留一个元素
                if (state.appStack.size > 1) {
                    state.appStack.removeLast()
                } else {
                    // AppStack 只剩一个元素（startKey），无法返回
                    // 在 Desktop / Web 平台，可能需要委托给平台处理（如关闭窗口）
                    // 在 Android / iOS 平台，通常忽略此操作或退出应用
                }
            }
            else -> {
                // 在子栈中，正常返回
                state.currentSubStack.removeLastOrNull()
            }
        }
    }

    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {
            remove(key)
            add(key)
        }
    }

    private fun goToApp(key: NavKey) {
        state.appStack.apply {
            if (key == state.startKey) {
                clear()
            } else {
                remove(key)
            }
            add(key)
        }
    }

    private fun clearSubStack() {
        state.currentSubStack.run {
            if (size > 1) subList(1, size).clear()
        }
    }
}
```

#### 4. Entry Provider

```kotlin
// feature/dashboard/src/commonMain/kotlin/navigation/DashboardEntryProvider.kt

import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.core.navigation.AppNavigator
import tech.zhifu.app.myhub.core.navigation.AppNavKey
import tech.zhifu.app.myhub.core.navigation.FeatureNavKey

fun EntryProviderScope<NavKey>.dashboardEntry(
    navigator: AppNavigator
) {
    entry<AppNavKey.Dashboard> {
        DashboardScreen(
            onNavigateToCardDetail = { cardId ->
                navigator.navigate(FeatureNavKey.CardDetail(cardId))
            },
            onNavigateToAllCards = {
                navigator.navigate(FeatureNavKey.AllCards())
            }
        )
    }

    entry<FeatureNavKey.CardDetail> { entry ->
        val cardId = (entry.key as FeatureNavKey.CardDetail).cardId
        CardDetailScreen(
            cardId = cardId,
            onNavigateBack = { navigator.goBack() }
        )
    }
}
```

#### 4.2.3 状态管理集成

**使用状态装饰器自动管理状态**：

```kotlin
// core/navigation/src/commonMain/kotlin/AppNavigationState.kt

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavEntry

@Composable
fun AppNavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
    // ⚠️ 性能说明：虽然每次 recomposition 都会执行 mapValues，
    // 但 rememberDecoratedNavEntries 内部会 remember，确保每个 SubStack 的
    // decorated entries 是独立且稳定的。
    // 
    // 注意：此实现依赖 Map 的 iteration 顺序稳定性。
    // 如果 appKeys 在运行时变化，可能需要额外的稳定性保证。
    val decoratedEntries = subStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator<NavKey>(),
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }

    // 缓存最终结果，避免每次 recomposition 都重新 flatMap
    return remember(appStack, decoratedEntries) {
        appStack
            .flatMap { key -> decoratedEntries[key] ?: emptyList() }
            .toMutableStateList()
    }
}
```

#### 4.2.4 App 层集成

```kotlin
// composeApp/src/commonMain/kotlin/MyHubApp.kt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import tech.zhifu.app.myhub.core.navigation.*

@Composable
fun MyHubApp() {
    val navigationState = rememberAppNavigationState(
        startKey = AppNavKey.Dashboard,
        appKeys = setOf(
            AppNavKey.Dashboard,
            AppNavKey.Profile
        )
    )

    val navigator = remember { AppNavigator(navigationState) }

    val entryProvider = entryProvider {
        dashboardEntry(navigator)
        profileEntry(navigator)
    }

    val entries = navigationState.toEntries(entryProvider)
    val sceneStrategy = rememberListDetailSceneStrategy<NavKey>()

    // 主导航栏（Navigation Rail / Bottom Navigation）
    AppNavigationSuiteScaffold(
        currentAppKey = navigationState.currentAppKey,
        onAppNavClick = { key ->
            navigator.navigate(key)
        }
    ) {
        NavDisplay(
            entries = entries,
            sceneStrategy = sceneStrategy,
            onBack = { navigator.goBack() },
        )
    }
}
```

### 4.3 与 Now in Android 的差异

| 维度                 | Now in Android                 | MyHub 方案                       | 说明                     |
|--------------------|--------------------------------|--------------------------------|------------------------|
| **导航库**            | Navigation 3                   | Navigation 3                   | ✅ 保持一致                 |
| **架构模式**           | 双层返回栈                          | 双层返回栈                          | ✅ 保持一致                 |
| **NavKey 定义**      | 每个 feature 模块定义                | 统一在 core/navigation            | ⚠️ 简化结构，便于管理           |
| **Entry Provider** | 每个 feature 模块提供                | 每个 feature 模块提供                | ✅ 保持一致                 |
| **状态装饰器**          | SaveableState + ViewModelStore | SaveableState + ViewModelStore | ✅ 保持一致                 |
| **自适应布局**          | Material 3 Adaptive            | Material 3 Adaptive（可选）        | ✅ 保持一致                 |
| **Settings 位置**    | 作为应用级导航项                       | 不再作为应用级导航项                     | ⚠️ 可通过 Profile 或其他方式访问 |

---

## 5. 核心概念详解

### 5.1 Navigation 3 的 Scope 语义

**关键理解**：Navigation 3 的 BackStack 更适合作为 **UI Navigation State**，而不是业务状态（Business State）。

**核心概念**：

> **Navigation 3 的 scope ≠ ViewModel scope**  
> 它更接近 **"导航存在性边界"**（Navigation Existence Boundary）

**这意味着**：

- ✅ BackStack 用于管理导航状态（当前屏幕、返回栈历史）
- ✅ BackStack entry 的 scope 表示"该屏幕在返回栈中存在"的生命周期边界
- ❌ 不应将业务数据（如用户信息、API 响应）存储在 BackStack 中
- ✅ 业务状态应使用 ViewModel、StateFlow 或其他状态管理方案

**最佳实践**：

```kotlin
// ✅ 正确：BackStack 只存储导航相关的 key
appStack.push(AppNavKey.Dashboard)
subStack.push(FeatureNavKey.CardDetail(cardId = "123"))

// ❌ 错误：不要在 BackStack 中存储业务数据
// appStack.push(AppNavKey.Dashboard(cardData = fullCardObject))
```

### 5.2 NavKey 的作用

NavKey 是 Navigation 3 中标识导航目的地的键类型，作为 BackStack 的泛型参数。

**这意味着**：

- ✅ NavKey 用于唯一标识每个导航目的地（如 `AppNavKey.Dashboard`、`FeatureNavKey.CardDetail(cardId)`）
- ✅ BackStack 使用 NavKey 作为泛型参数：`NavBackStack<NavKey>`
- ✅ BackStackEntry 的 `key` 属性是 NavKey 类型，用于内容解析和匹配
- ✅ NavKey 必须实现 `equals()` 和 `hashCode()`（用于比较和匹配）

**常见实现方式**：

- ✅ **Sealed Class**（推荐）：使用 sealed class 实现类型安全的 NavKey
- ✅ **String**（简单场景）：直接使用 String 作为 NavKey
- ⚠️ **自定义类型**：可以实现自定义的 NavKey 接口

#### NavKey 的关键要求

1. **唯一性**：每个导航目的地有唯一的 Key

    - ✅ `AppNavKey.Dashboard` 是唯一的 object
    - ✅ `FeatureNavKey.CardDetail("123")` 和 `FeatureNavKey.CardDetail("456")` 是不同的实例

2. **可比较性**：用于判断是否为同一目的地

    - ✅ data class 自动实现 `equals()` 和 `hashCode()`
    - ✅ object 是单例，天然唯一

3. **可序列化**（可选）：如果需要进程级持久化
    - ⚠️ 如果使用 `rememberSaveable` 或自定义持久化，NavKey 需要实现 `Parcelable` 或使用其他序列化机制

**在 BackStack 中的使用**：

```kotlin
// BackStack 使用 NavKey 作为泛型参数
val backStack = rememberNavBackStack<NavKey>(initialKey = AppNavKey.Dashboard)

// BackStackEntry 的 key 就是 NavKey 类型
NavDisplay(backStack = backStack) { entry ->
    when (val key = entry.key) {  // entry.key 是 NavKey 类型
        is AppNavKey.Dashboard -> DashboardScreen()
        is FeatureNavKey.CardDetail -> CardDetailScreen(key.cardId)
    }
}
```

### 5.3 状态装饰器（Decorator）的作用

**关键理解**：状态装饰器（NavEntryDecorator）是 Navigation 3 中为 NavEntry 添加状态管理能力的机制。

**核心概念**：

> **装饰器模式**：通过装饰器为 NavEntry 添加额外的状态管理能力，而不改变 NavEntry 本身的结构。

**主要装饰器类型**：

1. **`rememberSaveableStateHolderNavEntryDecorator`**
    - **作用**：为每个 NavEntry 提供独立的 `SaveableStateHolder`
    - **功能**：自动保存和恢复 Compose 状态（使用 `rememberSaveable` 的状态）
    - **生命周期**：当 NavEntry 从 BackStack 中移除时，状态会被保存；当 NavEntry 重新加入 BackStack 时，状态会被恢复
    - **使用场景**：保存屏幕内的 UI 状态（如滚动位置、输入框内容等）

2. **`rememberViewModelStoreNavEntryDecorator`**
    - **作用**：为每个 NavEntry 提供独立的 `ViewModelStore`
    - **功能**：管理 ViewModel 的作用域和生命周期
    - **生命周期**：当 NavEntry 从 BackStack 中移除时，ViewModel 会被保留（在内存中）；当 NavEntry 重新加入 BackStack 时，ViewModel 会被恢复
    - **使用场景**：管理屏幕级别的业务状态和 ViewModel

**装饰器的工作流程**：

```kotlin
// 1. 创建装饰器列表
val decorators = listOf(
    rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
    rememberViewModelStoreNavEntryDecorator<NavKey>(),
)

// 2. 使用装饰器创建 decorated entries
val decoratedEntries = rememberDecoratedNavEntries(
    backStack = stack,
    entryDecorators = decorators,
    entryProvider = entryProvider,
)

// 3. 每个 NavEntry 现在具有：
//    - 独立的 SaveableStateHolder（用于 Compose 状态）
//    - 独立的 ViewModelStore（用于 ViewModel）
```

- ✅ **状态恢复**：当 NavEntry 从 BackStack 中移除后重新加入时，状态会自动恢复
- ✅ **生命周期管理**：装饰器自动管理状态的生命周期，与 NavEntry 的生命周期绑定
- ✅ **解耦**：状态管理逻辑与导航逻辑分离，便于维护和测试

**最佳实践**：

```kotlin
// ✅ 正确：在 toEntries 中使用装饰器
@Composable
fun AppNavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries = subStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator<NavKey>(),
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }
    // ...
}

// ✅ 在屏幕中使用 rememberSaveable（会自动使用 SaveableStateHolder）
@Composable
fun CardDetailScreen(cardId: String) {
    var scrollPosition by rememberSaveable { mutableStateOf(0) }
    // scrollPosition 会在屏幕从 BackStack 移除时保存，恢复时自动恢复
}

// 2. ViewModel 作用域示例（需要 ViewModelStore 装饰器）
@Composable
fun CardDetailScreen(cardId: String) {
    val viewModel: CardDetailViewModel = viewModel()
    // ViewModel 的生命周期绑定到 NavEntry，即使屏幕从 BackStack 移除也会保留
}
```

### 5.4 状态保持的边界

**⚠️ 重要说明**：

> **Navigation 3 的 BackStack 状态"保持"指的是在当前 Composable 生命周期内保持；  
> ⚠️ Navigation 3 不负责进程重启后的 BackStack 恢复；  
> 如需进程重启后的恢复，应结合 `rememberSaveable` 或自定义持久化策略。**

**这意味着**：

- ✅ 在 Composable 生命周期内（配置变化、重组等），BackStack 状态会自动保持
- ✅ 切换应用级导航项时，各 BackStack 的状态在内存中保持
- ❌ **不等同于进程级持久化**：应用进程被系统杀死后，BackStack 状态会丢失
- ⚠️ 如需进程重启后的恢复，需要额外的持久化机制（如 `rememberSaveable`、ViewModel SavedStateHandle、数据库等）

### 5.5 应用级导航项切换行为

**行为说明**：

- ✅ 切换时在当前 Composable 生命周期内保持当前 BackStack 的状态
- ✅ 切换回时在当前 Composable 生命周期内恢复之前保持的状态
- ✅ 无需手动管理状态保持和恢复
- ✅ **点击当前 Tab 的行为**：如果点击当前已激活的 AppNavKey，会清空当前 SubStack，返回到该 AppNavKey 的根屏幕（遵循 Android BottomNav 再点回到 root 的标准行为）

**示例**：

```kotlin
// 切换应用级导航项
fun switchAppNav(key: NavKey) {
    // Navigation 3 在当前 Composable 生命周期内保持当前 BackStack 的状态
    navigator.navigate(key)
    // Navigation 3 自动恢复新 BackStack 的状态（在 Composable 生命周期内）
}

// 点击当前已激活的 Tab
navigator.navigate(AppNavKey.Dashboard) // 如果当前已在 Dashboard
// → 清空 Dashboard 的 SubStack，返回到 Dashboard 根屏幕
```

### 5.6 次级导航

**行为说明**：

- ✅ 将当前屏幕添加到 BackStack
- ✅ 导航到新屏幕
- ✅ 自动管理生命周期
- ✅ 保留状态

**示例**：

```kotlin
// 在当前应用级导航项的 BackStack 中导航
navigator.navigate(FeatureNavKey.CardDetail(cardId))
```

### 5.7 返回操作

**行为说明**：

- ✅ 从 BackStack 中弹出上一个屏幕
- ✅ 恢复上一个屏幕的状态
- ✅ 自动管理生命周期
- ✅ 更新 UI
- ⚠️ **App 级 root 不可被 pop**：当 AppStack 只剩一个元素（startKey）时，无法继续返回

**平台 Back 行为策略**：

不同平台对"返回"的理解不同，需要在 App 层实现平台特定的 glue code：

```kotlin
// 平台 Back 行为策略枚举（建议）
enum class PlatformBackBehavior {
    /** 在 SubStack 中返回（默认行为） */
    PopSubStack,

    /** 在 AppStack 中返回（当 SubStack 已到底部时） */
    SwitchAppStack,

    /** 退出应用（当 AppStack 已到底部时） */
    ExitApp,

    /** 关闭窗口（Desktop / Web 平台） */
    CloseWindow
}
```

**示例**：

```kotlin
// 从当前应用级导航项的 BackStack 返回
navigator.goBack()

// 平台特定的 Back 处理（示例）
@Composable
expect fun PlatformBackHandler(
    onBack: () -> Unit,
    onBackAtRoot: () -> Unit // 当在 root 时的处理
)
```

---

## 6. 实现细节

### 6.1 关键约束：BackStack 生命周期管理

**强制要求**：所有应用级 BackStack 必须放在应用级稳定 Composable 中（如 `AppRoot` / `AppScaffold`），不得位于可被销毁的子树中。

**原因**：

- `rememberNavBackStack` 依赖 Composable 生命周期
- 如果 `AppNavigation()` 因配置变化、根 Composable 重组或被拆分而重建，BackStack 会被重建，所有状态丢失
- 必须在应用启动时创建，并在整个应用生命周期中保持稳定

**正确示例**：

```kotlin
@Composable
fun AppRoot() {
    // ✅ 正确：在应用根级别创建 BackStack
    val navState = rememberAppNavigationState(
        startKey = AppNavKey.Dashboard,
        appKeys = setOf(AppNavKey.Dashboard, AppNavKey.Profile)
    )
    MyHubApp(navState)
}
```

**⚠️ 调用约束**：

> **`rememberAppNavigationState()` 必须只在应用根 Composable 调用一次，禁止在条件分支或可替换根节点中调用。**

**错误示例**：

```kotlin
// ❌ 错误：在条件分支中调用，BackStack 会被重建
if (isLoggedIn) {
    val navState = rememberAppNavigationState()  // 危险！
}

// ❌ 错误：在可替换的根节点中调用
when (appState) {
    is AppState.Loading -> rememberAppNavigationState()  // 危险！
    is AppState.Ready -> rememberAppNavigationState()   // 危险！
}
```

### 6.2 应用级 NavKey 语义边界

**强制要求**：应用级 NavKey 只作为 BackStack Root 使用，不应作为次级 push 目标。

- ✅ 正确：`navigator.navigate(AppNavKey.Profile)` 切换应用级导航项
- ❌ 错误：`subStack.push(AppNavKey.Profile)` 在栈中 push 应用级 NavKey

**原因**：

- 应用级 NavKey 代表应用级导航项，应该作为 BackStack 的根
- 如果需要在某个应用级的 BackStack 中导航到其他应用级，应该使用应用级导航切换逻辑，而不是 push
- 如果需要在同一应用级内导航到类似功能的详情页，应定义独立的功能 NavKey（如 `SettingsDetail`）

### 6.3 平台返回事件处理

**强制要求**：AndroidBackHandler 仅在 Android 平台启用。

- ✅ 正确：使用平台判断，仅在 Android 使用 `BackHandler`
- ❌ 错误：在 Desktop / Web / iOS 引入 `BackHandler` 依赖

**实现示例**：

```kotlin
// ⚠️ 重要：AndroidBackHandler 仅在 Android 平台启用
// Desktop / Web / iOS 不应引入 BackHandler 依赖

@Composable
expect fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
)

// Android 实现
@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    androidx.activity.compose.BackHandler(enabled = enabled) {
        onBack()
    }
}

// 其他平台实现（空实现或平台特定实现）
@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    // Desktop / Web / iOS 需要其他实现方式
}
```

**使用方式**：

```kotlin
@Composable
fun MyHubApp() {
    val navigationState = rememberAppNavigationState(...)
    val navigator = remember { AppNavigator(navigationState) }

    // 平台返回事件处理
    PlatformBackHandler(
        enabled = navigationState.currentSubStack.size > 1
    ) {
        navigator.goBack()
    }

    // ... 其他 UI
}
```

### 6.4 KMP 依赖坐标

**强制要求**：KMP 项目必须使用 JetBrains repackage 的 navigation3 依赖。

- ✅ 正确：`org.jetbrains.androidx.navigation3:navigation3-ui:1.1.0-alpha01`
- ❌ 错误：`androidx.navigation:navigation-compose-3`（Android-only）

**依赖配置**：

```kotlin
// core/navigation/build.gradle.kts
dependencies {
    // Navigation 3（KMP 支持）
    api("org.jetbrains.androidx.navigation3:navigation3-ui:1.1.0-alpha01")

    // 状态管理支持
    implementation("androidx.savedstate:savedstate-compose:1.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0")
}
```

**依赖说明**：

| 依赖坐标                                                | 适用场景           | 平台支持                    |
|-----------------------------------------------------|----------------|-------------------------|
| `org.jetbrains.androidx.navigation3:navigation3-ui` | ✅ KMP 项目（推荐）   | Android、iOS、Desktop、Web |
| `androidx.navigation:navigation-compose-3`          | ❌ Android-only | 仅 Android               |

---

## 7. 实施计划

### 7.1 当前进度

**方案状态**：🔒 已锁定

**状态说明**：

- 🔒 **已锁定**：方案已于 2026-01-11 锁定
- **锁定原因**：方案设计已完成并通过架构评审，已冻结作为 Navigation Infra v1 的基线设计
- **当前状态**：方案设计已确定，如需修改需要提交设计变更申请
- **实施状态**：方案设计阶段已完成，进入实施阶段

### 7.2 阶段 1：核心基础设施

1. **创建 core/navigation 模块**

    - 定义 `AppNavKey` 体系
    - 实现 `AppNavigationState`
    - 实现 `AppNavigator`

2. **添加依赖**

   ```kotlin
   dependencies {
       // Navigation 3（KMP 支持）
       runtimeOnly("org.jetbrains.androidx.navigation3:navigation3-ui:1.1.0-alpha01")

       // 状态管理支持
       implementation("androidx.savedstate:savedstate-compose:1.3.1")
       implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0")
   }
   ```

   **注意**：使用 `org.jetbrains.androidx.navigation3` 版本以支持 KMP，而不是 `androidx.navigation3`（仅 Android）。

3. **编写单元测试**
    - `AppNavigator` 测试
    - `AppNavigationState` 测试

### 7.3 阶段 2：功能模块集成

1. **Dashboard 模块**

    - 创建 `DashboardEntryProvider`
    - 迁移现有导航逻辑

2. **Profile 模块**

    - 创建 `ProfileEntryProvider`
    - 迁移现有导航逻辑

### 7.4 阶段 3：App 层集成

1. **更新 MyHubApp**

    - 集成 `AppNavigationState`
    - 集成 `NavDisplay`
    - 更新主导航栏

2. **测试验证**
    - 验证应用级导航项独立栈
    - 验证状态保持
    - 验证返回逻辑

### 7.5 阶段 4：平台特定优化

1. **Web 平台**

    - 绑定浏览器 history API
    - 处理前进/后退按钮

2. **iOS 平台**

    - 处理系统导航栏返回按钮

3. **Desktop 平台**
    - 处理窗口关闭事件

---

## 8. 风险评估

### 8.1 技术风险

**技术风险**：

- ⚠️ **Navigation 3 学习曲线**：团队需要时间熟悉 API
- ⚠️ **平台集成**：需要手动处理平台特定的返回事件（Web history、iOS 返回等）
- ⚠️ **状态序列化**：需要验证跨平台状态保存的一致性
- ⚠️ **平台返回事件绑定**：Navigation 3 不自动处理平台返回事件，需要编写 glue code
- ⚠️ **UI 适配**：Material Design 在非 Android 平台体验可能不完全一致

**缓解措施**：

- ✅ 提供详细的文档和示例
- ✅ 分阶段实施，逐步迁移
- ✅ 编写充分的单元测试和集成测试

### 8.2 业务风险

**业务风险**：

- ⚠️ **开发周期**：预计需要 4-6 周完成迁移
- ⚠️ **兼容性**：需要确保现有功能不受影响

**缓解措施**：

- ✅ 采用渐进式迁移策略
- ✅ 保持现有功能的同时逐步迁移
- ✅ 充分的测试覆盖

### 8.3 回滚方案

如遇严重问题，可按以下步骤回滚：

1. **代码回滚**：恢复到修改前的导航实现
2. **依赖清理**：移除 Navigation 3 依赖
3. **验证**：验证回滚后功能正常

**回滚时间**：< 30 分钟

---

## 9. 附录

### 9.1 参考文档

**Now in Android 相关**：

1. [Now in Android 源码](https://github.com/android/nowinandroid)

**Navigation 3 官方文档**：

1. [Navigation 3 官方文档](https://developer.android.com/guide/navigation/navigation-3)
2. [Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) ⭐
3. [Navigation3 Release Notes](https://developer.android.com/jetpack/androidx/releases/navigation3) ⭐
4. [Compose Multiplatform 1.10.0 What's New](https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html) ⭐

**Compose Multiplatform Navigation**：

1. [Compose Multiplatform Navigation 基础](https://kotlinlang.org/docs/multiplatform/compose-navigation.html)
2. [Compose Multiplatform Navigation Routing](https://kotlinlang.org/docs/multiplatform/compose-navigation-routing.html)
3. [Compose Multiplatform Navigation Deep Links](https://kotlinlang.org/docs/multiplatform/compose-navigation-deep-links.html)

**MyHub 相关文档**：

1. [官方导航文档技术点分析](./NAVIGATION_OFFICIAL_DOCS_ANALYSIS.md)
2. [KMP Navigation 能力全景图](./NAVIGATION_CAPABILITY_MAP.md)
3. [KMP 导航库调研与评估](./NAVIGATION_LIBRARY_EVALUATION.md)

### 9.2 关键代码位置

**Now in Android**：

- `core/navigation/src/main/kotlin/.../NavigationState.kt`
- `core/navigation/src/main/kotlin/.../Navigator.kt`
- `app/src/main/kotlin/.../NiaApp.kt`

**MyHub（计划）**：

- `core/navigation/src/commonMain/kotlin/.../AppNavigationState.kt`
- `core/navigation/src/commonMain/kotlin/.../AppNavigator.kt`
- `core/navigation/src/commonMain/kotlin/.../AppNavKey.kt`
- `composeApp/src/commonMain/kotlin/.../MyHubApp.kt`

### 9.3 测试场景

#### 场景 1：应用级导航切换（状态保持与恢复）⭐

```txt
1. Dashboard (根) [Dashboard BackStack: [Dashboard]]
2. Dashboard → CardDetail (card-1) [Dashboard BackStack: [Dashboard, CardDetail]]
3. 在 CardDetail 页面，点击 Navigation Rail 的 Profile [切换应用级导航项]
   - Navigation 3 在当前 Composable 生命周期内保持 Dashboard BackStack 状态
   - 切换到 Profile，显示 Profile (根) [Profile BackStack: [Profile]]
4. Profile → ProfileDetail [Profile BackStack: [Profile, ProfileDetail]]
5. ProfileDetail → 返回 [Profile BackStack: [Profile]] ✅ 回到 Profile
6. 点击 Navigation Rail 的 Dashboard [切换回 Dashboard]
   - Navigation 3 自动恢复 Dashboard BackStack 状态（在 Composable 生命周期内）
   - 显示 CardDetail ✅ 继续显示离开时的 CardDetail 页面
7. CardDetail → 返回 [Dashboard BackStack: [Dashboard]] ✅ 回到 Dashboard
```

**关键说明**：

- 切换应用级导航项时，使用应用级导航切换逻辑
- Navigation 3 自动在 Composable 生命周期内保持和恢复状态

#### 场景 2：从 CardDetail 直接切换应用级导航项

```txt
1. Dashboard → CardDetail (card-1) [Dashboard BackStack: [Dashboard, CardDetail]]
2. 在 CardDetail 页面，点击 Navigation Rail 的 Profile [切换应用级导航项]
   - Navigation 3 在当前 Composable 生命周期内保持 Dashboard BackStack 状态
   - 切换到 Profile，显示 Profile (根) [Profile BackStack: [Profile]]
3. 点击 Navigation Rail 的 Dashboard [切换回 Dashboard]
   - Navigation 3 自动恢复 Dashboard BackStack 状态（在 Composable 生命周期内）
   - 显示 CardDetail ✅ 继续显示离开时的 CardDetail 页面
```

**⚠️ 重要说明**：

> **上述"状态保持"仅在 Composable 生命周期内有效；  
> ⚠️ Navigation 3 不负责进程重启后的 BackStack 恢复；  
> 如需进程重启后的恢复，应结合 `rememberSaveable` 或自定义持久化策略。**

### 9.4 关键技术点补充

#### 9.4.1 测试优势（KMP）

Navigation 3 的 BackStack 是纯 Kotlin 实现，非常适合在 `commonTest` 中进行测试：

- ✅ 可以在 `commonTest` 中编写测试，所有平台共享
- ✅ 不依赖平台特定的 UI 框架
- ✅ 测试执行速度快，适合 CI/CD

**测试示例**：

```kotlin
@Test
fun testBackStackPushPop() {
    val backStack = NavBackStack(initialKey = AppNavKey.Dashboard)
    backStack.push(FeatureNavKey.CardDetail("123"))
    assertEquals(2, backStack.size)
    backStack.pop()
    assertEquals(1, backStack.size)
}
```

#### 9.4.2 未来演进判断

从 AndroidX 与 JetBrains 的投入方向看，Navigation 3 更像是**未来多设备 / 自适应导航的基础抽象**，而不是 Compose Navigation 的简单替代。

**证据支撑**：

- ✅ Navigation 3 强调自适应布局和多目的地显示
- ✅ 设计理念更接近"导航状态机"而非"路由图"
- ✅ 与 Compose Multiplatform 的深度整合表明长期投入
- ✅ 唯一官方明确支持 WasmJS 的导航库

#### 9.4.3 架构差异本质

> **Compose Nav：多个 NavController（每个 NavHost 独立）**  
> **Nav3：多个 BackStack，共享 Display（统一显示抽象）**

**这意味着**：

- ✅ Compose Nav 中，每个应用级导航项有独立的 NavHost，完全隔离
- ✅ Navigation 3 中，多个 BackStack 可以共享同一个 NavDisplay，提供更灵活的显示策略
- ✅ Navigation 3 更适合需要"同时显示多个目的地"或"自适应布局"的场景

---

**方案名称**：Navigation Infra v1  
**文档生成时间**：2026-01-11  
**锁定日期**：2026-01-11  
**最后更新**：2026-01-11  
**文档版本**：v1.0（已锁定）  
**评审状态**：🟢 通过（Approved with Minor Fixes）  
**方案状态**：🔒 **已锁定** - 此版本已冻结，作为 Navigation Infra v1 的基线设计

---

## 🔒 方案锁定说明

**Navigation Infra v1** 已正式锁定，此文档作为 MyHub 导航基础设施的基线设计。

### 锁定范围

- ✅ **架构设计**：双层返回栈架构（AppStack + SubStacks）
- ✅ **核心组件**：AppNavigationState、AppNavigator、AppNavKey、FeatureNavKey
- ✅ **技术选型**：Navigation 3（`org.jetbrains.androidx.navigation3:navigation3-ui:1.1.0-alpha01`）
- ✅ **状态管理**：状态装饰器模式（SaveableStateHolder + ViewModelStore）
- ✅ **命名规范**：App 级命名约定（AppNavKey、AppNavigationState、AppNavigator）

### 后续演进

如需修改或扩展导航方案，应：

1. **创建新版本**：基于 v1.0 创建 v2.0 设计文档
2. **保持兼容**：新版本应保持与 v1.0 的 API 兼容性
3. **文档化变更**：明确记录变更原因和影响范围

### 实施指导

- ✅ 可直接基于此方案启动实施
- ✅ 实施过程中如发现问题，应记录在技术债务章节
- ✅ 重大变更需重新进行架构评审

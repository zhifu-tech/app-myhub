# RFC: MyHub KMP Navigation 技术选型

**RFC 编号**：RFC-NAV-001  
**标题**：Kotlin Multiplatform Navigation 技术选型  
**状态**：Proposed  
**作者**：MyHub Development Team  
**创建日期**：2026-01-10  
**最后更新**：2026-01-10  
**评审截止日期**：2026-01-10

---

## 摘要

本 RFC 提出 MyHub 应用的 Kotlin Multiplatform（KMP）导航技术选型方案，基于对官方 Navigation 3 和 Compose Multiplatform Navigation 的深度调研，推荐采用 Navigation 3 作为主导航方案，以解决当前导航栈管理问题并支持主导航项独立栈需求。

---

## 动机

### 问题背景

当前 MyHub 应用存在以下导航问题：

1. **导航上下文丢失**：从 Dashboard → CardDetail → Settings，返回 Dashboard 时无法恢复 CardDetail 状态
2. **主导航项切换问题**：在 Settings 页面点击 Navigation Rail 的 Dashboard tab，期望回到离开时的 CardDetail，但实际回到了 Dashboard 根页面
3. **无导航栈管理**：当前实现仅维护单一的 `currentScreen` 状态，无法记录导航历史
4. **返回逻辑硬编码**：`onNavigateBack` 硬编码返回 Dashboard，不符合用户期望

### 用户场景

```
场景：Dashboard → CardDetail(card-1) → Settings → 点击 Dashboard tab
期望：返回到 CardDetail(card-1)
实际：返回到 Dashboard（导航上下文丢失）
```

---

## 目标

### 功能目标

1. ✅ **主导航项独立导航栈**：每个主导航项（Dashboard、Profile、Settings）维护独立的导航栈
2. ✅ **状态保存与恢复**：切换主导航项时自动保存，切换回时自动恢复
3. ✅ **返回操作作用域**：返回操作只在当前主导航项的栈内进行
4. ✅ **生命周期管理**：自动管理屏幕的创建和销毁

### 非功能目标

- **开发效率**：使用官方库，减少自实现成本
- **代码质量**：遵循官方最佳实践，提高可维护性
- **跨平台一致性**：Android、iOS、Desktop、Web 平台行为一致
- **长期维护**：选择官方维护的方案，降低技术债务

---

## 技术调研

### 候选方案

#### 方案 A：Navigation 3（推荐）⭐⭐⭐

**官方证据**：

- [Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) - JetBrains 官方文档
- [Navigation3 Release Notes](https://developer.android.com/jetpack/androidx/releases/navigation3) - Android 官方 Release Notes
- [Compose Multiplatform 1.10.0 What's New](https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html) - Compose Multiplatform 更新说明

**核心能力**：

- ✅ **KMP 支持**：官方层面提供完整的 Kotlin Multiplatform Runtime 支持
- ✅ **完全控制返回栈**：提供对返回栈的完全控制，导航就像在列表中添加和删除项
- ✅ **自适应布局**：支持多目的地同时显示，适合主导航项独立栈需求
- ✅ **支持平台**：Android、iOS、Web（WasmJS）、Desktop

**关键理解**：

- Navigation 3 的 scope ≠ ViewModel scope，更接近"导航存在性边界"
- 多个 BackStack 可以共享同一个 NavDisplay，提供更灵活的显示策略
- 是目前唯一一个官方明确把 WasmJS 纳入目标平台的导航模型

#### 方案 B：Compose Multiplatform Navigation（备选）⭐⭐

**核心能力**：

- ✅ **KMP 支持**：官方文档明确支持 KMP（Android、iOS、Desktop、Web）
- ✅ **自动栈管理**：官方库自动处理栈的创建、销毁和生命周期
- ✅ **API 简单**：API 设计简单直观，学习曲线平缓

**架构差异**：

- Compose Nav：多个 NavController（每个 NavHost 独立）
- Navigation 3：多个 BackStack，共享 Display（统一显示抽象）

### 方案对比

| 维度            | Navigation 3 | Compose Multiplatform Navigation |
|---------------|--------------|----------------------------------|
| **KMP 支持**    | ✅ 明确支持       | ✅ 明确支持                           |
| **返回栈管理**     | ✅ 完全控制       | ✅ 自动管理                           |
| **自适应布局**     | ✅ 完整支持       | ⚠️ 基础支持                          |
| **多目的地显示**    | ✅ 支持         | ⚠️ 基础支持                          |
| **API 复杂度**   | ⭐⭐⭐ 中等       | ⭐⭐⭐⭐ 简单                          |
| **学习曲线**      | ⭐⭐⭐ 中等       | ⭐⭐⭐⭐ 平缓                          |
| **WasmJS 支持** | ✅ 官方支持       | ⚠️ 基础支持                          |

### 详细调研报告

参见：[官方导航文档技术点分析](myhub-navigation-official-docs-analysis.md)

---

## 详细设计

### 架构设计

#### 核心组件

1. **BackStack（返回栈）**

    - 职责：管理导航栈，提供完全控制
    - 来源：Navigation 3 官方库
    - 优势：完全控制返回栈，支持推入和弹出操作

2. **NavDisplay（导航显示）**

    - 职责：显示返回栈内容，自动响应返回栈变化
    - 来源：Navigation 3 官方库
    - 优势：自动 UI 更新，支持动画过渡

3. **SceneStrategy（场景策略）**
    - 职责：定义如何显示返回栈内容，支持自适应布局
    - 来源：Navigation 3 官方库
    - 优势：支持多目的地同时显示，适应窗口大小变化

#### 数据结构

```kotlin
// 为每个主导航项创建独立的 BackStack
val dashboardBackStack = rememberBackStack(initialKey = Screen.Dashboard)
val profileBackStack = rememberBackStack(initialKey = Screen.Profile)
val settingsBackStack = rememberBackStack(initialKey = Screen.Settings)

// 当前活跃的主导航项
var currentPrimaryBackStack by remember { mutableStateOf(dashboardBackStack) }
```

#### 实现示例

```kotlin
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

### 关键算法

#### 主导航项切换（状态保存与恢复）

```kotlin
fun switchPrimaryBackStack(backStack: BackStack<Screen>) {
    // Navigation 3 自动保存当前 BackStack 的状态
    currentPrimaryBackStack = backStack
    // Navigation 3 自动恢复新 BackStack 的状态
}
```

**时间复杂度**：O(1)  
**空间复杂度**：O(1)

#### 次级导航（在当前栈中导航）

```kotlin
currentPrimaryBackStack.push(Screen.CardDetail(cardId))
```

**时间复杂度**：O(1)  
**空间复杂度**：O(1)

#### 返回操作

```kotlin
currentPrimaryBackStack.pop()
```

**时间复杂度**：O(1)  
**空间复杂度**：O(1)

---

## 实施计划

### Phase 1：准备阶段（0.5 天）

- [ ] 添加 Navigation 3 依赖
- [ ] 阅读官方文档，理解 API 使用方式
- [ ] 创建 POC（概念验证），验证 BackStack 和 NavDisplay 方案

### Phase 2：开发实现（1-2 天）

- [ ] Day 1：集成 Navigation 3 库
    - [ ] 实现主导航项独立 BackStack
    - [ ] 实现主导航项切换逻辑
    - [ ] 实现次级导航逻辑（使用 BackStack.push API）
    - [ ] 实现返回逻辑（使用 BackStack.pop API）
- [ ] Day 2：集成与优化
    - [ ] 重构 `App.kt`，使用 NavDisplay 和 BackStack
    - [ ] 更新 `AppNavigation` 组件，使用 Navigation 3 API
    - [ ] 实现场景策略，支持自适应布局
    - [ ] 为 `SettingsScreen` 添加返回按钮支持
    - [ ] 实现类型安全导航
    - [ ] 代码优化和重构
    - [ ] 单元测试编写

### Phase 3：测试与验证（1 天）

- [ ] 功能测试：验证所有导航场景
- [ ] 回归测试：确保现有功能不受影响
- [ ] 性能测试：验证导航响应时间
- [ ] 跨平台测试：iOS、Android、Desktop、Web

### Phase 4：发布（0.5 天）

- [ ] 代码审查
- [ ] 合并到主分支
- [ ] 发布到测试环境
- [ ] 监控和反馈收集

**总工作量**：3-4 个工作日

---

## 风险评估

### 技术风险

| 风险项          | 风险等级 | 影响                        | 缓解措施                                                     |
|--------------|------|---------------------------|----------------------------------------------------------|
| **平台返回事件集成** | ⭐⭐ 低 | 需要手动实现平台返回事件绑定            | 1. 参考官方示例实现 glue code<br>2. 分平台测试验证<br>3. 提供平台特定的实现指南    |
| **状态序列化差异**  | ⭐ 极低 | 不同平台的状态序列化策略可能有差异         | 1. 使用官方推荐的状态保存方式<br>2. 跨平台测试验证<br>3. 准备降级方案              |
| **UI 适配复杂度** | ⭐⭐ 低 | Material Design 体验可能不完全一致 | 1. 根据平台特性定制 UI<br>2. 使用平台特定的转场动画<br>3. 提供平台特定的样式指南       |
| **向后兼容性**    | ⭐ 极低 | 现有功能可能受影响                 | 1. 充分测试现有导航功能<br>2. 保留原有 API（标记为 deprecated）<br>3. 分阶段发布 |

### 业务风险

| 风险项        | 风险等级 | 影响               | 缓解措施                                   |
|------------|------|------------------|----------------------------------------|
| **用户体验变化** | ⭐ 极低 | 新行为符合平台规范，用户易于接受 | 1. 新行为符合平台规范<br>2. 提供用户引导（如需要）         |
| **性能影响**   | ⭐ 极低 | 导航响应时间可能增加       | 1. 性能测试验证（目标 < 100ms）<br>2. 使用官方库的优化实现 |

### 回滚方案

如遇严重问题，可按以下步骤回滚：

1. **代码回滚**：恢复到修改前的 `App.kt`
2. **依赖清理**：移除 Navigation 3 依赖
3. **验证**：验证回滚后功能正常

**回滚时间**：< 30 分钟

---

## 依赖变更

### 添加依赖

```kotlin
// composeApp/build.gradle.kts
dependencies {
    // Navigation 3（KMP 支持）
    implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0")
    // 或
    implementation("androidx.navigation:navigation-compose-3:1.0.0")
}
```

### 版本要求

- Navigation 3：1.0.0+
- Compose Multiplatform：1.10.0-beta+（支持 Navigation 3 多平台）
- Kotlin：2.0.0+

---

## 测试策略

### 单元测试

Navigation 3 的 BackStack 是纯 Kotlin 实现，可以在 `commonTest` 中进行测试：

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

### 集成测试

- 测试主导航项切换时的状态保存和恢复
- 测试返回栈操作的正确性
- 测试平台返回事件的绑定

### 平台验证

- 在每个目标平台验证返回栈行为
- 验证状态保存和恢复的一致性
- 测试平台特定的导航集成

---

## 边界条件与限制

### Navigation 3 不自动解决的问题

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

### 需要额外实现的能力

| 能力                          | Navigation 3 支持情况 | 需要额外实现 |
|-----------------------------|-------------------|--------|
| 多 BackStack 同步              | ⚠️ 隐含支持           | 需要手动实现 |
| 状态序列化策略                     | ⚠️ 基础支持           | 需要定制   |
| 跨平台 SavedState 行为           | ⚠️ 行为相似           | 需要验证   |
| DeepLink ↔ Web history 的一致性 | ⚠️ 基础支持           | 需要绑定   |
| Android-only 特性边界           | ❌ 不支持             | 需要平台判断 |

---

## 替代方案

### 方案 B：Compose Multiplatform Navigation

如果 Navigation 3 无法满足特定需求，可以使用 Compose Multiplatform Navigation：

**适用场景**：

- 简单的导航需求
- 需要快速上手
- 不需要完全控制返回栈
- 偏好自动栈管理

**实现方式**：

- 通过多个 NavController 实现主导航项独立栈
- 每个 NavController 自动维护独立的返回栈

### 方案 C：自实现

**不推荐**，理由：

- ❌ 需要手动管理栈的创建和销毁时机
- ❌ 需要手动管理生命周期，可能出现内存泄漏
- ❌ 需要自己处理各种边界情况，增加测试和维护成本

---

## 未来考虑

### 长期演进

从 AndroidX 与 JetBrains 的投入方向看，Navigation 3 更像是**未来多设备 / 自适应导航的基础抽象**，而不是 Compose Navigation 的简单替代。

**对选型的启示**：

- 如果项目需要**长期维护和演进**，Navigation 3 可能是更好的选择
- Navigation 3 对 Web/Wasm 的支持是官方长期投入的方向

### 扩展性

- 支持未来添加新的主导航项
- 支持嵌套导航场景
- 支持深度链接和结果处理

---

## 评审要点

### 技术评审

- [ ] Navigation 3 的 KMP 支持是否满足项目需求？
- [ ] 架构设计是否清晰合理？
- [ ] 实施计划是否可行？
- [ ] 风险评估是否充分？
- [ ] 测试策略是否完善？

### 业务评审

- [ ] 是否解决用户痛点？
- [ ] 用户体验是否符合预期？
- [ ] 开发成本是否可接受？
- [ ] 维护成本是否可控？

---

## 参考资料

1. [Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) - JetBrains 官方文档
2. [Navigation3 Release Notes](https://developer.android.com/jetpack/androidx/releases/navigation3) - Android 官方 Release Notes
3. [Compose Multiplatform 1.10.0 What's New](https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html) - Compose Multiplatform 更新说明
4. [官方导航文档技术点分析](myhub-navigation-official-docs-analysis.md) - 详细技术调研
5. [KMP 导航库调研与评估](myhub-navigation-library-evaluation.md) - 库对比分析

---

## 变更历史

| 版本  | 日期         | 作者               | 说明        |
|-----|------------|------------------|-----------|
| 1.0 | 2026-01-10 | Development Team | 初始 RFC 提案 |

---

**RFC 状态**：Proposed  
**下一步行动**：提交技术评审委员会评审

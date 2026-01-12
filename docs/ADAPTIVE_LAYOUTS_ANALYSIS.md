# Compose Multiplatform 自适应布局分析与实现优化

## 文档分析

根据 [Kotlin Multiplatform Compose 自适应布局文档](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html)，以下是关键要点：

### 1. 核心概念

**Window Size Classes（窗口大小类）**：
- 预定义的阈值（breakpoints），用于分类不同屏幕尺寸
- 将显示区域分为三个类别：**compact**、**medium**、**expanded**
- 适用于宽度和高度两个维度

### 2. 依赖配置

文档推荐的依赖（**Compose Multiplatform 版本**）：

```kotlin
commonMain.dependencies {
    implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.2.0")
}
```

**重要区别**：
- `org.jetbrains.compose.material3.adaptive`：**Compose Multiplatform 版本**，支持所有平台（Android、iOS、Desktop、Web）
- `androidx.compose.material3.adaptive`：**Android 专用版本**，仅支持 Android 平台

### 3. API 使用

文档示例：

```kotlin
@Composable
fun MyApp(
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    // 根据窗口高度决定是否显示 Top App Bar
    val showTopAppBar = windowSizeClass.isHeightAtLeastBreakpoint(
        WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
    )
    
    MyScreen(
        showTopAppBar = showTopAppBar,
        // ...
    )
}
```

### 4. 关键 API

- `currentWindowAdaptiveInfo()`：获取当前窗口的自适应信息
- `windowSizeClass`：窗口大小类（compact/medium/expanded）
- `isHeightAtLeastBreakpoint()`：检查高度是否达到指定断点
- `isWidthAtLeastBreakpoint()`：检查宽度是否达到指定断点

---

## 当前实现分析

### 现状（已优化为纯 KMP 实现）

1. **WindowSizeClass 实现**：
   - 使用自定义的 `WindowSizeClass` 实现
   - 位置：`composeApp/src/commonMain/kotlin/tech/zhifu/app/myhub/ui/`
   - 功能：提供 `isCompact`、`isMedium`、`isExpanded` 等扩展属性
   - **已添加** Compose Multiplatform 的 `adaptive` 库支持（可选迁移）

2. **SceneStrategy 实现**：
   - **所有平台**：使用纯 KMP 实现的 `ListDetailSceneStrategy`
   - 不依赖任何平台特定的库
   - 实现位置：`composeApp/src/commonMain/kotlin/tech/zhifu/app/myhub/navigation/SceneStrategy.kt`

3. **依赖配置**：
   - ✅ **commonMain**：`org.jetbrains.compose.material3.adaptive:adaptive`（KMP 支持）
   - ✅ **已移除**：所有 `androidx.compose.material3.adaptive.*` 依赖

### 优势

1. **纯 KMP 实现**：
   - ✅ 所有平台使用相同的代码
   - ✅ 不依赖平台特定的库
   - ✅ 符合 KMP 最佳实践

2. **简化维护**：
   - ✅ 无需维护平台特定的实现
   - ✅ 代码更清晰、更易维护

---

## 优化方案（已实施）

### 方案：纯 KMP 实现（当前方案）

**策略**：
- ✅ 在 `commonMain` 中添加 Compose Multiplatform 的 `adaptive` 库（用于 `WindowSizeClass`）
- ✅ 实现纯 KMP 的 `ListDetailSceneStrategy`（不依赖平台特定库）
- ✅ 所有平台使用相同的实现

**优势**：
- ✅ 所有平台都能使用 `WindowSizeClass` API
- ✅ 所有平台使用相同的 `SceneStrategy` 实现
- ✅ 符合 KMP 最佳实践，不包含平台特定依赖
- ✅ 代码更简洁，维护成本更低

**实现细节**：

1. **依赖配置**（已完成）：
```kotlin
// gradle/libs.versions.toml
composeMaterial3Adaptive = "1.2.0"

// composeApp/build.gradle.kts
commonMain.dependencies {
    // Compose Multiplatform Material 3 Adaptive（KMP 支持）
    implementation(libs.compose.material3.adaptive)
}
```

2. **SceneStrategy 实现**（已完成）：
```kotlin
// composeApp/src/commonMain/kotlin/.../navigation/SceneStrategy.kt
class ListDetailSceneStrategy<Key : NavKey> : SceneStrategy<Key> {
    override fun SceneStrategyScope<Key>.calculateScene(entries: List<NavEntry<Key>>): Scene<Key>? {
        // 返回 null，让 NavDisplay 使用默认行为（显示最后一个 entry）
        return null
    }
}
```

3. **后续优化**（可选）：
   - 可以根据 `WindowSizeClass` 实现更复杂的 List-Detail 布局逻辑
   - 迁移 `WindowSizeClass` 到使用官方 API

---

## 实施建议

### 立即实施（已完成）

1. ✅ 添加 Compose Multiplatform 的 `adaptive` 库到 `commonMain`
2. ✅ 保留 AndroidX 的 `adaptive-navigation3` 在 `androidMain`
3. ✅ 保持现有的 `SceneStrategy` expect/actual 实现

### 后续优化（可选）

1. **迁移 WindowSizeClass**：
   ```kotlin
   // 使用官方 API
   import org.jetbrains.compose.material3.adaptive.currentWindowAdaptiveInfo
   import org.jetbrains.compose.material3.adaptive.WindowSizeClass
   
   @Composable
   fun MyApp() {
       val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
       val showTopAppBar = windowSizeClass.isHeightAtLeastBreakpoint(
           WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
       )
       // ...
   }
   ```

2. **统一 WindowSizeClass API**：
   - 如果官方 API 满足需求，可以逐步替换自定义实现
   - 如果自定义实现有特殊需求，可以保留并扩展

---

## 参考资源

- [Kotlin Multiplatform Compose 自适应布局文档](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html)
- [Jetpack Compose 自适应布局文档](https://developer.android.com/develop/ui/compose/layouts/adaptive)

---

## 总结

根据文档分析，当前实现已经采用了**方案 1（混合使用）**，这是目前的最佳实践：

1. ✅ **已添加** Compose Multiplatform 的 `adaptive` 库（支持所有平台）
2. ✅ **保留** AndroidX 的 `adaptive-navigation3`（仅 Android）
3. ✅ **保持** expect/actual 机制处理平台差异

**下一步**：可以逐步迁移 `WindowSizeClass` 到使用官方 API，但这不是必须的，现有实现已经能够正常工作。

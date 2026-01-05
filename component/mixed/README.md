# Mixed Component 模块

## 📋 概述

Mixed Component 模块提供了 MyHub 应用中使用的混合 UI 组件。这些组件是通用的、可复用的 UI 元素，不属于特定的业务领域，可以在多个功能模块中使用。

## 🎯 设计目标

1. **通用性**：提供跨模块使用的通用 UI 组件
2. **Material Design 3**：遵循 Material 3 设计规范
3. **主题支持**：自动适配深色/浅色主题
4. **可复用性**：组件设计简洁，易于在不同场景中使用
5. **跨平台支持**：基于 Compose Multiplatform，支持 Android、iOS、Desktop、Web

## 🏗️ 模块结构

```
component/mixed/
├── build.gradle.kts            # 构建配置
├── README.md                   # 本文档
└── src/
    ├── commonMain/
    │   └── kotlin/tech/zhifu/app/myhub/component/mixed/
    │       └── Avatar.kt       # 头像组件
    └── devMain/                # Preview 支持
        └── kotlin/zhifu/app/myhub/component/mixed/
            └── Avatar.dev.kt   # Avatar 预览
```

## 📦 组件列表

### Avatar - 头像组件

一个简洁的头像组件，显示用户首字母，使用渐变背景。

**文件：** `Avatar.kt`

**API：**

```kotlin
@Composable
fun Avatar(size: Dp = 40.dp)
```

**特性：**

- 圆形头像，使用渐变背景（primary 到 tertiary 颜色）
- 显示首字母（当前固定为 "JD"）
- 根据尺寸自动调整文字大小
- 支持自定义尺寸（默认 40.dp）
- 自动适配深色/浅色主题

**使用示例：**

```kotlin
import tech.zhifu.app.myhub.component.mixed.Avatar

@Composable
fun UserProfile() {
    Column {
        // 默认大小（40.dp）
        Avatar()

        // 小尺寸（导航栏使用）
        Avatar(size = 24.dp)

        // 大尺寸（Profile 页面使用）
        Avatar(size = 96.dp)
    }
}
```

**实际使用场景：**

1. **导航栏**：在底部导航栏中显示用户头像（24.dp）

   ```kotlin
   // composeApp/src/commonMain/kotlin/tech/zhifu/app/myhub/navigation/AppNavigationBar.kt
   Avatar(size = 24.dp)
   ```

2. **Profile 页面**：在个人资料页面显示大头像（96.dp）
   ```kotlin
   // feature/profile/src/commonMain/kotlin/tech/zhifu/app/myhub/profile/ProfileScreen.kt
   Avatar(size = 96.dp)
   ```

**设计细节：**

- **渐变背景**：使用 `Brush.linearGradient` 从 `primary` 到 `tertiary` 颜色
- **文字大小**：
  - 尺寸 < 30.dp：使用 `labelSmall` 字体
  - 尺寸 ≥ 30.dp：使用 `titleSmall` 字体
- **文字颜色**：使用 `onPrimary` 颜色，确保对比度

## 🎨 Preview 支持

所有组件都提供了 Preview 支持，可以在 Android Studio 中预览。

### Preview 位置

Preview 文件位于 `src/devMain` source set 中，使用 `@Preview` 注解。

### Avatar Preview

- `AvatarLightPreview` - 浅色主题，默认大小
- `AvatarDarkPreview` - 深色主题，默认大小
- `AvatarSizesPreview` - 不同尺寸展示
- `AvatarSmallPreview` - 小尺寸（24.dp）
- `AvatarLargePreview` - 大尺寸（96.dp）

## 🛠️ 依赖

```kotlin
dependencies {
    // Compose UI 依赖
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)

    // Preview 支持
    implementation(compose.components.uiToolingPreview)
}
```

**Android Studio Preview 支持：**

```kotlin
dependencies {
    "androidRuntimeClasspath"(compose.uiTooling)
}
```

## 📚 使用指南

### 1. 基本使用

```kotlin
import tech.zhifu.app.myhub.component.mixed.Avatar

@Composable
fun MyScreen() {
    Avatar()
}
```

### 2. 自定义尺寸

```kotlin
@Composable
fun CustomSizeAvatar() {
    Avatar(size = 64.dp)
}
```

### 3. 在列表中使用

```kotlin
@Composable
fun UserList(users: List<User>) {
    LazyColumn {
        items(users) { user ->
            Row {
                Avatar(size = 48.dp)
                Text(text = user.name)
            }
        }
    }
}
```

## 🔮 未来扩展

### Avatar 组件增强

- [ ] 支持显示用户头像图片（网络图片或本地图片）
- [ ] 支持自定义首字母（当前固定为 "JD"）
- [ ] 支持自定义渐变颜色
- [ ] 支持点击事件
- [ ] 支持加载状态和占位符

### 其他组件

- [ ] 通用按钮组件
- [ ] 通用卡片组件
- [ ] 通用列表项组件
- [ ] 通用对话框组件

## 🤝 贡献

在添加新组件时，请确保：

1. ✅ 遵循 Material Design 3 设计规范
2. ✅ 支持深色/浅色主题
3. ✅ 添加 Preview 支持（devMain）
4. ✅ 添加使用示例
5. ✅ 更新本文档
6. ✅ 通过 lint 检查

## 📝 当前状态

**已完成：**

- ✅ Avatar 组件实现
- ✅ Preview 支持（5 个 Preview 函数）
- ✅ 主题适配（深色/浅色）
- ✅ 尺寸自适应文字大小

**待实现：**

- ⏳ Avatar 图片支持
- ⏳ Avatar 自定义首字母
- ⏳ 其他通用组件

---

**最后更新：** 2026 年

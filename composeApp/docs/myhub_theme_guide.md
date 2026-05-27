# MyHub 主题颜色指南

本文档详细说明 MyHub 应用中使用的 Material Design 3 颜色系统，包括每种颜色的作用、适用场景和代码示例。

## 📖 概述

Material Design 3 使用语义化的颜色系统，每种颜色都有明确的用途和适用场景。`background` 和 `surface` 在 MyHub 中已经区分开来，以提供更好的视觉层次。

## 🎨 颜色分类

Material Design 3 的颜色系统分为以下几类：

1. **主色系列**（Primary）：应用的核心标识色
2. **辅助色系列**（Secondary）：次要操作和信息
3. **第三色系列**（Tertiary）：补充颜色表达
4. **错误色系列**（Error）：错误和警告状态
5. **表面色系列**（Surface）：组件表面颜色
6. **背景色系列**（Background）：应用背景颜色
7. **轮廓色系列**（Outline）：边框和分隔线
8. **其他颜色**：反转色、遮罩等

---

## 1️⃣ 主色系列（Primary）

### `primary` - 主色

**作用**：应用的核心标识色，用于强调最重要的交互元素。

**适用场景**：
- ✅ 主要按钮（Primary Button）
- ✅ 悬浮操作按钮（FAB）
- ✅ 选中的标签或导航项
- ✅ 链接文本
- ✅ 进度指示器
- ✅ 复选框选中状态

**代码示例**：

```kotlin
// 主要按钮
Button(
    onClick = { /* 执行主要操作 */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
) {
    Text("保存", color = MaterialTheme.colorScheme.onPrimary)
}

// 悬浮操作按钮
FloatingActionButton(
    onClick = { /* 操作 */ },
    containerColor = MaterialTheme.colorScheme.primary
) {
    Icon(Icons.Default.Add, contentDescription = "添加")
}
```

**颜色值**：
- 深色模式：`#D0BCFF`（浅紫色）
- 浅色模式：`#6750A4`（深紫色）

---

### `onPrimary` - 主色上的文本/图标色

**作用**：用于显示在 `primary` 颜色上的文本和图标，确保足够的对比度。

**适用场景**：
- ✅ 主色按钮上的文本
- ✅ 主色背景上的图标
- ✅ 任何显示在主色上的内容

**代码示例**：

```kotlin
Button(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
) {
    Text(
        "保存",
        color = MaterialTheme.colorScheme.onPrimary  // ✅ 使用 onPrimary
    )
}
```

**颜色值**：
- 深色模式：`#381E72`（深紫色）
- 浅色模式：`#FFFFFF`（白色）

---

### `primaryContainer` - 主色容器

**作用**：主色的容器变体，用于需要更柔和主色表达的场景。

**适用场景**：
- ✅ 次要的主色按钮
- ✅ 标签背景
- ✅ 高亮区域背景
- ✅ 导航栏的"新建"按钮

**代码示例**：

```kotlin
// 次要主色按钮
ExtendedFloatingActionButton(
    onClick = { },
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Icon(Icons.Default.Add, null)
    Spacer(Modifier.width(12.dp))
    Text("新建卡片")
}

// 标签背景
Surface(
    color = MaterialTheme.colorScheme.primaryContainer,
    shape = RoundedCornerShape(8.dp)
) {
    Text(
        "重要",
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
```

**颜色值**：
- 深色模式：`#4F378B`（中等紫色）
- 浅色模式：`#EADDFF`（浅紫色）

---

### `onPrimaryContainer` - 主色容器上的文本/图标色

**作用**：用于显示在 `primaryContainer` 上的文本和图标。

**适用场景**：
- ✅ 主色容器上的文本
- ✅ 主色容器上的图标

**代码示例**：

```kotlin
Surface(
    color = MaterialTheme.colorScheme.primaryContainer
) {
    Text(
        "标签文本",
        color = MaterialTheme.colorScheme.onPrimaryContainer  // ✅ 使用 onPrimaryContainer
    )
}
```

**颜色值**：
- 深色模式：`#EADDFF`（浅紫色）
- 浅色模式：`#21005D`（深紫色）

---

## 2️⃣ 辅助色系列（Secondary）

### `secondary` - 辅助色

**作用**：用于次要操作和信息，提供与主色的对比。

**适用场景**：
- ✅ 次要按钮
- ✅ 筛选标签
- ✅ 进度条
- ✅ 图表中的次要数据系列

**代码示例**：

```kotlin
// 次要按钮
Button(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary
    )
) {
    Text("取消", color = MaterialTheme.colorScheme.onSecondary)
}

// 筛选标签
FilterChip(
    selected = false,
    onClick = { },
    label = { Text("标签") },
    colors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.secondary
    )
)
```

**颜色值**：
- 深色模式：`#CCC2DC`（浅灰紫色）
- 浅色模式：`#625B71`（深灰紫色）

---

### `onSecondary` - 辅助色上的文本/图标色

**作用**：用于显示在 `secondary` 颜色上的文本和图标。

**适用场景**：
- ✅ 辅助色按钮上的文本
- ✅ 辅助色背景上的图标

**代码示例**：

```kotlin
Button(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary
    )
) {
    Text(
        "取消",
        color = MaterialTheme.colorScheme.onSecondary  // ✅ 使用 onSecondary
    )
}
```

**颜色值**：
- 深色模式：`#332D41`（深灰紫色）
- 浅色模式：`#FFFFFF`（白色）

---

### `secondaryContainer` - 辅助色容器

**作用**：辅助色的容器变体，用于需要更柔和辅助色表达的场景。

**适用场景**：
- ✅ 次要操作的容器背景
- ✅ 信息提示区域
- ✅ 次要标签背景

**代码示例**：

```kotlin
Surface(
    color = MaterialTheme.colorScheme.secondaryContainer,
    shape = RoundedCornerShape(12.dp)
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "提示信息",
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
```

**颜色值**：
- 深色模式：`#4A4458`（中等灰紫色）
- 浅色模式：`#E8DEF8`（浅灰紫色）

---

### `onSecondaryContainer` - 辅助色容器上的文本/图标色

**作用**：用于显示在 `secondaryContainer` 上的文本和图标。

**适用场景**：
- ✅ 辅助色容器上的文本
- ✅ 辅助色容器上的图标

**代码示例**：

```kotlin
Surface(
    color = MaterialTheme.colorScheme.secondaryContainer
) {
    Text(
        "信息文本",
        color = MaterialTheme.colorScheme.onSecondaryContainer  // ✅ 使用 onSecondaryContainer
    )
}
```

**颜色值**：
- 深色模式：`#E8DEF8`（浅灰紫色）
- 浅色模式：`#1D192B`（深灰紫色）

---

## 3️⃣ 第三色系列（Tertiary）

### `tertiary` - 第三色

**作用**：用于补充主色和辅助色，提供额外的颜色表达。

**适用场景**：
- ✅ 强调特定信息
- ✅ 图表中的第三数据系列
- ✅ 特殊状态的指示

**代码示例**：

```kotlin
// 强调文本
Text(
    text = "重要提示",
    color = MaterialTheme.colorScheme.tertiary,
    fontWeight = FontWeight.Bold
)

// 特殊状态指示
Surface(
    color = MaterialTheme.colorScheme.tertiary,
    shape = CircleShape
) {
    Icon(
        Icons.Default.Star,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onTertiary,
        modifier = Modifier.padding(8.dp)
    )
}
```

**颜色值**：
- 深色模式：`#EFB8C8`（浅粉色）
- 浅色模式：`#7D5260`（深粉色）

---

### `onTertiary` - 第三色上的文本/图标色

**作用**：用于显示在 `tertiary` 颜色上的文本和图标。

**适用场景**：
- ✅ 第三色背景上的文本
- ✅ 第三色背景上的图标

**代码示例**：

```kotlin
Surface(
    color = MaterialTheme.colorScheme.tertiary
) {
    Text(
        "特殊内容",
        color = MaterialTheme.colorScheme.onTertiary  // ✅ 使用 onTertiary
    )
}
```

**颜色值**：
- 深色模式：`#492532`（深粉色）
- 浅色模式：`#FFFFFF`（白色）

---

### `tertiaryContainer` - 第三色容器

**作用**：第三色的容器变体。

**适用场景**：
- ✅ 第三色相关的容器背景
- ✅ 特殊信息的背景区域

**代码示例**：

```kotlin
Surface(
    color = MaterialTheme.colorScheme.tertiaryContainer,
    shape = RoundedCornerShape(8.dp)
) {
    Text(
        "特殊信息",
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier.padding(12.dp)
    )
}
```

**颜色值**：
- 深色模式：`#633B48`（中等粉色）
- 浅色模式：`#FFD8E4`（浅粉色）

---

### `onTertiaryContainer` - 第三色容器上的文本/图标色

**作用**：用于显示在 `tertiaryContainer` 上的文本和图标。

**适用场景**：
- ✅ 第三色容器上的文本
- ✅ 第三色容器上的图标

**代码示例**：

```kotlin
Surface(
    color = MaterialTheme.colorScheme.tertiaryContainer
) {
    Text(
        "特殊内容",
        color = MaterialTheme.colorScheme.onTertiaryContainer  // ✅ 使用 onTertiaryContainer
    )
}
```

**颜色值**：
- 深色模式：`#FFD8E4`（浅粉色）
- 浅色模式：`#31111D`（深粉色）

---

## 4️⃣ 错误色系列（Error）

### `error` - 错误色

**作用**：用于表示错误状态、警告信息和危险操作。

**适用场景**：
- ✅ 错误提示文本
- ✅ 错误状态的图标
- ✅ 危险操作的按钮
- ✅ 表单验证错误

**代码示例**：

```kotlin
// 错误提示文本
Text(
    text = "输入错误",
    color = MaterialTheme.colorScheme.error,
    style = MaterialTheme.typography.bodyMedium
)

// 错误状态的图标
Icon(
    Icons.Default.Error,
    contentDescription = "错误",
    tint = MaterialTheme.colorScheme.error
)

// 危险操作按钮
Button(
    onClick = { /* 删除操作 */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error
    )
) {
    Text("删除", color = MaterialTheme.colorScheme.onError)
}
```

**颜色值**：
- 深色模式：`#F2B8B5`（浅红色）
- 浅色模式：`#B3261E`（深红色）

---

### `onError` - 错误色上的文本/图标色

**作用**：用于显示在 `error` 颜色上的文本和图标。

**适用场景**：
- ✅ 错误色按钮上的文本
- ✅ 错误色背景上的图标

**代码示例**：

```kotlin
Button(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error
    )
) {
    Text(
        "删除",
        color = MaterialTheme.colorScheme.onError  // ✅ 使用 onError
    )
}
```

**颜色值**：
- 深色模式：`#601410`（深红色）
- 浅色模式：`#FFFFFF`（白色）

---

### `errorContainer` - 错误色容器

**作用**：错误色的容器变体，用于错误信息的背景区域。

**适用场景**：
- ✅ 错误提示框的背景
- ✅ 错误状态的容器
- ✅ 警告信息的背景

**代码示例**：

```kotlin
Surface(
    color = MaterialTheme.colorScheme.errorContainer,
    shape = RoundedCornerShape(8.dp)
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "操作失败，请重试",
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
```

**颜色值**：
- 深色模式：`#8C1D18`（中等红色）
- 浅色模式：`#F9DEDC`（浅红色）

---

### `onErrorContainer` - 错误色容器上的文本/图标色

**作用**：用于显示在 `errorContainer` 上的文本和图标。

**适用场景**：
- ✅ 错误色容器上的文本
- ✅ 错误色容器上的图标

**代码示例**：

```kotlin
Surface(
    color = MaterialTheme.colorScheme.errorContainer
) {
    Text(
        "错误信息",
        color = MaterialTheme.colorScheme.onErrorContainer  // ✅ 使用 onErrorContainer
    )
}
```

**颜色值**：
- 深色模式：`#F9DEDC`（浅红色）
- 浅色模式：`#410E0B`（深红色）

---

## 5️⃣ 表面色系列（Surface）

### `surface` - 表面色

**作用**：用于"浮在"背景之上的组件表面颜色，是可交互的组件表面。

**适用场景**：
- ✅ `Card` 组件的 `containerColor`
- ✅ `NavigationRail`、`NavigationBar` 的 `containerColor`
- ✅ `BottomSheet`、`Dialog` 的表面
- ✅ `TextField`、`SearchBar` 的背景
- ✅ 任何需要"浮在背景之上"的组件

**代码示例**：

```kotlin
// 卡片组件
Card(
    containerColor = MaterialTheme.colorScheme.surface
) {
    Text("卡片内容", modifier = Modifier.padding(16.dp))
}

// 导航栏
NavigationRail(
    containerColor = MaterialTheme.colorScheme.surface
) {
    // 导航项
}

// 搜索框
TextField(
    value = text,
    onValueChange = { },
    colors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface
    )
)
```

**颜色值**：
- 深色模式：`#2C2B2F`（中等灰色，比 background 稍亮）
- 浅色模式：`#F5F5F5`（浅灰色，比 background 稍暗）

---

### `onSurface` - 表面色上的文本/图标色

**作用**：用于显示在 `surface` 上的主要文本和图标。

**适用场景**：
- ✅ 卡片上的主要文本
- ✅ 导航栏上的文本
- ✅ 任何显示在表面上的主要内容

**代码示例**：

```kotlin
Card(
    containerColor = MaterialTheme.colorScheme.surface
) {
    Text(
        "卡片标题",
        color = MaterialTheme.colorScheme.onSurface,  // ✅ 使用 onSurface
        style = MaterialTheme.typography.titleLarge
    )
}
```

**颜色值**：
- 深色模式：`#E6E1E5`（浅灰色）
- 浅色模式：`#1C1B1F`（深灰色）

---

### `surfaceVariant` - 表面色变体

**作用**：`surface` 的变体，用于需要轻微区分的组件。

**适用场景**：
- ✅ 输入框的背景
- ✅ 分隔线区域
- ✅ 次要的表面区域
- ✅ 代码块的背景

**代码示例**：

```kotlin
// 输入框背景
Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = RoundedCornerShape(8.dp)
) {
    TextField(
        value = text,
        onValueChange = { },
        modifier = Modifier.fillMaxWidth()
    )
}

// 代码块背景
Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = RoundedCornerShape(8.dp)
) {
    Text(
        text = code,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(12.dp)
    )
}
```

**颜色值**：
- 深色模式：`#49454F`（中等灰色）
- 浅色模式：`#E7E0EC`（浅灰色）

---

### `onSurfaceVariant` - 表面色变体上的文本/图标色

**作用**：用于显示在 `surfaceVariant` 上的文本和图标，通常用于次要内容。

**适用场景**：
- ✅ 输入框中的占位符文本
- ✅ 次要标签文本
- ✅ 辅助信息文本

**代码示例**：

```kotlin
Surface(
    color = MaterialTheme.colorScheme.surfaceVariant
) {
    Text(
        "占位符文本",
        color = MaterialTheme.colorScheme.onSurfaceVariant,  // ✅ 使用 onSurfaceVariant
        style = MaterialTheme.typography.bodySmall
    )
}
```

**颜色值**：
- 深色模式：`#CAC4D0`（浅灰色）
- 浅色模式：`#49454F`（深灰色）

---

## 6️⃣ 背景色系列（Background）

### `background` - 背景色

**作用**：应用的最底层背景色，是整个界面的基础颜色。

**适用场景**：
- ✅ `Scaffold` 的 `containerColor`
- ✅ 内容区域的背景（如 `Box`、`Column` 的背景）
- ✅ 全屏组件的背景（如错误屏幕、加载屏幕）
- ✅ 对话框、底部表单等模态组件的背景

**代码示例**：

```kotlin
// Scaffold 的背景
Scaffold(
    containerColor = MaterialTheme.colorScheme.background
) { padding ->
    // 内容
}

// 内容区域的背景
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
) {
    // 内容
}

// 全屏组件的背景
Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
) {
    // 内容
}
```

**颜色值**：
- 深色模式：`#1C1B1F`（深灰色，最暗）
- 浅色模式：`#FFFBFE`（浅灰色，最亮）

**注意**：`background` 和 `surface` 在 MyHub 中已区分开来，`surface` 在深色模式中稍亮（`#2C2B2F`），在浅色模式中稍暗（`#F5F5F5`），以提供更好的视觉层次。

---

### `onBackground` - 背景色上的文本/图标色

**作用**：用于显示在 `background` 上的主要文本和图标。

**适用场景**：
- ✅ 内容区域的主要文本
- ✅ 背景上的主要图标
- ✅ 任何直接显示在背景上的主要内容

**代码示例**：

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
) {
    Text(
        "主要内容",
        color = MaterialTheme.colorScheme.onBackground,  // ✅ 使用 onBackground
        style = MaterialTheme.typography.bodyLarge
    )
}
```

**颜色值**：
- 深色模式：`#E6E1E5`（浅灰色）
- 浅色模式：`#1C1B1F`（深灰色）

---

## 7️⃣ 轮廓色系列（Outline）

### `outline` - 轮廓色

**作用**：用于边框、分隔线和轮廓，提供视觉分隔。

**适用场景**：
- ✅ 卡片边框
- ✅ 输入框边框
- ✅ 分隔线（Divider）
- ✅ 轮廓指示器

**代码示例**：

```kotlin
// 卡片边框
Card(
    border = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline
    )
) {
    // 内容
}

// 分隔线
HorizontalDivider(
    color = MaterialTheme.colorScheme.outline,
    thickness = 1.dp
)

// 输入框边框
OutlinedTextField(
    value = text,
    onValueChange = { },
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )
)
```

**颜色值**：
- 深色模式：`#938F99`（中等灰色）
- 浅色模式：`#79747E`（中等灰色）

---

### `outlineVariant` - 轮廓色变体

**作用**：轮廓色的变体，用于需要更柔和分隔的场景。

**适用场景**：
- ✅ 次要分隔线
- ✅ 轻微的分隔区域
- ✅ 内部边框

**代码示例**：

```kotlin
// 次要分隔线
HorizontalDivider(
    color = MaterialTheme.colorScheme.outlineVariant,
    thickness = 1.dp,
    modifier = Modifier.padding(horizontal = 16.dp)
)

// 内部边框
Surface(
    border = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    ),
    shape = RoundedCornerShape(8.dp)
) {
    // 内容
}
```

**颜色值**：
- 深色模式：`#44474E`（深灰色）
- 浅色模式：`#C4C6D0`（浅灰色）

---

## 8️⃣ 其他颜色

### `inverseSurface` - 反转表面色

**作用**：用于需要反转显示的表面，通常用于强调区域。

**适用场景**：
- ✅ 强调区域
- ✅ 反转主题的容器

**代码示例**：

```kotlin
Surface(
    color = MaterialTheme.colorScheme.inverseSurface
) {
    Text(
        "强调内容",
        color = MaterialTheme.colorScheme.inverseOnSurface
    )
}
```

**颜色值**：
- 深色模式：`#E6E1E5`（浅灰色）
- 浅色模式：`#313033`（深灰色）

---

### `inverseOnSurface` - 反转表面色上的文本/图标色

**作用**：用于显示在 `inverseSurface` 上的文本和图标。

**适用场景**：
- ✅ 反转表面上的文本
- ✅ 反转表面上的图标

**代码示例**：

```kotlin
Surface(
    color = MaterialTheme.colorScheme.inverseSurface
) {
    Text(
        "反转文本",
        color = MaterialTheme.colorScheme.inverseOnSurface  // ✅ 使用 inverseOnSurface
    )
}
```

**颜色值**：
- 深色模式：`#313033`（深灰色）
- 浅色模式：`#F4EFF4`（浅灰色）

---

### `inversePrimary` - 反转主色

**作用**：用于反转主题中的主色，通常用于强调。

**适用场景**：
- ✅ 反转主题中的主色元素
- ✅ 强调按钮

**代码示例**：

```kotlin
Surface(
    color = MaterialTheme.colorScheme.inverseSurface
) {
    Button(
        onClick = { },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.inversePrimary
        )
    ) {
        Text("操作")
    }
}
```

**颜色值**：
- 深色模式：`#6750A4`（深紫色）
- 浅色模式：`#D0BCFF`（浅紫色）

---

### `surfaceTint` - 表面色调

**作用**：用于为表面添加色调，通常用于 Elevated Surface。

**适用场景**：
- ✅ 提升的表面（Elevated Surface）
- ✅ 需要色调的表面

**代码示例**：

```kotlin
Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
) {
    // Card 会自动应用 surfaceTint
}
```

**颜色值**：
- 深色模式：`#D0BCFF`（浅紫色）
- 浅色模式：`#6750A4`（深紫色）

---

### `scrim` - 遮罩色

**作用**：用于遮罩层，通常用于对话框、底部表单等模态组件。

**适用场景**：
- ✅ 对话框背景遮罩
- ✅ 底部表单背景遮罩
- ✅ 模态组件的遮罩层

**代码示例**：

```kotlin
// 对话框遮罩（通常由 Dialog 组件自动处理）
Dialog(onDismissRequest = { }) {
    // Dialog 会自动应用 scrim 遮罩
}

// 底部表单遮罩
ModalBottomSheet(
    onDismissRequest = { },
    containerColor = MaterialTheme.colorScheme.surface
) {
    // 内容
    // ModalBottomSheet 会自动应用 scrim 遮罩
}
```

**颜色值**：
- 深色模式：`#000000`（黑色，通常带透明度）
- 浅色模式：`#000000`（黑色，通常带透明度）

---

## 📐 视觉层次关系

```
┌─────────────────────────────────────┐
│  background (应用背景)                │ ← 最底层
│  ┌───────────────────────────────┐ │
│  │  surface (导航栏/卡片表面)       │ │ ← 浮在背景之上
│  │  ┌─────────────────────────┐ │ │
│  │  │  surfaceVariant (变体)   │ │ │ ← 更浅/更深的变体
│  │  └─────────────────────────┘ │ │
│  └───────────────────────────────┘ │
│                                     │
│  primary (主色按钮)                  │ ← 强调元素
│  secondary (辅助色按钮)              │ ← 次要元素
└─────────────────────────────────────┘
```

## ✅ 最佳实践

### 1. 始终根据语义选择颜色

- 需要"浮在背景之上"的组件 → 使用 `surface`
- 应用的基础背景 → 使用 `background`
- 主要操作 → 使用 `primary`
- 次要操作 → 使用 `secondary`
- 错误状态 → 使用 `error`

### 2. 保持一致性

- 所有导航栏使用 `surface`
- 所有卡片使用 `surface`
- 所有内容区域使用 `background`
- 所有主要按钮使用 `primary`
- 所有次要按钮使用 `secondary`

### 3. 使用 "on" 颜色确保对比度

- 在 `primary` 上使用 `onPrimary`
- 在 `surface` 上使用 `onSurface`
- 在 `background` 上使用 `onBackground`
- 永远不要用 `primary` 作为文本颜色（除非在 `primary` 背景上）

### 4. 遵循 Material Design 3 规范

- 不要混用 `background` 和 `surface`
- 不要用 `background` 作为卡片或导航栏的颜色
- 不要用 `primary` 作为背景色（除非是按钮等组件）

## 🎯 当前项目中的颜色值

### 深色模式（DarkColorScheme）

```kotlin
primary = Color(0xFFD0BCFF)           // 浅紫色
onPrimary = Color(0xFF381E72)         // 深紫色
primaryContainer = Color(0xFF4F378B)   // 中等紫色
onPrimaryContainer = Color(0xFFEADDFF) // 浅紫色

secondary = Color(0xFFCCC2DC)         // 浅灰紫色
onSecondary = Color(0xFF332D41)       // 深灰紫色
secondaryContainer = Color(0xFF4A4458) // 中等灰紫色
onSecondaryContainer = Color(0xFFE8DEF8) // 浅灰紫色

tertiary = Color(0xFFEFB8C8)          // 浅粉色
onTertiary = Color(0xFF492532)        // 深粉色
tertiaryContainer = Color(0xFF633B48)  // 中等粉色
onTertiaryContainer = Color(0xFFFFD8E4) // 浅粉色

error = Color(0xFFF2B8B5)             // 浅红色
onError = Color(0xFF601410)           // 深红色
errorContainer = Color(0xFF8C1D18)    // 中等红色
onErrorContainer = Color(0xFFF9DEDC)  // 浅红色

outline = Color(0xFF938F99)           // 中等灰色
outlineVariant = Color(0xFF44474E)    // 深灰色

background = Color(0xFF1C1B1F)        // 深灰色，最暗
onBackground = Color(0xFFE6E1E5)      // 浅灰色

surface = Color(0xFF2C2B2F)           // 中等灰色，比 background 稍亮
onSurface = Color(0xFFE6E1E5)         // 浅灰色
surfaceVariant = Color(0xFF49454F)    // 中等灰色
onSurfaceVariant = Color(0xFFCAC4D0)  // 浅灰色

inverseSurface = Color(0xFFE6E1E5)    // 浅灰色
inverseOnSurface = Color(0xFF313033)  // 深灰色
inversePrimary = Color(0xFF6750A4)    // 深紫色

surfaceTint = Color(0xFFD0BCFF)       // 浅紫色
scrim = Color(0xFF000000)             // 黑色
```

### 浅色模式（LightColorScheme）

```kotlin
primary = Color(0xFF6750A4)           // 深紫色
onPrimary = Color(0xFFFFFFFF)         // 白色
primaryContainer = Color(0xFFEADDFF)   // 浅紫色
onPrimaryContainer = Color(0xFF21005D) // 深紫色

secondary = Color(0xFF625B71)         // 深灰紫色
onSecondary = Color(0xFFFFFFFF)       // 白色
secondaryContainer = Color(0xFFE8DEF8) // 浅灰紫色
onSecondaryContainer = Color(0xFF1D192B) // 深灰紫色

tertiary = Color(0xFF7D5260)          // 深粉色
onTertiary = Color(0xFFFFFFFF)        // 白色
tertiaryContainer = Color(0xFFFFD8E4)  // 浅粉色
onTertiaryContainer = Color(0xFF31111D) // 深粉色

error = Color(0xFFB3261E)             // 深红色
onError = Color(0xFFFFFFFF)           // 白色
errorContainer = Color(0xFFF9DEDC)    // 浅红色
onErrorContainer = Color(0xFF410E0B)  // 深红色

outline = Color(0xFF79747E)           // 中等灰色
outlineVariant = Color(0xFFC4C6D0)     // 浅灰色

background = Color(0xFFFFFBFE)        // 浅灰色，最亮
onBackground = Color(0xFF1C1B1F)      // 深灰色

surface = Color(0xFFF5F5F5)            // 浅灰色，比 background 稍暗
onSurface = Color(0xFF1C1B1F)         // 深灰色
surfaceVariant = Color(0xFFE7E0EC)     // 浅灰色
onSurfaceVariant = Color(0xFF49454F)  // 深灰色

inverseSurface = Color(0xFF313033)    // 深灰色
inverseOnSurface = Color(0xFFF4EFF4)  // 浅灰色
inversePrimary = Color(0xFFD0BCFF)    // 浅紫色

surfaceTint = Color(0xFF6750A4)       // 深紫色
scrim = Color(0xFF000000)             // 黑色
```

## 🔗 相关资源

- [Material Design 3 Color System](https://m3.material.io/styles/color/the-color-system/color-roles)
- [Material Design 3 Surface Roles](https://m3.material.io/styles/color/the-color-system/color-roles#surface-roles)
- [Compose Material 3 ColorScheme](https://developer.android.com/reference/kotlin/androidx/compose/material3/ColorScheme)
- [Material Design 3 Color Contrast](https://m3.material.io/styles/color/the-color-system/color-roles#color-contrast)

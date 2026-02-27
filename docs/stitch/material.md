好，这一步就很**体系化**了 👍  
我给你一张 **“Material3 colorScheme · 角色 × variant / 层级”工程向速查表**，不是设计稿语言，是**你写 Compose 时能直接对号入座的那种**。

---

## Material3 ColorScheme：角色 × 层级（Variant）表

> 纵轴 = **角色（是什么）**  
> 横轴 = **层级 / variant（用在什么强度）**

---

### 1️⃣ 强调色（Accent Roles）

| 角色        | 主强调         | 承载强调（Container）      | 前景                                     |
|-----------|-------------|----------------------|----------------------------------------|
| Primary   | `primary`   | `primaryContainer`   | `onPrimary` / `onPrimaryContainer`     |
| Secondary | `secondary` | `secondaryContainer` | `onSecondary` / `onSecondaryContainer` |
| Tertiary  | `tertiary`  | `tertiaryContainer`  | `onTertiary` / `onTertiaryContainer`   |

**使用速记：**

- 点状操作 → `primary`
- 高亮区域 / 选中态 → `primaryContainer`
- 情绪 / 装饰 → `tertiary*`

---

### 2️⃣ 地形色（Surface Roles）

| 角色         | 主层               | Variant（次级层）     | 前景                               |
|------------|------------------|------------------|----------------------------------|
| Background | `background`     | —                | `onBackground`                   |
| Surface    | `surface`        | `surfaceVariant` | `onSurface` / `onSurfaceVariant` |
| Inverse    | `inverseSurface` | —                | `inverseOnSurface`               |

**使用速记：**

- 整个屏幕 → `background`
- 卡片 / Sheet → `surface`
- 分区 / Side panel → `surfaceVariant`
- Dark ↔ Light 反转场景 → `inverseSurface`

---

### 3️⃣ 结构色（Structure Roles）

| 角色      | 主层        | Variant（弱化）      | 说明       |
|---------|-----------|------------------|----------|
| Outline | `outline` | `outlineVariant` | 边框 / 分割线 |
| Scrim   | `scrim`   | —                | 模态遮罩     |

**使用速记：**

- 需要“看清边界” → `outline`
- 只是“提示结构” → `outlineVariant`

---

### 4️⃣ 状态色（State Roles）

| 角色    | 主态      | 承载态              | 前景                             |
|-------|---------|------------------|--------------------------------|
| Error | `error` | `errorContainer` | `onError` / `onErrorContainer` |

**使用速记：**

- 文本 / 图标提示 → `error`
- 错误卡片 / 提示块 → `errorContainer`

---

### 5️⃣ 特殊桥接色（Bridge Roles）

| 角色               | 用途                         |
|------------------|----------------------------|
| `surfaceTint`    | Elevation 着色（主要用于 Android） |
| `inversePrimary` | 深浅主题切换时的品牌保持               |

---

## 一眼判断用哪个（决策树版）

你写 UI 时，可以在脑子里跑这个流程：

```
这是强调吗？
 ├─ 是 → Primary / Secondary / Tertiary
 │    ├─ 是一个“点” → primary
 │    └─ 是一整块 → primaryContainer
 └─ 否
     这是地面吗？
     ├─ 是 → surface / surfaceVariant
     └─ 否
         这是结构吗？
         ├─ 是 → outline / outlineVariant
         └─ 状态 → error / scrim
```

---

## 放到你项目里的“典型映射示例”

以你现在的 Capture / Preview 场景：

| UI 元素         | 推荐颜色               |
|---------------|--------------------|
| 屏幕背景          | `background`       |
| 主内容区          | `surface`          |
| Preview Panel | `surfaceVariant`   |
| Panel 边框      | `outline`          |
| Panel 内分割     | `outlineVariant`   |
| 主操作按钮         | `primary`          |
| 处理中高亮         | `primaryContainer` |
| 错误提示          | `errorContainer`   |

---

## 一句你可以留在 Theme 文件里的注释（真·工程级）

```kotlin
// ColorScheme usage rule:
// Accent = primary / secondary / tertiary
// Container = large highlighted areas
// Surface = UI ground (variant = secondary ground)
// onX = foreground for X
// Outline = structure, Scrim/Error = state
```

---

如果你愿意，下一步我可以直接帮你做一件更狠的事：

- 🔧 **把这张表转成你项目里的 `DesignColorTokens.kt`**
- 🎨 或者按 `CaptureScreen` 直接给你一份「组件 → 颜色」对照表

你现在已经不是“用 Material3”，而是在**驯服它**了。

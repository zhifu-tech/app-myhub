# 写死色值排查汇总

本文档汇总项目中硬编码颜色（`Color(0x...)`、`Color.Black`/`Color.White` 等）的位置与建议。

---

## 1. 主题定义（预期内，无需改）

| 位置 | 说明 |
|------|------|
| `core/platform-compose/.../theme/Theme.kt` | `DarkColorScheme` / `LightColorScheme` 中定义 Material 3 调色板，属于主题源数据，保留写死色值合理。 |

---

## 2. 透明 / 语义色（可保留）

| 位置 | 用法 | 说明 |
|------|------|------|
| `feature/dashboard/.../DashboardScreen.kt` | `Color.Transparent` | NewCaptureCard 虚线卡片容器，透明合理。 |
| `feature/profile/.../ProfileScreen.kt` | `Color.Transparent` | ListItem 容器透明，跟随背景。 |
| `feature/settings/.../SettingsScreen.kt` | `Color.Transparent` | 同上。 |

---

## 3. 媒体叠加（Black/White + alpha）

用于图片/视频上的遮罩或文字，通常需要固定对比度，可按设计保留；若需随主题微调再改。

| 文件 | 用法 |
|------|------|
| `component/card/.../ArticleCard.kt` | `Color.Black.copy(alpha = 0.2f)` 遮罩，`Color.White` 文字 |
| `component/card/.../VideoCard.kt` | `Color.Black` 背景，`Color.White` 文案/图标（多处以 alpha） |
| `component/card/.../QuoteCard.kt` | `Color.White.copy(alpha = 0.05f)` 细分割线 |

---

## 4. 卡片类型图标色（typeIconColor）

各 `*CardComponent.kt` 的 `getTypeIconColor()` 返回固定色，用于类型标识。若产品希望与主题统一，可改为 `MaterialTheme.colorScheme.primary` 或扩展主题语义色。

| 文件 | 当前色值 | 用途 |
|------|----------|------|
| `ArticleCardComponent.kt` | `0xFF3B82F6` (blue) | 文章类型 |
| `CodeCardComponent.kt` | `0xFF10B981` (green) | 代码类型 |
| `IdeaCardComponent.kt` | `0xFFF59E0B` (amber) | 想法类型 |
| `QuoteCardComponent.kt` | `0xFF8B5CF6` (purple) | 引用类型 |
| `TodoCardComponent.kt` | `0xFFEF4444` (red) | 待办类型 |
| `VideoCardComponent.kt` | `0xFFEF4444` (red) | 视频类型 |
| `WordCardComponent.kt` | `0xFF06B6D4` (cyan) | 词汇类型 |

---

## 5. 卡片样式与内容内写死色值（建议优先适配主题）

### 5.1 CardStyles.kt（设计系统级）

| 位置 | 色值 | 说明 |
|------|------|------|
| `IdeaCard.backgroundColor` | 深色 `0xFF2A261C`，浅色 `0xFFFEF9E7` | 可按主题用 `surfaceContainerHigh` / `surface` 等替代 |
| `IdeaCard.borderColor` | 深色 `0xFF92400E`，浅色 `0xFFFDE68A` | 可改为 `outline` / `outlineVariant` |
| `QuoteCard.backgroundColor` | 深色 `0xFF1e2025`，浅色 `0xFFfdfbf7` | 同上，用主题表面色 |
| `ArticleCard.GradientColors` | `0xFF6366f1`, `0xFFa855f7`, `0xFFec4899` | 渐变可保留品牌色或映射到 theme tertiary 等 |

### 5.2 各卡片 UI 内直接写死的颜色

| 文件 | 色值 / 用途 |
|------|-------------|
| **QuoteCard.kt** | `0xFFFFB020`、`0xFFFEF3C7`、`0xFF92400E`、`0xFF1e293b`（背景/边框/文字） |
| **WordCard.kt** | `0xFFFFB020` |
| **ArticleCard.kt** | `0xFFFFB020`（高亮等） |
| **CodeCard.kt** | `0xFFFFB020`、`0xFF3b82f6`（高亮/标签） |
| **IdeaCard.kt** | `0xFFF59E0B`、`0xFF92400E`、`0xFFFFB020`、`0xFF1e293b`（背景/边框/文字） |
| **TodoCard.kt** | `0xFFFFB020` |

建议：能统一用 `MaterialTheme.colorScheme`（如 `primary`、`surface`、`onSurface`、`outline`）或 CardStyles 里已有语义的，逐步替换为主题/设计 token，以改善 light/dark 一致性。

---

## 6. 标签/其他 UI

| 文件 | 色值 | 说明 |
|------|------|------|
| `feature/card/.../CardDetailTags.kt` | 一组 `0xFF3B82F6` 等 6 色 | 标签色盘，可保留或收拢为 theme 扩展色 |

---

## 7. 已修复

| 位置 | 原色值 | 修改 |
|------|--------|------|
| `feature/dashboard/.../DashboardScreen.kt` (CollectionCard) | `Color(0xFF1E1F23)` 预览区背景 | 已改为 `MaterialTheme.colorScheme.surfaceContainerHigh` |

---

## 建议优先级

1. **高**：卡片背景/边框/正文（CardStyles、IdeaCard/QuoteCard 等）改为主题色，避免 light 下对比异常。
2. **中**：各卡片内高亮/装饰色（如 `0xFFFFB020`）统一为 theme 或设计 token。
3. **低**：typeIconColor、标签色、渐变等品牌色，按产品决定是否与主题绑定。

如需，我可以按上述优先级给出具体替换示例（如 IdeaCard 全用 theme 的改法）。

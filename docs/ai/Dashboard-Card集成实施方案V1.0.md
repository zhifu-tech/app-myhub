# Dashboard × Card 集成实施方案 V1.0

## 1. 目标与范围

- 目标：将 Dashboard 当前 `mockContentItems()` 的 UI 数据流，替换为基于 `Card` 的真实数据流。
- 范围：
- `ContentItem` 与 `Card/CardUi` 字段映射策略
- `VMContent.kt` 的真实数据请求接入
- 在“尚无生成能力”阶段，将现有 mock 数据写入数据库作为初始化数据
- 约束：允许修改前端模型定义；不修改数据库结构与领域模型（`Card` 及相关 domain）。

## 2. 方案决策

### 2.1 候选方案

1. 方案 A：保留 `ContentItem`，增加 `Card -> ContentItem` 映射层（Adapter）
2. 方案 B：Dashboard 彻底改为直接使用 `Card`

### 2.2 选择

选择方案 A（映射层）。

### 2.3 原因（业内通用实践）

1. UI 模型与领域模型职责不同

- `ContentItem` 含 `ImageVector/Color`（纯 UI 类型）
- `Card` 是领域模型（可序列化、跨端稳定）
- 直接让 UI 持有 `Card` 会把 UI 解析逻辑扩散到多个 Composable

2. 领域稳定性更高

- 后续 `Card` 增字段不会强制 UI 立刻变更
- 映射层可做兼容与兜底（缺字段、旧数据）

3. 更利于多端与演进

- 映射层可按页面做差异转换（Dashboard/Detail/Search）
- 与“Repository 返回领域，ViewModel 输出 UI State”的常见分层一致

### 2.4 方案优劣对比

| 维度       | 方案 A：映射层 | 方案 B：直接用 Card  |
|----------|----------|----------------|
| 分层清晰度    | 高        | 中              |
| UI 开发效率  | 高（字段已就绪） | 中（每页都要解析 Card） |
| 改造成本     | 低到中      | 中到高            |
| 长期维护     | 高        | 中              |
| 领域模型污染风险 | 低        | 高              |

## 3. 数据模型映射设计

## 3.1 字段映射表（`Card/CardUi -> ContentItem`）

| ContentItem 字段     | 来源                       | 规则                                |
|--------------------|--------------------------|-----------------------------------|
| `id`               | `card.id`                | 直接映射                              |
| `title`            | `card.title`             | 直接映射                              |
| `summary`          | `card.summary`           | 直接映射                              |
| `location`         | `card.location?.name`    | 无则空字符串                            |
| `updatedTimeMs`    | `card.updatedAt`         | `toEpochMilliseconds()`           |
| `tags`             | `card.tags`              | 直接映射                              |
| `cover.url`        | `card.ui.cover.imageUrl` |                      |
| `cover.icon`       | `card.ui.cover.iconKey`  | 通过 `iconKey -> ImageVector` 注册表转换 |
| `cover.background` | `card.ui.cover.bgColor`  | `#RRGGBB` -> `Color`              |
| `cover.tint`       | `card.ui.cover.tintColor`| 同上                                |
| `action.*`         | UI 规则层                   | 基于 `status/type` 生成默认动作           |

## 3.2 关键约束

1. UI 相关值都存在 `CardUi`（iconKey/bgColor/tintColor/imageUrl），不放进 `ContentItem` 以外层。
2. `ContentItem` 只在 feature/dashboard 使用，作为 ViewModel 输出模型。
3. 所有兜底逻辑集中在 Mapper，避免 UI 页面分散 if/else。

## 4. 请求链路替换（`VMContent.kt`）

## 4.1 改造目标

- 从 `flowOf(mockContentItems())` 改为 `cardRepository.flowCards(...)`
- 输出仍为 `Flow<List<ContentItem>>`

## 4.2 伪代码

```kotlin
fun DashboardViewModel.streamContentItems(...): Flow<List<ContentItem>> {
    val orderByUpdated = sortAsDate
    val orderByTitle = sortAsName

    return cardRepository.flowCards(
        userId = userId,
        cursorCardId = null,          // 首屏
        cursorTitle = null,
        cursorUpdatedAt = null,
        orderByUpdated = orderByUpdated,
        orderByTitle = orderByTitle,
        limit = pageSize
    )
        .map { cards -> cards.map { it.toDashboardContentItem() } }
        .onEach { items -> reduceDashboardState(items) }
}
```

## 4.3 依赖改造

1. `DashboardViewModel` 增加 `CardRepository` 注入。
2. `VMContent.kt` 移除对 `mockContentItems()` 的直接依赖。
3. 新增 Mapper 文件（建议）：

- `feature/dashboard/.../content/item/CardContentItemMapper.kt`

## 5. Mock 重建为“入库种子数据”

## 5.1 目标

- 保留现有 demo 体验，但来源改为数据库。
- 避免每次进入页面都重复写入。

## 5.2 策略

1. 首次进入 Dashboard 时执行 `seedIfNeeded(userId)`。
2. 若该用户 `card` 为空，则将 `mockContentItems()` 转为 `Card` 并 `insertCard`。
3. 写入后，页面仍走 `flowCards` 查询（不再直连 mock）。

## 5.3 伪代码

```kotlin
suspend fun seedIfNeeded(userId: String) {
    val firstPage = cardRepository.flowCards(
        userId = userId,
        cursorCardId = null,
        limit = 1
    ).first()

    if (firstPage.isNotEmpty()) return

    mockContentItems()
        .map { it.toSeedCard(userId) }
        .forEach { cardRepository.insertCard(it, userId) }
}
```

## 5.4 数据转换（Mock -> Card）

- `ContentItem.summary` -> `card.summary`（直接映射）
- `ContentItem.cover.*` -> `card.ui.cover.*`（iconKey/color/url）

## 6. 落地步骤（建议顺序）

1. 新增 `Card -> ContentItem` Mapper + `icon/color` 解析工具。
2. 改造 `DashboardViewModel` 注入 `CardRepository`。
3. 改造 `VMContent.kt`：从 mock flow 切换到 `flowCards`。
4. 增加 `seedIfNeeded(userId)` 并在 Dashboard 初始化链路前置执行。
5. 删除 `VMContent.kt` 对 `mockContentItems()` 的直接读取（保留 mock 仅作 seed 输入）。

## 7. 风险与规避

1. 风险：`iconKey` 无法映射

- 规避：提供默认 icon（如 `EditNote`）

2. 风险：颜色字段非法

- 规避：解析失败走默认主题色

3. 风险：旧数据无 `ui`

- 规避：Mapper 内统一 fallback（media 首图 + 默认颜色/图标）

4. 风险：重复 seed

- 规避：以“该用户是否已有 card”作为幂等条件

## 8. 本期验收标准

1. Dashboard 不再直接使用 `mockContentItems()` 渲染。
2. 首次空库会自动注入 demo 数据，刷新后仍可读出相同内容。
3. 排序（按时间/按标题）由 `flowCards` 驱动，UI 展示与当前一致。
4. UI 层仍消费 `ContentItem`，未直接依赖 `Card` 细节。

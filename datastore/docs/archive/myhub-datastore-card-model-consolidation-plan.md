# Card Model Consolidation Plan

## 背景

当前卡片相关模型有三层：

- `tech.zhifu.app.myhub.datastore.database.Card`：SQLDelight 生成表模型，必须保留。
- `tech.zhifu.app.myhub.datastore.model.domain.Card`：领域模型，承载持久化语义与可扩展原始字段。
- `tech.zhifu.app.myhub.ui.model.ContentCard`：UI 展示模型，做了一层字段收敛和展示态归一化。

问题不在于模型数量本身，而在于 `Card -> ContentCard` 之间存在重复映射和字段拷贝。

## 结论

最终目标是只保留 `Card` 作为卡片主模型。

这次执行的边界是：

- 删除 `ContentCard` 作为独立 UI 模型。
- 让 Dashboard、Preview、AI 预览直接消费 `Card` + `CardAggregate`。
- `CardMedia` 改成可承载实际展示媒体的统一模型。
- `Card` 的扩展字段仍然保留，`map` 只做 raw 字段的懒解析缓存。

## 字段映射方案

`ContentCard` 当前字段到 `Card` 的映射关系如下，替换后这些字段要从 `Card` 或 `CardAggregate` 派生：

- `id` -> `Card.id`
- `title` -> `Card.title`
- `summary` -> `Card.summary`
- `location` -> `Card.location?.name.orEmpty()`
- `updatedAt` -> `Card.updatedAt.toEpochMilliseconds()`
- `status` -> `Card.status`
- `tags` -> `Card.tags`
- `cover` -> `Card.ui?.cover` 派生后的展示对象
- `media` -> 仍然是 UI 层额外拼装的数据，不属于 `Card`
- `action` -> 由 `Card.status` 派生，不属于 `Card`

### 新模型职责

- `Card` 负责存储、查询、同步、序列化。
- `CardAggregate` 负责把 `Card` 和媒体列表组合起来，作为 UI 传输容器。
- `CardMedia` 负责媒体展示数据，替代 `ContentCardMedia`。
- `cover`、`action` 继续由 `Card` 派生。

## `cover` 是否需要保留

`cover` 需要保留“展示语义”，但不需要保留在 `ContentCard` 里，因为 `ContentCard` 会被删除。

原因：

- 预览和列表确实依赖封面图、背景色、图标、tint。
- 这些信息在 `Card.ui.cover` 里已经存在。
- `cover` 应该改为 `Card` 的派生值，而不是独立模型字段。

建议：

- 保留 `Card.ui.cover` 作为卡片的持久化展示配置。
- 所有 UI 组件直接从 `Card` 或 `CardAggregate` 构建展示样式。

## 执行清单

- [ ] 让 `CardMedia` 变成实际媒体展示模型，替代 `ContentCardMedia`。
- [ ] 新增 `Card` / `CardAggregate` 展示扩展，提供 `cover`、`action`、`tags`、`location`、`updatedAtMillis` 等派生值。
- [ ] 让 Dashboard UI state 和 ViewModel 改为传递 `CardAggregate`。
- [ ] 让 Preview state 和 Preview UI 改为传递 `CardAggregate`。
- [ ] 让 AI 草稿预览直接构造 `CardAggregate`。
- [ ] 删除 `ContentCard`、`ContentCardMedia`、`ContentCardAction`、`ContentCardCover` 以及旧的 `Card -> ContentCard` 映射。
- [ ] 清理所有 `ContentCard` 的引用。
- [ ] 回头收敛 `CardAggregate` / `CardMedia` 的对外命名，保证只保留一条卡片主模型链路。

## 风险

- `Card` 不是 Compose 友好的展示模型，UI 会更依赖扩展属性和派生函数。
- `Card.updatedAt` 仍需要和 UI 侧的毫秒时间戳互相转换。
- `CardAggregate` 作为组合容器要控制边界，避免再次演化成第二套卡片模型。

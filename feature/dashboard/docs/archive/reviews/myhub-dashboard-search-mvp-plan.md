# Dashboard 搜索实现方案（MVP）

## 1. 目标

在 Dashboard 已有搜索入口的基础上，实现可用的 `query` 搜索能力，支持依据输入条件从数据库过滤匹配卡片数据，并保持现有排序与分页体验不变。

## 2. 本期范围

- 只实现 `query` 搜索。
- 不实现多词拆分（AND/OR）。
- 不实现 `types/status/tags/dateRange` 组合筛选。

## 3. 方案

### 3.1 搜索参数与接口变更

- 在现有 `flowCards(...)` 链路中直接新增可选参数 `query: String?`。
- `query` 为空时走原有列表查询，非空时走搜索查询。

### 3.2 数据层链路（数据库过滤）

数据层已打通 `query` 透传链路：

- `CardRepository`
- `CardStore`
- `LocalCardDataSource`
- `card.sq`

约束：搜索在数据库层完成，不在 UI 或内存列表上做二次过滤。

### 3.3 SQL 匹配策略

实现大小写不敏感模糊匹配（`LIKE` + `lower(...)`），覆盖字段：

- `title`
- `summary`
- `content`（JSON 文本）
- `tags`（JSON 文本）
- `location`（JSON 文本）
- `source`（JSON 文本）

并保持现有能力不变：

- 排序（按更新时间 / 按标题）
- 分页游标（first/next）

### 3.4 ViewModel 触发策略

- `searchState.query` 已接入数据请求链路。
- 输入变化时会触发刷新并执行数据库查询。
- 当前实现未引入 `debounce`；通过“相同 query 不重复触发”减少部分无效刷新。

### 3.5 Debounce 优化方案（建议）

目标：将“每次按键都触发查询”优化为“输入停顿后触发查询”，降低频繁刷新和数据库查询压力。

建议做法：

1. 在 `DashboardViewModel` 增加一个搜索输入流（例如 `MutableStateFlow<String>`）。
2. `updateSearchStateQuery(query)` 仅负责：
    - 更新 `searchState.query`
    - 推送 query 到搜索输入流
    - 不直接调用 `refresh()`。
3. 在 `observeUiStateFlow()` 中新增搜索处理链路：
    - `trim()`
    - `debounce(300ms)`（建议区间 `250~350ms`）
    - `distinctUntilChanged()`
    - `refresh()`
4. `resetSearchState()` 保持“立即恢复默认列表”的体验：
    - 清空 query 后直接触发一次 `refresh()`，不等待 debounce。
5. `loadMore()` 继续读取 `searchState.query`，分页逻辑无需变化。

建议验收：

- 快速连续输入时，查询次数显著少于按键次数。
- 停止输入约 `300ms` 后结果更新。
- 清空 query 后列表立即恢复。
- 排序与分页行为保持不变。

### 3.6 搜索框显示策略

- 搜索入口默认仍受内容数量阈值控制。
- 当 `query` 非空时，搜索框始终显示，避免结果变少后输入框消失。

## 4. 验收标准（MVP）

- 输入 `query` 后，Dashboard 列表展示数据库过滤结果（命中 `title/summary/content/tags/location/source` 任一字段）。
- 搜索结果遵循当前排序配置（按更新时间或按标题）。
- 搜索态下分页可继续加载下一页结果，游标逻辑不变。
- 清空 `query` 后恢复默认列表查询结果。
- `query` 非空时，搜索框不会因结果数量低于阈值而隐藏。

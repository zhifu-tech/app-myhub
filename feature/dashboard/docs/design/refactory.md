# Dashboard Refactor Plan（App / Collection / Card）

## 1. 目标

- 将 Dashboard 从“单一 `Content + flags`”重构为“显式状态 + 模块分层”。
- 以 `app / collection / card` 三域拆分职责，降低耦合与并发状态覆盖风险。
- 保持 UI 行为不变，逐步迁移，确保每一步可编译、可回滚。

## 2. 命名约定（To-Be）

- 初始化态统一使用 `init` 语义，不再使用 `bootstrap`。
- 推荐状态名：
    - `DASHBOARD_INIT_LOADING_BLOCKING`
    - `DASHBOARD_INIT_FAILED_RETRYABLE`
    - `DASHBOARD_CONTENT_EMPTY_READY`
    - `DASHBOARD_CONTENT_READY_STABLE`
    - `DASHBOARD_CONTENT_REFRESHING_NON_BLOCKING`
    - `DASHBOARD_CONTENT_PARTIAL_ERROR_STALE`
    - `DASHBOARD_CONTENT_PAGING_CARDS_LOADING_MORE`
    - `DASHBOARD_CONTENT_PAGING_COLLECTIONS_LOADING_MORE`
    - `DASHBOARD_AUTH_EXPIRED_REDIRECTING`

## 3. 分层模型

### 3.1 App 层（页面编排 / 全局状态）

职责：

- 维护页面级状态机（init/content/auth）。
- 聚合 collection/card 子域输出，生成 `DashboardUiState`。
- 统一 effect（如 `NavigateToLogin`、`ShowSnackbar`）。

建议文件：

- `DashboardScreen.kt`（Route，仅收集 state/effect 与导航）
- `DashboardContent.kt`（状态分发）
- `DashboardViewModel.kt`（只做 action->mutation->reduce）
- `state/DashboardUiState.kt`
- `state/DashboardAction.kt`
- `state/DashboardMutation.kt`
- `state/DashboardEffect.kt`
- `state/DashboardReducer.kt`

### 3.2 Collection 层（集合域）

职责：

- 管理 collections 列表与分页子状态。
- 仅输出集合域所需 VM 片段（items/hasMore/isLoading/error）。

建议文件：

- `collection/CollectionSection.kt`
- `collection/CollectionPagination.kt`
- `collection/CollectionUiModel.kt`

### 3.3 Card 层（卡片域）

职责：

- 管理 recent cards 列表、分页、卡片交互。
- 仅输出卡片域 VM 片段（items/hasMore/isLoading/error）。

建议文件：

- `card/CardSection.kt`
- `card/CardPagination.kt`
- `card/CardUiModel.kt`

## 4. 状态与并发规则

- 刷新与分页必须防重入：
    - `refresh` 期间默认禁止发起新的 `loadMoreCards/loadMoreCollections`。
- `cards` 与 `collections` 分页必须独立，不互锁。
- `_uiState` 只能由 reducer 更新，禁止在业务函数中直接 `copy` 覆盖。
- 401 优先收敛到 `AuthExpiredRedirecting` + 单次导航 effect。

## 5. 迁移步骤（分阶段）

### 阶段 A：状态模型重构（不改 UI 结构）

- 引入 `DashboardAction/Mutation/Reducer`。
- 将当前 `InitialLoading/Content` 迁移到显式状态结构。
- 保持 `DashboardScreen` 视觉与交互不变。

验收：

- 编译通过。
- 刷新/分页/401 行为与现网一致。

### 阶段 B：UI 分层（Route / Content / Sections）

- `DashboardScreen` 仅保留 Route。
- 拆 `AppBar`、`CollectionSection`、`CardSection`、`FocusReview`。
- 状态视图（InitLoading/InitFailed/Empty/PartialError）独立组件化。

验收：

- 代码搜索可直接定位到各域入口。
- 无功能回归。

### 阶段 C：细化子域边界

- collection/card 各自分页逻辑内聚。
- UI 仅消费域级 uiModel，不直接拼装原始仓库数据。

验收：

- `collection` 变更不影响 `card` 编译与行为（反之亦然）。

## 6. 测试建议

- 状态流转测试：覆盖 9 个主状态及关键转移。
- 并发测试：`refresh + loadMore` 冲突防重入。
- 401 测试：只触发一次跳转 effect。
- UI 快照测试：Init/Empty/Stable/PartialError。

## 7. 风险与回滚

风险：

- reducer 切换期可能出现状态字段缺失。
- 分页逻辑迁移期间可能触发重复请求。

回滚策略：

- 每阶段单独提交。
- 保留旧 `DashboardUiState` 适配层，直至新状态稳定后删除。

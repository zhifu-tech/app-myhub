# Store5 使用问题与纠正说明

> 基于对 gh-store-store 源码的重新阅读及与当前实现的对照，梳理出的**使用中不正确或需改进**之处，便于后续落地修正。
>
> 相关文档：[Store5 架构设计及使用指南](./store5-architecture-guide.md)、[Store 设计问题分析与优化建议](./store-design-analysis.md)

---

## 1. Fetcher 层：异常与 FetcherResult

### 1.1 问题描述

- **Store5 设计**：Fetcher 内抛出的异常**不会被 Store 捕获**，会直接传播给调用方（见 Fetcher.kt 注释）。
- **当前实现**：各 `XXXStoreFetcher` 使用 `Fetcher.of { key -> ... }`，在「无数据」等场景直接 `throw NoSuchElementException(...)`，或依赖 RemoteDataSource 抛异常。这些异常会一路传到 Repository/ViewModel，需在业务层处理。

### 1.2 是否正确

- **从「行为」上**：可以工作，但错误语义由「异常」表达，与 Store 推荐的「用 FetcherResult.Error 表达可预期错误」不一致。
- **从「可维护性」上**：若希望统一「网络/业务错误」由 Store 以 `StoreReadResponse.Error` 形式下发，更推荐在 Fetcher 内使用 `Fetcher.ofResult`，将「无数据」「404」等转为 `FetcherResult.Error.Message/Custom`，而不是抛异常。

### 1.3 建议

| 项目                   | 建议                                                                                                                                                                                |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **可预期错误**（如 404、无数据） | 使用 `Fetcher.ofResult`，返回 `FetcherResult.Error.*`，由 Store 转为 `StoreReadResponse.Error`，便于 UI 统一处理。                                                                                 |
| **不可预期异常**           | 要么在 Fetcher 内 try/catch 后转为 `FetcherResult.Error.Exception(e)`，要么保持抛出，在 Repository/ViewModel 统一 catch 并映射为业务错误。                                                                   |
| **文档**               | 在 Fetcher 相关代码或文档中注明：本项目当前采用「异常上抛、Repository 层处理」的策略，与官方推荐的 FetcherResult 方式二选一即可，但需统一。若暂时不改为 FetcherResult，应在 Fetcher 或 Repository 层统一 catch 并映射为业务错误/UI 状态，避免同一类错误在不同界面处理方式不一致。 |

---

## 2. SourceOfTruth reader：Flow 的语义

### 2.1 问题描述

- **Store5 设计**：`SourceOfTruth.reader(key)` 应返回一个**可持续发射**的 Flow，用于观察本地数据变化；Store 会把此 Flow 的值推送给 stream 的 collector。
- **当前实现**：例如 `CardStoreSourceOfTruth` 中，对 `ByUser` 等 key 使用 `flow { ... localDataSource.getCards(...); emit(...) }`，即**单次查询后只 emit 一次**，不是「观察数据库变化」的 Flow。

### 2.2 是否正确

- **ById + observeCard**：使用 `localCardDataSource.observeCard(key.id).collect { emit(...) }`，符合「可持续观察」的语义。
- **ByUser**：使用 `getCards` 单次查询再 emit，**不符合** SourceOfTruth 的「观察」语义：本地后续插入/更新/删除不会通过该 Flow 再推送给 Store，可能导致 UI 不随本地写入更新。

### 2.3 建议

| 项目                   | 建议                                                                                                                                                       |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| **ByUser / 列表类 Key** | 若本地有 `observeCards(userId)` 或等价「按列表观察」的 API，应改为在 reader 中返回该 Flow；若当前没有，需在 LocalCardDataSource 增加类似能力（如基于 SQLDelight 的 observe 查询），再在 SourceOfTruth 中使用。 |
| **文档**               | 在 Store 使用指南中明确：SourceOfTruth.reader 应尽量使用「可观察」的本地 API，避免仅单次 get 后 emit。                                                                                 |

---

## 3. Repository 同时依赖 Store 与 LocalDataSource

### 3.1 问题描述

- **CardRepositoryImpl** 同时持有：`CardStore`、`LocalCardDataSource`（以及 SyncRepository、TagRepository 等）。
- 读卡片：通过 `store.get/stream`，符合「以 Store 为单一读入口」的设计。
- **getReviewProgress(userId)**：直接调用 `localCardDataSource.getReviewProgress(userId)`，**绕过了 Store**。

### 3.2 是否正确

- **语义上**：若「复习进度」不属于 Store 管理的「卡片列表/单卡」同一数据视图，而是衍生/统计数据，则单独走 LocalDataSource 可以是合理分工。
- **架构上**：若希望「所有本地读都经 Store」，则应对 ReviewProgress 提供独立 Store 或至少明确文档说明：哪些读走 Store，哪些允许直接走 DataSource（及原因）。

### 3.3 建议

| 项目       | 建议                                                                                    |
|----------|---------------------------------------------------------------------------------------|
| **当前做法** | 在文档中明确：**getReviewProgress 等衍生/统计类读操作**不经过 CardStore，直接使用 LocalCardDataSource，属于有意设计。 |
| **长期**   | 若希望统一读路径，可考虑为「复习进度」建只读 Store 或扩展 CardStore Key/Output，再迁移。                            |

---

## 4. streamCard 中 refresh 被写死为 false

### 4.1 问题描述

- **CardRepositoryImpl.streamCard(cardId, refresh: Boolean)**：内部调用 `StoreReadRequest.cached(..., refresh = false)`，即**未使用参数 refresh**，始终不刷新。

### 4.2 建议

- 将 `refresh = false` 改为 `refresh = refresh`，使调用方可以控制是否在 stream 时顺带刷新该 key。

---

## 5. Store 与 Repository 的职责边界

### 5.1 当前状态

- **Store**：负责「按 Key 读/写、缓存、Fetcher/SourceOfTruth/Updater」。
- **Repository**：组合 Store + DataSource + Sync + 业务规则（如 Tag 解析），对外提供「业务语义 API」（如 insertCard、getCards、streamCards）。

### 5.2 是否合理

- 合理：Repository 作为「业务门面」，封装多数据源与业务规则；Store 作为「缓存+网络+本地」的统一读写在技术上是正确的。
- 需统一的是：**读路径**尽量以 Store 为主；**写路径**经 Store.write；**例外**（如 getReviewProgress、Sync 专用 Outbox）在文档中写清。

---

## 6. 小结：优先落地项

| 优先级 | 项                                       | 说明                                              | 完成标准                                                                        |
|-----|-----------------------------------------|-------------------------------------------------|-----------------------------------------------------------------------------|
| 高   | SourceOfTruth reader 对列表 Key 使用可观察 Flow | 避免列表类 key 只 emit 一次，本地后续变更不反映到 UI。              | 所有列表类 Key 的 reader 均使用 observe*，本地写入后 stream 能收到新数据。                        |
| 中   | Fetcher 可预期错误改用 FetcherResult           | 统一错误形态，便于 UI 和 Repository 处理。                   | 可预期错误（如 404、无数据）经 FetcherResult.Error 下发，UI 统一按 StoreReadResponse.Error 处理。 |
| 中   | streamCard 的 refresh 参数生效               | 小改动，避免 API 误导。                                  | `streamCard(cardId, refresh = true)` 会触发 Fetcher 刷新。                        |
| 低   | 文档明确「例外读路径」                             | 如 getReviewProgress 直接走 LocalDataSource 的原因与范围。 | 架构文档中列出不经 Store 的读路径及原因（衍生/统计、Sync、Auth）。                                   |

---

## 7. 落地执行状态

| 优先级 | 项                                       | 状态    | 说明                                                                                                                                                                                                                                    |
|-----|-----------------------------------------|-------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 高   | SourceOfTruth reader 对列表 Key 使用可观察 Flow | ✅ 已落地 | CardStoreSourceOfTruth ByUser 使用 observeCards + 分页 map；CollectionStoreSourceOfTruth ByUser 使用 observeCollections + 分页 map；TemplateStoreSourceOfTruth ById/All 使用 observeTemplates。LocalCardTemplateDataSource 已新增 observeTemplates()。 |
| 中   | Fetcher 可预期错误改用 FetcherResult           | ✅ 已落地 | CardStoreFetcher、TemplateStoreFetcher 已改为 Fetcher.ofResult，404/无数据返回 FetcherResult.Error.Message。                                                                                                                                     |
| 中   | streamCard 的 refresh 参数生效               | ✅ 已落地 | CardRepositoryImpl.streamCard 已使用 `refresh = refresh`。                                                                                                                                                                                |
| 低   | 文档明确「例外读路径」                             | ✅ 已落地 | 见 [Datasource 使用梳理及与 Store 的关系](./datasource-usage-and-store.md) 第 3.2 节。                                                                                                                                                             |

以上内容将随实现修正而更新版本与状态。

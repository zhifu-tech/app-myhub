# Datasource 使用梳理及与 Store 的关系

> 说明 DataSource 在项目中的角色、引入 Store 后的数据流变化，以及是否需要调整的结论与建议。
>
> 相关： [Store5 架构设计及使用指南](./myhub-datastore-store5-architecture-guide.md)、[Store 使用问题与纠正说明](./archive/myhub-datastore-store-usage-issues.md)

---

## 1. Datasource 的当前定位

### 1.1 职责

| 类型                    | 作用                   | 典型实现                                                                           |
|-----------------------|----------------------|--------------------------------------------------------------------------------|
| **Local DataSource**  | 提供**本地持久化（DB）**的操作入口 | 封装 SQLDelight/DB 的 CRUD、observe；如 `LocalCardDataSource`、`LocalUserDataSource`。 |
| **Remote DataSource** | 提供**网络请求**的操作入口      | 封装 HTTP API 调用、鉴权、序列化；如 `RemoteCardDataSource`、`RemoteUserDataSource`。         |

即：**DataSource = 单一数据来源的技术入口**（本地 DB 或 远程 API），不负责缓存策略、多级读写顺序、冲突解决，这些由上层（Store / Repository）负责。

### 1.2 当前被谁使用

- **Repository 层（repository-client）**
    - **Store 的组成**：
        - **Fetcher**：内部调用 **RemoteXXXDataSource**（如 `RemoteCardDataSource.getCardById/getCards`）。
        - **SourceOfTruth**：内部调用 **LocalXXXDataSource**（如 `LocalCardDataSource.observeCard/insertCard/getCards`）。
        - **Updater**：内部调用 **RemoteXXXDataSource** 的写接口（如 `updateCard`）。
    - **Repository 实现**：除通过 Store 读写外，部分场景**直接**使用 DataSource，例如：
        - `CardRepositoryImpl.getReviewProgress(userId)` → `LocalCardDataSource.getReviewProgress`。
        - Sync 相关：`SyncCoordinatorImpl`、`SyncRepositoryImpl` 等使用 `LocalSyncDataSource` / `RemoteSyncDataSource`（Outbox、OpLog、服务端拉取等）。

- **结论**：
    - **读/写「业务实体」的主路径**：已经过 **Store**（Store 再内部分别用 Remote + Local DataSource）。
    - **DataSource** 仍然是「DB 操作入口」和「网络操作入口」，但**不直接对 UI/业务暴露**，而是作为 Store 与 Sync 的底层依赖。

---

## 2. 引入 Store 之后是否需要调整 DataSource

### 2.1 是否需要「撤销」或「弱化」DataSource

- **不需要。**
    - Store 的 Fetcher / SourceOfTruth / Updater 必须依赖「能读网络」「能写读本地」「能写网络」的抽象；当前这些抽象就是 **Remote*DataSource** 和 **Local*DataSource**。
    - 若去掉 DataSource，就要把「HTTP 调用」「DB 访问」直接写在 Fetcher/SourceOfTruth/Updater 里，不利于测试、替换和复用，反而更差。

### 2.2 需要遵守的架构约定

引入 Store 后，建议在**约定**上做如下明确（并已在当前实现中大体遵循）：

| 约定                 | 说明                                                                                                                                                                 |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **业务实体的「读」**       | 以 **Store** 为统一入口（stream/get/fresh），不鼓励 Feature/ViewModel 直接调 Local/Remote DataSource 读业务数据。                                                                       |
| **业务实体的「写」**       | 经 **Store.write**（或 Repository 封装后的写接口，内部调 Store.write），由 Store 负责写本地 + Updater 写远程。                                                                               |
| **DataSource 的用途** | 仅作为 **Store（Fetcher/SourceOfTruth/Updater）** 和 **Sync/专项逻辑** 的底层实现依赖；不对外暴露为「业务 API」。                                                                               |
| **例外**             | 衍生/统计类读（如 getReviewProgress）、同步协议与 Outbox/OpLog、认证（Auth）等，可不经 Store，直接使用对应 DataSource；详细列表与原因见 [Store 使用问题与纠正说明](./archive/myhub-datastore-store-usage-issues.md)。 |

### 2.3 小结：DataSource 的角色变化（概念上）

- **引入 Store 前**：DataSource 可能是「业务层直接调用的读写入口」。
- **引入 Store 后**：DataSource 是 **「为 Store 和 Sync 提供 DB/网络能力」的技术层**；业务层面向的是 **Repository + Store**，而不是 DataSource。
- **接口形态**：无需为「迁就 Store」而改 DataSource 的接口风格；保持「按 key/条件返回数据、suspend/Flow」即可，由 Fetcher/SourceOfTruth/Updater 做适配。

---

## 3. 当前 DataSource 使用情况罗列

### 3.1 被 Store 使用（Fetcher / SourceOfTruth / Updater）

| Store / 组件 | Local DataSource                          | Remote DataSource                                                        |
|------------|-------------------------------------------|--------------------------------------------------------------------------|
| CardStore  | LocalCardDataSource（reader/writer/delete） | RemoteCardDataSource（getCardById/getCards；Updater：updateCard/deleteCard） |
| UserStore  | LocalUserDataSource                       | RemoteUserDataSource                                                     |
| Sync       | LocalSyncDataSource                       | RemoteSyncDataSource                                                     |

以上用法符合「Store 作为读写核心、DataSource 只做底层通道」的定位。

### 3.2 不经过 Store 的 DataSource 使用（例外）

| 调用方                                        | 使用的 DataSource                           | 用途                                  | 不经过 Store 的原因（概括）          |
|--------------------------------------------|------------------------------------------|-------------------------------------|----------------------------|
| CardRepositoryImpl                         | LocalCardDataSource                      | getReviewProgress(userId) — 衍生/统计类读 | 衍生/统计视图，非 Store 管理的实体读。    |
| SyncCoordinatorImpl / SyncRepositoryImpl 等 | LocalSyncDataSource、RemoteSyncDataSource | Outbox、OpLog、服务端拉取/推送等同步机制          | 同步协议与时序与 Store 的 Key 模型不同。 |
| AuthService                                | RemoteAuthDataSource                     | 登录等认证                               | 认证流程独立于业务实体读写。             |

这些应视为**有意例外**，在架构文档中保留说明即可，无需强行塞进 Store。

### 3.3 建议在文档中固化的结论

- **DataSource 的职责**：提供「本地 DB」与「远程网络」的操作入口；不做缓存与多级读写策略。
- **引入 Store 后**：业务实体的读写以 **Store（经 Repository）** 为主；DataSource 仅作为 Store 与 Sync 的依赖。
- **不需要**因为用了 Store 而删除或合并 DataSource 层；**需要**的是明确「谁可以调 DataSource」和「例外列表」，避免业务层绕过 Store 随意读本地/网络。

---

## 4. 参考资料

- [Store5 架构设计及使用指南](./myhub-datastore-store5-architecture-guide.md)
- [Store 使用问题与纠正说明](./archive/myhub-datastore-store-usage-issues.md)
- [MyHub 本地数据源模块方案设计](../datastore/datasource-local/docs/myhub-datastore-datasource-local-infra-v1.0.md)
- [MyHub 远程数据源模块方案设计](../datastore/datasource-remote/docs/myhub-datastore-datasource-remote-infra-v1.0.md)

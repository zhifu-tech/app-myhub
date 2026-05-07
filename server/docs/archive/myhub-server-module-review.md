# Server 模块梳理与架构评审（整合版）

> 从资深工程师视角对 server 相关模块的现状、模块划分、层级划分进行梳理与总结；文末整合外部资深 Backend 工程师点评与落地执行要点，便于按优先级执行。

**文档版本**：v1.1 整合版  
**编写/整合日期**：2026-01-29  
**适用范围**：Server 及 datastore 中与 server 相关的模块（repository-server-api、repository-server、database-server）。

---

## 一、现状梳理

### 1.1 与 Server 相关的模块清单

| 类型      | 模块                                | 说明                                                      | 启用条件              |
|---------|-----------------------------------|---------------------------------------------------------|-------------------|
| **应用**  | `server`                          | Ktor 应用入口、API 路由、Service、Auth、异常配置                      | `isServerEnabled` |
| **数据层** | `datastore:repository-server-api` | 服务端 Repository 接口定义（6 个）                                | `isServerEnabled` |
| **数据层** | `datastore:repository-server`     | 服务端 Repository 实现（依赖 LocalDataSource + database-server） | `isServerEnabled` |
| **数据层** | `datastore:database-server`       | 服务端数据库驱动与配置（SQLite/PostgreSQL）                          | `isServerEnabled` |

**共享模块（非 server 独有，但 server 依赖）**：

- `core:logger`
- `datastore:model`、`datastore:model-dto`（含 DTO、异常类）
- `datastore:sync`
- `datastore:database`（SQLDelight schema）
- `datastore:datasource-local`

### 1.2 Server 模块内部结构（当前）

```
server/
├── src/main/kotlin/tech/zhifu/app/myhub/
│   ├── Application.kt              # 入口 + Ktor 插件 + 路由挂载
│   ├── api/                         # API 层（8 个路由模块）
│   │   ├── AuthApi.kt
│   │   ├── CardsApi.kt
│   │   ├── CardTemplatesApi.kt
│   │   ├── CollectionsApi.kt
│   │   ├── SyncApi.kt
│   │   ├── TagsApi.kt
│   │   ├── TemplatesApi.kt         # 已整文件注释，历史遗留
│   │   └── UsersApi.kt
│   ├── auth/                        # 认证
│   │   ├── AuthenticationConfig.kt
│   │   ├── JwtConfig.kt
│   │   ├── TokenService.kt
│   │   └── UserPrincipal.kt
│   ├── di/
│   │   └── Koin.kt                  # 组装 logger / database / repository / service
│   ├── exception/
│   │   └── configException.kt      # StatusPages 统一异常处理
│   └── service/                     # 业务逻辑层（7 个 Service）
│       ├── CardService.kt
│       ├── CardTemplateService.kt
│       ├── CollectionService.kt
│       ├── SyncService.kt
│       ├── TagService.kt
│       ├── UserService.kt
│       └── di/
│           └── ServiceModule.kt
└── docs/
```

**数据流与依赖关系**：

- **API** → 接收请求、解析参数、调用 **Service**、序列化响应。
- **Service** → 业务逻辑、校验、调用 **Repository**（接口来自 `repository-server-api`）。
- **Repository 实现**（`repository-server`）→ 使用 **LocalDataSource** + **database-server** 读写持久化。

异常类（如 `ApiException`、`NotFoundException`）定义在 **model-dto**，server 仅在 `exception/configException.kt` 中配置 Ktor StatusPages 将异常映射为 HTTP 与统一错误体。

### 1.3 平台与构建约束

- `server` 仅在 `enabledPlatforms` 包含 `server` 时被 include。
- 合法组合：**必须** `jvm` + `server` 一起启用（不能只开 server）。
- 单模块构建：server 自身未再拆子模块，所有 API/Service/Auth/DI 均在一个 Gradle 模块内。

### 1.4 与文档的差异

- `server/docs/myhub-server-architecture.md` 中的「推荐结构」已大部分落地（api / service 在 server 内，repository 在 datastore）。
- 文档中的 **StatisticsApi/StatisticsService** 未实现。
- 文档中的 **Templates** 与实现中的 **CardTemplates** 命名不一致；`TemplatesApi.kt` 已整文件注释，为历史遗留。
- Repository 层文档写的是「在 server 内」，实际在 **datastore/repository-server-api** 与 **repository-server**，与当前实现一致，但文档需同步更新。

### 1.5 测试与运维

- **测试**：server 模块下无单元测试、集成测试（仅 build.gradle.kts 中声明了 testImplementation，未见测试源文件）。
- **部署**：具备 Dockerfile、docker-compose、健康检查 `/health`，与架构文档描述一致。

---

## 二、模块划分是否合理

### 2.1 已做对的部分

1. **datastore 侧接口与实现分离**
    - `repository-server-api`：纯接口 + model/sync，无实现与数据库依赖，便于测试与多实现。
    - `repository-server`：实现 + DI 模块，依赖 database-server、datasource-local，职责清晰。

2. **database-server 独立**
    - 驱动与配置（SQLite/PostgreSQL、环境变量）单独成模块，与 repository 解耦，便于切换/扩展数据库。

3. **DTO 与异常共享**
    - Request/Response 与 HTTP 相关异常放在 **model-dto**，client 与 server 共用，避免重复定义和分裂。

4. **Server 内按层分包**
    - api / service / auth / di / exception 分包清晰，符合「一个模块内按职责分包」的常见做法。

### 2.2 可改进点

1. **Server 为单体模块**
    - 当前所有 API、Service、Auth 都在同一 `server` 模块内。若未来有「多入口（如 admin API、公开 API）」或「按领域拆团队」需求，可考虑：
        - 将 **api + service** 按领域拆成子模块（如 `server:api-cards`、`server:api-users`），或
        - 保持单体，但用包/命名更严格地区分领域（如 `api.card`、`api.user`），便于后续再拆。
    - 当前规模下单体可接受，不必为拆而拆。

2. **历史遗留文件**
    - `TemplatesApi.kt` 已整文件注释且未被使用，建议删除或明确标记为 deprecated，避免与 `CardTemplatesApi` 混淆。

3. **文档与实现对齐**
    - `myhub-server-architecture.md` 中 Statistics、Templates 命名与推荐结构中的 repository 位置，建议更新为与当前实现一致（含 repository 在 datastore、CardTemplates 命名等）。

---

## 三、层级划分是否合理

### 3.1 依赖方向（总体正确）

```
server (API + Service + Auth)
  ↓
repository-server-api (接口)  ←  server 依赖接口
  ↑
repository-server (实现)     ←  依赖 database-server, datasource-local
  ↓
database-server / datasource-local
```

- Server 不直接依赖 **repository-server** 的实现类，只依赖 **repository-server-api** 的接口（通过 Koin 注入实现）。若将来把「接口」挪到 server 侧或共享模块，需要再评估；当前放在 datastore 侧与「数据访问契约」归属一致，合理。
- **database-server** 仅被 **repository-server** 使用，不向 server 暴露，层级清晰。

### 3.2 可改进点

1. **Application 中的依赖解析方式**
    - 当前在 `Application.module()` 里用 `GlobalContext.get().get<X>()` 获取 Service/Repository 再传给各 `xxxApi()`，可读性和可测试性一般。更推荐：
        - 使用 Ktor 的 **Koin 插件** 在 Route 内按需 `get<>()`，或
        - 在路由扩展函数层统一接收一个「Facade」或「ApiDependencies」对象，由 Koin 注入该对象，从而避免在 Application 里手写一长串 `get<>()`。

2. **异常处理与归属**
    - 异常类在 **model-dto**，StatusPages 配置在 **server/exception**，合理。若后续在 Service 层引入更多领域异常，建议仍放在共享的 model 或 model-dto 中，server 只做「异常 → HTTP/错误体」的映射。

3. **Service 与 Repository 的边界**
    - Service 做业务校验与编排，Repository 做持久化与查询，边界清晰。当前未见 Service 直接操作 DataSource 或越层调用，层级划分合理。

---

## 四、资深工程师视角总结

### 4.1 总体评价

- **现状**：Server 相关模块在「接口与实现分离」「数据库独立」「DTO/异常共享」上做得不错；server 自身是单体应用，内部按 api/service/auth/di/exception 分层清晰，与 datastore 的 repository-server-api / repository-server / database-server 配合良好。
- **模块划分**：datastore 侧模块划分合理；server 单体在当前规模下可接受，仅需清理历史遗留（TemplatesApi）并同步文档。
- **层级划分**：依赖方向正确，API → Service → Repository → DataSource/DB 清晰；主要改进点在于「路由层如何拿到依赖」（推荐 Koin 集成或 Facade 注入），而非拆更多层。

**外部评审结论（资深 Backend 工程师）**：上述结论站得住脚；文档结构清晰、信息有用、建议可执行。骨架和分层健康，无显著架构性错误，按 P1→P2 落地即可，不必提前拆模块。

### 4.2 建议优先级与落地执行要点

| 优先级    | 建议                                            | 说明                                                                 | 落地执行要点 / 验收标准                                                                                                                         |
|--------|-----------------------------------------------|--------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| **P1** | 删除或明确废弃 `TemplatesApi.kt`                     | 避免与 CardTemplatesApi 混淆，保持代码库干净                                    | 删除文件，或保留文件并加 `@Deprecated` 及注释说明废弃原因；全局搜索无引用后合入                                                                                       |
| **P1** | 更新 `server/docs/myhub-server-architecture.md` | 与当前实现一致                                                            | 文中：Repository 写为在 datastore（repository-server-api / repository-server）；API/Service 命名为 CardTemplates；删除或标注 Statistics 为未实现；推荐结构图与目录一致 |
| **P2** | 路由层依赖注入方式                                     | 用 Koin 插件或 Facade 替代 Application 内大量 `GlobalContext.get().get<>()` | 在 Route 内按需 `get<>()`，或引入 ApiDependencies/Facade 由 Koin 注入；Application.module() 中不再手写一长串 get<>()                                      |
| **P2** | 为 server 补充基础测试                               | 便于后续重构与回归                                                          | **至少 1 条「HTTP → Service/Repository」的集成测试或契约测试**作为回归底线；可选：1～2 个关键 Service 的单元测试                                                        |
| **P3** | 若未来有多入口/多团队需求                                 | 再评估 server 按领域拆子模块                                                 | 当前不执行；仅在有明确多入口/多团队需求时再评估                                                                                                              |

**落地执行顺序**：先完成 P1（清理遗留 + 文档同步），再推进 P2（依赖注入 + 测试）；P3 暂不执行。

**补充（评审中建议纳入）**：

- **入参校验归属**：在架构或本评审中明确「请求体验证」放在哪一层（API / Service / DTO 注解），便于后续统一。
- **异常与可观测性**：若后续做监控/告警，建议在 model-dto 或异常设计上区分「业务异常 vs 系统异常」或稳定错误码，便于日志与指标聚合；当前可只做约定，不做实现。

### 4.3 与「关注度」的呼应

之前基于关注度对 server 投入较少，当前梳理结论是：**骨架和分层是健康的**，没有明显的架构性错误。后续更适合把「关注度」放在：

- 文档与实现一致、清理遗留代码；
- 依赖注入方式与可测试性；
- 补充基础自动化测试；

而不是大规模拆模块或加层级。等业务和团队规模上来后，再按「多入口/多领域」考虑 server 侧子模块划分会更稳妥。

---

## 五、后续可选深化方向（非本次落地范围）

以下维度本次评审未深入，可在 v1.1 或单独文档中按需补充：

| 维度            | 说明                                                                 |
|---------------|--------------------------------------------------------------------|
| **安全与认证**     | JWT 签发/刷新/撤销、过期与黑名单、敏感接口限流/防刷等策略级结论与实现                             |
| **可观测性**      | 除 `/health` 外：日志规范（requestId、userId、错误码）、指标（QPS、延迟、错误率）、tracing 预留 |
| **API 契约与演进** | 版本策略（路径/头/查询参数）、向后兼容原则；对外或多端共用时建议单独成文                              |

---

**文档版本**：v1.1 整合版  
**编写/整合日期**：2026-01-29  
**适用范围**：Server 及 datastore 中与 server 相关的模块（repository-server-api、repository-server、database-server）。

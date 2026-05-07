# MyHub 同步机制（Offline-first + 可配置自动同步）

**方案名称**：MyHub Sync 机制  
**文档版本**：v1.0  
**文档类型**：技术方案设计文档  
**创建日期**：2026-01-20  
**最后更新**：2026-01-23  
**作者**：MyHub Development Team  
**评审状态**：🟢 通过  
**方案状态**：📝 进行中

---

## 📋 文档目录

1. [修改历史](#修改历史)
2. [问题背景](#1-问题背景)
3. [设计目标](#2-设计目标)
4. [技术调研](#3-技术调研)
5. [架构设计](#4-架构设计)
6. [实现细节](#5-实现细节)
7. [实施计划](#6-实施计划)
8. [风险评估](#7-风险评估)
9. [附录](#8-附录)

---

## 📊 方案状态摘要

**当前状态**：

- **评审状态**：🟢 通过（方案已通过评审）
- **方案状态**：📝 进行中（方案进入实施阶段）
- **当前进度**：已完成方案设计，进入工程落地

**状态说明**：

- 评审状态用于标识文档评审进度
- 方案状态用于标识实施进度
- 状态定义请参考 [MyHub 架构设计文档规范](./infra/myhub-infra-rules.md)

---

## 修改历史

| 版本   | 日期         | 修改内容   | 修改原因   |
|------|------------|--------|--------|
| v1.0 | 2026-01-20 | 初稿创建   | 新方案建立  |
| v1.0 | 2026-01-23 | 同步实现更新 | 落地实现更新 |

---

## 1. 问题背景

### 1.1 用户场景

- 用户希望在离线状态下创建/编辑卡片、标签、集合等数据，并在联网时自动同步到云端。
- 多端同时使用同一账号，需要保证数据一致性与冲突可控。
- 用户可在偏好设置中配置同步开关与同步间隔。

### 1.2 问题根因

- 纯在线方案无法满足弱网/无网使用。
- 纯本地方案无法满足多端一致性。
- 数据层目前缺少统一的变更记录、冲突处理与同步调度机制。

### 1.3 影响范围

- `datastore/database`：需要新增同步相关表结构与索引。
- `datastore/datasource-local`：需要写入 Outbox/SyncState。
- `datastore/datasource-remote`：需要提供增量拉取与变更提交接口。
- `datastore/repository`：需要编排同步策略与冲突处理。
- 用户偏好（`user_preferences`）：需要驱动同步配置与调度。

---

## 2. 设计目标

### 2.1 功能目标

- ✅ 支持 Offline-first：离线可写，本地可读
- ✅ 支持增量同步：仅同步变更数据
- ✅ 支持冲突处理：可配置冲突策略（默认 LWW）
- ✅ 支持同步调度：支持手动与前台自动触发
- ✅ 支持多用户：每个用户独立同步状态
- ✅ 支持纯离线模式：用户可关闭同步，仅本地读写

### 2.2 非功能目标

- 性能：同步过程不阻塞 UI 主线程
- 可维护性：同步机制可扩展，支持新表接入
- 跨平台：Android/iOS/Desktop/Web 均可复用
- 可观察性：支持日志与失败重试记录

---

## 3. 技术调研

### 3.1 候选方案

#### 方案 A：直接在线写入 + 失败重试

- 优势：实现简单
- 劣势：离线能力弱；重试难以保证一致性
- 适用场景：对离线要求不高的业务

#### 方案 B：Outbox Pattern + 增量拉取

- 优势：离线可写，变更可追踪，易扩展
- 劣势：需要维护变更队列与同步状态
- 适用场景：Offline-first + 多端一致性

#### 方案 C：基于全量快照同步

- 优势：逻辑直观
- 劣势：流量与性能开销大；冲突处理复杂
- 适用场景：小数据量或低频同步场景

### 3.2 方案对比

| 维度     | 方案 A | 方案 B | 方案 C |
|--------|------|------|------|
| 离线可用性  | ❌    | ✅    | ✅    |
| 增量同步支持 | ❌    | ✅    | ❌    |
| 冲突可控性  | ⚠️   | ✅    | ⚠️   |
| 实现复杂度  | ⭐    | ⭐⭐   | ⭐⭐   |
| 可扩展性   | ⚠️   | ✅    | ⚠️   |
| KMP 适配 | ✅    | ✅    | ✅    |

### 3.3 推荐方案

选择 **方案 B：Outbox Pattern + 增量拉取**。  
理由：兼顾离线能力、变更可追踪与可扩展性，适合多端同步场景。

**官方证据与参考**：

- SQLDelight: https://cashapp.github.io/sqldelight/
- SQLite: https://www.sqlite.org/
- Ktor Client: https://ktor.io/docs/getting-started-ktor-client.html
- Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html

---

## 4. 架构设计

### 4.1 整体架构

```
[UI/Feature]
     |
[Repository]  <---- Sync Scheduler (interval/manual)
     |
[Local DataSource]  <---->  [SQLDelight DB]
     |
[Remote DataSource] <----> [Server API]
```

### 4.2 核心组件

- **Outbox 表**：记录本地变更（INSERT/UPDATE/DELETE）以便推送
- **SyncState 表**：记录每个用户、每个实体的同步游标
- **ConflictLog 表**：记录冲突并提供追溯
- **SyncScheduler**：根据用户偏好与前台触发点执行同步
- **SyncChangeApplier**：按实体类型分发并落地变更（card/tag/collection/template/user/user_preferences）
- **SyncScope/规则配置**：定义哪些数据、哪些用户范围可以进入同步（可参数化过滤）

### 4.3 数据流

1. 本地写入数据 -> 事务内写入业务表 + Outbox（保证顺序）
2. SyncScheduler 触发 -> 读取 Outbox -> 调用 Remote API（失败重试）
3. 远端成功 -> 标记 Outbox 已处理 + 更新 SyncState
4. 拉取增量 -> 合并到本地 -> 记录冲突（如有）
    - 增量过多时按分页拉取
    - 当增量数量超过阈值或游标过旧/缺失时，回退全量拉取并重置游标
5. 未确认的本地变更不推进同步游标（避免跨 checkpoint 冲突）
6. UI 仅读取本地数据库（远端变更只影响本地数据更新）
7. 多端设备通过“增量拉取”实现一致性：B 端在前台触发点拉取 `since=last_sync_token` 的增量并更新本地
8. 关闭同步时仅执行本地读写，不触发上传/拉取
9. 重新开启同步时，优先执行增量拉取；仅当游标缺失/过旧或增量过大时回退全量拉取，再按 Outbox 顺序上传本地变更

### 4.4 状态管理

- 同步状态由 Repository 层统一管理
- UI 通过 `Flow` 订阅同步状态（如同步中/失败/需登录）

### 4.5 自动触发策略

- 仅使用前台触发与前台定时器实现自动同步
- 前台触发点清单：应用启动、前台切换、网络恢复、用户手动触发
- 用户关闭同步时不触发自动同步，可随时手动开启
- 用户重新开启同步时触发一次“恢复同步”（先拉取，再上传）
- 关闭同步期间 Outbox 继续累积，恢复同步时按顺序上传

### 4.6 模块划分

- `sync`：同步领域核心（模型、策略、调度接口、用例）
- `sync-client`：客户端同步实现（Outbox、增量拉取、冲突处理、本地落库）
- `sync-server`：服务端同步实现（变更流、增量接口、校验与幂等处理）

---

## 5. 实现细节

### 5.1 数据表设计（SQLDelight）

**Outbox**

```sql
CREATE TABLE sync_outbox (
  id TEXT PRIMARY KEY NOT NULL,
  user_id TEXT NOT NULL,
  entity_type TEXT NOT NULL,
  entity_id TEXT NOT NULL,
  operation TEXT NOT NULL, -- INSERT/UPDATE/DELETE
  payload TEXT NOT NULL,   -- JSON string
  sequence INTEGER NOT NULL,
  created_at TEXT NOT NULL,
  status TEXT NOT NULL,    -- PENDING/SUCCESS/FAILED
  retry_count INTEGER NOT NULL DEFAULT 0,
  next_retry_at TEXT,
  last_error TEXT
);
CREATE INDEX idx_sync_outbox_user_status ON sync_outbox(user_id, status);
CREATE INDEX idx_sync_outbox_entity ON sync_outbox(entity_type, entity_id);
CREATE UNIQUE INDEX idx_sync_outbox_user_sequence ON sync_outbox(user_id, sequence);
```

**SyncState**

```sql
CREATE TABLE sync_state (
  id TEXT PRIMARY KEY NOT NULL,
  user_id TEXT NOT NULL,
  entity_type TEXT NOT NULL,
  last_sync_at TEXT,
  last_sync_token TEXT
);
CREATE UNIQUE INDEX idx_sync_state_user_entity ON sync_state(user_id, entity_type);
```

**OpLog**

```sql
CREATE TABLE sync_oplog (
  id TEXT PRIMARY KEY NOT NULL,
  user_id TEXT NOT NULL,
  entity_type TEXT NOT NULL,
  entity_id TEXT NOT NULL,
  operation TEXT NOT NULL, -- INSERT/UPDATE/DELETE
  payload TEXT NOT NULL,
  created_at TEXT NOT NULL
);
CREATE INDEX idx_sync_oplog_user_entity ON sync_oplog(user_id, entity_type);
```

**ConflictLog**

```sql
CREATE TABLE sync_conflict_log (
  id TEXT PRIMARY KEY NOT NULL,
  user_id TEXT NOT NULL,
  entity_type TEXT NOT NULL,
  entity_id TEXT NOT NULL,
  local_payload TEXT NOT NULL,
  remote_payload TEXT NOT NULL,
  resolved_strategy TEXT NOT NULL, -- LWW/MANUAL/MERGE
  resolved_at TEXT NOT NULL
);
CREATE INDEX idx_sync_conflict_user ON sync_conflict_log(user_id);
```

### 5.2 关键 API 设计

```kotlin
package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow

enum class SyncStatus { IDLE, RUNNING, FAILED }

interface SyncRepository {
    fun observeSyncStatus(userId: String): Flow<SyncStatus>
    suspend fun requestSync(userId: String, trigger: SyncTrigger)
    fun startAutoSync(userId: String)
    fun stopAutoSync(userId: String)
}
```

```kotlin
package tech.zhifu.app.myhub.datastore.repository

interface SyncChangeApplier {
    suspend fun applyChanges(
        entity: SyncEntityType,
        operations: SyncOperations,
        change: SyncPullChange
    )
}
```

```kotlin
package tech.zhifu.app.myhub.datastore.datasource

import kotlinx.coroutines.flow.Flow

interface LocalSyncDataSource {
    fun observePendingOutboxByUserId(userId: String): Flow<List<Sync_outbox>>
}
```

### 5.3 同步调度伪代码

```kotlin
package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SyncScheduler(
    private val syncRepository: SyncRepository,
    private val userId: String,
    private val interval: Duration
) {
    suspend fun runLoop() {
        while (true) {
            syncRepository.requestSync(userId, SyncTrigger.TIMER)
            delay(interval)
        }
    }
}
```

### 5.4 冲突策略

- 默认：LWW（以 `updated_at` 为准）
- 记录冲突：写入 `sync_conflict_log`
- 可扩展：为关键实体支持人工合并策略
- 同步游标推进采用“服务端权威 + 本地确认后推进”的策略

### 5.5 自动触发与定时策略

- 仅使用前台触发与前台定时器实现“自动同步”
- 关闭同步时仅保留本地读写，不进行上传/拉取
- 重新开启同步时触发一次“恢复同步”流程

### 5.6 依赖配置

- 数据层：SQLDelight（现有）
- 网络：Ktor Client（现有）
- 协程：kotlinx-coroutines（现有）

---

## 6. 实施计划

### 6.1 当前进度

**方案状态**：📝 进行中  
**状态说明**：已完成核心设计与表结构定义，待评审并进入实现阶段。

### 6.2 阶段划分

- 阶段 1：同步表结构落地（✅ 已完成设计 / ⏸️ 待实现）
- 阶段 2：Local Outbox 写入与读取（⏸️ 待开始）
- 阶段 3：Remote 增量接口对接（⏸️ 待开始）
- 阶段 4：冲突处理与日志（⏸️ 待开始）
- 阶段 5：前台定时器与触发点接入（⏸️ 待开始）

### 6.3 里程碑

- 里程碑 1：同步基础可用（Outbox + 拉取）
- 里程碑 2：多端一致性与冲突处理
- 里程碑 3：自动调度与用户配置

### 6.4 回滚策略

- 回滚条件：同步错误率持续升高或数据一致性异常
- 回滚步骤：关闭自动同步开关，保留本地数据，仅允许手动同步
- 数据迁移：保留 `sync_*` 表，后续恢复时可继续使用

---

## 7. 风险评估

### 7.1 技术风险

- **冲突处理复杂度**：多端并发修改可能导致冲突 → 记录冲突并支持人工处理
- **同步失败重试**：网络异常导致重复请求 → 使用幂等接口 + 退避重试
- **性能风险**：Outbox 堆积导致同步变慢 → 分批上传 + 状态索引
- **维护成本**：同步策略迭代复杂 → 模块化拆分与清晰接口

### 7.2 边界条件

- 功能边界：不处理跨账号合并，仅支持单账号多端同步
- 性能边界：默认单次同步批量上限 200 条（可配置）
- 上传边界：Outbox 上传批量阈值默认 200 条，超出分批上传
- 归档边界：Outbox 成功项保留 7 天后清理（可配置）
- 日志边界：OpLog 保留 30 天后清理（可配置）
- 同步边界：增量数量阈值默认 500 条，超过则回退全量拉取
- 兼容性边界：与旧版本客户端不兼容时需服务端版本隔离
- 查询边界：同步过滤规则不建议使用复杂 join，复杂关系由服务端预处理视图或冗余表解决

---

## 8. 附录

### 8.1 相关文档

- [MyHub 基础设施规则](./infra/myhub-infra-rules.md)
- [数据存储套件总体架构设计](../datastore/docs/myhub-datastore-infra-v1.0.md)
- [数据库模块方案设计](../datastore/database/docs/myhub-datastore-database-infra-v1.0.md)

### 8.2 参考资料

- Outbox Pattern: https://microservices.io/patterns/data/transactional-outbox.html
- SQLite: https://www.sqlite.org/
- SQLDelight: https://cashapp.github.io/sqldelight/
- PowerSync: https://www.powersync.com/

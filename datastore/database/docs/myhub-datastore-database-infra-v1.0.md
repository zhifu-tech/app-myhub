# MyHub 数据库模块方案设计 v1.0

**方案名称**：Datastore Database Infra v1.0  
**文档版本**：v1.0  
**文档类型**：技术方案设计文档  
**创建日期**：2026-05-06  
**最后更新**：2026-05-06  
**作者**：MyHub Development Team  
**评审状态**：🟢 通过  
**方案状态**：🔒 已锁定

**依据文档**：本方案依据当前 `datastore/database` 的实际 Schema 与代码实现整理，不保留历史兼容叙事。

---

## 📋 文档目录

1. [修改历史](#修改历史)
2. [方案状态摘要](#-方案状态摘要)
3. [问题背景](#1-问题背景)
4. [设计目标](#2-设计目标)
5. [技术调研](#3-技术调研)
6. [架构设计](#4-架构设计)
7. [实现细节](#5-实现细节)
8. [实施计划](#6-实施计划)
9. [风险评估](#7-风险评估)
10. [附录](#8-附录)

---

## 📊 方案状态摘要

**当前状态**：

- **评审状态**：🟢 通过
- **方案状态**：🔒 已锁定
- **当前版本**：`v1.0`
- **当前结论**：`datastore/database` 是 MyHub 的本地数据库基础设施模块，负责 Schema、查询、索引和版本管理

**状态说明**：

- **评审状态**：用于标识文档的评审进度
    - 🟢 通过：文档已通过评审，可以进入实施阶段
    - 🟡 待评审：文档正在等待评审或评审进行中
    - 🔴 需修改：文档评审后需要修改
- **方案状态**：用于标识方案的实施进度
    - 🔒 已锁定：方案设计已确定，不允许随意修改
    - 📝 进行中：方案设计正在进行中，可以修改
    - ⏸️ 暂停：方案设计暂时停止，保留当前状态

---

## 修改历史

| 版本   | 日期         | 修改内容   | 修改原因          |
|------|------------|--------|---------------|
| v1.0 | 2026-05-06 | 初始方案设计 | 基于当前数据库现状重建文档 |

---

## 1. 问题背景

### 1.1 用户场景

- MyHub 需要一套统一的本地数据库基础设施
- 应用需要在多平台共享同一套 Schema 与查询定义
- 业务模块需要稳定的表结构承载用户、卡片、草稿、媒体和同步数据
- 数据库设计需要明确职责边界，避免把业务编排混入 Schema 模块

### 1.2 问题根因

- 如果 Schema、索引、查询定义分散在业务模块中，会导致维护成本升高
- 如果数据库表职责不清晰，会增加扩展和迁移风险
- 如果本地存储策略没有收口，平台行为会不一致

### 1.3 影响范围

- 本地数据库结构
- SQLDelight `.sq` 文件
- 数据库版本号
- 数据访问调用方
- 平台私有存储路径

---

## 2. 设计目标

### 2.1 功能目标

- 统一承载 MyHub 的本地数据库 Schema
- 提供一致的表结构和查询定义
- 支持卡片、草稿、媒体、AI 任务和同步基础设施
- 保持当前版本号和结构可追踪

### 2.2 非功能目标

- 保持 Schema 清晰可维护
- 保持跨平台一致性
- 避免业务逻辑侵入数据库模块
- 让表职责与领域模型尽量一致

### 2.3 模块特性说明

- `datastore/database` 只负责数据库基础设施
- 不负责平台驱动创建
- 不负责业务编排
- 不负责 UI 状态管理

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 SQLDelight + SQLite

**选择理由**：

- KMP 统一 Schema 管理
- 类型安全的查询生成
- 平台表现稳定

#### 3.1.2 版本管理方式

**选择理由**：

- 使用 SQLDelight 的 Schema 版本管理
- 通过版本号与迁移逻辑保持表结构演进

---

## 4. 架构设计

### 4.1 模块结构

```text
datastore/database/
├── README.md
├── docs/
│   └── myhub-datastore-database-infra-v1.0.md
├── src/
│   └── commonMain/
│       └── sqldelight/
│           └── tech/zhifu/app/myhub/datastore/database/
│               ├── user.sq
│               ├── user_preferences.sq
│               ├── card.sq
│               ├── user_card.sq
│               ├── draft_session.sq
│               ├── bookkeeper.sq
│               ├── media_asset.sq
│               ├── ai_job.sq
│               ├── sync_state.sq
│               ├── sync_outbox.sq
│               ├── sync_oplog.sq
│               └── sync_conflict_log.sq
└── build.gradle.kts
```

### 4.2 核心组件

#### 4.2.1 user 表

- 用户主表
- 承载用户身份和基础信息

#### 4.2.2 user_preferences 表

- 用户偏好设置
- 承载界面和行为偏好

#### 4.2.3 card 表

- 卡片事实表
- 承载核心业务对象

#### 4.2.4 user_card 表

- 用户与卡片关系表
- 承载用户视角下的主观关系

#### 4.2.5 draft_session 表

- 草稿会话恢复
- 承载草稿恢复相关状态

#### 4.2.6 bookkeeper 表

- 本地失败记录
- 承载重试或回溯需要的持久化状态

#### 4.2.7 media_asset 表

- 媒体资产索引
- 承载本地媒体与资源引用

#### 4.2.8 ai_job 表

- AI 任务记录
- 承载 AI 处理任务状态

#### 4.2.9 同步基础设施表

- `sync_state`
- `sync_outbox`
- `sync_oplog`
- `sync_conflict_log`

---

## 5. 实现细节

### 5.1 当前数据库版本

- 数据库名：`MyHubDatabase`
- 版本号：`1`
- Schema 位置：`src/commonMain/sqldelight/tech/zhifu/app/myhub/datastore/database`

### 5.2 表清单

#### 账号与偏好

- `user`
- `user_preferences`

#### 卡片与草稿

- `card`
- `user_card`
- `draft_session`
- `bookkeeper`

#### 媒体与 AI

- `media_asset`
- `ai_job`

#### 同步基础设施

- `sync_state`
- `sync_outbox`
- `sync_oplog`
- `sync_conflict_log`

### 5.3 设计约束

- `Card` 是事实
- `Collection` 是结构
- `Tag` 是语义
- `User` 是视角
- `user_card` 是用户对内容的主观关系
- `user_collection` 是用户在集合中的权限关系
- `card_tag` 是内容与用户语义的附着关系
- 派生统计表可以删、可以重建，不参与核心业务判断

### 5.4 存储策略

- Android / JVM / Desktop 通常使用 `FileKit.filesDir/app-data`
- iOS / macOS Designed for iPad 使用**应用沙盒容器**
- 数据库文件、草稿和媒体文件都属于**本地私有存储**

### 5.5 维护方式

- 新增或修改表结构时，必须同步更新：
    - `.sq` 文件
    - `build.gradle.kts` 版本号
    - 调用方代码
- 所有 Schema 和查询都通过 SQLDelight 管理
- 当前模块不包含业务编排逻辑

---

## 6. 实施计划

### 6.1 当前状态

- 当前 Schema 已稳定
- 当前文档已与代码对齐

### 6.2 后续调整原则

- 优先更新 `.sq` 文件
- 再同步版本号
- 再同步调用方
- 最后同步文档

---

## 7. 风险评估

### 7.1 技术风险

- Schema 变更可能影响现有数据
- 平台存储路径差异可能造成调试混乱

### 7.2 风险缓解

- 通过版本号和迁移逻辑管理变化
- 统一平台私有存储策略

### 7.3 边界条件

- 不在数据库模块处理驱动创建
- 不在数据库模块处理业务编排

---

## 8. 附录

### 8.1 相关文档

- [数据库模块入口](../README.md)
- [MyHub 数据存储套件概览](../../README.md)

### 8.2 代码入口

- `src/commonMain/sqldelight/tech/zhifu/app/myhub/datastore/database`

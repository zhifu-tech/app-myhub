# MyHub 数据库模块方案设计

**方案名称**：Datastore Database Infra v1  
**文档版本**：v1.0  
**文档类型**：技术方案设计文档  
**创建日期**：2026-01-20  
**锁定日期**：2026-01-20  
**最后更新**：2026-01-20  
**作者**：MyHub Development Team  
**评审状态**：🟢 通过  
**方案状态**：🔒 已锁定

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

- **评审状态**：🟢 通过（文档已通过评审，可以进入实施阶段）
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Datastore Database Infra v1 的基线设计
- **锁定日期**：2026-01-20
- **当前进度**：Schema 与查询已实现，作为 v1.0 基线设计

**状态说明**：

- **评审状态**：用于标识文档的评审进度
    - 🟢 通过：文档已通过评审，可以进入实施阶段
    - 🟡 待评审：文档正在等待评审或评审进行中
    - 🔴 需修改：文档评审后需要修改
- **方案状态**：用于标识方案的实施进度
    - 🔒 已锁定：方案设计已确定，不允许随意修改
    - 📝 进行中：方案设计正在进行中，可以修改
    - ⏸️ 暂停：方案设计暂时停止，保留当前状态
- 详细状态定义请参考 [MyHub 架构设计文档规范](../../../docs/infra/myhub-infra-rules.md)

---

## 修改历史

| 版本   | 日期         | 修改内容   | 修改原因 |
|------|------------|--------|------|
| v1.0 | 2026-01-20 | 全新方案设计 | 新建   |

---

## 1. 问题背景

### 1.1 用户场景

MyHub 进入新阶段后，数据库需要完全按照当前的领域模型与建模原则重建，确保：

1. **事实与主观分离**：Card 作为事实，不承载用户主观状态
2. **关系驱动**：User × Card、User × Collection 等关系作为独立表表达
3. **类型扩展**：Card 类型差异通过 Metadata 表扩展
4. **跨平台一致**：KMP 多平台使用统一 Schema
5. **类型安全**：使用 SQLDelight 生成类型安全查询

### 1.2 问题根因

旧 Schema 将事实、主观、派生混合在主表中，导致：

1. 数据语义混乱，难以演进
2. 用户状态与事实耦合，难以扩展多用户与协作
3. 类型字段堆叠，导致主表复杂且难维护

### 1.3 影响范围

- **产品一致性**：模型与产品概念不一致
- **演进成本**：Schema 变更频繁且风险高
- **多端一致性**：Schema 难以跨平台统一

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **事实模型稳定**：Card 表只保留最小事实字段
- ✅ **主观关系独立**：User × Card / User × Collection 关系独立建表
- ✅ **类型语义扩展**：每种 Card 类型独立 Metadata 表
- ✅ **结构与权限分离**：Collection 的 owner 为事实，权限在 user_collection
- ✅ **索引齐全**：每个表提供最基础索引
- ✅ **CRUD 完整**：每个表至少提供基础 CRUD 操作

### 2.2 非功能目标

- ✅ **跨平台一致**：所有平台共用 SQLDelight Schema
- ✅ **可迁移**：版本号清晰，迁移成本低
- ✅ **可维护**：结构清晰、语义可读

### 2.3 模块特性说明

`datastore/database` 仅负责 **Schema、索引与查询定义**，不包含业务逻辑或平台驱动实现。

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 数据库 Schema 管理：SQLDelight

- ✅ KMP 原生支持
- ✅ 生成类型安全的 Kotlin API
- ✅ 可维护的版本管理与迁移

#### 3.1.2 数据库引擎：SQLite

- ✅ 所有平台可用
- ✅ 轻量稳定，适合本地存储

---

## 4. 架构设计

### 4.1 模块结构

```text
datastore/database/
├── src/
│   └── commonMain/
│       └── sqldelight/
│           └── tech/zhifu/app/myhub/datastore/database/
│               ├── card.sq
│               ├── card_metadata_article.sq
│               ├── card_metadata_code.sq
│               ├── card_metadata_idea.sq
│               ├── card_metadata_quote.sq
│               ├── card_metadata_todo.sq
│               ├── card_metadata_word.sq
│               ├── card_tag.sq
│               ├── card_template.sq
│               ├── collection.sq
│               ├── tag.sq
│               ├── user.sq
│               ├── user_card.sq
│               ├── user_card_type_statistics.sq
│               ├── user_collection.sq
│               ├── user_preferences.sq
│               └── user_statistics.sq
└── build.gradle.kts
```

### 4.2 数据库 Schema

#### 4.2.1 核心事实与关系表

**user 表**：

```sql
CREATE TABLE user (
    id TEXT PRIMARY KEY NOT NULL,
    username TEXT NOT NULL UNIQUE,
    display_name TEXT,
    avatar_url TEXT,
    avatar_text TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    status TEXT,
    last_login_at TEXT
);
```

**user_preferences 表**：

```sql
CREATE TABLE user_preferences (
    user_id TEXT PRIMARY KEY NOT NULL,
    theme TEXT NOT NULL DEFAULT 'dark',
    language TEXT NOT NULL DEFAULT 'en',
    default_card_type TEXT,
    auto_sync INTEGER NOT NULL DEFAULT 1,
    sync_interval INTEGER NOT NULL DEFAULT 3600000,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
```

**card 表**：

```sql
CREATE TABLE card (
    id TEXT PRIMARY KEY NOT NULL,
    type TEXT NOT NULL,
    title TEXT,
    content TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);
```

**user_card 表（主观关系）**：

```sql
CREATE TABLE user_card (
    user_id TEXT NOT NULL,
    card_id TEXT NOT NULL,
    is_favorite INTEGER NOT NULL DEFAULT 0,
    last_reviewed_at TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    PRIMARY KEY (user_id, card_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**collection 表（结构事实）**：

```sql
CREATE TABLE collection (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    topic TEXT,
    description TEXT,
    user_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
```

**user_collection 表（权限关系）**：

```sql
CREATE TABLE user_collection (
    user_id TEXT NOT NULL,
    collection_id TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'owner',
    created_at TEXT NOT NULL,
    PRIMARY KEY (user_id, collection_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (collection_id) REFERENCES collection(id) ON DELETE CASCADE
);
```

**tag 表**：

```sql
CREATE TABLE tag (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    color TEXT,
    description TEXT,
    user_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    card_count INTEGER NOT NULL DEFAULT 0,
    UNIQUE(name, user_id)
);
```

**card_tag 表（多对多）**：

```sql
CREATE TABLE card_tag (
    card_id TEXT NOT NULL,
    tag_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    PRIMARY KEY (card_id, tag_id),
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);
```

**card_template 表**：

```sql
CREATE TABLE card_template (
    id TEXT PRIMARY KEY NOT NULL,
    type TEXT NOT NULL,
    title TEXT,
    content TEXT,
    description TEXT,
    created_at TEXT NOT NULL
);
```

#### 4.2.2 Metadata 表（按类型拆分）

**card_metadata_article**：

```sql
CREATE TABLE card_metadata_article (
    card_id TEXT PRIMARY KEY NOT NULL,
    url TEXT NOT NULL,
    summary TEXT,
    cover_image_url TEXT,
    author TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_code**：

```sql
CREATE TABLE card_metadata_code (
    card_id TEXT PRIMARY KEY NOT NULL,
    language TEXT NOT NULL,
    snippet TEXT NOT NULL,
    description TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_idea**：

```sql
CREATE TABLE card_metadata_idea (
    card_id TEXT PRIMARY KEY NOT NULL,
    priority TEXT,
    status TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_quote**：

```sql
CREATE TABLE card_metadata_quote (
    card_id TEXT PRIMARY KEY NOT NULL,
    author TEXT,
    category TEXT,
    source TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_todo**：

```sql
CREATE TABLE card_metadata_todo (
    card_id TEXT PRIMARY KEY NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending',
    priority TEXT,
    due_at TEXT,
    completed_at TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_word**：

```sql
CREATE TABLE card_metadata_word (
    card_id TEXT PRIMARY KEY NOT NULL,
    pronunciation TEXT,
    definition TEXT NOT NULL,
    example TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

#### 4.2.3 派生与统计表

**user_statistics 表**：

```sql
CREATE TABLE user_statistics (
    user_id TEXT PRIMARY KEY NOT NULL,
    total_cards INTEGER NOT NULL DEFAULT 0,
    favorite_cards INTEGER NOT NULL DEFAULT 0,
    recent_edits INTEGER NOT NULL DEFAULT 0,
    last_sync_at TEXT,
    updated_at TEXT NOT NULL
);
```

**user_card_type_statistics 表**：

```sql
CREATE TABLE user_card_type_statistics (
    card_type TEXT NOT NULL,
    count INTEGER NOT NULL DEFAULT 0,
    user_id TEXT NOT NULL DEFAULT '',
    PRIMARY KEY (user_id, card_type)
);
```

#### 4.2.4 索引设计

索引原则：每个表至少提供 **主键/高频过滤字段** 的索引，包含但不限于：

```sql
-- card
CREATE INDEX idx_card_type ON card(type);
CREATE INDEX idx_card_created_at ON card(created_at);
CREATE INDEX idx_card_updated_at ON card(updated_at);

-- user_card
CREATE INDEX idx_user_card_user_id ON user_card(user_id);
CREATE INDEX idx_user_card_card_id ON user_card(card_id);
CREATE INDEX idx_user_card_updated_at ON user_card(user_id, updated_at);

-- collection
CREATE INDEX idx_collection_user_id ON collection(user_id);
CREATE INDEX idx_collection_user_name ON collection(user_id, name);

-- tag
CREATE INDEX idx_tag_user_id ON tag(user_id);
CREATE INDEX idx_tag_user_name ON tag(user_id, name);

-- card_tag
CREATE INDEX idx_card_tag_card_id ON card_tag(card_id);
CREATE INDEX idx_card_tag_tag_id ON card_tag(tag_id);

-- metadata
CREATE INDEX idx_card_metadata_article_url ON card_metadata_article(url);
CREATE INDEX idx_card_metadata_code_language ON card_metadata_code(language);
CREATE INDEX idx_card_metadata_idea_status ON card_metadata_idea(status);
CREATE INDEX idx_card_metadata_todo_status ON card_metadata_todo(status);
```

### 4.3 版本管理

#### 4.3.1 版本历史

- **v1.0**：当前基线版本，完整 Schema 与 CRUD 定义

#### 4.3.2 迁移机制

v1.0 为全新基线，不承载历史迁移。后续版本通过 `.sqm` 文件进行增量迁移。

### 4.4 查询接口

每个表提供基础 CRUD，命名统一为：

```sql
-- select / insert / update / delete
selectCardById:
SELECT * FROM card WHERE id = ?;

insertCard:
INSERT OR REPLACE INTO card (id, type, title, content, created_at, updated_at)
VALUES (?, ?, ?, ?, ?, ?);
```

关系表与统计表提供按主键与外键的查询与删除，例如：

```sql
selectUserCardById:
SELECT * FROM user_card WHERE user_id = ? AND card_id = ?;

deleteUserCardsByUserId:
DELETE FROM user_card WHERE user_id = ?;
```

---

## 5. 实现细节

### 5.1 SQLDelight 配置

```kotlin
sqldelight {
    databases {
        create("MyHubDatabase") {
            packageName.set("tech.zhifu.app.myhub.datastore.database")
            generateAsync.set(true)
            version = 1
        }
    }
    linkSqlite = true
}
```

### 5.2 版本管理配置

```kotlin
sqldelight {
    databases {
        create("MyHubDatabase") {
            version = 1  // v1.0 基线
        }
    }
}
```

### 5.3 领域原则落地说明

- **Card 是事实**：Card 表不包含任何用户主观字段
- **User 是视角**：主观状态统一放在 user_card
- **Collection 是结构**：结构事实在 collection，权限在 user_collection
- **Tag 是语义**：Tag 用户私有，跨用户不共享语义

---

## 6. 实施计划

### 6.1 实施阶段

#### 阶段 1：Schema 定义（已完成）

- ✅ 事实表、关系表、元数据表、统计表

#### 阶段 2：索引与 CRUD（已完成）

- ✅ 所有表补齐索引
- ✅ 所有表提供基础 CRUD

### 6.2 里程碑

| 里程碑              | 目标日期       | 状态    |
|------------------|------------|-------|
| v1.0 Schema 完成   | 2026-01-20 | ✅ 已完成 |
| v1.0 索引与 CRUD 完成 | 2026-01-20 | ✅ 已完成 |

---

## 7. 风险评估

### 7.1 技术风险

#### 7.1.1 迁移风险

**风险描述**：后续版本增量迁移可能导致结构兼容问题  
**影响**：中  
**缓解措施**：小步迁移、自动化测试、保持向后兼容

### 7.2 维护风险

#### 7.2.1 语义漂移风险

**风险描述**：开发中绕过建模原则添加字段导致语义漂移  
**影响**：中  
**缓解措施**：文档约束、评审机制、强制 Schema 审核

---

## 8. 附录

### 8.1 相关文档

- [MyHub 领域模型图（Card / Collection / User / Tag）](./myhub_领域模型图（card_collection_user_tag）!!!.md)
- [MyHub 数据建模原则 v1](./myhub_数据建模原则_v1.0.md)
- [SQLDelight 官方文档](https://cashapp.github.io/sqldelight/)

### 8.2 术语表

| 术语         | 说明                                   |
|------------|--------------------------------------|
| Schema     | 数据库表结构定义                             |
| Metadata   | 按 Card 类型拆分的语义扩展表                    |
| 关系表        | 用于表达 User × Card / User × Collection |
| 派生表        | 可重建统计表，不作为事实来源                       |
| 迁移文件（.sqm） | SQLDelight 版本迁移脚本                    |
| 查询文件（.sq）  | SQLDelight 查询与表结构定义文件                |

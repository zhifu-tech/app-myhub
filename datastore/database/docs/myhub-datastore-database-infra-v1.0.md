# MyHub 数据库模块方案设计

**方案名称**：Datastore Database Infra v1  
**文档版本**：v1.0  
**文档类型**：技术方案设计文档  
**创建日期**：2026-01-13  
**锁定日期**：2026-01-13  
**最后更新**：2026-01-13  
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
- **锁定日期**：2026-01-13
- **当前进度**：所有阶段已完成，方案设计已确定并锁定

**状态说明**：

- **评审状态**：用于标识文档的评审进度
    - 🟢 通过：文档已通过评审，可以进入实施阶段
    - 🟡 待评审：文档正在等待评审或评审进行中
    - 🔴 需修改：文档评审后需要修改
- **方案状态**：用于标识方案的实施进度
    - 🔒 已锁定：方案设计已确定，不允许随意修改
    - 📝 进行中：方案设计正在进行中，可以修改
    - ⏸️ 暂停：方案设计暂时停止，保留当前状态
- 详细状态定义请参考 [MyHub 架构设计文档规范](../../../docs/myhub-infra-rules.md)

---

## 修改历史

| 版本   | 日期         | 修改内容            | 修改原因      |
|------|------------|-----------------|-----------|
| v1.0 | 2026-01-13 | 初始方案设计          | 新建        |
| v1.0 | 2026-01-13 | 完成架构设计文档        | 完善文档      |
| v1.0 | 2026-01-13 | 更新状态：评审通过、方案已锁定 | 状态更新：评审通过 |

---

## 1. 问题背景

### 1.1 用户场景

在 MyHub 应用的开发和运行过程中，需要处理数据库 Schema 的定义、版本管理和迁移。典型的场景包括：

1. **数据库 Schema 定义**：应用需要定义数据库表结构（如 Card、Tag、Template、User 等），这些表结构需要在客户端和服务端保持一致
2. **版本管理**：数据库 Schema 需要版本化管理，支持从旧版本升级到新版本
3. **数据迁移**：当 Schema 变更时，需要将现有数据迁移到新结构
4. **跨平台兼容**：在 KMP 项目中，数据库 Schema 需要在所有平台（Android、iOS、JVM、JS、WASM）上保持一致
5. **类型安全查询**：需要提供类型安全的 SQL 查询接口，避免运行时错误

### 1.2 问题根因

在引入统一的数据库 Schema 模块之前，MyHub 应用面临以下问题：

1. **Schema 定义分散**：客户端和服务端可能使用不同的 Schema 定义，导致不一致
2. **版本管理缺失**：缺乏统一的版本管理机制，难以追踪 Schema 变更历史
3. **迁移机制不完善**：数据迁移逻辑分散，容易出错
4. **类型安全问题**：直接使用字符串 SQL 查询，缺乏类型安全
5. **跨平台兼容性**：不同平台使用不同的数据库实现，Schema 定义难以统一

### 1.3 影响范围

- **数据一致性**：Schema 不一致导致数据同步问题
- **版本升级**：缺乏版本管理导致升级困难
- **开发效率**：缺乏类型安全的查询接口降低开发效率
- **维护成本**：Schema 变更需要手动同步客户端和服务端

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **统一的 Schema 定义**：使用 SQLDelight 定义数据库 Schema，客户端和服务端共享
- ✅ **版本管理**：支持数据库版本管理和自动迁移
- ✅ **类型安全查询**：提供类型安全的 SQL 查询接口
- ✅ **跨平台支持**：支持所有平台（Android、iOS、JVM、JS、WASM）
- ✅ **用户关联**：支持多用户数据隔离（版本 2）
- ✅ **性能优化**：提供索引优化查询性能

### 2.2 非功能目标

- ✅ **代码复用**：Schema 定义在客户端和服务端复用
- ✅ **易于维护**：Schema 变更易于追踪和维护
- ✅ **向后兼容**：支持从旧版本平滑升级
- ✅ **性能优化**：索引优化查询性能

### 2.3 模块特性说明

**重要说明**：`datastore/database` 模块是一个**单一功能模块**，专注于数据库 Schema 的定义和版本管理，不包含业务逻辑或平台特定实现。

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 数据库 Schema 管理：SQLDelight

**选择理由**：

- ✅ **KMP 原生支持**：SQLDelight 是 Square 提供的 SQL 代码生成工具，完全支持 KMP
- ✅ **类型安全**：编译时生成类型安全的 Kotlin 代码，避免运行时错误
- ✅ **跨平台支持**：支持 Android、iOS、JVM、JS、WASM 等多个平台
- ✅ **版本管理**：内置版本管理和迁移机制
- ✅ **代码生成**：自动生成类型安全的查询接口

**替代方案对比**：

| 方案         | 优点                 | 缺点                 | 结论       |
|------------|--------------------|--------------------|----------|
| SQLDelight | KMP 原生支持，类型安全，版本管理 | 需要学习 SQLDelight 语法 | ✅ **选择** |
| Room       | Android 官方支持       | 仅支持 Android        | ❌        |
| Realm      | 跨平台支持              | 商业许可，学习曲线陡         | ❌        |
| Exposed    | Kotlin DSL         | 不支持 KMP            | ❌        |

#### 3.1.2 数据库引擎：SQLite

**选择理由**：

- ✅ **跨平台支持**：SQLite 在所有平台上都有实现
- ✅ **轻量级**：SQLite 是轻量级数据库，适合移动应用
- ✅ **成熟稳定**：SQLite 是成熟稳定的数据库引擎
- ✅ **SQLDelight 支持**：SQLDelight 原生支持 SQLite

**平台实现**：

- **Android**：使用 Android 内置 SQLite
- **iOS**：使用 SQLite3（通过 SQLDelight）
- **JVM**：使用 SQLite JDBC
- **JS/WASM**：使用 SQL.js（WebAssembly）

### 3.2 架构模式

#### 3.2.1 Schema 版本管理

**设计原则**：

- **版本号**：使用整数版本号（1、2、3...）
- **迁移文件**：每个版本对应一个 `.sqm` 迁移文件
- **自动迁移**：SQLDelight 自动执行迁移脚本

**优势**：

- ✅ **版本追踪**：清晰的版本历史
- ✅ **自动迁移**：无需手动执行迁移脚本
- ✅ **向后兼容**：支持从任意版本升级

---

## 4. 架构设计

### 4.1 模块结构

```
datastore/database/
├── src/
│   └── commonMain/
│       └── sqldelight/
│           └── tech/zhifu/app/myhub/datastore/database/
│               ├── Card.sq          # 卡片相关表定义
│               ├── Tag.sq           # 标签表定义
│               ├── Template.sq      # 模板表定义
│               ├── User.sq          # 用户相关表定义
│               ├── Statistics.sq    # 统计信息表定义
│               ├── 1.sqm            # 版本 1 迁移文件
│               └── 2.sqm            # 版本 2 迁移文件
└── build.gradle.kts
```

### 4.2 数据库 Schema

#### 4.2.1 核心表结构

**user 表**：

```sql
CREATE TABLE user (
    id TEXT PRIMARY KEY NOT NULL,
    username TEXT NOT NULL UNIQUE,
    email TEXT,
    display_name TEXT,
    avatar_url TEXT,
    created_at TEXT NOT NULL
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
    author TEXT,
    source TEXT,
    language TEXT,
    is_favorite INTEGER NOT NULL DEFAULT 0,
    is_template INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    last_reviewed_at TEXT,
    user_id TEXT NOT NULL DEFAULT ''
);
```

**card_tag 表**（多对多关系）：

```sql
CREATE TABLE card_tag (
    card_id TEXT NOT NULL,
    tag_name TEXT NOT NULL,
    user_id TEXT NOT NULL DEFAULT '',
    PRIMARY KEY (card_id, tag_name),
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata 表**：

```sql
CREATE TABLE card_metadata (
    card_id TEXT PRIMARY KEY NOT NULL,
    quote_author TEXT,
    quote_category TEXT,
    code_language TEXT,
    code_snippet TEXT,
    article_url TEXT,
    article_summary TEXT,
    article_image_url TEXT,
    word_pronunciation TEXT,
    word_definition TEXT,
    word_example TEXT,
    idea_priority TEXT,
    idea_status TEXT,
    user_id TEXT NOT NULL DEFAULT '',
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**checklist_item 表**：

```sql
CREATE TABLE checklist_item (
    id TEXT PRIMARY KEY NOT NULL,
    card_id TEXT NOT NULL,
    text TEXT NOT NULL,
    is_completed INTEGER NOT NULL DEFAULT 0,
    item_order INTEGER NOT NULL DEFAULT 0,
    user_id TEXT NOT NULL DEFAULT '',
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**tag 表**：

```sql
CREATE TABLE tag (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    color TEXT,
    description TEXT,
    card_count INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    user_id TEXT NOT NULL DEFAULT '',
    UNIQUE(name, user_id)
);
```

**template 表**：

```sql
CREATE TABLE template (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    card_type TEXT NOT NULL,
    default_content TEXT,
    is_system_template INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    user_id TEXT NOT NULL DEFAULT ''
);
```

**statistics 表**：

```sql
CREATE TABLE statistics (
    user_id TEXT PRIMARY KEY NOT NULL,
    total_cards INTEGER NOT NULL DEFAULT 0,
    favorite_cards INTEGER NOT NULL DEFAULT 0,
    recent_edits INTEGER NOT NULL DEFAULT 0
);
```

**card_type_statistics 表**：

```sql
CREATE TABLE card_type_statistics (
    user_id TEXT NOT NULL,
    card_type TEXT NOT NULL,
    count INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, card_type)
);
```

**tag_statistics 表**：

```sql
CREATE TABLE tag_statistics (
    user_id TEXT NOT NULL,
    tag_name TEXT NOT NULL,
    count INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, tag_name)
);
```

#### 4.2.2 索引设计

**性能优化索引**：

```sql
-- Card 表索引
CREATE INDEX card_user_id_index ON card(user_id);
CREATE INDEX card_user_type_index ON card(user_id, type);
CREATE INDEX card_user_favorite_index ON card(user_id, is_favorite);
CREATE INDEX card_user_updated_index ON card(user_id, updated_at);

-- Tag 表索引
CREATE INDEX tag_user_id_index ON tag(user_id);
CREATE INDEX tag_user_name_index ON tag(user_id, name);

-- Template 表索引
CREATE INDEX template_user_id_index ON template(user_id);
CREATE INDEX template_user_type_index ON template(user_id, card_type);

-- 关联表索引
CREATE INDEX card_tag_user_id_index ON card_tag(user_id);
CREATE INDEX card_metadata_user_id_index ON card_metadata(user_id);
CREATE INDEX checklist_item_user_id_index ON checklist_item(user_id);

-- 统计表索引
CREATE INDEX statistics_user_id_index ON statistics(user_id);
CREATE INDEX card_type_statistics_user_type_index ON card_type_statistics(user_id, card_type);
CREATE INDEX tag_statistics_user_tag_index ON tag_statistics(user_id, tag_name);
```

### 4.3 版本管理

#### 4.3.1 版本历史

**版本 1**（已废弃）：

- ✅ 初始版本：基础表结构
- ⚠️ **注意**：此版本的表结构**未关联用户**，所有业务数据为全局共享

**版本 2**（当前版本）：

- ✅ 添加用户关联：所有业务表添加 `user_id` 字段
- ✅ 实现数据隔离：每个用户只能访问自己的数据
- ✅ 更新所有查询：添加 `WHERE user_id = ?` 过滤条件
- ✅ 统计信息按用户统计
- ✅ 系统模板对所有用户可见（特殊处理）

#### 4.3.2 迁移机制

**迁移文件命名**：

- `1.sqm` - 从版本 0（无版本）到版本 1
- `2.sqm` - 从版本 1 到版本 2
- `N.sqm` - 从版本 N-1 到版本 N

**迁移文件内容**（版本 2 示例）：

```sql
-- 版本 2：添加用户关联

-- 1. 为所有业务表添加 user_id 字段
ALTER TABLE card ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
ALTER TABLE tag ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
-- ... 其他表

-- 2. 数据迁移：为现有数据分配默认用户
UPDATE card SET user_id = COALESCE((SELECT id FROM user LIMIT 1), 'default-user') WHERE user_id = '';
-- ... 其他表

-- 3. 添加索引以优化查询性能
CREATE INDEX card_user_id_index ON card(user_id);
-- ... 其他索引
```

### 4.4 查询接口

#### 4.4.1 Card 查询

```sql
-- 获取所有卡片（按用户过滤）
selectAll:
SELECT * FROM card
WHERE user_id = ?
ORDER BY updated_at DESC;

-- 根据ID获取卡片
selectById:
SELECT * FROM card
WHERE id = ? AND user_id = ?;

-- 根据类型获取卡片
selectByType:
SELECT * FROM card
WHERE type = ? AND user_id = ?
ORDER BY updated_at DESC;
```

#### 4.4.2 Tag 查询

```sql
-- 获取所有标签（按用户过滤）
selectAll:
SELECT * FROM tag
WHERE user_id = ?
ORDER BY card_count DESC, name ASC;

-- 根据名称获取标签
selectByName:
SELECT * FROM tag
WHERE name = ? AND user_id = ?;
```

#### 4.4.3 Template 查询

```sql
-- 获取所有模板（包括系统模板）
selectAll:
SELECT * FROM template
WHERE user_id = ? OR is_system_template = 1
ORDER BY is_system_template DESC, updated_at DESC;
```

---

## 5. 实现细节

### 5.1 SQLDelight 配置

**build.gradle.kts**：

```kotlin
sqldelight {
    databases {
        create("MyHubDatabase") {
            packageName.set("tech.zhifu.app.myhub.datastore.database")
            generateAsync.set(true)
            version = 2
        }
    }
    linkSqlite = true
}
```

### 5.2 版本管理配置

**版本号配置**：

```kotlin
sqldelight {
    databases {
        create("MyHubDatabase") {
            version = 2  // 当前版本号
        }
    }
}
```

**迁移文件**：

- `1.sqm` - 版本 1 迁移文件（空文件，仅用于标记版本）
- `2.sqm` - 版本 2 迁移文件（包含用户关联迁移逻辑）

### 5.3 用户关联实现

#### 5.3.1 表结构变更

所有业务表添加 `user_id` 字段：

```sql
ALTER TABLE card ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
ALTER TABLE tag ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
-- ... 其他表
```

#### 5.3.2 查询更新

所有查询添加 `WHERE user_id = ?` 过滤条件：

```sql
-- 旧查询（版本 1）
SELECT * FROM card;

-- 新查询（版本 2）
SELECT * FROM card WHERE user_id = ?;
```

#### 5.3.3 数据迁移

为现有数据分配默认用户：

```sql
UPDATE card SET user_id = COALESCE((SELECT id FROM user LIMIT 1), 'default-user') WHERE user_id = '';
```

### 5.4 索引优化

**组合索引**：

```sql
-- 优化常用查询组合
CREATE INDEX card_user_type_index ON card(user_id, type);
CREATE INDEX card_user_favorite_index ON card(user_id, is_favorite);
CREATE INDEX tag_user_name_index ON tag(user_id, name);
```

**唯一索引**：

```sql
-- 确保用户标签名称唯一
CREATE UNIQUE INDEX tag_user_name_unique ON tag(user_id, name);
```

---

## 6. 实施计划

### 6.1 实施阶段

#### 阶段 1：核心 Schema 定义（已完成）

- ✅ Card 表和相关表（card_tag、card_metadata、checklist_item）
- ✅ Tag 表
- ✅ Template 表
- ✅ User 表和 user_preferences 表
- ✅ Statistics 相关表

#### 阶段 2：版本管理机制（已完成）

- ✅ SQLDelight 版本配置
- ✅ 版本 1 迁移文件
- ✅ 版本 2 迁移文件

#### 阶段 3：用户关联支持（已完成）

- ✅ 所有业务表添加 `user_id` 字段
- ✅ 更新所有查询添加用户过滤
- ✅ 数据迁移逻辑
- ✅ 索引优化

#### 阶段 4：查询接口完善（已完成）

- ✅ Card 查询接口
- ✅ Tag 查询接口
- ✅ Template 查询接口
- ✅ Statistics 查询接口

### 6.2 里程碑

| 里程碑          | 目标日期       | 状态    |
|--------------|------------|-------|
| 核心 Schema 完成 | 2026-01-13 | ✅ 已完成 |
| 版本管理完成       | 2026-01-13 | ✅ 已完成 |
| 用户关联完成       | 2026-01-13 | ✅ 已完成 |
| 查询接口完成       | 2026-01-13 | ✅ 已完成 |

---

## 7. 风险评估

### 7.1 技术风险

#### 7.1.1 迁移失败风险

**风险描述**：数据库迁移失败导致数据丢失或损坏

**影响**：高

**缓解措施**：

- ✅ 迁移前备份数据库
- ✅ 测试迁移脚本
- ✅ 提供回滚机制
- ✅ 分阶段迁移

#### 7.1.2 版本兼容性风险

**风险描述**：不同版本客户端使用不同 Schema 导致兼容性问题

**影响**：中

**缓解措施**：

- ✅ 版本化 API
- ✅ 向后兼容的 Schema 变更
- ✅ 客户端版本检查

#### 7.1.3 跨平台兼容性风险

**风险描述**：不同平台对 SQLite 的支持不同

**影响**：低

**缓解措施**：

- ✅ 使用 SQLDelight 统一接口
- ✅ 跨平台测试
- ✅ 避免平台特定 SQL 特性

### 7.2 业务风险

#### 7.2.1 数据迁移风险

**风险描述**：用户关联迁移导致数据丢失

**影响**：高

**缓解措施**：

- ✅ 迁移前备份
- ✅ 测试迁移逻辑
- ✅ 提供数据恢复机制

### 7.3 维护风险

#### 7.3.1 Schema 变更风险

**风险描述**：Schema 变更影响现有功能

**影响**：中

**缓解措施**：

- ✅ 版本化 Schema
- ✅ 向后兼容的变更
- ✅ 充分的测试

---

## 8. 附录

### 8.1 相关文档

- [MyHub 数据模型模块方案设计](../datastore-model/docs/myhub-datastore-model-infra-v1.0.md)
- [用户关联设计方案](./user_association_design.md)
- [实施计划](./user_association_implementation_plan.md)
- [版本控制和迁移指南](./version_control_migration_guide.md)
- [SQLDelight 官方文档](https://cashapp.github.io/sqldelight/)

### 8.2 代码示例

#### 8.2.1 创建数据库

```kotlin
val database = MyHubDatabase(
    driver = createDriver(),
    cardAdapter = Card.Adapter(/* ... */),
    tagAdapter = Tag.Adapter(/* ... */),
    // ... 其他适配器
)
```

#### 8.2.2 查询数据

```kotlin
// 获取所有卡片
val cards = database.myHubDatabaseQueries
    .selectAll(userId = currentUserId)
    .executeAsList()

// 根据ID获取卡片
val card = database.myHubDatabaseQueries
    .selectById(id = cardId, userId = currentUserId)
    .executeAsOneOrNull()
```

#### 8.2.3 插入数据

```kotlin
database.myHubDatabaseQueries.insertCard(
    id = card.id,
    type = card.type.name,
    title = card.title,
    content = card.content,
    userId = currentUserId,
    // ... 其他字段
)
```

### 8.3 术语表

| 术语         | 说明                             |
|------------|--------------------------------|
| SQLDelight | Square 提供的 SQL 代码生成工具，支持 KMP   |
| Schema     | 数据库表结构定义                       |
| 迁移文件（.sqm） | SQLDelight 迁移文件，包含版本升级的 SQL 语句 |
| 查询文件（.sq）  | SQLDelight 查询文件，定义 SQL 查询和表结构  |
| user_id    | 用户ID字段，用于多用户数据隔离               |
| 版本管理       | 数据库 Schema 的版本化管理和自动迁移机制       |

### 8.4 常见问题

#### Q1: 如何添加新的数据库表？

**A**: 创建对应的 `.sq` 文件，定义表结构和查询。如果当前版本已发布，需要在下一个版本的迁移文件中添加表创建语句。

#### Q2: 如何修改现有表结构？

**A**: 在下一个版本的 `.sqm` 迁移文件中添加 `ALTER TABLE` 语句，更新对应的 `.sq` 文件以反映新的表结构，更新所有相关的查询语句。

#### Q3: 如何执行数据库迁移？

**A**: SQLDelight 会自动执行迁移脚本。确保迁移文件按顺序命名（`1.sqm`、`2.sqm`、`N.sqm`），并在 `build.gradle.kts` 中更新版本号。

#### Q4: 如何处理用户关联？

**A**: 从版本 2 开始，所有业务表都包含 `user_id` 字段。查询时添加 `WHERE user_id = ?` 过滤条件，确保数据隔离。

---

# Core Datastore Database Module

本模块用于**规范**和**实现** MyHub 应用的数据库基础设施（Datastore Database Infra），为各功能模块**提供统一、跨平台的数据库 Schema 定义和版本管理能力**。它基于 **SQLDelight 和 SQLite**，实现了**类型安全的 Schema 定义**、**版本管理**、**自动迁移**、**用户关联**、**索引优化**等特性，并提供了面向 KMP 场景的**统一数据库接口**，方便在 **Android、iOS、JVM、JS、WASM** 等多端项目中集成和使用。

## 🗄️ 数据库结构

### 核心表

- **user** - 用户表
- **user_preferences** - 用户偏好设置表
- **card** - 卡片表（核心业务表）
- **card_tag** - 卡片标签关联表
- **card_metadata** - 卡片元数据表
- **checklist_item** - 待办清单项表
- **tag** - 标签表
- **template** - 模板表
- **statistics** - 统计信息表
- **card_type_statistics** - 卡片类型统计表
- **tag_statistics** - 标签统计表

### Schema 文件位置

```
src/commonMain/sqldelight/tech/zhifu/app/myhub/datastore/database/
├── Card.sq          # 卡片相关表定义
├── Tag.sq           # 标签表定义
├── Template.sq      # 模板表定义
├── User.sq          # 用户相关表定义
├── Statistics.sq    # 统计信息表定义
├── 1.sqm            # 版本 1 迁移文件
└── 2.sqm            # 版本 2 迁移文件（待创建）
```

## 📊 版本历史

### 版本 1

**状态**：已废弃（已升级到版本 2）

**版本号**：`1`  
**配置位置**：`build.gradle.kts`  
**迁移文件**：`1.sqm`

#### 变更说明

- ✅ 初始版本：基础表结构
- ✅ 创建所有核心业务表（card、tag、template、user、statistics 等）
- ✅ 创建所有索引
- ⚠️ **注意**：此版本的表结构**未关联用户**，所有业务数据为全局共享

#### 表结构

- 所有业务表（card、tag、template 等）**没有** `user_id` 字段
- 仅 `user` 和 `user_preferences` 表与用户相关
- 统计信息为全局统计，不区分用户

#### 迁移说明

- **新数据库**：使用 `Schema.create()` 从 `.sq` 文件创建表结构
- **已存在的数据库**：`1.sqm` 为空迁移文件，仅用于标记版本为 1

---

### 版本 2（当前版本）

**版本号**：`2`  
**配置位置**：`build.gradle.kts`  
**迁移文件**：`2.sqm`

#### 变更说明

- ✅ 添加用户关联：所有业务表添加 `user_id` 字段
- ✅ 实现数据隔离：每个用户只能访问自己的数据
- ✅ 更新所有查询：添加 `WHERE user_id = ?` 过滤条件
- ✅ 统计信息按用户统计
- ✅ 系统模板对所有用户可见（特殊处理）

#### 主要变更

1. **表结构变更**：
    - `card` 表添加 `user_id TEXT NOT NULL DEFAULT ''`
    - `tag` 表添加 `user_id TEXT NOT NULL DEFAULT ''`，并修改唯一约束为 `UNIQUE(name, user_id)`
    - `template` 表添加 `user_id TEXT NOT NULL DEFAULT ''`
    - `card_tag` 表添加 `user_id TEXT NOT NULL DEFAULT ''`
    - `card_metadata` 表添加 `user_id TEXT NOT NULL DEFAULT ''`
    - `checklist_item` 表添加 `user_id TEXT NOT NULL DEFAULT ''`
    - `statistics` 表添加 `user_id TEXT NOT NULL DEFAULT ''`
    - `card_type_statistics` 表添加 `user_id TEXT NOT NULL DEFAULT ''`，主键改为 `(user_id, card_type)`
    - `tag_statistics` 表添加 `user_id TEXT NOT NULL DEFAULT ''`，主键改为 `(user_id, tag_name)`

2. **索引变更**：
    - 为所有表的 `user_id` 字段添加索引
    - 添加组合索引优化常用查询：
        - `card(user_id, type)`
        - `card(user_id, is_favorite)`
        - `card(user_id, updated_at)`
        - `tag(user_id, name)`
        - `template(user_id, card_type)`
        - 等等

3. **查询变更**：
    - 所有查询添加 `WHERE user_id = ?` 过滤条件
    - 系统模板查询特殊处理：`WHERE user_id = ? OR is_system_template = 1`
    - 统计查询按用户过滤

4. **数据迁移**：
    - 为现有数据分配默认用户（从 user 表获取第一个用户，如果没有则使用 'default-user'）
    - 关联表通过主表获取 `user_id`
    - 确保数据完整性

#### 迁移步骤

1. ✅ 创建 `2.sqm` 迁移文件（已完成）
2. ✅ 更新 `build.gradle.kts` 版本号为 `version = 2`（已完成）
3. ✅ 更新所有 `.sq` 文件，添加 `user_id` 字段和更新查询（已完成）
4. SQLDelight 会自动执行从版本 1 到版本 2 的迁移

详细迁移方案请参考：[用户关联设计方案](./docs/user_association_design.md)

---

## 🔄 版本升级流程

### 升级到新版本

1. **创建迁移文件**
    - 在 `src/commonMain/sqldelight/.../database/` 目录下创建 `N.sqm` 文件
    - 包含从版本 N-1 到版本 N 的 SQL 迁移语句

2. **更新版本号**
    - 在 `build.gradle.kts` 中更新 `version = N`

3. **测试迁移**
    - 在开发环境测试从旧版本到新版本的迁移
    - 验证数据完整性和迁移逻辑

4. **更新文档**
    - 在本文档的"版本历史"部分添加新版本说明
    - 记录变更内容和迁移注意事项

### 迁移文件命名规则

- `1.sqm` - 从版本 0（无版本）到版本 1
- `2.sqm` - 从版本 1 到版本 2
- `N.sqm` - 从版本 N-1 到版本 N

**重要**：迁移文件必须按顺序命名，不能跳过版本号。

## 📝 版本变更记录模板

添加新版本时，请使用以下模板：

```markdown
### 版本 N

**版本号**：`N`  
**配置位置**：`build.gradle.kts`  
**迁移文件**：`N.sqm`

#### 变更说明

- ✅ 变更项 1
- ✅ 变更项 2
- ⚠️ 注意事项

#### 主要变更

1. **表结构变更**：
   - 表名：变更说明

2. **索引变更**：
   - 索引名：变更说明

3. **数据迁移**：
   - 迁移逻辑说明

#### 迁移步骤

1. 步骤 1
2. 步骤 2
```

## 🔍 查看当前版本

当前数据库版本配置在 `build.gradle.kts` 中：

```kotlin
sqldelight {
    databases {
        create("MyHubDatabase") {
            version = 1  // 当前版本号
        }
    }
}
```

## 📚 相关文档

- [用户关联设计方案](./docs/user_association_design.md) - 版本 2 的详细设计方案
- [实施计划](./docs/user_association_implementation_plan.md) - 版本 2 的实施计划
- [版本控制和迁移指南](./docs/version_control_migration_guide.md) - 版本控制和迁移的详细说明

## ⚠️ 注意事项

1. **版本一致性**：确保 Client 和 Server 使用相同版本的 Schema
2. **数据备份**：在执行迁移前，务必备份数据库
3. **测试迁移**：在开发环境充分测试迁移逻辑
4. **迁移顺序**：迁移文件必须按顺序执行，不能跳过版本

## 🛠️ 开发指南

### 添加新表

1. 创建对应的 `.sq` 文件（如 `NewTable.sq`）
2. 定义表结构和查询
3. 如果当前版本已发布，需要在下一个版本的迁移文件中添加表创建语句

### 修改现有表

1. 在下一个版本的 `.sqm` 迁移文件中添加 `ALTER TABLE` 语句
2. 更新对应的 `.sq` 文件以反映新的表结构
3. 更新所有相关的查询语句

### 删除表

1. 在迁移文件中添加 `DROP TABLE` 语句
2. 删除或注释对应的 `.sq` 文件


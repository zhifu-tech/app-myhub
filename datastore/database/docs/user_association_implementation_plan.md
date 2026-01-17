# 数据库用户关联实施计划

## 📋 概述

本文档是 `user_association_design.md` 的实施计划，按照优先级和依赖关系组织任务。

## 🎯 实施目标

1. 所有业务表添加 `user_id` 字段
2. 所有查询添加用户过滤条件
3. 实现数据迁移逻辑
4. 修改代码层以支持用户上下文

## 📅 实施阶段

### 阶段 1：数据库 Schema 修改（优先级：高）

#### 1.1 修改 Card.sq

- [ ] 在 `card` 表定义中添加 `user_id TEXT NOT NULL DEFAULT ''`
- [ ] 添加索引 `CREATE INDEX card_user_id_index ON card(user_id)`
- [ ] 修改所有查询，添加 `WHERE user_id = ?` 条件：
    - [ ] `selectAll`
    - [ ] `selectById`（改为 `WHERE id = ? AND user_id = ?`）
    - [ ] `selectFavorites`
    - [ ] `selectByType`
    - [ ] `selectByTag`
    - [ ] `searchCards`
- [ ] 修改 `insertCard`，添加 `user_id` 参数
- [ ] 修改 `updateCard`，添加 `user_id` 验证（可选）

#### 1.2 修改 Tag.sq

- [ ] 在 `tag` 表定义中添加 `user_id TEXT NOT NULL DEFAULT ''`
- [ ] 添加索引 `CREATE INDEX tag_user_id_index ON tag(user_id)`
- [ ] 修改所有查询，添加 `WHERE user_id = ?` 条件：
    - [ ] `selectAll`
    - [ ] `selectById`
    - [ ] `selectByName`
    - [ ] `selectPopular`
- [ ] 修改 `insertTag`，添加 `user_id` 参数
- [ ] 修改 `updateTag`，添加 `user_id` 验证（可选）

#### 1.3 修改 Template.sq

- [ ] 在 `template` 表定义中添加 `user_id TEXT NOT NULL DEFAULT ''`
- [ ] 添加索引 `CREATE INDEX template_user_id_index ON template(user_id)`
- [ ] 修改查询，支持系统模板：
    - [ ] `selectAll` - `WHERE user_id = ? OR is_system_template = 1`
    - [ ] `selectByType` - `WHERE card_type = ? AND (user_id = ? OR is_system_template = 1)`
    - [ ] `selectUserTemplates` - `WHERE is_system_template = 0 AND user_id = ?`
    - [ ] `selectSystemTemplates` - 保持不变（系统模板）
- [ ] 修改 `insertTemplate`，添加 `user_id` 参数

#### 1.4 修改关联表（可选，但推荐）

**card_tag.sq**

- [ ] 在 `card_tag` 表定义中添加 `user_id TEXT NOT NULL DEFAULT ''`
- [ ] 添加索引 `CREATE INDEX card_tag_user_id_index ON card_tag(user_id)`

**card_metadata.sq**

- [ ] 在 `card_metadata` 表定义中添加 `user_id TEXT NOT NULL DEFAULT ''`
- [ ] 添加索引 `CREATE INDEX card_metadata_user_id_index ON card_metadata(user_id)`

**checklist_item.sq**

- [ ] 在 `checklist_item` 表定义中添加 `user_id TEXT NOT NULL DEFAULT ''`
- [ ] 添加索引 `CREATE INDEX checklist_item_user_id_index ON checklist_item(user_id)`

#### 1.5 修改 Statistics.sq

- [ ] 修改 `statistics` 表：
    - [ ] 添加 `user_id TEXT NOT NULL DEFAULT ''`
    - [ ] 将主键改为 `user_id`（或添加唯一索引）
    - [ ] 修改 `selectStatistics` 查询为 `WHERE user_id = ?`
- [ ] 修改 `card_type_statistics` 表：
    - [ ] 添加 `user_id TEXT NOT NULL DEFAULT ''`
    - [ ] 修改主键为 `(user_id, card_type)` 组合
- [ ] 修改 `tag_statistics` 表：
    - [ ] 添加 `user_id TEXT NOT NULL DEFAULT ''`
    - [ ] 修改主键为 `(user_id, tag_name)` 组合

### 阶段 2：数据迁移逻辑（优先级：高）

#### 2.1 数据库版本控制配置

- [x] 创建初始版本迁移文件 `1.sqm`（已完成）
- [x] 在 `build.gradle.kts` 中配置版本号 `version = 1`（已完成）
- [ ] 创建版本 2 迁移文件 `2.sqm`（添加 `user_id` 字段）
- [ ] 更新 `build.gradle.kts` 版本号为 `version = 2`

#### 2.2 创建版本 2 迁移文件（2.sqm）

- [ ] 添加 `user_id` 字段到所有业务表：
    - [ ] `ALTER TABLE card ADD COLUMN user_id TEXT NOT NULL DEFAULT '';`
    - [ ] `ALTER TABLE tag ADD COLUMN user_id TEXT NOT NULL DEFAULT '';`
    - [ ] `ALTER TABLE template ADD COLUMN user_id TEXT NOT NULL DEFAULT '';`
    - [ ] `ALTER TABLE card_tag ADD COLUMN user_id TEXT NOT NULL DEFAULT '';`
    - [ ] `ALTER TABLE card_metadata ADD COLUMN user_id TEXT NOT NULL DEFAULT '';`
    - [ ] `ALTER TABLE checklist_item ADD COLUMN user_id TEXT NOT NULL DEFAULT '';`
    - [ ] `ALTER TABLE statistics ADD COLUMN user_id TEXT NOT NULL DEFAULT '';`
    - [ ] `ALTER TABLE card_type_statistics ADD COLUMN user_id TEXT NOT NULL DEFAULT '';`
    - [ ] `ALTER TABLE tag_statistics ADD COLUMN user_id TEXT NOT NULL DEFAULT '';`
- [ ] 添加索引：
    - [ ] `CREATE INDEX card_user_id_index ON card(user_id);`
    - [ ] `CREATE INDEX tag_user_id_index ON tag(user_id);`
    - [ ] `CREATE INDEX template_user_id_index ON template(user_id);`
    - [ ] 其他表的 `user_id` 索引
- [ ] 数据迁移逻辑：
    - [ ] 获取或创建默认用户
    - [ ] 为所有现有数据分配 `user_id`
    - [ ] 验证迁移结果

#### 2.3 集成到数据库初始化

- [ ] SQLDelight 会自动检测版本变化并执行迁移
- [ ] 验证迁移逻辑正确执行

### 阶段 3：代码层修改（优先级：中）

#### 3.1 创建 UserContextProvider

- [ ] 创建 `UserContextProvider` 接口
- [ ] 实现 Client 端版本（从本地获取当前用户）
- [ ] 实现 Server 端版本（从请求/认证中获取用户）

#### 3.2 修改 DataSource 层

- [ ] 修改 `LocalCardDataSource` 接口，添加 `userId` 参数
- [ ] 修改 `LocalCardDataSourceImpl` 实现
- [ ] 修改 `LocalTagDataSource` 接口和实现
- [ ] 修改 `LocalTemplateDataSource` 接口和实现
- [ ] 修改 `LocalStatisticsDataSource` 接口和实现

#### 3.3 修改 Repository 层

- [ ] 修改 `CardRepository` 实现，使用 `UserContextProvider`
- [ ] 修改 `TagRepository` 实现
- [ ] 修改 `TemplateRepository` 实现
- [ ] 修改 `StatisticsRepository` 实现

#### 3.4 修改 Service 层（Server 端）

- [ ] 修改 `CardService`，从请求中获取用户 ID
- [ ] 修改 `TagService`
- [ ] 修改 `TemplateService`
- [ ] 修改 `StatisticsService`

#### 3.5 修改 API 层（Server 端）

- [ ] 确保所有 API 端点都能获取当前用户
- [ ] 添加用户身份验证（如果还没有）

### 阶段 4：测试（优先级：高）

#### 4.1 单元测试

- [ ] 测试数据迁移逻辑
- [ ] 测试带 `user_id` 的查询
- [ ] 测试用户隔离（用户 A 不能访问用户 B 的数据）

#### 4.2 集成测试

- [ ] 测试完整的数据流（创建、查询、更新、删除）
- [ ] 测试多用户场景
- [ ] 测试系统模板的可见性

#### 4.3 迁移测试

- [ ] 测试从旧版本数据库迁移
- [ ] 测试迁移后数据完整性
- [ ] 测试迁移后查询功能

## 🔍 关键决策点

在开始实施前，需要确认以下问题：

1. **用户认证机制**

    - [ ] Server 端如何识别用户？（JWT、Session、API Key？）
    - [ ] Client 端如何存储用户信息？

2. **标签策略**

    - [ ] 标签是否完全隔离？（推荐：是）
    - [ ] 是否支持标签共享？

3. **系统模板**

    - [ ] 系统模板对所有用户可见？（推荐：是）
    - [ ] 系统模板的 `user_id` 如何处理？（推荐：使用特殊值如 "system"）

4. **统计信息**

    - [ ] 仅按用户统计？（推荐：是）
    - [ ] 是否需要全局统计？

5. **数据迁移**
    - [ ] 迁移脚本执行时机？（推荐：数据库初始化时自动检测）
    - [ ] 是否需要版本号管理？

## 📝 实施检查清单

### 数据库 Schema

- [ ] Card.sq 已修改
- [ ] Tag.sq 已修改
- [ ] Template.sq 已修改
- [ ] Statistics.sq 已修改
- [ ] 所有关联表已修改（可选）
- [ ] 所有索引已添加

### 数据迁移

- [ ] 迁移脚本已实现
- [ ] 迁移逻辑已集成到数据库初始化
- [ ] 迁移测试已通过

### 代码修改

- [ ] UserContextProvider 已实现
- [ ] DataSource 层已修改
- [ ] Repository 层已修改
- [ ] Service 层已修改（Server）
- [ ] API 层已修改（Server）

### 测试

- [ ] 单元测试已通过
- [ ] 集成测试已通过
- [ ] 迁移测试已通过
- [ ] 多用户场景测试已通过

## 🚨 风险与注意事项

1. **数据丢失风险**

    - 迁移前必须备份数据
    - 迁移脚本需要可回滚

2. **性能影响**

    - 添加 `user_id` 索引可能影响写入性能
    - 需要监控查询性能

3. **兼容性**

    - 确保 Client 和 Server 使用相同版本的 Schema
    - 考虑版本兼容策略

4. **安全性**
    - Server 端必须验证用户身份
    - 防止 SQL 注入（使用参数化查询）

## 📚 参考文档

- [用户关联设计方案](./user_association_design.md) - 详细设计方案
- [SQLDelight 迁移文档](https://cashapp.github.io/sqldelight/multiplatform_sqlite/migrations/) - SQLDelight 迁移指南

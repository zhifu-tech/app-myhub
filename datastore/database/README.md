# Core Datastore Database Module

本模块用于**规范**和**实现** MyHub 应用的数据库基础设施（Datastore Database Infra），为各功能模块**提供统一、跨平台的数据库 Schema 定义和版本管理能力**。它基于 **SQLDelight 和 SQLite**，实现了**类型安全的 Schema 定义**、**版本管理**、**索引优化**等特性，并提供面向 KMP 场景的**统一数据库接口**，适配 **Android、iOS、JVM、JS、WASM** 多端。

## 🗄️ 数据库结构

### 核心事实与关系表

- **user** - 用户表
- **user_preferences** - 用户偏好设置表
- **card** - 卡片事实表
- **user_card** - 用户 × 卡片主观关系
- **collection** - 结构化容器
- **user_collection** - 用户 × 容器权限关系
- **tag** - 用户私有语义标签
- **card_tag** - 卡片 × 标签关联
- **card_template** - 卡片模板（生成器）

### Metadata 表（按类型拆分）

- **card_metadata_article** - 文章/链接类
- **card_metadata_code** - 代码类
- **card_metadata_idea** - 想法/灵感类
- **card_metadata_quote** - 引用/摘抄类
- **card_metadata_todo** - 待办/行动项类
- **card_metadata_word** - 单词/语言学习类

### 派生与统计表

- **user_statistics** - 用户统计缓存
- **user_card_type_statistics** - 用户卡片类型统计

### Schema 文件位置

```text
src/commonMain/sqldelight/tech/zhifu/app/myhub/datastore/database/
├── card.sq
├── card_metadata_article.sq
├── card_metadata_code.sq
├── card_metadata_idea.sq
├── card_metadata_quote.sq
├── card_metadata_todo.sq
├── card_metadata_word.sq
├── card_tag.sq
├── card_template.sq
├── collection.sq
├── tag.sq
├── user.sq
├── user_card.sq
├── user_card_type_statistics.sq
├── user_collection.sq
├── user_preferences.sq
└── user_statistics.sq
```

## 📊 版本历史

### v1.0（当前基线）

- **版本号**：`1`
- **配置位置**：`build.gradle.kts`
- **迁移文件**：v1.0 为全新基线，不承载历史迁移

#### 设计要点

- ✅ Card 表只保留最小事实字段
- ✅ 主观状态统一放在 user_card
- ✅ Collection 的 owner 作为事实，权限在 user_collection
- ✅ Tag 为用户私有语义空间
- ✅ 每个表提供索引与基础 CRUD

## 🔄 版本升级流程

### 升级到新版本

1. **创建迁移文件**
    - 在 `src/commonMain/sqldelight/.../database/` 下创建 `N.sqm`
    - 包含从版本 N-1 到版本 N 的 SQL 迁移语句
2. **更新版本号**
    - 在 `build.gradle.kts` 中更新 `version = N`
3. **测试迁移**
    - 验证数据完整性与查询一致性
4. **更新文档**
    - 在“版本历史”追加新版本说明

### 迁移文件命名规则

- `1.sqm` - 从版本 0 到 1
- `2.sqm` - 从版本 1 到 2
- `N.sqm` - 从版本 N-1 到 N

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

- [Datastore Database Infra v1.0 方案设计](./docs/myhub-datastore-database-infra-v1.0.md)
- [MyHub 领域模型图（Card / Collection / User / Tag）](./docs/myhub_领域模型图（card_collection_user_tag）!!!.md)
- [MyHub 数据建模原则 v1](./docs/myhub_数据建模原则_v1.0.md)

## ⚠️ 注意事项

1. **版本一致性**：确保 Client 和 Server 使用相同版本的 Schema
2. **数据备份**：执行迁移前务必备份数据库
3. **测试迁移**：在开发环境充分验证迁移脚本
4. **语义约束**：禁止将主观字段加入事实表

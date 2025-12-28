# 数据库用户关联设计方案

## 📋 概述

当前数据库设计没有区分用户，所有业务数据（卡片、标签、模板等）都是全局共享的。在正式生产环境中，需要将数据与用户关联，实现多用户数据隔离。

## 🎯 目标

1. **保持 client/server 共用一套数据库表结构**
2. **所有业务表关联用户，实现数据隔离**
3. **向后兼容，支持数据迁移**

## 📊 当前状态分析

### 现有数据库表

| 表名                   | 是否关联用户 | 说明                                 |
| ---------------------- | ------------ | ------------------------------------ |
| `user`                 | ✅           | 用户表（已存在）                     |
| `user_preferences`     | ✅           | 用户偏好设置（已关联 user_id）       |
| `card`                 | ❌           | 卡片表（需要添加 user_id）           |
| `card_tag`             | ❌           | 卡片标签关联表（通过 card 间接关联） |
| `card_metadata`        | ❌           | 卡片元数据表（通过 card 间接关联）   |
| `checklist_item`       | ❌           | 待办清单项表（通过 card 间接关联）   |
| `tag`                  | ❌           | 标签表（需要添加 user_id）           |
| `template`             | ❌           | 模板表（需要添加 user_id）           |
| `statistics`           | ❌           | 统计信息表（需要按用户统计）         |
| `card_type_statistics` | ❌           | 卡片类型统计表（需要按用户统计）     |
| `tag_statistics`       | ❌           | 标签统计表（需要按用户统计）         |

### 当前查询模式

- 所有查询都没有用户过滤条件
- 使用 `selectCurrentUser` 获取当前用户（假设只有一个用户）
- Repository 层没有传递用户 ID 的概念

## 🔧 设计方案

### 1. 数据库 Schema 修改

#### 1.1 直接关联用户的表（添加 user_id 字段）

以下表需要直接添加 `user_id` 字段：

**card 表**

```sql
ALTER TABLE card ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
CREATE INDEX card_user_id_index ON card(user_id);
```

**tag 表**

```sql
ALTER TABLE tag ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
CREATE INDEX tag_user_id_index ON tag(user_id);
```

**template 表**

```sql
ALTER TABLE template ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
CREATE INDEX template_user_id_index ON template(user_id);
```

#### 1.2 间接关联用户的表（通过外键关联）

以下表通过关联的 card 表间接关联用户，但为了查询性能，建议也添加 `user_id`：

**card_tag 表**

```sql
ALTER TABLE card_tag ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
CREATE INDEX card_tag_user_id_index ON card_tag(user_id);
```

**card_metadata 表**

```sql
ALTER TABLE card_metadata ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
CREATE INDEX card_metadata_user_id_index ON card_metadata(user_id);
```

**checklist_item 表**

```sql
ALTER TABLE checklist_item ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
CREATE INDEX checklist_item_user_id_index ON checklist_item(user_id);
```

#### 1.3 统计表（按用户统计）

**statistics 表**

```sql
-- 方案A：将 id 改为 user_id（推荐）
ALTER TABLE statistics ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
CREATE UNIQUE INDEX statistics_user_id_index ON statistics(user_id);
-- 删除原来的 PRIMARY KEY，改为 user_id 作为主键

-- 或者方案B：保持 id，添加 user_id
ALTER TABLE statistics ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
CREATE INDEX statistics_user_id_index ON statistics(user_id);
```

**card_type_statistics 表**

```sql
-- 将主键改为 (user_id, card_type) 组合
ALTER TABLE card_type_statistics ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
-- 删除原主键，创建新的组合主键
CREATE UNIQUE INDEX card_type_statistics_user_type_index ON card_type_statistics(user_id, card_type);
```

**tag_statistics 表**

```sql
-- 将主键改为 (user_id, tag_name) 组合
ALTER TABLE tag_statistics ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
-- 删除原主键，创建新的组合主键
CREATE UNIQUE INDEX tag_statistics_user_tag_index ON tag_statistics(user_id, tag_name);
```

#### 1.4 外键约束（可选）

为了数据完整性，可以添加外键约束：

```sql
-- card 表
ALTER TABLE card ADD FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;

-- tag 表
ALTER TABLE tag ADD FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;

-- template 表
ALTER TABLE template ADD FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;

-- 其他表通过 card 关联，可以添加级联删除
```

**注意**：SQLite 的外键约束需要显式启用（`PRAGMA foreign_keys = ON`），且某些 SQLite 版本对 ALTER TABLE 添加外键支持有限。建议在应用层保证数据一致性。

### 2. SQL 查询修改

所有查询都需要添加 `WHERE user_id = ?` 条件。

#### 2.1 Card.sq 查询修改示例

```sql
-- 修改前
selectAll:
SELECT * FROM card
ORDER BY updated_at DESC;

-- 修改后
selectAll:
SELECT * FROM card
WHERE user_id = ?
ORDER BY updated_at DESC;
```

需要修改的查询：

- `selectAll` - 添加 `WHERE user_id = ?`
- `selectById` - 添加 `WHERE id = ? AND user_id = ?`（双重验证）
- `selectFavorites` - 添加 `WHERE is_favorite = 1 AND user_id = ?`
- `selectByType` - 添加 `WHERE type = ? AND user_id = ?`
- `selectByTag` - 在 JOIN 后添加 `WHERE ct.tag_name = ? AND c.user_id = ?`
- `searchCards` - 添加 `WHERE ... AND user_id = ?`

#### 2.2 Tag.sq 查询修改示例

```sql
-- 修改前
selectAll:
SELECT * FROM tag
ORDER BY card_count DESC, name ASC;

-- 修改后
selectAll:
SELECT * FROM tag
WHERE user_id = ?
ORDER BY card_count DESC, name ASC;
```

需要修改的查询：

- `selectAll` - 添加 `WHERE user_id = ?`
- `selectById` - 添加 `WHERE id = ? AND user_id = ?`
- `selectByName` - 添加 `WHERE name = ? AND user_id = ?`
- `selectPopular` - 添加 `WHERE user_id = ?`

#### 2.3 Template.sq 查询修改示例

所有查询都需要添加 `WHERE user_id = ?` 或 `WHERE user_id = ? OR is_system_template = 1`（系统模板对所有用户可见）。

#### 2.4 Statistics.sq 查询修改

```sql
-- 修改前
selectStatistics:
SELECT * FROM statistics
WHERE id = 1;

-- 修改后
selectStatistics:
SELECT * FROM statistics
WHERE user_id = ?;
```

### 3. 数据迁移策略

#### 3.1 迁移步骤

1. **获取或创建默认用户**

   - 如果数据库中没有用户，创建一个默认用户（id: "default-user"）
   - 如果已有用户，使用第一个用户作为默认用户

2. **为现有数据分配用户**

   ```sql
   -- 假设默认用户ID为 'default-user'
   UPDATE card SET user_id = 'default-user' WHERE user_id = '';
   UPDATE tag SET user_id = 'default-user' WHERE user_id = '';
   UPDATE template SET user_id = 'default-user' WHERE user_id = '';
   UPDATE card_tag SET user_id = 'default-user' WHERE user_id = '';
   UPDATE card_metadata SET user_id = 'default-user' WHERE user_id = '';
   UPDATE checklist_item SET user_id = 'default-user' WHERE user_id = '';
   ```

3. **迁移统计表**

   ```sql
   -- 为统计表分配用户
   UPDATE statistics SET user_id = 'default-user' WHERE user_id = '';
   UPDATE card_type_statistics SET user_id = 'default-user' WHERE user_id = '';
   UPDATE tag_statistics SET user_id = 'default-user' WHERE user_id = '';
   ```

4. **删除 DEFAULT 约束（可选）**
   ```sql
   -- SQLite 不支持直接删除 DEFAULT，需要重建表
   -- 或者保持 DEFAULT，但应用层确保始终提供 user_id
   ```

#### 3.2 数据库版本控制

项目使用 SQLDelight 的 `.sqm` 迁移文件进行版本控制：

**当前版本结构**：
- `1.sqm` - 初始版本（基础表结构，无用户关联）
- `2.sqm` - 待创建：添加用户关联（添加 `user_id` 字段）

**版本配置**：
在 `core/datastore-database/build.gradle.kts` 中配置：
```kotlin
sqldelight {
    databases {
        create("MyHubDatabase") {
            version = 1  // 当前版本号
        }
    }
}
```

**迁移文件位置**：
```
core/datastore-database/src/commonMain/sqldelight/tech/zhifu/app/myhub/datastore/database/
├── 1.sqm  # 初始版本
├── 2.sqm  # 用户关联版本（待创建）
├── Card.sq
├── Tag.sq
└── ...
```

#### 3.3 迁移时机

- **方案 A**：在数据库初始化时自动迁移（推荐）

  - SQLDelight 会自动检测版本变化
  - 从当前数据库版本升级到最新版本
  - 按顺序执行 `.sqm` 迁移文件

- **方案 B**：手动迁移脚本
  - 提供独立的迁移脚本
  - 在应用升级时手动执行

### 4. 代码层面修改

#### 4.1 用户上下文管理

需要建立用户上下文机制，在数据访问时自动注入用户 ID。

**方案 A：Repository 层注入用户 ID**

```kotlin
// 修改 Repository 接口，添加 userId 参数
interface CardRepository {
    suspend fun getAllCards(userId: String): List<Card>
    suspend fun getCardById(id: String, userId: String): Card?
    // ...
}
```

**方案 B：使用 UserContext（推荐）**

```kotlin
// 创建用户上下文提供者
interface UserContextProvider {
    suspend fun getCurrentUserId(): String
}

// Repository 实现中使用
class CardRepositoryImpl(
    private val localDataSource: LocalCardDataSource,
    private val userContextProvider: UserContextProvider
) : CardRepository {
    override suspend fun getAllCards(): List<Card> {
        val userId = userContextProvider.getCurrentUserId()
        return localDataSource.getAllCards(userId)
    }
}
```

**方案 C：Server 端从请求中获取用户（推荐用于 Server）**

```kotlin
// Server 端从认证信息中获取用户ID
class CardService(
    private val cardRepository: CardRepository,
    private val authService: AuthService
) {
    suspend fun getAllCards(request: Request): List<Card> {
        val userId = authService.getUserIdFromRequest(request)
        return cardRepository.getAllCards(userId)
    }
}
```

#### 4.2 DataSource 层修改

```kotlin
interface LocalCardDataSource {
    suspend fun getAllCards(userId: String): List<Card>
    suspend fun getCardById(id: String, userId: String): Card?
    suspend fun insertCard(card: Card, userId: String)
    // ...
}
```

#### 4.3 Model 层修改（可选）

可以考虑在 Card 模型中添加 `userId` 字段，但需要权衡：

- **优点**：模型更完整，便于验证
- **缺点**：需要修改所有创建 Card 的地方

**建议**：不在 Model 层添加 `userId`，只在数据库层和查询时使用，保持模型简洁。

### 5. 特殊场景处理

#### 5.1 系统模板

系统模板（`is_system_template = 1）应该对所有用户可见：

```sql
selectAll:
SELECT * FROM template
WHERE user_id = ? OR is_system_template = 1
ORDER BY usage_count DESC, name ASC;
```

#### 5.2 标签共享策略

**方案 A**：标签完全隔离（推荐）

- 每个用户有独立的标签列表
- 标签名称可以重复（不同用户可以有同名标签）

**方案 B**：标签全局共享

- 所有用户共享标签
- 需要修改 tag 表，移除 user_id，或使用特殊值（如 "global"）

**建议**：采用方案 A，标签完全隔离，更符合多用户场景。

#### 5.3 统计信息

统计信息应该按用户统计：

```sql
-- 每个用户有独立的统计
selectStatistics:
SELECT * FROM statistics
WHERE user_id = ?;
```

### 6. 索引优化

添加 `user_id` 相关索引以提升查询性能：

```sql
-- 单列索引
CREATE INDEX card_user_id_index ON card(user_id);
CREATE INDEX tag_user_id_index ON tag(user_id);
CREATE INDEX template_user_id_index ON template(user_id);

-- 组合索引（常用查询场景）
CREATE INDEX card_user_type_index ON card(user_id, type);
CREATE INDEX card_user_favorite_index ON card(user_id, is_favorite);
CREATE INDEX card_user_updated_index ON card(user_id, updated_at);
```

### 7. 实施步骤

#### 阶段 1：数据库 Schema 修改

1. 修改所有 `.sq` 文件，添加 `user_id` 字段
2. 更新所有查询，添加 `user_id` 过滤条件
3. 添加必要的索引

#### 阶段 2：数据迁移

1. 实现迁移逻辑
2. 测试迁移脚本
3. 备份现有数据

#### 阶段 3：代码修改

1. 实现 `UserContextProvider`
2. 修改 DataSource 接口和实现
3. 修改 Repository 接口和实现
4. 修改 Service 层（Server 端）

#### 阶段 4：测试

1. 单元测试
2. 集成测试
3. 迁移测试
4. 多用户场景测试

## ⚠️ 注意事项

1. **向后兼容性**

   - 迁移时需要处理现有数据
   - 确保迁移过程不会丢失数据

2. **性能考虑**

   - 添加 `user_id` 索引
   - 考虑组合索引优化常用查询

3. **数据完整性**

   - 确保所有插入操作都包含 `user_id`
   - 考虑添加应用层约束

4. **安全性**

   - Server 端必须验证用户身份
   - 防止用户访问其他用户的数据

5. **SQLite 限制**
   - SQLite 的 ALTER TABLE 功能有限
   - 某些操作可能需要重建表

## 📝 待确认问题

1. **用户认证方式**

   - Server 端如何获取当前用户 ID？
   - 是否需要实现 JWT 认证？
   - Client 端如何存储和传递用户信息？

2. **标签策略**

   - 标签是否完全隔离？
   - 还是支持全局共享标签？

3. **系统模板**

   - 系统模板是否对所有用户可见？
   - 系统模板的 `user_id` 如何处理？

4. **统计信息**

   - 是否需要全局统计（所有用户汇总）？
   - 还是仅按用户统计？

5. **数据迁移**
   - 迁移脚本的执行时机？
   - 是否需要版本控制？

## 🔄 后续优化

1. **用户权限管理**

   - 支持数据共享（如卡片分享）
   - 支持团队协作

2. **数据同步**

   - 多设备数据同步时确保用户隔离
   - 冲突解决策略

3. **性能优化**
   - 分区表（如果数据量很大）
   - 缓存策略

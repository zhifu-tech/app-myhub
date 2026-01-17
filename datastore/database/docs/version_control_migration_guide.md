# 数据库版本控制和迁移指南

## 📋 版本控制机制

### SQLDelight 版本号说明

1. **默认版本号**：SQLDelight 的默认版本号是 **1**
2. **版本文件**：`.sqm` 文件用于定义从一个版本到下一个版本的迁移
    - `1.sqm` - 从版本 0（无版本）到版本 1 的迁移
    - `2.sqm` - 从版本 1 到版本 2 的迁移
    - `N.sqm` - 从版本 N-1 到版本 N 的迁移

### 当前配置

- **版本号**：`version = 1`（在 `build.gradle.kts` 中配置）
- **迁移文件**：`1.sqm`（初始版本，包含所有表的创建语句）

## ⚠️ 重要问题：现有数据库的迁移

### 问题描述

如果数据库已经存在（没有版本信息），现在添加了版本控制：

1. **情况 A：新数据库**
    - 数据库不存在 → 使用 `Schema.create()` → 创建表结构（版本 1）
    - ✅ 正常工作

2. **情况 B：已存在的数据库（无版本信息）**
    - 数据库已存在，表已创建，但没有版本信息
    - SQLDelight 会认为它是版本 0（或没有版本）
    - 如果使用 `Schema.create()`，不会执行迁移
    - 如果使用 `Schema.migrate()`，会尝试执行 `1.sqm`，但表已存在，会报错

### 解决方案

#### 方案 1：使用 Schema.migrate()（推荐）

修改 `DatabaseDriverFactory`，使用 `Schema.migrate()` 替代 `Schema.create()`：

```kotlin
// 修改前
if (!databaseExists) {
    MyHubDatabase.Schema.synchronous().create(driver)
}

// 修改后
if (!databaseExists) {
    MyHubDatabase.Schema.synchronous().create(driver)
} else {
    // 对于已存在的数据库，使用 migrate 进行迁移
    MyHubDatabase.Schema.synchronous().migrate(driver, oldVersion = 0, newVersion = 1)
}
```

**但是**，对于已经存在的数据库（表结构已经匹配版本 1），我们需要：

- 手动设置数据库版本为 1，或者
- 创建一个特殊的迁移逻辑

#### 方案 2：检测并设置版本（推荐用于现有数据库）

对于已经存在的数据库，我们需要：

1. 检测数据库是否已有表结构
2. 如果表结构已存在且匹配版本 1，手动设置版本号为 1
3. 如果表结构不存在，使用 `Schema.create()`
4. 如果表结构存在但不匹配，执行迁移

```kotlin
private fun createSqliteDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databaseFile.absolutePath}")
    val databaseExists = databaseFile.exists()
    
    if (!databaseExists) {
        // 新数据库：创建表结构
        MyHubDatabase.Schema.synchronous().create(driver)
    } else {
        // 已存在的数据库：检查是否需要迁移
        val currentVersion = getCurrentDatabaseVersion(driver)
        if (currentVersion == 0) {
            // 数据库存在但没有版本信息，说明是旧版本数据库
            // 检查表是否存在
            if (hasTables(driver)) {
                // 表已存在，手动设置版本为 1（假设表结构匹配版本 1）
                setDatabaseVersion(driver, 1)
            } else {
                // 表不存在，创建表结构
                MyHubDatabase.Schema.synchronous().create(driver)
            }
        } else {
            // 有版本信息，使用 migrate 进行迁移
            MyHubDatabase.Schema.synchronous().migrate(driver, currentVersion, 1)
        }
    }
    
    return driver
}
```

#### 方案 3：创建空的 1.sqm（最简单，但需要手动处理）

如果现有数据库的表结构已经匹配版本 1，可以创建一个空的 `1.sqm`：

```sql
-- 版本 1：空迁移（表结构已存在）
-- 此迁移文件用于标记现有数据库为版本 1
```

这样，当 SQLDelight 执行迁移时，不会尝试创建已存在的表。

## 🔄 从版本 1 升级到版本 2

### 步骤

1. **创建 `2.sqm` 文件**
   ```sql
   -- 版本 2：添加 user_id 字段
   ALTER TABLE card ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
   ALTER TABLE tag ADD COLUMN user_id TEXT NOT NULL DEFAULT '';
   -- ... 其他表的 ALTER 语句
   ```

2. **更新 `build.gradle.kts`**
   ```kotlin
   version = 2
   ```

3. **使用 Schema.migrate()**
   ```kotlin
   MyHubDatabase.Schema.synchronous().migrate(driver, oldVersion = 1, newVersion = 2)
   ```

### 迁移执行逻辑

SQLDelight 会：

1. 检测数据库当前版本（存储在内部表中）
2. 如果当前版本 < 目标版本，按顺序执行迁移文件
3. 例如：从版本 1 到版本 2，会执行 `2.sqm`

## 📝 实施建议

### 对于当前情况（添加版本控制到现有数据库）

**推荐方案**：方案 3（创建空的 1.sqm）

1. **修改 `1.sqm`**，使其成为空迁移：
   ```sql
   -- 版本 1：空迁移
   -- 现有数据库的表结构已经匹配此版本，无需执行任何操作
   ```

2. **更新 `DatabaseDriverFactory`**，使用 `migrate()`：
   ```kotlin
   if (!databaseExists) {
       MyHubDatabase.Schema.synchronous().create(driver)
   } else {
       // 对于已存在的数据库，执行迁移（从版本 0 到版本 1）
       // 由于 1.sqm 是空的，不会执行任何操作，只是标记版本为 1
       MyHubDatabase.Schema.synchronous().migrate(driver, oldVersion = 0, newVersion = 1)
   }
   ```

3. **当需要升级到版本 2 时**：
    - 创建 `2.sqm`（包含添加 user_id 的 ALTER 语句）
    - 更新 `build.gradle.kts` 为 `version = 2`
    - SQLDelight 会自动从版本 1 迁移到版本 2

### 注意事项

1. **备份数据**：在执行任何迁移前，务必备份数据库。

2. **测试迁移**：在开发环境中充分测试迁移逻辑。

3. **版本一致性**：确保 Client 和 Server 使用相同版本的 Schema。

4. **迁移文件顺序**：迁移文件必须按顺序命名（1.sqm, 2.sqm, 3.sqm...），不能跳过。

## 🔍 验证迁移

### 检查数据库版本

SQLDelight 会在数据库中存储版本信息。可以通过以下方式验证：

```sql
-- SQLite 中，版本信息存储在内部表中
SELECT * FROM sqlite_master WHERE type='table' AND name='_sqlite_schema';
```

### 测试迁移

1. **新数据库测试**：删除数据库文件，重新运行应用，验证表结构创建
2. **现有数据库测试**：保留现有数据库，运行应用，验证迁移执行
3. **版本升级测试**：从版本 1 升级到版本 2，验证数据迁移



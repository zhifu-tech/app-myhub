# MyHub 数据库管理模块方案设计

**方案名称**：Datastore Database Manage Infra v1  
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
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Datastore Database Manage Infra v1 的基线设计
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

在 MyHub 应用的开发和运行过程中，需要从 JSON 文件加载初始化数据到数据库。典型的场景包括：

1. **初始化数据加载**：应用首次启动时加载示例数据
2. **开发测试**：开发过程中快速填充测试数据
3. **数据迁移**：从 JSON 文件导入数据到数据库
4. **数据重置**：清空现有数据后重新加载

### 1.2 问题根因

在引入统一的数据库管理模块之前，MyHub 应用面临以下问题：

1. **数据加载逻辑分散**：各模块可能使用不同的方式加载数据，导致代码重复
2. **资源文件管理混乱**：JSON 数据文件分散在各处，难以统一管理
3. **用户关联缺失**：加载的数据可能没有正确关联到用户
4. **依赖顺序问题**：数据加载顺序错误可能导致外键约束失败

### 1.3 影响范围

- **开发效率**：缺乏统一的数据加载工具降低开发效率
- **数据一致性**：数据加载逻辑不一致导致数据不一致
- **维护成本**：代码重复增加维护成本

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **从 JSON 文件加载数据**：从 Compose Resources 加载 JSON 数据并写入数据库
- ✅ **支持按表分类的数据文件**：Card、Tag、User、Template
- ✅ **支持重复加载**：可以多次写入数据
- ✅ **支持清空数据后重新加载**：`clearBeforeLoad` 参数
- ✅ **支持用户关联**：所有业务数据（Card、Tag、Template）会自动关联到用户
- ✅ **跨平台支持**：支持所有平台（Android、iOS、JVM、JS、WASM）

### 2.2 非功能目标

- ✅ **易于使用**：提供简洁的 API
- ✅ **资源管理**：使用 Compose Resources 统一管理 JSON 文件
- ✅ **类型安全**：使用 kotlinx.serialization 确保类型安全

### 2.3 模块特性说明

**重要说明**：`datastore/database-manage` 模块是一个**工具模块**，用于数据库数据管理，主要用于开发和测试场景。

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 资源管理：Compose Resources

**选择理由**：

- ✅ **KMP 原生支持**：Compose Resources 完全支持 KMP
- ✅ **类型安全**：编译时检查资源文件存在性
- ✅ **跨平台支持**：所有平台统一使用资源文件

#### 3.1.2 JSON 序列化：kotlinx.serialization

**选择理由**：

- ✅ **KMP 原生支持**：kotlinx.serialization 完全支持 KMP
- ✅ **类型安全**：编译时生成序列化代码
- ✅ **性能优化**：编译时生成，性能优于反射方案

### 3.2 架构模式

#### 3.2.1 数据加载器模式

**设计原则**：

- **DatabaseManager**：统一管理所有数据加载器
- **DataLoader**：每个表对应一个数据加载器（CardDataLoader、TagDataLoader 等）
- **依赖顺序**：按依赖顺序加载数据（User -> Tag -> Card -> Template）

**优势**：

- ✅ **关注点分离**：每个加载器只负责一个表
- ✅ **易于扩展**：添加新表只需添加新的加载器
- ✅ **依赖管理**：统一管理数据加载顺序

---

## 4. 架构设计

### 4.1 模块结构

```text
datastore/database-manage/
├── src/
│   └── commonMain/
│       ├── composeResources/
│       │   └── files/
│       │       └── database/
│       │           └── init/
│       │               ├── card.json
│       │               ├── tag.json
│       │               ├── template.json
│       │               └── user.json
│       └── kotlin/tech/zhifu/app/myhub/datastore/database/manage/
│           ├── DatabaseManager.kt          # 数据库管理器
│           ├── CardDataLoader.kt           # 卡片数据加载器
│           ├── TagDataLoader.kt            # 标签数据加载器
│           ├── TemplateDataLoader.kt       # 模板数据加载器
│           ├── UserDataLoader.kt            # 用户数据加载器
│           ├── InstantSerializer.kt        # Instant 序列化器
│           └── di/
│               └── DatabaseManagerModule.kt # Koin DI 模块
└── build.gradle.kts
```

### 4.2 核心组件

#### 4.2.1 DatabaseManager（数据库管理器）

**功能**：

- 统一管理所有数据加载器
- 提供 `loadAllData()` 方法加载所有表的数据
- 提供 `clearAllData()` 方法清空所有表的数据
- 管理数据加载顺序和用户关联

**主要方法**：

```kotlin
class DatabaseManager(
    private val database: MyHubDatabase
) {
    suspend fun loadAllData(
        resourcePath: String = "database/init",
        clearBeforeLoad: Boolean = false,
        defaultUserId: String? = null
    )
    
    suspend fun clearAllData()
}
```

#### 4.2.2 DataLoader（数据加载器接口）

**功能**：

- 从 JSON 文件加载数据
- 写入数据库
- 清空数据

**实现类**：

- `CardDataLoader`：卡片数据加载器
- `TagDataLoader`：标签数据加载器
- `TemplateDataLoader`：模板数据加载器
- `UserDataLoader`：用户数据加载器

#### 4.2.3 InstantSerializer（时间序列化器）

**功能**：

- 自定义 `Instant` 类型的序列化/反序列化
- 支持 ISO 8601 格式

---

## 5. 实现细节

### 5.1 数据加载流程

**加载顺序**：

1. **User**：首先加载用户数据
2. **Tag**：加载标签数据（关联到用户）
3. **Card**：加载卡片数据（关联到用户和标签）
4. **Template**：加载模板数据（关联到用户）

**用户关联**：

```kotlin
suspend fun loadAllData(
    resourcePath: String = "database/init",
    clearBeforeLoad: Boolean = false,
    defaultUserId: String? = null
) {
    // 1. 加载用户
    userLoader.loadFromResource("$resourcePath/user.json")
    
    // 2. 获取默认用户ID
    val userId = defaultUserId ?: getFirstUserId() ?: "default-user"
    
    // 3. 加载业务数据，关联到用户
    tagLoader.loadFromResource("$resourcePath/tag.json", userId)
    cardLoader.loadFromResource("$resourcePath/card.json", userId)
    templateLoader.loadFromResource("$resourcePath/template.json", userId)
}
```

### 5.2 JSON 文件格式

**user.json**：

```json
[
  {
    "id": "user-1",
    "username": "testuser",
    "email": "test@example.com",
    "displayName": "Test User",
    "createdAt": "2023-01-01T00:00:00Z"
  }
]
```

**card.json**：

```json
[
  {
    "id": "card-1",
    "type": "QUOTE",
    "content": "Test content",
    "createdAt": "2023-01-01T00:00:00Z",
    "updatedAt": "2023-01-01T00:00:00Z"
  }
]
```

### 5.3 数据清空

**清空顺序**：

1. 清空业务数据（Card、Tag、Template）
2. 清空用户数据（会触发外键级联删除）

```kotlin
suspend fun clearAllData() {
    val userId = getFirstUserId()
    
    if (userId != null) {
        cardLoader.clearData(userId)
        tagLoader.clearData(userId)
        templateLoader.clearData(userId)
    }
    
    userLoader.clearData()
}
```

---

## 6. 实施计划

### 6.1 实施阶段

#### 阶段 1：核心功能实现（已完成）

- ✅ DatabaseManager 实现
- ✅ CardDataLoader 实现
- ✅ TagDataLoader 实现
- ✅ TemplateDataLoader 实现
- ✅ UserDataLoader 实现

#### 阶段 2：资源文件管理（已完成）

- ✅ 创建 JSON 数据文件
- ✅ 配置 Compose Resources
- ✅ Instant 序列化器实现

#### 阶段 3：依赖注入集成（已完成）

- ✅ DatabaseManagerModule 实现
- ✅ Koin 集成

### 6.2 里程碑

| 里程碑    | 目标日期       | 状态    |
|--------|------------|-------|
| 核心功能完成 | 2026-01-13 | ✅ 已完成 |
| 资源文件完成 | 2026-01-13 | ✅ 已完成 |
| 依赖注入完成 | 2026-01-13 | ✅ 已完成 |

---

## 7. 风险评估

### 7.1 技术风险

#### 7.1.1 数据加载顺序风险

**风险描述**：数据加载顺序错误可能导致外键约束失败

**影响**：高

**缓解措施**：

- ✅ 统一管理数据加载顺序
- ✅ 先加载用户，再加载业务数据
- ✅ 文档说明加载顺序

#### 7.1.2 JSON 格式错误风险

**风险描述**：JSON 文件格式错误导致加载失败

**影响**：中

**缓解措施**：

- ✅ 使用 kotlinx.serialization 进行类型检查
- ✅ 提供错误处理机制
- ✅ 单元测试覆盖

### 7.2 业务风险

#### 7.2.1 用户关联缺失风险

**风险描述**：加载的数据没有正确关联到用户

**影响**：高

**缓解措施**：

- ✅ 自动获取第一个用户ID
- ✅ 支持手动指定用户ID
- ✅ 所有业务数据都关联到用户

---

## 8. 附录

### 8.1 相关文档

- [MyHub 数据库模块方案设计](../datastore-database/docs/myhub-datastore-database-infra-v1.0.md)
- [MyHub 数据模型模块方案设计](../datastore-model/docs/myhub-datastore-model-infra-v1.0.md)
- [Compose Resources 官方文档](https://github.com/JetBrains/compose-multiplatform-core)

### 8.2 代码示例

#### 8.2.1 基本使用

```kotlin
val databaseManager = DatabaseManager(database)

// 加载所有数据
databaseManager.loadAllData()

// 清空后重新加载
databaseManager.loadAllData(clearBeforeLoad = true)

// 指定用户ID
databaseManager.loadAllData(defaultUserId = "user-1")
```

#### 8.2.2 依赖注入使用

```kotlin
val databaseManagerModule = module {
    single<DatabaseManager> {
        DatabaseManager(get<MyHubDatabase>())
    }
}
```

### 8.3 术语表

| 术语                | 说明                             |
|-------------------|--------------------------------|
| Compose Resources | JetBrains 提供的 KMP 资源管理方案       |
| DataLoader        | 数据加载器，负责从 JSON 文件加载数据并写入数据库    |
| 用户关联              | 业务数据（Card、Tag、Template）关联到用户ID |
| 依赖顺序              | 数据加载的顺序，确保外键约束正确               |

### 8.4 常见问题

#### Q1: 如何添加新的数据表？

**A**: 创建对应的 DataLoader 类，在 DatabaseManager 中添加加载逻辑，并创建对应的 JSON 文件。

#### Q2: 数据加载失败怎么办？

**A**: 检查 JSON 文件格式是否正确，确保数据加载顺序正确，检查用户是否存在。

#### Q3: 如何自定义数据加载顺序？

**A**: 修改 DatabaseManager 的 `loadAllData()` 方法，调整数据加载顺序。

---

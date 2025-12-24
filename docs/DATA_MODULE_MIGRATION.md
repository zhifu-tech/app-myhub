# Datastore 模块迁移说明

## 📋 概述

已将 `composeApp` 中的 `data` 目录抽取为独立的 `core:datastore` 模块，以提高代码的组织性、可维护性和模块化程度。

> **注意**：模块已从 `core:data` 重命名为 `core:datastore`，包名从 `tech.zhifu.app.myhub.data` 重命名为 `tech.zhifu.app.myhub.datastore`。

## 🎯 迁移目标

1. **方便管理**：数据层代码集中在一个独立模块中
2. **避免互相依赖**：清晰的模块边界，减少循环依赖
3. **内部逻辑更聚焦**：数据层专注于数据相关功能

## 📁 模块结构

### 新模块位置

```
core/datastore/
├── build.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/tech/zhifu/app/myhub/datastore/  # 数据层代码
    │   └── sqldelight/                              # SQLDelight Schema
    ├── androidMain/                           # Android 平台实现
    ├── iosMain/                               # iOS 平台实现
    ├── jsMain/                                # Web 平台实现
    ├── jvmMain/                               # Desktop 平台实现
    └── [platform]Test/                        # 各平台测试代码
```

### 迁移的内容

- ✅ **数据模型** (`model/`) - Card、Tag、Template、User、Statistics、SearchFilter
- ✅ **数据传输对象** (`dto/`) - CardDto
- ✅ **数据源** (`datasource/`) - LocalDataSource、RemoteDataSource 及其实现
- ✅ **仓库层** (`repository/`) - 所有 Repository 接口和实现
- ✅ **依赖注入** (`di/`) - DatabaseModule、NetworkModule、DataModule
- ✅ **网络层** (`network/`) - ApiConfig、KtorClientFactory
- ✅ **数据库** (`database/`) - DatabaseDriverFactory
- ✅ **UI 状态** (`ui/`) - CardUiState
- ✅ **SQLDelight Schema** (`sqldelight/`) - 所有 .sq 和 .sqm 文件
- ✅ **测试代码** - 所有平台的测试代码
- ✅ **Web 平台配置** - `webpack.config.d/` 和 `karma.config.d/`（SQL.js 相关）

## 🔧 配置变更

### 1. settings.gradle.kts

添加了新模块：

```kotlin
include(":core:datastore")
```

### 2. composeApp/build.gradle.kts

**移除的依赖**：
- SQLDelight 相关依赖（移至 data 模块）
- Ktor Client 相关依赖（移至 data 模块）
- `sqldelight` 插件配置

**添加的依赖**：
```kotlin
implementation(projects.core.datastore)
```

### 3. core/datastore/build.gradle.kts

新创建的配置文件，包含：
- Kotlin Multiplatform 配置
- SQLDelight 配置（从 composeApp 迁移）
- 所有数据层相关的依赖

## 📦 依赖关系

### core:datastore 模块的依赖

```
core:datastore
├── core:platform (SERVER_PORT 等常量)
├── kotlinx.serialization
├── kotlinx.coroutines
├── SQLDelight
├── Ktor Client
└── Koin
```

### composeApp 对 core:datastore 的依赖

```
composeApp
└── core:datastore (Repository、Model、DI Modules)
```

## 🔄 导入路径

导入路径**保持不变**，因为包名没有改变：

```kotlin
// 使用新的导入路径
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.di.dataModule
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
```

## ✅ 迁移检查清单

- [x] 创建 `core:data` 模块目录结构
- [x] 创建 `build.gradle.kts` 配置文件
- [x] 迁移所有源代码文件
- [x] 迁移 SQLDelight Schema 文件
- [x] 迁移所有测试代码
- [x] 更新 `settings.gradle.kts`
- [x] 更新 `composeApp/build.gradle.kts` 依赖
- [x] 删除 `composeApp` 中的旧 data 目录
- [x] 删除 `composeApp` 中的旧 sqldelight 目录
- [x] 验证导入路径正确
- [x] 重命名模块：`core:data` → `core:datastore`
- [x] 重命名包名：`tech.zhifu.app.myhub.data` → `tech.zhifu.app.myhub.datastore`
- [x] 更新数据库包名：`tech.zhifu.app.myhub.database` → `tech.zhifu.app.myhub.datastore.database`

## 🚀 下一步

1. **清理构建**：
   ```bash
   ./gradlew clean
   ```

2. **验证构建**：
   ```bash
   ./gradlew :core:datastore:build
   ./gradlew :composeApp:build
   ```

3. **运行测试**：
   ```bash
   ./gradlew :core:datastore:allTests
   ```

## 📚 相关文档

- [Datastore Module README](../core/datastore/README.md)
- [架构设计文档](../core/datastore/docs/datastore_architecture.md)
- [待办事项](../core/datastore/docs/datastore_todos.md)

---

**迁移日期**: 2025-12-24  
**维护者**: MyHub Team


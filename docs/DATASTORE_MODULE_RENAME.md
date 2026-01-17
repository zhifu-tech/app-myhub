# Datastore 模块重命名说明

## 📋 概述

已将 `core:data` 模块重命名为 `datastore`，并将所有相关包名从 `tech.zhifu.app.myhub.data` 重命名为 `tech.zhifu.app.myhub.datastore`。

## 🎯 重命名原因

1. **语义更清晰**：`datastore` 比 `data` 更能准确描述模块的职责（数据存储层）
2. **避免关键字冲突**：`data` 是 Kotlin 关键字，在某些上下文中需要使用反引号
3. **命名一致性**：与其他模块命名风格保持一致

## 📝 重命名内容

### 1. 模块名称

- **旧名称**：`core:data`
- **新名称**：`datastore`
- **目录**：`core/data/` → `datastore/`

### 2. 包名变更

#### 主要包名

- **旧包名**：`tech.zhifu.app.myhub.data`
- **新包名**：`tech.zhifu.app.myhub.datastore`

#### 数据库包名

- **旧包名**：`tech.zhifu.app.myhub.database`
- **新包名**：`tech.zhifu.app.myhub.datastore.database`

### 3. 配置更新

#### settings.gradle.kts

```kotlin
// 旧配置
include(":core:data")

// 新配置
include(":datastore")
```

#### build.gradle.kts

**datastore/build.gradle.kts**:
```kotlin
androidLibrary {
    namespace = "tech.zhifu.app.myhub.datastore"  // 更新命名空间
    // ...
}

sqldelight {
    databases {
        create("MyHubDatabase") {
            packageName.set("tech.zhifu.app.myhub.datastore.database")  // 更新包名
            generateAsync.set(true)
        }
    }
}
```

**composeApp/build.gradle.kts**:
```kotlin
dependencies {
    implementation(projects.datastore)  // 更新依赖
}
```

### 4. 导入路径更新

#### 旧导入路径

```kotlin
import tech.zhifu.app.myhub.data.repository.CardRepository
import tech.zhifu.app.myhub.data.model.Card
import tech.zhifu.app.myhub.data.di.dataModule
import tech.zhifu.app.myhub.database.MyHubDatabase
```

#### 新导入路径

```kotlin
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.di.dataModule
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
```

## ✅ 已完成的工作

- [x] 重命名模块目录：`core/data` → `datastore/`（已从 core 目录移动到根目录）
- [x] 更新 `settings.gradle.kts` 中的模块引用
- [x] 更新 `build.gradle.kts` 中的命名空间和 SQLDelight 包名
- [x] 重命名所有源代码文件的包名声明
- [x] 更新所有导入语句
- [x] 更新 SQLDelight `.sq` 文件的包名配置
- [x] 更新 `composeApp` 中的依赖和导入
- [x] 更新所有测试文件
- [x] 更新文档中的引用

## 🔍 验证

### 编译验证

```bash
# 编译 datastore 模块
./gradlew :datastore:build

# 编译 composeApp 模块
./gradlew :composeApp:build

# 编译 iOS framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### 包名验证

```bash
# 检查是否还有旧的包名引用
find . -type f -name "*.kt" -exec grep -l "tech.zhifu.app.myhub.data\." {} \;

# 检查新的包名
find datastore/src -type f -name "*.kt" -exec grep -l "package tech.zhifu.app.myhub.datastore" {} \;
```

## 📚 相关文档

- [Datastore Module README](../datastore/README.md)
- [数据模型架构设计文档](../datastore/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/README.md)
- [模块迁移说明](./DATA_MODULE_MIGRATION.md)

---

**重命名日期**: 2025-12-25  
**维护者**: MyHub Team




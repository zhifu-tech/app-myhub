# iOS Framework: 静态库 vs 动态库

## 📋 概述

在 Kotlin Multiplatform iOS 项目中，可以选择将框架编译为静态库（Static Framework）或动态库（Dynamic Framework）。本文档详细说明两者的区别、优缺点以及选择标准。

## 🔍 基本概念

### 静态库（Static Framework）

- **链接时机**：代码在编译时被复制到最终的可执行文件中
- **代码位置**：每个使用它的应用都包含一份完整的库代码
- **运行时**：不需要额外的库文件

### 动态库（Dynamic Framework）

- **链接时机**：代码在运行时才被加载
- **代码位置**：多个应用可以共享同一份库代码（系统级共享）
- **运行时**：需要额外的库文件（.framework）

## 📊 详细对比

| 特性 | 静态库 (`isStatic = true`) | 动态库 (`isStatic = false`) |
|------|---------------------------|----------------------------|
| **链接时机** | 编译时链接 | 运行时链接 |
| **文件大小** | 每个应用都包含完整库代码，体积较大 | 库代码可共享，应用体积较小 |
| **启动速度** | 稍快（无需加载库） | 稍慢（需要加载库） |
| **内存占用** | 每个应用独立占用内存 | 可共享内存（系统级） |
| **更新方式** | 需要重新编译整个应用 | 可以单独更新库（App Extension） |
| **App Extension** | 每个 Extension 都包含库代码 | Extension 可以共享主应用的库 |
| **符号可见性** | 所有符号都在主应用中 | 符号在运行时解析 |
| **SQLDelight linkSqlite** | ❌ 无效，需要手动链接 | ✅ 自动处理 |
| **iOS 版本要求** | 所有版本 | iOS 8.0+（现在基本不是问题） |
| **调试复杂度** | 简单 | 稍复杂（符号在运行时解析） |

## ⚙️ 配置方式

### 静态库配置

```kotlin
kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true  // 静态库
            freeCompilerArgs += listOf("-Xbinary=bundleId=tech.zhifu.app.myhub")
            // 需要手动链接 SQLite
            linkerOpts += listOf("-lsqlite3")
        }
    }
}

sqldelight {
    databases {
        create("MyHubDatabase") {
            packageName.set("tech.zhifu.app.myhub.database")
            generateAsync.set(true)
        }
    }
    linkSqlite = true  // ⚠️ 对静态库无效，但仍需保留
}
```

**Xcode 配置** (`iosApp/Configuration/Config.xcconfig`)：
```
// Link SQLite library for SQLDelight NativeSqliteDriver
OTHER_LDFLAGS = $(inherited) -lsqlite3
```

### 动态库配置

```kotlin
kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false  // 动态库（默认值）
            freeCompilerArgs += listOf("-Xbinary=bundleId=tech.zhifu.app.myhub")
            // 不需要手动链接 SQLite，linkSqlite = true 会自动处理
        }
    }
}

sqldelight {
    databases {
        create("MyHubDatabase") {
            packageName.set("tech.zhifu.app.myhub.database")
            generateAsync.set(true)
        }
    }
    linkSqlite = true  // ✅ 自动处理 SQLite 链接
}
```

## ✅ 选择标准

### 选择静态库（`isStatic = true`）的场景

1. **简单应用**：单一应用，没有 App Extension
2. **兼容性优先**：需要支持非常老的 iOS 版本（iOS 7.0 及以下）
3. **调试优先**：需要更简单的调试体验
4. **体积不敏感**：应用体积不是主要考虑因素
5. **第三方库要求**：某些第三方库要求静态链接
6. **快速启动**：需要最快的启动速度

### 选择动态库（`isStatic = false`）的场景

1. **有 App Extension**：Today Extension、Widget Extension、Share Extension 等
2. **体积敏感**：需要减小应用体积
3. **多个应用**：需要共享库代码
4. **使用 SQLDelight**：希望自动处理 SQLite 链接（`linkSqlite = true`）
5. **现代应用**：只支持 iOS 8.0+（现在基本所有应用）
6. **内存优化**：希望多个应用共享库代码，减少内存占用

## 📦 实际影响示例

### 静态库示例

```
MyHub.app (50MB)
├── ComposeApp.framework (30MB) ← 静态链接，包含在 app 中
└── 其他代码 (20MB)

如果有 Widget Extension：
MyHubWidget.appex (25MB)
├── ComposeApp.framework (30MB) ← 重复包含
└── Widget 代码 (5MB)

总计：75MB（库代码重复）
```

### 动态库示例

```
MyHub.app (20MB)
├── ComposeApp.framework (30MB) ← 动态链接，系统共享
└── 其他代码 (20MB)

如果有 Widget Extension：
MyHubWidget.appex (5MB)
└── Widget 代码 (5MB) ← 共享主应用的库

系统级别：
└── ComposeApp.framework (30MB) ← 多个应用共享

总计：55MB（库代码共享）
```

## 🔧 SQLDelight linkSqlite 配置说明

### linkSqlite = true 的作用

- **作用**：在 Kotlin/Native 平台上自动链接 SQLite 库
- **默认值**：`true`（可以省略）
- **限制**：**只对动态框架有效，对静态框架无效**

### 静态框架的处理方式

由于 `linkSqlite = true` 对静态框架无效，需要手动配置：

1. **Gradle 配置**：
   ```kotlin
   iosTarget.binaries.framework {
       isStatic = true
       linkerOpts += listOf("-lsqlite3")  // 手动链接
   }
   ```

2. **Xcode 配置** (`Config.xcconfig`)：
   ```
   OTHER_LDFLAGS = $(inherited) -lsqlite3
   ```

### 动态框架的处理方式

动态框架可以依赖 `linkSqlite = true` 自动处理，无需手动配置：

```kotlin
sqldelight {
    linkSqlite = true  // ✅ 自动处理
}

// 不需要 linkerOpts 和 OTHER_LDFLAGS
```

## 💡 项目建议

### 当前项目（MyHub）配置

**当前使用静态库**：
- ✅ 适合简单应用场景
- ✅ 调试简单
- ✅ 启动速度快
- ⚠️ 需要手动链接 SQLite

**如果将来需要 App Extension**：
- 建议改为动态库
- 可以共享库代码，减少体积
- SQLDelight 自动处理链接

### 推荐配置

| 场景 | 推荐配置 |
|------|---------|
| 简单应用（无 Extension） | 静态库（`isStatic = true`） |
| 有 App Extension | 动态库（`isStatic = false`） |
| 体积敏感 | 动态库（`isStatic = false`） |
| 需要 SQLDelight 自动链接 | 动态库（`isStatic = false`） |
| 兼容性优先（iOS 7.0-） | 静态库（`isStatic = true`） |

## 🔄 切换方式

### 从静态库切换到动态库

1. **修改 Gradle 配置**：
   ```kotlin
   iosTarget.binaries.framework {
       isStatic = false  // 改为 false
       // 可以移除手动链接 SQLite 的配置
       // linkerOpts += listOf("-lsqlite3")  // 不再需要
   }
   ```

2. **移除 Xcode 手动链接**（可选）：
   ```diff
   - OTHER_LDFLAGS = $(inherited) -lsqlite3
   ```
   注意：如果 `linkSqlite = true`，可以移除；否则保留。

3. **清理并重新构建**：
   ```bash
   ./gradlew :composeApp:clean
   ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
   ```

### 从动态库切换到静态库

1. **修改 Gradle 配置**：
   ```kotlin
   iosTarget.binaries.framework {
       isStatic = true  // 改为 true
       // 需要添加手动链接 SQLite
       linkerOpts += listOf("-lsqlite3")
   }
   ```

2. **添加 Xcode 手动链接**：
   ```
   OTHER_LDFLAGS = $(inherited) -lsqlite3
   ```

3. **清理并重新构建**：
   ```bash
   ./gradlew :composeApp:clean
   ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
   ```

## ⚠️ 注意事项

1. **SQLite 链接**：
   - 静态库：必须手动配置 `linkerOpts` 和 `OTHER_LDFLAGS`
   - 动态库：`linkSqlite = true` 自动处理

2. **App Extension**：
   - 静态库：每个 Extension 都包含完整库代码
   - 动态库：Extension 可以共享主应用的库

3. **iOS 版本**：
   - 动态库需要 iOS 8.0+（现在基本不是问题）
   - 静态库支持所有 iOS 版本

4. **调试**：
   - 静态库：符号在编译时确定，调试更简单
   - 动态库：符号在运行时解析，调试稍复杂

5. **性能**：
   - 静态库：启动稍快，但内存占用高
   - 动态库：启动稍慢，但内存可共享

## 📚 参考资料

- [Kotlin Multiplatform iOS Framework 文档](https://kotlinlang.org/docs/native-ios-frameworks.html)
- [SQLDelight Native 文档](https://cashapp.github.io/sqldelight/native_sqlite/)
- [Apple Framework 文档](https://developer.apple.com/library/archive/documentation/MacOSX/Conceptual/BPFrameworks/Concepts/FrameworkAnatomy.html)

---

**最后更新**: 2025-12-24  
**维护者**: MyHub Team


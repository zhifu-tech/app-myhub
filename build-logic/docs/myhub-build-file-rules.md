# MyHub KMP 模块构建文件规则

**本文档详细说明 MyHub 项目中 KMP 模块的 `build.*.gradle.kts` 文件定义和启用规则。**

- **版本编号：v1.1**
- **创建日期：2026-01-13**
- **最后更新：2026-01-13**
- **文档类型：技术规范文档**

## 📋 概述

MyHub 项目中的 KMP 模块采用 Kotlin Multiplatform (KMP) 架构，支持多平台构建。根据项目配置的启用平台，Gradle 会自动选择合适的构建文件。

### 动态构建文件生成系统

**重要更新（v1.1）：** MyHub 项目现在使用**动态构建文件生成系统**，系统会根据 `build.gradle.kts` 自动生成平台特定的构建文件。

**工作原理：**

1. **主构建文件**：每个模块维护一个 `build.gradle.kts`，包含所有平台的配置
2. **动态生成**：根据 `enabledPlatforms` 参数，系统从 `build.gradle.kts` 中提取平台特定配置，动态生成 `build.{platform}.gradle.kts` 文件
3. **文件位置**：生成的平台特定构建文件位于 `{module}/build/build-gradle-files-kts/build.{platform}.gradle.kts`
4. **缓存机制**：如果文件已存在，直接使用，避免重复生成

**构建文件类型：**

1. `build.gradle.kts` - 主构建文件（全平台，手动维护）
2. `build/build-gradle-files-kts/build.{platform}.gradle.kts` - 平台特定构建文件（动态生成）

## 🎯 核心规则

### 规则 1：build.gradle.kts（全平台配置文件）

**定义：** `build.gradle.kts` 是全平台支持的配置文件，包含所有平台可运行的全部配置。

**配置要求：**

- 包含所有平台插件（android、ios、jvm、js、wasmJs）
- 包含所有平台特定的配置（如 Android namespace、JVM 依赖等）
- 包含所有 sourceSets 的依赖配置（commonMain、commonTest、jvmMain、jvmTest 等）

**适用场景：**

- 所有平台全部启用时使用（`enabledPlatforms` 包含所有平台）
- 作为默认构建文件，当没有平台特定文件时使用

### 规则 2：build.{platform}.gradle.kts（平台特定配置文件 - 动态生成）

**定义：** 系统会根据 `build.gradle.kts` 自动生成平台特定的构建文件，文件位于 `build/build-gradle-files-kts/build.{platform}.gradle.kts`。

**生成条件（满足任一条件即可）：**

1. **平台插件存在**：`build.gradle.kts` 中包含平台插件
    - Android：`myhub.kmp.android`
    - JVM：`myhub.kmp.jvm`
    - iOS：`myhub.kmp.ios`
    - JS：`myhub.kmp.js`
    - WASM JS：`myhub.kmp.wasmJs`

2. **平台 sourceSet 存在**：`build.gradle.kts` 中包含平台特定的 sourceSet
    - Android：`android { namespace = "..." }`、`androidMain`、`androidTest`
    - JVM：`jvmMain`、`jvmTest`
    - iOS：`iosMain`、`iosTest`
    - JS：`jsMain`、`jsTest`
    - WASM JS：`wasmJsMain`、`wasmJsTest`

**重要说明：**

- **即使没有平台 sourceSet，只要有平台插件，也会生成平台特定的构建文件**
- 例如：如果 `build.gradle.kts` 中包含 `alias(libs.plugins.myhub.kmp.ios)`，即使没有 `iosMain` 或 `iosTest`，也会生成 `build.ios.gradle.kts`

**配置要求：**

- 仅包含该平台相关的插件（如 `myhub.kmp` + `myhub.kmp.android`）
- 仅包含该平台特定的配置（如 Android namespace、平台特定的 sourceSet 依赖）
- 包含 commonMain 和 commonTest（所有平台都需要）

**适用场景：**

- 仅启用该平台时（如 `enabledPlatforms=android`）
- 启用该平台 + Server 两个平台时（如 `enabledPlatforms=android,server`）

**回退机制：**

- 如果某个平台不满足生成条件（既没有平台插件，也没有平台 sourceSet），系统会使用默认的 `build.gradle.kts`
- 这确保了所有平台都能正确构建，即使某些平台没有特定配置

## 📁 构建文件类型说明

### 1. build.gradle.kts（主构建文件）

**文件路径：** `{module}/build.gradle.kts`

**启用条件：**

- 所有平台全部启用时使用（`enabledPlatforms` 包含所有平台）
- 或者没有找到平台特定的构建文件时作为默认文件

**配置内容：**

- **插件：** 包含所有平台插件

    - `myhub.kmp` - KMP 基础插件
    - `myhub.kmp.android` - Android 平台插件
    - `myhub.kmp.ios` - iOS 平台插件
    - `myhub.kmp.jvm` - JVM 平台插件
    - `myhub.kmp.js` - JS 平台插件
    - `myhub.kmp.wasmJs` - WASM JS 平台插件
    - 其他模块特定的插件（如 `kotlinSerialization`、`composeCompiler`、`composeMultiplatform` 等）

- **平台特定配置：**

    - Android：`android { namespace = "..." }`
    - 其他平台特定配置（如 JVM、iOS 等）

- **依赖配置：**
    - **commonMain：** 所有平台共享的依赖
    - **commonTest：** 所有平台共享的测试依赖
    - **平台特定 sourceSet：** 如 `jvmMain`、`jvmTest`、`androidMain` 等

**适用场景：**

- 需要同时支持 Android、iOS、JVM、JS、WASM JS 等多个平台
- 作为默认构建文件，当没有平台特定文件时使用

---

### 2. build.android.gradle.kts（Android 平台专用）

**文件路径：** `{module}/build.android.gradle.kts`

**启用条件：**

- 仅启用 Android 平台时（`enabledPlatforms=android`）
- 或者启用 Android + Server 两个平台时（`enabledPlatforms=android,server`）

**提取原因：**

- `build.gradle.kts` 中包含 Android 特定配置（如 `android { namespace = "..." }`、`androidMain` 依赖等）
- 需要为仅 Android 平台构建提供优化配置

**配置内容：**

- **插件：** 仅包含 Android 相关插件

    - `myhub.kmp` - KMP 基础插件
    - `myhub.kmp.android` - Android 平台插件
    - 其他模块特定的插件（如 `kotlinSerialization`、`composeCompiler`、`composeMultiplatform` 等，如果模块需要）

- **命名空间：** Android 平台的 namespace 配置

- **依赖配置：**
    - **commonMain：** 所有平台共享的依赖
    - **commonTest：** 所有平台共享的测试依赖
    - **androidMain：** Android 平台特定的依赖（如果存在）

**特点：**

- 仅配置 Android 平台相关的插件和依赖
- 包含 Android 平台特定的配置和依赖
- 配置了 Android namespace
- 不包含其他平台的配置，减少不必要的插件加载

**适用场景：**

- 仅构建 Android 应用
- Android + Server 双平台构建

---

### 4. build.jvm.gradle.kts（JVM 平台专用）

**文件路径：** `{module}/build.jvm.gradle.kts`

**启用条件：**

- 仅启用 JVM 平台时（`enabledPlatforms=jvm`）
- 或者启用 JVM + Server 两个平台时（`enabledPlatforms=jvm,server`）

**提取原因：**

- `build.gradle.kts` 中包含 JVM 特定配置（如 `jvmMain`、`jvmTest` sourceSet 依赖等）
- 需要为仅 JVM 平台构建提供优化配置

**配置内容：**

- **插件：** 仅包含 JVM 相关插件

    - `myhub.kmp` - KMP 基础插件
    - `myhub.kmp.jvm` - JVM 平台插件
    - 其他模块特定的插件（如 `kotlinSerialization`、`composeCompiler`、`composeMultiplatform` 等，如果模块需要）

- **依赖配置：**
    - **commonMain：** 所有平台共享的依赖
    - **commonTest：** 所有平台共享的测试依赖
    - **jvmMain：** JVM 平台特定的依赖（如果存在）
    - **jvmTest：** JVM 平台特定的测试依赖（如果存在）

**特点：**

- 仅配置 JVM 平台相关的插件和依赖
- 包含 JVM 平台特定的依赖和测试依赖
- 不包含其他平台的配置，减少不必要的插件加载

**适用场景：**

- 仅构建 JVM 应用
- JVM + Server 双平台构建

---

### 5. 其他平台特定构建文件

**文件路径：** `{module}/build.{platform}.gradle.kts`

**支持的平台：**

- `build.ios.gradle.kts` - iOS 平台专用
- `build.js.gradle.kts` - JS 平台专用
- `build.wasmJs.gradle.kts` - WASM JS 平台专用

**提取原则：**

- 如果 `build.gradle.kts` 中包含该平台的特定配置，应创建对应的平台特定构建文件
- 配置要求与其他平台特定文件相同

---

## ⚙️ 动态构建文件生成系统详解

### 系统架构

动态构建文件生成系统位于 `gradle/build-file-generator.gradle.kts`，包含以下核心组件：

1. **BuildFileParser**：解析 `build.gradle.kts`，提取插件、kotlin 块、android 块、sourceSets 等
2. **PlatformConfigExtractor**：从解析结果中提取特定平台的配置
3. **BuildFileGenerator**：根据平台配置生成 `build.{platform}.gradle.kts` 文件
4. **generateAndSetBuildFile**：主函数，协调整个生成流程

### 生成流程

```text
1. 检查根项目
   └─> 如果是根项目，直接使用 build.gradle.kts，跳过生成

2. 检查文件是否存在
   └─> 如果 build/build-gradle-files-kts/build.{platform}.gradle.kts 存在
       └─> 直接使用，跳过解析和生成（性能优化）

3. 解析 build.gradle.kts
   ├─> 提取插件列表
   ├─> 提取 kotlin 配置块
   ├─> 提取 android 配置块（如果存在）
   └─> 提取 sourceSets 配置

4. 提取平台配置
   ├─> 过滤平台相关插件（如 myhub.kmp.android）
   ├─> 提取 commonMain 和 commonTest
   └─> 提取平台特定的 sourceSet（如 androidMain、jvmMain）

5. 检查生成条件
   ├─> 检查是否有平台插件（如 myhub.kmp.ios）
   └─> 检查是否有平台 sourceSet（如 iosMain）

6. 生成构建文件
   └─> 如果满足条件，生成 build/build-gradle-files-kts/build.{platform}.gradle.kts
```

### 平台配置检测逻辑

系统使用以下逻辑判断是否需要生成平台特定的构建文件：

```kotlin
// 检查是否有平台插件
val hasPlatformPlugin = config.plugins.contains("myhub.kmp.{platform}")

// 检查是否有平台 sourceSet
val hasPlatformSourceSet = config.sourceSets.any { it.contains("{platform}") }

// 只要满足任一条件即可生成
val hasPlatformConfig = hasPlatformPlugin || hasPlatformSourceSet
```

**示例：**

- 如果 `build.gradle.kts` 中包含 `alias(libs.plugins.myhub.kmp.ios)`，即使没有 `iosMain`，也会生成 `build.ios.gradle.kts`
- 如果 `build.gradle.kts` 中包含 `iosMain { ... }`，即使没有 `myhub.kmp.ios` 插件（虽然这种情况不应该发生），也会生成 `build.ios.gradle.kts`

**重要说明：**

- **即使没有平台 sourceSet，只要有平台插件，也会生成平台特定的构建文件**
- 这确保了所有启用的平台都能正确构建，即使模块只使用 common 代码

### 文件位置

- **动态生成的文件**：`{module}/build/build-gradle-files-kts/build.{platform}.gradle.kts`
- **手动维护的文件**：`{module}/build.{platform}.gradle.kts`（项目根目录，向后兼容）

### 性能优化

1. **文件存在性检查**：如果文件已存在，直接使用，跳过所有解析和生成操作
2. **根项目跳过**：根项目不进行任何平台判定和生成，直接使用默认构建文件
3. **配置缓存**：Gradle 配置缓存会缓存构建文件选择结果

## 🔄 构建文件选择规则

构建文件的选择由 `settings.gradle.kts` 中的动态构建文件生成系统控制。

### 动态生成流程

1. **根项目处理**
    - 根项目（`MyHub`）始终使用默认的 `build.gradle.kts`
    - 不进行平台判定和生成，跳过所有动态生成逻辑

2. **子项目处理**
    - 检查目标文件是否存在：`build/build-gradle-files-kts/build.{platform}.gradle.kts`
    - **文件已存在**：直接使用，跳过解析和生成（性能优化）
    - **文件不存在**：从 `build.gradle.kts` 解析并生成平台特定的构建文件

3. **生成逻辑**
    - 解析 `build.gradle.kts` 中的插件和配置
    - 提取平台特定的配置（插件、sourceSet、Android 配置等）
    - 生成平台特定的构建文件
    - 文件保存在 `build/build-gradle-files-kts/` 目录下

### 优先级顺序

1. **动态生成的平台特定构建文件**（最高优先级）
    - 格式：`build/build-gradle-files-kts/build.{platform}.gradle.kts`
    - 示例：`build/build-gradle-files-kts/build.android.gradle.kts`
    - 如果文件已存在，直接使用（性能优化）
    - 如果文件不存在，动态生成

2. **手动维护的平台特定构建文件**（次优先级）
    - 格式：`build.{platform}.gradle.kts`（项目根目录）
    - 如果存在，优先使用（向后兼容）

3. **主构建文件**（默认）
    - 格式：`build.gradle.kts`
    - 如果平台特定文件不存在或不满足生成条件，使用此文件

### 启用规则

根据 `settings.gradle.kts` 中的平台配置：

- **所有平台启用**：所有项目使用 `build.gradle.kts`
- **仅 Android 启用**：
    - 根项目：使用 `build.gradle.kts`
    - 子项目：动态生成 `build/build-gradle-files-kts/build.android.gradle.kts`（如果满足生成条件）
- **仅 JVM 启用**：
    - 根项目：使用 `build.gradle.kts`
    - 子项目：动态生成 `build/build-gradle-files-kts/build.jvm.gradle.kts`（如果满足生成条件）
- **仅 iOS/JS/WASM JS 启用**：
    - 根项目：使用 `build.gradle.kts`
    - 子项目：动态生成 `build/build-gradle-files-kts/build.{platform}.gradle.kts`（如果满足生成条件），否则使用 `build.gradle.kts`
- **Android + Server 启用**：动态生成 `build/build-gradle-files-kts/build.android.gradle.kts`（如果满足生成条件）
- **JVM + Server 启用**：动态生成 `build/build-gradle-files-kts/build.jvm.gradle.kts`（如果满足生成条件）

### 平台配置方式

通过 Gradle 属性 `enabledPlatforms` 配置启用的平台：

```bash
# 启用所有平台
./gradlew build

# 仅启用 Android
./gradlew build -PenabledPlatforms=android

# 仅启用 JVM
./gradlew build -PenabledPlatforms=jvm

# 启用 Android + Server
./gradlew build -PenabledPlatforms=android,server

# 启用 JVM + Server
./gradlew build -PenabledPlatforms=jvm,server
```

## 📊 构建文件对比

| 特性                    | build.gradle.kts | build.android.gradle.kts   | build.jvm.gradle.kts   |
|-----------------------|------------------|----------------------------|------------------------|
| **插件数量**              | 6 个（全平台）+ 模块特定插件 | 2 个（KMP + Android）+ 模块特定插件 | 2 个（KMP + JVM）+ 模块特定插件 |
| **Android Namespace** | ✅（如果模块需要）        | ✅（如果模块需要）                  | ❌                      |
| **commonMain 依赖**     | ✅                | ✅                          | ✅                      |
| **commonTest 依赖**     | ✅                | ✅                          | ✅                      |
| **androidMain 依赖**    | ✅（如果模块需要）        | ✅（如果模块需要）                  | ❌                      |
| **jvmMain 依赖**        | ✅（如果模块需要）        | ❌                          | ✅（如果模块需要）              |
| **jvmTest 依赖**        | ✅（如果模块需要）        | ❌                          | ✅（如果模块需要）              |
| **适用场景**              | 全平台或回退使用         | Android 单/双平台              | JVM 单/双平台              |

## 🎯 最佳实践

### 1. 维护主构建文件

**推荐做法：**

- **只维护 `build.gradle.kts`**：包含所有平台的配置
- 系统会自动生成平台特定的构建文件，无需手动创建
- 确保 `build.gradle.kts` 包含：
    - 所有需要的平台插件（如 `myhub.kmp.android`、`myhub.kmp.ios` 等）
    - 所有平台的 sourceSet 配置（如 `androidMain`、`jvmMain`、`iosMain` 等）
    - Android 特定配置（如 `namespace`）

**平台特定文件生成条件：**

- **有平台插件**：即使没有平台 sourceSet，只要有平台插件，也会生成
    - 例如：`alias(libs.plugins.myhub.kmp.ios)` → 生成 `build.ios.gradle.kts`
- **有平台 sourceSet**：如果有平台 sourceSet，也会生成
    - 例如：`iosMain { ... }` → 生成 `build.ios.gradle.kts`
- **Android 配置**：如果有 `android { namespace = "..." }`，也会生成
    - 例如：`android { namespace = "..." }` → 生成 `build.android.gradle.kts`

**不生成条件：**

- 如果某个平台在 `build.gradle.kts` 中既没有插件也没有 sourceSet，不会生成对应的平台特定文件
- 此时该平台会使用默认的 `build.gradle.kts` 作为回退

### 2. 依赖管理

- **通用依赖**：放在 `commonMain` 中，所有平台共享
- **平台特定依赖**：放在对应的平台 sourceSet 中（如 `androidMain`、`jvmMain`）
- **测试依赖**：
    - 通用测试依赖放在 `commonTest` 中
    - 平台特定测试依赖放在对应的平台 test sourceSet 中（如 `jvmTest`）

### 3. 构建优化

- **单平台构建**：系统自动生成平台特定的构建文件，只加载必要的插件，提高构建速度
- **全平台构建**：使用主构建文件统一管理，简化配置维护
- **文件缓存**：如果平台特定构建文件已存在，直接使用，避免重复解析和生成（性能优化）
- **根项目优化**：根项目跳过所有动态生成逻辑，直接使用默认构建文件

### 4. 配置一致性

- 所有构建文件中的 `commonMain` 和 `commonTest` 依赖应保持一致
- 平台特定配置由系统自动从 `build.gradle.kts` 中提取，确保配置的一致性
- **只需维护 `build.gradle.kts`**，系统会自动生成平台特定的构建文件

### 5. 插件管理

- 模块特定的插件（如 `kotlinSerialization`、`composeCompiler`、`composeMultiplatform` 等）应在 `build.gradle.kts` 中包含
- 系统会自动将这些插件包含在生成的平台特定构建文件中
- 如果某个插件只在特定平台需要，可以在 `build.gradle.kts` 中条件性包含，系统会正确提取

## 📝 注意事项

1. **动态生成文件位置**

    - 动态生成的构建文件位于 `build/build-gradle-files-kts/build.{platform}.gradle.kts`
    - 这些文件由系统自动生成，**不应手动编辑**
    - 如果需要修改配置，应修改 `build.gradle.kts`，然后执行 `gradlew clean` 重新生成

2. **文件缓存机制**

    - 如果平台特定构建文件已存在，系统会直接使用，跳过解析和生成（性能优化）
    - 如果遇到编译问题，可以执行 `gradlew clean` 清理后重新生成
    - 系统会输出提示信息：`ℹ️  使用已存在的平台特定构建文件: build.{platform}.gradle.kts`

3. **根项目处理**

    - **根项目（`MyHub`）始终使用默认的 `build.gradle.kts`**
    - 根项目不进行平台判定和生成，跳过所有动态生成逻辑
    - 根项目的 `build.gradle.kts` 主要用于插件管理（`apply false`）

4. **平台插件检测**

    - **重要**：即使没有平台 sourceSet，只要有平台插件，也会生成平台特定的构建文件
    - 例如：如果 `build.gradle.kts` 中包含 `alias(libs.plugins.myhub.kmp.ios)`，即使没有 `iosMain` 或 `iosTest`，也会生成 `build.ios.gradle.kts`
    - 这确保了所有启用的平台都能正确构建

5. **文件命名规范**

    - 动态生成的构建文件：`build/build-gradle-files-kts/build.{platform}.gradle.kts`
    - 手动维护的构建文件：`build.{platform}.gradle.kts`（项目根目录）
    - 平台名称必须与 `settings.gradle.kts` 中定义的平台名称一致

6. **插件一致性**

    - 所有构建文件中的插件版本应保持一致
    - 通过 `libs.plugins` 统一管理插件版本

7. **依赖版本管理**

    - 所有依赖版本通过 `libs` 目录统一管理
    - 避免在构建文件中硬编码版本号

8. **命名空间配置**

    - Android 平台必须配置 `namespace`（如果模块需要）
    - 其他平台不需要配置命名空间

9. **配置提取原则**

    - 系统会自动从 `build.gradle.kts` 提取平台特定配置
    - 确保 `build.gradle.kts` 包含完整的平台配置
    - 生成的平台特定文件包含该平台所需的所有配置（插件、sourceSet、Android 配置等）

10. **模块特定插件**
    - 如果模块使用了特定的插件（如 Compose、Serialization 等），需要在 `build.gradle.kts` 中包含
    - 系统会自动将这些插件包含在生成的平台特定构建文件中

11. **回退机制**
    - 如果某个平台不满足生成条件（既没有平台插件，也没有平台 sourceSet），系统会使用默认的 `build.gradle.kts`
    - 这确保了所有平台都能正确构建，即使某些平台没有特定配置
    - 根项目始终使用 `build.gradle.kts`，不进行任何平台判定和生成

12. **清理和重新生成**
    - 如果需要重新生成平台特定的构建文件，执行：`./gradlew clean`
    - 清理后，系统会在下次构建时重新生成所有平台特定的构建文件

## 🔗 相关文档

- [MyHub 基础设施规则](./infra/myhub-infra-rules.md)
- [项目 settings.gradle.kts](../settings.gradle.kts)

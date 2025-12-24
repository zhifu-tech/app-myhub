# iOS Framework 默认配置说明

## 📋 概述

当在 Kotlin Multiplatform 项目中不显式配置 `iosTarget.binaries.framework` 时，Kotlin 会使用默认配置。本文档详细说明默认行为、`baseName` 的作用以及打包格式。

## 1️⃣ 默认配置是什么？

### 当不配置 `iosTarget.binaries.framework` 时

如果注释掉或省略 `iosTarget.binaries.framework` 配置块，Kotlin Multiplatform 的默认行为是：

```kotlin
listOf(
    iosArm64(),
    iosSimulatorArm64()
).forEach { iosTarget ->
    // 没有 framework 配置
}
```

**默认行为**：

1. **不会生成 `.framework` 文件**
   - 模块会被编译为 **Kotlin 库（`.klib`）** 格式
   - 这是 Kotlin/Native 的中间表示格式

2. **默认库类型**：
   - 作为**库模块**（library module）被其他模块依赖
   - 不会生成独立的 framework bundle

3. **默认名称**：
   - 如果生成了 framework，默认名称是**模块名称**（Gradle 项目名称）
   - 例如：`core:data` 模块 → 默认 framework 名称是 `data`

### 对比：配置 vs 不配置

#### ✅ 配置了 `framework`（生成 framework）

```kotlin
iosTarget.binaries.framework {
    baseName = "MyHubData"
    isStatic = false
}
```

**结果**：
- 生成 `MyHubData.framework`（动态库）
- 可以在 Xcode 中直接使用
- 适合作为**独立模块**供其他应用使用

#### ❌ 不配置 `framework`（生成 klib）

```kotlin
iosTarget.binaries.framework {
    // 注释掉或省略
}
```

**结果**：
- 生成 `.klib` 文件（Kotlin 库）
- 只能被其他 Kotlin Multiplatform 模块依赖
- **不能**直接在 Xcode 中使用
- 适合作为**内部依赖模块**

## 2️⃣ `baseName` 的作用是什么？

### 作用

`baseName` 指定生成的**二进制文件的基础名称**。

### 命名规则

```kotlin
iosTarget.binaries.framework {
    baseName = "MyHubData"  // 设置基础名称
}
```

**生成的 framework 名称**：
- iOS Framework: `MyHubData.framework`
- 内部二进制: `MyHubData`（在 framework 内部）

### 默认值

如果不设置 `baseName`：
- **默认值**：Gradle 项目名称（去掉路径前缀）
- 例如：`core:data` → `data`
- 例如：`composeApp` → `composeApp`

### 示例

```kotlin
// 模块：core:data
iosTarget.binaries.framework {
    baseName = "MyHubData"  // 生成 MyHubData.framework
}

// 模块：composeApp
iosTarget.binaries.framework {
    baseName = "ComposeApp"  // 生成 ComposeApp.framework
}

// 不设置 baseName
iosTarget.binaries.framework {
    // baseName 默认为模块名
    // core:data → data.framework
    // composeApp → composeApp.framework
}
```

## 3️⃣ 打包格式是什么？`.a`、`.so`、`.klib`？

### iOS 平台的输出格式

在 Kotlin Multiplatform iOS 项目中，根据配置不同，会生成不同的格式：

#### 1. Framework 格式（`.framework`）

**配置**：
```kotlin
iosTarget.binaries.framework {
    baseName = "MyHubData"
    isStatic = false  // 或 true
}
```

**输出**：
- **格式**：`.framework` bundle（iOS Framework）
- **位置**：`build/bin/iosSimulatorArm64/debugFramework/MyHubData.framework`
- **内容**：
  - `MyHubData` - 二进制文件（动态库或静态库）
  - `Headers/` - 头文件目录
  - `Info.plist` - Framework 信息
  - `Modules/` - 模块映射文件

**内部二进制格式**：
- **动态库** (`isStatic = false`)：`.dylib`（动态链接库）
- **静态库** (`isStatic = true`)：`.a`（静态归档文件）

#### 2. Kotlin 库格式（`.klib`）

**配置**：
```kotlin
// 不配置 framework，或配置为 library
iosTarget.binaries {
    // 默认生成 klib
}
```

**输出**：
- **格式**：`.klib`（Kotlin 库）
- **位置**：`build/klib/iosSimulatorArm64/main/`
- **用途**：
  - 只能被其他 Kotlin Multiplatform 模块依赖
  - 不能直接在 Xcode 中使用
  - 适合作为内部依赖

#### 3. 其他格式（iOS 不使用）

- **`.so`**：Linux 共享库格式，iOS **不使用**
- **`.dylib`**：macOS/iOS 动态库，在 framework **内部**使用

### 格式对比表

| 配置 | 输出格式 | 文件扩展名 | 用途 | 是否可在 Xcode 使用 |
|------|---------|-----------|------|-------------------|
| `framework { isStatic = false }` | Framework (动态) | `.framework` | 独立模块，可共享 | ✅ 是 |
| `framework { isStatic = true }` | Framework (静态) | `.framework` | 独立模块，嵌入应用 | ✅ 是 |
| 不配置 `framework` | Kotlin 库 | `.klib` | 内部依赖 | ❌ 否 |

### 实际文件结构示例

#### Framework 结构（`.framework`）

```
MyHubData.framework/
├── MyHubData                    # 二进制文件（.dylib 或 .a）
├── Headers/
│   └── MyHubData.h              # 头文件
├── Info.plist                   # Framework 信息
└── Modules/
    └── module.modulemap         # 模块映射
```

#### Klib 结构（`.klib`）

```
data.klib/
├── data.klib                    # Kotlin 库文件
└── manifest                     # 清单文件
```

## 🎯 实际应用场景

### 场景 1：独立模块（需要 framework）

**需求**：`core:data` 模块需要作为独立 framework 供 Xcode 使用

**配置**：
```kotlin
iosTarget.binaries.framework {
    baseName = "MyHubData"
    isStatic = false
}
```

**结果**：
- ✅ 生成 `MyHubData.framework`
- ✅ 可以在 Xcode 中直接链接
- ✅ 可以被其他应用复用

### 场景 2：内部依赖（不需要 framework）

**需求**：`core:data` 只作为内部依赖，不需要独立 framework

**配置**：
```kotlin
// 不配置 framework
listOf(
    iosArm64(),
    iosSimulatorArm64()
).forEach { iosTarget ->
    // 注释掉 framework 配置
}
```

**结果**：
- ✅ 生成 `.klib` 文件
- ✅ 被 `composeApp` 模块依赖
- ✅ 最终打包到 `ComposeApp.framework` 中
- ❌ 不能单独在 Xcode 中使用

## ⚠️ 注意事项

### 1. 依赖关系

- **Framework 模块**：可以被其他 framework 或应用依赖
- **Klib 模块**：只能被其他 Kotlin Multiplatform 模块依赖

### 2. 链接方式

- **Framework**：在 Xcode 中通过 "Embedded Frameworks" 链接
- **Klib**：在 Gradle 构建时自动链接

### 3. 当前项目建议

对于 `core:data` 模块：

**选项 A：生成 framework**（如果需要独立使用）
```kotlin
iosTarget.binaries.framework {
    baseName = "MyHubData"
    isStatic = false
}
```

**选项 B：不生成 framework**（如果只作为内部依赖）✅ **推荐**
```kotlin
// 注释掉 framework 配置
// 代码会被编译到 composeApp.framework 中
```

## 📚 参考资料

- [Kotlin Multiplatform iOS Frameworks](https://kotlinlang.org/docs/native-ios-frameworks.html)
- [Kotlin/Native Libraries](https://kotlinlang.org/docs/native-libraries.html)
- [Gradle Kotlin DSL Reference](https://kotlinlang.org/docs/multiplatform-multiplatform-dsl-reference.html)

---

**最后更新**: 2025-12-25  
**维护者**: MyHub Team


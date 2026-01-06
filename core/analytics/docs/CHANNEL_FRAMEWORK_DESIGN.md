# Analytics 渠道框架设计汇总

## 📋 概述

Analytics 框架采用**平台 + 渠道**的双维度变体系统，支持在不同平台和渠道下注册不同的 Analytics Provider。

## 🏗️ 架构设计

### 1. 目录结构

框架使用 KMP 变体系统，通过**平台维度**和**渠道维度**的组合来组织代码：

```
core/analytics/src/
├── commonMain/                    # 所有平台和渠道共享的代码
│   ├── AnalyticsProvider.kt       # Provider 接口定义
│   ├── AnalyticsManager.kt        # 核心管理器
│   ├── AnalyticsModule.kt         # Koin DI 模块
│   └── provider/
│       ├── CommonAnalyticsRegistrar.kt  # 通用注册器（ConsoleProvider）
│       └── ConsoleProvider.kt     # 控制台输出 Provider
│
├── {platform}Main/                # 平台标准代码
│   ├── androidMain/                # Android 平台标准代码
│   │   └── di/
│   │       └── AnalyticsRegistrar.android.kt  # expect/actual 实现
│   ├── iosMain/                   # iOS 平台标准代码
│   ├── jsMain/                    # JS 平台标准代码
│   ├── wasmJsMain/                # WASM 平台标准代码
│   └── jvmMain/                   # JVM 平台标准代码
│
├── {platform}{Channel}Main/       # 平台 + 渠道组合代码（互斥）
│   │                              # 根据构建时的 appChannel 参数，只会注入其中一个
│   ├── androidGooglePlayMain/     # Android + Google Play（appChannel=googlePlay）
│   │   └── provider/
│   │       └── AndroidAnalyticsRegistrar.kt  # 注册 FirebaseProvider
│   ├── androidChannelMain/        # Android + 默认渠道（appChannel=channel）
│   │   └── provider/
│   │       └── AndroidAnalyticsRegistrar.kt  # 仅注册 ConsoleProvider
│   ├── iosGooglePlayMain/         # iOS + Google Play
│   ├── iosChannelMain/            # iOS + 默认渠道
│   ├── jsGooglePlayMain/          # JS + Google Play
│   ├── jsChannelMain/             # JS + 默认渠道
│   ├── wasmJsGooglePlayMain/      # WASM + Google Play
│   └── wasmJsChannelMain/         # WASM + 默认渠道
│
└── {channel}Main/                 # 渠道通用代码（跨平台，互斥）
    ├── googlePlayMain/            # Google Play 渠道通用代码（appChannel=googlePlay）
    │   └── provider/
    │       └── FirebaseProvider.kt  # FirebaseProvider 实现
    └── channelMain/               # 默认渠道通用代码（appChannel=channel）
```

### 2. 注册机制

#### 2.1 平台注册器（Platform Registrar）

每个平台都有一个标准注册器，位于 `{platform}Main` 目录：

- **`androidMain`**: `getAnalyticsRegistrar()` 的 actual 实现
- **`iosMain`**: `getAnalyticsRegistrar()` 的 actual 实现
- **`jsMain`**: `getAnalyticsRegistrar()` 的 actual 实现
- **`jvmMain`**: `getAnalyticsRegistrar()` 的 actual 实现（Desktop，注册 FileProvider）

通过 `expect/actual` 机制在 `AnalyticsModule` 中调用：

```kotlin
// commonMain/kotlin/.../di/AnalyticsRegistrar.kt
internal expect fun getAnalyticsRegistrar(): AnalyticsProviderRegistrar?

// {platform}Main/kotlin/.../di/AnalyticsRegistrar.{platform}.kt
internal actual fun getAnalyticsRegistrar(): AnalyticsProviderRegistrar? {
    return { Platform } AnalyticsRegistrar ()  // 返回 {platform}{Channel}Main 中的实现
}
```

#### 2.2 渠道注册器（Channel Registrar）

每个平台+渠道组合都有一个注册器，位于 `{platform}{Channel}Main` 目录：

- **`androidGooglePlayMain`**: `AndroidAnalyticsRegistrar`（注册 FirebaseProvider）
- **`androidChannelMain`**: `AndroidAnalyticsRegistrar`（仅注册 ConsoleProvider）
- **`iosGooglePlayMain`**: `IosAnalyticsRegistrar`（注册 FirebaseProvider）
- **`iosChannelMain`**: `IosAnalyticsRegistrar`（仅注册 ConsoleProvider）
- **`jsGooglePlayMain`**: `JsAnalyticsRegistrar`（注册 FirebaseProvider）
- **`jsChannelMain`**: `JsAnalyticsRegistrar`（仅注册 ConsoleProvider）

**关键机制**：

- **互斥性**：根据构建时的 `appChannel` 参数，只会注入**一个**渠道目录
    - `appChannel=googlePlay` → 注入 `androidGooglePlayMain`，其中的 `AndroidAnalyticsRegistrar` 生效
    - `appChannel=channel` → 注入 `androidChannelMain`，其中的 `AndroidAnalyticsRegistrar` 生效
- **类替换**：`{platform}{Channel}Main` 中的 `AndroidAnalyticsRegistrar` 类会**替换** `androidMain` 中引用的同名类
- **不会同时存在**：默认渠道和 Google Play 渠道是互斥的，不可能同时存在

#### 2.3 Google Play 渠道注册器（可选）

在 `AnalyticsModule` 中还有一个 `getGooglePlayRegistrar()` 机制，用于注册渠道特定的 Provider：

```kotlin
// commonMain/kotlin/.../di/AnalyticsModule.kt
internal expect fun getGooglePlayRegistrar(): AnalyticsProviderRegistrar?

// googlePlayMain/kotlin/.../di/AnalyticsModule.googlePlay.kt
internal actual fun getGooglePlayRegistrar(): AnalyticsProviderRegistrar? {
    return GooglePlayAnalyticsRegistrar()
}

// channelMain/kotlin/.../di/AnalyticsModule.channel.kt
internal actual fun getGooglePlayRegistrar(): AnalyticsProviderRegistrar? {
    return null
}
```

### 3. Provider 注册流程

在 `AnalyticsModule` 中，Provider 注册按以下顺序进行：

```kotlin
single<AnalyticsProviderFactory> {
    val factory = AnalyticsProviderFactory()

    // 1. 注册平台特定的 Provider（通过平台注册器）
    getAnalyticsRegistrar()?.register(factory)

    // 2. 如果是 googlePlay 渠道，注册渠道特定的 Provider
    getGooglePlayRegistrar()?.register(factory)

    factory
}
```

**注册优先级**：

1. 平台注册器（`getAnalyticsRegistrar()`）- 注册平台标准 Provider
2. 渠道注册器（`getGooglePlayRegistrar()`）- 注册渠道特定 Provider（如 Firebase）

### 4. FirebaseProvider 实现

#### 4.1 Expect 声明

在 `commonMain` 中定义 `FirebaseProvider` 的 expect 声明：

```kotlin
// commonMain/kotlin/.../provider/FirebaseProvider.kt
expect class FirebaseProvider : BaseAnalyticsProvider {
    override val name: String
    override val supportedPlatforms: Set<Platform>
    override val supportedRegions: Set<Region>
    // ... 其他方法
}
```

#### 4.2 Actual 实现

在 `googlePlayMain` 中提供实际实现（使用 GitLiveApp/firebase-kotlin-sdk）：

```kotlin
// googlePlayMain/kotlin/.../provider/FirebaseProvider.kt
actual class FirebaseProvider(...) : BaseAnalyticsProvider() {
    actual override val name = "Firebase"
    actual override val supportedPlatforms = setOf(Platform.ANDROID)
    // ... 实现
}
```

**注意**：`FirebaseProvider` 的实现位于 `googlePlayMain`，这意味着：

- 只有在 `googlePlay` 渠道编译时才会存在
- 所有平台（Android、iOS、JS、WASM）共享同一个实现
- 使用 GitLiveApp/firebase-kotlin-sdk 实现跨平台支持

### 5. 依赖配置

在 `build.gradle.kts` 中，Firebase SDK 依赖仅在 `googlePlay` 渠道添加：

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Firebase Analytics（仅在 googlePlay 渠道）
            if (project.isChannel("googlePlay")) {
                implementation("dev.gitlive:firebase-analytics:2.4.0")
            }
        }

        androidMain.dependencies {
            if (project.isChannel("googlePlay")) {
                implementation("dev.gitlive:firebase-analytics:2.4.0")
            }
        }

        iosMain.dependencies {
            if (project.isChannel("googlePlay")) {
                implementation("dev.gitlive:firebase-analytics:2.4.0")
            }
        }

        jsMain.dependencies {
            if (project.isChannel("googlePlay")) {
                implementation("dev.gitlive:firebase-analytics:2.4.0")
            }
        }

        wasmJsMain.dependencies {
            if (project.isChannel("googlePlay")) {
                implementation("dev.gitlive:firebase-analytics:2.4.0")
            }
        }
    }
}
```

## 🔑 关键设计点

### 1. 变体目录互斥机制

KMP 变体系统的目录注入机制确保了：

- **互斥性**：根据构建时的 `appChannel` 参数，只会注入**一个**渠道目录
    - `appChannel=googlePlay` → 注入 `androidGooglePlayMain`
    - `appChannel=channel` → 注入 `androidChannelMain`
- **类替换**：`{platform}{Channel}Main` 中的类会**替换** `androidMain` 中引用的同名类
- **不会同时存在**：不同渠道的目录是互斥的，不会同时生效

### 2. 渠道隔离

- **Google Play 渠道**：注册 `FirebaseProvider`，使用 Firebase Analytics
- **默认渠道（channel）**：仅注册 `ConsoleProvider`，不包含 Firebase

### 3. 平台 + 渠道组合

每个平台都可以有不同的渠道实现：

- `androidGooglePlayMain`：Android 平台的 Google Play 渠道
- `iosGooglePlayMain`：iOS 平台的 Google Play 渠道
- `jsGooglePlayMain`：JS 平台的 Google Play 渠道

### 4. 通用 Provider

- **`CommonAnalyticsRegistrar`**：注册所有平台都支持的 `ConsoleProvider`
- **`FirebaseProvider`**：仅在 `googlePlay` 渠道可用，支持 Android、iOS、JS、WASM

## 📝 使用示例

### 构建不同渠道

```bash
# 构建 Google Play 渠道（包含 Firebase）
./gradlew :core:analytics:build -PappChannel=googlePlay

# 构建默认渠道（不包含 Firebase）
./gradlew :core:analytics:build -PappChannel=channel
```

### 在代码中使用

```kotlin
// 在 AnalyticsConfig 中配置 Provider
val config = AnalyticsConfig(
    region = Region.OVERSEAS,
    enabled = true,
    providers = listOf(
        ProviderConfig(
            type = ProviderType.FIREBASE,
            enabled = true
        )
    )
)

// FirebaseProvider 会自动在 googlePlay 渠道注册
// 在 channel 渠道中，FirebaseProvider 不存在，会使用 ConsoleProvider
```

## 🎯 设计优势

1. **清晰的职责分离**：平台代码和渠道代码分离，易于维护
2. **灵活的扩展性**：可以轻松添加新的渠道（如 `umeng`、`huawei` 等）
3. **编译期优化**：不同渠道的代码在编译期分离，不会包含不必要的依赖
4. **类型安全**：使用 expect/actual 机制确保类型安全
5. **统一接口**：所有 Provider 实现统一的 `AnalyticsProvider` 接口

## 📚 相关文档

- [Analytics Framework Design](../../../docs/ANALYTICS_FRAMEWORK_DESIGN.md)
- [KMP Variant Guide](../../../docs/myhub-kmp-variant-guide.md)
- [App Build Config](../../app-build-config/README.md)

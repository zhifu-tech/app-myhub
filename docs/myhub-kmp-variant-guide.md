# MyHub KMP 变体系统使用指南

## 目录

1. [变体系统概述](#变体系统概述)
2. [变体工具函数 API](#变体工具函数-api)
3. [如何配置变体](#如何配置变体)
4. [如何配置变体特定依赖](#如何配置变体特定依赖)
5. [完整使用示例](#完整使用示例)
6. [实际项目案例](#实际项目案例)
7. [验证和调试](#验证和调试)
8. [注意事项和最佳实践](#注意事项和最佳实践)

---

## 变体系统概述

MyHub KMP 项目支持多维度变体系统，允许你根据不同的环境和级别构建不同的应用版本。

### 变体维度

当前支持的变体维度：

- **环境（Environment）**: `dev` / `prod`

  - `dev`: 开发环境，用于开发和测试
  - `prod`: 生产环境，用于正式发布

- **级别（Tier）**: `free` / `premium`

  - `free`: 免费版本，包含基础功能
  - `premium`: 高级版本，包含完整功能

- **渠道（Channel）**: `googlePlay`, `umeng`, `vivo`, `oppo`, `xiaomi`, `huawei` 等（可选）
  - `googlePlay`: Google Play 应用商店
  - `umeng`: 友盟统计（国内）
  - `vivo`: vivo 应用商店
  - `oppo`: OPPO 应用商店
  - `xiaomi`: 小米应用商店
  - `huawei`: 华为应用商店
  - 可以自定义其他渠道名称

### 变体激活

变体通过独立的 Gradle 属性激活，每个维度都是独立的。可以通过以下三种方式设置：

#### 方式 1：命令行参数（推荐用于临时构建）

```bash
# 基本构建（使用默认值：dev + free + channel）
./gradlew build

# 指定环境
./gradlew build -PappEnv=dev
./gradlew build -PappEnv=prod

# 指定级别
./gradlew build -PappTier=free
./gradlew build -PappTier=premium

# 指定渠道（可选）
./gradlew build -PappChannel=googlePlay
./gradlew build -PappChannel=umeng
./gradlew build -PappChannel=vivo

# 组合使用（所有参数独立）
./gradlew build -PappEnv=prod -PappTier=premium -PappChannel=googlePlay
./gradlew build -PappEnv=dev -PappTier=free -PappChannel=umeng
```

#### 方式 2：使用统一管理脚本（推荐用于运行应用）

项目提供了统一管理脚本 `scripts/run.sh`，支持使用 `-P` 格式参数（与 Gradle 对齐）：

```bash
# 运行桌面应用（使用默认值：debug, dev, free）
./scripts/run.sh desktop

# 运行桌面应用（release 模式）
./scripts/run.sh desktop -PbuildType=release

# 运行桌面应用（生产环境，收费版）
./scripts/run.sh desktop -PappEnv=prod -PappTier=premium

# 运行桌面应用（生产环境，收费版，Google Play 渠道）
./scripts/run.sh desktop -PappEnv=prod -PappTier=premium -PappChannel=googlePlay

# 构建并安装 Android 应用
./scripts/run.sh android -PappEnv=prod -PappTier=premium -PappChannel=googlePlay

# 构建所有模块
./scripts/run.sh build -PappEnv=prod -PappTier=premium -PappChannel=googlePlay
```

**注意：** 脚本使用 `-PbuildType=debug` 或 `-PbuildType=release` 来指定构建类型，使用 `-PappEnv`、`-PappTier`、`-PappChannel` 来指定变体参数，与 Gradle 命令的参数格式完全一致，便于统一管理。

#### 方式 3：gradle.properties 文件（推荐用于默认配置）

在项目根目录的 `gradle.properties` 文件中配置：

```properties
# 构建变体配置
appEnv=dev
appTier=free
appChannel=channel
```

这样配置后，直接运行 `./gradlew build` 就会使用这些默认值。

#### 配置优先级

配置的优先级从高到低：

1. **命令行参数** `-PappEnv=prod`（最高优先级）
2. **gradle.properties** 文件（项目级配置）
3. **~/.gradle/gradle.properties** 文件（用户级配置）
4. **代码中的默认值**（`dev`, `free`, `channel`）

**注意：** 无论是直接使用 `./gradlew` 命令还是通过 `scripts/run.sh` 脚本，都使用相同的 `-P` 格式参数，参数优先级规则一致。

**注意：** `local.properties` 文件不用于构建变体配置，它仅用于 Android SDK 路径等本地配置。如果需要个人本地配置，可以使用用户级的 `~/.gradle/gradle.properties` 文件。

**默认值：**

- `appEnv`: 默认为 `dev`
- `appTier`: 默认为 `free`
- `appChannel`: 默认为 `channel`

**注意：** 变体参数是独立的，不存在组合逻辑。每个维度单独判断和注入目录。

---

## 变体工具函数 API

MyHub KMP 插件提供了一系列工具函数，用于在 `build.gradle.kts` 中判断和获取当前变体信息。

### 基本函数

#### 获取变体信息

```kotlin
// 获取当前变体的环境类型
val env = project.getVariantEnvironment() // "dev" 或 "prod"
val env = project.getVariantEnvironment("prod") // 指定默认值

// 获取当前变体的级别类型
val tier = project.getVariantTier() // "free" 或 "premium"
val tier = project.getVariantTier("premium") // 指定默认值

// 获取当前变体的渠道类型
val channel = project.getVariantChannel() // "googlePlay", "umeng" 等，如果未指定则返回 "channel"（默认值）
```

#### 环境判断函数

```kotlin
// 判断是否为开发环境
if (project.isDev()) {
    // dev 环境特定逻辑
    implementation(compose.components.uiToolingPreview)
}

// 判断是否为生产环境
if (project.isProd()) {
    // prod 环境特定逻辑
    implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
}
```

#### 级别判断函数

```kotlin
// 判断是否为免费级别
if (project.isFree()) {
    // free 级别特定逻辑
}

// 判断是否为高级级别
if (project.isPremium()) {
    // premium 级别特定逻辑
    implementation("com.example:premium-features:1.0.0")
}
```

#### 渠道判断函数

```kotlin
// 判断是否为指定渠道
if (project.isChannel("googlePlay")) {
    // Google Play 渠道特定逻辑
    implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
}

// 判断是否为 Google Play 渠道
if (project.isGooglePlay()) {
    // Google Play 渠道特定逻辑
    implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.6.1")
}

// 判断是否为 Umeng 渠道
if (project.isUmeng()) {
    // Umeng 渠道特定逻辑
    implementation("com.umeng:umeng-common:9.5.0")
}

// 判断是否为 Vivo 渠道
if (project.isVivo()) {
    // Vivo 渠道特定逻辑
}

// 判断是否为 OPPO 渠道
if (project.isOppo()) {
    // OPPO 渠道特定逻辑
}

// 判断是否为小米渠道
if (project.isXiaomi()) {
    // 小米渠道特定逻辑
}

// 判断是否为华为渠道
if (project.isHuawei()) {
    // 华为渠道特定逻辑
}
```

### 完整使用示例

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // 标准依赖（所有变体）
            implementation(projects.core.platform)

            // dev 环境特定依赖
            if (project.isDev()) {
                implementation(compose.components.uiToolingPreview)
                implementation("com.example:dev-tools:1.0.0")
            }

            // prod 环境特定依赖
            if (project.isProd()) {
                implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
            }

            // premium 级别特定依赖
            if (project.isPremium()) {
                implementation("com.example:premium-features:1.0.0")
            }

            // 渠道判断：Google Play
            if (project.isGooglePlay()) {
                implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
            }

            // 多个条件组合判断（如果需要）
            if (project.isProd() && project.isPremium() && project.isGooglePlay()) {
                implementation("com.google.firebase:firebase-crashlytics-ktx:18.6.1")
            }
        }
    }
}
```

### 向后兼容性

为了保持向后兼容，旧版本的函数名仍然可用，但已标记为 `@Deprecated`：

- `isDevVariant()` → 使用 `isDev()` 替代
- `isProdVariant()` → 使用 `isProd()` 替代
- `isFreeVariant()` → 使用 `isFree()` 替代
- `isPremiumVariant()` → 使用 `isPremium()` 替代

建议在新代码中使用新的函数名，它们更简洁且语义更清晰。

---

## 如何配置变体

### 1. 源集目录结构

变体系统通过目录注入机制工作。你可以在模块的 `src` 目录下创建变体特定的目录：

```
module-name/
├── src/
│   ├── commonMain/              # 所有变体共享代码
│   ├── devMain/                 # dev 环境代码（目录注入）
│   ├── prodMain/                # prod 环境代码（目录注入）
│   ├── freeMain/                # free 级别代码（目录注入）
│   ├── premiumMain/             # premium 级别代码（目录注入）
│   ├── googlePlayMain/          # Google Play 渠道代码（目录注入，如果指定了渠道）
│   ├── umengMain/               # Umeng 渠道代码（目录注入，如果指定了渠道）
│   ├── androidMain/             # Android 平台标准代码
│   ├── androidDevMain/          # Android dev 环境代码（目录注入）
│   ├── androidProdMain/         # Android prod 环境代码（目录注入）
│   ├── androidFreeMain/         # Android free 级别代码（目录注入）
│   ├── androidPremiumMain/      # Android premium 级别代码（目录注入）
│   ├── androidGooglePlayMain/   # Android Google Play 渠道代码（目录注入，如果指定了渠道）
│   ├── androidUmengMain/        # Android Umeng 渠道代码（目录注入，如果指定了渠道）
│   ├── iosMain/                 # iOS 平台标准代码
│   ├── iosDevMain/              # iOS dev 环境代码（目录注入）
│   ├── iosProdMain/             # iOS prod 环境代码（目录注入）
│   ├── iosFreeMain/             # iOS free 级别代码（目录注入）
│   ├── iosPremiumMain/          # iOS premium 级别代码（目录注入）
│   ├── jvmMain/                 # JVM/Desktop 平台标准代码
│   ├── jvmDevMain/              # JVM dev 环境代码（目录注入）
│   ├── jvmProdMain/             # JVM prod 环境代码（目录注入）
│   ├── jvmFreeMain/             # JVM free 级别代码（目录注入）
│   ├── jvmPremiumMain/          # JVM premium 级别代码（目录注入）
│   ├── jsMain/                  # JavaScript 平台标准代码
│   ├── jsDevMain/               # JS dev 环境代码（目录注入）
│   ├── jsProdMain/              # JS prod 环境代码（目录注入）
│   ├── jsFreeMain/              # JS free 级别代码（目录注入）
│   ├── jsPremiumMain/           # JS premium 级别代码（目录注入）
│   ├── wasmJsMain/              # WebAssembly 平台标准代码
│   ├── wasmJsDevMain/           # WASM dev 环境代码（目录注入）
│   ├── wasmJsProdMain/          # WASM prod 环境代码（目录注入）
│   ├── wasmJsFreeMain/          # WASM free 级别代码（目录注入）
│   ├── wasmJsPremiumMain/       # WASM premium 级别代码（目录注入）
│   └── wasmJsGooglePlayMain/    # WASM Google Play 渠道代码（目录注入，如果指定了渠道）
```

### 2. 目录注入机制

插件会自动根据当前激活的变体注入相应的目录。所有变体目录都是平级的，位于 `src/` 目录下：

**通用变体目录（commonMain）：**

- **环境维度**：`devMain`, `prodMain`
- **级别维度**：`freeMain`, `premiumMain`
- **渠道维度**：`googlePlayMain`, `umengMain`, `vivoMain`, `oppoMain`, `xiaomiMain`, `huaweiMain` 等（如果指定了渠道）

**平台特定变体目录（androidMain, iosMain, jvmMain, jsMain, wasmJsMain）：**

- **平台+环境**：`androidDevMain`, `androidProdMain`, `iosDevMain`, `iosProdMain`, `jvmDevMain`, `jvmProdMain`, `jsDevMain`, `jsProdMain`, `wasmJsDevMain`, `wasmJsProdMain`
- **平台+级别**：`androidFreeMain`, `androidPremiumMain`, `iosFreeMain`, `iosPremiumMain`, `jvmFreeMain`, `jvmPremiumMain`, `jsFreeMain`, `jsPremiumMain`, `wasmJsFreeMain`, `wasmJsPremiumMain`
- **平台+渠道**：`androidGooglePlayMain`, `androidUmengMain`, `iosGooglePlayMain` 等（如果指定了渠道）

**注意：**

- 平台特定的变体目录是平级的，不是嵌套在平台目录内。例如：
  - ✅ 正确：`src/androidDevMain/`
  - ❌ 错误：`src/androidMain/devMain/`
- 每个变体维度都是独立的，不存在组合目录（如 `devFreeMain`, `androidDevFreeMain` 等）
- 渠道目录只有在指定了 `appChannel` 属性时才会被注入
- 变体参数通过独立的 Gradle 属性设置：`appEnv`, `appTier`, `appChannel`

### 3. 代码组织示例

```kotlin
// src/commonMain/kotlin/AnalyticsManager.kt
// 所有变体共享的基础代码
class AnalyticsManager {
    fun initialize() {
        // 通用初始化逻辑
    }
}

// src/devMain/kotlin/AnalyticsManager.kt
// dev 环境特定的实现
fun AnalyticsManager.setupDevMode() {
    // 开发环境特定的设置
    enableDebugLogging()
}

// src/premiumMain/kotlin/AnalyticsManager.kt
// premium 级别特定的实现
fun AnalyticsManager.enablePremiumFeatures() {
    // Premium 功能启用逻辑
}

// src/googlePlayMain/kotlin/AnalyticsManager.kt
// Google Play 渠道特定的实现
fun AnalyticsManager.setupGooglePlay() {
    // Google Play 渠道特定的逻辑
    initializeFirebase()
}
```

---

## 如何配置变体特定依赖

### 1. 基本语法（推荐方式）

在模块的 `build.gradle.kts` 文件中，直接在 `dependencies` 块中使用条件判断：

```kotlin
plugins {
    alias(libs.plugins.myhub.kmp)
    // ... 其他插件
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 标准依赖（所有变体都会包含）
            implementation("com.example:common-lib:1.0.0")

            // 变体特定依赖（使用条件判断）
            if (project.isDev()) {
                implementation("com.example:dev-tools:1.0.0")
            }

            if (project.isPremium()) {
                implementation("com.example:premium-features:1.0.0")
            }
        }
    }
}
```

这种方式简单直接，推荐使用。

### 2. 环境特定依赖（dev/prod）

为特定环境添加依赖：

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // 标准依赖
            implementation("com.example:common-lib:1.0.0")

            // dev 环境专用依赖
            if (project.isDev()) {
                implementation("com.example:dev-tools:1.0.0")
                implementation("com.example:debug-helper:2.0.0")
            }

            // prod 环境专用依赖
            if (project.isProd()) {
                implementation("com.example:prod-analytics:1.0.0")
            }
        }

        androidMain.dependencies {
            if (project.isDev()) {
                implementation("com.example:android-dev:1.0.0")
            }

            if (project.isProd()) {
                implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
            }
        }

        jvmMain.dependencies {
            if (project.isDev()) {
                implementation("com.example:jvm-dev-console:1.0.0")
            }
        }
    }
}
```

### 3. 级别特定依赖（free/premium）

为特定级别添加依赖：

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // free 版本依赖
            if (project.isFree()) {
                implementation("com.example:free-features:1.0.0")
            }

            // premium 版本依赖
            if (project.isPremium()) {
                implementation("com.example:premium-features:2.0.0")
                implementation("com.example:advanced-tools:1.5.0")
            }
        }

        androidMain.dependencies {
            if (project.isPremium()) {
                implementation("com.example:premium-android:1.0.0")
            }
        }

        jvmMain.dependencies {
            if (project.isPremium()) {
                implementation("com.example:jvm-premium-tools:1.0.0")
            }
        }
    }
}
```

### 4. 多条件判断

可以组合多个条件来判断（如果需要）：

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // 多个条件组合判断（如果需要）
            if (project.isDev() && project.isFree()) {
                implementation("com.example:dev-free-only:1.0.0")
            }

            if (project.isProd() && project.isPremium()) {
                implementation("com.example:prod-premium-only:1.0.0")
                implementation("com.example:enterprise-analytics:1.0.0")
            }
        }

        androidMain.dependencies {
            // 组合判断：生产环境 + Premium + Google Play
            if (project.isProd() && project.isPremium() && project.isGooglePlay()) {
                implementation("com.example:android-prod-premium-googleplay:1.0.0")
            }
        }
    }
}
```

**注意**：虽然可以组合多个条件，但目录注入是独立的。例如，如果设置了 `-PappEnv=dev -PappTier=free`，会注入 `src/devMain/` 和 `src/freeMain/`，但不会注入 `src/devFreeMain/`。

### 5. 支持的源集

可以在以下源集中配置变体依赖：

- `commonMain` - 所有平台的通用代码
- `androidMain` - Android 平台特定代码
- `iosMain` - iOS 平台特定代码
- `jvmMain` - JVM/Desktop 平台特定代码
- `jsMain` - JavaScript 平台特定代码
- `wasmJsMain` - WebAssembly 平台特定代码
- `commonTest` - 通用测试代码

### 6. 支持的依赖类型

可以使用所有标准的 Gradle 依赖 API：

- `implementation(...)` - 实现依赖
- `api(...)` - API 依赖
- `compileOnly(...)` - 编译时依赖
- `runtimeOnly(...)` - 运行时依赖
- `projects.core.module` - 项目模块依赖
- `libs.xxx.xxx` - 版本目录依赖

---

## 完整使用示例

### 示例 1：最小配置

最简单的变体依赖配置：

```kotlin
plugins {
    alias(libs.plugins.myhub.kmp)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.example"
    }

    sourceSets {
        commonMain.dependencies {
            // 标准依赖
            implementation(projects.core.logger)

            // 只在 dev 环境添加开发工具
            if (project.isDev()) {
                implementation("com.example:dev-tools:1.0.0")
            }

            // 只在 premium 版本添加高级功能
            if (project.isPremium()) {
                implementation("com.example:premium-features:1.0.0")
            }
        }
    }
}
```

### 示例 2：完整配置

完整的模块配置示例：

```kotlin
plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.analytics"
    }

    sourceSets {
        // ========== 标准依赖（所有变体都会包含）==========
        commonMain.dependencies {
            // 项目模块依赖
            implementation(projects.core.platform)
            implementation(projects.core.logger)

            // Kotlinx Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Kotlinx Serialization
            implementation(libs.kotlinx.serialization.json)

            // Koin（用于 DI 模块）
            implementation(libs.koin.core)

            // ========== 1. 环境特定依赖 ==========

            // dev 环境：添加开发工具和调试库
            if (project.isDev()) {
                implementation("com.jakewharton.timber:timber:5.0.1")
                implementation("com.example:analytics-debugger:2.0.0")
            }

            // prod 环境：添加生产环境的分析库
            if (project.isProd()) {
                implementation("com.example:prod-analytics-service:1.0.0")
                implementation("com.example:production-monitoring:2.0.0")
            }

            // ========== 2. 级别特定依赖 ==========

            // free 版本：基础功能依赖
            if (project.isFree()) {
                implementation("com.example:basic-analytics:1.0.0")
            }

            // premium 版本：高级功能依赖
            if (project.isPremium()) {
                implementation("com.example:advanced-analytics:2.0.0")
                implementation("com.example:premium-features:1.5.0")
                implementation("com.example:ab-testing-sdk:1.0.0")
            }

            // ========== 3. 渠道特定依赖 ==========

            // Google Play 渠道
            if (project.isGooglePlay()) {
                implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
            }

            // Umeng 渠道
            if (project.isUmeng()) {
                implementation("com.umeng:umeng-common:9.5.0")
            }
        }

        androidMain.dependencies {
            if (project.isDev()) {
                // Android 开发环境特定库
                // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
            }

            if (project.isProd()) {
                // Android 生产环境特定库
                implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
                implementation("com.google.firebase:firebase-crashlytics-ktx:18.6.1")
            }

            if (project.isPremium()) {
                implementation("com.example:android-premium-analytics:1.0.0")
            }

            if (project.isDev() && project.isPremium()) {
                implementation("com.example:android-dev-premium:1.0.0")
            }

            if (project.isProd() && project.isPremium()) {
                implementation("com.example:android-prod-premium:1.0.0")
            }
        }

        jvmMain.dependencies {
            if (project.isDev()) {
                implementation("com.example:jvm-dev-console:1.0.0")
            }

            if (project.isPremium()) {
                implementation("com.example:jvm-premium-tools:1.0.0")
            }

            if (project.isProd() && project.isPremium()) {
                implementation("com.example:jvm-enterprise-analytics:1.0.0")
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}
```

### 示例 3：使用项目模块依赖

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            if (project.isPremium()) {
                // Premium 版本包含额外的功能模块
                implementation(projects.feature.premiumAnalytics)
                implementation(projects.feature.advancedReporting)
            }

            if (project.isDev()) {
                // 开发环境包含测试工具模块
                implementation(projects.core.testUtils)
                implementation(projects.core.analyticsTestUtils)
            }
        }
    }
}
```

### 示例 4：使用版本目录（libs）

```kotlin
kotlin {
    sourceSets {
        androidMain.dependencies {
            if (project.isProd()) {
                // 使用 libs 版本目录
                implementation(libs.firebase.analytics)
                implementation(libs.firebase.crashlytics)
            }
        }

        commonMain.dependencies {
            if (project.isPremium()) {
                // 使用 libs 版本目录
                implementation(libs.advanced.analytics)
            }
        }
    }
}
```

---

## 实际项目案例

### 案例场景

假设我们有一个 `core/analytics` 模块，需要根据不同的变体添加不同的依赖：

- **dev 环境**：需要添加开发工具和调试库
- **prod 环境**：需要添加生产环境的分析库（Firebase Analytics）
- **premium 级别**：需要添加高级分析功能库
- **Google Play 渠道**：需要添加 Firebase 服务

### 依赖应用示例

#### 场景 1：使用 dev + free 构建

```bash
./gradlew :core:analytics:build -PappEnv=dev -PappTier=free
```

**实际应用的依赖：**

- ✅ 标准依赖（所有变体）
- ✅ dev 环境依赖
- ✅ free 级别依赖

**commonMain 源集最终包含：**

- `projects.core.platform`
- `projects.core.logger`
- `libs.kotlinx.coroutines.core`
- `libs.kotlinx.serialization.json`
- `libs.koin.core`
- `com.jakewharton.timber:timber:5.0.1` (dev)
- `com.example:analytics-debugger:2.0.0` (dev)
- `com.example:basic-analytics:1.0.0` (free)

#### 场景 2：使用 prod + premium + Google Play 构建

```bash
./gradlew :core:analytics:build -PappEnv=prod -PappTier=premium -PappChannel=googlePlay
```

**实际应用的依赖：**

- ✅ 标准依赖（所有变体）
- ✅ prod 环境依赖
- ✅ premium 级别依赖
- ✅ Google Play 渠道依赖

**commonMain 源集最终包含：**

- `projects.core.platform`
- `projects.core.logger`
- `libs.kotlinx.coroutines.core`
- `libs.kotlinx.serialization.json`
- `libs.koin.core`
- `com.example:prod-analytics-service:1.0.0` (prod)
- `com.example:production-monitoring:2.0.0` (prod)
- `com.example:advanced-analytics:2.0.0` (premium)
- `com.example:premium-features:1.5.0` (premium)
- `com.example:ab-testing-sdk:1.0.0` (premium)

**androidMain 源集额外包含：**

- `com.google.firebase:firebase-analytics-ktx:21.5.0` (prod)
- `com.google.firebase:firebase-crashlytics-ktx:18.6.1` (prod)
- `com.example:android-premium-analytics:1.0.0` (premium)
- `com.google.firebase:firebase-messaging-ktx:23.4.0` (googlePlay)

### 实际项目依赖示例

#### 示例 1：Firebase Analytics（仅生产环境）

```kotlin
kotlin {
    sourceSets {
        androidMain.dependencies {
            if (project.isProd()) {
                // Firebase Analytics 仅在生产环境使用
                implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
            }
        }
    }
}
```

#### 示例 2：Crashlytics（仅生产环境）

```kotlin
kotlin {
    sourceSets {
        androidMain.dependencies {
            if (project.isProd()) {
                // Crashlytics 仅在生产环境使用
                implementation("com.google.firebase:firebase-crashlytics-ktx:18.6.1")
            }
        }
    }
}
```

#### 示例 3：开发环境的 Mock 库

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            if (project.isDev()) {
                // 开发环境使用 Mock 数据
                implementation("com.example:mock-analytics:1.0.0")
            }
        }
    }
}
```

#### 示例 4：Premium 功能的 A/B 测试库

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            if (project.isPremium()) {
                // Premium 版本支持 A/B 测试
                implementation("com.example:ab-testing-sdk:1.0.0")
            }
        }
    }
}
```

#### 示例 5：Compose Preview（仅开发环境）

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            if (project.isDev()) {
                // Preview 支持仅在开发环境
                implementation(compose.components.uiToolingPreview)
            }
        }
    }
}
```

#### 示例 6：Google Play 渠道 - Firebase（仅 Google Play）

```kotlin
kotlin {
    sourceSets {
        androidMain.dependencies {
            if (project.isGooglePlay()) {
                // Firebase 服务仅用于 Google Play 渠道
                implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
                implementation("com.google.firebase:firebase-crashlytics-ktx:18.6.1")
                implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")
            }
        }
    }
}
```

#### 示例 7：Umeng 渠道 - 友盟统计（仅 Umeng）

```kotlin
kotlin {
    sourceSets {
        androidMain.dependencies {
            if (project.isUmeng()) {
                // 友盟统计仅用于国内渠道
                implementation("com.umeng:umeng-common:9.5.0")
                implementation("com.umeng:umeng-asms:1.6.3")
            }
        }
    }
}
```

#### 示例 8：组合判断 - 生产环境 + Premium + Google Play

```kotlin
kotlin {
    sourceSets {
        androidMain.dependencies {
            // 仅在生产环境 + Premium + Google Play 渠道使用 Firebase
            if (project.isProd() && project.isPremium() && project.isGooglePlay()) {
                implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
                implementation("com.google.firebase:firebase-crashlytics-ktx:18.6.1")
            }
        }
    }
}
```

---

## 验证和调试

### 查看依赖树

```bash
# 查看 dev + free 的依赖
./gradlew :core:analytics:dependencies --configuration commonMainCompileClasspath -PappEnv=dev -PappTier=free

# 查看 prod + premium 的依赖
./gradlew :core:analytics:dependencies --configuration commonMainCompileClasspath -PappEnv=prod -PappTier=premium

# 查看 prod + premium + Google Play 的依赖
./gradlew :core:analytics:dependencies --configuration commonMainCompileClasspath -PappEnv=prod -PappTier=premium -PappChannel=googlePlay

# 查看 Android 平台的依赖
./gradlew :core:analytics:dependencies --configuration androidMainCompileClasspath -PappEnv=prod -PappTier=premium -PappChannel=googlePlay
```

### 构建不同变体

```bash
# 使用默认值构建（dev + free）
./gradlew :core:analytics:build

# 指定环境
./gradlew :core:analytics:build -PappEnv=prod

# 指定级别
./gradlew :core:analytics:build -PappTier=premium

# 指定渠道
./gradlew :core:analytics:build -PappChannel=googlePlay

# 组合使用
./gradlew :core:analytics:build -PappEnv=prod -PappTier=premium -PappChannel=googlePlay
./gradlew :core:analytics:build -PappEnv=dev -PappTier=free -PappChannel=umeng
```

### 在代码中验证

```kotlin
// 在 commonMain 中
class AnalyticsManager {
    fun initialize() {
        // 这些依赖在所有变体中可用
        val logger = getLogger()
        val coroutineScope = CoroutineScope(Dispatchers.Default)
    }
}

// 在 devMain 中（dev 环境特定代码）
// 注意：devMain 是目录，不是源集
// 代码可以通过条件编译或 expect/actual 模式使用变体特定依赖

// 在 premiumMain 中（premium 级别特定代码）
// Premium 特定的功能实现
```

---

## 注意事项和最佳实践

### 1. 变体参数独立设置

变体参数通过独立的 Gradle 属性设置，每个维度都是独立的：

```bash
# ✅ 正确：独立设置每个维度
./gradlew build -PappEnv=prod -PappTier=premium -PappChannel=googlePlay

# ❌ 错误：不再使用 appVariant
./gradlew build -PappVariant=prodPremium
```

变体参数判断不区分大小写，但建议使用标准格式（如 `dev`, `prod`, `free`, `premium`, `googlePlay`）：

```kotlin
// ✅ 推荐：使用独立的判断函数
if (project.isDev() && project.isFree()) { ... }

// ✅ 也可以：使用 getVariantEnvironment() 和 getVariantTier()
if (project.getVariantEnvironment() == "dev" && project.getVariantTier() == "free") { ... }
```

**重要**：每个变体维度都是独立的，不存在组合逻辑。目录注入和依赖判断都是基于单个维度进行的。

### 2. 依赖不会重复添加

即使多个条件都配置了相同的依赖，Gradle 会自动去重，无需担心：

```kotlin
if (project.isDev()) {
    implementation("com.example:lib:1.0.0")
}

if (project.isPremium()) {
    implementation("com.example:lib:1.0.0")  // 不会重复添加
}
```

### 3. 源集名称必须正确

确保源集名称与 Kotlin Multiplatform 的标准源集名称匹配：

- ✅ `commonMain`, `androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain`
- ✅ `commonTest`, `androidTest`, `iosTest`, `jvmTest`, `jsTest`, `wasmJsTest`
- ❌ `main`, `test`, `common`, `android`（这些是错误的）

### 4. 项目依赖支持

可以使用 `projects.core.module` 格式添加项目模块依赖：

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            if (project.isPremium()) {
                implementation(projects.feature.premiumAnalytics)
            }
        }
    }
}
```

### 5. 类型安全

在配置块中可以使用所有标准的 Gradle 依赖 API：

- `implementation(...)` - 实现依赖
- `api(...)` - API 依赖
- `compileOnly(...)` - 编译时依赖
- `runtimeOnly(...)` - 运行时依赖
- `projects.core.module` - 项目模块依赖
- `libs.xxx.xxx` - 版本目录依赖

### 6. 平台特定配置

可以为不同平台配置不同的变体依赖：

- `commonMain` - 所有平台
- `androidMain` - Android 平台
- `iosMain` - iOS 平台
- `jvmMain` - JVM/Desktop 平台
- `jsMain` - JavaScript 平台
- `wasmJsMain` - WebAssembly 平台

### 7. 工作原理

1. **变体激活**：通过独立的 Gradle 属性设置变体维度：
   - `appEnv`: 环境（`dev` 或 `prod`，默认 `dev`）
   - `appTier`: 级别（`free` 或 `premium`，默认 `free`）
   - `appChannel`: 渠道（如 `googlePlay`, `umeng`，可选，默认 `channel`）
2. **条件判断**：在 `build.gradle.kts` 中使用 `project.isDev()`, `project.isPremium()` 等函数判断当前变体
3. **依赖应用**：根据条件判断结果，只有匹配当前变体的依赖才会被添加到对应的源集
4. **目录注入**：插件自动根据当前变体注入相应的源集目录（如 `src/devMain`, `src/premiumMain`）

### 8. 完整项目结构示例

```
core/analytics/
├── build.gradle.kts          # 包含变体依赖配置
├── src/
│   ├── commonMain/           # 所有变体共享代码
│   ├── devMain/              # dev 环境代码（目录注入）
│   ├── prodMain/             # prod 环境代码（目录注入）
│   ├── freeMain/             # free 级别代码（目录注入）
│   ├── premiumMain/          # premium 级别代码（目录注入）
│   ├── googlePlayMain/       # Google Play 渠道代码（目录注入，如果指定了渠道）
│   ├── umengMain/            # Umeng 渠道代码（目录注入，如果指定了渠道）
│   └── androidMain/          # Android 平台标准代码
│   ├── androidDevMain/       # Android dev 环境代码（目录注入）
│   ├── androidProdMain/      # Android prod 环境代码（目录注入）
│   ├── androidFreeMain/      # Android free 级别代码（目录注入）
│   ├── androidPremiumMain/   # Android premium 级别代码（目录注入）
│   ├── androidGooglePlayMain/# Android Google Play 渠道代码（目录注入，如果指定了渠道）
│   └── androidUmengMain/     # Android Umeng 渠道代码（目录注入，如果指定了渠道）
```

### 9. 最佳实践

1. **使用条件判断**：直接在 `dependencies` 块中使用 `if (project.isDev())` 等条件判断，简单直接
2. **分离关注点**：将标准依赖和变体依赖分开配置，保持清晰
3. **使用版本目录**：优先使用 `libs` 版本目录管理依赖版本
4. **文档化**：在代码中添加注释说明为什么某个依赖只在特定变体中
5. **测试所有变体**：确保所有变体都能正常构建和运行
6. **避免过度配置**：只在必要时使用变体特定依赖，避免不必要的复杂性
7. **使用新的 API**：优先使用 `isDev()`, `isProd()`, `isFree()`, `isPremium()` 等新函数，而不是已废弃的 `isXxxVariant()` 函数

---

## 总结

MyHub KMP 变体系统提供了强大的多维度变体支持，允许你：

1. **通过目录注入**：根据变体组织代码结构
2. **通过依赖配置**：根据变体添加不同的依赖
3. **灵活组合**：支持环境、级别和完整变体三个维度的组合

这样，你就可以根据不同的变体灵活地配置代码和依赖，而不影响其他变体的构建，大大提高了项目的可维护性和灵活性。

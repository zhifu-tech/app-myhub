# Logger 模块使用指南

本文档提供 MyHub Logger 模块的详细使用指南，包括配置、基本用法、高级用法和最佳实践。

## 📋 目录

1. [快速开始](#1-快速开始)
2. [配置](#2-配置)
3. [基本用法](#3-基本用法)
4. [高级用法](#4-高级用法)
5. [平台特定配置](#5-平台特定配置)
6. [最佳实践](#6-最佳实践)
7. [常见问题](#7-常见问题)

---

## 1. 快速开始

### 1.1 添加依赖

在 `build.gradle.kts` 中添加依赖：

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.logging)
                implementation(libs.koin.core)
            }
        }
        
        jvmMain {
            dependencies {
                implementation(libs.slf4j.api)
                implementation(libs.slf4j.simple)
            }
        }
    }
}
```

### 1.2 配置 Koin

在应用启动时配置 Logger 模块：

```kotlin
import org.koin.core.context.startKoin
import tech.zhifu.app.myhub.logger.LoggerConfig
import tech.zhifu.app.myhub.logger.di.loggerModule

fun main() {
    startKoin {
        modules(
            loggerModule {
                LoggerConfig(
                    appName = "MyHub",
                    useAndroidLogger = true // Android 平台专用
                )
            }
        )
    }
}
```

### 1.3 创建 Logger 实例

```kotlin
import tech.zhifu.app.myhub.logger.logger

// 方式一：使用默认 Logger
val logger = logger()

// 方式二：使用标签创建 Logger
val moduleLogger = logger("MyModule")
val componentLogger = logger("MyModule", "Component")
```

### 1.4 记录日志

```kotlin
val logger = logger("MyModule")

logger.info { "Application started" }
logger.debug { "Loading data..." }
logger.error { "Operation failed" }
```

---

## 2. 配置

### 2.1 LoggerConfig

`LoggerConfig` 是日志配置类，包含以下属性：

```kotlin
data class LoggerConfig(
    val appName: String,                    // 应用名称
    val useAndroidLogger: Boolean = true    // Android 平台专用：是否使用 Android Logcat
)
```

### 2.2 配置方式

#### 方式一：通过 Koin 配置（推荐）

```kotlin
startKoin {
    modules(
        loggerModule {
            LoggerConfig(
                appName = "MyHub",
                useAndroidLogger = true
            )
        }
    )
}
```

#### 方式二：默认配置

如果未配置 Koin，Logger 模块会使用默认配置：

```kotlin
LoggerConfig("ZhifuTech")  // 默认应用名称
```

### 2.3 配置说明

- **appName**：应用名称，用于生成 Logger 名称
    - 如果使用标签创建 Logger，名称格式为：`appName:tag1:tag2`
    - 如果不使用标签，Logger 名称为：`appName`

- **useAndroidLogger**（Android 平台专用）：
    - `true`：使用 Android Logcat 输出日志
    - `false`：使用 kotlin-logging 的默认实现
    - 需要添加 `kotlin-logging-android` 依赖（在应用层配置）

---

## 3. 基本用法

### 3.1 创建 Logger 实例

#### 使用默认 Logger

```kotlin
import tech.zhifu.app.myhub.logger.logger

val logger = logger()
logger.info { "Using default logger" }
```

#### 使用标签创建 Logger

```kotlin
// 单个标签
val moduleLogger = logger("UserModule")

// 多个标签
val componentLogger = logger("UserModule", "Authentication")
// Logger 名称：MyHub:UserModule:Authentication
```

#### 使用顶层属性

```kotlin
import tech.zhifu.app.myhub.logger.logger

logger.info { "Using top-level logger property" }
```

### 3.2 记录不同级别的日志

Logger 支持五个日志级别（按优先级从低到高）：

```kotlin
val logger = logger("MyModule")

// TRACE - 最详细的日志信息，用于深度调试
logger.trace { "Trace message" }

// DEBUG - 调试信息，开发阶段的主要工具
logger.debug { "Debug message" }

// INFO - 信息性消息，记录重要事件和状态变化
logger.info { "Info message" }

// WARN - 警告信息，记录潜在问题
logger.warn { "Warning message" }

// ERROR - 错误信息，记录严重问题
logger.error { "Error message" }
```

### 3.3 记录异常

使用带异常参数的日志方法：

```kotlin
try {
    // 可能抛出异常的操作
    performOperation()
} catch (e: Exception) {
    logger.error(e) { "Operation failed" }
}
```

### 3.4 使用标记（Marker）

标记用于对日志进行分类和过滤：

```kotlin
// 使用字符串标记
logger.info("DATABASE") { "Database operation completed" }
logger.error("NETWORK") { "Network request failed" }

// 使用 Marker 对象
import io.github.oshai.kotlinlogging.KMarkerFactory

val securityMarker = KMarkerFactory.getMarker("SECURITY")
logger.warn(securityMarker) { "Security warning" }
```

### 3.5 检查日志级别

对于昂贵的操作，先检查日志级别是否启用：

```kotlin
if (logger.isDebugEnabled()) {
    val expensiveData = computeExpensiveData()
    logger.debug { "Data: $expensiveData" }
}
```

---

## 4. 高级用法

### 4.1 延迟求值

Logger 使用 lambda 表达式实现延迟求值，避免不必要的字符串拼接：

```kotlin
// ✅ 推荐：使用 lambda，仅在日志级别启用时执行
logger.debug { "User: $user, Action: $action" }

// ❌ 不推荐：直接字符串拼接，即使日志级别被禁用也会执行
logger.debug("User: $user, Action: $action")  // 编译错误：Logger 不支持此方法
```

### 4.2 扩展函数

Logger 提供了便捷的扩展函数，简化常见用法：

```kotlin
// 只传递消息
logger.info { "Simple message" }

// 传递异常和消息
logger.error(exception) { "Error occurred" }

// 传递标记和消息
logger.debug("DATABASE") { "Database query" }

// 完整参数（标记、异常、消息）
logger.warn(marker, exception) { "Full parameters" }
```

### 4.3 在 ViewModel 中使用

```kotlin
import androidx.lifecycle.ViewModel
import tech.zhifu.app.myhub.logger.logger

class MyViewModel : ViewModel() {
    private val logger = logger("MyViewModel")
    
    fun loadData() {
        logger.info { "Loading data..." }
        try {
            // 加载数据
            val data = fetchData()
            logger.debug { "Data loaded: ${data.size} items" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load data" }
        }
    }
}
```

### 4.4 在 Repository 中使用

```kotlin
import tech.zhifu.app.myhub.logger.logger

class UserRepository {
    private val logger = logger("UserRepository")
    
    suspend fun getUser(id: String): User? {
        logger.debug("DATABASE") { "Fetching user: $id" }
        return try {
            val user = database.getUser(id)
            logger.info("DATABASE") { "User fetched: $id" }
            user
        } catch (e: Exception) {
            logger.error("DATABASE", e) { "Failed to fetch user: $id" }
            null
        }
    }
}
```

### 4.5 在 Composable 中使用

```kotlin
import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.logger.logger

@Composable
fun MyScreen() {
    val logger = remember { logger("MyScreen") }
    
    LaunchedEffect(Unit) {
        logger.info { "Screen composed" }
    }
    
    // UI 代码
}
```

---

## 5. 平台特定配置

### 5.1 Android 平台

#### 启用 Android Logcat

在 `LoggerConfig` 中设置 `useAndroidLogger = true`：

```kotlin
loggerModule {
    LoggerConfig(
        appName = "MyHub",
        useAndroidLogger = true  // 启用 Android Logcat
    )
}
```

#### 添加依赖

在应用层的 `build.gradle.kts` 中添加：

```kotlin
dependencies {
    implementation("io.github.oshai:kotlin-logging-android:5.1.0")
}
```

#### Android 日志输出

日志会输出到 Android Logcat，可以通过 `adb logcat` 查看：

```bash
adb logcat | grep MyHub
```

### 5.2 JVM 平台

#### 添加 SLF4J 实现

在 `build.gradle.kts` 中添加：

```kotlin
jvmMain {
    dependencies {
        implementation(libs.slf4j.api)
        implementation(libs.slf4j.simple)  // 或使用其他实现（Logback、Log4j2 等）
    }
}
```

#### JVM 日志输出

日志会输出到控制台（使用 `slf4j-simple`）或配置的日志系统。

### 5.3 iOS 平台

#### iOS 配置说明

iOS 平台使用 `kotlin-logging` 的默认实现，无需特殊配置。

#### iOS 日志输出

日志会输出到 Xcode Console。

### 5.4 Web 平台

#### Web 配置说明

Web 平台使用浏览器 Console API，无需特殊配置。

#### Web 日志输出

日志会输出到浏览器开发者工具的 Console。

---

## 6. 最佳实践

### 6.1 使用延迟求值

始终使用 lambda 表达式传递消息：

```kotlin
// ✅ 推荐
logger.debug { "User: $user, Action: $action" }

// ❌ 不推荐（编译错误）
logger.debug("User: $user, Action: $action")
```

### 6.2 检查日志级别

对于昂贵的操作，先检查日志级别：

```kotlin
if (logger.isDebugEnabled()) {
    val expensiveData = computeExpensiveData()
    logger.debug { "Data: $expensiveData" }
}
```

### 6.3 使用有意义的标签

创建 Logger 时使用有意义的标签，便于日志过滤和查找：

```kotlin
// ✅ 推荐：使用有意义的标签
val logger = logger("UserRepository", "Authentication")

// ❌ 不推荐：使用无意义的标签
val logger = logger("A", "B")
```

### 6.4 记录异常堆栈

记录异常时使用带异常参数的版本：

```kotlin
// ✅ 推荐：记录异常堆栈
logger.error(exception) { "Operation failed" }

// ❌ 不推荐：只记录消息
logger.error { "Operation failed: ${exception.message}" }
```

### 6.5 使用标记分类日志

使用标记对日志进行分类，便于过滤和分析：

```kotlin
logger.info("DATABASE") { "Database operation" }
logger.info("NETWORK") { "Network request" }
logger.info("UI") { "User interaction" }
```

### 6.6 生产环境配置

在生产环境中禁用 TRACE 和 DEBUG 级别，避免性能问题和敏感信息泄露：

```kotlin
// 生产环境配置示例（需要在日志框架层面配置）
// 例如：SLF4J 的 logback.xml 配置
// <logger name="MyHub" level="INFO"/>
```

### 6.7 避免敏感信息

不要在日志中记录敏感信息（密码、Token、个人信息等）：

```kotlin
// ❌ 不推荐：记录敏感信息
logger.debug { "User password: $password" }

// ✅ 推荐：记录非敏感信息
logger.debug { "User login attempt: $username" }
```

---

## 7. 常见问题

### 7.1 Logger 名称格式

**Q：Logger 名称的格式是什么？**

A：Logger 名称格式为：`appName:tag1:tag2:...`

- 如果不使用标签：`appName`
- 如果使用单个标签：`appName:tag1`
- 如果使用多个标签：`appName:tag1:tag2`

示例：

```kotlin
logger()                    // 名称：MyHub
logger("Module")           // 名称：MyHub:Module
logger("Module", "Comp")   // 名称：MyHub:Module:Comp
```

### 7.2 日志级别配置

**Q：如何配置日志级别？**

A：日志级别的配置取决于底层日志框架：

- **JVM 平台**：通过 SLF4J 实现（如 Logback、Log4j2）的配置文件
- **Android 平台**：通过 `kotlin-logging-android` 或系统属性配置
- **iOS/Web 平台**：使用 `kotlin-logging` 的默认配置

### 7.3 默认配置回退

**Q：如果未配置 Koin，Logger 会使用什么配置？**

A：如果未配置 Koin 或获取配置失败，Logger 会使用默认配置：

```kotlin
LoggerConfig("ZhifuTech")  // 默认应用名称
```

### 7.4 性能影响

**Q：日志记录会影响性能吗？**

A：Logger 模块通过以下方式优化性能：

1. **延迟求值**：使用 lambda 表达式，仅在日志级别启用时执行
2. **级别检查**：底层日志框架会在记录前检查日志级别
3. **异步记录**：底层日志框架通常支持异步记录（取决于配置）

### 7.5 跨平台兼容性

**Q：Logger 在不同平台的行为是否一致？**

A：Logger 接口在所有平台保持一致，但底层实现可能不同：

- **Android**：使用 Android Logcat（如果启用）
- **JVM**：使用 SLF4J
- **iOS**：使用 NSLog
- **Web**：使用 Console API

日志格式和输出位置可能因平台而异，但 API 使用方式完全一致。

### 7.6 测试中的使用

**Q：如何在测试中使用 Logger？**

A：在测试中，Logger 的行为与生产环境相同。如果需要验证日志输出，可以：

1. 使用日志框架的测试工具（如 SLF4J Test）
2. 配置日志输出到测试工具可以捕获的位置
3. 使用 Mock 或 Spy 来验证日志调用

---

## 8. 相关文档

- [Logger 模块 README](../README.md) - 快速开始和核心组件
- [MyHub 日志模块方案设计](./myhub-logger-infra-v1.0.md) - 架构设计文档
- [kotlin-logging GitHub](https://github.com/oshai/kotlin-logging) - 官方文档
- [SLF4J 官方文档](http://www.slf4j.org/) - SLF4J 文档

---

**文档版本**：v1.0  
**最后更新**：2026-01-13  
**维护者**：MyHub Development Team

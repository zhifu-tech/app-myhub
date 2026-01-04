# Logger Module

## 📋 概述

`core:logger` 是 MyHub 应用的日志模块，提供统一的日志接口和实现。该模块基于 [kotlin-logging](https://github.com/oshai/kotlin-logging) 库，为应用提供跨平台的日志记录功能。

## ✨ 特性

- **跨平台支持**：支持 Android、iOS、JVM、JS 和 WebAssembly 平台
- **统一接口**：提供简洁的 `Logger` 接口，隐藏底层实现细节
- **多级别日志**：支持 TRACE、DEBUG、INFO、WARN、ERROR 五个日志级别
- **延迟求值**：使用 lambda 表达式实现延迟求值，避免不必要的字符串拼接
- **标记支持**：支持使用标记（Marker）对日志进行分类
- **异常记录**：支持记录异常堆栈信息
- **依赖注入**：集成 Koin，支持通过依赖注入配置日志

## 🏗️ 模块结构

### 核心组件

#### 1. `Logger` 接口

定义所有日志级别的方法：

```kotlin
interface Logger {
    fun isTraceEnabled(): Boolean
    fun trace(marker: Any?, throwable: Throwable?, message: () -> Any?)

    fun isDebugEnabled(): Boolean
    fun debug(marker: Any?, throwable: Throwable?, message: () -> Any?)

    fun isInfoEnabled(): Boolean
    fun info(marker: Any?, throwable: Throwable?, message: () -> Any?)

    fun isWarnEnabled(): Boolean
    fun warn(marker: Any?, throwable: Throwable?, message: () -> Any?)

    fun isErrorEnabled(): Boolean
    fun error(marker: Any?, throwable: Throwable?, message: () -> Any?)
}
```

#### 2. `LoggerImpl` 实现类

`Logger` 接口的实现，内部委托给 `KLogger`。

#### 3. `LoggerFactory`

提供创建 `Logger` 实例的工厂函数：

```kotlin
// 使用默认应用名称创建 Logger
val logger = logger()

// 使用标签创建 Logger（名称格式：appName:tag1:tag2）
val logger = logger("Module", "Component")
```

#### 4. `LoggerConfig`

日志配置类：

```kotlin
data class LoggerConfig(
    val appName: String,
    val useAndroidLogger: Boolean = true
)
```

#### 5. `LoggerModule`

Koin 依赖注入模块，用于注册 `LoggerConfig`：

```kotlin
fun loggerModule(config: () -> LoggerConfig): Module
```

## 📖 使用指南

### 1. 配置 Koin

在应用启动时配置 Logger 模块：

```kotlin
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
```

### 2. 创建 Logger 实例

#### 方式一：使用工厂函数

```kotlin
// 使用默认 Logger
val logger = logger()

// 使用标签创建 Logger
val moduleLogger = logger("Module")
val componentLogger = logger("Module", "Component")
```

#### 方式二：使用顶层属性

```kotlin
// 使用预定义的 logger 属性
logger.info { "Application started" }
```

### 3. 记录日志

#### 基本用法

```kotlin
val logger = logger("MyModule")

// 记录不同级别的日志
logger.trace { "Trace message" }
logger.debug { "Debug message" }
logger.info { "Info message" }
logger.warn { "Warning message" }
logger.error { "Error message" }
```

#### 记录异常

```kotlin
try {
    // 可能抛出异常的操作
} catch (e: Exception) {
    logger.error(e) { "Operation failed" }
}
```

#### 使用标记（Marker）

```kotlin
// 使用字符串标记
logger.info("DATABASE") { "Database operation completed" }

// 使用 Marker 对象
val marker = KMarkerFactory.getMarker("SECURITY")
logger.warn(marker) { "Security warning" }
```

#### 检查日志级别是否启用

```kotlin
if (logger.isDebugEnabled()) {
    logger.debug { "Expensive debug operation" }
}
```

### 4. 扩展函数

模块提供了便捷的扩展函数，简化常见用法：

```kotlin
// 只传递消息
logger.info { "Simple message" }

// 传递异常和消息
logger.error(exception) { "Error occurred" }

// 完整参数（标记、异常、消息）
logger.debug(marker, exception) { "Full parameters" }
```

## 🧪 测试

模块包含完整的单元测试，覆盖以下内容：

- `LoggerImpl` 的功能测试
- `Logger` 扩展函数的测试
- `LoggerConfig` 的测试
- `LoggerFactory` 的测试

运行测试：

```bash
./gradlew :core:logger:jvmTest
```

## 🔧 平台特定配置

### Android

Android 平台使用 `kotlin-logging-android` 库，可以通过 `LoggerConfig.useAndroidLogger` 控制是否使用 Android Logger。

### JVM

JVM 平台使用 SLF4J 作为底层实现，需要添加相应的 SLF4J 实现（如 `slf4j-simple`）。

### iOS

iOS 平台使用 `kotlin-logging` 的默认实现。

### JS/WebAssembly

JS 和 WebAssembly 平台使用 `kotlin-logging` 的默认实现。

## 📦 依赖

### 核心依赖

- `kotlin-logging` - Kotlin 日志库
- `koin-core` - 依赖注入框架

### 平台特定依赖

- **Android**: `kotlin-logging-android`
- **JVM**: `slf4j-api`, `slf4j-simple`

## 📝 日志级别说明

| 级别      | 用途                               | 生产环境建议 |
| --------- | ---------------------------------- | ------------ |
| **TRACE** | 最详细的日志信息，用于深度调试     | ❌ 应禁用    |
| **DEBUG** | 调试信息，开发阶段的主要工具       | ❌ 应禁用    |
| **INFO**  | 信息性消息，记录重要事件和状态变化 | ✅ 可启用    |
| **WARN**  | 警告信息，记录潜在问题             | ✅ 应启用    |
| **ERROR** | 错误信息，记录严重问题             | ✅ 必须启用  |

## 🎯 最佳实践

1. **使用延迟求值**：始终使用 lambda 表达式传递消息，避免不必要的字符串拼接

   ```kotlin
   // ✅ 推荐
   logger.debug { "User: $user, Action: $action" }

   // ❌ 不推荐
   logger.debug("User: $user, Action: $action")
   ```

2. **检查日志级别**：对于昂贵的操作，先检查日志级别是否启用

   ```kotlin
   if (logger.isDebugEnabled()) {
       val expensiveData = computeExpensiveData()
       logger.debug { "Data: $expensiveData" }
   }
   ```

3. **使用有意义的标签**：创建 Logger 时使用有意义的标签，便于日志过滤和查找

   ```kotlin
   val logger = logger("UserRepository", "Authentication")
   ```

4. **记录异常堆栈**：记录异常时使用带异常参数的版本

   ```kotlin
   logger.error(exception) { "Operation failed" }
   ```

5. **生产环境配置**：在生产环境中禁用 TRACE 和 DEBUG 级别，避免性能问题和敏感信息泄露

## 🔗 相关链接

- [kotlin-logging GitHub](https://github.com/oshai/kotlin-logging)
- [SLF4J 官方文档](http://www.slf4j.org/)
- [Koin 官方文档](https://insert-koin.io/)

# Core Logger Module
本模块用于**规范**和**实现** MyHub 应用的日志基础设施（Logger Infra），为各功能模块**提供统一、跨平台的日志记录能力**。它基于 [kotlin-logging](https://github.com/oshai/kotlin-logging) 库，实现了**延迟求值**、**多级别日志**、**标记支持**等特性，并提供了面向 KMP 场景的**统一日志接口**，方便在 **Android、iOS、JVM、Web** 等多端项目中集成和使用。

## 功能特性

- ✅ 跨平台支持（Android、iOS、JVM、Web）
- ✅ 统一的日志接口（SLF4J API）
- ✅ 平台特定实现
- ✅ 可配置的日志级别

## 平台实现

### Android
- 使用 `kotlin-logging-android`
- 自动集成 Android Logcat

### iOS
- 使用 `NSLog` 输出
- 集成 iOS 日志系统

### JVM
- 使用 SLF4J + 实现
- 默认：`slf4j-simple`（简单快速）
- 可选：`logback`（功能强大）

### Web
- 使用浏览器 `console` API
- 支持不同日志级别

## JVM 平台配置

### 使用 slf4j-simple（默认）

配置文件：`src/jvmMain/resources/simplelogger.properties`

```properties
# 默认日志级别
org.slf4j.simpleLogger.defaultLogLevel=debug

# 特定包的日志级别
org.slf4j.simpleLogger.log.tech.zhifu.app.myhub=debug
```

### 切换到 Logback（可选）

1. **修改依赖**（`build.gradle.kts`）：
```kotlin
jvmMain.dependencies {
    // 注释掉 slf4j-simple
    // implementation(libs.slf4j.simple)
    
    // 使用 Logback
    implementation(libs.logback)
}
```

2. **配置文件**：`src/jvmMain/resources/logback.xml`（已提供模板）

3. **删除**：`simplelogger.properties`（如果存在）

## 使用方式

```kotlin
import tech.zhifu.app.myhub.logger.logger

// 使用全局 logger
logger.debug { "调试信息" }
logger.info { "信息" }
logger.warn { "警告" }
logger.error { "错误" }

// 使用带标签的 logger
val componentLogger = logger("MyComponent")
componentLogger.debug { "组件日志" }
```

## 配置说明

### 日志级别（从低到高）

- **TRACE** - 最详细的日志
- **DEBUG** - 调试信息（开发环境）
- **INFO** - 信息性消息（生产环境）
- **WARN** - 警告信息
- **ERROR** - 错误信息

### 配置文件位置

所有日志配置文件都在 `core/logger` 模块中：

- `src/jvmMain/resources/simplelogger.properties` - slf4j-simple 配置
- `src/jvmMain/resources/logback.xml` - Logback 配置（如果使用）

**注意**：不要在应用模块（如 `composeApp`）中创建日志配置文件，统一在 `core/logger` 模块中管理。

## 迁移指南

### 从 slf4j-simple 迁移到 Logback

1. 修改 `core/logger/build.gradle.kts`：
```kotlin
jvmMain.dependencies {
    // implementation(libs.slf4j.simple)  // 注释掉
    implementation(libs.logback)          // 使用 Logback
}
```

2. 配置文件会自动使用 `logback.xml`（如果存在）

3. 代码不需要修改，因为都使用 SLF4J API

## 参考文档

- [JVM 日志查看指南](../../composeApp/docs/JVM_LOGGING_GUIDE.md)
- [Logback 介绍](../../composeApp/docs/LOGBACK_INTRO.md)

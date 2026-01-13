# Core Logger Module

本模块用于**规范**和**实现** MyHub 应用的日志基础设施（Logger Infra），为各功能模块**提供统一、跨平台的日志记录能力**。它基于 [kotlin-logging](https://github.com/oshai/kotlin-logging) 库，实现了**延迟求值**、**多级别日志**、**标记支持**等特性，并提供了面向 KMP 场景的**统一日志接口**，方便在 **Android、iOS、JVM、Web** 等多端项目中集成和使用。

## 核心组件

### 1. Logger 接口

统一的日志接口，定义所有日志级别的方法：

- **TRACE**：最详细的日志信息，用于深度调试
- **DEBUG**：调试信息，开发阶段的主要工具
- **INFO**：信息性消息，记录重要事件和状态变化
- **WARN**：警告信息，记录潜在问题
- **ERROR**：错误信息，记录严重问题

### 2. LoggerFactory

工厂函数，提供创建 `Logger` 实例的能力：

- `logger()`：使用默认应用名称创建 Logger
- `logger(vararg tags: String)`：使用标签创建 Logger（名称格式：`appName:tag1:tag2`）

### 3. LoggerConfig

日志配置类，用于统一管理日志配置：

- `appName`：应用名称
- `useAndroidLogger`：是否使用 Android Logcat（Android 平台专用）

### 4. LoggerModule

Koin 依赖注入模块，用于注册 `LoggerConfig`。

## 使用示例

```kotlin
// 1. 配置 Koin
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

// 2. 创建 Logger 实例
val logger = logger("MyModule")

// 3. 记录日志
logger.info { "Application started" }
logger.debug { "Loading data..." }

// 4. 记录异常
try {
    // 可能抛出异常的操作
} catch (e: Exception) {
    logger.error(e) { "Operation failed" }
}

// 5. 使用标记分类日志
logger.info("DATABASE") { "Database operation completed" }

// 6. 检查日志级别（避免不必要的计算）
if (logger.isDebugEnabled()) {
    val expensiveData = computeExpensiveData()
    logger.debug { "Data: $expensiveData" }
}
```

## 文档

- [MyHub 日志模块方案设计](./docs/myhub-logger-infra-v1.0.md) - 架构设计文档
- [Logger 模块使用指南](./docs/USAGE_GUIDE.md) - 详细使用指南

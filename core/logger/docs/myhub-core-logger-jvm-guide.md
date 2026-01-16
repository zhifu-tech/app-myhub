# MyHub Core Logger JVM 平台指南

## 目录

1. [概述](#概述)
2. [日志框架生态](#日志框架生态)
3. [快速开始](#快速开始)
4. [Logback 介绍](#logback-介绍)
5. [配置指南](#配置指南)
6. [常见问题](#常见问题)
7. [迁移指南](#迁移指南)
8. [参考资源](#参考资源)

---

## 概述

本指南介绍如何在 MyHub 项目的 JVM 平台上配置和使用日志系统。

### 核心要点

- ✅ **统一管理**：所有日志配置都在 `core/logger` 模块中
- ✅ **SLF4J API**：代码使用 SLF4J 接口，底层可切换不同实现
- ✅ **默认实现**：开发环境使用 `slf4j-simple`，生产环境推荐 `Logback`
- ✅ **零代码改动**：切换日志实现不需要修改业务代码

---

## 日志框架生态

### 日志框架层次结构

```
应用代码
    ↓
SLF4J (日志门面/接口)
    ↓
Logback / Log4j2 / slf4j-simple (日志实现)
    ↓
控制台 / 文件 / 数据库 / 网络等
```

### SLF4J vs Logback

| 组件               | 作用       | 类比              |
|------------------|----------|-----------------|
| **SLF4J**        | 日志门面（接口） | USB 接口标准        |
| **Logback**      | 日志实现     | USB 设备（U 盘、鼠标等） |
| **slf4j-simple** | 简单实现     | 基础的 USB 设备      |

**SLF4J** 是接口，**Logback** 是实现。你的代码使用 SLF4J API，底层可以使用不同的实现。

---

## 快速开始

### 问题：日志没有输出到控制台

在 JVM 平台上运行应用时，`logger.debug()` 等日志没有输出到控制台。

### 原因

项目使用 `kotlin-logging`，底层使用 SLF4J。在 JVM 平台上，SLF4J 需要一个实现（如 `slf4j-simple` 或 `Logback`），并且需要配置日志级别。

### 解决方案

#### 1. 检查日志配置文件

**重要**：日志配置文件统一在 `core/logger` 模块中管理，不在应用模块中。

**使用 slf4j-simple**：

- 配置文件：`core/logger/src/jvmMain/resources/simplelogger.properties`

**使用 Logback**：

- 配置文件：`core/logger/src/jvmMain/resources/logback.xml`

#### 2. 查看日志输出

**方式一：IDE 控制台**

在 IntelliJ IDEA 或 Android Studio 中运行 JVM 应用时，日志会直接输出到运行控制台（Run/Debug 窗口）。

**方式二：终端**

如果通过命令行运行，日志会输出到标准输出（stdout）。

```bash
./gradlew :composeApp:run
```

**方式三：查看日志文件（如果配置了文件输出）**

默认情况下，`slf4j-simple` 只输出到控制台。如果需要输出到文件，建议使用 Logback。

#### 3. 使用示例

```kotlin
import tech.zhifu.app.myhub.logger.logger

@Composable
fun MyComponent() {
    // 使用全局 logger
    logger.debug { "组件渲染" }

    // 使用带标签的 logger
    val componentLogger = logger("MyComponent")
    componentLogger.info { "组件初始化" }
}
```

---

## Logback 介绍

### 什么是 Logback？

**Logback** 是一个功能强大的 Java 日志框架，由 SLF4J 的作者 Ceki Gülcü 开发。它是 Log4j 的继承者，设计目标是更快、更小、更灵活。

### 核心特点

- ✅ **高性能**：比 Log4j 快 10 倍
- ✅ **零依赖**：除了 SLF4J API，不需要其他依赖
- ✅ **功能丰富**：支持多种输出方式、日志轮转、过滤等
- ✅ **向后兼容**：与 Log4j 配置兼容
- ✅ **活跃维护**：持续更新和维护

### Logback 的组成部分

Logback 由三个模块组成：

1. **Logback Core** - 核心功能模块，提供基础日志功能
2. **Logback Classic** - 实现了 SLF4J API，包含 Logback Core（**最常用的模块**）
3. **Logback Access** - 用于 Servlet 容器（如 Tomcat），记录 HTTP 访问日志

### Logback vs slf4j-simple

| 特性        | slf4j-simple | Logback |
|-----------|--------------|---------|
| **配置复杂度** | 简单           | 中等      |
| **功能丰富度** | 基础           | 强大      |
| **文件输出**  | ❌            | ✅       |
| **日志轮转**  | ❌            | ✅       |
| **异步日志**  | ❌            | ✅       |
| **性能**    | 一般           | 优秀      |
| **适用场景**  | 开发/测试        | 生产环境    |

**slf4j-simple 优点：**

- ✅ 简单轻量
- ✅ 零配置即可使用
- ✅ 适合开发和测试

**slf4j-simple 缺点：**

- ❌ 功能有限（只能输出到控制台）
- ❌ 不支持日志文件
- ❌ 不支持日志轮转
- ❌ 配置选项少

**Logback 优点：**

- ✅ 功能强大
- ✅ 支持多种输出方式（控制台、文件、数据库等）
- ✅ 支持日志轮转（按大小、时间）
- ✅ 灵活的配置（XML、Groovy）
- ✅ 性能优化（异步日志）
- ✅ 过滤和条件配置

**Logback 缺点：**

- ⚠️ 需要配置文件
- ⚠️ 稍微复杂一些

### Logback 核心概念

#### Logger（日志记录器）

```kotlin
// 创建 Logger
val logger = LoggerFactory.getLogger("MyClass")

// 记录日志
logger.debug("调试信息")
logger.info("信息")
logger.warn("警告")
logger.error("错误")
```

#### Appender（输出目标）

定义日志输出到哪里：

- **ConsoleAppender** - 输出到控制台
- **FileAppender** - 输出到文件
- **RollingFileAppender** - 输出到文件，支持轮转
- **SMTPAppender** - 发送邮件
- **DBAppender** - 写入数据库

#### Layout（格式化）

定义日志格式：

```xml

<pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
```

输出示例：

```
2024-01-01 12:00:00 [main] INFO  com.example.MyClass - 应用启动
```

#### Level（日志级别）

从低到高：

- **TRACE** - 最详细
- **DEBUG** - 调试信息
- **INFO** - 信息性消息
- **WARN** - 警告
- **ERROR** - 错误

---

## 配置指南

### 使用 slf4j-simple（开发环境）

#### 1. 添加依赖

在 `core/logger/build.gradle.kts` 中：

```kotlin
kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.slf4j.api)
            implementation(libs.slf4j.simple)
        }
    }
}
```

#### 2. 配置文件

配置文件位置：`core/logger/src/jvmMain/resources/simplelogger.properties`

```properties
# 默认日志级别设置为 DEBUG
org.slf4j.simpleLogger.defaultLogLevel=debug
# 设置特定包的日志级别
org.slf4j.simpleLogger.log.tech.zhifu.app.myhub=debug
org.slf4j.simpleLogger.log.Myhub=debug
org.slf4j.simpleLogger.log.MyHub=debug
org.slf4j.simpleLogger.log.ZhifuTech=debug
# 显示日期时间
org.slf4j.simpleLogger.showDateTime=true
org.slf4j.simpleLogger.dateTimeFormat=yyyy-MM-dd HH:mm:ss.SSS
# 显示线程名
org.slf4j.simpleLogger.showThreadName=true
# 显示日志名称（简化）
org.slf4j.simpleLogger.showLogName=true
org.slf4j.simpleLogger.showShortLogName=true
# 显示日志级别
org.slf4j.simpleLogger.showSimpleName=true
```

#### 3. 日志级别说明

SLF4J Simple Logger 支持的日志级别（从低到高）：

- `trace` - 最详细的日志
- `debug` - 调试信息（当前配置）
- `info` - 信息性消息
- `warn` - 警告信息
- `error` - 错误信息

### 使用 Logback（生产环境推荐）

#### 1. 添加依赖

在 `core/logger/build.gradle.kts` 中：

```kotlin
kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.slf4j.api)
            // 移除 slf4j-simple
            // implementation(libs.slf4j.simple)

            // 添加 Logback
            implementation(libs.logback)
        }
    }
}
```

#### 2. 基础配置（logback.xml）

配置文件位置：`core/logger/src/jvmMain/resources/logback.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 控制台输出 -->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 文件输出（可选，开发时可以注释掉） -->
    <!--
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/myhub.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/myhub.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>7</maxHistory>
            <totalSizeCap>100MB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    -->

    <!-- 日志级别配置 -->
    <root level="DEBUG">
        <appender-ref ref="STDOUT" />
        <!-- 开发时可以注释掉文件输出 -->
        <!-- <appender-ref ref="FILE" /> -->
    </root>

    <!-- 特定包的日志级别 -->
    <logger name="tech.zhifu.app.myhub" level="DEBUG" />
</configuration>
```

#### 3. 带日志轮转的配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 控制台输出 -->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 文件输出（带轮转） -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/myhub.log</file>

        <!-- 轮转策略：按大小和时间 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <!-- 日志文件命名模式 -->
            <fileNamePattern>logs/myhub.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <!-- 单个文件最大大小 -->
            <maxFileSize>10MB</maxFileSize>
            <!-- 保留天数 -->
            <maxHistory>30</maxHistory>
            <!-- 总大小限制 -->
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>

        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 异步日志（提高性能） -->
    <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>512</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <appender-ref ref="FILE" />
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="ASYNC" />
    </root>

    <logger name="tech.zhifu.app.myhub" level="DEBUG" />
</configuration>
```

#### 4. Logback 高级特性

**条件配置：**

```xml

<configuration>
    <!-- 只在 Windows 上启用文件输出 -->
    <if condition='property("os.name").contains("Windows")'>
        <then>
            <appender name="FILE" class="ch.qos.logback.core.FileAppender">
                <file>logs/application.log</file>
                <!-- ... -->
            </appender>
        </then>
    </if>
</configuration>
```

**过滤器：**

```xml

<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <!-- 只输出 ERROR 级别 -->
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        <level>ERROR</level>
    </filter>
    <!-- ... -->
</appender>
```

**异步日志：**

```xml
<!-- 异步日志提高性能 -->
<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>512</queueSize>
    <discardingThreshold>0</discardingThreshold>
    <appender-ref ref="FILE" />
</appender>
```

---

## 常见问题

### 问题 1：日志仍然不显示

**检查点：**

1. 确认配置文件在 `core/logger/src/jvmMain/resources/` 目录下
2. 确认 `core/logger` 模块已正确编译并包含在依赖中
3. 确认文件已正确编译到 classpath 中
4. 重启应用（配置文件在启动时加载）

**验证方法：**

```kotlin
// 在代码中添加测试日志
logger.info { "测试日志输出" }
logger.debug { "DEBUG 日志测试" }
```

### 问题 2：日志级别不正确

**解决方法：**

**使用 slf4j-simple：**
修改 `simplelogger.properties` 中的日志级别：

```properties
# 设置为 DEBUG 以查看所有调试信息
org.slf4j.simpleLogger.defaultLogLevel=debug
# 或者只针对特定包设置
org.slf4j.simpleLogger.log.tech.zhifu.app.myhub=debug
```

**使用 Logback：**
修改 `logback.xml` 中的日志级别：

```xml

<root level="DEBUG">
    <!-- ... -->
</root>

<logger name="tech.zhifu.app.myhub" level="DEBUG" />
```

### 问题 3：日志格式不清晰

当前配置已包含：

- 日期时间：`yyyy-MM-dd HH:mm:ss.SSS`
- 线程名
- 日志名称
- 日志级别

如果需要自定义格式，可以修改配置文件。

### 问题 4：需要文件日志

**解决方案：**

1. 切换到 Logback（推荐）
2. 配置 `RollingFileAppender`
3. 启用文件输出

---

## 迁移指南

### 从 slf4j-simple 迁移到 Logback

**重要**：所有操作都在 `core/logger` 模块中进行。

#### 步骤 1：添加依赖

在 `core/logger/build.gradle.kts` 中：

```kotlin
jvmMain.dependencies {
    implementation(libs.slf4j.api)
    // 移除 slf4j-simple
    // implementation(libs.slf4j.simple)

    // 添加 Logback
    implementation(libs.logback)
}
```

#### 步骤 2：创建配置文件

配置文件位置：`core/logger/src/jvmMain/resources/logback.xml`

模板文件已提供，根据需求修改配置。

#### 步骤 3：删除旧配置（可选）

如果使用 Logback，可以删除 `simplelogger.properties`（或保留作为备用）。

#### 步骤 4：测试

重启应用，验证日志输出。

**代码不需要修改**，因为都使用 SLF4J API！

---

## 何时使用 Logback？

### ✅ 应该使用 Logback 的场景

1. **生产环境** - 需要文件日志和日志轮转
2. **需要日志分析** - 日志写入文件便于分析
3. **性能要求高** - 异步日志提高性能
4. **复杂日志需求** - 多种输出方式、过滤等

### ✅ 可以继续使用 slf4j-simple 的场景

1. **开发环境** - 简单快速
2. **原型开发** - 快速验证
3. **简单应用** - 只需要控制台输出

### 推荐方案

- **开发环境**：使用 `slf4j-simple`（简单快速）
- **生产环境**：使用 `Logback`（功能强大）

或者统一使用 Logback，通过配置文件控制不同环境的日志行为。

---

## 重要提示

⚠️ **配置文件位置**：所有日志配置文件都应该在 `core/logger` 模块中，不要在应用模块（如 `composeApp`）中创建日志配置文件。

- ✅ 正确：`core/logger/src/jvmMain/resources/simplelogger.properties`
- ✅ 正确：`core/logger/src/jvmMain/resources/logback.xml`
- ❌ 错误：`composeApp/src/jvmMain/resources/simplelogger.properties`
- ❌ 错误：`composeApp/src/jvmMain/resources/logback.xml`

---

## 快速检查清单

- [ ] 配置文件在 `core/logger/src/jvmMain/resources/` 目录下
- [ ] `core/logger` 模块已正确编译并包含在依赖中
- [ ] 日志级别设置为 `debug` 或更低
- [ ] 应用已重启（配置文件在启动时加载）
- [ ] 查看 IDE 运行控制台或终端输出
- [ ] 确认 logger 名称匹配配置中的设置

---

## 参考资源

- [SLF4J 官方文档](http://www.slf4j.org/)
- [Logback 官方文档](https://logback.qos.ch/)
- [Logback 配置手册](https://logback.qos.ch/manual/configuration.html)
- [SLF4J Simple Logger 文档](http://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html)
- [Kotlin Logging 文档](https://github.com/oshai/kotlin-logging)

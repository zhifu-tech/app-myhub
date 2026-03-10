# MyHub 统计框架模块方案设计

**方案名称**：Analytics Infra v1  
**文档版本**：v1.0  
**文档类型**：技术方案设计文档  
**创建日期**：2026-01-13  
**锁定日期**：2026-01-13  
**最后更新**：2026-01-13  
**作者**：MyHub Development Team  
**评审状态**：🟢 通过  
**方案状态**：🔒 已锁定

---

## 📋 文档目录

1. [修改历史](#修改历史)
2. [方案状态摘要](#-方案状态摘要)
3. [问题背景](#1-问题背景)
4. [设计目标](#2-设计目标)
5. [技术调研](#3-技术调研)
6. [架构设计](#4-架构设计)
7. [实现细节](#5-实现细节)
8. [实施计划](#6-实施计划)
9. [风险评估](#7-风险评估)
10. [附录](#8-附录)

---

## 📊 方案状态摘要

**当前状态**：

- **评审状态**：🟢 通过（文档已通过评审，可以进入实施阶段）
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Analytics Infra v1 的基线设计
- **锁定日期**：2026-01-13
- **当前进度**：所有阶段已完成，方案设计已确定并锁定

**状态说明**：

- **评审状态**：用于标识文档的评审进度
  - 🟢 通过：文档已通过评审，可以进入实施阶段
  - 🟡 待评审：文档正在等待评审或评审进行中
  - 🔴 需修改：文档评审后需要修改
- **方案状态**：用于标识方案的实施进度
  - 🔒 已锁定：方案设计已确定，不允许随意修改
  - 📝 进行中：方案设计正在进行中，可以修改
  - ⏸️ 暂停：方案设计暂时停止，保留当前状态
- 详细状态定义请参考 [MyHub 架构设计文档规范](../../../docs/infra/myhub-infra-rules.md)

---

## 修改历史

| 版本 | 日期       | 修改内容                           | 修改原因           |
| ---- | ---------- | ---------------------------------- | ------------------ |
| v1.0 | 2026-01-13 | 初始方案设计                       | 新建               |
| v1.0 | 2026-01-13 | 完成架构设计文档                   | 完善文档           |
| v1.0 | 2026-01-13 | 更新状态：评审通过、方案已锁定     | 状态更新：评审通过 |

---

## 1. 问题背景

### 1.1 用户场景

在 MyHub 应用的开发和运行过程中，需要进行用户行为统计和数据分析。典型的场景包括：

1. **多统计服务支持**：应用需要在不同市场使用不同的统计服务（如 Google Play 使用 Firebase，国内市场使用 Umeng）
2. **统一统计接口**：业务代码需要统一的统计接口，避免直接依赖底层 SDK
3. **跨平台统计**：应用需要在 Android、iOS、JVM、Web 等多个平台上使用统一的统计 API
4. **隐私合规**：应用需要支持 GDPR、CCPA 等隐私法规，允许用户控制统计数据的收集
5. **事件缓冲**：统计服务初始化期间的事件需要缓冲，避免丢失
6. **类型安全**：统计参数需要类型安全，避免跨平台兼容性问题

### 1.2 问题根因

在引入统一的统计框架模块之前，MyHub 应用面临以下问题：

1. **统计 SDK 分散**：各模块可能使用不同的统计 SDK，导致代码重复和不一致
2. **平台特定代码耦合**：业务代码直接使用平台特定的统计 SDK（如 Android 使用 Firebase，iOS 使用 Umeng），导致跨平台代码难以维护
3. **渠道管理混乱**：不同渠道（Google Play、Umeng）的统计配置分散在各处，难以统一管理
4. **类型安全问题**：使用 `Any` 类型导致跨平台兼容性问题（JS/WASM 不支持所有 Any 类型）
5. **隐私合规缺失**：缺乏统一的隐私合规机制，难以满足 GDPR、CCPA 等法规要求
6. **初始化竞态**：统计服务初始化期间的事件可能丢失

### 1.3 影响范围

- **开发效率**：缺乏统一的统计接口导致开发人员需要了解多个统计 SDK，降低开发效率
- **代码维护**：统计相关代码分散，增加维护成本和出错风险
- **跨平台兼容性**：直接使用平台特定统计 SDK 导致代码难以在不同平台间复用
- **隐私合规**：缺乏统一的隐私合规机制可能导致合规风险
- **数据丢失**：初始化期间的事件丢失导致统计数据不完整

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **统一的统计接口**：提供统一的 `AnalyticsService` 接口，隐藏底层 SDK 实现细节
- ✅ **多 Provider 支持**：支持同时使用多个统计服务（Firebase、Umeng、Console、File）
- ✅ **渠道框架**：支持通过构建变体（Build Variant）选择不同的统计服务
- ✅ **类型安全**：使用 `AnalyticsValue` 替代 `Any`，避免跨平台兼容性问题
- ✅ **事件缓冲**：Provider 初始化期间的事件自动缓冲，避免丢失
- ✅ **隐私合规**：内置 `AnalyticsConsent` 支持 GDPR、CCPA 等隐私法规
- ✅ **跨平台支持**：支持 Android、iOS、JVM、JS、WASM 等多个平台
- ✅ **易于扩展**：采用 Registry 模式，便于添加新的统计服务

### 2.2 非功能目标

- ✅ **性能优化**：事件上报应该是异步的，不影响应用性能
- ✅ **易用性**：提供简洁、易用的 API，降低学习成本
- ✅ **可扩展性**：易于添加新的统计服务或新的统计功能
- ✅ **可测试性**：提供可测试的接口，支持单元测试和集成测试
- ✅ **代码复用**：最大化代码复用，减少重复代码

### 2.3 模块特性说明

**重要说明**：`core/analytics` 模块是一个**混合（Mixed）模块**，它包含多个统计相关的功能：

1. **统一统计接口功能**：提供统一的统计服务接口
2. **多 Provider 管理功能**：管理多个统计服务提供商
3. **渠道框架功能**：支持平台+渠道双维度变体系统
4. **类型安全功能**：提供类型安全的统计值类型
5. **隐私合规功能**：提供隐私合规支持
6. **事件缓冲功能**：提供事件缓冲机制
7. **未来可能扩展的功能**：如批量上报、事件过滤、数据加密等

这种混合设计是合理的，因为：

- 这些功能都与统计相关，属于同一领域
- 将它们放在同一个模块中可以减少模块数量，简化依赖关系
- 它们共享相同的统计抽象机制和渠道框架
- 便于统一管理和维护统计相关的代码

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 统计服务提供商

**Firebase Analytics**：

- ✅ **Google 官方**：Google 官方提供的统计服务
- ✅ **跨平台支持**：支持 Android、iOS、Web
- ✅ **功能丰富**：提供用户属性、事件、转化跟踪等功能
- ✅ **免费使用**：基础功能免费使用

**Umeng Analytics**：

- ✅ **国内市场**：友盟+是国内市场主流的统计服务
- ✅ **Android/iOS 支持**：支持 Android 和 iOS 平台
- ✅ **功能丰富**：提供用户属性、事件、转化跟踪等功能
- ✅ **免费使用**：基础功能免费使用

**Console Provider**：

- ✅ **调试支持**：所有平台都支持，用于测试和调试
- ✅ **零依赖**：无需外部 SDK，使用 logger 输出

**File Provider**：

- ✅ **Desktop 支持**：用于 Desktop QA 测试
- ✅ **数据导出**：支持 JSON 和 CSV 格式导出

#### 3.1.2 渠道框架

**KMP Source Sets + 构建变体**：

**选择理由**：

- ✅ **KMP 原生支持**：利用 KMP 的 Source Sets 机制，天然支持变体维度分离
- ✅ **编译时确定**：渠道选择在编译时确定，运行时零开销
- ✅ **类型安全**：使用 Kotlin 的类型系统，编译期检查
- ✅ **互斥性保证**：编译时保证渠道互斥，不会同时存在多个渠道代码
- ✅ **无外部依赖**：纯 Kotlin 实现，无需引入外部依赖

#### 3.1.3 expect/actual 机制

**选择理由**：

- ✅ **KMP 原生支持**：Kotlin Multiplatform 提供的 expect/actual 机制是跨平台抽象的标准方式
- ✅ **编译时检查**：expect/actual 机制在编译时检查，确保所有平台都提供了实现
- ✅ **类型安全**：使用 Kotlin 的类型系统，提供类型安全的 API
- ✅ **零运行时开销**：expect/actual 在编译时解析，运行时无额外开销

### 3.2 平台支持策略

#### 3.2.1 支持的平台

| 平台    | 支持状态    | 说明                     |
| ------- | ----------- | ------------------------ |
| Android | ✅ 完全支持 | Firebase、Umeng、Console |
| iOS     | ✅ 完全支持 | Firebase、Umeng、Console |
| JVM     | ✅ 完全支持 | File、Console            |
| JS      | ✅ 完全支持 | Firebase、Console        |
| WASM    | ✅ 完全支持 | Firebase、Console        |

#### 3.2.2 渠道支持策略

| 渠道       | 支持状态    | 说明                   |
| ---------- | ----------- | ---------------------- |
| googlePlay | ✅ 完全支持 | Firebase Analytics     |
| umeng      | ✅ 完全支持 | Umeng Analytics        |
| channel    | ✅ 完全支持 | 默认渠道（仅 Console） |

### 3.3 渠道框架策略

#### 3.3.1 平台+渠道双维度变体系统

- **平台维度**：Android、iOS、JVM、JS、WASM
- **渠道维度**：googlePlay、umeng、channel（默认）
- **组合方式**：`{platform}{Channel}Main`（如 `androidGooglePlayMain`、`iosUmengMain`）

#### 3.3.2 渠道互斥性

- `{platform}ChannelMain` 和 `{platform}{Channel}Main` 是互斥的
- 构建时只会注入一个渠道的代码
- 例如：`iosChannelMain` 和 `iosUmengMain` 不会同时存在

---

## 4. 架构设计

### 4.1 整体架构

```text
┌─────────────────────────────────────────────────────────────┐
│                     应用层（Application）                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Feature A   │  │  Feature B   │  │  Feature C   │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
└─────────┼──────────────────┼──────────────────┼──────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
          ┌──────────────────▼──────────────────┐
          │   core:analytics 模块                │
          │  (混合模块 - 统计功能集合)             │
          │                                     │
          │  ┌──────────────────────────────┐   │
          │  │    AnalyticsService API      │   │
          │  │  (统一接口)                   │   │
          │  └──────────────┬─────────────┘   │
          │                 │                  │
          │  ┌──────────────┼──────────────┐   │
          │  │              │              │   │
          │  ▼              ▼              ▼   │
          │  ┌──────────┐  ┌──────────┐  ┌──────────┐
          │  │Analytics │  │Analytics │  │Analytics │
          │  │Manager   │  │Config    │  │Consent   │
          │  │          │  │          │  │          │
          │  └────┬─────┘  └────┬─────┘  └────┬─────┘
          │       │             │             │
          │       │             │             │
          │  ┌────▼─────────────▼─────────────▼─────┐
          │  │     AnalyticsProviderFactory        │
          │  │  (Registry 模式)                    │
          │  └────────────────────────────────────┘
          │                 │
          │  ┌──────────────┼──────────────┐
          │  │              │              │
          │  ▼              ▼              ▼
          │  ┌──────────┐  ┌──────────┐  ┌──────────┐
          │  │Firebase  │  │Umeng    │  │Console   │
          │  │Provider  │  │Provider │  │Provider  │
          │  └────┬─────┘  └────┬─────┘  └────┬─────┘
          │       │             │             │
          │       │             │             │
          │  ┌────▼─────────────▼─────────────▼─────┐
          │  │     渠道框架                        │
          │  │  (平台+渠道双维度变体系统)            │
          │  └────────────────────────────────────┘
          └──────────────────┬───────────────────────┘
                             │
          ┌──────────────────▼──────────────────┐
          │      KMP Source Sets                │
          │                                     │
          │  ┌──────────────┐                  │
          │  │  commonMain  │                  │
          │  │  (公共接口)  │                  │
          │  └──────┬───────┘                  │
          │         │                           │
          │  ┌───────┼───────┐                 │
          │  │       │       │                 │
          │  ▼       ▼       ▼                 │
          │  ┌──────┐ ┌──────┐ ┌──────┐        │
          │  │android│ │ ios │ │ jvm │ │ js/  │
          │  │ Main  │ │Main │ │Main │ │wasmJs│
          │  └───┬───┘ └───┬──┘ └───┬──┘ └───┬──┘
          │      │         │        │        │
          │  ┌───▼─────────▼────────▼─────────▼───┐
          │  │  平台+渠道组合源集                │
          │  │  androidGooglePlayMain            │
          │  │  androidUmengMain                 │
          │  │  iosGooglePlayMain                 │
          │  │  iosUmengMain                      │
          │  │  jsGooglePlayMain                  │
          │  └──────────────────────────────────┘
          └──────────────────┬───────────────────────┘
                             │
          ┌──────────────────▼──────────────────┐
          │      统计服务 SDK                    │
          │  - Firebase Analytics               │
          │  - Umeng Analytics                   │
          │  - Logger (Console)                  │
          │  - File System (File)                 │
          └─────────────────────────────────────┘
```

### 4.2 核心组件设计

#### 4.2.1 AnalyticsService

`AnalyticsService` 接口提供统一的统计服务接口：

```kotlin
package tech.zhifu.app.myhub.analytics

interface AnalyticsService {
    fun logEvent(event: AnalyticsEvent)
    fun logEvents(events: List<AnalyticsEvent>)
    fun setUserProperty(key: String, value: AnalyticsValue?)
    fun setUserId(userId: String?)
    fun setScreen(screenName: String, screenClass: String? = null)
    fun reset()
}
```

**设计要点**：

- 使用 `interface` 定义统一接口
- 提供事件记录、用户属性、屏幕设置等功能
- 隐藏底层 Provider 实现细节

#### 4.2.2 AnalyticsManager

`AnalyticsManager` 类管理多个统计服务提供商：

```kotlin
package tech.zhifu.app.myhub.analytics

class AnalyticsManager(
    private val config: AnalyticsConfig,
    private val consent: AnalyticsConsent,
    private val providerFactory: AnalyticsProviderFactory
) : AnalyticsService {
    suspend fun initialize()
    // ... 实现 AnalyticsService 接口
}
```

**设计要点**：

- 管理多个 Provider 实例
- 初始化时根据配置和渠道注册 Provider
- 统一转发统计请求到所有 Provider

#### 4.2.3 AnalyticsProvider

`AnalyticsProvider` 接口定义统计服务提供商的抽象：

```kotlin
package tech.zhifu.app.myhub.analytics

interface AnalyticsProvider {
    val name: String
    val isInitialized: Boolean
    val isReady: StateFlow<Boolean>
    val supportedPlatforms: Set<Platform>
    val supportedRegions: Set<Region>

    suspend fun initialize(config: ProviderConfig)
    fun logEvent(event: AnalyticsEvent)
    fun setUserProperty(key: String, value: AnalyticsValue?)
    fun setUserId(userId: String?)
    fun setScreen(screenName: String, screenClass: String? = null)
    fun reset()
}
```

**设计要点**：

- 定义 Provider 的抽象接口
- 提供初始化、事件记录、用户属性等功能
- 支持平台和地区过滤

#### 4.2.4 BaseAnalyticsProvider

`BaseAnalyticsProvider` 抽象类提供事件缓冲机制：

```kotlin
package tech.zhifu.app.myhub.analytics

abstract class BaseAnalyticsProvider : AnalyticsProvider {
    private val eventBuffer = mutableListOf<AnalyticsEvent>()

    override fun logEvent(event: AnalyticsEvent) {
        if (_isReady.value) {
            doLogEvent(event)
        } else {
            // 缓冲事件
            eventBuffer.add(event)
        }
    }

    protected abstract fun doLogEvent(event: AnalyticsEvent)

    protected suspend fun markAsReady() {
        _isReady.value = true
        flushBufferedEvents()
    }
}
```

**设计要点**：

- 提供事件缓冲机制，避免初始化期间的事件丢失
- 使用 `StateFlow` 监听初始化完成状态
- 子类实现具体的上报逻辑

#### 4.2.5 AnalyticsValue

`AnalyticsValue` 密封接口提供类型安全的统计值：

```kotlin
package tech.zhifu.app.myhub.analytics

sealed interface AnalyticsValue {
    data class Str(val value: String) : AnalyticsValue
    data class Num(val value: Double) : AnalyticsValue
    data class Int(val value: Long) : AnalyticsValue
    data class Bool(val value: Boolean) : AnalyticsValue
}
```

**设计要点**：

- 使用密封接口，类型安全
- 避免使用 `Any` 导致的跨平台兼容性问题
- 支持字符串、数字、整数、布尔值四种类型

#### 4.2.6 AnalyticsConsent

`AnalyticsConsent` 接口提供隐私合规支持：

```kotlin
package tech.zhifu.app.myhub.analytics

interface AnalyticsConsent {
    fun isAnalyticsAllowed(): Boolean
    fun isPersonalizationAllowed(): Boolean
    fun updateConsent(
        analyticsAllowed: Boolean,
        personalizationAllowed: Boolean = false
    )
}
```

**设计要点**：

- 支持 GDPR、CCPA 等隐私法规
- 提供统计和个性化统计的分别控制
- 支持动态更新用户同意状态

#### 4.2.7 AnalyticsProviderFactory

`AnalyticsProviderFactory` 类使用 Registry 模式管理 Provider：

```kotlin
package tech.zhifu.app.myhub.analytics

class AnalyticsProviderFactory {
    private val creators = mutableMapOf<ProviderType, ProviderCreator>()

    fun register(type: ProviderType, creator: ProviderCreator)
    fun create(config: ProviderConfig): AnalyticsProvider
}
```

**设计要点**：

- 使用 Registry 模式，支持动态注册 Provider
- 平台特定的注册器通过 `AnalyticsProviderRegistrar` 注册 Provider
- 便于扩展新的 Provider

### 4.3 渠道框架设计

#### 4.3.1 目录结构

```text
core/analytics/src/
├── commonMain/                    # 所有平台和渠道共享的代码
│   ├── AnalyticsService.kt        # 统一统计接口
│   ├── AnalyticsManager.kt        # 核心管理器
│   ├── AnalyticsProvider.kt       # Provider 接口
│   ├── AnalyticsConfig.kt         # 配置类
│   ├── AnalyticsValue.kt          # 类型安全的统计值
│   ├── AnalyticsConsent.kt        # 隐私合规接口
│   ├── AnalyticsEvent.kt          # 事件模型
│   ├── AnalyticsEvents.kt         # 事件命名规范
│   ├── AnalyticsProviderFactory.kt # Provider 工厂
│   └── provider/
│       ├── CommonAnalyticsRegistrar.kt  # 通用注册器
│       └── ConsoleProvider.kt     # 控制台输出 Provider
│
├── androidMain/                   # Android 平台标准代码
│   └── di/
│       ├── AnalyticsModule.android.kt  # Android DI 模块
│       └── AnalyticsRegistrar.android.kt # expect/actual 实现
│
├── androidGooglePlayMain/         # Android + Google Play
│   └── provider/
│       ├── AndroidAnalyticsRegistrar.kt  # 注册 FirebaseProvider
│       └── FirebaseProvider.kt    # Firebase Provider 实现
│
├── androidUmengMain/              # Android + Umeng
│   └── provider/
│       ├── AndroidAnalyticsRegistrar.kt  # 注册 UmengProvider
│       └── UmengProvider.kt        # Umeng Provider 实现
│
├── androidChannelMain/            # Android + 默认渠道
│   └── provider/
│       └── AndroidAnalyticsRegistrar.kt  # 仅注册 ConsoleProvider
│
├── iosMain/                       # iOS 平台标准代码
│   └── di/
│       ├── AnalyticsModule.ios.kt
│       └── AnalyticsRegistrar.ios.kt
│
├── iosGooglePlayMain/             # iOS + Google Play
│   └── provider/
│       ├── IosAnalyticsRegistrar.kt
│       └── FirebaseProvider.kt
│
├── iosUmengMain/                  # iOS + Umeng
│   ├── cinterop/
│   │   └── umeng.def              # cinterop 定义文件
│   └── provider/
│       ├── IosAnalyticsRegistrar.kt
│       └── UmengProvider.kt
│
├── iosChannelMain/                # iOS + 默认渠道
│   └── provider/
│       └── IosAnalyticsRegistrar.kt
│
├── jsMain/                        # JS 平台标准代码
│   └── di/
│       ├── AnalyticsModule.js.kt
│       └── AnalyticsRegistrar.js.kt
│
├── jsGooglePlayMain/              # JS + Google Play
│   └── provider/
│       ├── JsAnalyticsRegistrar.kt
│       └── FirebaseProvider.kt
│
├── jsChannelMain/                 # JS + 默认渠道
│   └── provider/
│       └── JsAnalyticsRegistrar.kt
│
├── jvmMain/                       # JVM 平台标准代码
│   ├── di/
│   │   ├── AnalyticsModule.jvm.kt
│   │   └── AnalyticsRegistrar.jvm.kt
│   └── provider/
│       └── FileProvider.kt        # File Provider 实现
│
└── wasmJsMain/                    # WASM 平台标准代码
    └── di/
        ├── AnalyticsModule.wasmJs.kt
        └── AnalyticsRegistrar.wasmJs.kt
```

#### 4.3.2 渠道注册机制

**平台注册器（Platform Registrar）**：

```kotlin
// commonMain/kotlin/.../di/AnalyticsRegistrar.kt
internal expect fun getAnalyticsRegistrar(): AnalyticsProviderRegistrar?

// androidMain/kotlin/.../di/AnalyticsRegistrar.android.kt
internal actual fun getAnalyticsRegistrar(): AnalyticsProviderRegistrar? {
    return AndroidAnalyticsRegistrar()  // 返回 {platform}{Channel}Main 中的实现
}
```

**渠道注册器（Channel Registrar）**：

```kotlin
// androidGooglePlayMain/kotlin/.../provider/AndroidAnalyticsRegistrar.kt
class AndroidAnalyticsRegistrar : AnalyticsProviderRegistrar {
    override fun register(factory: AnalyticsProviderFactory) {
        factory.register(ProviderType.FIREBASE) { config ->
            FirebaseProvider(config)
        }
    }
}

// androidChannelMain/kotlin/.../provider/AndroidAnalyticsRegistrar.kt
class AndroidAnalyticsRegistrar : AnalyticsProviderRegistrar {
    override fun register(factory: AnalyticsProviderFactory) {
        // 仅注册 ConsoleProvider（已在 CommonAnalyticsRegistrar 中注册）
    }
}
```

### 4.4 模块结构

```text
core/analytics/
├── build.gradle.kts              # 模块构建配置
├── README.md                     # 模块说明文档
├── docs/                         # 架构设计文档
│   ├── myhub-analytics-infra-v1.0.md
│   ├── CHANNEL_FRAMEWORK_DESIGN.md
│   └── TEST_SUMMARY.md
└── src/
    ├── commonMain/               # 公共接口和实现
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       ├── AnalyticsService.kt          # 统一统计接口
    │       ├── AnalyticsManager.kt          # 核心管理器
    │       ├── AnalyticsProvider.kt         # Provider 接口
    │       ├── AnalyticsConfig.kt           # 配置类
    │       ├── AnalyticsValue.kt            # 类型安全的统计值
    │       ├── AnalyticsConsent.kt         # 隐私合规接口
    │       ├── AnalyticsEvent.kt           # 事件模型
    │       ├── AnalyticsEvents.kt          # 事件命名规范
    │       ├── AnalyticsProviderFactory.kt  # Provider 工厂
    │       └── provider/
    │           ├── CommonAnalyticsRegistrar.kt  # 通用注册器
    │           └── ConsoleProvider.kt       # 控制台输出 Provider
    │
    ├── androidMain/              # Android 平台标准代码
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── di/
    │           ├── AnalyticsModule.android.kt
    │           └── AnalyticsRegistrar.android.kt
    │
    ├── androidGooglePlayMain/    # Android + Google Play
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── provider/
    │           ├── AndroidAnalyticsRegistrar.kt
    │           └── FirebaseProvider.kt
    │
    ├── androidUmengMain/         # Android + Umeng
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── provider/
    │           ├── AndroidAnalyticsRegistrar.kt
    │           └── UmengProvider.kt
    │
    ├── androidChannelMain/       # Android + 默认渠道
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── provider/
    │           └── AndroidAnalyticsRegistrar.kt
    │
    ├── iosMain/                  # iOS 平台标准代码
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── di/
    │           ├── AnalyticsModule.ios.kt
    │           └── AnalyticsRegistrar.ios.kt
    │
    ├── iosGooglePlayMain/        # iOS + Google Play
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── provider/
    │           ├── IosAnalyticsRegistrar.kt
    │           └── FirebaseProvider.kt
    │
    ├── iosUmengMain/             # iOS + Umeng
    │   ├── cinterop/
    │   │   └── tech/zhifu/app/myhub/analytics/provider/
    │   │       └── umeng.def                # cinterop 定义文件
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── provider/
    │           ├── IosAnalyticsRegistrar.kt
    │           └── UmengProvider.kt
    │
    ├── iosChannelMain/           # iOS + 默认渠道
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── provider/
    │           └── IosAnalyticsRegistrar.kt
    │
    ├── jsMain/                   # JS 平台标准代码
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── di/
    │           ├── AnalyticsModule.js.kt
    │           └── AnalyticsRegistrar.js.kt
    │
    ├── jsGooglePlayMain/         # JS + Google Play
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── provider/
    │           ├── JsAnalyticsRegistrar.kt
    │           └── FirebaseProvider.kt
    │
    ├── jsChannelMain/            # JS + 默认渠道
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── provider/
    │           └── JsAnalyticsRegistrar.kt
    │
    ├── jvmMain/                  # JVM 平台标准代码
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       ├── di/
    │       │   ├── AnalyticsModule.jvm.kt
    │       │   └── AnalyticsRegistrar.jvm.kt
    │       └── provider/
    │           └── FileProvider.kt          # File Provider 实现
    │
    ├── wasmJsMain/               # WASM 平台标准代码
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── di/
    │           ├── AnalyticsModule.wasmJs.kt
    │           └── AnalyticsRegistrar.wasmJs.kt
    │
    └── commonTest/               # 公共测试代码
        └── kotlin/tech/zhifu/app/myhub/analytics/
            ├── AnalyticsManagerTest.kt      # AnalyticsManager 测试
            ├── AnalyticsValueTest.kt        # AnalyticsValue 测试
            ├── AnalyticsEventTest.kt        # AnalyticsEvent 测试
            ├── ConsoleProviderTest.kt       # ConsoleProvider 测试
            ├── BaseAnalyticsProviderTest.kt # BaseAnalyticsProvider 测试
            └── MockProvider.kt              # Mock Provider
```

---

## 5. 实现细节

### 5.1 关键 API 设计

#### 5.1.1 AnalyticsService 使用示例

```kotlin
import tech.zhifu.app.myhub.analytics.AnalyticsService
import tech.zhifu.app.myhub.analytics.AnalyticsEvent
import tech.zhifu.app.myhub.analytics.AnalyticsEvents
import tech.zhifu.app.myhub.analytics.AnalyticsValue

class DashboardViewModel(
    private val analyticsService: AnalyticsService
) : ViewModel() {

    fun onScreenShown() {
        analyticsService.setScreen("Dashboard", "DashboardScreen")
        analyticsService.logEvent(
            AnalyticsEvent.screenView("Dashboard", "DashboardScreen")
        )
    }

    fun onCardCreated(card: Card) {
        analyticsService.logEvent(
            AnalyticsEvent(
                name = AnalyticsEvents.CARD_CREATED,
                parameters = mapOf(
                    "card_id" to AnalyticsValue.Str(card.id),
                    "card_type" to AnalyticsValue.Str(card.type.name)
                )
            )
        )
    }

    fun onUserLogin(userId: String) {
        analyticsService.setUserId(userId)
        analyticsService.logEvent(
            AnalyticsEvent.userLogin("email")
        )
    }
}
```

#### 5.1.2 AnalyticsManager 初始化

```kotlin
import tech.zhifu.app.myhub.analytics.AnalyticsManager
import tech.zhifu.app.myhub.analytics.AnalyticsConfig
import tech.zhifu.app.myhub.analytics.Region
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.ProviderType

// 在 Koin 中配置
val analyticsModule = module {
    single<AnalyticsConfig> {
        AnalyticsConfig(
            region = Region.DOMESTIC, // 或 Region.OVERSEAS
            enabled = true,
            debugMode = BuildConfig.DEBUG,
            providers = listOf(
                // Provider 配置（平台特定的注册器会自动注册）
            )
        )
    }

    single<AnalyticsManager> {
        AnalyticsManager(
            config = get(),
            consent = get(),
            providerFactory = get()
        )
    }
}

// 初始化
GlobalScope.launch {
    delay(100)
    getKoin().get<AnalyticsManager>().initialize()
}
```

#### 5.1.3 AnalyticsValue 使用示例

```kotlin
import tech.zhifu.app.myhub.analytics.AnalyticsValue

// 创建类型安全的统计值
val cardId = AnalyticsValue.Str("card_123")
val cardCount = AnalyticsValue.Int(5L)
val price = AnalyticsValue.Num(99.99)
val isPremium = AnalyticsValue.Bool(true)

// 从 Any 转换（兼容性）
val value = AnalyticsValue.from(42) // AnalyticsValue.Int(42L)
val value2 = AnalyticsValue.from("test") // AnalyticsValue.Str("test")
```

#### 5.1.4 AnalyticsConsent 使用示例

```kotlin
import tech.zhifu.app.myhub.analytics.AnalyticsConsent
import tech.zhifu.app.myhub.analytics.DefaultAnalyticsConsent

// 创建隐私合规实例
val consent = DefaultAnalyticsConsent(
    analyticsAllowed = true,
    personalizationAllowed = false
)

// 更新用户同意状态
consent.updateConsent(
    analyticsAllowed = true,
    personalizationAllowed = false
)

// 检查是否允许统计
if (consent.isAnalyticsAllowed()) {
    analyticsService.logEvent(event)
}
```

### 5.2 渠道框架实现

#### 5.2.1 平台注册器实现

**Android 平台**：

```kotlin
// androidMain/kotlin/.../di/AnalyticsRegistrar.android.kt
internal actual fun getAnalyticsRegistrar(): AnalyticsProviderRegistrar? {
    return AndroidAnalyticsRegistrar()  // 返回 {platform}{Channel}Main 中的实现
}

// androidGooglePlayMain/kotlin/.../provider/AndroidAnalyticsRegistrar.kt
class AndroidAnalyticsRegistrar : AnalyticsProviderRegistrar {
    override fun register(factory: AnalyticsProviderFactory) {
        factory.register(ProviderType.FIREBASE) { config ->
            FirebaseProvider(config)
        }
    }
}

// androidUmengMain/kotlin/.../provider/AndroidAnalyticsRegistrar.kt
class AndroidAnalyticsRegistrar : AnalyticsProviderRegistrar {
    override fun register(factory: AnalyticsProviderFactory) {
        factory.register(ProviderType.UMENG) { config ->
            UmengProvider(config)
        }
    }
}
```

**iOS 平台**：

```kotlin
// iosMain/kotlin/.../di/AnalyticsRegistrar.ios.kt
internal actual fun getAnalyticsRegistrar(): AnalyticsProviderRegistrar? {
    return IosAnalyticsRegistrar()  // 返回 {platform}{Channel}Main 中的实现
}

// iosGooglePlayMain/kotlin/.../provider/IosAnalyticsRegistrar.kt
class IosAnalyticsRegistrar : AnalyticsProviderRegistrar {
    override fun register(factory: AnalyticsProviderFactory) {
        factory.register(ProviderType.FIREBASE) { config ->
            FirebaseProvider(config)
        }
    }
}

// iosUmengMain/kotlin/.../provider/IosAnalyticsRegistrar.kt
class IosAnalyticsRegistrar : AnalyticsProviderRegistrar {
    override fun register(factory: AnalyticsProviderFactory) {
        factory.register(ProviderType.UMENG) { config ->
            UmengProvider(config)
        }
    }
}
```

### 5.3 Provider 实现示例

#### 5.3.1 ConsoleProvider 实现

```kotlin
package tech.zhifu.app.myhub.analytics.provider

class ConsoleProvider(
    private val logger: Logger = logger("Analytics.ConsoleProvider")
) : BaseAnalyticsProvider() {
    override val name = "Console"
    override val supportedPlatforms = setOf(
        Platform.ANDROID, Platform.IOS, Platform.JVM, Platform.JS, Platform.WASM
    )
    override val supportedRegions = setOf(Region.DOMESTIC, Region.OVERSEAS)

    override suspend fun initialize(config: ProviderConfig) {
        markAsReady()
    }

    override fun doLogEvent(event: AnalyticsEvent) {
        logger.info { "Event: ${event.name}" }
    }
}
```

#### 5.3.2 FirebaseProvider 实现（Android）

```kotlin
package tech.zhifu.app.myhub.analytics.provider

import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseProvider(
    private val config: ProviderConfig
) : BaseAnalyticsProvider() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override val name = "Firebase"
    override val supportedPlatforms = setOf(
        Platform.ANDROID, Platform.IOS, Platform.JS, Platform.WASM
    )
    override val supportedRegions = setOf(Region.OVERSEAS)

    override suspend fun initialize(config: ProviderConfig) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        markAsReady()
    }

    override fun doLogEvent(event: AnalyticsEvent) {
        val bundle = Bundle().apply {
            event.parameters.forEach { (key, value) ->
                when (value) {
                    is AnalyticsValue.Str -> putString(key, value.value)
                    is AnalyticsValue.Int -> putLong(key, value.value)
                    is AnalyticsValue.Num -> putDouble(key, value.value)
                    is AnalyticsValue.Bool -> putBoolean(key, value.value)
                }
            }
        }
        firebaseAnalytics.logEvent(event.name, bundle)
    }
}
```

### 5.4 使用示例

#### 5.4.1 基本使用

```kotlin
import tech.zhifu.app.myhub.analytics.AnalyticsService
import tech.zhifu.app.myhub.analytics.AnalyticsEvent
import tech.zhifu.app.myhub.analytics.AnalyticsEvents
import tech.zhifu.app.myhub.analytics.AnalyticsValue

class MyViewModel(
    private val analyticsService: AnalyticsService
) : ViewModel() {

    fun onScreenShown() {
        analyticsService.setScreen("MyScreen")
        analyticsService.logEvent(
            AnalyticsEvent.screenView("MyScreen", "MyScreenClass")
        )
    }

    fun onButtonClicked(buttonId: String) {
        analyticsService.logEvent(
            AnalyticsEvent(
                name = "button_clicked",
                parameters = mapOf(
                    "button_id" to AnalyticsValue.Str(buttonId)
                )
            )
        )
    }
}
```

#### 5.4.2 批量事件

```kotlin
val events = listOf(
    AnalyticsEvent.screenView("Screen1"),
    AnalyticsEvent.screenView("Screen2"),
    AnalyticsEvent(AnalyticsEvents.CARD_CREATED, mapOf(
        "card_id" to AnalyticsValue.Str("123")
    ))
)

analyticsService.logEvents(events)
```

#### 5.4.3 用户属性

```kotlin
analyticsService.setUserId("user_123")
analyticsService.setUserProperty("user_type", AnalyticsValue.Str("premium"))
analyticsService.setUserProperty("login_count", AnalyticsValue.Int(10L))
```

### 5.5 测试策略

#### 5.5.1 单元测试

**AnalyticsManagerTest**：

```kotlin
package tech.zhifu.app.myhub.analytics

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnalyticsManagerTest {
    @Test
    fun testInitialize() = runTest {
        val config = AnalyticsConfig(
            region = Region.DOMESTIC,
            enabled = true,
            providers = emptyList()
        )
        val consent = DefaultAnalyticsConsent()
        val factory = AnalyticsProviderFactory()

        val manager = AnalyticsManager(config, consent, factory)
        manager.initialize()

        // 验证初始化完成
    }

    @Test
    fun testLogEvent() = runTest {
        val manager = createTestManager()
        manager.initialize()

        val event = AnalyticsEvent.screenView("TestScreen")
        manager.logEvent(event)

        // 验证事件已记录
    }
}
```

---

## 6. 实施计划

### 6.1 实施阶段

#### 阶段 1：核心框架搭建（已完成）

- ✅ 创建 `core/analytics` 模块
- ✅ 定义核心接口（AnalyticsService、AnalyticsProvider）
- ✅ 实现 AnalyticsManager
- ✅ 实现 AnalyticsConfig 和配置管理
- ✅ 创建 Koin DI 模块
- ✅ 实现 ConsoleProvider（所有平台默认支持）
- ✅ 实现 FileProvider（用于 Desktop QA）
- ✅ 实现 AnalyticsValue 类型安全机制
- ✅ 实现 AnalyticsConsent 隐私合规支持
- ✅ 实现事件缓冲机制
- ✅ 编写单元测试

#### 阶段 2：渠道框架实现（已完成）

- ✅ 渠道框架设计（平台+渠道双维度变体系统）
- ✅ 源集注入机制（通过构建变体动态注入）
- ✅ 渠道互斥性保证（编译时互斥）
- ✅ 平台特定注册器（expect/actual + 渠道变体）

#### 阶段 3：Firebase 集成（已完成）

- ✅ Firebase Android SDK 集成
- ✅ Firebase iOS SDK 集成
- ✅ Firebase Web SDK 集成（JS/WASM）
- ✅ FirebaseProvider 实现（Android、iOS、Web）
- ✅ 自动配置（google-services.json、GoogleService-Info.plist）

#### 阶段 4：Umeng 集成（已完成）

- ✅ Umeng Android SDK 集成
- ✅ Umeng iOS SDK 集成（cinterop）
- ✅ UmengProvider 实现（Android、iOS）
- ✅ iOS cinterop 配置和优化
- ✅ CInterop Commonization 启用

#### 阶段 5：文档完善（已完成）

- ✅ 创建架构设计文档
- ✅ 完善使用文档
- ✅ 添加代码示例
- ✅ 添加最佳实践指南

### 6.2 里程碑

| 里程碑            | 目标日期   | 状态     |
| ----------------- | ---------- | -------- |
| 核心框架完成      | 2026-01-13 | ✅ 已完成 |
| 渠道框架完成      | 2026-01-13 | ✅ 已完成 |
| Firebase 集成完成 | 2026-01-13 | ✅ 已完成 |
| Umeng 集成完成    | 2026-01-13 | ✅ 已完成 |
| 文档完善          | 2026-01-13 | ✅ 已完成 |

---

## 7. 风险评估

### 7.1 技术风险

#### 风险 1：统计 SDK API 变更

**风险描述**：Firebase、Umeng 等统计 SDK 更新可能导致 API 变更，影响 Provider 实现。

**影响程度**：中

**应对措施**：

- 使用稳定的统计 SDK API，避免使用实验性 API
- 定期更新统计 SDK 版本，及时适配 API 变更
- 编写测试用例，确保 Provider 实现正确
- 关注统计 SDK 的更新日志和迁移指南

#### 风险 2：iOS cinterop 兼容性

**风险描述**：iOS cinterop 配置可能在不同环境下出现问题，影响 Umeng Provider。

**影响程度**：中

**应对措施**：

- 使用稳定的 cinterop 配置
- 提供详细的 cinterop 配置文档
- 启用 CInterop Commonization，减少配置复杂度
- 提供 IDE 索引问题的解决方案

### 7.2 维护风险

#### 风险 1：渠道框架复杂度

**风险描述**：渠道框架涉及多个源集，可能导致代码复杂度增加。

**影响程度**：中

**应对措施**：

- 保持清晰的模块结构，每个渠道独立实现
- 使用清晰的命名规范，避免混淆
- 编写清晰的文档，说明渠道框架的工作原理
- 定期重构，保持代码简洁

#### 风险 2：Provider 扩展性限制

**风险描述**：未来可能需要添加更多统计服务，可能超出模块职责范围。

**影响程度**：低

**应对措施**：

- 使用 Registry 模式，易于添加新的 Provider
- 保持 Provider 接口稳定，新 Provider 只需实现接口
- 编写 Provider 实现指南，降低新 Provider 接入成本

### 7.3 合规性风险

#### 风险 1：隐私合规

**风险描述**：隐私法规可能发生变化，需要及时更新合规机制。

**影响程度**：中

**应对措施**：

- 提供统一的隐私合规接口，便于更新
- 关注隐私法规变化，及时更新合规机制
- 提供隐私合规最佳实践文档

---

## 8. 附录

### 8.1 相关文档

- [MyHub 基础设施文档](../../../docs/infra/myhub-infra.md)
- [MyHub 架构设计文档规范](../../../docs/infra/myhub-infra-rules.md)
- [渠道框架设计文档](CHANNEL_FRAMEWORK_DESIGN.md)
- [Firebase Analytics 官方文档](https://firebase.google.com/docs/analytics)
- [Umeng Analytics 官方文档](https://developer.umeng.com/docs/67966/detail/193837)

### 8.2 参考实现

- `core/analytics` 模块实现代码
- 其他 core 模块的架构设计文档（如 `core/logger`、`core/platform`）

### 8.3 术语表

| 术语                       | 说明                                                             |
| -------------------------- | ---------------------------------------------------------------- |
| AnalyticsService           | 统一统计服务接口，业务代码通过此接口进行统计上报                 |
| AnalyticsProvider          | 统计服务提供商接口，各个统计 SDK 的适配器实现此接口              |
| AnalyticsManager           | 统计管理器，管理多个统计服务提供商                               |
| AnalyticsValue             | 类型安全的统计值类型，避免使用 Any 导致的跨平台兼容性问题        |
| AnalyticsConsent           | 隐私合规接口，用于处理 GDPR、CCPA 等隐私法规要求                 |
| AnalyticsEvent             | 统计事件模型，包含事件名称、参数等信息                           |
| ProviderType               | 提供商类型枚举（CONSOLE、FILE、FIREBASE、UMENG）                 |
| AnalyticsProviderFactory   | Provider 工厂，使用 Registry 模式管理 Provider                   |
| AnalyticsProviderRegistrar | Provider 注册器接口，各平台模块实现此接口注册平台特定的 Provider |
| 渠道框架                   | 平台+渠道双维度变体系统，支持通过构建变体选择不同的统计服务      |
| 事件缓冲                   | Provider 初始化期间的事件自动缓冲机制，避免事件丢失              |

### 8.4 常见问题

#### Q1: 如何添加新的统计服务 Provider？

**A**: 添加新 Provider 需要：

1. 实现 `AnalyticsProvider` 接口或继承 `BaseAnalyticsProvider`
2. 在 `AnalyticsProviderFactory` 中注册 Provider
3. 在平台特定的注册器中注册 Provider（如需要）
4. 在 `ProviderType` 枚举中添加新的类型
5. 编写测试用例

#### Q2: 渠道框架如何工作？

**A**: 渠道框架使用 KMP Source Sets + 构建变体系统：

1. **构建时选择**：根据 `appChannel` 参数选择渠道
2. **源集注入**：Gradle 自动注入对应的源集（如 `androidGooglePlayMain`）
3. **类替换**：渠道源集中的类会替换平台标准源集中的同名类
4. **互斥性保证**：编译时保证只有一个渠道源集被注入

详细说明请参考：[渠道框架设计文档](CHANNEL_FRAMEWORK_DESIGN.md)

#### Q3: 如何处理隐私合规？

**A**: 使用 `AnalyticsConsent` 接口：

```kotlin
val consent = DefaultAnalyticsConsent(
    analyticsAllowed = true,
    personalizationAllowed = false
)

// 在 AnalyticsManager 中检查
if (consent.isAnalyticsAllowed()) {
    analyticsService.logEvent(event)
}
```

#### Q4: 事件缓冲机制如何工作？

**A**: `BaseAnalyticsProvider` 提供事件缓冲机制：

1. **初始化前**：事件被添加到缓冲队列
2. **初始化完成**：调用 `markAsReady()` 标记为就绪
3. **自动上报**：就绪后自动上报缓冲的事件
4. **后续事件**：直接上报，不再缓冲

#### Q5: 如何测试统计功能？

**A**: 使用 `ConsoleProvider` 或 `MockProvider`：

```kotlin
// 使用 ConsoleProvider（所有平台都支持）
val config = AnalyticsConfig(
    providers = listOf(
        ProviderConfig(type = ProviderType.CONSOLE, enabled = true)
    )
)

// 使用 MockProvider（测试专用）
val mockProvider = MockProvider()
// 验证事件是否被记录
```

---

## 文档结束

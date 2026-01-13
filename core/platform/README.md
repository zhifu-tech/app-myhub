# Core Platform Module

本模块用于**规范**和**实现** MyHub 应用的平台抽象基础设施（Platform Infra），为各功能模块**提供统一的跨平台抽象能力**。它基于 **Kotlin Multiplatform expect/actual 机制**，实现了**平台检测**、**系统属性访问**、**平台特定依赖注入**等特性，并提供了面向 KMP 场景的**统一平台抽象接口**，方便在 **Android、iOS、JVM、JS、WASM** 等多端项目中集成和使用。

**重要说明**：`core/platform` 是一个**混合（Mixed）模块**，与其他单一功能的 core 模块不同，它包含多个平台相关的功能集合，这些功能都与平台相关，属于同一领域，便于统一管理和维护。

## 核心组件

### 1. Platform 接口

统一的平台检测接口，提供平台信息：

- **`getPlatform()`**：获取当前运行平台的实例
- **`name`**：平台名称和版本信息（如 "Android 34"、"iOS 17.0"、"Java 17.0.1"）

### 2. SystemProperty 函数

跨平台的系统属性访问函数：

- **`getSystemProperty(key: String)`**：获取系统属性值
- **平台支持**：Android/JVM 返回实际值，iOS/Web 返回 `null`（不支持）
- **安全处理**：空 key 返回 `null`，不会抛出异常

### 3. PlatformModule 函数

平台特定的依赖注入模块：

- **`platformModule()`**：返回平台特定的 Koin 模块
- **平台配置**：Android 注册 `Context`，iOS/JVM/Web 可注册平台特定服务
- **统一集成**：通过 Koin 模块系统统一管理平台依赖

## 使用示例

```kotlin
// 1. 配置 Koin，包含平台特定模块
import org.koin.core.context.startKoin
import tech.zhifu.app.myhub.di.platformModule

startKoin {
    modules(
        platformModule(), // 平台特定的模块
        // 其他模块...
    )
}

// 2. 获取当前平台信息
import tech.zhifu.app.myhub.getPlatform

val platform = getPlatform()
println("当前平台: ${platform.name}")
// Android: "Android 34"
// iOS: "iOS 17.0"
// JVM: "Java 17.0.1"
// JS: "Web with Kotlin/JS"
// WASM: "Web with Kotlin/WASM"

// 3. 访问系统属性（平台特定）
import tech.zhifu.app.myhub.system.getSystemProperty

val javaVersion = getSystemProperty("java.version")
if (javaVersion != null) {
    println("Java 版本: $javaVersion")
} else {
    println("当前平台不支持系统属性访问")
}
// Android/JVM: 返回实际值，如 "17.0.1"
// iOS/Web: 返回 null（不支持）

// 4. 使用平台特定的依赖注入（Android 示例）
import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyService : KoinComponent {
    // Android 平台上可以获取 Context
    private val context: Context by inject()
    
    fun doSomething() {
        // 使用 context...
    }
}
```

## 文档

- [MyHub 平台抽象模块方案设计](./docs/myhub-platform-infra-v1.0.md)

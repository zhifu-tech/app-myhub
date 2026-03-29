# Core Platform Compose Module

本模块用于**规范**和**实现** MyHub 应用的 Compose UI 平台抽象基础设施（Platform Compose Infra），为各功能模块**提供统一的跨平台 Compose UI 抽象能力**。它基于 **Compose Multiplatform** 和 **Kotlin Multiplatform expect/actual 机制**，实现了**主题管理**、**语言环境**、**窗口尺寸检测**、**手势交互**等特性，并提供了面向 KMP 场景的**统一 Compose UI 平台抽象接口**，方便在 **Android、iOS、JVM、JS、WASM** 等多端项目中集成和使用。

**重要说明**：`core/platform-compose` 是一个**混合（Mixed）模块**，与其他单一功能的 core 模块不同，它包含多个 Compose UI 平台相关的功能集合，这些功能都与 Compose UI 平台相关，属于同一领域，便于统一管理和维护。

## 文档

- [MyHub Compose UI 平台抽象模块方案设计](./docs/myhub-platform-compose-infra-v1.0.md)

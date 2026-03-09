# core/startup

一个基于 Kotlin Multiplatform 的启动初始化框架模块，为 MyHub 提供跨平台一致的启动任务编排能力，支持任务分级执行、依赖顺序控制、失败重试与启动后资源清理。

## 目标

- 统一定义启动任务接口
- 支持关键任务与非关键任务分级执行
- 支持依赖排序、失败隔离、重试策略
- 提供启动后资源清理，避免长期保留任务对象

## 核心组件

- `StartupTask`：启动任务契约
- `StartupTaskIds`：统一任务 ID 常量
- `StartupRetryPolicy`：重试策略（指数退避 + jitter）
- `StartupOrchestrator`：任务调度与执行入口
- `AppStartupScope`：启动期协程作用域
- `startupModule()`：Koin 注册入口

## 使用方式

1. 在业务模块中实现 `StartupTask`

- 示例：`core/analytics`、`datastore/bootstrap`
- 对外仅以 `StartupTask` 类型注册，不暴露实现细节

2. 在 DI 中接入 `startupModule()`

- `composeApp` 的 `initKoin()` 中加载该模块

3. 在平台入口触发编排

- `koin.get<StartupOrchestrator>().start()`

## 执行语义

- `critical = true`：失败会中断启动流程
- `critical = false`：失败记录后继续执行其他任务
- 可重试错误按策略重试；不可重试错误快速失败
- 编排完成后自动清理任务引用并关闭启动作用域

## 测试

```bash
./gradlew :core:startup:jvmTest
```

## 设计文档

- [myhub-startup-initialization-infra-v1.0.md](./docs/myhub-startup-initialization-infra-v1.0.md)

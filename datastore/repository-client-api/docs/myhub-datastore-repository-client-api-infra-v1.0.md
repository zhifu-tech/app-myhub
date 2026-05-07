# Core Datastore Repository Client API Module

本模块用于**规范**和**实现** MyHub 应用仓库客户端 API 基础设施（Datastore Repository Client API Infra），为各功能模块**提供统一的 Store 抽象能力**。它基于 **Store5**，实现了**读写响应封装**、**接口抽象**、**客户端依赖隔离**等特性，并提供了面向 KMP 场景的**统一仓库客户端契约**，方便在 **客户端业务模块** 中集成和使用。

## 核心组件

### 1. `CardStore`

卡片仓库的客户端接口。

- **统一读写**：封装 Store5 的读写流程
- **业务聚合**：作为上层卡片数据入口

### 2. `StoreReadResponse`

读取结果响应类型。

- **状态表达**：表示成功、缓存命中或失败等状态
- **类型安全**：减少业务层分支判断

### 3. `StoreWriteResponse`

写入结果响应类型。

- **写入反馈**：表达写入成功或失败结果
- **统一处理**：方便调用方处理同步逻辑

## 使用示例

```kotlin
// 1. 注入 Store
val store: CardStore = cardStore

// 2. 读取数据
val result = store.get(key)

// 3. 写入数据
store.put(key, value)
```

## 模块职责

- 定义 `CardStore` 接口（基于 Store5）
- 暴露必要的 Store5 类型（`StoreReadResponse`、`StoreWriteResponse`）

## 设计原则

- **简洁接口**：只暴露类型安全的方法（如 `streamCards`、`getCard`）
- **隐藏实现细节**：内部的 `CardStoreKey`、`CardOutput` 等类型不对外暴露
- **依赖倒置**：feature 模块依赖 API，实现类在 repository-client 模块

## 模块结构

```
repository-client-api/
└── src/commonMain/kotlin/.../store/
    └── CardStore.kt       # CardStore 接口定义
```

## 依赖关系

```
repository-client-api
├── datastore:model        # 领域模型（Card 等）
├── store5                 # Store5 API 类型
└── kotlinx-coroutines     # Flow
```

## 使用方式

其他模块（如 feature 模块）只需依赖 API 模块：

```kotlin
// feature/xxx/build.gradle.kts
dependencies {
    implementation(projects.datastore.repositoryClientApi)
}
```

实际使用时通过 DI 注入实现类：

```kotlin
// ViewModel 中使用
class DashboardViewModel(private val cardStore: CardStore) {
    fun loadCards(userId: String) {
        cardStore.streamCards(userId, refresh = true)
            .collect { response -> ... }
    }
}
```

## 文档

- [MyHub 数据层架构](../../docs/myhub_architecture.md)
- [Datastore 模块总览](../README.md)

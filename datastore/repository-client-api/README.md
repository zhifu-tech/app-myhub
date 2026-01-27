# Repository Client API

数据仓库客户端 API 模块，定义 Store 接口。

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

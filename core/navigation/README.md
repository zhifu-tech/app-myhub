# Core Navigation Module

本模块用于**规范**和**实现** MyHub 应用的导航基础设施（Navigation Infra），为各功能模块**提供统一、可扩展的导航能力**。它基于 **Jetpack Compose Navigation 3**，实现了**多导航栈**、**状态保存与恢复**、**类型安全的导航参数传递**等特性，并提供了面向 KMP 场景的**可组合架构接口**，方便在 **Compose 多端项目**中集成和扩展。

## 核心组件

### 1. AppNavKey / FeatureNavKey

导航键定义，用于标识导航目的地：

- **AppNavKey**：应用级导航键（Dashboard、Profile）
- **FeatureNavKey**：功能导航键（CardDetail、AllCards）

### 2. AppNavigationState

应用导航状态管理，维护双层返回栈：

- **AppStack**：应用级导航项的返回栈
- **SubStacks**：每个应用级导航项对应的子返回栈

### 3. AppNavigator

导航器，处理导航逻辑：

- `navigate(key)`：导航到指定的 NavKey
- `goBack()`：返回操作

## 使用示例

```kotlin
// 1. 初始化导航状态
val navigationState = rememberAppNavigationState(
    startKey = AppNavKey.Dashboard,
    appKeys = setOf(AppNavKey.Dashboard, AppNavKey.Profile)
)
val navigator = remember { AppNavigator(navigationState) }

// 2. 注册各功能入口
val entryProvider = entryProvider {
    dashboardEntry(navigator)
    profileEntry(navigator)
}

// 3. 渲染导航
val entries = navigationState.toEntries(entryProvider)
NavDisplay(
    entries = entries,
    onBack = { navigator.goBack() }
)

// 4. Dashboard 调用导航
fun EntryProviderScope<NavKey>.dashboardEntry(navigator: AppNavigator) {
    entry<AppNavKey.Dashboard> {
        DashboardScreen(
            onNavigateToCardDetail = { cardId ->
                navigator.navigate(FeatureNavKey.CardDetail(cardId))
            },
        )
    }
}

// 5. CardDetail 调用导航
@Composable
fun DashboardScreen(onNavigateToCardDetail: (String) -> Unit) {
    Card(onClick = { onNavigateToCardDetail(card.id) }) {}
}
```

## 文档

- [MyHub 导航方案设计](./docs/myhub-nav-infra.md)

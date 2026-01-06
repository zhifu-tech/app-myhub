# Analytics 集成测试指南

## ✅ 集成状态

ConsoleProvider 已成功集成到 composeApp 中。

## 📋 集成内容

### 1. 依赖配置

- ✅ 在 `composeApp/build.gradle.kts` 中添加了 `core:analytics` 依赖

### 2. Koin 配置

- ✅ 在 `Koin.kt` 中配置了 `analyticsModule`
- ✅ 平台特定的 Provider 注册器已内置在 `core:analytics` 模块中
- ✅ JVM 平台自动注册 ConsoleProvider 和 FileProvider
- ✅ 应用启动时自动初始化统计服务

### 3. 使用示例

- ✅ 在 `AppViewModel` 中添加了统计埋点：
  - 应用启动事件（`app_started`）
  - 屏幕切换事件（`screen_view`）

## 🧪 测试步骤

### Desktop 平台测试

1. **运行 Desktop 应用**

   ```bash
   ./gradlew :composeApp:runDistributable
   ```

2. **查看控制台输出**
   - 应用启动时应该看到：
     ```
     [Analytics] Event: app_started
       environment: DEVELOPMENT
       version_type: FREE
     ```
   - 切换屏幕时应该看到：
     ```
     [Analytics] Screen: dashboard (class: Dashboard)
     [Analytics] Event: screen_view
       screen_name: dashboard
       screen_class: Dashboard
     ```

### 验证统计功能

1. **启动应用**

   - 观察控制台是否有 `app_started` 事件

2. **导航到不同屏幕**

   - Dashboard → Settings → Profile
   - 每次切换应该看到 `screen_view` 事件

3. **检查事件参数**
   - 验证事件参数是否正确
   - 验证屏幕名称和类名是否正确

## 📝 代码示例

### 在 ViewModel 中使用

```kotlin
class MyViewModel(
    private val analyticsService: AnalyticsService? = null
) {
    fun onButtonClick() {
        analyticsService?.logEvent(
            AnalyticsEvent(
                name = AnalyticsEvents.CARD_CREATED,
                parameters = mapOf(
                    "card_type" to AnalyticsValue.Str("note")
                )
            )
        )
    }
}
```

### 在 Composable 中使用

```kotlin
@Composable
fun MyScreen(
    analyticsService: AnalyticsService = koinInject()
) {
    LaunchedEffect(Unit) {
        analyticsService.setScreen("MyScreen")
        analyticsService.logEvent(
            AnalyticsEvent.screenView("MyScreen", "MyScreenComposable")
        )
    }
}
```

## 🔍 调试技巧

1. **检查统计是否启用**

   - 查看控制台是否有 `[Analytics]` 前缀的输出
   - 如果没有，检查 `AnalyticsConfig.enabled` 是否为 `true`

2. **检查 Provider 初始化**

   - 应用启动时应该看到：
     ```
     Analytics provider initialized: Console
     ```

3. **检查事件缓冲**
   - 如果事件在初始化前发送，会被缓冲
   - 初始化完成后会自动上报

## 🚀 下一步

1. ✅ ConsoleProvider 集成完成
2. ⏭️ 实现 FirebaseProvider(Android) 作为生产级样板
3. ⏭️ 在更多 Feature 模块中添加统计埋点

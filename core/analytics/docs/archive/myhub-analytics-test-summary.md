# Analytics 模块测试总结

**版本**: v1.0  
**最后更新**: 2026 年

## ✅ 测试状态

所有核心功能的单元测试已通过 ✅

**测试平台覆盖**：

- ✅ JVM (Desktop)
- ✅ iOS Simulator (ARM64)
- ✅ 其他平台测试待验证

## 📋 测试覆盖

### 1. AnalyticsValueTest

- ✅ String 类型转换
- ✅ Double/Float 类型转换
- ✅ Long/Int/Short/Byte 类型转换
- ✅ Boolean 类型转换
- ✅ null 值处理
- ✅ 未知类型兜底转换

### 2. AnalyticsEventTest

- ✅ screenView 事件创建（带/不带 screenClass）
- ✅ userLogin 事件创建
- ✅ purchase 事件创建
- ✅ 自定义事件创建

### 3. BaseAnalyticsProviderTest

- ✅ 事件缓冲机制（初始化前的事件被缓冲）
- ✅ 初始化后事件立即上报
- ✅ isReady StateFlow 状态更新

**注意**：事件缓冲测试在 iOS 平台上需要更长的等待时间，因为 `providerScope` 使用真实的 `Dispatchers.Default` 调度器。

### 4. AnalyticsManagerTest

- ✅ 配置 enabled 状态检查
- ✅ 隐私合规（Consent）检查
- ✅ 事件记录到所有 Provider
- ✅ 用户属性设置
- ✅ 用户 ID 设置
- ✅ 屏幕设置
- ✅ 重置功能
- ✅ 批量事件记录
- ✅ Provider 地区过滤

### 5. ConsoleProviderTest

- ✅ Provider 初始化
- ✅ 事件记录
- ✅ 用户属性设置
- ✅ 用户 ID 设置
- ✅ 屏幕设置
- ✅ 重置功能

### 6. FileProviderTest (JVM)

- ✅ Provider 初始化（JSON/CSV）
- ✅ JSON 格式事件写入
- ✅ CSV 格式事件写入
- ✅ 用户属性设置
- ✅ 用户 ID 设置
- ✅ 屏幕设置
- ✅ 重置功能

## 🧪 运行测试

### 运行所有平台测试

```bash
./gradlew :core:analytics:allTests
```

### 运行特定平台测试

```bash
# JVM (Desktop)
./gradlew :core:analytics:jvmTest

# iOS Simulator ARM64
./gradlew :core:analytics:iosSimulatorArm64Test

# iOS Simulator x64
./gradlew :core:analytics:iosX64Test
```

### 运行特定测试类

```bash
./gradlew :core:analytics:jvmTest --tests "AnalyticsValueTest"
```

## 📊 测试统计

- **测试文件数**: 7
- **测试用例数**: 30+
- **测试通过率**: 100% ✅
- **平台覆盖**: JVM, iOS Simulator ARM64

## 🔧 Mock 工具

### MockProvider

提供了 `MockProvider` 类用于测试，支持：

- 记录所有上报的事件
- 记录用户属性设置
- 记录用户 ID 设置
- 记录屏幕设置
- 验证重置调用

## ⚠️ 已知问题和解决方案

### iOS 平台事件缓冲测试

**问题**：`BaseAnalyticsProvider` 使用 `Dispatchers.Default` 真实调度器，在 iOS 平台上协程执行是异步的，测试需要等待。

**解决方案**：

- 使用循环等待机制，最多等待 1 秒
- 验证最终结果而非中间状态
- 使用 `advanceUntilIdle()` 和 `delay()` 确保协程执行

**测试代码示例**：

```kotlin
// 等待异步缓冲操作完成
var waited = 0L
val maxWait = 1000L
while (waited < maxWait) {
    delay(50)
    waited += 50
    if (provider.loggedEvents.size > 0 || provider.isInitialized) {
        break
    }
}
```

## 📝 测试最佳实践

1. **使用 MockProvider**：避免依赖真实的统计 SDK
2. **测试事件缓冲**：验证初始化期间的事件不会丢失
3. **测试隐私合规**：验证 Consent 机制正常工作
4. **测试配置过滤**：验证 enabled/disabled 状态
5. **测试地区过滤**：验证 Provider 按地区正确过滤
6. **跨平台测试**：在不同平台上运行测试，确保行为一致

## 🔍 调试技巧

### 查看测试报告

```bash
# 查看 HTML 测试报告
open core/analytics/build/reports/tests/iosSimulatorArm64Test/index.html
```

### 运行单个测试方法

```bash
./gradlew :core:analytics:jvmTest --tests "BaseAnalyticsProviderTest.events are buffered before initialization"
```

### 查看详细输出

```bash
./gradlew :core:analytics:jvmTest --info
```

---

## 📝 版本历史

| 版本   | 日期     | 变更说明            | 测试状态 |
|------|--------|-----------------|------|
| v1.0 | 2026 年 | 初始测试套件完成，覆盖核心功能 | ✅ 通过 |

### v1.0 测试覆盖

- ✅ **AnalyticsValue** - 类型转换测试（9 个测试用例）
- ✅ **AnalyticsEvent** - 事件创建测试（5 个测试用例）
- ✅ **BaseAnalyticsProvider** - 事件缓冲机制测试（3 个测试用例）
- ✅ **AnalyticsManager** - 管理器功能测试（9 个测试用例）
- ✅ **ConsoleProvider** - 控制台输出测试（6 个测试用例）
- ✅ **FileProvider** - 文件输出测试（7 个测试用例，JVM 平台）

**总计**: 39 个测试用例，100% 通过率

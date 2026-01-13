# 平台按需加载方案

## 问题
一次性加载所有平台（Android、iOS、Desktop、Web、Server）到工程中，导致 IDE 响应速度变慢。

## 解决方案
通过配置控制哪些平台被加载到工程中，只加载指定的平台。

## 配置方式

### 方式1: gradle.properties（推荐用于默认配置）

在 `gradle.properties` 中添加：

```properties
# 启用的平台（用逗号分隔，不区分大小写）
# 可选值: android, ios, desktop, js, wasmJs, server
# 默认: 所有平台（如果未设置）
enabledPlatforms=android
```

### 方式2: 命令行参数（推荐用于临时切换）

```bash
# 只加载 Android 平台
./gradlew build -PenabledPlatforms=android

# 加载多个平台
./gradlew build -PenabledPlatforms=android,desktop

# 使用 run.sh 脚本
./scripts/run.sh android -PenabledPlatforms=android
```

## 平台标识

- `android` - Android 平台
- `ios` - iOS 平台
- `desktop` - Desktop (JVM) 平台
- `js` - JavaScript 平台
- `wasmJs` - WebAssembly 平台
- `server` - Server (JVM) 平台

## 实现细节

### 1. settings.gradle.kts
- 读取 `enabledPlatforms` 属性
- 条件性地包含平台相关模块：
  - `android` → 包含 `:androidApp`
  - `ios` → 包含 `:composeApp`（iOS 目标）
  - `desktop` → 包含 `:composeApp`（JVM 目标）
  - `js` → 包含 `:composeApp`（JS 目标）
  - `wasmJs` → 包含 `:composeApp`（WASM 目标）
  - `server` → 包含 `:server`

### 2. composeApp/build.gradle.kts
- 条件性地配置平台目标：
  - `android` → 配置 `android {}` 块
  - `ios` → 配置 `iosTargets()` 和 `cocoapods {}` 块
  - `desktop` → 配置 JVM 相关依赖
  - `js` → 配置 `js {}` 块
  - `wasmJs` → 配置 `wasmJs {}` 块

### 3. run.sh
- 支持 `-PenabledPlatforms` 参数
- 自动传递到 Gradle 命令

## 注意事项

1. **核心模块始终加载**：所有 `core:*` 和 `feature:*`、`component:*` 模块始终加载，不受平台限制
2. **配置优先级**：命令行参数 > gradle.properties
3. **默认行为**：如果未设置 `enabledPlatforms`，加载所有平台（保持向后兼容）
4. **IDE 同步**：修改配置后需要重新同步 Gradle 项目

## 使用示例

### 只开发 Android
```properties
# gradle.properties
enabledPlatforms=android
```

### 只开发 Desktop
```properties
# gradle.properties
enabledPlatforms=desktop
```

### 同时开发 Android 和 Desktop
```properties
# gradle.properties
enabledPlatforms=android,desktop
```

### 临时切换（不修改配置文件）
```bash
./gradlew build -PenabledPlatforms=android
./scripts/run.sh android -PenabledPlatforms=android
```

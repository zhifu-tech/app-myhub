# App Build Config

应用构建配置模块，提供变体维度的构建配置。

## 模块结构

```
core/app-build-config/
├── src/
│   ├── commonMain/          # 基础配置接口和主配置对象
│   ├── devMain/             # 开发环境配置实现
│   ├── prodMain/            # 生产环境配置实现
│   ├── freeMain/            # 免费版配置实现
│   ├── premiumMain/         # 高级版配置实现
│   ├── googlePlayMain/      # Google Play 渠道配置实现
│   └── channelMain/         # 默认渠道配置实现
```

## 配置维度

### 环境维度（Environment）

- `devMain`: 开发环境配置
- `prodMain`: 生产环境配置

### 级别维度（Tier）

- `freeMain`: 免费版配置
- `premiumMain`: 高级版配置

### 渠道维度（Channel）

- `googlePlayMain`: Google Play 渠道配置
- `channelMain`: 默认渠道配置

## 使用方式

```kotlin
import tech.zhifu.app.myhub.config.AppBuildConfig

// 获取变体维度
val env = AppBuildConfig.appEnv        // "dev" 或 "prod"
val tier = AppBuildConfig.appTier      // "free" 或 "premium"
val channel = AppBuildConfig.appChannel // "googlePlay" 或 "channel"

// 获取配置属性
val appName = AppBuildConfig.APP_NAME
val enableLogging = AppBuildConfig.enableLogging
val enableDebugFeatures = AppBuildConfig.enableDebugFeatures
```

## 构建变体

### 方式 1：通过命令行参数设置（推荐用于临时构建）

```bash
# 开发环境 + 免费版
./gradlew build -PappEnv=dev -PappTier=free

# 生产环境 + 高级版 + Google Play 渠道
./gradlew build -PappEnv=prod -PappTier=premium -PappChannel=googlePlay
```

### 方式 2：通过 gradle.properties 文件设置（推荐用于默认配置）

在项目根目录的 `gradle.properties` 文件中配置：

```properties
# 构建变体配置
appEnv=dev
appTier=free
appChannel=channel
```

### 优先级说明

配置的优先级从高到低：
1. **命令行参数** `-PappEnv=prod`（最高优先级）
2. **gradle.properties** 文件（项目级配置）
3. **~/.gradle/gradle.properties** 文件（用户级配置）
4. **代码中的默认值**（`dev`, `free`, `channel`）

**注意：** `local.properties` 文件不用于构建变体配置，它仅用于 Android SDK 路径等本地配置。

### 默认值

- `appEnv`: 默认为 `dev`
- `appTier`: 默认为 `free`
- `appChannel`: 默认为 `channel`

## 设计说明

使用委托模式（Delegation Pattern）组合各个维度的配置：

- `AppBuildEnvConfig`: 环境维度接口
- `AppBuildTierConfig`: 级别维度接口
- `AppBuildChannelConfig`: 渠道维度接口

各变体目录通过提供具体的实现类来覆盖默认值。

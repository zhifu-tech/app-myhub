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

通过 Gradle 属性设置变体：

```bash
# 开发环境 + 免费版
./gradlew build -PappEnv=dev -PappTier=free

# 生产环境 + 高级版 + Google Play 渠道
./gradlew build -PappEnv=prod -PappTier=premium -PappChannel=googlePlay
```

## 设计说明

使用委托模式（Delegation Pattern）组合各个维度的配置：
- `AppBuildEnvConfig`: 环境维度接口
- `AppBuildTierConfig`: 级别维度接口
- `AppBuildChannelConfig`: 渠道维度接口

各变体目录通过提供具体的实现类来覆盖默认值。

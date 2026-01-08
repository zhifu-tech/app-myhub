# 构建变体配置说明

## 📋 概述

项目支持多维度独立构建变体系统，每个维度都是独立的：

1. **环境维度（Environment）**：开发环境（dev） / 生产环境（prod）
2. **版本维度（Tier）**：免费版（free） / 付费版（premium）
3. **渠道维度（Channel）**：googlePlay、umeng、vivo、oppo、xiaomi、huawei 等（可选）

### 变体参数

变体参数通过独立的 Gradle 属性设置，使用 `-P` 格式：

```bash
# 基本构建（使用默认值）
./gradlew build

# 指定环境
./gradlew build -PappEnv=prod

# 指定版本
./gradlew build -PappTier=premium

# 指定渠道
./gradlew build -PappChannel=googlePlay

# 组合使用（所有参数独立）
./gradlew build -PappEnv=prod -PappTier=premium -PappChannel=googlePlay
```

### 使用统一管理脚本

项目提供了统一管理脚本 `scripts/run.sh`，支持相同的 `-P` 格式参数：

```bash
# 运行桌面应用
./scripts/run.sh desktop -PappEnv=prod -PappTier=premium -PappChannel=googlePlay

# 构建并安装 Android 应用
./scripts/run.sh android -PbuildType=release -PappEnv=prod -PappTier=premium

# 构建所有模块
./scripts/run.sh build -PappEnv=prod -PappTier=premium -PappChannel=googlePlay
```

**注意：** 变体参数是独立的，不存在组合逻辑。每个维度单独判断和注入目录。详细说明请参考 [MyHub KMP 变体系统使用指南](myhub-kmp-variant-guide.md)。

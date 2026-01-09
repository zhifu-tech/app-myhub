# iOS App 配置说明

本文档说明 MyHub iOS 应用的配置、构建和运行方式。

## 📋 目录结构

```
iosApp/
├── iosApp/                    # iOS 应用源代码
│   ├── iOSApp.swift          # 应用入口
│   ├── ContentView.swift     # 主视图
│   └── Assets.xcassets/      # 资源文件
├── iosApp.xcodeproj/         # Xcode 项目文件
├── iosApp.xcworkspace/       # CocoaPods 工作空间（重要！）
├── Podfile                   # CocoaPods 依赖配置
├── Podfile.lock              # 依赖版本锁定文件（应该提交）
├── Pods/                     # CocoaPods 依赖（自动生成，不应提交）
├── Configuration/            # 配置文件
│   └── Config.xcconfig       # Xcode 配置
└── scripts/                  # 工具脚本
    ├── check-cocoapods.sh    # CocoaPods 环境检查脚本
    ├── fix-linker-errors.sh  # 链接器错误修复脚本
    └── DSYM_EXPLANATION.md   # dSYM 说明文档
```

## 🚀 快速开始

### 前置要求

- **Xcode 14.0+**（推荐最新版本）
- **CocoaPods**（用于依赖管理）
- **iOS 15.0+** 部署目标

### 安装 CocoaPods（如果尚未安装）

#### 方法 1: 使用 RubyGems（推荐）

```bash
sudo gem install cocoapods
```

#### 方法 2: 使用 Homebrew（macOS）

```bash
brew install cocoapods
```

#### 验证安装

```bash
# 检查 CocoaPods 是否安装
pod --version

# 检查 pod 命令路径
which pod
```

如果命令未找到，可能需要：

1. **检查 PATH 环境变量**：

   ```bash
   echo $PATH
   # 确保包含 /usr/local/bin 或 /opt/homebrew/bin
   ```

2. **重新加载 shell 配置**：

   ```bash
   # 对于 zsh
   source ~/.zshrc

   # 对于 bash
   source ~/.bash_profile
   ```

3. **如果使用 Homebrew 安装**：
   ```bash
   # 确保 Homebrew 的 bin 目录在 PATH 中
   echo 'export PATH="/opt/homebrew/bin:$PATH"' >> ~/.zshrc
   source ~/.zshrc
   ```

### 初始化 CocoaPods 依赖

推荐使用项目脚本（会自动处理环境变量配置）：

```bash
# 使用默认配置（从 gradle.properties 读取）
./scripts/run.sh pod install

# 通过参数指定配置
./scripts/run.sh pod install -PappChannel=umeng -PappEnv=dev
./scripts/run.sh pod install -PappChannel=googlePlay -PappEnv=prod
```

或者使用 Gradle 任务：

```bash
# 在项目根目录执行
./gradlew :composeApp:podInstall
```

或者手动执行：

```bash
cd iosApp
pod install
```

⚠️ **注意**：Podfile 会根据配置自动安装不同的依赖：
- `googlePlay` 渠道：安装 FirebaseCore, FirebaseAnalytics
- `umeng` 渠道：安装 UMCommon, UMDevice
- 其他渠道：仅安装基础依赖（composeApp）

配置优先级（从高到低）：
1. 脚本参数（`-PappChannel=xxx`）
2. gradle.properties 文件

### 打开项目

⚠️ **重要**：必须使用 `.xcworkspace` 文件打开项目，而不是 `.xcodeproj`！

```bash
# 方式 1: 使用命令行
open iosApp/iosApp.xcworkspace

# 方式 2: 使用项目脚本
./scripts/run.sh ios

# 方式 3: 在 Xcode 中手动打开
# File > Open > 选择 iosApp/iosApp.xcworkspace
```

## 🔧 CocoaPods 集成

### 为什么使用 CocoaPods？

本项目使用 **CocoaPods** 来管理 iOS 平台的依赖，特别是：

1. **Kotlin Multiplatform Framework 集成**：`composeApp` 模块通过 CocoaPods 集成到 iOS 项目
2. **Firebase 集成**：在 `googlePlay` 渠道下，FirebaseCore 通过 CocoaPods 自动集成
3. **依赖管理**：统一管理所有 iOS 原生依赖

### Podfile 配置

Podfile 已重构为专业的 iOS 开发风格，使用清晰的代码结构和配置管理：

```ruby
# CocoaPods Podfile for MyHub iOS App
# 
# Configuration:
# - Environment variables (highest priority): APP_CHANNEL, APP_ENV
# - Fallback: gradle.properties file

source 'https://cdn.cocoapods.org/'

# Constants
IOS_DEPLOYMENT_TARGET = '15.0'
CHANNEL_GOOGLE_PLAY = 'googlePlay'
CHANNEL_UMENG = 'umeng'
ENV_DEV = 'dev'

# Configuration Helpers
def read_gradle_property(file_path, property_name)
  return nil unless File.exist?(file_path)
  content = File.read(file_path)
  pattern = /^#{Regexp.escape(property_name)}\s*=\s*(\S+?)(?:\s*#|$)/
  match = content.match(pattern)
  match ? match[1].strip : nil
end

def get_build_config
  gradle_props_path = File.join(__dir__, '..', 'gradle.properties')
  channel = ENV['APP_CHANNEL'] || read_gradle_property(gradle_props_path, 'appChannel')
  env = ENV['APP_ENV'] || read_gradle_property(gradle_props_path, 'appEnv')
  
  {
    channel: channel,
    env: env,
    is_google_play: channel == CHANNEL_GOOGLE_PLAY,
    is_umeng: channel == CHANNEL_UMENG,
    is_dev: env == ENV_DEV
  }
end

target 'iosApp' do
  use_frameworks!
  platform :ios, IOS_DEPLOYMENT_TARGET
  
  # Kotlin Multiplatform Framework
  pod 'composeApp', :path => '../composeApp'
  
  # Build configuration
  config = get_build_config
  
  # Firebase Analytics (Google Play channel only)
  if config[:is_google_play]
    pod 'FirebaseCore'
    pod 'FirebaseAnalytics'
  end
  
  # Umeng Analytics (Umeng channel only)
  if config[:is_umeng]
    pod 'UMCommon'   # Statistics SDK base library
    pod 'UMDevice'   # Device information collection
  end
  
  post_install do |installer|
    # Unify iOS deployment target to 15.0 for all pods
    installer.pods_project.targets.each do |target|
      target.build_configurations.each do |config|
        current_target = config.build_settings['IPHONEOS_DEPLOYMENT_TARGET']
        if current_target.to_f < IOS_DEPLOYMENT_TARGET.to_f
          config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = IOS_DEPLOYMENT_TARGET
        end
      end
    end
    
    # Fix Xcode warning: Add output paths for composeApp build script
    installer.pods_project.targets.each do |target|
      next unless target.name == 'composeApp'
      target.build_phases.each do |phase|
        next unless phase.respond_to?(:name) && phase.name == '[CP-User] Build composeApp'
        framework_path = "${PODS_TARGET_SRCROOT}/../build/cocoapods/framework/ComposeApp.framework"
        phase.output_paths ||= []
        phase.output_paths << framework_path unless phase.output_paths.include?(framework_path)
      end
    end
  end
end
```

**主要特性**：
- ✅ 使用常量定义配置值，易于维护
- ✅ 提取配置读取逻辑到独立方法
- ✅ 清晰的代码结构，符合 iOS 开发规范
- ✅ 支持环境变量和 gradle.properties 两种配置方式
- ✅ 自动根据渠道安装不同的依赖

### Gradle 配置

在 `composeApp/build.gradle.kts` 中配置了 CocoaPods 插件：

```kotlin
cocoapods {
    summary = "MyHub Compose App"
    homepage = "https://github.com/zhifu-tech/app-myhub"
    version = "1.0.0"
    name = "composeApp"

    framework {
        baseName = "ComposeApp"
        // 使用静态链接以解决 Firebase 依赖链接问题
        isStatic = true
    }

    podfile = project.file("../iosApp/Podfile")
    
    // iOS 部署目标（统一为 15.0）
    val iosDeploymentTarget = "15.0"
    ios.deploymentTarget = iosDeploymentTarget
    extraSpecAttributes["ios.deployment_target"] = iosDeploymentTarget

    // 注意：Firebase pods 不需要在 cocoapods 块中手动添加
    // dev.gitlive:firebase-analytics 会自动处理 FirebaseCore 和 FirebaseAnalytics 的 CocoaPods 依赖
    // 在 cocoapods 块中手动添加会导致符号重复定义错误（symbol multiply defined）
    // Firebase pods 应该在 Podfile 中直接添加（见 iosApp/Podfile），这样可以在 Xcode 构建时正确链接
}
```

### 更新依赖

当修改了 `composeApp/build.gradle.kts` 中的 CocoaPods 配置后：

```bash
# 方式1: 使用项目脚本（推荐）
./scripts/run.sh pod install -PappChannel=umeng -PappEnv=dev

# 方式2: 使用 Gradle 任务
./gradlew :composeApp:podInstall

# 方式3: 手动执行
cd iosApp && pod install
```

⚠️ **重要**：如果切换了渠道（如从 `umeng` 切换到 `googlePlay`），需要重新执行 `pod install` 以安装正确的依赖。

## 🏗️ 构建和运行

### 在 Xcode 中构建

1. 打开 `iosApp.xcworkspace`
2. 选择目标设备：
   - **iOS Simulator**（推荐用于开发）
   - **真实 iOS 设备**（需要开发者账号）
3. 点击运行按钮（⌘R）或使用菜单：**Product > Run**

### 使用命令行构建

```bash
# 构建 Framework（由 CocoaPods 自动处理，通常不需要手动执行）
./gradlew :composeApp:syncFramework \
    -Pkotlin.native.cocoapods.platform=iphonesimulator \
    -Pkotlin.native.cocoapods.archs="arm64" \
    -Pkotlin.native.cocoapods.configuration=Debug
```

⚠️ **注意**：`syncFramework` 任务需要从 Xcode 内部调用才能正确推断架构。通常不需要手动执行，CocoaPods 的构建脚本会自动处理。

### 使用项目脚本

```bash
# 打开 Xcode 项目
./scripts/run.sh ios

# 列出可用设备
./scripts/run.sh ios list

# 指定设备类型
./scripts/run.sh ios simulator  # 使用模拟器
./scripts/run.sh ios device     # 使用真机

# 指定渠道和环境
./scripts/run.sh ios -PappChannel=umeng -PappEnv=dev
./scripts/run.sh ios simulator -PappChannel=googlePlay -PappEnv=prod
```

## 🔥 渠道配置和依赖管理

### 支持的渠道

项目支持多个渠道，每个渠道会安装不同的依赖：

| 渠道 | 依赖 | 说明 |
|------|------|------|
| `googlePlay` | FirebaseCore, FirebaseAnalytics | Google Play 应用商店 |
| `umeng` | UMCommon, UMDevice | 友盟统计（国内） |
| 其他 | composeApp（基础） | 默认渠道 |

### 配置方式

#### 方式1: 使用项目脚本（推荐）

```bash
# 使用 googlePlay 渠道
./scripts/run.sh pod install -PappChannel=googlePlay -PappEnv=dev

# 使用 umeng 渠道
./scripts/run.sh pod install -PappChannel=umeng -PappEnv=dev
```

脚本会自动：
- 显示当前配置和将安装的依赖
- 提示配置优先级
- 询问确认后执行安装

#### 方式2: 修改 gradle.properties

在 `gradle.properties` 中设置：

```properties
appChannel=googlePlay  # 或 umeng
appEnv=dev            # 或 prod
```

然后执行：

```bash
./scripts/run.sh pod install
# 或
./gradlew :composeApp:podInstall
```

### Firebase 集成

Firebase 仅在 `googlePlay` 渠道启用。要使用 Firebase：

1. 使用脚本安装（推荐）：
   ```bash
   ./scripts/run.sh pod install -PappChannel=googlePlay
   ```

2. 或在 `gradle.properties` 中设置：
   ```properties
   appChannel=googlePlay
   ```
   然后执行 `./scripts/run.sh pod install`

### Firebase 配置

Firebase 配置通过 `GoogleService-Info.plist` 文件管理。该文件需要：

1. 从 [Firebase Console](https://console.firebase.google.com/) 下载
2. 放置到 `iosApp/iosApp/` 目录
3. 添加到 Xcode 项目中

⚠️ **注意**：`GoogleService-Info.plist` 文件包含敏感信息，不应提交到版本控制系统。

### Umeng 集成

Umeng（友盟）仅在 `umeng` 渠道启用。要使用 Umeng：

1. 使用脚本安装（推荐）：
   ```bash
   ./scripts/run.sh pod install -PappChannel=umeng
   ```

2. 或在 `gradle.properties` 中设置：
   ```properties
   appChannel=umeng
   ```
   然后执行 `./scripts/run.sh pod install`

**注意**：
- `UMCCommonLog` 已禁用，因为它在 iOS 模拟器上存在架构兼容性问题
- 如需调试日志，可以使用 `UMCommon` 自带的日志功能（通过 `UMConfigure.setLogEnabled`）
- 或在真机上测试

## 📱 应用配置

### Bundle Identifier

- **开发环境**：`tech.zhifu.app.myhub.dev`
- **生产环境**：`tech.zhifu.app.myhub`

### 签名配置

在 Xcode 中配置代码签名：

1. 选择项目 > `iosApp` target
2. 进入 **Signing & Capabilities** 标签
3. 确保：
   - ✅ `Automatically manage signing` 已勾选
   - ✅ 选择了正确的 **Team**
   - ✅ **Bundle Identifier** 正确

### 部署目标

- **最低 iOS 版本**：iOS 15.0
- **推荐 iOS 版本**：iOS 16.0+

## 🐛 常见问题

### Q1: 找不到 `iosApp.xcworkspace` 文件

**原因**：CocoaPods 依赖未安装。

**解决方案**：

```bash
# 推荐：使用项目脚本
./scripts/run.sh pod install

# 或使用 Gradle 任务
./gradlew :composeApp:podInstall

# 或手动执行
cd iosApp && pod install
```

### Q1.1: CocoaPods executable not found in your PATH

**原因**：

1. CocoaPods 未安装
2. `pod` 命令不在 PATH 环境变量中
3. Gradle 执行时无法找到 `pod` 命令

**解决方案**：

#### 方案 1: 检查 CocoaPods 是否安装

```bash
# 检查 CocoaPods 版本
pod --version

# 如果命令未找到，安装 CocoaPods
sudo gem install cocoapods
# 或
brew install cocoapods
```

#### 方案 2: 检查 PATH 环境变量

```bash
# 查看当前 PATH
echo $PATH

# 查找 pod 命令位置
which pod

# 如果找到（例如 /opt/homebrew/bin/pod），确保该路径在 PATH 中
```

#### 方案 3: 修复 PATH（如果使用 Homebrew）

```bash
# 对于 zsh
echo 'export PATH="/opt/homebrew/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# 对于 bash
echo 'export PATH="/opt/homebrew/bin:$PATH"' >> ~/.bash_profile
source ~/.bash_profile
```

#### 方案 4: 在 local.properties 中指定 pod 路径（推荐）

如果 Gradle 仍然找不到 pod，在 `local.properties` 文件中指定：

```properties
# 在项目根目录的 local.properties 中添加
kotlin.apple.cocoapods.bin=/opt/homebrew/bin/pod
# 或
kotlin.apple.cocoapods.bin=/usr/local/bin/pod
```

**注意**：`local.properties` 文件不会被提交到版本控制，适合配置本地环境。

#### 方案 4.1: 在 gradle.properties 中指定 pod 路径（备选）

如果不想使用 `local.properties`，也可以在 `gradle.properties` 中指定：

```properties
# 在项目根目录的 gradle.properties 中添加
org.jetbrains.kotlin.native.cocoapods.podCommand=/opt/homebrew/bin/pod
# 或
org.jetbrains.kotlin.native.cocoapods.podCommand=/usr/local/bin/pod
```

**注意**：`gradle.properties` 会被提交到版本控制，所有开发者都会使用相同的路径。

#### 方案 5: 使用完整路径执行 pod install

```bash
# 找到 pod 的完整路径
which pod

# 使用完整路径执行
/opt/homebrew/bin/pod install
# 或
/usr/local/bin/pod install
```

#### 方案 6: 检查 Ruby 环境

CocoaPods 需要 Ruby，确保 Ruby 已正确安装：

```bash
# 检查 Ruby 版本
ruby --version

# 如果 Ruby 未安装，使用 Homebrew 安装
brew install ruby

# 或使用 rbenv/rvm 管理 Ruby 版本
```

### Q2: 构建失败，提示 "Incompatible 'embedAndSign' Task with CocoaPods Dependencies"

**原因**：尝试使用 `embedAndSignAppleFrameworkForXcode` 任务，但该项目使用 CocoaPods。

**解决方案**：

- 不要手动调用 `embedAndSignAppleFrameworkForXcode` 任务
- Framework 构建由 CocoaPods 的构建脚本自动处理
- 确保使用 `.xcworkspace` 文件打开项目

### Q3: 找不到 Framework 或符号未定义

**原因**：

1. Framework 未正确构建
2. 使用了 `.xcodeproj` 而不是 `.xcworkspace`

**解决方案**：

1. 确保使用 `.xcworkspace` 文件
2. 清理构建：**Product > Clean Build Folder** (⇧⌘K)
3. 重新构建：**Product > Build** (⌘B)

### Q3.1: 链接器错误 "ld invocation reported errors"

**原因**：

1. Framework 架构不匹配（模拟器 vs 真机）
2. Framework 未正确构建或损坏
3. CocoaPods 配置未正确应用
4. 构建缓存问题

**解决方案**：

#### 方案 1: 清理并重新构建 Framework

```bash
# 1. 清理 Gradle 构建
./gradlew :composeApp:clean

# 2. 重新生成 Framework
./gradlew :composeApp:podInstall

# 3. 在 Xcode 中清理构建
# Product > Clean Build Folder (⇧⌘K)
```

#### 方案 2: 检查 Framework 架构

确保 Framework 的架构与目标设备匹配：

```bash
# 检查 Framework 支持的架构
lipo -info composeApp/build/cocoapods/framework/ComposeApp.framework/ComposeApp

# 应该显示类似：
# Architectures in the fat file: ComposeApp are: arm64 x86_64
```

如果架构不匹配，需要重新构建对应架构的 Framework。

#### 方案 3: 重新安装 CocoaPods 依赖

```bash
cd iosApp

# 删除 Pods 和锁定文件
rm -rf Pods Podfile.lock

# 重新安装
pod install

# 或者使用 Gradle
cd ..
./gradlew :composeApp:podInstall
```

#### 方案 4: 检查 Xcode 构建设置

1. 在 Xcode 中选择项目 > `iosApp` target
2. 进入 **Build Settings**
3. 搜索 "Framework Search Paths"
4. 确保包含：
   - `$(inherited)`
   - `"${PODS_CONFIGURATION_BUILD_DIR}/..."`（由 CocoaPods 自动添加）
   - `"${PODS_ROOT}/../../composeApp/build/cocoapods/framework"`

#### 方案 5: 检查链接器标志

1. 在 **Build Settings** 中搜索 "Other Linker Flags"
2. 确保包含：
   - `$(inherited)`
   - `-framework ComposeApp`
   - **注意**：由于 `composeApp` 使用静态 Framework（`isStatic = true`），SQLite3 符号已经包含在 Framework 中，因此不需要在 Xcode 项目中再次链接 `-lsqlite3`，避免重复链接警告

#### 方案 6: 删除 Derived Data

```bash
# 删除 Xcode Derived Data
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# 或者在 Xcode 中：
# Xcode > Settings > Locations > Derived Data > 点击箭头打开，删除相关文件夹
```

#### 方案 7: SQLite3 符号未定义错误

如果遇到 `Undefined symbols for architecture arm64: "_sqlite3_*"` 错误：

**原因**：CocoaPods 构建的 Framework（`podDebugFramework`/`podReleaseFramework`）没有链接 SQLite3 库。

**解决方案**：

1. **检查配置**：确保 `composeApp/build.gradle.kts` 中已配置：
   ```kotlin
   iosTargets().forEach { iosTarget ->
       iosTarget.binaries.framework {
           linkerOpts += listOf("-lsqlite3")
       }
       // 为 CocoaPods 构建的 Framework 也添加 SQLite3 链接
       iosTarget.binaries.all {
           if (this is org.jetbrains.kotlin.gradle.plugin.mpp.Framework) {
               linkerOpts += listOf("-lsqlite3")
           }
       }
   }
   ```

2. **检查 podspec**：确保 `composeApp.podspec` 中包含：
   ```ruby
   spec.libraries = 'c++', 'sqlite3'
   ```

3. **注意**：由于 `composeApp` 使用静态 Framework（`isStatic = true`），SQLite3 符号已经包含在 Framework 中，因此：
   - **不需要**在 Xcode 项目的 `Config.xcconfig` 中添加 `-lsqlite3`
   - 添加会导致重复链接警告：`ld: warning: ignoring duplicate libraries: '-lsqlite3'`
   - 如果遇到符号未定义错误，检查 Framework 构建配置，而不是 Xcode 项目的链接器标志

4. **清理并重新构建**：
   ```bash
   ./gradlew :composeApp:clean
   ./gradlew :composeApp:podInstall
   ./gradlew :composeApp:linkPodDebugFrameworkIosArm64
   ```

#### 方案 8: 检查构建日志

在 Xcode 中查看详细的构建日志：

1. 打开 **Report Navigator** (⌘9)
2. 选择最新的构建
3. 展开失败的链接步骤
4. 查看具体的错误信息

常见错误信息及解决方案：

- **"library not found"**: Framework 路径不正确，检查 Framework Search Paths
- **"symbol(s) not found"**: Framework 未正确链接，检查 Other Linker Flags
- **"architecture mismatch"**: Framework 架构与目标不匹配，重新构建对应架构
- **"duplicate symbols"**: 重复链接，检查是否有多个地方链接了同一个 Framework
- **"ignoring duplicate libraries: '-lsqlite3'"**: SQLite3 被重复链接，对于静态 Framework，移除 Xcode 项目中的 `-lsqlite3` 链接器标志（参考方案 7）
- **"Undefined symbols for architecture arm64: _sqlite3_*"**: SQLite3 未链接，参考方案 7
- **"Undefined symbols for architecture arm64: _GUL*" 或 "_FIR*"**: Firebase 依赖未正确链接，参考 Q4.1

### Q4.1: Firebase 链接器错误（Undefined symbols）

**错误信息**：
```
Undefined symbols for architecture arm64:
  "_GULIsLoggableLevel", referenced from: ...
  "_FIRInstallationIDDidChangeNotification", referenced from: ...
```

**原因**：
Kotlin Native 在链接动态 Framework 时，可能无法正确链接 Firebase 的传递依赖（如 `GoogleUtilities`）。

**解决方案**：

1. **使用静态链接**（推荐）：
   在 `composeApp/build.gradle.kts` 中，将 Framework 设置为静态链接：
   ```kotlin
   framework {
       baseName = "ComposeApp"
       isStatic = true  // 改为静态链接
   }
   ```

2. **确保添加了所有必要的 Firebase pods**：
   ```kotlin
   if (project.isChannel("googlePlay")) {
       pod("FirebaseCore")
       pod("FirebaseAnalytics")
   }
   ```

3. **清理并重新构建**：
   ```bash
   ./gradlew :composeApp:clean
   ./gradlew :composeApp:podInstall
   ./gradlew :composeApp:linkPodDebugFrameworkIosArm64
   ```

### Q4: Firebase 初始化失败

**原因**：

1. 未在 `googlePlay` 渠道下构建
2. `GoogleService-Info.plist` 文件缺失或配置错误

**解决方案**：

1. 确保使用 `-PappChannel=googlePlay` 构建
2. 检查 `GoogleService-Info.plist` 文件是否存在且配置正确
3. 重新安装 Pods：`cd iosApp && pod install`

### Q5: iOS 部署目标版本错误

**错误信息**：
```
The iOS deployment target 'IPHONEOS_DEPLOYMENT_TARGET' is set to 9.0, but the range of supported deployment target versions is 12.0 to 26.0.99.
```

**原因**：某些 CocoaPods 依赖的默认部署目标版本过低（如 9.0），不在 Xcode 支持的范围内。

**解决方案**：

1. **确保 Podfile 中的 `post_install` 钩子已正确配置**：
   ```ruby
   post_install do |installer|
     # 统一设置所有 pods 的 iOS 部署目标为 15.0
     installer.pods_project.targets.each do |target|
       target.build_configurations.each do |config|
         if config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'].to_f < 15.0
           config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '15.0'
         end
       end
     end
   end
   ```

2. **重新安装 Pods**：
   ```bash
   cd iosApp
   pod install
   ```

3. **验证部署目标**：
   ```bash
   grep -r "IPHONEOS_DEPLOYMENT_TARGET" Pods/Pods.xcodeproj/project.pbxproj | head -5
   ```
   应该显示所有配置都是 `15.0`

### Q6: 设备选择器显示 Mac 而不是 iOS 设备

**原因**：Xcode 选择了错误的运行目标。

**解决方案**：

1. 在 Xcode 顶部工具栏，点击设备选择器
2. 选择 **iOS Simulator**（如 iPhone 15 Pro）或真实的 iOS 设备
3. **不要选择 Mac 设备**

## 📝 开发注意事项

### 1. 始终使用 `.xcworkspace`

⚠️ **重要**：使用 CocoaPods 的项目必须使用 `.xcworkspace` 文件打开，直接打开 `.xcodeproj` 会导致依赖无法找到。

### 2. Framework 构建

Kotlin Multiplatform Framework 的构建由 CocoaPods 的构建脚本自动处理，无需手动干预。构建脚本位于 `Pods/Target Support Files/composeApp/` 目录。

### 3. 依赖更新

当修改了 `composeApp/build.gradle.kts` 中的 CocoaPods 配置后，需要：

```bash
./gradlew :composeApp:podInstall
```

### 4. 清理构建

如果遇到构建问题，尝试：

1. 在 Xcode 中：**Product > Clean Build Folder** (⇧⌘K)
2. 删除 `Pods/` 目录和 `Podfile.lock`
3. 重新安装：`pod install`

### 5. 版本控制

以下文件/目录应该提交到版本控制：

- ✅ `Podfile`
- ✅ `Podfile.lock`（锁定依赖版本）
- ✅ `iosApp.xcworkspace/`（工作空间配置）

以下文件/目录不应提交：

- ❌ `Pods/`（依赖文件，可通过 `pod install` 重新生成）
- ❌ `iosApp.xcworkspace/xcuserdata/`（用户特定配置）
- ❌ `*.xcuserstate`（Xcode 用户状态）

## 🔗 相关文档

- [项目主 README](../README.md)
- [架构设计文档](../docs/myhub_architecture.md)
- [FAQ](../docs/FAQ.md)
- [CocoaPods 官方文档](https://guides.cocoapods.org/)
- [Kotlin Multiplatform CocoaPods 文档](https://kotlinlang.org/docs/multiplatform-cocoapods.html)

## 🔧 工具脚本

### CocoaPods 环境检查

如果遇到 "CocoaPods executable not found" 错误，先运行检查脚本：

```bash
# 检查 CocoaPods 环境
./iosApp/scripts/check-cocoapods.sh
```

该脚本会检查：

1. CocoaPods 是否安装
2. PATH 环境变量配置
3. Ruby 和 gem 环境
4. Gradle 配置
5. 提供修复建议

### 链接器错误修复

如果遇到链接器错误，可以使用快速修复脚本：

```bash
# 运行修复脚本
./iosApp/scripts/fix-linker-errors.sh
```

该脚本会自动：

1. 清理 Gradle 构建
2. 检查 Framework 状态
3. 重新生成 Framework
4. 可选：清理并重新安装 CocoaPods 依赖

## 📞 获取帮助

如果遇到问题：

1. 查看 [FAQ](../docs/FAQ.md)
2. 检查 [GitHub Issues](https://github.com/zhifu-tech/app-myhub/issues)
3. 联系开发团队

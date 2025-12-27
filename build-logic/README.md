# Build Logic 模块

这个模块包含了项目的构建约定插件，用于统一管理构建配置。

## 结构

```
build-logic/
├── settings.gradle.kts          # build-logic 模块的设置文件
├── build.gradle.kts              # build-logic 根构建文件
├── gradle/
│   └── libs.versions.toml       # 版本目录（与根项目同步）
└── convention/                   # 约定插件子模块
    ├── build.gradle.kts
    └── src/main/kotlin/.../convention/
        ├── kotlin-multiplatform.gradle.kts      # Kotlin Multiplatform 基础配置
        ├── android-kotlin-multiplatform.gradle.kts  # Android KMP 配置
        ├── compose-multiplatform.gradle.kts     # Compose Multiplatform 配置
        └── kotlin-serialization.gradle.kts      # Kotlin 序列化配置
```

## 约定插件

### kotlin-multiplatform
提供 Kotlin Multiplatform 的基础配置：
- 应用默认的层次模板
- 配置 JVM、JS、iOS 目标平台

### android-kotlin-multiplatform
提供 Android Kotlin Multiplatform 库的配置：
- 配置 Android 命名空间（需要设置 `android.namespace` 属性）
- 设置编译 SDK 和最小 SDK
- 配置 JVM 目标版本

### compose-multiplatform
提供 Compose Multiplatform 的配置：
- 配置 Compose 资源
- 设置资源包名（可通过 `compose.resources.package` 属性自定义）

### kotlin-serialization
提供 Kotlin 序列化插件的配置。

## 使用方法

在模块的 `build.gradle.kts` 中应用约定插件：

```kotlin
plugins {
    id("tech.zhifu.app.myhub.convention.kotlin-multiplatform")
    id("tech.zhifu.app.myhub.convention.android-kotlin-multiplatform")
    id("tech.zhifu.app.myhub.convention.compose-multiplatform")
    id("tech.zhifu.app.myhub.convention.kotlin-serialization")
}
```

## 注意事项

1. `android-kotlin-multiplatform` 插件需要在项目中设置 `android.namespace` 属性：
   ```kotlin
   // 在 gradle.properties 或 build.gradle.kts 中
   android.namespace = "tech.zhifu.app.myhub.example"
   ```

2. `compose-multiplatform` 插件可以通过 `compose.resources.package` 属性自定义资源包名，默认为项目组名。

3. 版本目录文件 `gradle/libs.versions.toml` 需要与根项目的版本目录保持同步。


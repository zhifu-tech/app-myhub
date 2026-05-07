# build-logic 和 settings.gradle.kts 参数传递方案

## 问题

`settings.gradle.kts` 在 `build-logic` 编译之前执行，无法直接共享代码和变量。

## 解决方案

### 方案1: Gradle Extra Properties（推荐）

在 `settings.gradle.kts` 中设置，在 `build-logic` 中读取。

#### settings.gradle.kts

```kotlin
// 解析平台配置
val enabledPlatforms = getEnabledPlatforms()

// 通过 extra 属性传递给 build-logic
gradle.ext.set("myhub.enabledPlatforms", enabledPlatforms)
```

#### build-logic 中读取

```kotlin
fun Project.getEnabledPlatformsFromSettings(): Set<String>? {
    return project.rootProject.extensions.extraProperties.get("myhub.enabledPlatforms") as? Set<String>
}
```

### 方案2: 系统属性

在 `settings.gradle.kts` 中设置系统属性。

#### settings.gradle.kts

```kotlin
val enabledPlatforms = getEnabledPlatforms()
System.setProperty("myhub.enabledPlatforms", enabledPlatforms.joinToString(","))
```

#### build-logic 中读取

```kotlin
fun getEnabledPlatformsFromSystem(): Set<String>? {
    val platformsStr = System.getProperty("myhub.enabledPlatforms") ?: return null
    return platformsStr.split(",").map { it.trim().lowercase() }.toSet()
}
```

### 方案3: 共享文件

在 `settings.gradle.kts` 中写入文件，在 `build-logic` 中读取。

#### settings.gradle.kts

```kotlin
val enabledPlatforms = getEnabledPlatforms()
val cacheFile = file(".gradle/myhub-platforms.cache")
cacheFile.parentFile.mkdirs()
cacheFile.writeText(enabledPlatforms.joinToString(","))
```

#### build-logic 中读取

```kotlin
fun getEnabledPlatformsFromFile(project: Project): Set<String>? {
    val cacheFile = project.rootProject.file(".gradle/myhub-platforms.cache")
    if (!cacheFile.exists()) return null
    val platformsStr = cacheFile.readText()
    return platformsStr.split(",").map { it.trim().lowercase() }.toSet()
}
```

### 方案4: 统一从 gradle.properties 读取（当前方案）

两者都从 `gradle.properties` 读取，使用全局缓存。

**优点：**

- 简单直接
- 无需额外传递
- 逻辑一致

**缺点：**

- 无法在 settings 阶段修改后传递给 build-logic

## 推荐方案

对于平台加载场景，推荐使用**方案1（Gradle Extra Properties）**：

1. 在 `settings.gradle.kts` 中解析并缓存
2. 通过 `gradle.ext` 传递给 build-logic
3. `PlatformLoadingUtils` 优先从 `gradle.ext` 读取，如果没有则从 `gradleProperty` 读取

这样既保证了性能（只解析一次），又保证了数据一致性。

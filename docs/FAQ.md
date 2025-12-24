# 常见问题解答 (FAQ)

本文档包含项目开发过程中遇到的常见问题及解决方案。

---

## iOS 开发问题

### Q1: TLS 错误导致安全连接失败

**问题描述：**  
Xcode 与 Apple 服务器通信时出现 TLS/SSL 证书验证失败。

**解决方案：**

#### 方案 1: 检查网络和代理设置

```bash
# 检查网络连接
ping developer.apple.com

# 如果使用代理，检查代理设置
echo $http_proxy
echo $https_proxy
```

#### 方案 2: 重新登录 Apple ID

1. 打开 Xcode
2. 进入 `Xcode` > `Settings` (或 `Preferences`)
3. 选择 `Accounts` 标签
4. 移除当前的 Apple ID，然后重新添加
5. 确保选择了正确的 Team (R9JS3478MA)

#### 方案 3: 清理 Xcode 缓存

```bash
# 清理 DerivedData
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# 清理 Archives
rm -rf ~/Library/Developer/Xcode/Archives/*

# 清理 Provisioning Profiles 缓存
rm -rf ~/Library/MobileDevice/Provisioning\ Profiles/*

# 重启 Xcode
```

#### 方案 4: 更新 Xcode 和证书

1. 确保 Xcode 是最新版本
2. 在 Xcode 中：`Xcode` > `Settings` > `Accounts` > 选择你的账号 > `Download Manual Profiles`
3. 或者运行：

```bash
# 更新证书
security find-identity -v -p codesigning
```

---

### Q2: Provisioning Profile 不包含当前设备

**错误信息：**

```
Provisioning profile "iOS Team Provisioning Profile: tech.zhifu.app.myhub.MyHub"
doesn't include the currently selected device "Zhifu's MacBook Pro"
(identifier 00006030-001638A234D0001C).
```

**原因分析：**  
**Mac 设备不应该作为 iOS 应用的运行目标**。这个错误通常发生在：

- Xcode 错误地将 Mac 识别为运行设备
- 项目配置了 Mac Catalyst 但未正确设置
- 设备选择器选择了错误的设备类型

**解决方案：**

#### 方案 1: 选择正确的运行目标（推荐）

1. 在 Xcode 顶部工具栏，点击设备选择器（显示 "Zhifu's MacBook Pro" 的地方）
2. 选择以下之一：
   - **iOS Simulator**（如 iPhone 15 Pro, iPad Pro 等）
   - **真实的 iOS 设备**（如果已连接）
3. **不要选择 Mac 设备**

#### 方案 2: 检查项目设置

1. 在 Xcode 中选择项目文件
2. 选择 `iosApp` target
3. 进入 `Signing & Capabilities` 标签
4. 确保：
   - ✅ `Automatically manage signing` 已勾选
   - ✅ `Team` 设置为正确的团队 (R9JS3478MA)
   - ✅ `Bundle Identifier` 为 `tech.zhifu.app.myhub.MyHub`

#### 方案 3: 如果确实需要在 Mac 上运行（Mac Catalyst）

如果项目需要支持 Mac，需要额外配置：

1. 在 `Signing & Capabilities` 中：

   - 添加 `Mac (Designed for iPad)` capability
   - 或启用 `Mac Catalyst`

2. 更新 `project.pbxproj` 中的配置：

   ```pbxproj
   SUPPORTS_MACCATALYST = YES;
   SUPPORTS_MAC_DESIGNED_FOR_IPAD_IPHONE = YES;
   ```

#### 方案 4: 手动刷新 Provisioning Profiles

```bash
# 在 Xcode 中
# Xcode > Settings > Accounts > [你的账号] > Download Manual Profiles

# 或使用命令行
xcrun altool --list-providers
```

---

### Q3: 为什么 Mac 不能作为 iOS 运行设备？

**回答：**  
iOS 应用需要在 iOS 设备或 iOS 模拟器上运行。Mac 需要 Mac Catalyst 或 "Designed for iPad" 配置才能在 Mac 上运行。

---

### Q4: 如何添加设备到 Provisioning Profile？

**回答：**  
如果使用自动签名，Xcode 会自动管理。如果使用手动签名，需要在 [Apple Developer Portal](https://developer.apple.com/account) 中添加设备 UDID。

---

### Q5: TLS 错误持续出现怎么办？

**回答：**  
尝试以下方法：

1. 检查防火墙/代理设置
2. 使用 VPN 或更换网络
3. 联系网络管理员检查企业网络限制
4. 更新 macOS 和 Xcode 到最新版本

---

## 相关资源

- [Apple Developer Documentation - Code Signing](https://developer.apple.com/documentation/security/code_signing_services)
- [Xcode Help - Managing Signing](https://help.apple.com/xcode/mac/current/#/dev60b6fbbc7)
- [Troubleshooting Code Signing Issues](https://developer.apple.com/forums/tags/code-signing)
- [iOS Framework: 静态库 vs 动态库](./iOS_FRAMEWORK_STATIC_VS_DYNAMIC.md) - 详细说明静态库和动态库的区别及选择标准

---

## Kotlin Multiplatform 测试问题

### Q1: 为什么在 Kotlin/JS 测试中，`@BeforeTest` 里的 `runTest {}` 不会等待完成？

**问题描述：**  
在 Kotlin/JS 或 wasmJs 测试环境中，`@BeforeTest` 中使用 `runTest {}` 时，测试方法可能在数据库初始化完成之前就开始执行，导致测试失败。

**原因分析：**

1. **JS 没有"阻塞线程"这回事**：JS 是单线程 + event loop，`runTest {}` 返回 Promise，但测试框架不会等待它完成
2. **`@BeforeTest` 在 JS 是"fire-and-forget"**：`@BeforeTest` 不能是 suspend，返回值不会被 await，coroutine 直接丢进 event loop
3. **平台行为差异**：JVM/Android 上 `runTest` 会阻塞直到完成，但 JS/wasmJs 上 `runTest` 启动协程后立即返回

**项目中的解决方案：**

使用 `runDatabaseTest` 辅助函数，它已经处理了平台差异，自动创建和清理数据库：

```kotlin
@Test
fun `test database schema creation`() = runDatabaseTest { database ->
    val result = database.cardQueries.selectAll().awaitAsList()
    assertEquals(0, result.size)
}
```

**优点：**

- ✅ 平台一致：JVM、Android、iOS、JS 都使用相同写法
- ✅ 自动管理：自动创建和清理数据库，无需 `@BeforeTest` / `@AfterTest`
- ✅ 避免 JS 平台问题：不依赖测试框架等待 coroutine 完成

---

## Kotlin Multiplatform 构建问题

### Q1: `android.experimental.kmp.enableAndroidResources` 参数是什么？什么时候需要开启？

**问题描述：**  
在 KMP 项目的 `androidLibrary` 配置中，遇到 Compose Resources 无法加载的错误：
```
MissingResourceException: Missing resource with path: composeResources/...
```

**一句话结论：**

> **`android.experimental.kmp.enableAndroidResources`**  
> 👉 用来**决定 KMP 的 Android sourceSet 是否支持 Android 资源系统（R、res、Manifest 合并）**  
> **默认：关闭（false）**  
> **打开后：KMP Android sourceSet 才能像普通 Android Module 一样用 res/**

**什么时候需要开启？**

✅ **需要开启的场景：**
- shared 模块包含 `res/` 或 `composeResources/`
- shared 模块包含 Compose UI（Android 端）
- shared 模块是 Android UI Library

❌ **不需要开启的场景：**
- shared 模块只是数据层/业务逻辑（SQLDelight、ViewModel 等）
- Android UI 在独立的 `androidApp` 模块中

**判断口诀：**  
> **shared 里有没有 `res/`？**
> - 有 → 打开
> - 没有 → 不要打开

**配置方法：**

在 `composeApp/build.gradle.kts` 的 `androidLibrary` 块中：

```kotlin
kotlin {
    androidLibrary {
        // ... 其他配置
        
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
        
        // ... 其他配置
    }
}
```

**常见误解：**

- ❌ "打开这个就能生成 APK？" → 不对，它只影响资源系统
- ❌ "不打开就不能用 Android API？" → 可以用，只是不能用资源
- ❌ "所有 KMP 项目都应该打开？" → 不对，这是 UI 级别的能力

### Q2: 如何决定 JVM 目标版本？

**问题描述：**  
在配置 Kotlin 编译选项时，如何选择合适的 JVM 目标版本（如 `JVM_11`、`JVM_17`）？

**决定因素（按优先级）：**

#### 1. Android 平台的最低要求

| Android 版本 | 最低支持的 Java 版本 | 推荐 JVM Target |
|------------|------------------|---------------|
| Android 7.0 (API 24) | Java 8 | JVM_1_8 |
| Android 8.0 (API 26) | Java 8 | JVM_1_8 |
| Android 9.0 (API 28) | Java 8 | JVM_1_8 |
| Android 10+ (API 29+) | Java 8+ | JVM_11 或更高 |

**你的项目：**
- `minSdk = 24` → 最低需要 Java 8
- `targetSdk = 36` → 可以使用 Java 11 或更高

#### 2. Android Gradle Plugin (AGP) 的要求

| AGP 版本 | 最低 Java 版本 | 推荐 JVM Target |
|---------|-------------|---------------|
| AGP 7.x | Java 11 | JVM_11 |
| AGP 8.x | Java 17 | JVM_17 或 JVM_11（向后兼容）|

**你的项目：**
- `agp = "8.13.2"` → 最低需要 Java 17，但可以设置为 JVM_11（向后兼容）

#### 3. 依赖库要求

检查主要依赖库的最低 Java 版本要求。常见库通常支持 Java 8+：
- Compose Multiplatform → 支持 Java 8+
- Kotlin → 支持 Java 8+
- Ktor → 支持 Java 8+
- SQLDelight → 支持 Java 8+

#### 4. 项目统一性

- ✅ 所有模块应使用相同的 JVM 目标版本
- ✅ `compileOptions` 和 `jvmTarget` 必须一致

**配置示例：**

```kotlin
// Android Application 模块
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)  // 必须与 compileOptions 一致
        }
    }
}

// KMP Android Library 模块
kotlin {
    androidLibrary {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)  // 保持一致
        }
    }
}
```

**当前项目推荐：JVM_11**

- ✅ 兼容 minSdk 24+
- ✅ AGP 8.x 兼容
- ✅ 主要依赖库都支持

**何时升级到 JVM_17？**

- AGP 9.x+（未来版本可能要求）
- 需要使用 Java 17+ 特性
- 依赖库要求 Java 17+

---

## 其他问题

如果您遇到其他问题，请：

1. 查看项目 [README.md](../README.md)
2. 检查 [build-logic/README.md](../build-logic/README.md) 了解构建配置
3. 提交 Issue 到项目仓库
